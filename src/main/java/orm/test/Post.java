package orm.test;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.ManyToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Post {
    @Id
    public int id;
    @ManyToMany(tableName = "post_tags")
    public List<Tag> tags = new ArrayList<>();
    @Column
    public String name;
}
