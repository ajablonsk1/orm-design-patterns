###Aktualny podział pracy (do 13.12.2021):
- Diagramy i opisy modułów:
- - Session - Paweł
- - Relacje, adnotacje - Piotrek
- - Builder, Executor, Scanner, ConnectionPool - Gabriel
- Dziedziczenie, generowanie ID - Adrian

### Postać dokumentacji:
1. Krótki opis bibloteki (co robi i jak)
2. Użyte technologie (Java, MySQL)
3. Architektura fizyczna
4. Architektura logiczna
5. Diagram biblioteki w UML (z jakich modułów się składa i jakie są między nimi powiązania)
6. Diagramy poszczególnych modułów wraz z opisem
7. Sposób obsługi dziedziczenia oraz generowania kluczy głównych
8. Lista zastosowanych wzorców projektowych wraz z diagramami, opisem i uzasadnieniem
9. Przykłady użycia biblioteki przez klienta (użycie adnotacji, użycie Session, diagram sekwencji)
10. Lista używanych adnotacji z opisem

Wszystkie diagramy umieszczone są w katalogu diagrams, wraz ze źródłem w XML lub innym formacie pozwalającym na edycję.

Wzorce:
- Builder - QueryBuilder
- Fabryka - SessionFactory
- Unit of Work - Session
- Identity Map - Session
- Class Table Inheritance - dziedziczenie
- być może Query Object do obsługi zapytań z parametrami, by zabezpieczyć się przed SQL Injection

Identity Map jako Map<Class, Map<Integer, Object>>  
Generowanie ID - ID unikalne na całą bazę, baza przechowuje tabelę z jednym wierszem zawierającym następne dostępne ID  
Dziedziczenie - Class Table Inheritance, tabela z polami klasy dziedziczącej zawiera FK na wiersz klasy nadrzędnej  
OneToOne - Adnotacja posiada atrybut "this"/"other", tabela klasy z "this" zawiera kolumnę z FK  
OneToMany - FK w tabeli po stronie Many  
ManyToMany - Tablica łącznikowa, adnotacja powinna posiadać atrybut z nazwą klasy łącznikowej  

Algorytm load:
1. sprawdzic czy obiekt jest w identity map i jezeli jest to go zwracamy, jesli nie:
2. bierzemy wszystkie klasy nadrzedne wzgledem danej klasy, robimy selecta i za pomoca joina (foreign key) zbieramy wszystkie potrzebne dane do stwworzenia obiektu, tworzymy pusty obiekt (potrzebny pusty konstruktor) i wsadzamy go do identity map
3. uzyskujemy liste adnotacji @OneToOne(foreignKey="this"/"other") -> po stronie this znajduje sie foreign key
4. @OneToMany - to bedzie kolekcja, wyszukujemy w tabeli 'Many' wszyztkich rekordow ktore maja foreign key taki jak primary key 'One', dla kazdego id elementów wykonujemy load
5. @ManyToMany(attr="name - nazwa tablicy łącznikowej") - wchodzimy w tabele łącznikową i wyciagmy id, i przechodzimy do tablicy z drugim 'Many' i tworzymy obiekty z takim id, dla kazdego id elementów wykonujemy load
6. przy kazdym sprawdzamy czy KAZDY loadowany obiekt o danym id nie znajduje sie juz w identity map - jezeli to go stamtad zwracamy

Algorytm save:
0. Dodajemy do identity map, jezeli jest to go nie zapisujemy
1. Dla inhritace -> sprawdzamy wszyskti klasy nadrzedne i dla kazdej klasy tworzymy osobny wiersz z polami zadeklarowanymi w danej klasie zaczynajac od gory, dla kazdego wiersza nizej tworzymy foreigh key wskazujacy na wiersz wyzej
2. @OneToOne, @OneToMany, @ManyToMany -> sprawdzamy czy obiekty nie jest zapisany, jesli nie to go zapisujemy, jesli tak to go updatujemy

