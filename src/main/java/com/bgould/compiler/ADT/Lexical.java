package com.bgould.compiler.ADT;

import java.io.*;

/**
 * Lexical
 */
public class Lexical {
	private File file;                     // File to be read for input
	private FileReader filereader;         // Reader, Java reqd
	private BufferedReader bufferedreader; // Buffered, Java reqd
	private String line;                   // Current line of input from file
	private int linePos;                   // Current character position in the current line
	private SymbolTable saveSymbols; // SymbolTable used in Lexical sent as parameter to construct
	private boolean EOF;             // End Of File indicator
	private boolean echo;            // true means echo each input line
	private boolean printToken;      // true to print found tokens here
	private int lineCount;           // line #in file, for echo- ing
	private boolean needLine;        // track when to read a new line

	// Tables to hold the reserve words and the mnemonics for token codes
	private final int sizeReserveTable = 50; // a few more than # reserves
	private ReserveTable reserveWords = new ReserveTable(sizeReserveTable);
	private ReserveTable mnemonics = new ReserveTable(sizeReserveTable);

	/**
	 * Constructs a new lexical analyzer on the given file
	 *
	 * @param filename Input file to parse as code
	 * @param symbols SymbolTable to store found identifiers and constants
	 * @param echoOn Set to print all input lines to stdout with a line number
	 */
	public Lexical(String filename, SymbolTable symbols, boolean echoOn) {
		saveSymbols = symbols; // map the initialized parameter to the local ST
		echo = echoOn;         // store echo status
		lineCount = 0;         // start the line number count
		line = "";             // line starts empty
		needLine = true;       // need to read a line
		printToken = false;    // default OFF, do not print tokens here within GetNextToken; call
		                       // setPrintToken to change it publicly.
		linePos = -1;          // no chars read yet

		// call initializations of tables
		initReserveWords(reserveWords);
		initMnemonics(mnemonics);

		// set up the file access, get first character, line retrieved 1st time
		try {
			file = new File(filename);         // creates a new file instance
			filereader = new FileReader(file); // reads the file
			bufferedreader =
				new BufferedReader(filereader); // creates a buffering character input stream
			EOF = false;
			currCh = GetNextChar();
		} catch (IOException e) {
			EOF = true;
			e.printStackTrace();
		}
	}

	/**
	 * Represents a lexical token
	 */
	public class token {
		// Actual text associated with this token
		public String lexeme;
		// Token code for reserve table
		public int code;
		// Chosen abbreviation for token
		public String mnemonic;

		token() {
			lexeme = "";
			code = 0;
			mnemonic = "";
		}
	}

	// This is the DISCARDABLE dummy method for getting and returning single characters STUDENT
	// TURN-IN SHOULD NOT USE THIS!
	private token dummyGet() {
		token result = new token();
		result.lexeme = "" + currCh; // have the first char
		currCh = GetNextChar();
		result.code = 0;
		result.mnemonic = "DUMY";
		return result;
	}

	// ******************* PUBLIC USEFUL METHODS These are nice for syntax to call later
	// given a mnemonic, find its token code value
	public int codeFor(String mnemonic) { return mnemonics.LookupName(mnemonic); }
	// given a mnemonic, return its reserve word
	public String reserveFor(String mnemonic) {
		return reserveWords.LookupCode(mnemonics.LookupName(mnemonic));
	}

	// Public access to the current End Of File status
	public boolean EOF() { return EOF; }
	// DEBUG enabler, turns on/OFF token printing inside of GetNextToken
	public void setPrintToken(boolean on) { printToken = on; }

