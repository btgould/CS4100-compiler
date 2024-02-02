package com.bgould.compiler.ADT;

import java.util.Arrays;

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
	public QuadTable(int maxSize) {
		this.maxSize = maxSize;
		this.count = 0;

		// Initialize quads to all -1
		this.quads = new int[maxSize][4];
		Arrays.setAll(quads, i -> new int[] {-1, -1, -1, -1});
	}

	/**
	 * Gets the index of the next open slot in the QuadTable. If there are no open slots, returns
	 * -1.
	 *
	 * @return Index of the first open slot in this QuadTable, or -1
	 */
	public int NextQuad() { return (count < maxSize) ? count : -1; }

	/**
	 * Adds a new row to this QuadTable at the first open slot.
	 *
	 * If the QuadTable is already full, no modifications will be made to the table.
	 *
	 * @param opCode The code for operation type
	 * @param op1 First operation argument
	 * @param op2 Second operation argument
	 * @param op3 Third operation argument
	 */
	public void AddQuad(int opCode, int op1, int op2, int op3) {
		// check if table is full
		if (count >= maxSize)
			return;

		// add row
		quads[count] = new int[] {opCode, op1, op2, op3};
		count++;
	}

	/**
	 * Retrieves the operation stored at the given index of this QuadTable.
	 *
	 * If there is no operation stored at the given index, then an array of four -1s is returned.
	 *
	 * No error checking is performed. If index is out of range of maxSize, an exception will be
	 * thrown.
	 *
	 * @param index The location of the operation to retrieve
	 * @return An int[4] describing an operation: opCode, op1, op2, op3
	 */
	public int[] GetQuad(int index) { return quads[index]; }

	/**
	 * Changes the jump value (op3) for the operation stored at the given index.
	 *
	 * If there is no operation stored at the given index, or the index is out of range for this
	 * QuadTable, then no modifications are made.
	 *
	 * @param index The location of the operation to update
	 * @param op3 The new jump value
	 */
	public void UpdateJump(int index, int op3) {
		// check if there is an operation at the given index
		if (count <= index || index < 0)
			return;

		// update operation
		quads[index][3] = op3;
	}

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
		// Calculate length needed for each column of the table
		int idxColLen = 5;
		int codeColLen = 6;
		int op1ColLen = 3;
		int op2ColLen = 3;

		for (int i = 0; i < count; i++) {
			idxColLen = Math.max(idxColLen, Integer.valueOf(i).toString().length());
			codeColLen = Math.max(codeColLen, Integer.valueOf(quads[i][0]).toString().length());
			op1ColLen = Math.max(op1ColLen, Integer.valueOf(quads[i][1]).toString().length());
			op2ColLen = Math.max(op2ColLen, Integer.valueOf(quads[i][2]).toString().length());
		}

		// Construct table
		String repr = StringUtils.PadToLength("Index", idxColLen) + "\t" +
		              StringUtils.PadToLength("Opcode", codeColLen) + "\t" +
		              StringUtils.PadToLength("Op1", op1ColLen) + "\t" +
		              StringUtils.PadToLength("Op2", op1ColLen) + "\t"
		              + "Op3";
		for (int i = 0; i < count; i++) {
			repr += "\n";
			repr +=
				StringUtils.PadToLength(Integer.valueOf(i).toString(), idxColLen) + "\t" +
				StringUtils.PadToLength(Integer.valueOf(quads[i][0]).toString(), codeColLen) +
				"\t" + StringUtils.PadToLength(Integer.valueOf(quads[i][1]).toString(), op1ColLen) +
				"\t" + StringUtils.PadToLength(Integer.valueOf(quads[i][2]).toString(), op2ColLen) +
				"\t" + quads[i][3];
		}

		return repr;
	}

	private int maxSize;
	private int count;

	private int[][] quads;
}
