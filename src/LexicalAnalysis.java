import dao.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LexicalAnalysis {
	static int[][] dfaTable = {}; //dfa表
	
	final static int[] endState = {1,2,3,4,6,7,8,9,10,11,12,13,14,15,16,17,20,22,24,26,29,31,34,35,36,37,38,39,40,41,42}; //终止状态

	final static String[] Keywords =
		      ("include,define,auto,bool,break,case,catch,char,class,const,const_cast,continue,default,delete,do,double," +
							"dynamic_cast,else,enum,explicit,extern,false,float,for,friend,goto,if,inline,int,long,mutable," +
							"namespace,new,operator,private,protected,public,register,reinterpret_cast,return,short,signed,sizeof," +
							"static,static_cast,struct,switch,template,this,throw,true,try,typedef,typeid,typename,union,unsigned," +
							"using,virtual,void,volatile,while,then,real,integer,record,proc,call")
	          .split(",");
	

	public List<Token> lexicalAnaly(String filePath) throws IOException {
		List<Token> tokens = new ArrayList<>();
		String dfaFilePath = "src/data/DFATable.xls";
//		String slrFilePath = "src/data/SLRTable.xls";
//		String grammarFilePath = "src/data/grammar.xls";
		int line = 1;
		System.out.println("使用的DFA转换表");
		ReadTableFile.dfaTableToString(dfaFilePath);
//		System.out.println("使用的SLR的ACTION表和GOTO表");
//		readTableFile.slrTableString(slrFilePath);
//		readTableFile.grammarToString(grammarFilePath);
		dfaTable = ReadTableFile.readDfaTable(dfaFilePath);
		String text = ReadSourceFile.readFile(filePath);
	
		int state = 0;
		int lastState = 0;
		List<Character> readChar = new ArrayList<>(); //保存读到的字符
		char[] textChar = text.toCharArray();//将字符串变成字符数组
		for(int i = 0;i<textChar.length;++i) {
			
			if((textChar[i] == ' '||textChar[i] == '\n')&&state!=18&&state!=21) {                //读到空格开始进行识别 
				
				if(readChar.size()==0) {
					continue;
				}
				if(isEndState(state)) {                 //判断当前状态是否为终止状态
					tokens.add(printToken(state, readChar,line));
				}
				else {
					System.out.println("erro");
				}
				readChar.clear();           //识别完后清空
				state = 0;
				if(textChar[i] == '\n') {
					line++;
				}
				continue;
			}
			state = getNextState(state, chartype(textChar[i])); //获得下一个状态
			if(state == -1) {            //当前字符没有后继状态
				if(isOperation(textChar[i])) {  //如果当前字符是界符或者运算符
					i--;
					tokens.add(printToken(lastState, readChar,line));
					readChar.clear();
					state = 0;
					continue;
				}
				else if(readChar.size() == 0) {
					System.out.println("erro");
					state = 0;
					readChar.clear();
					continue;
				}
				else if(isOperation(readChar.get(readChar.size()-1))) { //若上一个字符是界符或者运算符
					
					tokens.add(printToken(lastState, readChar,line));
					readChar.clear();
					i--;
					state = 0;
					continue;
				}
				else {
					System.out.println("erro");
					state = 0;
					readChar.clear();
					continue;
				}
			}
			readChar.add(textChar[i]);
			lastState = state;
		}
		return tokens;
	}
	//获得字符类型
	static List<Integer> chartype(char c) {
		List<Integer> type = new ArrayList<>();
		if(Character.isDigit(c)) {
			type.add(0);
		}
		if(c == '0') {
			type.add(1);
		}
		if(Character.isDigit(c)&&c != '0') {
			type.add(2);
		}

		if(c == '1') {
			type.add(3);
		}
		if(Character.isDigit(c)||java.util.regex.Pattern.matches("[a-f]", c+""))
		{
			type.add(4);
		}
		if(c == 'x') {
			type.add(5);
		}
		if(Character.isAlphabetic(c)) {
			type.add(6);
		}
		if(c=='_') {
			type.add(7);
		}
		if(c == ';'||c == '{'||c == '}'||c == ',') {
			type.add(8);
		}
		if(c == '+') {
			type.add(9);
		}
		if(c == '-') {
			type.add(10);
		}
		if(c == '*') {
			type.add(11);
		}
		if(c == '=') {
			type.add(12);
		}
		if(c == '!') {
			type.add(13);
		}
		if(c == '/') {
			type.add(14);
		}
		if(c == '"') {
			type.add(15);
		}
		if(c == '&') {
			type.add(16);
		}
		if(c == '|') {
			type.add(17);
		}
		if(c == '\'') {
			type.add(18);
		}
		if(c == '.') {
			type.add(19);
		}
		if(c == 'E') {
			type.add(20);
		}
		if(c == '(') {
			type.add(21);
		}
		if(c == ')') {
			type.add(22);
		}
		if(c == '<') {
			type.add(23);
		}
		if(c == '>') {
			type.add(24);
		}
		if(c == '[') {
			type.add(25);
		}
		if(c == ']') {
			type.add(26);
		}
		type.add(27);
		return type;
	}
	//根据终止状态判断识别的字符
	static String endType(int state) {
		return null;
	}
	//查表获得下一个状态
	static int getNextState(int currentState,List<Integer> type) {
		//使用list判断下一个状态
		for(int i:type) {
			if(dfaTable[currentState][i]!=-1) {
				return dfaTable[currentState][i];
			}
		}
		return -1;
	}
	//判断是否为终止状态
	static boolean isEndState(int state) {
		for(int i:endState) {
			if(i==state) {
				return true;
			}
		}
		return false;
	}
	//判断是否为关键词
	static boolean isKeyword(String word) {
		for(String i : Keywords) {
			if(i.equals(word)) {
				return true;
			}
		}
		return false;
	}
	//识别并输出状态对应的种别码
	static Token printToken(int endstate2,List<Character> readChar,int line) {
		Token token;
		WordType type = new DFATable().dfaStateToWordType(endstate2);
		StringBuilder str = new StringBuilder();
		for(Character c:readChar) {
			str.append(c);
		}
		switch (type) {
		case identify:
			if(isKeyword(str.toString())) {    //识别出是关键字
				System.out.print(String.format("%-20s", str));
				System.out.println("<"+str.toString()+", ->");
				token = new Token(str.toString(),null,line);
			}
			else {                            //识别出是标识符
				System.out.print(String.format("%-20s", str));
				System.out.println("<idn, "+str.toString()+">");
				token = new Token("identify",str.toString(),line);
			}
			break;
		case decimalConstant:
			System.out.print(String.format("%-20s", str));
			System.out.println("<decimalConstant, "+str.toString()+">");
			token = new Token("decimalConstant",str.toString(),line);

			break;
		case octalConstant:
			System.out.print(String.format("%-20s", str));
			System.out.println("<octalConstant, "+str.toString()+">");
			token = new Token("octalConstant",str.toString(),line);
			break;
		case hexadecimalConstant:
			System.out.print(String.format("%-20s", str));
			System.out.println("<hexadecimalConstant, "+str.toString()+">");
			token = new Token("hexadecimalConstant",str.toString(),line);
			break;
		case string:
			System.out.print(String.format("%-20s", str));
			System.out.println("<string, "+str.toString()+">");
			token = new Token("string",str.toString(),line);
			break;
		case Char:
			System.out.print(String.format("%-20s", str));
			System.out.println("<char, "+str.toString()+">");
			token = new Token("char",str.toString(),line);
			break;
		case separator:
			System.out.print(String.format("%-20s", str));
			System.out.println("<separator, "+str.toString()+">");
			token = new Token(str.toString(),null,line);
			break;
		case relop:
			System.out.print(String.format("%-20s", str));
			System.out.println("<relop, "+str.toString()+">");
			token = new Token("relop",str.toString(),line);
			break;
			default:
			System.out.print(String.format("%-20s", str));
			System.out.println("<"+type.toString()+", ->");
			token = new Token(type.toString(),null,line);
			break;
		}
		return token;
	}
	static boolean isOperation(char c) {   //判断字符是否为界符或者运算符
		return c == '[' || c == ']' || c == '=' || c == '+' || c == '(' || c == ')' || c == '-' || c == '*'
				|| c == '/' || c == ';' || c == '{' || c == '}' || c == '!' || c == ',' || c == '<' || c == '>';
	}
}
