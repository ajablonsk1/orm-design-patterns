package orm.test;

import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {

    @Id int id;

    @OneToMany
    List<Item> items = new ArrayList<>();

    public Cart() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
