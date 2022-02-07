package orm.test;

import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.ManyToOne;

@Entity
public class Item {

    @Id int id;
    @ManyToOne Cart cart;

    public Item() {
    }

    public int getId() {
        return id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}
