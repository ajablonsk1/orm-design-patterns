package orm.sql;

public enum CommandType {
    DROP,
    CREATE,
    ALTER,
    INSERT,
    UPDATE,
    DELETE,
    SELECT,
    SET;

    @Override
    public String toString() {
        switch (this) {
            case DROP -> {
                return "DROP TABLE";
            }
            case CREATE -> {
                return "CREATE TABLE";
            }
            case ALTER -> {
                return "ALTER TABLE";
            }
            case INSERT -> {
                return "INSERT INTO";
            }
            case UPDATE -> {
                return "UPDATE";
            }
            case DELETE -> {
                return "DELETE FROM";
            }
            case SELECT -> {
                return "SELECT";
            }
            case SET -> {
                return "SET";
            }
            default -> {return "INCORRECT";}
        }
    }
}
