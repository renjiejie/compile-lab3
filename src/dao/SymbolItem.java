package dao;
public class SymbolItem {
    // 符号名字，类型，行号，偏移量
    private final String identifier;
    private final String type;
    private final int line;
    private final int offset;

    public SymbolItem(String identifier, String type, int line, int offset) {
        this.identifier = identifier;
        this.type = type;
        this.line = line;
        this.offset = offset;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "<" + identifier + ", " + type + ", " + line + ", " + offset + ">";
    }
}
