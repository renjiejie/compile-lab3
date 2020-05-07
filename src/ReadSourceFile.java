import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ReadSourceFile {
  public static String readFile(String filePath){
    StringBuffer result = new StringBuffer();
    try {
      File file = new File(filePath);
      if (file.isFile() && file.exists()) { // 判断文件是否存在
        InputStreamReader read = new InputStreamReader(new FileInputStream(file));
        BufferedReader bufferedReader = new BufferedReader(read);
        String lineTxt = null;

        while ((lineTxt = bufferedReader.readLine()) != null) {
          result.append(lineTxt);
          result.append("\n");
        }
        bufferedReader.close();
        read.close();
      }
      else {
        System.out.println("找不到指定的文件");
      }
    }
    catch (Exception e) {
      System.out.println("读取文件内容出错");
      e.printStackTrace();
    }
    return String.valueOf(result);
  }
}
