package dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//三地址指令
public class InterCode {
    private final List<String> interCode = new ArrayList<>();

    public InterCode(String[] interCode) {
        Collections.addAll(this.interCode, interCode);
    }

    public void backPatch(String back) {
        interCode.add(back);
    }

    public List<String> getInterCode() {
        return new ArrayList<>(interCode);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : interCode) {
            stringBuilder.append(" ");
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }
}
