package orm.schema.columns;

import java.util.Objects;

public class ForeignKeyColumn extends SimpleColumn {
    private final String refTable;
    private final String table;

    public ForeignKeyColumn(String name, String table, String refTable) {
        super(name, "INT");
        this.refTable = refTable;
        this.table = table;
    }

    public String getRefTable() {
        return refTable;
    }

    public String getFkConstraintName() {
        return ForeignKeyColumn.getConstraintName(this.table, this.name, this.refTable);
    }

    public static String getConstraintName(String table, String column, String refTable){
        return "fk_" + table + "_" + column + "_" + refTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ForeignKeyColumn that = (ForeignKeyColumn) o;
        return refTable.equals(that.refTable) && table.equals(that.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), refTable.toLowerCase(), table.toLowerCase());
    }
}
