package com.bgould.compiler.ADT;

import com.bgould.compiler.utils.StringUtils;

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
  public ReserveTable(int maxSize) {
    this.names = new String[maxSize];
    this.codes = new int[maxSize];
    this.count = 0;
  }

  /**
   * Appends a new row to the ReserveTable. Duplicates are allowed, and entries
   * are not sorted in any way
   *
   * @param name The name of the row in the ReserveTable
   * @param code The code of the row
   * @return The index of the added row in the ReserveTable
   */
  public int Add(String name, int code) {
    names[count] = name;
    codes[count] = code;
    return count++;
  }

  /**
   * Gets the code of a row with the given name (case-insensitive)
   *
   * @param name The name of the row to search for
   * @return The code of the row in the ReserveTable, or -1 if row not found
   */
  public int LookupName(String name) {
    int code = -1;

    // linear search through names array
    for (int i = 0; i < names.length; i++) {
      if (name.equalsIgnoreCase(names[i])) {
        code = codes[i];
        break;
      }
    }

    return code;
  }

  /**
   * Gets the name of a row with the given code
   *
   * @param code The code of the row to search for
   * @return The name of the row in the ReserveTable, or an empty String if row
   *     not found
   */
  public String LookupCode(int code) {
    String name = "";

    // linear search through codes array
    for (int i = 0; i < codes.length; i++) {
      if (code == codes[i]) {
        name = names[i];
      }
    }

    return name;
  }

  /**
   * Pretty prints the contents of the ReserveTable. Empty rows are not printed
   *
   * @param filename The name of the file to print to.
   */
  public void PrintReserveTable(String filename) {
    StringUtils.PrintToFile(filename, toString());
  }

  /**
   * Represents the ReserveTable as a nicely formatted string.
   *
   * @return A String representation of the ReserveTable.
   */
  @Override
  public String toString() {
    // Calculate length needed for each column of the table
    int idxColLen = 5;
    int nameColLen = 4;

    idxColLen = Math.max(idxColLen, Integer.valueOf(count).toString().length());
    for (int i = 0; i < count; i++) {
      nameColLen = Math.max(nameColLen, names[i].length());
    }

    // Construct table
    String repr = StringUtils.PadToLength("Index", idxColLen) + "\t" +
                  StringUtils.PadToLength("Name", nameColLen) + "\t"
                  + "Code";
    for (int i = 0; i < count; i++) {
      repr += "\n";
      repr +=
          StringUtils.PadToLength(Integer.valueOf(i).toString(), idxColLen) +
          "\t" + StringUtils.PadToLength(names[i], nameColLen) + "\t" +
          codes[i];
    }

    return repr;
  }

  // Array of names in ReserveTable
  private String[] names;
  // Array of codes in ReserveTable
  private int[] codes;
  // Number of elements added to ReserveTable
  private int count;
}
