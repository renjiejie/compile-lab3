import dao.Grammar;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 这个类是实现xls文件读取，提供读取各种xls文件，并处理成dfa，goto，action等表
 */
public class ReadTableFile {
  /**
   * 读取xls文件类型，并把每个位置对应成二维字符数组返回（提供给其他文件读取方法的公共部分）
   * @param filePath 文件位置
   * @return 二维字符数组，各个位置与原来的xls文件位置对应
   * @throws IOException 文件读取异常
   */
  public static String[][] readTable(String filePath) throws IOException {
    File file = new File(filePath);
    List<String[]> dfaTable = new ArrayList<>();
    int rowSize = 0;
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
    // 打开HSSFWorkbook
    POIFSFileSystem fs = new POIFSFileSystem(in);
    HSSFWorkbook wb = new HSSFWorkbook(fs);
    HSSFCell cell;
    HSSFSheet st = wb.getSheetAt(0);
    for (int rowIndex = 0; rowIndex <= st.getLastRowNum(); rowIndex++) { //去除第一列，没有读入
      HSSFRow row = st.getRow(rowIndex);
      if (row == null) {
        continue;
      }
      int tempRowSize = row.getLastCellNum();
      if (tempRowSize > rowSize) {
        rowSize = tempRowSize;
      }
      String[] values = new String[rowSize];
      Arrays.fill(values, "-1");
      boolean hasValue = false;
      for (short columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
        String value = "-1";
        cell = row.getCell(columnIndex);
        if (cell != null) {
          cell.setEncoding(HSSFCell.ENCODING_UTF_16);
          if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            value = new DecimalFormat("0").format(cell.getNumericCellValue());
          }else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING){
            value = cell.getStringCellValue();
          }
        }
        values[columnIndex] = value;
        hasValue = true;
      }
      if (hasValue) {
        dfaTable.add(values);
      }
    }
    in.close();
    String[][] result = new String[dfaTable.size()][rowSize];
    for (int i = 0; i < dfaTable.size(); i++) {
      result[i] = dfaTable.get(i);
    }
    return result;
  }

   /**
    * 从文件中读取DFA的表
    * @param filePath 文件路径
    * @return 一个存储DFA状态转移表的二维数组
    * @throws IOException 文件读取异常
    */
  public static int[][] readDfaTable(String filePath) throws IOException {
    String[][] tableContext = readTable(filePath);
    int[][] result = new int[tableContext.length-1][tableContext[0].length-1];
    for (int i = 1; i < tableContext.length; i++) {  //去除最上面一行
      for (int j = 1; j < tableContext[0].length; j++) {  //去除最左边一行
        result[i-1][j-1] = Integer.parseInt(tableContext[i][j]);
      }
    }
    return result;
  }

  /**
   * 从文件中读取SLR的表
   * @param filepath 文件路径
   * @return 一个存储SLR GOTO和ACTION表的二维数组
   * @throws IOException 文件读取异常
   */
  public static String[][] readSLRTable(String filepath) throws IOException{
    String[][] tableContext = readTable(filepath);
    String[][] result = new String[tableContext.length-1][tableContext[0].length-1];
    for (int i = 1; i < tableContext.length; i++) {  //去除最上面一行
      //去除最左边一行
      if (tableContext[0].length - 1 >= 0)
        System.arraycopy(tableContext[i], 1, result[i - 1], 0, tableContext[0].length - 1);
    }
    return result;
  }

  /**
   * 将dfa表中的符号格式化转换成String类型, 再打印输出
   * @param filePath DFA表的路径
   * @throws IOException 文件读取异常
   */
  public static void dfaTableToString(String filePath) throws IOException {
    StringBuilder DFAShowString = new StringBuilder();
    String[][] DFAAllTAble = readTable(filePath);
    for (String[] col: DFAAllTAble) {
      for (String item: col) {
        DFAShowString.append(String.format("%-20s", item));
      }
      DFAShowString.append("\n");
    }
    System.out.println(DFAShowString);
  }

  /**
   * 将dfa表中的符号格式化转换成String类型,再打印输出(内部逻辑与dfaTableToString相同，故直接调用)
   * @param filePath SLR表的路径
   * @throws IOException 文件读取异常
   */
  public static void slrTableToString(String filePath) throws IOException {
    dfaTableToString(filePath);
  }

  /**
   * 文法表达式的格式化输出
   * @param filePath 文法表达式的路径
   * @throws IOException 文件读取异常
   */
  public static void grammarToString(String filePath) throws IOException {
    StringBuilder DFAShowString = new StringBuilder();
    DFAShowString.append("\n").append("文法表达式").append("\n");
    String[][] DFAAllTAble = readTable(filePath);
    for (String[] col: DFAAllTAble) {
      String expressionLeft = col[0];
      DFAShowString.append(String.format("%-5s", expressionLeft)).append("  --->  ");
      for (int i = 1; i < col.length; i++) {
        if(!col[i].equals("-1")){
          DFAShowString.append(col[i]).append(" | ");
        }
      }
      DFAShowString.deleteCharAt(DFAShowString.length()-1);  //删除最后一个符号‘ ’
      DFAShowString.deleteCharAt(DFAShowString.length()-1);  //删除最后一个符号‘|’
      DFAShowString.append("\n");
    }
    System.out.println(DFAShowString);
  }

  /**
   * 初始化grammar的列表
   * @param filePath grammar的文件路径
   * @return 所有的grammar的list
   * @throws IOException 文件读取异常
   */
  public static List<Grammar> initGrammarList(String filePath) throws IOException {
    List<Grammar> grammars = new ArrayList<>();
    String[][] DFAAllTAble = readTable(filePath);
    Map<String,List<String>> grammarMap = new HashMap<>();
    for (String[] col: DFAAllTAble) {
      grammarMap.put(col[0], Arrays.asList(Arrays.copyOfRange(col, 1,col.length)));
    }
    Set<String> keySet = grammarMap.keySet();
    for (String key: keySet) {
      List<String> values= grammarMap.get(key);
      for (String value: values) {
        if(!value.equals("-1")){
          ArrayList<String> rightValue =new ArrayList<>();
          Collections.addAll(rightValue, value.split(" "));
          grammars.add(new Grammar(key, rightValue));
        }
      }
    }
    return grammars;
  }

  public static void main(String[] args) throws IOException {
    //int[][] dfa = r.readDfaTable("src/data/DFATable.xls");
    //String[][] slr = r.readSLRTable("src/data/SLRTable.xls");
    //r.dfaTableToString("src/data/DFATable.xls");
    //r.slrTableString("src/data/SLRTable.xls");
    //System.out.println(Arrays.deepToString(slr));
    List<Grammar> a = initGrammarList("src/data/grammar.xls");
    System.out.println(a);
  }
}
