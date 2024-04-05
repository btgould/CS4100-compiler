package com.bgould.compiler.ADT;

/**
 * Class performing CFG based syntactic parsing of source code
 *
 */
public class Syntactic {
	private String filein;          // The full file path to input file
	private SymbolTable symbolList; // Symbol table storing ident/const
	private Lexical lex;            // Lexical analyzer
	private Lexical.token token;    // Next Token retrieved
	private boolean traceon;        // Controls tracing mode
	private int level = 0;          // Controls indent for trace mode
	private boolean anyErrors;      // Set TRUE if an error happens
	private final int symbolSize = 250;

	public Syntactic(String filename, boolean traceOn) {
		filein = filename;
		traceon = traceOn;
		symbolList = new SymbolTable(symbolSize);
		lex = new Lexical(filein, symbolList, true);
		lex.setPrintToken(traceOn);
		anyErrors = false;
	}

	// The interface to the syntax analyzer, initiates parsing
	// Uses variable RECUR to get return values throughout the non-terminal methods
	public void parse() {
		int recur = 0;
		// prime the pump to get the first token to process
		token = lex.GetNextToken();
		// call PROGRAM
		recur = Program();
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
			symbolList.UpdateSymbol(symbolList.LookupSymbol(token.lexeme), 'P', 0);
			// move on
			token = lex.GetNextToken();
		}

