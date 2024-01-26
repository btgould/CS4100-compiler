package com.bgould.compiler.ADT;

/**
 * ReserveTable
 */
public class ReserveTable {
  /**
   * Initializes a ReserveTable with enough internal storage to contain maxSize
   * entries. This storage size cannot be changed later.
   *
   * @param maxSize The max number of rows in this ReserveTable
   */
  public ReserveTable(int maxSize) {}

  /**
   * Appends a new row to the ReserveTable. Duplicates are allowed, and entries
   * are not sorted in any way
   *
   * @param name The name of the row in the ReserveTable
   * @param code The code of the row
   * @return The index of the added row in the ReserveTable
   */
  public int Add(String name, int code) { return 0; }

  /**
   * Gets the code of a row with the given name
   *
   * @param name The name of the row to search for
   * @return The code of the row in the ReserveTable, or -1 if row not found
   */
  public int LookupName(String name) { return 0; }

  /**
   * Gets the name of a row with the given code
   *
   * @param code The code of the row to search for
   * @return The name of the row in the ReserveTable, or an empty String if row
   *     not found
   */
  public String LookupCode(int code) { return ""; }

		/**
		 * Pretty prints the contents of the ReserveTable. Empty rows are not printed
		 *
		 * @param filename The name of the file to print to.
		 */
  public void PrintReserveTable(String filename) {}
}
