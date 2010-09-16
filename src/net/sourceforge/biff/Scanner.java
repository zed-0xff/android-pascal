/*"Pascal interpreter written in Java" jest przeznaczony do 
 interpretacji kodu napisanego w języku Pascal.
Copyright (C) 2004/2005 Bartyna Waldemar, Faderewski Marek,
Fedorczyk Łukasz, Iwanowski Wojciech.
Niniejszy program jest wolnym oprogramowaniem; możesz go 
rozprowadzać dalej i/lub modyfikować na warunkach Powszechnej
Licencji Publicznej GNU, wydanej przez Fundację Wolnego
Oprogramowania - według wersji 2-giej tej Licencji lub którejś
z późniejszych wersji. 

Niniejszy program rozpowszechniany jest z nadzieją, iż będzie on 
użyteczny - jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domyślnej 
gwarancji PRZYDATNOŚCI HANDLOWEJ albo PRZYDATNOŚCI DO OKREŚLONYCH 
ZASTOSOWAŃ. W celu uzyskania bliższych informacji - Powszechna 
Licencja Publiczna GNU. 

Z pewnością wraz z niniejszym programem otrzymałeś też egzemplarz 
Powszechnej Licencji Publicznej GNU (GNU General Public License);
jeśli nie - napisz do Free Software Foundation, Inc., 675 Mass Ave,
Cambridge, MA 02139, USA.
*/

package net.sourceforge.biff;

import java.io.*;
import java.util.ArrayList;

import me.zed_0xff.android.pascal.*;

public class Scanner {

	public ArrayList lexems = new ArrayList();

	// variables for validation process
	ArrayList vars = new ArrayList();
	ArrayList var_types = new ArrayList();
	ArrayList var_vals = new ArrayList();
	// position in Advenced Lexems Array
	private int pos = 0;
	// position during instruction evaluating
	private int place = 0;
	private int main_begin = 0;

	public String stderr;
	public String stdout;
        
        private Main activity;

	public Scanner(String data, Main activity_) throws Exception {

		stderr = "";
		stdout = "";
                activity = activity_;

		StringReader filein = new StringReader(data);

		// array of 'lexem' objects
		lexems = new ArrayList();

		// current lexem
		StringBuffer value = new StringBuffer();

		// current type of lexem
		byte type = 0;

		// current line
		int line = 1;

		// current column
		int column = 1;

		// where the lexem began
		int began = 1;
		
		// where the String began
		int sline = 1;

		int bite = filein.read();
		while (bite != -1) {
			// if bite is a quotation mark
			if (bite == 34) {
				if (type != 0) {
					lexems.add(new Lexem(value, type, line, began));
					value = new StringBuffer();
				}
				if (type != 5) {
					type = 5;
					began = column;
					sline = line;
				}
				else
					type = 0;
			}
			else if (type == 5) {
				value.append((char)bite);
				if (bite == 13) {
					line ++;
					column = -1;
				}
			}
			// if bite is a digit
			else if ((bite >= 48) && (bite <= 57)) {
				if (type == 0) {
					type = 1;
					began = column;
				}
				value.append((char)bite);
			}
			else
				// if bite is a letter
				if (((bite >= 65) && (bite <= 90)) ||
					((bite >= 97) && (bite <= 122))) {
					if (type == 0) {
						type = 3;
						began = column;
						value.append((char)bite);
					}
					else
						if (type == 3)
							value.append((char)bite);
						else {
							type = 7;
							break;
						}
				}
				else
					// if bite is a sign
					if (((bite >= 40) && (bite <= 45)) ||
						 (bite == 47) ||
						((bite >= 58) && (bite <= 62))) {
						if (type != 0) {
							lexems.add(new Lexem(value, type, line, began));
							type = 0;
							value = new StringBuffer();
						}
						lexems.add(new Lexem(bite, 4, line, column));
					}
					else
						// if bite is a dot
						if (bite == 46)
							if (type != 0)
								if (type == 3)
									if (((value.toString())
										  .toUpperCase()).equals("END")) {
										lexems.add(new Lexem("END.",
												   (byte)3, line, began));
										type = 0;
										value = new StringBuffer();
									}
									else {
										type = 8;
										break;
									}
								else
									if (type == 1) {
										type = 2;
										value.append('.');
									}
									else {
										type = 8;
										break;
									}
							else {
								type = 8;
								break;
							}
						else
							// if bite is a separator
							if ((bite ==  9) || (bite == 32) ||
								(bite == 13) || (bite == 10)) {
									if (type != 0) {
										lexems.add(new Lexem(value,
												   type, line, began));
										type = 0;
										value = new StringBuffer();
									}
									if (bite == 13) {
										line ++;
										column = -1;
									}
								}
							// bite is not allowed character
							else {
								type = 9;
								break;
							}
			bite = filein.read();
			if ((bite == -1) && (type == 5))
				type = 6;
			else if (bite == -1) 
				if (type != 0) {
					lexems.add(new Lexem(value, type, line, began));
					type = 0;
					value = new StringBuffer();
				}
			column ++;
		}

		// error handling
		if (type == 6)
			err("ERROR: String not closed ! "
				+ "line: " + sline + ", column: " + began);
		else if (type == 7)
			err("ERROR: letter joined with number ! "
				+ "line: " + line + ", column: " + column);
		else
			if (type == 8)
				err("ERROR: dot in wrong place ! "
			  		+ "line: " + line + ", column: " + column);
			else
				if (type == 9)
					err("ERROR: illegal character !: "
						+ (char)bite
			  			+ " line: " + line + ", column: " + column);
				else {
					// if everything is OK
//					printLexems(lexems);
					// advenced scunning (father, deeper clasification)
					ArrayList AdvL = advScanning(lexems);
//                	printAdvLexems(AdvL);
					// validation process
                	String tmp_result = checkStructure(AdvL);
          			if (tmp_result.equals("No errors !")) {
          				//System.out.println("===================");
          				// variables initialization
          				initializeVars();
          				pos = main_begin;
          				// interpretation process
          				ArrayList ExecL = interBody(AdvL);
          				// execution
          				execute(ExecL);
          				
          			
/*    
	System.out.println("IAL Size = " + ExecL.size());  
	for (int i = 0; i < ExecL.size(); i++) {
		System.out.println(((ExecLexem)ExecL.get(i)).type);
		System.out.println(((ExecLexem)ExecL.get(i)).variable);
		System.out.println("If TRUE SIZE = " + ((ExecLexem)ExecL.get(i)).
															iftrue.size());
		for (int j = 0; j < ((ExecLexem)ExecL.get(i)).lexems.length; j++)
			System.out.println(((ExecLexem)ExecL.get(i)).lexems[j].value);
	} 	*/
	
} else {
         			err("ERROR!");
         			err(tmp_result);
}
				}
	}// Scanner()

	public void printLexems(ArrayList lex) {
		System.out.println("\nLEXEMS: " + lex.size());
		System.out.println("VALUE   TYPE   LINE   COLUMN");
		Object[] lexems = lex.toArray();
		for (int i = 0; i < lexems.length; i ++) {
			Lexem lexem = (Lexem)lexems[i];
			System.out.println(lexem.value + "   " + lexem.type + "   "
							 + lexem.line + "   " + lexem.column);
		}
		System.out.println();
	}// printLexems(ArrayList)
	
	public void printAdvLexems(ArrayList AdvLex) {
		System.out.println("\nLEXEMS: " + AdvLex.size());
		System.out.println("VALUE   CODE   LCATEGORY   "
						 + "HCATEGORY   LINE   COLUMN");
		Object[] AdvLexems = AdvLex.toArray();
		for (int i = 0; i < AdvLexems.length; i ++) {
			AdvLexem advlexem = (AdvLexem)AdvLexems[i];
			System.out.println(advlexem.value + "   " + advlexem.code + "   "
							 + advlexem.lcategory + "   " 
							 + advlexem.hcategory + "   "
							 + advlexem.line + "   " + advlexem.column);
		}
		System.out.println();
	}// printLexems(ArrayList)

