package com.bgould.compiler.ADT;

import com.bgould.compiler.utils.StringUtils;

/**
 * SymbolTable
 */
public class SymbolTable {
	/**
	 * Creates a new, empty SymbolTable with maxSize rows.
	 *
	 * @param maxSize The maximum number of rows in this SymbolTable
	 */
	public SymbolTable(int maxSize) {}

	/**
	 * Appends a symbol with the given usage and value to the SymbolTable.
	 *
	 * If the given symbol is already in this SymbolTable (case-insensitive match), then no
	 * modifications are made to the SymbolTable.
	 *
	 * If the SymbolTable is already full, then no modifications are made, and an error code is
	 * returned.
	 *
	 * @param symbol The symbol to add to the table
	 * @param usage The way the symbol is used in the program
	 * @param value The value stored in the symbol
	 * @return If the symbol was successfully added, then the index it is now stored at.
	 * 		   If the symbol was already present, then the index where it was found.
	 * 		   If the SymbolTable was already full, then -1.
	 */
	public int AddSymbol(String symbol, char usage, int value) { return 0; }

	/**
	 * Appends a symbol with the given usage and value to the SymbolTable.
	 *
	 * If the given symbol is already in this SymbolTable (case-insensitive match), then no
	 * modifications are made to the SymbolTable.
	 *
	 * If the SymbolTable is already full, then no modifications are made, and an error code is
	 * returned.
	 *
	 * @param symbol The symbol to add to the table
	 * @param usage The way the symbol is used in the program
	 * @param value The value stored in the symbol
	 * @return If the symbol was successfully added, then the index it is now stored at.
	 * 		   If the symbol was already present, then the index where it was found.
	 * 		   If the SymbolTable was already full, then -1.
	 */
	public int AddSymbol(String symbol, char usage, double value) { return 0; }

	/**
	 * Appends a symbol with the given usage and value to the SymbolTable.
	 *
	 * If the given symbol is already in this SymbolTable (case-insensitive match), then no
	 * modifications are made to the SymbolTable.
	 *
	 * If the SymbolTable is already full, then no modifications are made, and an error code is
	 * returned.
	 *
	 * @param symbol The symbol to add to the table
	 * @param usage The way the symbol is used in the program
	 * @param value The value stored in the symbol
	 * @return If the symbol was successfully added, then the index it is now stored at.
	 * 		   If the symbol was already present, then the index where it was found.
	 * 		   If the SymbolTable was already full, then -1.
	 */
	public int AddSymbol(String symbol, char usage, String value) { return 0; }

	/**
	 * Finds a symbol in the SymbolTable (using case-insensitive search).
	 *
	 * @param symbol The symbol to search for
	 * @return The index where the symbol was found, or -1 if it not present in the SymbolTable.
	 */
	public int LookupSymbol(String symbol) { return 0; }

	/**
	 * Gets the symbol representation of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, then the empty string is returned.
	 *
	 * @param index The location of the symbol to get
	 * @return The name of the corresponding symbol, or the empty string if none.
	 */
	public String GetSymbol(int index) { return ""; }

	/**
	 * Gets the usage of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, then the null char is returned.
	 *
	 * @param index The location of the symbol to get
	 * @return The usage of the corresponding symbol, or the null char if none.
	 */
	public char GetUsage(int index) { return '\0'; }

	/**
	 * Gets the data type of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, then the null char is returned.
	 *
	 * @param index The location of the symbol to get
	 * @return The data type of the corresponding symbol, or the null char if none.
	 */
	public char GetDataType(int index) { return '\0'; }

	/**
	 * Gets the String value of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or the symbol there does not have a String data
	 * type, then an exception will be thrown.
	 *
	 * @param index The location of the symbol to get
	 * @return The String value of the corresponding symbol
	 */
	public String GetString(int index) { return ""; }

	/**
	 * Gets the integer value of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or the symbol there does not have a integer data
	 * type, then an exception will be thrown.
	 *
	 * @param index The location of the symbol to get
	 * @return The integer value of the corresponding symbol
	 */
	public int GetInteger(int index) { return 0; }

	/**
	 * Gets the floating-point value of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or the symbol there does not have a floating-point
	 * data type, then an exception will be thrown.
	 *
	 * @param index The location of the symbol to get
	 * @return The floating-point value of the corresponding symbol
	 */
	public double GetFloat(int index) { return 0; }

	/**
	 * Update the usage and value of the symbol at the given index.
	 *
	 * If there is no value at the given index, then no modifications to the SybmolTable are
	 * performed.
	 *
	 * @param index The location of the sybmol to modify
	 * @param usage The new usage code for the symbol
	 * @param value The new value to store in the symbol
	 */
	public void UpdateSymbol(int index, char usage, int value) {}

	/**
	 * Update the usage and value of the symbol at the given index.
	 *
	 * If there is no value at the given index, then no modifications to the SybmolTable are
	 * performed.
	 *
	 * @param index The location of the sybmol to modify
	 * @param usage The new usage code for the symbol
	 * @param value The new value to store in the symbol
	 */
	public void UpdateSymbol(int index, char usage, double value) {}

	/**
	 * Update the usage and value of the symbol at the given index.
	 *
	 * If there is no value at the given index, then no modifications to the SybmolTable are
	 * performed.
	 *
	 * @param index The location of the sybmol to modify
	 * @param usage The new usage code for the symbol
	 * @param value The new value to store in the symbol
	 */
	public void UpdateSymbol(int index, char usage, String value) {}

	/**
	 * Pretty prints the SymbolTable to a file. Empty rows are not printed.
	 *
	 * @param filename The file to print to
	 */
	public void PrintSymbolTable(String filename) {
		StringUtils.PrintToFile(filename, this.toString());
	}

	@Override
	public String toString() {
		return "";
	}
}
