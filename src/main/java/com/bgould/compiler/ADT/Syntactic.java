package com.bgould.compiler.ADT;

import java.util.ArrayList;
import java.util.UUID;

import com.bgould.compiler.Interpreter;

/**
 * Class performing CFG based syntactic parsing of source code
 *
 */
public class Syntactic {
	private String filein;          // The full file path to input file
	private SymbolTable symbolList; // Symbol table storing ident/const
	private QuadTable quads;
	private Interpreter interp;
	private Lexical lex;         // Lexical analyzer
	private Lexical.token token; // Next Token retrieved

	private boolean traceon;   // Controls tracing mode
	private int level = 0;     // Controls indent for trace mode
	private boolean anyErrors; // Set TRUE if an error happens

	private final int symbolSize = 250;
	private final int quadsSize = 1000;
	private int Minus1Index;
	private int Plus1Index;

	public Syntactic(String filename, boolean traceOn) {
		filein = filename;
		traceon = traceOn;
		symbolList = new SymbolTable(symbolSize);
		Minus1Index = symbolList.AddSymbol("-1", SymbolTable.CONSTANT_USAGE, -1);
		Plus1Index = symbolList.AddSymbol("1", SymbolTable.CONSTANT_USAGE, 1);

		quads = new QuadTable(quadsSize);
		interp = new Interpreter();

		lex = new Lexical(filein, symbolList, true);
		lex.setPrintToken(traceOn);
		anyErrors = false;
	}

	// The interface to the syntax analyzer, initiates parsing
	// Uses variable RECUR to get return values throughout the non-terminal methods
	public void parse() {
		// Use source filename as pattern for symbol table and quad table output later
		String filenameBase = filein.substring(0, filein.length() - 4);
		System.out.println(filenameBase);
		int recur = 0;

		// prime the pump to get the first token to process
		token = lex.GetNextToken();
		// call PROGRAM
		recur = Program();

		// Done with recursion, so add the final STOP quad
		quads.AddQuad(interp.opcodeFor("STOP"), 0, 0, 0);
		// Print SymbolTable, QuadTable before execute
		symbolList.PrintSymbolTable(filenameBase + "ST-before.txt");
		quads.PrintQuadTable(filenameBase + "QUADS.txt");
		// interpret
		if (!anyErrors) {
			interp.InterpretQuads(quads, symbolList, false, filenameBase + "TRACE.txt");
		} else {
			System.out.println("Errors, unable to run program.");
		}
		symbolList.PrintSymbolTable(filenameBase + "ST-after.txt");
	}

