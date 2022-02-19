package orm.schema.columns;

import java.util.Locale;
import java.util.Objects;

public class SimpleColumn {
    protected String name;
    protected String type;

    public SimpleColumn(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleColumn column)) return false;
        return name.equals(column.name) && type.equals(column.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), type.toLowerCase());
    }
}