	public ArrayList advScanning(ArrayList lexems) {
		ArrayList AdvLexems = new ArrayList();
		String value = "";
		Codes codes = new Codes();
		codes.fillLists();
		Lexem lexem = null;
		byte code = 0;
		byte lcategory = 0;
		byte hcategory = 0;
		
		int size = lexems.size();
		int i = 0;
		while (i < size) {
			lexem = (Lexem)lexems.get(i);
			
			// if lexem is a word
			if (lexem.type == codes.STRING)
				AdvLexems.add(new AdvLexem(lexem.value, codes.STRING,
							  codes.STRING, codes.STRING,
							  lexem.line, lexem.column));
			else if (lexem.type == codes.WORD) {
				hcategory = codes.WORD;
				code = codes.getKeywordCode(lexem.value);

				if (code == codes.VARIABLE)
					lcategory = codes.VARIABLE;
				else if ((code >= codes.AND) && (code <= codes.OR))
					lcategory = codes.LOPERATOR;
				else if ((code >= codes.INTEGER) && (code <= codes.BOOLEAN))
					lcategory = codes.VTYPE;					
				else
					lcategory = codes.KEYWORD;
				
				AdvLexems.add(new AdvLexem(lexem.value, code,
							  lcategory, hcategory,
							  lexem.line, lexem.column));
			}
			
			// if lexem is a sign
			else if (lexem.type == codes.SIGN) {
				value = lexem.value;;
				hcategory = codes.SIGN;
				code = codes.getSignCode(lexem.value);
				
				if ((code >= codes.COLON) && (code <= codes.QUOTATION)) {
					lcategory = codes.OOPERATOR;
					if (lexem.value.equals(":"))
						if ((i + 1) < size)
							if (((Lexem)lexems.get(i + 1)).value.equals("=")) {
								code = codes.ASOCIATE;
								value = ":=";
								i ++;
							}
				}
				else if ((code >= codes.E) && (code <= codes.LE)) {
					lcategory = codes.ROPERATOR;
					if (lexem.value.equals("<"))
						if ((i + 1) < size) {
							if (((Lexem)lexems.get(i + 1)).value.equals("=")) {
								code = codes.LE;
								value = "<=";
								i ++;
							}
							if (((Lexem)lexems.get(i + 1)).value.equals(">")) {
								code = codes.NE;
								value = "<>";
								i ++;
							}
						}
					else if (lexem.value.equals(">"))
						if ((i + 1) < size)
							if (((Lexem)lexems.get(i + 1)).value.equals("=")) {
								code = codes.GE;
								value = ">=";
								i ++;
							}
				}
				else if ((code >= codes.PLUS) && (code <= codes.DIV)) {
					lcategory = codes.AOPERATOR;
				}
				AdvLexems.add(new AdvLexem(value, code,
							  lcategory, hcategory,
							  lexem.line, lexem.column));
			}
			else
				AdvLexems.add(new AdvLexem(lexem.value, lexem.type,
							  codes.NUMBER, codes.WORD,
							  lexem.line, lexem.column));
			i ++;
		}
		return AdvLexems;
	}// advScanning()

	private String errorMess(String expected, AdvLexem found) {
		return ("expected: " + expected
			 + "\n   found: " + found.value
			 + "\n   line: " + found.line
			 + "\n   column: " + found.column);
	}// errorMess()

	private String errorMess(String expected) {
		return ("expected: " + expected
			 + "   nothing found");
	}// errorMess()
	
	
	// checks if there is a lexem of given Code
	private String checkC(String s, byte code, ArrayList AL) {
		if (pos < AL.size()) {
			AdvLexem al = (AdvLexem)AL.get(pos);
			if (al.code != code)
				return errorMess(s, al);
		}
		else
			return errorMess(s);
		return "";
	}// checkC()

	private String checkC(String s, byte code, ArrayList AL, int which) {
		if (which < AL.size()) {
			AdvLexem al = (AdvLexem)AL.get(which);
			if (al.code != code)
				return errorMess(s, al);
		}
		else
			return errorMess(s);
		return "";
	}// checkC()
	
	// checks if there is a lexem of given Lower category
	private String checkL(String s, byte code, ArrayList AL) {
		if (pos < AL.size()) {
			AdvLexem al = (AdvLexem)AL.get(pos);
			if (al.lcategory != code)
				return errorMess(s, al);
		}
		else
			return errorMess(s);
		return "";
	}// checkL()
	
// mmmmmmmmmmmmmmmmmmm	VALIDATION	mmmmmmmmmmmmmmmmmmmmmmmmmmm
	
	public String checkStructure(ArrayList AL) {		
		Codes codes = new Codes();
		codes.fillLists();
		String result = "";
		AdvLexem al;
		int size = AL.size();
		result = checkC("PROGRAM", codes.PROGRAM, AL);
		if (!(result.equals("")))
			return result;
		pos ++;
		result = checkC("VARIABLE", codes.VARIABLE, AL);
		if (!(result.equals("")))
			return result;
		pos ++;
		result = checkC("SEMICOLON", codes.SEMICOLON, AL);
		if (!(result.equals("")))
			return result;
		pos ++;
		result = checkC("VAR", codes.VAR, AL);
		if (!(result.equals(""))) {
			result = checkC("VAR or BEGIN", codes.BEGIN, AL);
			if (!(result.equals("")))
				return result;
		}
		if ((((AdvLexem)AL.get(pos)).value).equals("VAR")) {
			pos ++;
			result = checkVAR(AL);
			if (!(result.equals("")))
				return result;
		}

		// now we check if there is END. at the end of the program
		if (AL.size() - 1 > pos) {
			result = checkC("END.", Codes.END_DOT, AL, AL.size() - 1);
			if (!(result.equals("")))
				return result;
		}
		else
			return "expected: END. at the end of program";

		// now we check if all VARIABLES are declared
		for (int i = pos + 1; i < AL.size() - 2; i ++) {
			al = (AdvLexem)(AL.get(i));
			if (al.code == Codes.VARIABLE)
				if (!(vars.contains(al.value)))
					return "cannot resolve symbol variable " + al.value
						 + "\n   line: " + al.line
						 + "\n   column: " + al.column;
		}
		
		pos ++;
		main_begin = pos;
		result = checkBody(AL);

/*
//testing...
System.out.println("Position = " + pos);
al = (AdvLexem)AL.get(pos);
System.out.println("Lexem value = " + al.value);
*/
		
		if (!(result.equals("")))
			return result;

		return "No errors !";
	}// checkStructure

	private String checkVAR(ArrayList AL) {
		byte var_count = 0;
		String result = "";
		AdvLexem al = null;
		// if BEGIN appears changes to TRUE
		boolean finish_b = false;
		// if there is no COMMA changes to TRUE
		boolean finish_c = false;

		while (!finish_b) {
			result = checkC("BEGIN", Codes.BEGIN, AL);
			// if there is no BEGIN yet
			if (!result.equals("")) {
				result = checkC("VARIABLE", Codes.VARIABLE, AL);
				// if there is a VARIABLE
				if (!result.equals(""))
					return result;
				al = (AdvLexem)AL.get(pos);
				if ((((AdvLexem)AL.get(1)).value).equals(al.value))
					return "VARIABLE is already used as a neme of the program"
						 + "\n   line: " + al.line
						 + "\n   column: " + al.column;
				if (vars.contains(al.value))
					return ("VARIABLE already declared: " + al.value
						 + "\n   line: " + al.line
						 + "\n   column: " + al.column);
				vars.add(al.value);
				var_count ++;
				finish_c = false;
				while (!finish_c) {
					pos ++;
					result = checkC("COMMA", Codes.COMMA, AL);
					if (result.equals("")) {
						pos ++;
						result = checkC("VARIABLE", Codes.VARIABLE, AL);
						if (!result.equals(""))
							return result;
						al = (AdvLexem)AL.get(pos);
						if ((((AdvLexem)AL.get(1)).value).equals(al.value))
							return "VARIABLE is already used as a neme "
							     + "of the program"
								 + "\n   line: " + al.line
								 + "\n   column: " + al.column;
						if (vars.contains(al.value))
							return ("VARIABLE already declared: " + al.value
								 + "\n   line: " + al.line
								 + "\n   column: " + al.column);
						vars.add(al.value);
						var_count ++;
					}
					else
						finish_c = true;
				}// while
				result = checkC("COLON", Codes.COLON, AL);
				if (!result.equals(""))
					return result;
				pos ++;
				result = checkL("VARIABLE TYPE", Codes.VTYPE, AL);
				if (!result.equals(""))
					return result;

				byte code = ((AdvLexem)AL.get(pos)).code;
				for (byte i = 0; i < var_count; i ++)
					var_types.add(new Integer(code));
				var_count = 0;
				
				pos ++;
				result = checkC("SEMICOLON", Codes.SEMICOLON, AL);
				if (!result.equals(""))
					return result;	
				pos ++;	
			}
			else 
				finish_b = true;
		}// while
		return "";
	}// checkVAR()