	/**
	 * Initializes the given ReserveTable with a mapping of every reserved token in PL24 to its
	 * assigned integer code.
	 *
	 * @param reserveWords The ReserveTable to store reserved tokens in
	 */
	private void initReserveWords(ReserveTable reserveWords) {
		reserveWords.Add("GOTO", 0);
		reserveWords.Add("INTEGER", 1);
		reserveWords.Add("TO", 2);
		reserveWords.Add("DO", 3);
		reserveWords.Add("IF", 4);
		reserveWords.Add("THEN", 5);
		reserveWords.Add("ELSE", 6);
		reserveWords.Add("FOR", 7);
		reserveWords.Add("OF", 8);
		reserveWords.Add("WRITELN", 9);
		reserveWords.Add("READLN", 10);
		reserveWords.Add("BEGIN", 11);
		reserveWords.Add("END", 12);
		reserveWords.Add("VAR", 13);
		reserveWords.Add("WHILE", 14);
		reserveWords.Add("UNIT", 15);
		reserveWords.Add("LABEL", 16);
		reserveWords.Add("REPEAT", 17);
		reserveWords.Add("UNTIL", 18);
		reserveWords.Add("PROCEDURE", 19);
		reserveWords.Add("DOWNTO", 20);
		reserveWords.Add("FUNCTION", 21);
		reserveWords.Add("RETURN", 22);
		reserveWords.Add("REAL", 23);
		reserveWords.Add("STRING", 24);
		reserveWords.Add("ARRAY", 25);

		reserveWords.Add("/", 30);
		reserveWords.Add("*", 31);
		reserveWords.Add("+", 32);
		reserveWords.Add("-", 33);
		reserveWords.Add("(", 34);
		reserveWords.Add(")", 35);
		reserveWords.Add(";", 36);
		reserveWords.Add(":=", 37);
		reserveWords.Add(">", 38);
		reserveWords.Add("<", 39);
		reserveWords.Add(">=", 40);
		reserveWords.Add("<=", 41);
		reserveWords.Add("=", 42);
		reserveWords.Add("<>", 43);
		reserveWords.Add(",", 44);
		reserveWords.Add("[", 45);
		reserveWords.Add("]", 46);
		reserveWords.Add(":", 47);
		reserveWords.Add(".", 48);

		reserveWords.Add("NOTFOUND", 99);
	}

	/**
	 * Initializes the given ReserveTable with a mapping of 5-char mnemonic abbreviations for each
	 * reserved token in PL24 to its assigned integer code.
	 *
	 * @param mnemonics The ReserveTable to store mnemonics in
	 */
	private void initMnemonics(ReserveTable mnemonics) {
		mnemonics.Add("GOTO", 0);
		mnemonics.Add("INTR", 1);
		mnemonics.Add("TO__", 2);
		mnemonics.Add("DO__", 3);
		mnemonics.Add("IF__", 4);
		mnemonics.Add("THEN", 5);
		mnemonics.Add("ELSE", 6);
		mnemonics.Add("FOR_", 7);
		mnemonics.Add("OF__", 8);
		mnemonics.Add("WTLN", 9);
		mnemonics.Add("RDLN", 10);
		mnemonics.Add("BGIN", 11);
		mnemonics.Add("END_", 12);
		mnemonics.Add("VAR_", 13);
		mnemonics.Add("WHIL", 14);
		mnemonics.Add("UNIT", 15);
		mnemonics.Add("LABL", 16);
		mnemonics.Add("REPT", 17);
		mnemonics.Add("UNTL", 18);
		mnemonics.Add("PCDR", 19);
		mnemonics.Add("DNTO", 20);
		mnemonics.Add("FNCN", 21);
		mnemonics.Add("RTRN", 22);
		mnemonics.Add("DFPR", 23);
		mnemonics.Add("STRR", 24);
		mnemonics.Add("ARAY", 25);

		mnemonics.Add("DVDE", 30);
		mnemonics.Add("MTPY", 31);
		mnemonics.Add("PLUS", 32);
		mnemonics.Add("MNUS", 33);
		mnemonics.Add("LFTP", 34);
		mnemonics.Add("RITP", 35);
		mnemonics.Add("SCLN", 36);
		mnemonics.Add("DEFN", 37);
		mnemonics.Add("GTHN", 38);
		mnemonics.Add("LTHN", 39);
		mnemonics.Add("GRET", 40);
		mnemonics.Add("LSET", 41);
		mnemonics.Add("EQUL", 42);
		mnemonics.Add("NEQL", 43);
		mnemonics.Add("COMA", 44);
		mnemonics.Add("LBKT", 45);
		mnemonics.Add("RBKT", 46);
		mnemonics.Add("COLN", 47);
		mnemonics.Add("DOT_", 48);

		mnemonics.Add("IDNT", 50);
		mnemonics.Add("INTV", 51);
		mnemonics.Add("DFPV", 52);
		mnemonics.Add("STRV", 53);

		mnemonics.Add("UKWN", 99);
	}

