package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.ManyToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Tag {
    @Id
    private int id;
    @ManyToMany(tableName = "post_tags")
    public List<Post> posts = new ArrayList<>();
    @Column
    public String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