	private String checkIASSOC(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		byte b_count = 0; // brackets count
		boolean finish_b = false; // finish if no more brackets

		while (!finish_b) {
			result = checkC("VALUE", Codes.LBRACKET, AL);
			if (!result.equals(""))
				finish_b = true;
			else {
				b_count ++;
				pos ++;
			}
		}
		result = checkC("VALUE", Codes.MINUS, AL);
		if (result.equals("")) {
			pos ++;
			result = checkC("INTEGER NUMBER", Codes.INUMBER, AL);
			if (result.equals("")) {
				al = (AdvLexem)AL.get(pos);
				AL.set(pos - 1, new AdvLexem("-" + al.value, al.code,
											  al.lcategory, al.hcategory,
											  al.line, al.column));
				AL.remove(pos);
			}
			else
				return result;
		}
		else {
			result = checkC("INTEGER VALUE", Codes.INUMBER, AL);
			if (!result.equals("")) {
				result = checkC("INTEGER VARIABLE", Codes.VARIABLE, AL);
				if (!result.equals(""))
					return result;			
				else {
					al = (AdvLexem)AL.get(pos);
					int tmp = vars.indexOf(al.value);
					if (!(((Integer)var_types.get(tmp)).equals(
										new Integer(Codes.INTEGER)))) {
						al = (AdvLexem)AL.get(pos);
						return "INTEGER VARIABLE expected "
							 + "\n   found: " + al.value
							 + "\n   line: " + al.line
							 + "\n   column: " + al.column;
					}
					else
						pos++;
				}
			}
			else
				pos ++;
		}
		finish_b = false;
		while (!finish_b) {
			result = checkC("OPERATOR", Codes.RBRACKET, AL);
			if (!result.equals(""))
				finish_b = true;
			else {
				if (b_count > 0) {
					b_count --;
					pos ++;
				}
				else {
					al = (AdvLexem)AL.get(pos);
					return "RIGHT BRACKET not opened"
							 + "\n   line: " + al.line
							 + "\n   column: " + al.column;
				}
			}
		}
		boolean finish_o = false; // if there is no operator
		while (!finish_o){
			result = checkL("ARITHMETIC OPERATOR", Codes.AOPERATOR, AL);
			if (!result.equals(""))
				finish_o = true;
			else {
				pos ++;
				finish_b = false;
				while (!finish_b) {
					result = checkC("VALUE", Codes.LBRACKET, AL);
					if (!result.equals(""))
						finish_b = true;
					else {
						b_count ++;
						pos ++;
					}
				}
				result = checkC("VALUE", Codes.MINUS, AL);
				if (result.equals("")) {
					pos ++;
					result = checkC("INTEGER NUMBER", Codes.INUMBER, AL);
					if (result.equals("")) {
						al = (AdvLexem)AL.get(pos);
						AL.set(pos - 1, new AdvLexem("-" + al.value, al.code,
													  al.lcategory,
													  al.hcategory,
													  al.line, al.column));
						AL.remove(pos);
					}
					else
						return result;
				}
				else {
					result = checkC("INTEGER VALUE", Codes.INUMBER, AL);
					if (!result.equals("")) {
						result = checkC("INTEGER VARIABLE", Codes.VARIABLE, AL);
						if (!result.equals(""))
							return result;			
						else {
							al = (AdvLexem)AL.get(pos);
							int tmp = vars.indexOf(al.value);
							if (!(((Integer)var_types.get(tmp)).equals(
												new Integer(Codes.INTEGER)))) {
								al = (AdvLexem)AL.get(pos);
								return "INTEGER VARIABLE expected "
									 + "\n   found: " + al.value
									 + "\n   line: " + al.line
									 + "\n   column: " + al.column;
							}
							else
								pos++;
						}
					}
					else
						pos ++;
				}
				finish_b = false;
				while (!finish_b) {
					result = checkC("OPERATOR", Codes.RBRACKET, AL);
					if (!result.equals(""))
						finish_b = true;
					else {
						if (b_count > 0) {
							b_count --;
							pos ++;
						}
						else {
							al = (AdvLexem)AL.get(pos);
							return "RIGHT BRACKET not opened"
									 + "\n   line: " + al.line
									 + "\n   column: " + al.column;
						}
					}
				}				
			}
		}
		
		return "";
	}// checkIASSOC()
	
	private String checkRASSOC(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		byte b_count = 0; // brackets count
		boolean finish_b = false; // finish if no more brackets

		while (!finish_b) {
			result = checkC("VALUE", Codes.LBRACKET, AL);
			if (!result.equals(""))
				finish_b = true;
			else {
				b_count ++;
				pos ++;
			}
		}
		result = checkC("VALUE", Codes.MINUS, AL);
		if (result.equals("")) {
			pos ++;
			result = checkC("INTEGER OR REAL NUMBER", Codes.INUMBER, AL);
			if ((result.equals("")) ||
				(checkC("", Codes.RNUMBER, AL).equals(""))) {
				al = (AdvLexem)AL.get(pos);
				AL.set(pos - 1, new AdvLexem("-" + al.value, al.code,
											  al.lcategory, al.hcategory,
											  al.line, al.column));
				AL.remove(pos);
			}
			else
				return result;
		}
		else {
			result = checkC("", Codes.INUMBER, AL);
			if (!((result.equals("")) ||
				(checkC("", Codes.RNUMBER, AL).equals("")))) {
				result = checkC("INTEGER OR REAL VARIABLE",
								 Codes.VARIABLE, AL);
				if (!result.equals(""))
					return result;
				else {
					al = (AdvLexem)AL.get(pos);
					int tmp = vars.indexOf(al.value);
					int type = ((Integer)var_types.get(tmp)).intValue();
					if (!((type == Codes.INTEGER) || (type == Codes.REAL))) {
						al = (AdvLexem)AL.get(pos);
						return "INTEGER OR REAL VARIABLE expected "
							 + "\n   found: " + al.value
							 + "\n   line: " + al.line
							 + "\n   column: " + al.column;
					}
					else
						pos++;
				}
			}
			else
				pos ++;
		}
		finish_b = false;
		while (!finish_b) {
			result = checkC("OPERATOR", Codes.RBRACKET, AL);
			if (!result.equals(""))
				finish_b = true;
			else {
				if (b_count > 0) {
					b_count --;
					pos ++;
				}
				else {
					al = (AdvLexem)AL.get(pos);
					return "RIGHT BRACKET not opened"
							 + "\n   line: " + al.line
							 + "\n   column: " + al.column;
				}
			}
		}
		boolean finish_o = false; // if there is no operator
		while (!finish_o){
			result = checkL("closing BRACKET", Codes.AOPERATOR, AL);
			if (!result.equals("")) {
				finish_o = true;
				if (b_count > 0)
				return result;
			}
			else {
				pos ++;
				finish_b = false;
				while (!finish_b) {
					result = checkC("VALUE", Codes.LBRACKET, AL);
					if (!result.equals(""))
						finish_b = true;
					else {
						b_count ++;
						pos ++;
					}
				}
				result = checkC("VALUE", Codes.MINUS, AL);
				if (result.equals("")) {
					pos ++;
					result = checkC("INTEGER OR REAL NUMBER",
									 Codes.INUMBER, AL);
					if ((result.equals("")) ||
						(checkC("", Codes.RNUMBER, AL).equals(""))) {
						al = (AdvLexem)AL.get(pos);
						AL.set(pos - 1, new AdvLexem("-" + al.value, al.code,
													  al.lcategory,
													  al.hcategory,
													  al.line, al.column));
						AL.remove(pos);
					}
					else
						return result;
				}
				else {
					result = checkC("", Codes.INUMBER, AL);
					if (!((result.equals("")) ||
						(checkC("", Codes.RNUMBER, AL).equals("")))) {
						result = checkC("INTEGER OR REAL VARIABLE",
										 Codes.VARIABLE, AL);
						if (!result.equals(""))
							return result;			
						else {
							al = (AdvLexem)AL.get(pos);
							int tmp = vars.indexOf(al.value);
							int type = ((Integer)var_types.get(tmp))
																.intValue();
							if (!((type == Codes.INTEGER) ||
								 (type == Codes.REAL))) {
								al = (AdvLexem)AL.get(pos);
								return "INTEGER OR REAL VARIABLE expected "
									 + "\n   found: " + al.value
									 + "\n   line: " + al.line
									 + "\n   column: " + al.column;
							}
							else
								pos++;
						}
					}
					else
						pos ++;
				}
				finish_b = false;
				while (!finish_b) {
					result = checkC("OPERATOR", Codes.RBRACKET, AL);
					if (!result.equals(""))
						finish_b = true;
					else {
						if (b_count > 0) {
							b_count --;
							pos ++;
						}
						else {
							al = (AdvLexem)AL.get(pos);
							return "RIGHT BRACKET not opened"
									 + "\n   line: " + al.line
									 + "\n   column: " + al.column;
						}
					}
				}				
			}
		}		
		return "";
	}// checkRASSOC()

