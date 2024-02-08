package com.bgould.compiler.ADT;

import java.util.ArrayList;

import com.bgould.compiler.utils.StringUtils;

/**
 * Implements a fixed index list of Symbols used in the compiled program.
 */
public class SymbolTable {
	private class Symbol<T> {
		public Symbol(String indentifier, char usage, char dataType, T value) {
			this.indentifier = indentifier;
			this.usage = usage;
			this.dataType = dataType;
			this.value = value;
		}

		public String getIndentifier() { return indentifier; }
		public char getDataType() { return dataType; }
		public T getValue() { return value; }
		public char getUsage() { return usage; }

		public void setUsage(char usage) { this.usage = usage; }
		public void setValue(T value) { this.value = value; }

		private String indentifier;
		private char usage;
		private char dataType;
		private T value;
	}

	/**
	 * Creates a new, empty SymbolTable with maxSize rows.
	 *
	 * @param maxSize The maximum number of rows in this SymbolTable
	 */
	public SymbolTable(int maxSize) {
		this.maxSize = maxSize;
		this.count = 0;

		this.symbols = new ArrayList<Symbol<?>>(maxSize);
	}

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
	public int AddSymbol(String symbol, char usage, int value) {
		// check if symbol in table
		int symIdx = LookupSymbol(symbol);
		if (symIdx != -1)
			return symIdx;

		// check if table is full
		if (count >= maxSize)
			return -1;

		// add symbol to table
		symbols.add(new Symbol<Integer>(symbol, usage, 'I', value));
		return count++;
	}

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
	public int AddSymbol(String symbol, char usage, double value) {
		// check if symbol in table
		int symIdx = LookupSymbol(symbol);
		if (symIdx != -1)
			return symIdx;

		// check if table is full
		if (count >= maxSize)
			return -1;

		// add symbol to table
		symbols.add(new Symbol<Double>(symbol, usage, 'F', value));
		return count++;
	}

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
	public int AddSymbol(String symbol, char usage, String value) {
		// check if symbol in table
		int symIdx = LookupSymbol(symbol);
		if (symIdx != -1)
			return symIdx;

		// check if table is full
		if (count >= maxSize)
			return -1;

		// add symbol to table
		symbols.add(new Symbol<String>(symbol, usage, 'S', value));
		return count++;
	}

	/**
	 * Finds a symbol in the SymbolTable (using case-insensitive search).
	 *
	 * @param symbol The symbol to search for
	 * @return The index where the symbol was found, or -1 if it not present in the SymbolTable.
	 */
	public int LookupSymbol(String symbol) {
		// check if symbol in table w/ linear search
		for (int i = 0; i < count; i++) {
			if (symbols.get(i).getIndentifier().equalsIgnoreCase(symbol))
				return i;
		}

		// symbol not found in table
		return -1;
	}

	/**
	 * Gets the symbol representation of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or index is out of range for maxSize, then the
	 * empty string is returned.
	 *
	 * @param index The location of the symbol to get
	 * @return The name of the corresponding symbol, or the empty string if none.
	 */
	public String GetSymbol(int index) {
		// check if there is a symbol at the given index
		if (count <= index || index < 0)
			return "";

		return symbols.get(index).getIndentifier();
	}

	/**
	 * Gets the usage of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or index is out of range for maxSize, then the null
	 * char is returned.
	 *
	 * @param index The location of the symbol to get
	 * @return The usage of the corresponding symbol, or the null char if none.
	 */
	public char GetUsage(int index) {
		// check if there is a symbol at the given index
		if (count <= index || index < 0)
			return '\0';

		return symbols.get(index).getUsage();
	}

	/**
	 * Gets the data type of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or index is out of range for maxSize, then the null
	 * char is returned.
	 *
	 * @param index The location of the symbol to get
	 * @return The data type of the corresponding symbol, or the null char if none.
	 */
	public char GetDataType(int index) {
		// check if there is a symbol at the given index
		if (count <= index || index < 0)
			return '\0';

		return symbols.get(index).getDataType();
	}

	/**
	 * Gets the String value of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or the symbol there does not have a String data
	 * type, then an exception may be thrown.
	 *
	 * Additionally, no error checking is performed on index. If it is out of range of maxSize, an
	 * exception may be thrown.
	 *
	 * @param index The location of the symbol to get
	 * @return The String value of the corresponding symbol
	 */
	// HACK: more specific exceptions
	public String GetString(int index) { return (String) symbols.get(index).getValue(); }

	/**
	 * Gets the integer value of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or the symbol there does not have a integer data
	 * type, then an exception may be thrown.
	 *
	 * Additionally, no error checking is performed on index. If it is out of range of maxSize, an
	 * exception may be thrown.
	 *
	 * @param index The location of the symbol to get
	 * @return The integer value of the corresponding symbol
	 */
	// HACK: more specific exceptions
	public int GetInteger(int index) { return (int) symbols.get(index).getValue(); }

