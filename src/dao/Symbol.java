package dao;

import java.util.*;

/**
 * 符号类
 */
public class Symbol {
    //名字，属性值对，trueList，falseList，nextList
    private final String name;
    private final Map<String, String> attribute = new HashMap<>();
    private List<Integer> trueList = new ArrayList<>();
    private List<Integer> falseList = new ArrayList<>();
    private List<Integer> nextList = new ArrayList<>();

    public Symbol(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addAttribute(String key, String value) {
        attribute.put(key, value);
    }

    public String getAttribute(String key) {
        return attribute.get(key);
    }

    public List<Integer> getFalseList() {
        return new ArrayList<>(falseList);
    }


    public List<Integer> getTrueList() {
        return new ArrayList<>(trueList);
    }

    public List<Integer> getNextList() {
        return new ArrayList<>(nextList);
    }

    /**
     * 将一个value存入一个非终结符的list中
     * @param value 需要存入的value
     * @param type 当i=1时，存入truelist，i=0时，存入falselist，i为其他时，存入nextlist
     */
    public void makeList(int value, int type) {
        if (type == 0) {
            falseList.add(value);
        } else if (type == 1) {
            trueList.add(value);
        } else {
            nextList.add(value);
        }
    }

    /**
     * merge两个list到当前符号的list中
     * @param list1 第一个list
     * @param list2 第二个list
     * @param type  当i=1时，存入truelist，i=0时，存入falselist，i为其他时，存入nextlist
     */
    public void merge(List<Integer> list1, List<Integer> list2, int type) {
        if (type == 0) {
            falseList.addAll(new HashSet<>(list1));
            falseList.addAll(new HashSet<>(list2));
        } else if (type == 1) {
            trueList.addAll(new HashSet<>(list1));
            trueList.addAll(new HashSet<>(list2));
        } else {
            nextList.addAll(new HashSet<>(list1));
            nextList.addAll(new HashSet<>(list2));
        }
    }

    /**
     * 将一个list存入一个非终结符的list中
     * @param list 需要存入的list
     * @param type 当i=1时，存入truelist，i=0时，存入falselist，i为其他时，存入nextlist
     */
    public void addList(List<Integer> list, int type) {
        if (type == 0) {
            falseList.addAll(new HashSet<>(list));
        } else if (type == 1) {
            trueList.addAll(new HashSet<>(list));
        } else {
            nextList.addAll(new HashSet<>(list));
        }
    }
}