	private String checkLEXPR(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		byte b_count = 0; // brackets count
		boolean finish_o = false;
		boolean finish_n = false;
		boolean finish_b = false;		

		while (!finish_n) {
			finish_b = false;
			while (!finish_b) {
				result = checkC("VALUE", Codes.LBRACKET, AL);
				if (!result.equals(""))
					finish_b = true;
				else {
					b_count ++;
					pos ++;
				}
			}
			result = checkC("NOT", Codes.NOT, AL);
			if (!result.equals(""))
				finish_n = true;
			else
				pos ++;
		}
		result = checkLEXPRNElem(AL);
		if (result.equals("")) {
			result = checkL("RELATION OPERATOR",
							 Codes.ROPERATOR, AL);
			if (!result.equals(""))
				return result;
			else {
				pos ++;
				result = checkLEXPRNElem(AL);
				if (!result.equals(""))
					return result;
			}
		}
		else {
			result = checkLEXPRLElem(AL);
			if (!result.equals(""))
				return result;
		}
		finish_b = false;
		while (!finish_b) {
			result = checkC("OPERATOR", Codes.RBRACKET, AL);
			if (!result.equals(""))
				finish_b = true;
			else {
				if (b_count > 0) {
					b_count --;
					pos ++;
				}
				else {
					al = (AdvLexem)AL.get(pos);
					return "RIGHT BRACKET not opened"
							 + "\n   line: " + al.line
							 + "\n   column: " + al.column;
				}
			}
		}
		finish_o = false;
		while (!finish_o){
			result = checkL("closing BRACKET", Codes.LOPERATOR, AL);
			if (!result.equals("")) {
				finish_o = true;
				if (b_count > 0)
				return result;
			}
			else {
				pos ++;
				finish_n = false;
				while (!finish_n) {
					finish_b = false;
					while (!finish_b) {
						result = checkC("VALUE", Codes.LBRACKET, AL);
						if (!result.equals(""))
							finish_b = true;
						else {
							b_count ++;
							pos ++;
						}
					}
					result = checkC("NOT", Codes.NOT, AL);
					if (!result.equals(""))
						finish_n = true;
					else
						pos ++;
				}
				result = checkLEXPRNElem(AL);
				if (result.equals("")) {
					result = checkL("RELATION OPERATOR",
									 Codes.ROPERATOR, AL);
					if (!result.equals(""))
						return result;
					else {
						pos ++;
						result = checkLEXPRNElem(AL);
						if (!result.equals(""))
							return result;
					}
				}
				else {
					result = checkLEXPRLElem(AL);
					if (!result.equals(""))
						return result;
				}
				finish_b = false;
				while (!finish_b) {
					result = checkC("OPERATOR", Codes.RBRACKET, AL);
					if (!result.equals(""))
						finish_b = true;
					else {
						if (b_count > 0) {
							b_count --;
							pos ++;
						}
						else {
							al = (AdvLexem)AL.get(pos);
							return "RIGHT BRACKET not opened"
									 + "\n   line: " + al.line
									 + "\n   column: " + al.column;
						}
					}
				}
			}
		}
		return "";
	}// checkLEXPR()
	
	private String checkWRITE(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		result = checkC("LEFT BRACKET", Codes.LBRACKET, AL);
		if (!result.equals(""))
			return result;
		pos ++;
		result = checkC("STRING or VARIABLE", Codes.STRING, AL);
		if (!(result.equals("") ||
			(checkC("", Codes.VARIABLE, AL)).equals("")))
			return result;
		pos ++;
		boolean finish_c = false;
		while (!finish_c) {
			result = checkC("COMMA", Codes.COMMA, AL);
			if (!result.equals(""))
				finish_c = true;
			else {
				pos ++;
				result = checkC("STRING or VARIABLE", Codes.STRING, AL);
				if (!(result.equals("") ||
					(checkC("", Codes.VARIABLE, AL)).equals("")))
					return result;
				else
					pos ++;			
			}
		}
		result = checkC("RIGHT BRACKET", Codes.RBRACKET, AL);
		if (!result.equals(""))
			return result;
		pos ++;
		return "";
	}// checkWRITE()
	
	private String checkREAD(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		result = checkC("LEFT BRACKET", Codes.LBRACKET, AL);
		if (!result.equals(""))
			return result;
		pos ++;
		result = checkC("VARIABLE", Codes.VARIABLE, AL);		
		if (!result.equals(""))
			return result;
		pos ++;
		result = checkC("RIGHT BRACKET", Codes.RBRACKET, AL);
		if (!result.equals(""))
			return result;
		pos ++;		
		return "";
	}// checkREAD()


//mmmmmmmmmmmmmmmmmm	MAIN VALIDATOR   mmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
	