	/**
	 * Gets the floating-point value of the symbol stored at the given index.
	 *
	 * If there is no symbol at the given index, or the symbol there does not have a floating-point
	 * data type, then an exception may be thrown.
	 *
	 * Additionally, no error checking is performed on index. If it is out of range of maxSize, an
	 * exception may be thrown.
	 *
	 * @param index The location of the symbol to get
	 * @return The floating-point value of the corresponding symbol
	 */
	// HACK: more specific exceptions
	public double GetFloat(int index) { return (double) symbols.get(index).getValue(); }

	/**
	 * Update the usage and value of the symbol at the given index.
	 *
	 * If there is no value at the given index, then no modifications to the SybmolTable are
	 * performed.
	 *
	 * If the symbol at the given index does not have an integer data type, than an exception may be
	 * thrown.
	 *
	 * @param index The location of the sybmol to modify
	 * @param usage The new usage code for the symbol
	 * @param value The new value to store in the symbol
	 */
	public void UpdateSymbol(int index, char usage, int value) {
		// check if there is a symbol at the given index
		if (count <= index || index < 0)
			return;

		// Update the symbol's information
		@SuppressWarnings("unchecked")
		Symbol<Integer> sym = (Symbol<Integer>) symbols.get(
			index); // This raises an exception if symbol is not an integer
		// HACK: more specific exceptions
		sym.setUsage(usage);
		sym.setValue(value);
	}

	/**
	 * Update the usage and value of the symbol at the given index.
	 *
	 * If there is no value at the given index, then no modifications to the SybmolTable are
	 * performed.
	 *
	 * If the symbol at the given index does not have a floating-point data type, than an exception
	 * may be thrown.
	 *
	 * @param index The location of the sybmol to modify
	 * @param usage The new usage code for the symbol
	 * @param value The new value to store in the symbol
	 */
	public void UpdateSymbol(int index, char usage, double value) {
		// check if there is a symbol at the given index
		if (count <= index || index < 0)
			return;

		// Update the symbol's information
		@SuppressWarnings("unchecked")
		Symbol<Double> sym = (Symbol<Double>) symbols.get(
			index); // This raises an exception if symbol is not a floating-point
		// HACK: more specific exceptions
		sym.setUsage(usage);
		sym.setValue(value);
	}

	/**
	 * Update the usage and value of the symbol at the given index.
	 *
	 * If there is no value at the given index, then no modifications to the SybmolTable are
	 * performed.
	 *
	 * If the symbol at the given index does not have a String data type, than an exception
	 * may be thrown.
	 *
	 * @param index The location of the sybmol to modify
	 * @param usage The new usage code for the symbol
	 * @param value The new value to store in the symbol
	 */
	public void UpdateSymbol(int index, char usage, String value) {
		// check if there is a symbol at the given index
		if (count <= index || index < 0)
			return;

		// Update the symbol's information
		@SuppressWarnings("unchecked")
		Symbol<String> sym = (Symbol<String>) symbols.get(
			index); // This raises an exception if symbol is not a String
		// HACK: more specific exceptions
		sym.setUsage(usage);
		sym.setValue(value);
	}

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
		// Calculate length needed for each column of the table
		int idxColLen = 5;
		int nameColLen = 4;
		int useColLen = 3;
		int typeColLen = 4;

		for (int i = 0; i < count; i++) {
			idxColLen = Math.max(idxColLen, Integer.valueOf(i).toString().length());
			nameColLen = Math.max(nameColLen, symbols.get(i).getIndentifier().length());
		}

		// Construct table
		String repr = StringUtils.PadToLength("Index", idxColLen) + "\t" +
		              StringUtils.PadToLength("Name", nameColLen) + "\t" +
		              StringUtils.PadToLength("Use", useColLen) + "\t" +
		              StringUtils.PadToLength("Type", typeColLen) + "\t"
		              + "Value";
		for (int i = 0; i < count; i++) {
			Symbol<?> sym = symbols.get(i);
			repr += "\n";
			repr += StringUtils.PadToLength(Integer.valueOf(i).toString(), idxColLen) + "\t" +
			        StringUtils.PadToLength(sym.getIndentifier(), nameColLen) + "\t" +
			        StringUtils.PadToLength(String.valueOf(sym.getUsage()), useColLen) + "\t" +
			        StringUtils.PadToLength(String.valueOf(sym.getDataType()), typeColLen) + "\t" +
			        sym.getValue();
		}

		return repr;
	}

	private int maxSize;                  // maximum symbols that fit in this table
	private int count;                    // the number of symbols currently in this table
	private ArrayList<Symbol<?>> symbols; // A list of symbols stored in the table
}
