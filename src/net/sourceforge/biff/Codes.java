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

public class Codes {

	// codes for keywords
	static final byte PROGRAM   = 10;
	static final byte BEGIN     = 11;	
	static final byte END       = 12;
	static final byte END_DOT   = 13; // END.
	static final byte VAR       = 14;
	static final byte IF        = 15;
	static final byte THEN      = 16;
	static final byte ELSE      = 17;
	static final byte WHILE     = 18;
	static final byte DO        = 19;
	static final byte NOT       = 20;
	static final byte AND       = 21;
	static final byte OR        = 22;
	static final byte READLN    = 23;
	static final byte WRITELN   = 24;
	static final byte INTEGER   = 25;
	static final byte REAL      = 26;
	static final byte BOOLEAN   = 27;
	static final byte TRUE      = 28;
	static final byte FALSE     = 29;

	// codes for signs
	static final byte PLUS      = 30; // '+'
	static final byte MINUS     = 31; // '-'
	static final byte MULT      = 32; // '*'
	static final byte DIV       = 33; // '/'
	static final byte E         = 34; // EQUAL
	static final byte NE        = 35; // NOTEQUAL
	static final byte GT        = 36; // GREATER THEN
	static final byte GE        = 37; // GREATER OR EQUAL
	static final byte LT        = 38; // LESS THEN
	static final byte LE        = 39; // LESS OR EQUAL
	static final byte COLON     = 40; // ':'
	static final byte SEMICOLON = 41; // ';'
	static final byte COMMA     = 42; // ','
	static final byte ASOCIATE  = 43; // ':='
	static final byte LBRACKET  = 44; // LEFT BRACKET
	static final byte RBRACKET  = 45; // RIGHT BRACKET
	static final byte QUOTATION = 46; // QUOTATION MARK

	// codes for lower categories
	static final byte KEYWORD   = 60;
	static final byte VARIABLE  = 61;
	static final byte VTYPE     = 62; // VARIABLE TYPE
	static final byte LOPERATOR = 63; // LOGICAL OPERATOR
	static final byte AOPERATOR = 64; // ARITHMETIC OPERATOR
	static final byte ROPERATOR = 65; // RELATIONAL OPERATOR
	static final byte OOPERATOR = 66; // OTHER OPERATORS
	static final byte NUMBER    = 67;

	// codes for higher categories
	static final byte INUMBER   =  1; // INTEGER NUMBER
	static final byte RNUMBER   =  2; // REAL NUMBER
	static final byte WORD      =  3;
	static final byte SIGN      =  4;
	static final byte STRING    =  5;

	public ArrayList keywords = new ArrayList();;
	private ArrayList signs = new ArrayList();;

	public void fillLists() {
		keywords.add("PROGRAM");
		keywords.add("BEGIN");
		keywords.add("END");
		keywords.add("END.");
		keywords.add("VAR");
		keywords.add("IF");
		keywords.add("THEN");
		keywords.add("ELSE");
		keywords.add("WHILE");
		keywords.add("DO");
		keywords.add("NOT");
		keywords.add("AND");
		keywords.add("OR");
		keywords.add("READLN");
		keywords.add("WRITELN");
		keywords.add("INTEGER");
		keywords.add("REAL");
		keywords.add("BOOLEAN");
		keywords.add("TRUE");
		keywords.add("FALSE");

		signs.add("+");
		signs.add("-");
		signs.add("*");
		signs.add("/");
		signs.add("=");
		signs.add("<>");
		signs.add(">");
		signs.add(">=");
		signs.add("<");
		signs.add("<=");
		signs.add(":");
		signs.add(";");
		signs.add(",");
		signs.add(":=");
		signs.add("(");
		signs.add(")");
		signs.add("\"");	
	}// fillLists()

	public byte getKeywordCode(String value) {
		int index = keywords.indexOf(value);
		if (index == -1)
			return VARIABLE;
		else 
			return (byte)(10 + index);
	}// getKeywordCode(Stirng)

	public byte getSignCode(String value) {
		return (byte)(30 + signs.indexOf(value));
	}// getSignCode(Stirng)
}
