//DFA状态转移表
public class DFATable {
  /**
   * 将一个终结状态转换为对应的单词类型。
   * @param state dfa某个的状态
   * @return 一个当前状态对应的单词类型
   */
  public WordType dfaStateToWordType(int state){
    switch (state){
      case 1:
        return WordType.valueOf("identify");
      case 2: case 3: case 31: case 34:
        return WordType.valueOf("decimalConstant");
      case 4:
        return WordType.valueOf("octalConstant");
      case 6:
        return WordType.valueOf("hexadecimalConstant");
      case 7:
        return WordType.valueOf("separator");
      case 8:
        return WordType.valueOf("plus");
      case 9:
        return WordType.valueOf("plusPlusOrEqual");
      case 10:
        return WordType.valueOf("minus");
      case 11:
        return WordType.valueOf("minusMinusOrEqual");
      case 12:
        return WordType.valueOf("multi");
      case 13:
        return WordType.valueOf("equal");
      case 15:
        return WordType.valueOf("not");
      case 17:
        return WordType.valueOf("division");
      case 20:
        return WordType.valueOf("comment");
      case 22:
        return WordType.valueOf("string");
      case 24:
        return WordType.valueOf("and");
      case 26:
        return WordType.valueOf("or");
      case 29:
        return WordType.valueOf("Char");
      case 35:  //‘(’
        return WordType.valueOf("leftParenthesis");
      case 36:
        return WordType.valueOf("rightParenthesis");
      case 14: case 16: case 37: case 38: case 39: case 40:
        return WordType.valueOf("relop");
      case 41: //‘[’
        return WordType.valueOf("leftBracket");
      case 42:
        return WordType.valueOf("rightBracket");
      default:  //输入状态为非终结状态
        return WordType.valueOf("ERROR");
    }
  }
}
