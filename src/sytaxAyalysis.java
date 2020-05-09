import dao.Grammar;
import dao.InterCode;
import dao.Order;
import dao.Symbol;
import dao.SymbolItem;
import dao.Token;
import dao.Tree;

import java.io.IOException;
import java.util.*;

public class sytaxAyalysis {
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Stack<Integer> stateStack = new Stack<>();  //状态栈
		Stack<Token> tokenStack = new Stack<>(); //token栈
		Stack<Symbol> symbolStack = new Stack<>();
		Stack<Tree> tree = new Stack<>();
		Tree root = null;
		String filePath = null;
		System.out.println("请输入需要分析的文件\t1.正确的文件（program.txt）\t2.包含错误的文件（error.txt）");
		Scanner sc = new Scanner(System.in);
		int choose = sc.nextInt();
		if(choose==1){
			filePath = "src/data/program.txt";
		}else {
			filePath = "src/data/error.txt";
		}
		List<Grammar> grammar = ReadTableFile.initGrammarList("src/data/grammar.xls");  //文法
		String[][] slrTable = ReadTableFile.readSLRTable("src/data/SLRTable.xls");		//slr表
		List<Token> tokens = new LexicalAnalysis().lexicalAnaly(filePath); //词法分析得出的token序列
		List<Symbol> symbols = tokenToSymbol(tokens);
		List<InterCode> intercode = new ArrayList<InterCode>(); //中间代码, 从1号开始
		List<String> four = new ArrayList<String>();
		StringBuilder errorMessages = new StringBuilder();
		List<SymbolItem> symbolItem = new ArrayList<SymbolItem>();  //符号表
		int localVarNumber = 0;  //记录当前生成了多少个临时变量，一个 newtemp（）生成一个临时变量，
		                         // 所以在使用临时变量在三元式中时名字不重复
		//临时变量
		String localT = "";
		String localW = "";
		int offset=0; //偏移量
		
