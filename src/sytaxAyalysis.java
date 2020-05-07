import dao.Grammar;
import dao.InterCode;
import dao.Order;
import dao.Symbol;
import dao.SymbolItem;
import dao.Token;
import dao.Tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
public class sytaxAyalysis {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Stack<Integer> stateStack = new Stack<>();  //状态栈
		Stack<Token> tokenStack = new Stack<>(); //token栈
		Stack<Symbol> symbolStack = new Stack<>();
		Stack<Tree> tree = new Stack<>();
		Tree root = null;
		List<Grammar> grammar = ReadTableFile.initGrammarList("src/data/grammar.xls");  //文法
		String[][] slrTable = ReadTableFile.readSLRTable("src/data/SLRTable.xls");		//slr表
		List<Token> tokens = new LexicalAnalysis().lexicalAnaly(); //词法分析得出的token序列
		List<Symbol> symbols = tokenToSymbol(tokens);
		List<InterCode> intercode = new ArrayList<InterCode>(); //中间代码
		List<SymbolItem> symbolItem = new ArrayList<SymbolItem>();  //符号表
		int localVarNumber = 0;  //记录当前生成了多少个临时变量，一个 newtemp（）生成一个临时变量，
		                         // 所以在使用临时变量在三元式中时名字不重复
		//临时变量
		String localT;
		String localW;
		
