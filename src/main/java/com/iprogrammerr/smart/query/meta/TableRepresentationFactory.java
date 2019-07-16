package com.iprogrammerr.smart.query.meta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableRepresentationFactory {

    private static final String PACKAGE_PREFIX = "package";
    private static final List<String> IMPORTS = Arrays.asList("import java.sql.ResultSet;", "import java.util.List;",
        "import java.util.ArrayList;");
    private static final String CONSTANTS_MODIFIED = "public static final";
    private static final String CLASS_PREFIX = "public class";
    private static final String START_CURLY_BRACKET = "{";
    private static final String END_CURLY_BRACKET = "}";
    private static final String TAB = "\t";
    private static final String DOUBLE_TAB = TAB + TAB;
    private static final String NEW_LINE = "\n";
    private static final String EMPTY_LINE = NEW_LINE + NEW_LINE;
    private static final String FIELD_MODIFIER = "public final";
    private static final String START_BRACKET = "(";
    private static final String END_BRACKET = ")";
    private static final String SEMICOLON = ";";
    private static final String PUBLIC_MODIFIER = "public";
    private static final String COMMA = ",";
    private static final String DOT = ".";
    private static final String EQUAL = " = ";
    private static final String THIS = "this";
    private static final String TABLE = "TABLE";
    private static final String STRING = "String";
    private static final String FACTORIES_MODIFIER = "public static";
    private static final String FACTORY_NAME = "fromResult";
    private static final String LIST_FACTORY_NAME = "fromListResult";
    private static final String RESULT_SET = "ResultSet";
    private static final String RESULT_SET_ARG = "result";
    private static final String THROWS_EXCEPTION = "throws Exception";
    private static final String FACTORY_ARG_SUFFIX = "Label";
    private static final String LIST_NAME = "list";
    private static final String INITIALIZED_LIST = "new ArrayList<>();";
    private final String packageName;

    public TableRepresentationFactory(String packageName) {
        this.packageName = packageName;
    }

    public String newRepresentation(MetaData data) {
        return new StringBuilder()
            .append(header(data.className))
            .append(fields(data))
            .append(constructor(data.className, data.fieldsTypes))
            .append(factories(data))
            .append(NEW_LINE).append(END_CURLY_BRACKET)
            .toString();
    }

    private String header(String className) {
        StringBuilder builder = new StringBuilder()
            .append(PACKAGE_PREFIX).append(" ").append(packageName).append(SEMICOLON)
            .append(EMPTY_LINE);
        for (String i : IMPORTS) {
            builder.append(i).append(NEW_LINE);
        }
        return builder.append(NEW_LINE)
            .append(CLASS_PREFIX).append(" ").append(className).append(" ").append(START_CURLY_BRACKET)
            .append(EMPTY_LINE).toString();
    }

    private String fields(MetaData data) {
        StringBuilder builder = new StringBuilder();
        builder.append(constant(TABLE, data.tableName));
        for (String l : data.columnsLabels) {
            builder.append(constant(l.toUpperCase(), l.toLowerCase()));
        }
        builder.append(NEW_LINE);
        for (Map.Entry<String, String> e : data.fieldsTypes.entrySet()) {
            builder.append(TAB).append(FIELD_MODIFIER).append(" ").append(e.getValue())
                .append(" ").append(e.getKey()).append(SEMICOLON).append(NEW_LINE);
        }
        builder.append(NEW_LINE);
        return builder.toString();
    }

    private String constant(String name, String value) {
        return new StringBuilder().append(TAB).append(CONSTANTS_MODIFIED)
            .append(" String ").append(name)
            .append(" = ").append("\"").append(value).append("\"")
            .append(SEMICOLON).append(NEW_LINE)
            .toString();
    }

    private String constructor(String className, Map<String, String> fieldsTypes) {
        StringBuilder builder = new StringBuilder();
        builder.append(TAB).append(PUBLIC_MODIFIER).append(" ").append(className).append(START_BRACKET);
        for (Map.Entry<String, String> e : fieldsTypes.entrySet()) {
            builder.append(e.getValue()).append(" ").append(e.getKey()).append(COMMA).append(" ");
        }
        int length = builder.length();
        builder.replace(length - 2, length, END_BRACKET);
        builder.append(" ").append(START_CURLY_BRACKET);
        for (String f : fieldsTypes.keySet()) {
            builder.append(NEW_LINE).append(DOUBLE_TAB)
                .append(THIS).append(DOT).append(f).append(EQUAL).append(f).append(SEMICOLON);
        }
        return builder.append(NEW_LINE).append(TAB).append(END_CURLY_BRACKET).toString();
    }

    private String factories(MetaData data) {
        return new StringBuilder()
            .append(EMPTY_LINE)
            .append(singleAliasedFactory(data))
            .append(EMPTY_LINE)
            .append(singleFactory(data))
            .append(EMPTY_LINE)
            .append(aliasedListFactory(data))
            .append(EMPTY_LINE)
            .append(listFactory(data))
            .toString();
    }

    private String singleAliasedFactory(MetaData data) {
        StringBuilder builder = new StringBuilder();
        builder.append(factoryPrefix(false, data.className))
            .append(aliasedFactoryArgs(data.fieldsTypes))
            .append(END_BRACKET).append(" ").append(THROWS_EXCEPTION)
            .append(" ").append(START_CURLY_BRACKET).append(NEW_LINE);
        for (Map.Entry<String, String> e : data.fieldsTypes.entrySet()) {
            builder.append(fieldInitialization(e.getValue() + " " + e.getKey(),
                resultSetInvocation(e.getValue(), aliased(e.getKey()))))
                .append(NEW_LINE);
        }
        builder.append(DOUBLE_TAB).append("return new ").append(data.className).append(START_BRACKET);
        for (String f : data.fieldsTypes.keySet()) {
            builder.append(f).append(COMMA).append(" ");
        }
        int length = builder.length();
        builder.replace(length - 2, length, "");
        return builder.append(END_BRACKET).append(SEMICOLON)
            .append(NEW_LINE).append(TAB).append(END_CURLY_BRACKET)
            .toString();
    }

    private String aliasedFactoryArgs(Map<String, String> fieldsTypes) {
        StringBuilder builder = new StringBuilder()
            .append(RESULT_SET).append(" ").append(RESULT_SET_ARG);
        for (String f : fieldsTypes.keySet()) {
            builder.append(COMMA).append(" ").append(STRING).append(" ").append(aliased(f));
        }
        return builder.toString();
    }

    private List<String> aliasedArgsNames(Map<String, String> fieldsTypes) {
        return fieldsTypes.keySet().stream().map(this::aliased).collect(Collectors.toList());
    }

    private String factoryPrefix(boolean list, String className) {
        StringBuilder builder = new StringBuilder()
            .append(TAB).append(FACTORIES_MODIFIER).append(" ");
        if (list) {
            builder.append(list(className)).append(" ").append(LIST_FACTORY_NAME);
        } else {
            builder.append(className).append(" ").append(FACTORY_NAME);
        }
        return builder.append(START_BRACKET)
            .toString();
    }

    private String list(String className) {
        return String.format("List<%s>", className);
    }

    private String singleFactory(MetaData data) {
        StringBuilder builder = new StringBuilder();
        builder.append(factoryPrefix(false, data.className))
            .append(RESULT_SET).append(" ").append(RESULT_SET_ARG)
            .append(END_BRACKET).append(" ").append(THROWS_EXCEPTION)
            .append(" ").append(START_CURLY_BRACKET).append(NEW_LINE);
        return builder.append(DOUBLE_TAB).append("return ")
            .append(constantsFactoryInvocation(false, data.columnsLabels))
            .append(SEMICOLON).append(NEW_LINE).append(TAB).append(END_CURLY_BRACKET)
            .toString();
    }

    private String constantsFactoryInvocation(boolean list, List<String> columnsLabels) {
        return factoryInvocation(list, RESULT_SET_ARG, columnsLabels.stream().map(String::toUpperCase)
            .collect(Collectors.toList()));
    }

    private String factoryInvocation(boolean list, String arg, List<String> args) {
        StringBuilder builder = new StringBuilder()
            .append(list ? LIST_FACTORY_NAME : FACTORY_NAME).append(START_BRACKET)
            .append(arg);
        for (String a : args) {
            builder.append(COMMA).append(" ").append(a);
        }
        return builder.append(END_BRACKET).toString();
    }

    private String aliased(String name) {
        return name + FACTORY_ARG_SUFFIX;
    }

    private String aliasedListFactory(MetaData data) {
        return new StringBuilder()
            .append(factoryPrefix(true, data.className))
            .append(aliasedFactoryArgs(data.fieldsTypes))
            .append(END_BRACKET).append(" ").append(THROWS_EXCEPTION)
            .append(" ").append(START_CURLY_BRACKET).append(NEW_LINE)
            .append(listMapping(data))
            .append(NEW_LINE).append(TAB).append(END_CURLY_BRACKET)
            .toString();
    }

    private String listFactory(MetaData data) {
        return new StringBuilder()
            .append(factoryPrefix(true, data.className))
            .append(RESULT_SET).append(" ").append(RESULT_SET_ARG)
            .append(END_BRACKET).append(" ").append(THROWS_EXCEPTION)
            .append(" ").append(START_CURLY_BRACKET).append(NEW_LINE)
            .append(DOUBLE_TAB).append("return ")
            .append(factoryInvocation(true, RESULT_SET_ARG, data.columnsLabels.stream()
                .map(String::toUpperCase).collect(Collectors.toList()))).append(";")
            .append(NEW_LINE).append(TAB).append(END_CURLY_BRACKET)
            .toString();
    }

    private String listMapping(MetaData meta) {
        return new StringBuilder()
            .append(DOUBLE_TAB)
            .append(list(meta.className)).append(" ").append(LIST_NAME).append(EQUAL).append(INITIALIZED_LIST)
            .append(NEW_LINE).append(DOUBLE_TAB)
            .append("while (").append(RESULT_SET_ARG).append(DOT).append("next()").append(") ")
            .append(START_CURLY_BRACKET)
            .append(NEW_LINE).append(DOUBLE_TAB).append(TAB)
            .append(LIST_NAME).append(".add(").append(factoryInvocation(false, RESULT_SET_ARG,
                aliasedArgsNames(meta.fieldsTypes))).append(");")
            .append(NEW_LINE).append(DOUBLE_TAB).append(END_CURLY_BRACKET)
            .append(NEW_LINE).append(DOUBLE_TAB).append("return list;")
            .toString();
    }

    private String resultSetInvocation(String type, String key) {
        return new StringBuilder()
            .append(RESULT_SET_ARG).append(DOT).append("get")
            .append(capitalized(type))
            .append(START_BRACKET)
            .append(key)
            .append(END_BRACKET)
            .toString();
    }

    private String capitalized(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    private String fieldInitialization(String field, String value) {
        return new StringBuilder().append(DOUBLE_TAB)
            .append(field).append(EQUAL).append(value).append(SEMICOLON)
            .toString();
    }
}
