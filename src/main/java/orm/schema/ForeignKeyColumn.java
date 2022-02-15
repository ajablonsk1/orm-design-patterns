package orm.schema;

public class ForeignKeyColumn extends SimpleColumn {
    private String refTable;

    public ForeignKeyColumn(String name, String refTable) {
        super(name, "INT");
        this.refTable = refTable;
    }

    public String getRefTable() {
        return refTable;
    }

    public void setRefTable(String refTable) {
        this.refTable = refTable;
    }
}
