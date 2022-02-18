package orm.schema.columns;

public class PrimaryKeyColumn extends SimpleColumn{
    public PrimaryKeyColumn(){
        super("id", "INT");
    }
}

