<div text-align="center"><h1>Framework realizujący mapowanie OR klas w języku Java</h1></div>
<p align=center>Piotr Makarewicz, Gabriel Kępka, Adrian Jabłoński, Paweł Hołowicki</p>

### 1. Opis biblioteki
Framework realizuje podstawowe mapowanie obiektowo-relacyjne, pozwala na wykonywanie operacji CRUD na obiektach utrwalając ich stan w bazie danych lub 
odczytując je z niej, określanie relacji między obiektami różnych klas (One to One, One to Many, Many to Many) oraz obsługę dziedziczenia. 
Wykorzystuje mechanizm adnotacji, pozwalając oznaczyć jakie klasy mają być persystowane w bazie danych, wartości których pól mają być zapisywane oraz określić relacje między różnymi obiektami.
Pakiety zawierające klasy modelu dziedzinowego są określone w pliku konfiguracyjnym.

### 2. Użyte technologie  
- Java
- MySQL


### 3. Architektura fizyczna

<img src="./diagrams/architektura_fizyczna.drawio.png">

Framework umożliwia persystencję na więcej niż jednym serwerze baz danych.

### 4. Architektura logiczna

<img src="./diagrams/architektura_logiczna.drawio.png">

Framework umożliwia persystencję danych w wielu bazach na raz. Z każdą bazą powiązany jest osobny obiekt klasy Session.
Architektura logiczna zakłada istnienie tylko jednego modelu dziedzinowego w ramach pojedynczej aplikacji korzystającej z naszego frameworka.
Do tego modelu należą wszystkie klasy z adnotacją @Entity w obrębie aplikacji.

### 5. Diagram modułów biblioteki

### 6. Moduły

### 7. Sposób obsługi dziedziczenia i generowania kluczy głównych

### 8. Zastosowane wzorce projektowe

### 9. Przykłady użycia biblioteki przez klienta

Klasa reprezentująca encję:
```aidl
@Entity
public class Person {

    @Id
    private Long id;

    @Column
    private String name;

    public Person() {
    }
}
```

Zapisanie obiektu w bazie:
```aidl
SessionFactory sessionFactory = SessionFactory.getInstance();
Session session = sessionFactory.createSession();

Person person = new Person();
person.setName(”John”);

session.save(person);
session.flush();
session.close();
```

Załadowanie obiektu z bazy i jego aktualizacja:
```aidl
SessionFactory sessionFactory = SessionFactory.getInstance();
Session session = sessionFactory.createSession();

Person person = session.load(Person.class, 2);
person.setName(”Json”);

session.update(person);
session.flush();
session.close();
```

### 10. Lista używanych adnotacji
