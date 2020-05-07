package dao;

import java.util.ArrayList;

public class Grammar {
	//TODO：建议先写left，在写right，否则后面的构造器就很奇怪，right在左边，left在右边（反正我觉得很奇怪），
	// 要是改了的话就把使用该构造器的参数位置互换。
	String left;
	ArrayList<String> right;

	public Grammar(String left, ArrayList<String> right) {
		super();
		this.right = right;
		this.left = left;
	}

	public String getLeft() {
		return left;
	}
	public void setLeft(String left) {
		this.left = left;
	}
	public ArrayList<String> getRight() {
		return right;
	}
	public void setRight(ArrayList<String> right) {
		this.right = right;
	}
	@Override
	public String toString() {
		return String.format("%-3s", left) + "-->  " + String.format("%-5s", right.toString());
	}
}