		Queue<Integer> q = new LinkedList<>();
		tokens.add(new Token("$", null));
		stateStack.add(0);
		tokenStack.add(new Token("$", null));
		String action;
		int ti = 0;
		while(ti<tokens.size()){
			Token token = tokens.get(ti);
			Symbol symbol = symbols.get(ti);
			action = slrTable[stateStack.peek()][getTokenIndex(token)];
			
			System.out.println(action);
			System.out.println(stateStack.toString());
			System.out.println(tokenStack.toString());
			if(action.equals("-1")) {
				System.out.println("错误");
				break;
			}
			if(action.substring(0, 1).equals("s")) { //进行移入
				
				stateStack.add(Integer.parseInt(action.substring(1))-1);
				tokenStack.add(token);
				symbolStack.add(symbol);
				tree.add(new Tree(token));
				ti++;
			}
			else if(action.substring(0, 1).equals("r")) {  //进行规约
				
				Grammar e = grammar.get(Integer.parseInt(action.substring(1)));
				List<String> right = e.getRight();
				String rightStr="";
				for(int i=0;i<right.size();++i) {
					rightStr = rightStr+right.get(i);
				}
				String str="";

				if(!rightStr.equals("no")) {
					List<Tree> trees = new ArrayList<Tree>();
					for(int i=0;i<right.size();++i) {
						trees.add(tree.pop());
						stateStack.pop();
						Token t = tokenStack.pop();
						str = t.getKey()+str;
					}
					if(str.equals(rightStr)) {
						Token t1 = new Token(e.getLeft(), null);
						tokenStack.add(t1);
						int i = getTokenIndex(t1);
						stateStack.add(Integer.parseInt(slrTable[stateStack.peek()][i])-1);
						Tree parent = new Tree(t1);
						parent.addChild(trees);
						tree.add(parent);
					
						//语义操作

						switch (Integer.parseInt(action.substring(1))) {
						//P->D {P.nq = D.nq}
						case 0:
							Symbol P = new Symbol("P");
							P.addAttribute("nq", symbolStack.pop().getAttribute("nq"));
							symbolStack.add(P);
							break;
						case 1:
							
							break;
						//B->B1 or B2 {B.nq  = B1.nq;backpatch(B1.falselist,B2.nq);
						//B.truelist=merge(B1.truelist,B2.truelist);B.falselist=B2.falselist;}
						case 2:
							Symbol B2 = symbolStack.pop();
							symbolStack.pop();
							Symbol B1 = symbolStack.pop();
							Symbol B = new Symbol("B");
							B.addAttribute("nq", B1.getAttribute("nq"));
							for(int j:B1.getFalseList()) {
								intercode.get(j).backPatch(B2.getAttribute("nq"));
							}
							symbolStack.add(B);
							break;
						/***
						 * S→ call id ( F) 
						 * {S.nq = F,nq; 
						 * n=0;
						 * for q中的每个t do{gen(‘param’ t );n = n+1；}gen(‘call’ i d.addr‘,’  n);} 特殊处理
						 */
						case 11:
							symbolStack.pop();
							Symbol F = symbolStack.pop();
							symbolStack.pop();
							Symbol id = symbolStack.pop();
							symbolStack.pop();
							Symbol S = new Symbol("S");
							S.addAttribute("nq", F.getAttribute("nq"));
							int size = q.size();
							int n = 0;
							for(n=0;n<size;++n) {
								gen("param "+q.poll(), intercode);
							}
							gen("call "+ id.getAttribute("addr")+","+n,intercode);
							break;

							//L->id [ E ] {L,nq = E.nq; L.array=lookup(id.lexeme);if L.array==null then error;
							//             L.type=L.array.type.elem; L.offset = newtemp(); gen(L.offset=E.addr*L.type.width);}
						case 34:
							symbolStack.pop();
							Symbol E = symbolStack.pop();
							symbolStack.pop();
							Symbol L1 = symbolStack.pop();
							Symbol id = new Symbol("id");


							// L->L [ E ] {L.nq = L1.nq; L.array = L1.array;L.type=L1.type.elem;t=newtemp();gen(t=E.addr*L.type.width);
							// 						L.offset = newtemp(); gen(L.offset= L1.offset+t);}
						case 35:
							symbolStack.pop();
							Symbol E = symbolStack.pop();
							symbolStack.pop();
							Symbol L1 = symbolStack.pop();
							Symbol L = new Symbol("L");
							L.addAttribute("nq", L1.getAttribute("nq"));
							L.addAttribute("type", getArrayELemType(L1.getAttribute("type")));
							gen("t"+localVarNumber+"="+E.getAttribute("addr")+"*"+getTypeWidth(L1.getAttribute("type"))
								,intercode);
							localVarNumber++;
							int t = Integer.parseInt(E.getAttribute("addr")) * getTypeWidth(L1.getAttribute("type");
							gen("t"+localVarNumber+"="+L1.getAttribute("offset")+t, intercode);
							localVarNumber++;
							symbolStack.add(L);

							default:
							
							break;
						}
					}
					else {
						System.out.println("erro");
						ti++;
						continue;
					}
				}
				else {
					Token t1 = new Token(e.getLeft(), null);
					tokenStack.add(t1);
					tree.add(new Tree(t1));
					int i = getTokenIndex(t1);
					stateStack.add(Integer.parseInt(slrTable[stateStack.peek()][i])-1);
				}
				
			}
			else if(action.substring(0,1).equals("e")) {
				System.out.println("erro");
				ti++;
				continue;
			}
			else if(action.equals("acc0")) {
				root = tree.peek();
				System.out.println("完成");
				root.setNodeDepth(0);
				new Order().preOrder(root);
				System.exit(0);
			}
		}
		
	}
	/**
	 * 根据token返回索引
	 * @return 表索引
	 */
	static int getTokenIndex(Token token) {
		int index = 0 ;
		String key = token.getKey();
		switch (key){
		case "plus":
			return 0;
		case "multi":
			return 1;
		case "minus":
			return 2;
		case "leftParenthesis":
			return 3;
		case "identify":
			return 4;
		case "decimalConstant":
			return 5;
		case "equal":
			return 6;
		case "call":
			return 7;
		case "rightParenthesis":
			return 8;
		case "leftBracket":
			return 9;
		case "rightBracket":
			return 10;
		case ",":
			return 11;
		case ";":
			return 12;
		case "relop":
			return 13;	
		case "true":
			return 14;
		case "false":
			return 15;
		case "not":
			return 16;
		case "and":
			return 17;
		case "or":
			return 18;
		case "if":
			return 19;
		case "then":
			return 20;
		case "else":
			return 21;
		case "while":
			return 22;
		case "do":
			return 23;
		case "proc":
			return 24;
		case "record":
			return 25;
		case "integer":
			return 26;
		case "real":
			return 27;
		case "$":
			return 28;
		case "S":
			return 29;
		case "E":
			return 30;
		case "L":
			return 32-1;
		case "F":
			return 33-1;
		case "B":
			return 34-1;
		case "P":
			return 35-1;
		case "D":
			return 35;
		case "X":
			return 36;
		case "C":
			return 37;
		case "T":
			return 38;
		default:
			System.out.println("unkonw token");
			return -1;
		}
	}
	/***
	 * 当出现重复返回false
	 * @param l
	 * @param item
	 * @return
	 */
	static boolean lookUpSymbolItem(List<SymbolItem> l,String item) {
		for(SymbolItem i :l) {
			if(i.getIdentifier().equals(item)) 
				return false;
		}
		return true;
	}
	static void gen(String inter,List<InterCode> intercode) {
		intercode.add(new InterCode(new String[]{inter}));
	}
	static List<Symbol> tokenToSymbol(List<Token> tokens) {
		List<Symbol> symbols = new ArrayList<Symbol>();
		for(Token t:tokens) {
			Symbol s = new Symbol(t.getKey());
			s.addAttribute("value", t.getValue());
			s.addAttribute("line", t.getLine()+"");
			symbols.add(s);
		}
		return symbols;
	}

	/**
	 * 获取数组的元素类型
	 * @param arrayType 多维（一维）数组的类型
	 * @return 数组元素的类型
	 */
	static String getArrayELemType(String arrayType){
		assert arrayType.contains("[");
		assert arrayType.contains("]");
		int first_left_index = arrayType.indexOf('[');
		int first_right_index = arrayType.indexOf(']');
		return arrayType.substring(0, first_left_index)+arrayType.substring(first_right_index+1);
	}

	/**
	 * 计算类型的宽度信息
	 * @param typeName 类型名
	 * @return 宽度
	 */
	static int getTypeWidth(String typeName){
		int width = 4;
		if (typeName.contains("[")){ //数组类型
			while(typeName.contains("[")){
				int left_index = typeName.indexOf('[');
				int right_index = typeName.indexOf(']');
				int subWidth = Integer.parseInt(typeName.substring(left_index+1, right_index));
				width*=subWidth;
				typeName = typeName.substring(right_index+1);
			}
		}
		return width;
	}
}


