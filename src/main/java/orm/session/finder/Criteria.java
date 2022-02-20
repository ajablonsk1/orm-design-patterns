package orm.session.finder;


import orm.sql.QueryBuilder;

public class Criteria {

    private String op;
    private String field;
    private Object value;
    private Criteria lft;
    private Criteria rhs;
    private String SQL;

    private Criteria(String op, String field, Object value) {
        this.op = op;
        this.field = field;
        this.value = value;
    }

    public Criteria(String op,Criteria lft, Criteria rhs) {
        this.op = op;
        this.lft = lft;
        this.rhs = rhs;
    }
    public Criteria(String op,String sqlQuery){
        this.op = op;
        this.SQL = sqlQuery;
    }

    //greater  than
    public static Criteria gt(String fieldName,Object value){
        return new Criteria(">",fieldName,value);
    }


    //greather than or equal
    public static Criteria ge(String fieldName,Object value){
        return new Criteria(">=",fieldName,value);

    }
    //less than
    public static Criteria lt(String fieldName,Object value){
        return new Criteria("<",fieldName,value);
    }
    //less than or equal
    public static Criteria le(String fieldName,double value){
        return new Criteria("<=",fieldName,value);
    }
    //equal
    public static Criteria eq(String fieldName,String value){
        return new Criteria("=",fieldName,value);
    }
    public static Criteria eq(String fieldName,Object value){
        return new Criteria("=",fieldName,value);
    }
    //not equal
    public static Criteria ne(String fieldName,String value){
        return new Criteria("!=",fieldName,value);
    }
    public static Criteria isNull(String fieldName){
        return new Criteria("=",fieldName,null);

    }
    public static Criteria isNotNull(String fieldName){
        return new Criteria("!=",fieldName,null);

    }
    public static Criteria and(Criteria lft , Criteria rhs){
        return new Criteria("AND",lft,rhs);

    }
    public static Criteria or(Criteria lft , Criteria rhs){
        return new Criteria("or",lft,rhs);
    }
    public static Criteria sqlRestriction(String sqlRestriction){
        return new Criteria("SQL",sqlRestriction);
    }
    public String generateSql(){
        if (op.equals(">")){
            return field + ">" + value;
        }
        if (op.equals("=") && value != null){
            return field + "=" + "'" + value+"'";
        }
        if (op.equals("=")){
            return field + "="  + null;
        }
        if (op.equals(">=")){
            return field + ">=" + value;
        }
        if (op.equals("<")){
            return field + "<" + value;
        }
        if (op.equals("<=")){
            return field + "<=" + value;
        }
        if (op.equals("!=") && value != null){
            return field + "!=" + value;
        }
        if (op.equals("!=")){
            return field + "!=" + "'" + null +"'";
        }
        if (op.equals("or")){
            return  "("+lft.generateSql() + ") OR (" + rhs.generateSql() +")" ;
        }
        if (op.equals("AND")){
            return  "("+lft.generateSql() + ") AND (" + rhs.generateSql() +")" ;
        }
        if (op.equals("SQL")){
            return SQL;
        }
        throw new IllegalStateException();

    }

}