Algorytm delete:
1. Sprawdzamy czy obiekt jest w identity map, jezeli go nie ma to nie robimy nic, jezeli jest to go bierzemy
2. CASCADE: Przechodzimy przez adnotacje i w zaleznosci od kaskady: usuwamy wszystkie powiazane obiekty albo dajemy nulle (do ustalenia jaka opcja)
3. Przy inheritance wywalamy wszystko od dołu po kolei do góry

Algorytm update:
1. usuwamy wszystko i wstawiamy od nowa

Do ustalenia:
1. Delete przyjmujący argument, określający czy usuwanie ma być kaskadowe, czy nie
2. Czy algorytm update jest akceptowalny
3. Wydzielenie tworzenia tabel z ClassScanner do osobnej klasy, ClassScanner zajmowałby się jedynie szukaniem pól, adnotacji, klas nadrzędnych
4. Co to znaczy "konfiguracja modelu encji dziedziny powinna realizować paradygmat „by exception”"

Stara dokumentacja - https://docs.google.com/document/d/1ATEsbiftdQEcgce-aWxOYNcQAnRioykP7WYx5PvhvUk/edit
Jamboardy:
1. https://jamboard.google.com/d/1Gdy-kuHveB5x6WmK6X0u2UBXLGTieq6YyzuTVxKJy1g/viewer?usp=sharing
2. https://jamboard.google.com/d/1r5wh0uyWfTVD-rnwy8idFrn-nBij1oT1veAJuiA57rU/viewer
---

Stare README.md

## Wykorzystanie adnotacji do oznaczenia klas przeznaczonych do persystencji do utworzenia odpowiednich tabel w bazie.

1. Odczytujemy z pliku konfiguracyjnego nazwy z klas.
2. Otwieramy połączenie z bazą danych
3. Dropujemy wszystkie tabele
4. Przechodzimy po wszystkich klasach pierwszy raz:
- tworzymy tabele i kolumny na podstawie klas i atrybutów, na razie bez foreign keys
- sprawdzamy czy klasa o zadanej nazwie posiada adnotacje “Entity”
- iterujemy i zapisujemy pola (nazwy typów).
- SQLBuilder tworzy zapytanie/polecenie sql. (wzorzec builder)
- Executor dodaje tabele do bazy danych
- Zapisujemy relacje w zbiorze
5. Na podstawie zbioru relacji używamy ponownie SQLBuildera do stworzenia relacji

SQLStringBuilder udostępnia metody:
- createTable(class) // tworzy pustą tabelę
- addOneToOneRelation(classWithFK, classWithoutFK)
- addManyToOne(classWithFK, classWithoutFK)
- addManyToManyRelation(class1, class2)
- updateTable(object)

Jak uniknąć dwukrotnego tworzenia tej samej relacji - trzymamy globalny zbiór wszystkich relacji.

Dodajemy kolumny w SQL przez ALTER TABLE

## Adnotacje, których będziemy używać:
Klasowe:
- @Entity

Pól:
- @Column
- @Id
- @OneToOne(foreignKey = this/other): this - ForeignKey pochodzi z tego obiektu, zapisywany jest w tabeli odpowiadającej drugiemu obiektowi
- @OneToMany - foreignKey trzymamy w drugiej tabeli
- @ManyToOne - foreignKey trzymamy w tej tabeli
- @ManyToMany - tworzymy tabele łącznikową

## Wzorce:
- Builder - do tworzenia zapytania SQL
- Command (?) - do UPDATE, DELETE, INSERT

## Problemy do rozwiązania później:
- Co jeśli pojawi się @Entity w klasie zagnieżdżonej?
- Reprezentacja relacji One-to-One, One-to-Many, Many-to-Many.
- Reprezentacja dziedziczenia na trzy sposoby z możliwością wyboru sposobu.
- Implementacja operacji CRUD na samych obiektach.