		return recur;
	}

	// Non Terminal PROGRAM is fully implemented here.
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

	// Non Terminal BLOCK is fully implemented here.
	private int Block() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Block", true);

		if (token.code == lex.codeFor("BGIN")) {
			token = lex.GetNextToken();
			recur = Statement();
			while ((token.code == lex.codeFor("SCLN")) && (!lex.EOF()) && (!anyErrors)) {
				token = lex.GetNextToken();
				recur = Statement();
			}
			if (token.code == lex.codeFor("END_")) {
				token = lex.GetNextToken();
			} else {
				error(lex.reserveFor("END_"), token.lexeme);
			}
		} else {
			error(lex.reserveFor("BGIN"), token.lexeme);
		}

		trace("Block", false);
		return recur;
	}

	// Not a Non Terminal, but used to shorten Statement code body for readability.
	//<variable> $COLON-EQUALS <simple expression>
	private int handleAssignment() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("handleAssignment", true);

		// have ident already in order to get to here, handle as Variable
		recur = Variable(); // Variable moves ahead, next token ready
		if (token.code == lex.codeFor("DEFN")) {
			token = lex.GetNextToken();
			recur = SimpleExpression();
		} else {
			error(lex.reserveFor("DEFN"), token.lexeme);
		}

		trace("handleAssignment", false);
		return recur;
	}

	/**
	 * Syntactically parses a simple arithmetic expression.
	 * Production rule: [<sign>] <term> {<addop> <term>}*
	 *
	 * @return Unused for now
	 */
	private int SimpleExpression() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("SimpleExpression", true);

		// optional sign value
		if (isAddOp(token)) {
			recur = Sign();
		}

		// mandatory term
		recur = Term();

		// optional additional terms
		while ((!anyErrors) && isAddOp(token)) {
			recur = AddOp();
			recur = Term();
		}

		trace("SimpleExpression", false);
		return recur;
	}

	/**
	 * Syntactically parses a term in an arithmetic expression
	 * Production rule: <factor> {<mulop> <factor> }*
	 *
	 * @return Unused for now
	 */
	private int Term() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Term", true);

		// Mandatory first factor
		recur = Factor();

		// Optional additional factors
		while ((!anyErrors) && isMulOp(token)) {
			recur = MulOp();
			recur = Factor();
		}

		trace("Term", false);
		return recur;
	}

	/**
	 * Syntactically parses a factor in an arithmetic expression
	 * Production rule: <unsigned constant> | <variable> | $LPAR <simple expression> $RPAR
	 *
	 * @return Unused for now
	 */
	private int Factor() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Factor", true);

		if (isNumber(token)) { // some constant
			recur = UnsignedConstant();
		} else if (token.code == lex.codeFor("IDNT")) { // some variable
			recur = Variable();
		} else if (token.code == lex.codeFor("LFTP")) { // nested expression
			token = lex.GetNextToken();

			recur = SimpleExpression();

			if (token.code == lex.codeFor("RITP")) {
				token = lex.GetNextToken();
			} else {
				error("')'", token.lexeme);
			}
		} else {
			error("Constant, Variable, or '('", token.lexeme);
		}

		trace("Factor", false);
		return recur;
	}

	/**
	 * Syntactically parses an unsigned constant in an arithmetic expression
	 * Production rule: <unsigned number>
	 *
	 * @return Unused for now
	 */
	private int UnsignedConstant() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("UnsignedConstant", true);

		recur = UnsignedNumber();

		trace("UnsignedConstant", false);
		return recur;
	}

	/**
	 * Syntactically parses an unsigned number in an arithmetic expression
	 * Production rule: $FLOAT | $INTEGER
	 *
	 * @return Unused for now
	 */
	private int UnsignedNumber() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("UnsignedNumber", true);

		if (!isNumber(token)) {
			error("Float or Integer", token.lexeme);
		}
		token = lex.GetNextToken();

		trace("UnsignedNumber", false);
		return recur;
	}

	/**
	 * Syntactically parses an addition operation in an arithmetic expression
	 * Production rule: $PLUS | $MINUS
	 *
	 * @return Unused for now
	 */
	private int AddOp() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("AddOp", true);

		if (!isAddOp(token)) {
			error("+ or -", token.lexeme);
		}
		token = lex.GetNextToken();

		trace("AddOp", false);
		return recur;
	}

	/**
	 * Syntactically parses a sign in an arithmetic expression
	 * Production rule: $PLUS | $MINUS
	 *
	 * @return Unused for now
	 */
	private int Sign() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Sign", true);

		if (!isAddOp(token)) {
			error("+ or -", token.lexeme);
		}
		token = lex.GetNextToken();

		trace("Sign", false);
		return recur;
	}

	/**
	 * Syntactically parses a multiplication operation in an arithmetic expression
	 * Production rule: $MULTIPLY | $DIVIDE
	 *
	 * @return Unused for now
	 */
	private int MulOp() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("MulOp", true);

		if (!isMulOp(token)) {
			error("* or /", token.lexeme);
		}
		token = lex.GetNextToken();

		trace("MulOp", false);
		return recur;
	}

	// Eventually this will handle all possible statement starts in
	// a nested if/else or switch structure. Only ASSIGNMENT is implemented now.
	private int Statement() {
		int recur = 0;
		if (anyErrors) {
			return -1;
		}
		trace("Statement", true);

		if (token.code == lex.codeFor("IDNT")) { // must be an ASSIGNMENT
			recur = handleAssignment();
		} else {
			if (token.code == lex.codeFor("IF__")) { // must be an IF
				// this would handle the rest of the IF statment IN PART B
			} else
			// if/elses should look for the other possible statement starts...
			// but not until PART B
			{
				error("Statement start", token.lexeme);
			}
		}

		trace("Statement", false);
		return recur;
	}

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
			token = lex.GetNextToken();
		} else {
			error("Variable", token.lexeme);
		}

		trace("Variable", false);
		return recur;
	}

	/**
	 * *************************************************
	 * UTILITY FUNCTIONS USED THROUGHOUT THIS CLASS
	 */
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

	/* Template for all the non-terminal method bodies
	// ALL OF THEM SHOULD LOOK LIKE THE FOLLOWING AT THE ENTRY/EXIT POINTS
	private int exampleNonTerminal(){
	int recur = 0; //Return value used later
	if (anyErrors) { // Error check for fast exit, error status -1
	return -1;
	}
	trace("NameOfThisMethod", true);
	// The unique non-terminal stuff goes here, assigning to "recur" based
	// on recursive calls that were made
	trace("NameOfThisMethod", false);
	// Final result of assigning to "recur" in the body is returned
	return recur;
	}
	*/
}