	// ********************** UTILITY FUNCTIONS
	private void consoleShowError(String message) {
		System.out.println("**** ERROR FOUND: " + message);
	}

	private void consoleShowWarn(String message) { System.out.println("**** WARNING: " + message); }

	// Character category for alphabetic chars
	private boolean isLetter(char ch) {
		return (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')));
	}

	// Character category for 0..9
	private boolean isDigit(char ch) { return ((ch >= '0') && (ch <= '9')); }

	// Category for any whitespace to be skipped over
	private boolean isWhitespace(char ch) {
		// SPACE, TAB, NEWLINE are white space
		return ((ch == ' ') || (ch == '\t') || (ch == '\n'));
	}

	// Returns the VALUE of the next character without removing it from the input line. Useful for
	// checking 2-character tokens that start with a 1-character token.
	private char PeekNextChar() {
		char result = ' ';
		if ((needLine) || (EOF)) {
			result = ' '; // at end of line, so nothing
		} else {
			if ((linePos + 1) < line.length()) { // have a char to peek
				result = line.charAt(linePos + 1);
			}
		}
		return result;
	}

	// Called by GetNextChar when the cahracters in the current line are used up. STUDENT CODE
	// SHOULD NOT EVER CALL THIS!
	private void GetNextLine() {
		try {
			line = bufferedreader.readLine();
			if ((line != null) && (echo)) {
				lineCount++;
				System.out.println(String.format("%04d", lineCount) + " " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (line == null) { // The readLine returns null at EOF, set flag
			EOF = true;
		}
		linePos = -1;     // reset vars for new line if we have one
		needLine = false; // we have one, no need
		                  // the line is ready for the next call to get a character
	}

	// Called to get the next character from file, automatically gets a new line when needed. CALL
	// THIS TO GET CHARACTERS FOR GETIDENT etc.
	// Does fetch a newline character when line ends
	public char GetNextChar() {
		char result;
		if (needLine) // ran out last time we got a char, so get a new line
		{
			GetNextLine();
		}

		// try to get char from line buff
		if (EOF) {
			result = '\n';
			needLine = false;
		} else {
			if ((linePos < line.length() - 1)) { // have a character available
				linePos++;
				result = line.charAt(linePos);
			} else { // need a new line, but want to return eoln on this call first
				result = '\n';
				needLine = true; // will read a new line on next GetNextChar call
			}
		}
		return result;
	}

	// The constants below allow flexible comment start/end characters
	final char commentStart_1 = '{';
	final char commentEnd_1 = '}';
	final char commentStart_2 = '(';
	final char commentPairChar = '*';
	final char commentEnd_2 = ')';

	// Skips past single and multi-line comments, and outputs UNTERMINATED COMMENT when end of line
	// is reached before terminating
	String unterminatedComment = "Comment not terminated before End Of File ";
	String unterminatedString = "String not terminated before end of line";

	// cap length of tokens
	final int MAX_TOKEN_LEN = 20;
	final int MAX_INTEGER_LEN = 6;
	final int MAX_FLOAT_LEN = 12;

	// useful constant code numbers
	final int IDENTIFIER_CODE = 50;
	final int INTEGER_CODE = 51;
	final int FLOAT_CODE = 52;
	final int STRING_CODE = 53;

	public char skipComment(char curr) {
		if (curr == commentStart_1) {
			curr = GetNextChar();
			while ((curr != commentEnd_1) && (!EOF)) {
				curr = GetNextChar();
			}
			if (EOF) {
				consoleShowError(unterminatedComment);
			} else {
				curr = GetNextChar();
			}
		} else {
			if ((curr == commentStart_2) && (PeekNextChar() == commentPairChar)) {
				curr = GetNextChar(); // get the second
				curr = GetNextChar(); // into comment or end of comment

				while ((!((curr == commentPairChar) && (PeekNextChar() == commentEnd_2))) &&
				       (!EOF)) {
					curr = GetNextChar();
				}

				if (EOF) {
					consoleShowError(unterminatedComment);
				} else {
					curr = GetNextChar(); // must move past close
					curr = GetNextChar(); // must get following
				}
			}
		}
		return (curr);
	}

	// Reads past all whitespace as defined by isWhiteSpace. NOTE THAT COMMENTS ARE SKIPPED AS
	// WHITESPACE AS WELL!
	public char skipWhiteSpace() {
		do {
			while ((isWhitespace(currCh)) && (!EOF)) {
				currCh = GetNextChar();
			}
			currCh = skipComment(currCh);
		} while (isWhitespace(currCh) && (!EOF));
		return currCh;
	}

	private boolean isPrefix(char ch) { return ((ch == ':') || (ch == '<') || (ch == '>')); }
	private boolean isStringStart(char ch) { return ch == '\''; }
	// global char
	char currCh;

	private token getIdentifier() {
		token result = new token();

		if (isLetter(currCh)) {
			result.lexeme = "" + currCh; // have the first char
			currCh = GetNextChar();
		} else {
			return result;
		}
		int len = 1;

		while ((isLetter(currCh) || isDigit(currCh) || currCh == '_' || currCh == '$') &&
		       len < MAX_TOKEN_LEN) {
			result.lexeme = result.lexeme + currCh; // extend lexeme
			currCh = GetNextChar();
			len++;
		}

		// Check for truncation
		char nextChar = PeekNextChar();
		if (result.lexeme.length() >= MAX_TOKEN_LEN && (isLetter(nextChar) || isDigit(nextChar))) {
			consoleShowWarn("identifier truncated (" + result.lexeme + ")");
		}

		// end of token, lookup or IDENT
		result.code = reserveWords.LookupName(result.lexeme);
		if (result.code == -1) {
			result.code = IDENTIFIER_CODE;

			// Identifiers need to be added to the symbol table after truncation as needed
			// Identifiers receive a value of 0 by default
			saveSymbols.AddSymbol(result.lexeme, 'V', 0);
		}

		result.mnemonic = mnemonics.LookupCode(result.code);
		return result;
	}

	private token getNumber() {
		token result = new token();

		// empty token if no number
		if (isDigit(currCh)) {
			result.lexeme += currCh;
			result.code = INTEGER_CODE;
			currCh = GetNextChar();
		} else {
			return result;
		}

		int len = 1;

		// digits before decimal
		while (isDigit(currCh) && len < MAX_INTEGER_LEN) {
			result.lexeme += currCh;
			currCh = GetNextChar();
			len++;
		}

		// digits after decimal, before exponential
		if (currCh == '.' && len < MAX_FLOAT_LEN) {
			result.lexeme += currCh;
			result.code = FLOAT_CODE;
			currCh = GetNextChar();
			len++;

			while (isDigit(currCh) && len < MAX_FLOAT_LEN) {
				result.lexeme += currCh;
				currCh = GetNextChar();
				len++;
			}

			// digits after exponential
			if (currCh == 'E' && isDigit(PeekNextChar()) && len < MAX_FLOAT_LEN - 1) {
				// Add exponential and first digit
				result.lexeme += currCh;
				currCh = GetNextChar();
				len++;
				result.lexeme += currCh;
				currCh = GetNextChar();
				len++;

				// Add all remaining digits
				while (isDigit(currCh) && len < MAX_FLOAT_LEN) {
					result.lexeme += currCh;
					currCh = GetNextChar();
					len++;
				}
			}
		}

		// check for truncation
		char nextCh = PeekNextChar();
		boolean nextChIntOK = (result.code == INTEGER_CODE && (isDigit(nextCh) || nextCh == '.'));
		boolean nextChFloatOK =
			(result.code == FLOAT_CODE &&
		     (isDigit(nextCh) || (!result.lexeme.contains("E") && nextCh == 'E')));
		if ((len == MAX_INTEGER_LEN && nextChIntOK) || (len == MAX_FLOAT_LEN && nextChFloatOK)) {
			consoleShowWarn("number truncated (" + result.lexeme + ")");
		}

		// add constant number to symbol table
		if (integerOK(result.lexeme)) {
			saveSymbols.AddSymbol(result.lexeme, 'C', Integer.parseInt(result.lexeme));
		} else if (doubleOK(result.lexeme)) {
			saveSymbols.AddSymbol(result.lexeme, 'C', Double.parseDouble(result.lexeme));
		} else {
			// numbers of the form "123.456E-" followed by any non-digit will not be a valid integer
			// or double. However, since you have to check the 'E', '-', AND the character
			// afterwards, it is impossible to detect these errors with just 1 character lookahead.
			throw new IllegalStateException(
				"Poorly formatted number that cannot be detected by 1-char lookahead");
		}

		result.mnemonic = mnemonics.LookupCode(result.code);
		return result;
	}

	private token getString() {
		token result = new token();

		// Check for string start
		if (isStringStart(currCh)) {
			result.lexeme += currCh;
			result.code = STRING_CODE;
			result.mnemonic = mnemonics.LookupCode(result.code);

			currCh = GetNextChar();
		}

		// All characters inside string
		while (!(isStringStart(currCh) || currCh == '\n')) {
			result.lexeme += currCh;
			currCh = GetNextChar();
		}

		// String end, forcing no newline
		result.lexeme += currCh;

		if (!isStringStart(currCh)) {
			consoleShowError(unterminatedString);
			result.code = mnemonics.LookupName("UKWN");
			result.mnemonic = mnemonics.LookupCode(result.code);
		}

		currCh = GetNextChar();

		return result;
	}

	private token getOtherToken() {
		token result = new token();
		result.lexeme += currCh;
		currCh = GetNextChar();

		// check for two char reserve tokens
		if (isPrefix(result.lexeme.charAt(0))) {
			if (reserveWords.LookupName(result.lexeme + currCh) != -1) {
				result.lexeme += currCh;
				currCh = GetNextChar();
			}
		}

		// set token code
		result.code = reserveWords.LookupName(result.lexeme);
		if (result.code == -1) {
			result.code = mnemonics.LookupName("UKWN");
		}
		result.mnemonic = mnemonics.LookupCode(result.code);

		return result;
	}

	// Checks to see if a string contains a valid DOUBLE
	public boolean doubleOK(String stin) {
		boolean result;
		Double x;
		try {
			x = Double.parseDouble(stin);
			result = true;
		} catch (NumberFormatException ex) {
			result = false;
		}
		return result;
	}

	// Checks the input string for a valid INTEGER
	public boolean integerOK(String stin) {
		boolean result;
		int x;
		try {
			x = Integer.parseInt(stin);
			result = true;
		} catch (NumberFormatException ex) {
			result = false;
		}
		return result;
	}

	public token GetNextToken() {
		token result = new token();
		currCh = skipWhiteSpace();

		if (isLetter(currCh)) { // is identifier
			result = getIdentifier();
		} else if (isDigit(currCh)) { // is numeric
			result = getNumber();
		} else if (isStringStart(currCh)) { // string literal
			result = getString();
		} else // default char checks
		{
			result = getOtherToken();
		}

		if ((result.lexeme.equals("")) || (EOF)) {
			result = null;
		}

		// Print result if needed
		if (result != null) {
			if (printToken) {
				System.out.println("\t" + result.mnemonic + " | \t" +
				                   String.format("%04d", result.code) + " | \t" + result.lexeme);
			}
		}

		return result;
	}
}
