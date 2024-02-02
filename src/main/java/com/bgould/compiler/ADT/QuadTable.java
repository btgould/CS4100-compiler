package com.bgould.compiler.ADT;

import com.bgould.compiler.utils.StringUtils;

/**
 * Data structure storing "assembly" operations.
 *
 * Each operation is stored as 4 integers, giving the name QuadTable.
 */
public class QuadTable {
	/**
	 * Creates a new, empty QuadTable with maxSize rows
	 *
	 * @param maxSize The maximum number of rows to store in this QuadTable
	 */
	public QuadTable(int maxSize) {}

	/**
	 * Gets the index of the next open slot in the QuadTable
	 *
	 * @return Index of the first open slot in this QuadTable
	 */
	public int NextQuad() { return 0; }

	/**
	 * Adds a new row to this QuadTable at the first open slot
	 *
	 * @param opCode The code for operation type
	 * @param op1 First operation argument
	 * @param op2 Second operation argument
	 * @param op3 Third operation argument
	 */
	public void AddQuad(int opCode, int op1, int op2, int op3) {}

	/**
	 * Retrieves the operation stored at the given index of this QuadTable
	 *
	 * @param index The location of the operation to retrieve
	 * @return An int[4] describing an operation: opCode, op1, op2, op3
	 */
	public int[] GetQuad(int index) { return new int[4]; }

	/**
	 * Changes the jump value (op3) for the operation stored at the given index
	 *
	 * @param index The location of the operation to update
	 * @param op3 The new jump value
	 */
	public void UpdateJump(int index, int op3) {}

	/**
	 * Pretty prints the contents of the QuadTable. Empty rows are not printed.
	 *
	 * @param filename The name of the file to print to.
	 */
	public void PrintQuadTable(String filename) {
		StringUtils.PrintToFile(filename, this.toString());
	}

	@Override
	public String toString() {
		return "";
	}
}
