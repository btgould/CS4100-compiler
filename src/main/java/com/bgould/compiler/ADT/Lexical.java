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
	private final int sizeReserveTable = 50;
	private ReserveTable reserveWords =
		new ReserveTable(sizeReserveTable); // a few more than # reserves
	private ReserveTable mnemonics = new ReserveTable(sizeReserveTable);
	// a few more than # reserves

	// constructor
	public Lexical(String filename, SymbolTable symbols, boolean echoOn) {
		saveSymbols = symbols; // map the initialized parameter to the local ST
		echo = echoOn;         // store echo status
		lineCount = 0;         // start the line number count
		line = "";             // line starts empty
		needLine = true;       // need to read a line
		printToken = false;    // default OFF, do not print tokesn here within GetNextToken; call
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

	// inner class "token" is declared here, no accessors needed
	public class token {
		public String lexeme;
		public int code;
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

	/* @@@ */
	private void initReserveWords(ReserveTable reserveWords) {
		// Student must provide the rest
		reserveWords.Add("BEGIN", 11);
		// 1 and 2-char
		reserveWords.Add("/", 30);
		reserveWords.Add("<>", 43);
	}

	/* @@@ */
	private void initMnemonics(ReserveTable mnemonics) {
		// Student must create their own 5-char mnemonics
		mnemonics.Add("ARRAY", 25);
		// 1 and 2-char
		mnemonics.Add("NTEQL", 43);
	}

	// ********************** UTILITY FUNCTIONS
	private void consoleShowError(String message) {
		System.out.println("**** ERROR FOUND: " + message);
	}

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
		} else            //
		{
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
				// while ((curr != commentPairChar) && (PeekNextChar() != commentEnd_2) &&(!EOF)) {
				while ((!((curr == commentPairChar) && (PeekNextChar() == commentEnd_2))) &&
				       (!EOF)) {
					// if (lineCount >=4) { System.out.println("In Comment, curr, peek: "+curr+",
					// "+PeekNextChar());}
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
	private boolean isStringStart(char ch) { return ch == '"'; }
	// global char
	char currCh;

	private token getIdentifier() { return dummyGet(); }

	private token getNumber() {
		/* a number is: see token description! */
		return dummyGet();
	}

	private token getString() { return dummyGet(); }

	private token getOtherToken() { return dummyGet(); }

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
		// set the mnemonic
		if (result != null) {
			// THIS LINE REMOVED-- PUT BACK IN TO USE LOOKUP
			// result.mnemonic = mnemonics.LookupCode(result.code);
			if (printToken) {
				System.out.println("\t" + result.mnemonic + " | \t" +
				                   String.format("%04d", result.code) + " | \t" + result.lexeme);
			}
		}
		return result;
	}
}