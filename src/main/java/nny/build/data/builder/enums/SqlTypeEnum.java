package nny.build.data.builder.enums;


/**
 * 数据库字段数据类型
 *
 * @author shengyong.huang
 * @date 2020-02-24
 */
public enum SqlTypeEnum {

    STRING("STRING", "VARCHAR"),
    INTEGER("INTEGER", "INT"),
    Boolean("Boolean", "BOOLEAN"),
    LONG("LONG", "INTEGER"),
    DATE("DATE", "DATETIME"),
    DOUBLE("DOUBLE", "DECIMAL"),
    Currency("Currency", "VARCHAR"),
    DICTIONARY("DICTIONARY", "VARCHAR"),
    TIMESTAMP("TIMESTAMP", "TIMESTAMP"),
    Maps("Maps", "VARCHAR"),
    Lists("Lists", "VARCHAR"),
    ListItem("ListItem", "VARCHAR");

    private String name;
    private String type;

    SqlTypeEnum(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public static String getType(String name) {
        name = name.toUpperCase();
        for (SqlTypeEnum sqlTypeEnum : values()) {
            if (sqlTypeEnum.name.equals(name)) {
                return sqlTypeEnum.type;
            }
        }
        return "VARCHAR";
    }

    public static SqlTypeEnum convertSqlType(String type) {
        type = type.toUpperCase();
        for (SqlTypeEnum sqlTypeEnum : values()) {
            if (sqlTypeEnum.type.equals(type)) {
                return sqlTypeEnum;
            }
        }
        return SqlTypeEnum.STRING;
    }
}
