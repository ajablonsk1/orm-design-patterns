package orm.schema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Schema {
    private final Map<String, Set<SimpleColumn>> tables;
    private boolean idTableExists;

    public Schema() {
        tables = new HashMap<>();
    }

    public Set<SimpleColumn> addTable(Class<?> clazz){
        Set<SimpleColumn> columns = new HashSet<>();
        this.tables.put(clazz.getSimpleName().toLowerCase(), columns);
        return columns;
    }

    public Set<SimpleColumn> getTable(Class<?> clazz){
        return this.tables.get(clazz.getSimpleName().toLowerCase());
    }

    public Map<String, Set<SimpleColumn>> getTables() {
        return tables;
    }
    
    public boolean isIdTableExists() {
        return idTableExists;
    }

    public void setIdTableExists(boolean idTableExists) {
        this.idTableExists = idTableExists;
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