	private String checkBody(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		int begins = 0;

		while (true) {
			al = (AdvLexem)AL.get(pos);
			if (al.code == Codes.VARIABLE) {
				pos ++;
				result = checkC("ASSOCIATE MARK", Codes.ASOCIATE, AL);
				if (!result.equals(""))
					return result;
				pos ++;
				int tmp = vars.indexOf(al.value);
				int type = ((Integer)var_types.get(tmp)).intValue();
				if (type == Codes.INTEGER) {
					result = checkIASSOC(AL);
					if (!result.equals(""))
						return result;
				}
				else if (type == Codes.REAL) {
					result = checkRASSOC(AL);
					if (!result.equals(""))
						return result;
				}
				else if (type == Codes.BOOLEAN) {
					result = checkLEXPR(AL);
					if (!result.equals(""))
						return result;
				}
				result = checkEnding(AL);
				if (!result.equals(""))
					return result;
			}
			else if (al.code == Codes.WRITELN) {
				pos ++;
				result = checkWRITE(AL);
				if (!result.equals(""))
					return result;
				result = checkEnding(AL);
				if (!result.equals(""))
					return result;
			}
			else if (al.code == Codes.READLN) {
				pos ++;
				result = checkREAD(AL);
				if (!result.equals(""))
					return result;
				result = checkEnding(AL);
				if (!result.equals(""))
					return result;
			}
			else if (al.code == Codes.WHILE) {
				pos ++;
				result = checkLEXPR(AL);
				if (!result.equals(""))
					return result;
				result = checkC("keyword DO", Codes.DO, AL);
				if (!result.equals(""))
					return result;
				pos ++;
				result = checkOne(AL);
				if (!result.equals(""))
					return result;				
			}
			else if (al.code == Codes.IF) {
				pos ++;
				result = checkLEXPR(AL);
				if (!result.equals(""))
					return result;
				result = checkC("keyword THEN", Codes.THEN, AL);
				if (!result.equals(""))
					return result;
				pos ++;
				result = checkOne(AL);
				if (!result.equals(""))
					return result;
				result = checkC("keyword ELSE", Codes.ELSE, AL);
				if (result.equals("")) {
					pos ++;
					result = checkOne(AL);
					if (!result.equals(""))
						return result;
				}
			}
			else if (al.code == Codes.BEGIN) {
				AL.remove(pos);
				result = checkBlock(AL);
				if (!result.equals(""))
					return result;
				else {
					pos --;
					result = checkC("SEMICOLON", Codes.SEMICOLON, AL);
					if (result.equals("")) {
						AL.remove(pos);
						pos --;
					}
					AL.remove(pos);

				}
			}
			else if (al.code == Codes.END) {
				return "END without coresponding BEGIN"
					 + "\n   found: " + al.value
					 + "\n   line: " + al.line
					 + "\n   column: " + al.column;	
			}
			else if (al.code == Codes.END_DOT) {
				return "";
			}
			else
				return "Not a statement"
					 + "\n   found: " + al.value
					 + "\n   line: " + al.line
					 + "\n   column: " + al.column;
		}
	}// checkBODY()	

	private String checkBlock(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		int begins = 0;

		while (true) {
			al = (AdvLexem)AL.get(pos);
			if (al.code == Codes.VARIABLE) {
				pos ++;
				result = checkC("ASSOCIATE MARK", Codes.ASOCIATE, AL);
				if (!result.equals(""))
					return result;
				pos ++;
				int tmp = vars.indexOf(al.value);
				int type = ((Integer)var_types.get(tmp)).intValue();
				if (type == Codes.INTEGER) {
					result = checkIASSOC(AL);
					if (!result.equals(""))
						return result;
				}
				else if (type == Codes.REAL) {
					result = checkRASSOC(AL);
					if (!result.equals(""))
						return result;
				}
				else if (type == Codes.BOOLEAN) {
					result = checkLEXPR(AL);
					if (!result.equals(""))
						return result;
				}
				result = checkEnding(AL);
				if (!result.equals(""))
					return result;
			}
			else if (al.code == Codes.WRITELN) {
				pos ++;
				result = checkWRITE(AL);
				if (!result.equals(""))
					return result;
				result = checkEnding(AL);
				if (!result.equals(""))
					return result;
			}
			else if (al.code == Codes.READLN) {
				pos ++;
				result = checkREAD(AL);
				if (!result.equals(""))
					return result;
				result = checkEnding(AL);
				if (!result.equals(""))
					return result;
			}
			else if (al.code == Codes.WHILE) {
				pos ++;
				result = checkLEXPR(AL);
				if (!result.equals(""))
					return result;
				result = checkC("keyword DO", Codes.DO, AL);
				if (!result.equals(""))
					return result;
				pos ++;
				result = checkOne(AL);
				if (!result.equals(""))
					return result;				
			}
			else if (al.code == Codes.IF) {
				pos ++;
				result = checkLEXPR(AL);
				if (!result.equals(""))
					return result;
				result = checkC("keyword THEN", Codes.THEN, AL);
				if (!result.equals(""))
					return result;
				pos ++;
				result = checkOne(AL);
				if (!result.equals(""))
					return result;
				result = checkC("keyword ELSE", Codes.ELSE, AL);
				if (result.equals("")) {
					pos ++;
					result = checkOne(AL);
					if (!result.equals(""))
						return result;
				}
			}
			else if (al.code == Codes.BEGIN) {
				pos ++;
				result = checkBlock(AL);
				if (!result.equals(""))
					return result;
			}
			else if (al.code == Codes.END) {
				pos ++;
				result = checkEnding(AL);
				if (!result.equals(""))
					return result;
				else
					return "";
			}
			else if (al.code == Codes.END_DOT) {
				return "END expected"
					 + "\n   line: " + al.line
					 + "\n   column: " + al.column;
			}
			else
				return "Not a statement"
					 + "\n   found: " + al.value
					 + "\n   line: " + al.line
					 + "\n   column: " + al.column;
		}
	}// checkBlock()

	private String checkOne(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		int begins = 0;
		al = (AdvLexem)AL.get(pos);
		if (al.code == Codes.VARIABLE) {

			pos ++;
			result = checkC("ASSOCIATE MARK", Codes.ASOCIATE, AL);
			if (!result.equals(""))
				return result;
			pos ++;
			int tmp = vars.indexOf(al.value);
			int type = ((Integer)var_types.get(tmp)).intValue();

			if (type == Codes.INTEGER) {
				result = checkIASSOC(AL);
				if (!result.equals(""))
					return result;
			}
			else if (type == Codes.REAL) {
				result = checkRASSOC(AL);
				if (!result.equals(""))
					return result;
			}
			else if (type == Codes.BOOLEAN) {
				result = checkLEXPR(AL);
				if (!result.equals(""))
					return result;
			}
			return checkEnding(AL);
		}
		else if (al.code == Codes.WRITELN) {
			pos ++;
			result = checkWRITE(AL);
			if (!result.equals(""))
				return result;
			return checkEnding(AL);
		}
		else if (al.code == Codes.READLN) {
			pos ++;
			result = checkREAD(AL);
			if (!result.equals(""))
				return result;
			return checkEnding(AL);
		}
		else if (al.code == Codes.WHILE) {
			pos ++;
			result = checkLEXPR(AL);
			if (!result.equals(""))
				return result;
			result = checkC("keyword DO", Codes.DO, AL);
			if (!result.equals(""))
				return result;
			pos ++;
			return checkOne(AL);
		}
		else if (al.code == Codes.IF) { 
			pos ++;
			result = checkLEXPR(AL);
			if (!result.equals(""))
				return result;
			result = checkC("keyword THEN", Codes.THEN, AL);
			if (!result.equals(""))
				return result;
			pos ++;

			result = checkOne(AL);
			if (!result.equals(""))
				return result;
			result = checkC("keyword ELSE", Codes.ELSE, AL);
			if (result.equals("")) {
				pos ++;
				return checkOne(AL);
			}
			else
				return "";
		}
		else if (al.code == Codes.BEGIN) {
			pos ++;

			return checkBlock(AL);
		}
		else if (al.code == Codes.END) {
				return "END without coresponding BEGIN"
					 + "\n   found: " + al.value
					 + "\n   line: " + al.line
					 + "\n   column: " + al.column;					
		}
		else if ((al.code == Codes.END_DOT) || 
				 (al.code == Codes.ELSE)) {
			return "INSTRUCTION expected"
				 + "\n   line: " + al.line
				 + "\n   column: " + al.column;
		}
		else
			return "Not a statement"
				 + "\n   found: " + al.value
				 + "\n   line: " + al.line
				 + "\n   column: " + al.column;
	}// checkOne()

	private String checkEnding(ArrayList AL) {
		String result = "";
		
		result = checkC("SEMICOLON", Codes.SEMICOLON, AL);
		if (!result.equals("")) {
			result = checkC("SEMICOLON", Codes.END, AL);
			if (!result.equals("")) {
				result = checkC("SEMICOLON", Codes.ELSE, AL);
				if (!result.equals("")) {
					result = checkC("SEMICOLON", Codes.END_DOT, AL);
					if (!result.equals(""))
						return result;
				}
			}
		}
		else
			pos ++;
		return "";
		
	}// checkEnding()

