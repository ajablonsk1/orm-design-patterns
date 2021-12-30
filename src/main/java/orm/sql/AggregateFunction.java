package orm.sql;

public enum AggregateFunction {
    COUNT,
    MAX,
    MIN,
    SUM,
    AVG;

    public String toString(String columm) {
        switch (this) {
            case COUNT -> {
                return "COUNT("+columm+")";
            }
            case MAX -> {
                return "MAX("+columm+")";
            }
            case MIN -> {
                return "MIN("+columm+")";
            }
            case SUM -> {
                return "SUM("+columm+")";
            }
            case AVG -> {
                return "AVG("+columm+")";
            }
            default -> {
                return "INCORRECT("+columm+")";
            }
        }
    }
}
