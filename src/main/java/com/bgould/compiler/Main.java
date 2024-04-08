package com.bgould.compiler;

import com.bgould.compiler.ADT.Syntactic;

/**
 * The supported CFG for the language is as follows:
 *
 * <program> -> $UNIT <identifier> $SEMICOLON <block> $PERIOD
 * NOTE: the <identifier> here names the program, and must be unique and unused elsewhere
 *
 * <block> -> {<variable-dec-sec>}* <block-body>
 * <variable-dec-sec> -> $VAR <variable-declaration>
 * <variable-declaration> -> {<identifier> {$COMMA <identifier>}* $COLON <simple type> $SEMICOLON}+
 * <block-body> -> $BEGIN <statement> {$SCOLN <statement>}* $END
 *
 * <statement> -> {
 *      [
 *          <variable> $ASSIGN (<simple expression> | <string literal>) |
 *          <block-body> |
 *          $IF <relexpression> $THEN <statement> [$ELSE <statement>] |
 *          $WHILE <relexpression> DO <statement> |
 *          $REPEAT <statement> $UNTIL <relexpression> |
 *          $FOR <variable> $ASSIGN <simple expression> $TO <simple expression> $DO <statement> |
 *          $WRITELN $LPAR (<simple expression> | <identifier> | <stringconst> ) $RPAR |
 *          $READLN $LPAR <identifier> $RPAR
 *      ]+
 * }
 *
 * <variable> -> <identifier>
 * <relexpression> -> <simple expression> <relop> <simple expression>
 * <relop> -> $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ
 * <simple expression> -> [<sign>] <term> {<addop> <term>}*
 * <addop> -> $PLUS | $MINUS
 * <sign> -> $PLUS | $MINUS
 * <term> -> <factor> {<mulop> <factor> }*
 * <mulop> -> $MULT | $DIVIDE
 * <factor> -> <unsigned constant> | <variable> | $LPAR <simple expression> $RPAR
 * <simple type> -> $INTEGER | $FLOAT | $STRING
 * <constant> -> [<sign>] <unsigned constant>
 * <unsigned constant> -> <unsigned number>
 * <identifier> -> $IDENTIFIER (Token code 50)
 * <unsigned number> -> $FLOATTYPE | $INTTYPE (Token codes 52 or 51)
 * <stringconst> -> $STRINGTYPE (Token code 53)
 */

public class Main {
	public static void main(String[] args) {
		String filePath = args[0];
		boolean traceon = true;
		System.out.println("Brendan Gould, 4267, CS4100, SPRING 2024");
		System.out.println("INPUT FILE TO PROCESS IS: " + filePath);

		Syntactic parser = new Syntactic(filePath, traceon);
		parser.parse();

		System.out.println("Done.");
	}
}