	// checks Logical EXPRexion Number Element
	private String checkLEXPRNElem(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		
		result = checkC("VALUE", Codes.MINUS, AL);
		if (result.equals("")) {
			pos ++;
			result = checkC("INTEGER OR REAL NUMBER",
							 Codes.INUMBER, AL);
			if ((result.equals("")) ||
				(checkC("", Codes.RNUMBER, AL).equals(""))) {
				al = (AdvLexem)AL.get(pos);
				AL.set(pos - 1, new AdvLexem("-" + al.value, al.code,
											  al.lcategory,
											  al.hcategory,
											  al.line, al.column));
				AL.remove(pos);
				return "";
			}
			else
				return result;
		}
		else {
			result = checkC("", Codes.INUMBER, AL);
			if (!((result.equals("")) ||
				(checkC("", Codes.RNUMBER, AL).equals("")))) {
				result = checkC("INTEGER OR REAL VARIABLE",
								 Codes.VARIABLE, AL);
				if (!result.equals(""))
					return result;
				else {
					al = (AdvLexem)AL.get(pos);
					int tmp = vars.indexOf(al.value);
					int type = ((Integer)var_types.get(tmp)).intValue();
					if (!((type == Codes.INTEGER) || (type == Codes.REAL))) {
						al = (AdvLexem)AL.get(pos);
						return "INTEGER OR REAL VARIABLE expected "
							 + "\n   found: " + al.value
							 + "\n   line: " + al.line
							 + "\n   column: " + al.column;
					}
					else {
						pos++;
						return "";
					}
				}
			}
			else {
				pos ++;
				return "";
			}
		}
	}// checkLEXPRNElem()
	
	// checks Logical EXPRexion Logic Element
	private String checkLEXPRLElem(ArrayList AL) {
		String result = "";
		AdvLexem al = null;
		result = checkC("", Codes.TRUE, AL);
		if (!((result.equals("")) ||
			(checkC("", Codes.FALSE, AL).equals("")))) {
			result = checkC("LOGICAL VALUE",
							 Codes.VARIABLE, AL);
			if (!result.equals(""))
				return result;
			else {
				al = (AdvLexem)AL.get(pos);
				int tmp = vars.indexOf(al.value);
				int type = ((Integer)var_types.get(tmp)).intValue();
				if (!(type == Codes.BOOLEAN)) {
					al = (AdvLexem)AL.get(pos);
					return "expected: LOGICAL VALUE"
						 + "\n   found: " + al.value
						 + "\n   line: " + al.line
						 + "\n   column: " + al.column;
				}
				else {
					pos++;
					return "";
				}
			}
		}
		else {
			pos ++;
			return "";
		}
	}// checkLEXPRLElem()

//mmmmmmmmmmmmmmmmmmmmmm	INTERPRETER   mmmmmmmmmmmmmmmmmmmmmmmmmmm

	private ArrayList interBody(ArrayList AL) {
		ArrayList baze = new ArrayList();
		String itype = "";
		String variable = "";
		int start = 0;
		int finish = 0;
		ArrayList iftrue = new ArrayList();
		ArrayList ifelse = new ArrayList();

		AdvLexem al = null;

		while (true) {
			al = (AdvLexem)AL.get(pos);
			if (al.code == Codes.VARIABLE) {
				variable = al.value;
				pos ++;
				pos ++;
				start = pos;
				int tmp = vars.indexOf(al.value);
				int type = ((Integer)var_types.get(tmp)).intValue();
	
				if (type == Codes.INTEGER) {
					checkIASSOC(AL);
					finish = pos - 1;
					itype = "IASSOC";
				}
				else if (type == Codes.REAL) {
					checkRASSOC(AL);
					finish = pos - 1;
					itype = "RASSOC";
				}
				else if (type == Codes.BOOLEAN) {
					checkLEXPR(AL);
					finish = pos - 1;
					itype = "BASSOC";
				}
				checkEnding(AL);
			}
			else if (al.code == Codes.WRITELN) {
				itype = "WRITE";
				pos ++;
				start = pos;
				checkWRITE(AL);
				finish = pos - 1;
				checkEnding(AL);
			}
			else if (al.code == Codes.READLN) {
				itype = "READ";
				pos ++;
				start = pos;
				checkREAD(AL);
				finish = pos - 1;
				checkEnding(AL);
			}
			else if (al.code == Codes.WHILE) {
				itype = "WHILE";
				pos ++;
				start = pos;
				checkLEXPR(AL);
				finish = pos - 1;
				pos ++;
				iftrue = interOne(AL);
			}
			else if (al.code == Codes.IF) {
				itype = "IF";
				pos ++;
				start = pos;
				checkLEXPR(AL);
				finish = pos - 1;
				pos ++;
				iftrue = interOne(AL);
				if (checkC("keyword ELSE", Codes.ELSE, AL).equals("")) {
					pos ++;
					ifelse = interOne(AL);
				}
			}
			else if (al.code == Codes.BEGIN) {
				pos ++;
				return interBlock(AL);

			}
			else if (al.code == Codes.END_DOT) {
				pos ++;
				checkEnding(AL);
				return baze;
			}
			baze.add(new ExecLexem(itype, variable, AL, start, finish,
					               iftrue, ifelse));
			iftrue = new ArrayList();
			ifelse = new ArrayList();
			variable = "";
		}
	}// interBODY()	

	private ArrayList interBlock(ArrayList AL) {
		ArrayList baze = new ArrayList();
		String itype = "";
		String variable = "";
		int start = 0;
		int finish = 0;
		ArrayList iftrue = new ArrayList();
		ArrayList ifelse = new ArrayList();

		AdvLexem al = null;

		while (true) {
			al = (AdvLexem)AL.get(pos);
			if (al.code == Codes.VARIABLE) {
				variable = al.value;
				pos ++;
				pos ++;
				start = pos;
				int tmp = vars.indexOf(al.value);
				int type = ((Integer)var_types.get(tmp)).intValue();
	
				if (type == Codes.INTEGER) {
					checkIASSOC(AL);
					finish = pos - 1;
					itype = "IASSOC";
				}
				else if (type == Codes.REAL) {
					checkRASSOC(AL);
					finish = pos - 1;
					itype = "RASSOC";
				}
				else if (type == Codes.BOOLEAN) {
					checkLEXPR(AL);
					finish = pos - 1;
					itype = "BASSOC";
				}
				checkEnding(AL);
			}
			else if (al.code == Codes.WRITELN) {
				itype = "WRITE";
				pos ++;
				start = pos;
				checkWRITE(AL);
				finish = pos - 1;
				checkEnding(AL);
			}
			else if (al.code == Codes.READLN) {
				itype = "READ";
				pos ++;
				start = pos;
				checkREAD(AL);
				finish = pos - 1;
				checkEnding(AL);
			}
			else if (al.code == Codes.WHILE) {
				itype = "WHILE";
				pos ++;
				start = pos;
				checkLEXPR(AL);
				finish = pos - 1;
				pos ++;
				iftrue = interOne(AL);
			}
			else if (al.code == Codes.IF) {
				itype = "IF";
				pos ++;
				start = pos;
				checkLEXPR(AL);
				finish = pos - 1;
				pos ++;
				iftrue = interOne(AL);
				if (checkC("keyword ELSE", Codes.ELSE, AL).equals("")) {
					pos ++;
					ifelse = interOne(AL);
				}
			}
			else if (al.code == Codes.BEGIN) {
				pos ++;
				return interBlock(AL);
			}
			else if (al.code == Codes.END) {
				pos ++;
				checkEnding(AL);
				return baze;
			}
			baze.add(new ExecLexem(itype, variable, AL, start, finish,
					               iftrue, ifelse));
			iftrue = new ArrayList();
			ifelse = new ArrayList();
			variable = "";

		}
	}// interBlock()