		Queue<Integer> q = new LinkedList<>();
		tokens.add(new Token("$", null));
		symbols.add(new Symbol("$"));
		stateStack.add(0);
		tokenStack.add(new Token("$", null));
		symbolStack.add(new Symbol("$"));
		String action;
		int ti = 0;
		while(ti<tokens.size()){
			Token token = tokens.get(ti);
			Symbol symbol = symbols.get(ti);
			action = slrTable[stateStack.peek()][getTokenIndex(token)];

			if (!symbolStack.isEmpty() && symbolStack.peek().getName().equals("X")) {  //将栈中X的属放在临时变量t，w中
				localT = symbolStack.peek().getAttribute("type");
				localW = symbolStack.peek().getAttribute("width");
			}
			

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
						int flage=0;
						Token t1 = new Token(e.getLeft(), null);
						tokenStack.add(t1);
						int i = getTokenIndex(t1);
						stateStack.add(Integer.parseInt(slrTable[stateStack.peek()][i])-1);
						Tree parent = new Tree(t1);
						parent.addChild(trees);
						tree.add(parent);
						//语义操作
						int r_num = Integer.parseInt(action.substring(1));
						if (r_num ==0){ //P->D {P.nq = D.nq}
							Symbol P = new Symbol("P");
							P.addAttribute("nq", symbolStack.pop().getAttribute("nq"));
							symbolStack.push(P);
						}else if(r_num==1){
							//P->S {P.nq = S.nq}
							Symbol S = symbolStack.pop();
							Symbol P = new Symbol("P");

							P.addAttribute("nq", S.getAttribute("nq"));
							symbolStack.push(P);
						}else if(r_num ==2){
							//B->B1 or B2 {B.nq  = B1.nq;backpatch(B1.falselist,B2.nq);
							//B.truelist=merge(B1.truelist,B2.truelist);B.falselist=B2.falselist;}
							Symbol B2 = symbolStack.pop();
							symbolStack.pop();
							Symbol B1 = symbolStack.pop();
							Symbol B = new Symbol("B");

							B.addAttribute("nq", B1.getAttribute("nq"));
							backpatch(B1.getFalseList(), Integer.parseInt(B2.getAttribute("nq")), intercode,four);
							B.merge(B1.getTrueList(), B2.getTrueList(), 1);
							B.addList(B2.getFalseList(), 0);
							symbolStack.push(B);
						}else if(r_num==3){
							//B->B1 and B2{B.nq = B1.nq; backpatch(B1.truelist,B2.nq);
							//             B.truelist=B2.truelist;B.falselist=merge(B1.falselist,B2.falselist);}
							Symbol B2 = symbolStack.pop();
							symbolStack.pop();
							Symbol B1 = symbolStack.pop();
							Symbol B = new Symbol("B");

							B.addAttribute("nq", B1.getAttribute("nq"));
							backpatch(B1.getTrueList(), Integer.parseInt(B2.getAttribute("nq")), intercode,four);
							B.addList(B2.getTrueList(), 1);
							B.merge(B1.getFalseList(), B2.getFalseList(), 0);
							symbolStack.push(B);
						}else if(r_num==4){
							//B->not B1 {B.nq = B1.nq; B.truelist=B1.falselist;B.falselist=B1.truelist;
							Symbol B1 = symbolStack.pop();
							symbolStack.pop();
							Symbol B = new Symbol("B");

							B.addAttribute("nq", B1.getAttribute("nq"));
							B1.addList(B1.getFalseList(), 1);
							B1.addList(B1.getTrueList(), 0);
							symbolStack.push(B);
						}else if(r_num==5){
							//B->B1{B.nq = B1.nq; B.truelist=B1.truelist;B.falselist=B1.falselist;}
							Symbol B1 = symbolStack.pop();
							Symbol B = new Symbol("B");

							B.addAttribute("nq", B1.getAttribute("nq"));
							B1.addList(B1.getTrueList(), 1);
							B1.addList(B1.getFalseList(), 0);
							symbolStack.push(B);
						}else if(r_num==6){
							//B->E1 relop E2 {B.nq = E1.nq; B.truelist=makelist(nextquad);B.falselist=makelist(nextquad+1);
							//                gen(‘if’E1.addr relop E2.addr‘goto’);gen(‘goto’);}
							Symbol E2 = symbolStack.pop();
							Symbol relop = symbolStack.pop();
							Symbol E1 = symbolStack.pop();
							Symbol B = new Symbol("B");

							B.addAttribute("nq", E1.getAttribute("nq"));
							B.makeList(intercode.size()+1, 1);
							B.makeList(intercode.size()+2, 0);
							gen("if "+E1.getAttribute("addr")+relop.getAttribute("lexeme")+E2.getAttribute("addr")+" goto ", intercode);
							four.add("("+"j"+relop.getAttribute("lexeme")+","+E1.getAttribute("addr")+","+E2.getAttribute("addr"));
							gen("goto ", intercode);
							four.add("(j,_,_,");
							symbolStack.push(B);
						}else if(r_num==7){
							//B->true{B.nq = nextquad; B.truelist=makelist(nextquad);gen(‘goto_’);}
							symbolStack.pop();
							Symbol B = new Symbol("B");

							B.addAttribute("nq", String.valueOf(intercode.size()+1));
							B.makeList(intercode.size()+1, 1);
							gen("goto ", intercode);
							four.add("(j,_,_,");
							symbolStack.push(B);
						}else if(r_num==8){
							//B->false {B.nq = nextquad; B.falselist=makelist(nextquad);gen(‘goto’);}
							symbolStack.pop();
							Symbol B = new Symbol("B");

							B.addAttribute("nq", String.valueOf(intercode.size()+1));
							B.makeList(intercode.size()+1, 0);
							gen("goto ", intercode);
							four.add("(j,_,_,");
							symbolStack.push(B);
						}else if(r_num==9){
							// S->id = E ; {S.nq = E.nq; p=lookup(id.lexeme);if p== null then error;gen(p’=’E.addr);}
							symbolStack.pop();
							Symbol E = symbolStack.pop();
							symbolStack.pop();
							Symbol id = symbolStack.pop();
							Symbol S = new Symbol("S");

							S.addAttribute("nq", E.getAttribute("nq"));
							String p = null;
							if (lookUpSymbolItem(symbolItem, id.getAttribute("lexeme"))){
								p = id.getAttribute("lexeme");
							}
							if (null==p){
								String errorMessage = "Error at line["+id.getAttribute("line")+"]"+":  ["
									+id.getAttribute("lexeme")+" not defined]";
								System.out.println(errorMessage);
								errorMessages.append(errorMessage).append("\n");
							}
							gen(p+" = "+E.getAttribute("addr"), intercode);
							addFour("=", E.getAttribute("addr"), "_", p, four);
							symbolStack.push(S);
						}else if(r_num==10){
							//S->L = E ; {S.nq = L.nq; gen(L.array[L.offset]=E.addr);}
							symbolStack.pop();
							Symbol E = symbolStack.pop();
							symbolStack.pop();
							Symbol L = symbolStack.pop();
							Symbol S = new Symbol("S");
							S.addAttribute("nq", L.getAttribute("nq"));
							gen(L.getAttribute("array")+"["+L.getAttribute("offset")+"]"+"="+E.getAttribute("addr"),
								intercode);
							addFour("=", E.getAttribute("addr"), "_",L.getAttribute("array")+"["+L.getAttribute("offset")+"]" , four);
							symbolStack.push(S);
						}else if(r_num==11){
							/***
							 * S→ call id ( F);
							 * {S.nq = F,nq;
							 * n=0;
							 * for q中的每个t do{gen(‘param’ t );n = n+1；}gen(‘call’ i d.addr‘,’  n);} 特殊处理
							 */
							symbolStack.pop();
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
								addFour("param","_", "_",q.poll()+"" , four);
							}
							gen("call "+ id.getAttribute("lexeme")+","+n,intercode);
							addFour("call", n+"", "_", id.getAttribute("lexeme"), four);
							symbolStack.push(S);
						}else if(r_num==12){
							// S->if B then S1 {S.nq = B.nq; backpatch(B.truelist,M.quad);S.nextlist=merge(B.falselist,S1.nextlist);}
							Symbol S1 = symbolStack.pop();
							symbolStack.pop();
							Symbol B = symbolStack.pop();
							symbolStack.pop();
							Symbol S = new Symbol("S");

							S.addAttribute("nq", B.getAttribute("nq"));
							backpatch(B.getTrueList(), Integer.parseInt(S1.getAttribute("nq")), intercode,four);
							backpatch(B.getFalseList(), intercode.size()+1, intercode,four);
							backpatch(S1.getNextList(), intercode.size()+1, intercode,four);
							S.merge(B.getFalseList(), S1.getNextList(), 2);
							symbolStack.push(S);
						}else if(r_num==13){
							System.out.println("error r13");
						}else if(r_num==14){
							//S-> while B do S1 {S.nq=B.nq; backpatch(S1.nextlist,B.nq);backpatch(B.truelist,S1.nq);
							//                  S.nextlist=B.falselist;gen(‘goto’B.nq);}
							Symbol S1 = symbolStack.pop();
							symbolStack.pop();
							Symbol B = symbolStack.pop();
							symbolStack.pop();
							Symbol S = new Symbol("S");

							S.addAttribute("nq", B.getAttribute("nq"));
							backpatch(S1.getNextList(), Integer.parseInt(B.getAttribute("nq")), intercode,four);
							backpatch(B.getTrueList(), Integer.parseInt(S1.getAttribute("nq")), intercode,four);
							backpatch(B.getFalseList(), intercode.size()+1, intercode,four);
							S.addList(B.getFalseList(), 2);  // S.nextlist=S2.nextlist;
							gen("goto "+B.getAttribute("nq"), intercode);
							four.add("(j,_,_,"+B.getAttribute("nq")+")");
							symbolStack.push(S);
						}else if(r_num==15){
							//S->S1 S2{S.nq = S1.nq; backpatch(S1.nextlist,S2.nq); S.nextlist=S2.nextlist;}
							Symbol S2 = symbolStack.pop();
							Symbol S1 = symbolStack.pop();
							Symbol S = new Symbol("S");

							S.addAttribute("nq", S1.getAttribute("nq"));
							backpatch(S1.getNextList(), Integer.parseInt(S2.getAttribute("nq")), intercode,four);
							S.addList(S2.getNextList(), 2);  // S.nextlist=S2.nextlist;
							symbolStack.push(S);
						}else if(r_num==16){
							// C->[ digit ] C {C.nq=C1.nq; C.type=array(digit.val,C1.type); C.width=digit.val*C1.width;}
							Symbol C1 = symbolStack.pop();
							symbolStack.pop();
							Symbol digit = symbolStack.pop();
							symbolStack.pop();
							Symbol C = new Symbol("C");

							C.addAttribute("nq", C1.getAttribute("nq"));
							String C1Type = C1.getAttribute("type");
							if(C1Type.contains("[")){
								int indexOfFirst = C1Type.indexOf('[');
								C.addAttribute("type", C1Type.substring(0, indexOfFirst)+
									"["+digit.getAttribute("value")+"]"+C1Type.substring(indexOfFirst));
							}else{
								C.addAttribute("type", C1Type+"["+digit.getAttribute("value")+"]");
							}
							C.addAttribute("width", String.valueOf(Integer.parseInt(digit.getAttribute("value"))*
								Integer.parseInt(C1.getAttribute("width"))));
							symbolStack.push(C);
						}else if(r_num==18){
							//D->D1 D2{D.nq = D1.nq;}
							Symbol D2 = symbolStack.pop();
							Symbol D1 = symbolStack.pop();
							Symbol D = new Symbol("D");

							D.addAttribute("nq", D1.getAttribute("nq"));
							symbolStack.push(D);
						}else if(r_num==19){
							//D->proc id ; D1 S{D.nq = D1.nq;}
							Symbol S = symbolStack.pop();
							Symbol D1 = symbolStack.pop();
							symbolStack.pop();
							symbolStack.pop();
							symbolStack.pop();  // proc id ;
							Symbol D = new Symbol("D");

							D.addAttribute("nq", D1.getAttribute("nq"));
							symbolStack.push(D);
						}else if (r_num==20){
							// D->T id;{D.nq= T.nq; enter( id.lexeme, T.type, offset); offset= offset+ T.width; }
							symbolStack.pop();
							Symbol id = symbolStack.pop();
							Symbol T = symbolStack.pop();
							Symbol D = new Symbol("D");

							D.addAttribute("nq", T.getAttribute("nq"));
							if (lookUpSymbolItem(symbolItem, id.getAttribute("lexeme"))){
								String errorMessage = "Error at line["+id.getAttribute("line")+"]"+": ["
									+id.getAttribute("lexeme")+" has been defined]";
								System.out.println(errorMessage);
								errorMessages.append(errorMessage).append("\n");
							}else{
								symbolItem.add(new SymbolItem(id.getAttribute("lexeme"), T.getAttribute("type"),
									Integer.parseInt(id.getAttribute("line")), offset));
								if (null != T.getAttribute("width")){
									offset += Integer.parseInt(T.getAttribute("width"));
									System.out.println("offset: "+offset);
								}
							}
							symbolStack.push(D);
						}else if (r_num==21){
							// T->X {t=X.type;w=X.width;}（前面做了） C{T.nq =X.nq; T.type=C.type;T.width=C.width;}
							Symbol C = symbolStack.pop();
							Symbol X = symbolStack.pop();
							Symbol T = new Symbol("T");

							T.addAttribute("nq", X.getAttribute("nq"));
							T.addAttribute("type", C.getAttribute("type"));
							T.addAttribute("width", C.getAttribute("width"));
							symbolStack.push(T);
						}else if (r_num==22){
							// T->record D{T.nq = D.nq;}
							Symbol D = symbolStack.pop();
							symbolStack.pop();
							Symbol T = new Symbol("T");
							T.addAttribute("nq", D.getAttribute("nq"));
							T.addAttribute("type", "record");
							symbolStack.push(T);
						}else if (r_num==23){
							// E->E + E {E.nq = E1.nq; E.addr=newtemp();gen(E.addr=E1.addr+E2.addr);}
							Symbol E2 = symbolStack.pop();
							Symbol plus = symbolStack.pop();  // "*"
							Symbol E1 = symbolStack.pop();
							Symbol E = new Symbol("E");

							E.addAttribute("nq", E1.getAttribute("nq"));
							if (E1.getAttribute("addr").matches("[0-9]*") && E2.getAttribute("addr").matches("[0-9]*")){
								E.addAttribute("addr", String.valueOf(Integer.parseInt(E1.getAttribute("addr")) +
									Integer.parseInt(E2.getAttribute("addr"))));
								gen("t"+localVarNumber+" = "+E1.getAttribute("addr")+" + "+ E2.getAttribute("addr"), intercode);
								addFour("+", E1.getAttribute("addr"), E2.getAttribute("addr"), "t"+localVarNumber, four);
								localVarNumber++;
							}else{
								String errorMessage = "Error at line["+plus.getAttribute("line")+"]"+": [calculated component mismatch]";
								System.out.println(errorMessage);
								errorMessages.append(errorMessage).append("\n");
							}
							symbolStack.push(E);
						} else if (r_num==24){
							 // E->E * E {E.nq = E1.nq; E.addr=newtemp();gen(E.addr=E1.addr*E2.addr);
							Symbol E1 = symbolStack.pop();
							symbolStack.pop();  // "*"
							Symbol E2 = symbolStack.pop();
							stateStack.pop();
							Symbol E = new Symbol("E");

							E.addAttribute("nq", E1.getAttribute("nq"));
							E.addAttribute("addr", String.valueOf(Integer.parseInt(E1.getAttribute("addr")) *
								Integer.parseInt(E2.getAttribute("addr"))));
							gen("t"+localVarNumber+" = "+E1.getAttribute("addr")+" * "+ E2.getAttribute("addr"), intercode);
							addFour("*", E1.getAttribute("addr"),E2.getAttribute("addr"), "t"+localVarNumber, four);
							localVarNumber++;
							symbolStack.push(E);
						} else if (r_num==25){
							// E->- E  {E.nq = E1.nq; E.addr=newtemp();gen(E.addr=uminus E1.addr);}
							Symbol E1 = symbolStack.pop();
							stateStack.pop();
							Symbol E = new Symbol("E");

							E.addAttribute("nq", E1.getAttribute("nq"));
							E.addAttribute("addr", String.valueOf(-Integer.parseInt(E1.getAttribute("addr"))));
							gen("t"+localVarNumber+" = uminus "+E1.getAttribute("addr"), intercode);
							addFour("uminus",E1.getAttribute("addr"), "_", "t"+localVarNumber, four);
							localVarNumber++;
							symbolStack.push(E);
						} else if (r_num==26){
							 //E->( E ) {E.nq = E1.nq; E.addr=E1.addr}
							symbolStack.pop(); // "("
							Symbol E1 = symbolStack.pop();
							symbolStack.pop(); // ")"
							Symbol E = new Symbol("E");

							E.addAttribute("nq", E1.getAttribute("nq"));
							E.addAttribute("addr",E1.getAttribute("addr"));
							symbolStack.push(E);
						} else if (r_num==27){
							// E->id  {E.nq = nextquad; E.addr=lookup(id.lexeme);if E.addr==null then error}
							Symbol id = symbolStack.pop();
							Symbol E = new Symbol("E");

							E.addAttribute("nq", String.valueOf(intercode.size()+1));
							if(lookUpSymbolItem(symbolItem, id.getAttribute("lexeme"))){
								E.addAttribute("addr", id.getAttribute("lexeme"));
							}else {
								E.addAttribute("addr", null);
								String errorMessage = "Error at line["+id.getAttribute("line")+"]"+": ["
									+id.getAttribute("lexeme")+" not defined]";
								System.out.println(errorMessage);
								errorMessages.append(errorMessage).append("\n");
							}
							symbolStack.push(E);
						} else if (r_num==28){
							//E->digit {E.nq = nextquad; E.addr=lookup(digit.lexeme);if E.addr==null then error}
							Symbol digit = symbolStack.pop();
							Symbol E = new Symbol("E");
							E.addAttribute("nq", String.valueOf(intercode.size()+1));
							if(digit.getAttribute("value")==null){
								String errorMessage = "Error at line["+digit.getAttribute("line")+"]"+": ["
									+digit.getAttribute("value")+" not defined]";
								System.out.println(errorMessage);
								errorMessages.append(errorMessage).append("\n");
							}
							E.addAttribute("addr", digit.getAttribute("value"));
							symbolStack.push(E);
						}else if(r_num==29){
							//E->L {E.nq = L.nq; E.addr=newtemp(); gen(E.addr=L.array[L.offset];}
							Symbol L = symbolStack.pop();
							Symbol E = new Symbol("E");
							E.addAttribute("nq", L.getAttribute("nq"));
							E.addAttribute("addr", L.getAttribute("array")+"["+L.getAttribute("offset")+"]");
							gen("t"+localVarNumber+"="+L.getAttribute("array")+"["+L.getAttribute("offset")+"]", intercode);
							addFour("=", L.getAttribute("array")+"["+L.getAttribute("offset")+"]", "_", "t"+localVarNumber, four);
							localVarNumber++;
							symbolStack.push(E);
						} else if(r_num==30){
							// F->F1, E{ F.nq =F1.nq; 将E.addr添加到q的队尾;    } 特殊
							Symbol E = symbolStack.pop();
							symbolStack.pop();
							Symbol F1 = symbolStack.pop();
							Symbol F = new Symbol("F");
							F.addAttribute("nq", F1.getAttribute("nq"));
							q.offer(Integer.valueOf(E.getAttribute("addr")));
							symbolStack.push(F);
						}else if (r_num==31){
							// F->E{F.nq=E.nq;将q初始化为只包含E.addr;  }
							Symbol E = symbolStack.pop();
							Symbol F = new Symbol("F");
							F.addAttribute("nq", E.getAttribute("nq"));
							q = new LinkedList<>();
							q.offer(Integer.valueOf(E.getAttribute("addr")));
							symbolStack.push(F);
						}else if(r_num==32){
							//X->int {X.nq = nextquad; X.type=int;X.width=4;}
							symbolStack.pop();
							Symbol X = new Symbol("X");
							X.addAttribute("nq", String.valueOf(intercode.size()+1));
							X.addAttribute("type", "int");
							X.addAttribute("width", String.valueOf(4));
							symbolStack.push(X);
						} else if(r_num==33){
							//X->char {X.nq = nextquad; X.type=char;X.width=4;}
							symbolStack.pop();
							Symbol X = new Symbol("X");
							X.addAttribute("nq", String.valueOf(intercode.size()+1));
							X.addAttribute("type", "char");
							X.addAttribute("width", String.valueOf(4));
							symbolStack.push(X);
						}else if(r_num==34){
							//L->id [ E ] {L.nq = E.nq; L.array=lookup(id.lexeme);if L.array==null then error;
							//             L.type=L.array.type.elem; L.offset = newtemp(); gen(L.offset=E.addr*L.type.width);}
							symbolStack.pop();
							Symbol E = symbolStack.pop();
							symbolStack.pop();
							Symbol id = symbolStack.pop();
							Symbol L = new Symbol("L");

							L.addAttribute("nq", E.getAttribute("nq"));
							SymbolItem symbolItem1 = null;
							for (SymbolItem oneSymbolItem : symbolItem){
								if (oneSymbolItem.getIdentifier().equals(id.getAttribute("lexeme"))){
									symbolItem1 = oneSymbolItem;
								}
							}

							if (symbolItem1==null){
								String errorMessage = "Error at line["+id.getAttribute("line")+"]"+": ["
									+id.getAttribute("lexeme")+" not defined]";
								System.out.println(errorMessage);
								errorMessages.append(errorMessage).append("\n");
							} else if (!symbolItem1.getType().contains("]")) {
								String errorMessage = "Error at line["+id.getAttribute("line")+"]"+": ["
									+id.getAttribute("lexeme")+" type error]";
								System.out.println(errorMessage);
								errorMessages.append(errorMessage).append("\n");
							}else{
								L.addAttribute("array", symbolItem1.getIdentifier());
								L.addAttribute("type", getArrayELemType(symbolItem1.getType()));
								L.addAttribute("offset", String.valueOf(Integer.parseInt(E.getAttribute("addr"))*getTypeWidth(L.getAttribute("type"))));
								gen("t"+localVarNumber+"="+E.getAttribute("addr")+"*"+getTypeWidth(L.getAttribute("type"))
									, intercode);
								addFour("*", E.getAttribute("addr"), getTypeWidth(L.getAttribute("type"))+"", "t"+localVarNumber, four);
								localVarNumber++;
							}
							symbolStack.push(L);
						}
						else if(r_num==35){
							// L->L [ E ] {L.nq = L1.nq; L.array = L1.array;L.type=L1.type.elem;t=newtemp();gen(t=E.addr*L.type.width);
							// 						L.offset = newtemp(); gen(L.offset= L1.offset+t);}
							symbolStack.pop();
							Symbol E = symbolStack.pop();
							symbolStack.pop();
							Symbol L1 = symbolStack.pop();
							Symbol L = new Symbol("L");
							L.addAttribute("nq", L1.getAttribute("nq"));
							L.addAttribute("array", L1.getAttribute("array"));
							L.addAttribute("type", getArrayELemType(L1.getAttribute("type")));
							gen("t"+localVarNumber+"="+E.getAttribute("addr")+"*"+getTypeWidth(L1.getAttribute("type"))
								,intercode);
							addFour("*", E.getAttribute("addr"), getTypeWidth(L1.getAttribute("type"))+"", "t"+localVarNumber, four);
							localVarNumber++;
							int t = Integer.parseInt(E.getAttribute("addr")) * getTypeWidth(L1.getAttribute("type"));
							L.addAttribute("offset", String.valueOf(Integer.parseInt(L1.getAttribute("offset"))+t));
							gen("t"+localVarNumber+"="+L1.getAttribute("offset")+" + "+t, intercode);
							four.add("(+,"+L1.getAttribute("offset")+","+t
							+",t"+localVarNumber+")");
							localVarNumber++;
							symbolStack.push(L);
						}
					}
					else {
						System.out.println("erro");
						ti++;
						continue;
					}
				}
				else {
					int r_num = Integer.parseInt(action.substring(1));
					if(r_num==17){
						//C->no {C.nq = nextquad; C.type=t;C.width=4;}
						Symbol C = new Symbol("C");

						C.addAttribute("nq", String.valueOf(intercode.size()+1));
						C.addAttribute("type", localT);
						C.addAttribute("width", String.valueOf(4));
						symbolStack.push(C);
					}
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
				System.out.println("\n字母表");
				for(SymbolItem item :symbolItem){
					System.out.println(item);
				}
				System.out.println("\n三地址指令");
				int c=1;
				for(InterCode inco: intercode){
					System.out.println(c+": "+inco);
					++c;
				}
				System.out.println("\n四元式指令");
				c=1;
				for(String s:four) {
					System.out.println(c+": "+s);
					++c;
				}
				System.out.println("\n错误信息");
				System.out.println(errorMessages);
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
	 * 当找到了，返回true，没找到返回false
	 * @param symbolItems
	 * @param item
	 * @return
	 */
	static boolean lookUpSymbolItem(List<SymbolItem> symbolItems,String item) {
		for(SymbolItem symbolItem :symbolItems) {
			if(symbolItem.getIdentifier().equals(item))
				return true;
		}
		return false;
	}

//	static String lookUpAndGetSymbolItem(List<SymbolItem> symbolItems, String lexeme){
//		for(SymbolItem symbolItem :symbolItems) {
//			if(symbolItem.getIdentifier().equals(lexeme))
//				return symbolItem;
//		}
//
//	}
	static void gen(String inter,List<InterCode> intercode) {
		intercode.add(new InterCode(new String[]{inter}));
		System.out.println("gen: "+inter);
	}
	static List<Symbol> tokenToSymbol(List<Token> tokens) {
		List<Symbol> symbols = new ArrayList<Symbol>();
		for(Token t:tokens) {
			Symbol s = new Symbol(t.getKey());
			if(t.getKey().equals("decimalConstant")){
				s.addAttribute("value", t.getValue());
			}else {
				s.addAttribute("lexeme", t.getValue());
			}
			s.addAttribute("type", t.getKey());
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

	/**
	 * backpatch 回填函数
	 * @param list 回填的三元式地址
	 * @param value 回填的值
	 * @param interCodes 三元式列表
	 */
	static void backpatch(List<Integer> list, int value, List<InterCode> interCodes,List<String> four){
		for(int listNumber: list) {  //backpatch
			interCodes.get(listNumber-1).backPatch(String.valueOf(value));
			four.set(listNumber-1, four.get(listNumber-1)+","+String.valueOf(value)+")");
			
		}
	}
	static void addFour(String t1,String t2,String t3,String t4,List<String> four) {
		four.add("("+t1+","+t2+","+t3+","+t4+")");
	}
}


