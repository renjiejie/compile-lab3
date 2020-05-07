package dao;

public class Token {
	private String key;
	private String value;
	private int line=-1;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public Token(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public Token(String key, String value, int line) {
		super();
		this.key = key;
		this.value = value;
		this.line = line;
	}
	@Override
	public String toString() {
		return "Token key=" + key+" value="+value;
	}
	
	
}