	private ArrayList interOne(ArrayList AL) {
		ArrayList baze = new ArrayList();
		String itype = "";
		String variable = "";
		int start = 0;
		int finish = 0;
		ArrayList iftrue = new ArrayList();
		ArrayList ifelse = new ArrayList();

		AdvLexem al = null;
		al = (AdvLexem)AL.get(pos);
		if (al.code == Codes.VARIABLE) {
			variable = al.value;
			pos ++;
			pos ++;
			start = pos;
			int tmp = vars.indexOf(al.value);
			int type = ((Integer)var_types.get(tmp)).intValue();

			if (type == Codes.INTEGER) {
				checkIASSOC(AL);
				finish = pos - 1;
				itype = "IASSOC";
			}
			else if (type == Codes.REAL) {
				checkRASSOC(AL);
				finish = pos - 1;
				itype = "RASSOC";
			}
			else if (type == Codes.BOOLEAN) {
				checkLEXPR(AL);
				finish = pos - 1;
				itype = "BASSOC";
			}
			checkEnding(AL);
		}
		else if (al.code == Codes.WRITELN) {
			itype = "WRITE";
			pos ++;
			start = pos;
			checkWRITE(AL);
			finish = pos - 1;
			checkEnding(AL);
		}
		else if (al.code == Codes.READLN) {
			itype = "READ";
			pos ++;
			start = pos;
			checkREAD(AL);
			finish = pos - 1;
			checkEnding(AL);
		}
		else if (al.code == Codes.WHILE) {
			itype = "WHILE";
			pos ++;
			start = pos;
			checkLEXPR(AL);
			finish = pos - 1;
			pos ++;
			iftrue = interOne(AL);
		}
		else if (al.code == Codes.IF) {
			itype = "IF";
			pos ++;
			start = pos;
			checkLEXPR(AL);
			finish = pos - 1;
			pos ++;
			iftrue = interOne(AL);
			if (checkC("keyword ELSE", Codes.ELSE, AL).equals("")) {
				pos ++;
				ifelse = interOne(AL);
			}
		}
		else if (al.code == Codes.BEGIN) {
			pos ++;
			return interBlock(AL);
		}
		baze.add(new ExecLexem(itype, variable, AL, start, finish,
				               iftrue, ifelse));
		return baze;

	}// checkOne()

//mmmmmmmmmmmmmmmmmmmmmmm	EVALUATION	mmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

	private void initializeVars() {
		for (int i = 0; i < var_types.size(); i++)
			if (((Integer)var_types.get(i)).intValue() == Codes.INTEGER)
				var_vals.add(new Integer(0));
			else if (((Integer)var_types.get(i)).intValue() == Codes.REAL)
				var_vals.add(new Float(0));
			else
				var_vals.add(new Boolean(false));
	}// initializeVars()