	// Non Terminal PROGIDENTIFIER is fully implemented here, leave it as-is.
	private int ProgIdentifier() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}

		// This non-term is used to uniquely mark the program identifier
		if (token.code == lex.codeFor("IDNT")) {
			// Because this is the progIdentifier, it will get a 'P' type to prevent re-use as a var
			symbolList.AddSymbol(token.lexeme, 'P', 0);
			// move on
			token = lex.GetNextToken();
		}

		return recur;
	}

	// =========================================================================
	// Top-level non-terminals
	// =========================================================================

	/**
	 * Top-level parser for a program.
	 * Production rule: <program> -> $UNIT <identifier> $SEMICOLON <block> $PERIOD
	 *
	 * @return Unused for now
	 */
	private int Program() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Program", true);

		if (token.code == lex.codeFor("UNIT")) {
			token = lex.GetNextToken();
			recur = ProgIdentifier();
			if (token.code == lex.codeFor("SCLN")) {
				token = lex.GetNextToken();
				recur = Block();
				if (token.code == lex.codeFor("DOT_")) {
					if (!anyErrors) {
						System.out.println("Success.");
					} else {
						System.out.println("Compilation failed.");
					}
				} else {
					error(lex.reserveFor("DOT_"), token.lexeme);
				}
			} else {
				error(lex.reserveFor("SCLN"), token.lexeme);
			}
		} else {
			error(lex.reserveFor("UNIT"), token.lexeme);
		}

		trace("Program", false);
		return recur;
	}

	/**
	 * Parses the main body of a program.
	 * Production rule: <block> -> {<variable-dec-sec>}* <block-body>
	 *
	 * @return Unused for now
	 */
	private int Block() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Block", true);

		// optional variable declaration section
		while (token.code == lex.codeFor("VAR_")) {
			recur = VariableDeclarationSection();
		}

		// parse block body
		recur = BlockBody();

		trace("Block", false);
		return recur;
	}

	/**
	 * Parses the variable declaration section of the program
	 * Production rule: <variable-dec-sec> -> $VAR <variable-declaration>
	 *
	 * @return Unused for now
	 */
	private int VariableDeclarationSection() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("VariableDeclarationSection", true);

		if (token.code == lex.codeFor("VAR_")) {
			token = lex.GetNextToken();
			VariableDeclaration();
		} else {
			error(lex.reserveFor("VAR_"), token.lexeme);
		}

		trace("VariableDeclarationSection", false);
		return recur;
	}

	/**
	 * Parses the identifier and type information of variable declarations
	 * Production rule: <variable-declaration> -> {<identifier> {$COMMA <identifier>}* $COLON
	 * <simple type> $SEMICOLON}+
	 *
	 * @return Unused for now
	 */
	private int VariableDeclaration() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("VariableDeclaration", true);

		do {
			ArrayList<Integer> variableIdx = new ArrayList<>();
			// Get list of identifiers to declare
			variableIdx.add(symbolList.AddSymbol(
				token.lexeme, 'V', 0)); // if not identifier, error will show in Identifier()
			Identifier();
			while (token.code == lex.codeFor("COMA")) {
				token = lex.GetNextToken();

				// Add found identifier to symbol table.
				variableIdx.add(symbolList.AddSymbol(token.lexeme, 'V', 0));

				Identifier();
			}

			// Get type of identifiers
			if (token.code == lex.codeFor("COLN")) {
				token = lex.GetNextToken();

				// Update type of all symbols in this declaration
				// Variables are int by default, only need to change type if double or string
				if (token.code == lex.codeFor("DFPR")) {
					for (int i : variableIdx) {
						symbolList.UpdateSymbol(i, 'V', 0.0f);
					}
				} else if (token.code == lex.codeFor("STRR")) {
					for (int i : variableIdx) {
						symbolList.UpdateSymbol(i, 'V', "");
					}
				} // if token lexeme is unrecognized, SimpleType will output error

				SimpleType();
			} else {
				error("':'", token.lexeme);
			}

			if (token.code != lex.codeFor("SCLN")) {
				error("';'", token.lexeme);
			}
			token = lex.GetNextToken();
		} while (token.code == lex.codeFor("IDNT"));

		trace("VariableDeclaration", false);
		return recur;
	}

	/**
	 * Parse a block of statements
	 * Production rule: $BEGIN <statement> {$SCOLN <statement>}* $END
	 *
	 * @return Unused for now
	 */
	private int BlockBody() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("BlockBody", true);

		if (token.code == lex.codeFor("BGIN")) {
			// Get first statement
			token = lex.GetNextToken();
			recur = Statement();

			// Get optional extra statements
			while ((token.code == lex.codeFor("SCLN")) && (!lex.EOF()) && (!anyErrors)) {
				token = lex.GetNextToken();
				recur = Statement();
			}

			// get end of block
			if (token.code == lex.codeFor("END_")) {
				token = lex.GetNextToken();
			} else {
				error(lex.reserveFor("END_"), token.lexeme);
			}
		} else {
			error(lex.reserveFor("BGIN"), token.lexeme);
		}

		trace("BlockBody", false);
		return recur;
	}

	// =========================================================================
	// Statement level non-terminals
	// =========================================================================

	/**
	 * Parses a single statement
	 * Production rule:
	 * <statement> -> {
	 *      [
	 *          <variable> $ASSIGN (<simple expression> | <string literal>) |
	 *          <block-body> |
	 *          $IF <relexpression> $THEN <statement> [$ELSE <statement>] |
	 *          $WHILE <relexpression> DO <statement> |
	 *          $REPEAT <statement> $UNTIL <relexpression> |
	 *          $FOR <variable> $ASSIGN <simple expression> $TO <simple expression> $DO <statement>
	 * | $WRITELN $LPAR (<simple expression> | <identifier> | <stringconst> ) $RPAR | $READLN $LPAR
	 * <identifier> $RPAR
	 *      ]+
	 * }
	 *
	 * @return Unused for now
	 */
	private int Statement() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Statement", true);

		boolean badStatement = false;

		do {
			if (token.code == lex.codeFor("IDNT")) { // assignment
				recur = handleAssignment();
			} else if (token.code == lex.codeFor("BGIN")) { // block-body
				recur = BlockBody();
			} else if (token.code == lex.codeFor("IF__")) { // if statement
				recur = handleIf();
			} else if (token.code == lex.codeFor("WHIL")) { // while loop
				recur = handleWhile();
			} else if (token.code == lex.codeFor("REPT")) { // repeat until loop
				recur = handleRepeat();
			} else if (token.code == lex.codeFor("FOR_")) { // for loop
				recur = handleFor();
			} else if (token.code == lex.codeFor("WTLN")) { // writeln statement
				recur = handleWriteln();
			} else if (token.code == lex.codeFor("RDLN")) { // readln statement
				recur = handleReadln();
			} else {
				error("Statement start", token.lexeme);
			}

			// if errors in statement were found, we need to restart to find good statement
			badStatement = resynch();
		} while (badStatement);

		trace("Statement", false);
		return recur;
	}

	// Not a Non Terminal, but used to shorten Statement code body for readability.
	//<variable> $COLON-EQUALS <simple expression>
	private int handleAssignment() {
		if (anyErrors) {
			return -1;
		}
		trace("handleAssignment", true);

		// have ident already in order to get to here, handle as Variable
		int varLoc = Variable(); // Variable moves ahead, next token ready
		if (token.code == lex.codeFor("DEFN")) {
			int valLoc;
			token = lex.GetNextToken();

			if (token.code == lex.codeFor("STRR")) {
				valLoc = StringConst();
			} else {
				valLoc = SimpleExpression();
			}

			// Generate code
			quads.AddQuad(interp.opcodeFor("MOV"), valLoc, 0, varLoc);
		} else {
			error(lex.reserveFor("DEFN"), token.lexeme);
		}

		trace("handleAssignment", false);
		return varLoc;
	}

	private int handleIf() {
		int jumpQuad, elseQuad, elseJumpQuad, endQuad;
		if (anyErrors) {
			return -1;
		}
		trace("handleIf", true);

		// Get mandatory IF
		if (token.code != lex.codeFor("IF__")) {
			error(lex.reserveFor("IF__"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Get conditional expression
		jumpQuad = RelExpression();

		// Get mandatory THEN
		if (token.code != lex.codeFor("THEN")) {
			error(lex.reserveFor("THEN"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Get conditional statement
		Statement();

		// Get optional else
		if (token.code == lex.codeFor("ELSE")) {
			// after executing if branch, jump over else
			elseJumpQuad = quads.NextQuad();
			quads.AddQuad(interp.opcodeFor("JMP"), 0, 0, 0);

			// Save location for jump to else branch
			elseQuad = quads.NextQuad();
			quads.UpdateJump(jumpQuad, elseQuad);

			token = lex.GetNextToken();
			Statement();

			// save location for jump to end of statement
			endQuad = quads.NextQuad();
			quads.UpdateJump(elseJumpQuad, endQuad);
		} else {
			// save location for jump to end of statement
			endQuad = quads.NextQuad();
			quads.UpdateJump(jumpQuad, endQuad);
		}

		trace("handleIf", false);
		return jumpQuad;
	}

	private int handleWhile() {
		int jumpQuad, testQuad;
		if (anyErrors) {
			return -1;
		}
		trace("handleWhile", true);

		// Get mandatory WHILE
		if (token.code != lex.codeFor("WHIL")) {
			error(lex.reserveFor("WHIL"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Get conditional expression
		testQuad = quads.NextQuad(); // quad location of test instruction
		jumpQuad = RelExpression();

		// Get mandatory DO
		if (token.code != lex.codeFor("DO__")) {
			error(lex.reserveFor("DO__"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Get conditional statement
		jumpQuad = Statement();

		// Implement loop jumps
		quads.AddQuad(interp.opcodeFor("JMP"), 0, 0, testQuad);
		quads.UpdateJump(jumpQuad, quads.NextQuad());

		trace("handleWhile", false);
		return jumpQuad;
	}

	private int handleRepeat() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("handleRepeat", true);

		// Get mandatory REPEAT
		if (token.code != lex.codeFor("REPT")) {
			error(lex.reserveFor("REPT"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Get conditional statement
		recur = Statement();

		// Get mandatory UNTIL
		if (token.code != lex.codeFor("UNTL")) {
			error(lex.reserveFor("UNTL"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Get conditional expression
		recur = RelExpression();

		trace("handleRepeat", false);
		return recur;
	}

	private int handleFor() {
		int counter, startVal, endVal, loopStartQuad, temp;
		if (anyErrors) {
			return -1;
		}
		trace("handleFor", true);

		// Get FOR intializer
		if (token.code != lex.codeFor("FOR_")) {
			error(lex.reserveFor("FOR_"), token.lexeme);
		}
		token = lex.GetNextToken();

		counter = Variable();

		if (token.code != lex.codeFor("DEFN")) {
			error(lex.reserveFor("DEFN"), token.lexeme);
		}
		token = lex.GetNextToken();

		startVal = SimpleExpression();

		if (token.code != lex.codeFor("TO__")) {
			error(lex.reserveFor("TO__"), token.lexeme);
		}
		token = lex.GetNextToken();

		endVal = SimpleExpression();

		// Get repeated statement
		if (token.code != lex.codeFor("DO__")) {
			error(lex.reserveFor("DO__"), token.lexeme);
		}
		token = lex.GetNextToken();

		loopStartQuad = quads.NextQuad();

		Statement();

		// Increment counter
		quads.AddQuad(interp.opcodeFor("ADD"), counter, Plus1Index, counter);

		// Conditional jump to loop start
		temp = GenSymbol();
		quads.AddQuad(interp.opcodeFor("SUB"), endVal, counter, temp);
		quads.AddQuad(interp.opcodeFor("JP"), temp, 0, loopStartQuad);

		trace("handleFor", false);
		return counter;
	}

	private int handleWriteln() {
		int recur = 0;
		int toprint = 0;
		if (anyErrors) {
			return -1;
		}
		trace("handleWriteln", true);

		// Get command start
		if (token.code != lex.codeFor("WTLN")) {
			error(lex.reserveFor("WTLN"), token.lexeme);
		}
		token = lex.GetNextToken();

		if (token.code != lex.codeFor("LFTP")) {
			error(lex.reserveFor("LFTP"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Get expression to write
		if (isAddOp(token) || isNumber(token) || token.code == lex.codeFor("IDNT")) {
			toprint = SimpleExpression();
		} else if (token.code == lex.codeFor("STRV")) {
			toprint = StringConst();
		} else {
			error("expression, identifier, or string", token.lexeme);
		}

		// Get command end
		if (token.code != lex.codeFor("RITP")) {
			error(lex.reserveFor("RITP"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Generate print instruction
		quads.AddQuad(interp.opcodeFor("PRINT"), 0, 0, toprint);

		trace("handleWriteln", false);
		return recur;
	}

	private int handleReadln() {
		int dst = 0;
		if (anyErrors) {
			return -1;
		}
		trace("handleReadln", true);

		// Get command start
		if (token.code != lex.codeFor("RDLN")) {
			error(lex.reserveFor("RDLN"), token.lexeme);
		}
		token = lex.GetNextToken();

		if (token.code != lex.codeFor("LFTP")) {
			error(lex.reserveFor("LFTP"), token.lexeme);
		}
		token = lex.GetNextToken();

		dst = Identifier();

		// Get command end
		if (token.code != lex.codeFor("RITP")) {
			error(lex.reserveFor("RITP"), token.lexeme);
		}
		token = lex.GetNextToken();

		// Generate instructions
		quads.AddQuad(interp.opcodeFor("READ"), 0, 0, dst);

		trace("handleReadln", false);
		return dst;
	}

	/**
	 * Syntactically parses a simple arithmetic expression.
	 * Production rule: [<sign>] <term> {<addop> <term>}*
	 *
	 * @return Location of expression result as an index into the symbol table
	 */
	private int SimpleExpression() {
		int left, right, signval, temp, opcode;
		signval = 1;
		if (anyErrors) {
			return -1;
		}
		trace("SimpleExpression", true);

		// optional sign value
		if (isAddOp(token)) {
			signval = Sign();
		}

		// mandatory term
		left = Term();

		if (signval == -1)
			quads.AddQuad(interp.opcodeFor("MUL"), left, Minus1Index, left);

		// optional additional terms
		while ((!anyErrors) && isAddOp(token)) {
			opcode = AddOp();
			right = Term();

			// Generate code
			temp = GenSymbol();
			quads.AddQuad(opcode, left, right, temp);
			left = temp; // iterative result becomes new LHS
		}

		trace("SimpleExpression", false);
		return left;
	}

	/**
	 * Syntactically parses a term in an arithmetic expression
	 * Production rule: <factor> {<mulop> <factor> }*
	 *
	 * @return Location of expression result as an index into the symbol table
	 */
	private int Term() {
		int left, right, temp, opcode;
		if (anyErrors) {
			return -1;
		}
		trace("Term", true);

		// Mandatory first factor
		left = Factor();

		// Optional additional factors
		while ((!anyErrors) && isMulOp(token)) {
			opcode = MulOp();
			right = Factor();

			// Generate code
			temp = GenSymbol();
			quads.AddQuad(opcode, left, right, temp);
			left = temp; // iterative result becomes new LHS
		}

		trace("Term", false);
		return left;
	}

	/**
	 * Syntactically parses a factor in an arithmetic expression
	 * Production rule: <unsigned constant> | <variable> | $LPAR <simple expression> $RPAR
	 *
	 * @return Location of expression result as an index into the symbol table
	 */
	private int Factor() {
		int ret = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Factor", true);

		if (isNumber(token)) { // some constant
			ret = UnsignedConstant();
		} else if (token.code == lex.codeFor("IDNT")) { // some variable
			ret = Variable();
		} else if (token.code == lex.codeFor("LFTP")) { // nested expression
			token = lex.GetNextToken();

			ret = SimpleExpression();

			if (token.code == lex.codeFor("RITP")) {
				token = lex.GetNextToken();
			} else {
				error("')'", token.lexeme);
			}
		} else {
			error("Constant, Variable, or '('", token.lexeme);
		}

		trace("Factor", false);
		return ret;
	}

	/**
	 * Parses a single relative / conditional expression.
	 * Production rule: <relexpression> -> <simple expression> <relop> <simple expression>
	 *
	 * Also generates a quad to jump to the false branch of a relative expression.
	 *
	 * @return Location of jump quad as an index into the quad table
	 */
	private int RelExpression() {
		int left, right, saveRelop, ret, temp;
		if (anyErrors) {
			return -1;
		}
		trace("RelExpression", true);

		// Parse expression
		left = SimpleExpression();
		saveRelop = RelOp();
		right = SimpleExpression();

		// Generate code
		temp = GenSymbol();
		quads.AddQuad(interp.opcodeFor("SUB"), left, right, temp);
		ret = quads.NextQuad();
		quads.AddQuad(RelopToOpcode(saveRelop), temp, 0, 0); // jump destination set later

		trace("RelExpression", false);
		return ret;
	}

	// =========================================================================
	// Token-level non-terminals
	// =========================================================================

	// Non-terminal VARIABLE just looks for an IDENTIFIER. Later, a
	//  type-check can verify compatible math ops, or if casting is required.
	private int Variable() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Variable", true);

		if ((token.code == lex.codeFor("IDNT"))) {
			// bookkeeping and move on
			recur = symbolList.LookupSymbol(token.lexeme);
			if (recur == -1) {
				System.out.println("ERROR: Undeclared identifier " + token.lexeme);
				recur = symbolList.AddSymbol(token.lexeme, SymbolTable.VARIABLE_USAGE, 0);
			}
			token = lex.GetNextToken();
		} else {
			error("Variable", token.lexeme);
		}

		trace("Variable", false);
		return recur;
	}

	/**
	 * Parses an identifier
	 * Production rule: <identifier> -> $IDENTIFIER (Token code 50)
	 *
	 * @return Location of identifier as an index into the symbol table
	 */
	private int Identifier() {
		int ret = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Identifier", true);

		if (token.code != lex.codeFor("IDNT")) {
			error("identifier", token.lexeme);
		}

		ret = symbolList.LookupSymbol(token.lexeme);
		if (ret == -1) {
			ret = symbolList.AddSymbol(token.lexeme, SymbolTable.VARIABLE_USAGE, 0);
		}

		token = lex.GetNextToken();

		trace("Identifier", false);
		return ret;
	}

	/**
	 * Parses an type declaration
	 * Production rule: $INTEGER | $FLOAT | $STRING
	 *
	 * @return Unused for now
	 */
	private int SimpleType() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("SimpleType", true);

		if (token.code != lex.codeFor("INTR") && token.code != lex.codeFor("DFPR") &&
		    token.code != lex.codeFor("STRR")) {
			error(lex.reserveFor("INTR") + ", " + lex.reserveFor("DFPR") + ", or " +
			          lex.reserveFor("STRR"),
			      token.lexeme);
		}
		token = lex.GetNextToken();

		trace("SimpleType", false);
		return recur;
	}

	/**
	 * Syntactically parses an unsigned constant in an arithmetic expression
	 * Production rule: <unsigned number>
	 *
	 * @return Location of constant as an index into the symbol table
	 */
	private int UnsignedConstant() {
		int ret = 0;
		if (anyErrors) {
			return -1;
		}
		trace("UnsignedConstant", true);

		ret = UnsignedNumber();

		trace("UnsignedConstant", false);
		return ret;
	}

	/**
	 * Syntactically parses an unsigned number in an arithmetic expression
	 * Production rule: $FLOAT | $INTEGER
	 *
	 * @return Location of number as index into the symbol table
	 */
	private int UnsignedNumber() {
		int ret = 0;
		if (anyErrors) {
			return -1;
		}
		trace("UnsignedNumber", true);

		ret = symbolList.LookupSymbol(token.lexeme);
		if (!isNumber(token)) {
			error("Float or Integer", token.lexeme);
		}

		// Generate code
		int val = Integer.parseInt(token.lexeme); // NOTE: assumed that only integer math is used
		ret = symbolList.LookupSymbol(token.lexeme);
		if (ret == -1) {
			ret = symbolList.AddSymbol(token.lexeme, SymbolTable.VARIABLE_USAGE, val);
		} else {
			symbolList.UpdateSymbol(ret, SymbolTable.VARIABLE_USAGE, val);
		}

		token = lex.GetNextToken();

		trace("UnsignedNumber", false);
		return ret;
	}

	/**
	 * Syntactically parses a string constant
	 * Production rule: $STRING
	 *
	 * @return The location of the string constant as an index into the symbol table
	 */
	private int StringConst() {
		int location = 0;
		if (anyErrors) {
			return -1;
		}
		trace("StringConst", true);

		if (token.code != lex.codeFor("STRV")) {
			error("string constant", token.lexeme);
		}

		// NOTE: String should already be in symbol table; added by lexical
		location = symbolList.LookupSymbol(token.lexeme);
		symbolList.UpdateSymbol(location, SymbolTable.VARIABLE_USAGE, token.lexeme);

		token = lex.GetNextToken();

		trace("StringConst", false);
		return location;
	}

	/**
	 * Syntactically parses an addition operation in an arithmetic expression
	 * Production rule: $PLUS | $MINUS
	 *
	 * @return opcodeFor(ADD) if '+' found, opcodeFor(SUB) if '-' found
	 */
	private int AddOp() {
		int ret = 0;
		if (anyErrors) {
			return -1;
		}
		trace("AddOp", true);

		if (!isAddOp(token)) {
			error("+ or -", token.lexeme);
		}
		ret = (token.lexeme.equals("+")) ? interp.opcodeFor("ADD") : interp.opcodeFor("SUB");
		token = lex.GetNextToken();

		trace("AddOp", false);
		return ret;
	}

	/**
	 * Syntactically parses a sign in an arithmetic expression
	 * Production rule: $PLUS | $MINUS
	 *
	 * @return +1 if '+' found, -1 if '-' found
	 */
	private int Sign() {
		int ret = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Sign", true);

		if (!isAddOp(token)) {
			error("+ or -", token.lexeme);
		}
		ret = (token.lexeme.equals("+")) ? 1 : -1;
		token = lex.GetNextToken();

		trace("Sign", false);
		return ret;
	}

	/**
	 * Syntactically parses a multiplication operation in an arithmetic expression
	 * Production rule: $MULTIPLY | $DIVIDE
	 *
	 * @return opcodeFor(MUL) if '*' found, opcodeFor(DIV) if '/' found
	 */
	private int MulOp() {
		int ret = 0;
		if (anyErrors) {
			return -1;
		}
		trace("MulOp", true);

		if (!isMulOp(token)) {
			error("* or /", token.lexeme);
		}
		ret = (token.lexeme.equals("*")) ? interp.opcodeFor("MUL") : interp.opcodeFor("DIV");
		token = lex.GetNextToken();

		trace("MulOp", false);
		return ret;
	}

	/**
	 * Syntactically parses a relative operation in an logical expression
	 * Production rule: <relop> -> $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ
	 *
	 * @return Token code of parsed operator
	 */
	private int RelOp() {
		int ret = 0;
		if (anyErrors) {
			return -1;
		}
		trace("RelOp", true);

		if (token.code != lex.codeFor("GTHN") && token.code != lex.codeFor("LTHN") &&
		    token.code != lex.codeFor("GRET") && token.code != lex.codeFor("LSET") &&
		    token.code != lex.codeFor("EQUL") && token.code != lex.codeFor("NEQL")) {
			error("relative expression", token.lexeme);
		}
		ret = token.code;
		token = lex.GetNextToken();

		trace("RelOp", false);
		return ret;
	}

	// =========================================================================
	// UTILITY FUNCTIONS USED THROUGHOUT THIS CLASS
	// =========================================================================

	/**
	 * Writes a simple error message to stdout. Currently, the only type of error message supported
	 * by this function is when one type of expression is expected, but a different type is found.
	 *
	 * @param wanted The type of expression that was expected
	 * @param got The type of expression that was found instead
	 */
	private void error(String wanted, String got) {
		anyErrors = true;
		System.out.println("ERROR: Expected " + wanted + " but found " + got);
	}

	/**
	 * Print a trace message when entering / exiting parsing a particular expression. If traceOn is
	 * not set, does nothing.
	 *
	 * @param proc The name of the expression to include in the trace
	 * @param enter true if we are entering the expression, false if exiting
	 */
	private void trace(String proc, boolean enter) {
		String tabs = "";
		if (!traceon) {
			return;
		}

		if (enter) {
			tabs = repeatChar(" ", level);
			System.out.print(tabs);
			System.out.println("--> Entering " + proc);
			level++;
		} else {
			if (level > 0) {
				level--;
			}
			tabs = repeatChar(" ", level);
			System.out.print(tabs);
			System.out.println("<-- Exiting " + proc);
		}
	}

	/**
	 * Used in error recovery. If a malformed statement is found, this will skip tokens until the
	 * start of a new statement is found.
	 *
	 * @return true if a malformed statement was found, false otherwise
	 */
	private boolean resynch() {
		if (!anyErrors)
			return false;

		System.out.println("** Error recovery: Resynching...");

		// search until token starts a new statement
		while (!lex.EOF() && !isStatementStart(token)) {
			token = lex.GetNextToken();
		}

		if (lex.EOF()) {
			System.out.println("** Resynch failed: reached EOF");
			return false;
		} else {
			// assume rest of source is error free
			anyErrors = false;
			System.out.println("** Found statement start: " + token.lexeme);
		}

		return true;
	}

	private int GenSymbol() {
		String name = UUID.randomUUID().toString();
		return symbolList.AddSymbol(name, SymbolTable.VARIABLE_USAGE, 0);
	}

	private int RelopToOpcode(int relop) {
		int ret = 0;

		if (relop == lex.codeFor("GTHN")) {
			ret = interp.opcodeFor("JNP");
		} else if (relop == lex.codeFor("LTHN")) {
			ret = interp.opcodeFor("JNN");
		} else if (relop == lex.codeFor("GRET")) {
			ret = interp.opcodeFor("JN");
		} else if (relop == lex.codeFor("LSET")) {
			ret = interp.opcodeFor("JP");
		} else if (relop == lex.codeFor("EQUL")) {
			ret = interp.opcodeFor("JNZ");
		} else if (relop == lex.codeFor("NEQL")) {
			ret = interp.opcodeFor("JZ");
		} else {
			throw new RuntimeException("Invalid relop code");
		}

		return ret;
	}

	// repeatChar returns a string containing x repetitions of string s;
	// nice for making a varying indent format
	private String repeatChar(String s, int x) {
		int i;
		String result = "";
		for (i = 1; i <= x; i++) {
			result = result + s;
		}
		return result;
	}

	private boolean isAddOp(Lexical.token t) {
		return t.code == lex.codeFor("PLUS") || t.code == lex.codeFor("MNUS");
	}

	private boolean isMulOp(Lexical.token t) {
		return t.code == lex.codeFor("DVDE") || t.code == lex.codeFor("MTPY");
	}

	private boolean isNumber(Lexical.token t) {
		return t.code == lex.codeFor("INTV") || t.code == lex.codeFor("DFPV");
	}

	private boolean isStatementStart(Lexical.token t) {
		return token.code == lex.codeFor("IDNT") || token.code == lex.codeFor("BGIN") ||
		    token.code == lex.codeFor("IF__") || token.code == lex.codeFor("WHIL") ||
		    token.code == lex.codeFor("REPT") || token.code == lex.codeFor("FOR_") ||
		    token.code == lex.codeFor("WTLN") || token.code == lex.codeFor("RDLN");
	}
}
