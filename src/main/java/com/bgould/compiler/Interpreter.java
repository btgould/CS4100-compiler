package com.bgould.compiler;

import com.bgould.compiler.ADT.QuadTable;
import com.bgould.compiler.ADT.ReserveTable;
import com.bgould.compiler.ADT.SymbolTable;

/**
 * Class to execute compiled code using the generated QuadTable and SymbolTable
 */
public class Interpreter {

	public Interpreter() { initializeOpTable();
	programCounter = 0;}

	/**
	 * Executes the program specified by the given QuadTable and SymbolTable
	 *
	 * @param q QuadTable containing instruction memory for execution
	 * @param s SymbolTable containing data memory for execution
	 * @param traceOn Switch determining if trace data is logged to a file
	 * @param filename File to log trace data to
	 */
	public void InterpretQuads(QuadTable q, SymbolTable s, boolean traceOn, String filename) {}

	/**
	 * Hardcodes the data and instruction memory necessary to execute a simple factorial program.
	 *
	 * @param sTable The SymbolTable to hold data memory (assumed non-null)
	 * @param qTable The QuadTable to hold instruction memory (assumed non-null)
	 *
	 * @return true if initialization was performed successfully, false otherwise
	 */
	public boolean initializeFactorialTest(SymbolTable sTable, QuadTable qTable) { return true; }

	/**
	 * Hardcodes the data and instruction memory necessary to execute a simple summation program.
	 *
	 * @param sTable The SymbolTable to hold data memory (assumed non-null)
	 * @param qTable The QuadTable to hold instruction memory (assumed non-null)
	 *
	 * @return true if initialization was performed successfully, false otherwise
	 */
	public boolean initializeSummationTest(SymbolTable sTable, QuadTable qTable) { return true; }

	/**
	 * Adds QuadTable instruction names and their corresponding codes to the opcode table.
	 */
	private void initializeOpTable() {
		opTable = new ReserveTable(20);

		opTable.Add("STOP", 0);

		opTable.Add("DIV", 1);
		opTable.Add("MUL", 2);
		opTable.Add("SUB", 3);
		opTable.Add("ADD", 4);

		opTable.Add("MOV", 5);

		opTable.Add("PRINT", 6);
		opTable.Add("READ", 7);

		opTable.Add("JMP", 8);
		opTable.Add("JZ", 9);
		opTable.Add("JP", 10);
		opTable.Add("JN", 11);
		opTable.Add("JNZ", 12);
		opTable.Add("JNP", 13);
		opTable.Add("JNN", 14);
		opTable.Add("JINDR", 15);
	}

	private ReserveTable opTable;
	private int programCounter;
}
