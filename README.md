1. Wykorzystanie adnotacji do oznaczenia klas przeznaczonych do persystencji do utworzenia odpowiednich tabel w bazie.

Odczytujemy z pliku konfiguracyjnego nazwy z klas.
Otwieramy połączenie z bazą danych
Dropujemy wszystkie tabele
Przechodzimy po wszystkich klasach pierwszy raz - tworzymy tabele i kolumny na podstawie klas i atrybutów, na razie bez foreign keys:
Sprawdzamy czy klasa o zadanej nazwie posiada adnotacje “Entity”
Iterujemy i zapisujemy pola (nazwy typy) 
SQLBuilder tworzy zapytanie/polecenie sql. (wzorzec builder)
Executor dodaje tabele do bazy danych.
zapisujemy relacje w zbiorze
Usuwamy ze zbioru relacje niepoprawne
Na podstawie zbioru używamy ponownie SQLBuildera do stworzenia relacji

SQLStringBuilder:
createTable(class) // tworzy pustą tabelę
addOneToOneRelation(classWithFK, classWithoutFK)
addManyToOne(classWithFK, classWithoutFK)
addManyToManyRelation(class1, class2)
updateTable(object)

Jak uniknąć dwukrotnego tworzenia tej samej relacji - trzymamy globalny zbiór wszystkich relacji.

Dodajemy kolumny w SQL przez ALTER TABLE

Adnotacje, których będziemy używać:
Klasowe:
@Entity
Pól:
@Column
@Id
@OneToOne(foreignKey = this/other): this - ForeignKey pochodzi z tego obiektu, zapisywany jest w tabeli odpowiadającej drugiemu obiektowi
@OneToMany - foreignKey trzymamy w drugiej tabeli
@ManyToOne - foreignKey trzymamy w tej tabeli
@ManyToMany - tworzymy tabele łącznikową

Wzorce:
Builder - do tworzenia zapytania SQL
Command (?) - do UPDATE, DELETE, INSERT

Problemy do rozwiązania później:
Co jeśli pojawi się @Entity w klasie zagnieżdżonej?
Reprezentacja relacji One-to-One, One-to-Many, Many-to-Many.
Reprezentacja dziedziczenia na trzy sposoby z możliwością wyboru sposobu.
Implementacja operacji CRUD na samych obiektach.