	private int evalInt(AdvLexem[] lex) {
		ArrayList args = new ArrayList();
		ArrayList oper = new ArrayList();

		while (place < lex.length) {
			if (lex[place].code == Codes.RBRACKET) {
				break;
			}
			if (lex[place].code == Codes.INUMBER)
				args.add(new Integer(lex[place].value));
			else if (lex[place].code == Codes.VARIABLE) {
				int tmp = vars.indexOf(lex[place].value);
				args.add((Integer)var_vals.get(tmp));
			}
			else if (lex[place].code == Codes.LBRACKET) {
				place ++;
				args.add(new Integer(evalInt(lex)));	
			}
			else if (lex[place].lcategory == Codes.AOPERATOR)
				oper.add(lex[place].value);
			place ++;
		}

		for (int i = 0; i < oper.size(); i++)
			if (((String)oper.get(i)).equals("*")) {
				int tmp1 = ((Integer)args.get(i)).intValue();
				int tmp2 = ((Integer)args.get(i + 1)).intValue();
				args.set(i, new Integer(tmp1 * tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}
			else if (((String)oper.get(i)).equals("/")) {
				int tmp1 = ((Integer)args.get(i)).intValue();
				int tmp2 = ((Integer)args.get(i + 1)).intValue();
				args.set(i, new Integer(tmp1 / tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}

		for (int i = 0; i < oper.size(); i++)
			if (((String)oper.get(i)).equals("+")) {
				int tmp1 = ((Integer)args.get(i)).intValue();
				int tmp2 = ((Integer)args.get(i + 1)).intValue();
				args.set(i, new Integer(tmp1 + tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}
			else if (((String)oper.get(i)).equals("-")) {
				int tmp1 = ((Integer)args.get(i)).intValue();
				int tmp2 = ((Integer)args.get(i + 1)).intValue();
				args.set(i, new Integer(tmp1 - tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}

		return ((Integer)args.get(0)).intValue();
	}// evalInt()
	
	private float evalReal(AdvLexem[] lex) {
		ArrayList args = new ArrayList();
		ArrayList oper = new ArrayList();

		while (place < lex.length) {
			if (lex[place].code == Codes.RBRACKET) {
				break;
			}
			if (lex[place].lcategory == Codes.NUMBER)
				args.add(new Float(lex[place].value));
			else if (lex[place].code == Codes.VARIABLE) {
				int tmp = vars.indexOf(lex[place].value);
				int type = ((Integer)var_types.get(tmp)).intValue();
				if (type == Codes.INTEGER) 
					args.add(new Float(((Integer)var_vals.get(tmp)).
															intValue()));
				else
					args.add((Float)var_vals.get(tmp));
			}
			else if (lex[place].code == Codes.LBRACKET) {
				place ++;
				args.add(new Float(evalReal(lex)));	
			}
			else if (lex[place].lcategory == Codes.AOPERATOR)
				oper.add(lex[place].value);
			place ++;
		}

		for (int i = 0; i < oper.size(); i++)
			if (((String)oper.get(i)).equals("*")) {
				float tmp1 = ((Float)args.get(i)).floatValue();
				float tmp2 = ((Float)args.get(i + 1)).floatValue();
				args.set(i, new Float(tmp1 * tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}
			else if (((String)oper.get(i)).equals("/")) {
				float tmp1 = ((Float)args.get(i)).floatValue();
				float tmp2 = ((Float)args.get(i + 1)).floatValue();
				args.set(i, new Float(tmp1 / tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}

		for (int i = 0; i < oper.size(); i++)
			if (((String)oper.get(i)).equals("+")) {
				float tmp1 = ((Float)args.get(i)).floatValue();
				float tmp2 = ((Float)args.get(i + 1)).floatValue();
				args.set(i, new Float(tmp1 + tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}
			else if (((String)oper.get(i)).equals("-")) {
				float tmp1 = ((Float)args.get(i)).floatValue();
				float tmp2 = ((Float)args.get(i + 1)).floatValue();
				args.set(i, new Float(tmp1 - tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}

		return ((Float)args.get(0)).floatValue();
	}// evalReal()
	
	private boolean evalBool(AdvLexem[] lex) {
		ArrayList args = new ArrayList();
		ArrayList oper = new ArrayList();
		ArrayList nots = new ArrayList();
		int pointer = 0;

		while (place < lex.length) {
			if (lex[place].code == Codes.RBRACKET) {
				break;
			}
			if (lex[place].code == Codes.VARIABLE) {
				int tmp = vars.indexOf(lex[place].value);
				int type = ((Integer)var_types.get(tmp)).intValue();
				if (type == Codes.BOOLEAN) 
					pointer = 1;
				else
					pointer = 2;
			}

			if ((lex[place].code == Codes.TRUE) ||
				(lex[place].code == Codes.FALSE))
				args.add(new Boolean(lex[place].value));
			else if (pointer == 1) {
				int tmp = vars.indexOf(lex[place].value);
				args.add((Boolean)var_vals.get(tmp));
				pointer = 0;
			}
			else if (lex[place].code == Codes.LBRACKET) {
				place ++;
				args.add(new Boolean(evalBool(lex)));	
			}
			else if ((lex[place].code == Codes.AND) ||
					 (lex[place].code == Codes.OR))
				oper.add(lex[place].value);
			else if (lex[place].code == Codes.NOT) 
				nots.add(new Integer(args.size()));
			else {
				float tmp1 = 0;
				float tmp2 = 0;
				int rel = 0;
				if (lex[place].lcategory == Codes.NUMBER)
					tmp1 = (new Float(lex[place].value)).floatValue();
				else if (pointer == 2) {
					pointer = 0;
					int tmp = vars.indexOf(lex[place].value);
					int type = ((Integer)var_types.get(tmp)).intValue();
					if (type == Codes.INTEGER) 
						tmp1 = ((Integer)var_vals.get(tmp)).intValue();
					else
						tmp1 = ((Float)var_vals.get(tmp)).floatValue();
				}
				place ++;
				rel = lex[place].code;
				place ++;
				if (lex[place].lcategory == Codes.NUMBER)
					tmp2 = (new Float(lex[place].value)).floatValue();
				else if (lex[place].code == Codes.VARIABLE) {
					int tmp = vars.indexOf(lex[place].value);
					int type = ((Integer)var_types.get(tmp)).intValue();
					if (type == Codes.INTEGER) 
						tmp2 = ((Integer)var_vals.get(tmp)).intValue();
					else
						tmp2 = ((Float)var_vals.get(tmp)).floatValue();
				}
				boolean bool = false;
				if (rel == Codes.E)
					bool = tmp1 == tmp2;
				else if (rel == Codes.NE)
					bool = tmp1 != tmp2;
				else if (rel == Codes.GT) {
					bool = tmp1 > tmp2;
				}
				else if (rel == Codes.GE)
					bool = tmp1 >= tmp2;
				else if (rel == Codes.LT)
					bool = tmp1 < tmp2;
				else if (rel == Codes.LE)
					bool = tmp1 <= tmp2;		

				args.add(new Boolean(bool));				
			}
				
			place ++;
		}
		
		for (int i = 0; i < nots.size(); i++) {
			int neg = ((Integer)nots.get(i)).intValue();
			boolean temp = ((Boolean)args.get(neg)).booleanValue();
			args.set(neg, new Boolean(!temp));
		}

		for (int i = 0; i < oper.size(); i++)
			if (((String)oper.get(i)).equals("AND")) {
				boolean tmp1 = ((Boolean)args.get(i)).booleanValue();
				boolean tmp2 = ((Boolean)args.get(i + 1)).booleanValue();
				args.set(i, new Boolean(tmp1 && tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}
		for (int i = 0; i < oper.size(); i++)
			if (((String)oper.get(i)).equals("OR")) {
				boolean tmp1 = ((Boolean)args.get(i)).booleanValue();
				boolean tmp2 = ((Boolean)args.get(i + 1)).booleanValue();
				args.set(i, new Boolean(tmp1 || tmp2));
				args.remove(i + 1);
				oper.remove(i);
				i --;
			}

		return ((Boolean)args.get(0)).booleanValue();
	}// evalBool()

//mmmmmmmmmmmmmmmmmmmmmmmmmmm	EXECUTOR	mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
	
	private void execute(ArrayList al) {
		for (int i = 0; i < al.size(); i++)
			execLexem((ExecLexem)al.get(i));
	}// execute()

	private void execLexem(ExecLexem el) {
		if (el.type.equals("IASSOC"))
			execInt(el);
		else if (el.type.equals("RASSOC"))
			execReal(el);
		else if (el.type.equals("BASSOC"))
			execBool(el);
		else if (el.type.equals("READ"))
			execRead(el);		
		else if (el.type.equals("WRITE"))
			execWrite(el);
		else if (el.type.equals("WHILE"))
			execWhile(el);
		else if (el.type.equals("IF"))
			execIf(el);

	}// execLexem()

	private void execInt(ExecLexem el) {
		int tmp = vars.indexOf(el.variable);
		place = 0;
		var_vals.set(tmp, new Integer(evalInt(el.lexems)));
	}// execInt()
	
	private void execReal(ExecLexem el) {
		int tmp = vars.indexOf(el.variable);
		place = 0;
		var_vals.set(tmp, new Float(evalReal(el.lexems)));
	}// execReal()
	
	private void execBool(ExecLexem el) {
		int tmp = vars.indexOf(el.variable);
		place = 0;
		var_vals.set(tmp, new Boolean(evalBool(el.lexems)));
	}// execBool()

	private void execWhile(ExecLexem el) {
		place = 0;
		
		while (evalBool(el.lexems)) {
			execute(el.iftrue);
			place = 0;
		}
	}// execWhile()

	private void execIf(ExecLexem el) {
		place = 0;
		
		if (evalBool(el.lexems))
			execute(el.iftrue);
		else
			execute(el.ifelse);
	}// execIf()

	private void execRead(ExecLexem el) {
		int tmp = vars.indexOf(el.lexems[1].value);
		int type = ((Integer)var_types.get(tmp)).intValue();
		String s = "";
		
		try {
	  	    s = activity.inputText();
	  	}catch (Exception ex) { 
	  		err("Error during input operation !"); }

  	    if (type == Codes.INTEGER)
  	    	var_vals.set(tmp, new Integer(s));
  	    else if (type == Codes.REAL)
  	    	var_vals.set(tmp, new Float(s));
  	    else
  	    	var_vals.set(tmp, new Boolean(s));
 
	}// execRead()
	
	private void execWrite(ExecLexem el) {
		for (int i = 1; i < el.lexems.length; i = i + 2)
			if (el.lexems[i].code == Codes.STRING)
				stdout += el.lexems[i].value;
			else {
				int tmp = vars.indexOf(el.lexems[i].value);
				int type = ((Integer)var_types.get(tmp)).intValue();
				String s = "";

		  	    if (type == Codes.INTEGER)
		  	    	s = ((Integer)var_vals.get(tmp)).toString();
		  	    else if (type == Codes.REAL)
		  	    	s = ((Float)var_vals.get(tmp)).toString();
		  	    else
		  	    	s = ((Boolean)var_vals.get(tmp)).toString();
		  	    stdout += s;
			}
		stdout += "\n";
	}// execWrite()

	private void err(String msg){
		stderr += msg + "\n";
	}
	
}// class Scanner

//mmmmmmmmmmmmmmmmmmmm	ADDITIONAL CLASSES	mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm

class Lexem {
	String value;
	byte type;
	int line;
	int column;

	public Lexem(String value, byte type, int line, int column) {
		if (type != Codes.STRING)
			this.value = value.toUpperCase();
		else
			this.value = value;
		this.type = type;
		this.line = line;
		this.column = column;
	}

	public Lexem(StringBuffer value, byte type, int line, int column) {
		if (type != Codes.STRING)
			this.value = value.toString().toUpperCase();
		else
			this.value = value.toString();
		this.type = type;
		this.line = line;
		this.column = column;
	}

	public Lexem(int value, int type, int line, int column) {
		char[] sign = new char[1];
		sign[0] = (char)value;
		this.value = new String(sign);
		this.type = (byte)type;
		this.line = line;
		this.column = column;
	}
}// class Lexem

class AdvLexem {
	String value;
	byte code;
	byte lcategory; // lower category
	byte hcategory; // higher category
	byte ccategory; // custom category, if needed
	int line;
	int column;

	public AdvLexem(String value, byte code, byte lcategory,
					byte hcategory, int line, int column) {
		this.value = value;
		this.code = code;
		this.lcategory = lcategory;
		this.hcategory = hcategory;
		this.line = line;
		this.column = column;
	}// AdvLexem()
}// class AdvLexem

class ExecLexem {
	String type;     // instruction type
	String variable;  // name of variable in association instruction
	AdvLexem[] lexems; // lexems of the given instruction
	ArrayList iftrue;
	ArrayList ifelse;
	
	ExecLexem(String type, String variable, ArrayList AL,
			  int start, int finish,
			  ArrayList iftrue, ArrayList ifelse) {
		this.type = type;
		this.variable = variable;
		lexems = new AdvLexem[finish - start + 1];
		for (int i = start, j = 0; i <= finish; i++, j++)
			lexems[j] = (AdvLexem)AL.get(i);
		this.iftrue = iftrue;
		this.ifelse = ifelse;
	}// ExecLexem()
}// class ExecLexem
