<div text-align="center"><h1>Framework realizujący mapowanie OR klas w języku Java</h1></div>
<p align=center>Piotr Makarewicz, Gabriel Kępka, Adrian Jabłoński, Paweł Hołowicki</p>

## 1. Opis biblioteki
Framework realizuje podstawowe mapowanie obiektowo-relacyjne, pozwala na wykonywanie operacji CRUD na obiektach utrwalając ich stan w bazie danych lub 
odczytując je z niej, określanie relacji między obiektami różnych klas (One to One, One to Many, Many to Many) oraz obsługę dziedziczenia. 
Wykorzystuje mechanizm adnotacji, pozwalając oznaczyć jakie klasy mają być persystowane w bazie danych, wartości których pól mają być zapisywane oraz określić relacje między różnymi obiektami.
Pakiety zawierające klasy modelu dziedzinowego są określone w pliku konfiguracyjnym.

## 2. Użyte technologie  
- Java
- MySQL


## 3. Architektura fizyczna

<img src="./diagrams/architektura_fizyczna.drawio.png">

Framework umożliwia persystencję na więcej niż jednym serwerze baz danych.

## 4. Architektura logiczna

<img src="./diagrams/architektura_logiczna.drawio.png">

Framework umożliwia persystencję danych w wielu bazach na raz. Z każdą bazą powiązany jest osobny obiekt klasy *Session*.
Architektura logiczna zakłada istnienie tylko jednego modelu dziedzinowego w ramach pojedynczej aplikacji korzystającej z naszego frameworka.
Do tego modelu należą wszystkie klasy z adnotacją *@Entity* w obrębie aplikacji.

## 5. Diagram modułów biblioteki

## 6. Moduły

### 6.1. orm.session

```
<miejsce na diagram>
```

Klasy:
 - *Session* - **opis klasy**
 - inne klasy...

### 6.2. orm.scanner

```
<miejsce na diagram>
```

Klasy:
 - *ClassScanner* - **opis klasy**
 - *ClassFinder* - **opis klasy**
 -  inne klasy...

### 6.3. orm.sqlbuilder

```
<miejsce na diagram>
```

**Klasy:**
 - *SqlBuilder* - **opis klasy**
 - inne klasy...

### 6.4. orm.annotations

```
<miejsce na diagram>
```

**Adnotacje:**
 - *@Entity* - z opisami!
 - *@Column*
 - *@OneToOne*(foreignKey="this"/"other")
 - *@OneToMany*
 - *@ManyToOne*
 - *@ManyToMany*

## 7. Zastosowane wzorce projektowe

### 7.1. Unit of Work

```
<miejsce na diagram klas, które realizują ten wzorzec>
```

Zaimplementowany w klasie UnitOfWork, agregowanej przez Session. Klasa UnitOfWork utrzymuje trzy zbiory: obiektów do aktualizacji, obiektów do dodania oraz obiektów do usunięcia. Unit of Work pozwoli na optymalizację transakcji przez usunięcie operacji redundantnych i zwiększenie wydajności systemu.

### 7.2. Identity Map

```
<miejsce na diagram klas, które realizują ten wzorzec>
```

miejsce na opis

### 7.3. Class Table Inheritance 

```
<miejsce na diagram klas, które realizują ten wzorzec>
```

Framework obsługuje dziedziczenie za pomocą wzorca projektowego **Class Table Inheritance**, gdzie dla każdej klasy z osobna tworzy tabele, w której każdy wiersz odpowiada konkretnym polom w danej klasie. 

<img src="./diagrams/ClassTableInheritance.png">

Każdy obiekt, odpowiadający rekordowi w bazie, posiada własne unikalne id. W bazie istnieje tabela, w której przechowywana jest informacja o następnym wolnym id. Nowy obiekt otrzymuje to id, a następnie wcześniej wspomniana tabela jest aktualizowana.

### 7.4. Foreign Key Mapping

```
<miejsce na diagram klas, które realizują ten wzorzec>
```

Framework obsługuje tworzenie relacji jeden-do-jeden i jeden-do-wielu za pomocą wzorca projektowego **Foreign Key Mapping**.

miejsce na dalszy opis

### 7.5. Association Table Mapping
```
<miejsce na diagram klas, które realizują ten wzorzec>
```

Framework obsługuje tworzenie relacji wiele-do-wielu za pomocą wzorca projektowego **Assotiation Table Mapping**. Tworzy tabelę łącznikową dla każdej pary klas tworzących tę relację.

miejsce na dalszy opis

### 7.6. Factory i Singleton
```
<miejsce na diagram klas, które realizują ten wzorzec>
```

Zaimplementowane razem w klasie SessionFactory. Służą zapewnieniu globalnej kontroli nad obiektami klasy Session. SessionFactory pilnuje, by nie było więcej niż jednej sesji podłączonej do konkretnej bazy danych. Implementacja Singletona opiera się na *lazy-loading* i podwójnym sprawdzeniu istnienia obiektu w metodzie *getInstance()* w celu zapewnienia bezpieczeństwa w programach wielowątkowych.

miejsce na dalszy opis

### 7.7. Builder

```
<miejsce na diagram klas, które realizują ten wzorzec>
```

Służy ułatwieniu tworzenia różnych typów zapytań SQL.

## 8. Przykłady użycia biblioteki przez klienta

Klasa reprezentująca encję:
```
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
```
SessionFactory sessionFactory = SessionFactory.getInstance();
Session session = sessionFactory.createSession();

Person person = new Person();
person.setName(”John”);

session.save(person);
session.flush();
session.close();
```

Załadowanie obiektu z bazy i jego aktualizacja:
```
SessionFactory sessionFactory = SessionFactory.getInstance();
Session session = sessionFactory.createSession();

Person person = session.load(Person.class, 2);
person.setName(”Json”);

session.update(person);
session.flush();
session.close();
```
