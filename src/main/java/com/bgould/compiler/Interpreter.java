package com.bgould.compiler;

import java.util.Scanner;

import com.bgould.compiler.ADT.QuadTable;
import com.bgould.compiler.ADT.ReserveTable;
import com.bgould.compiler.ADT.SymbolTable;
import com.bgould.compiler.utils.Constants;

/**
 * Class to execute compiled code using the generated QuadTable and SymbolTable
 */
public class Interpreter {

	public Interpreter() {
		initializeOpTable();
		programCounter = 0;
	}

	/**
	 * Executes the program specified by the given QuadTable and SymbolTable
	 *
	 * @param q QuadTable containing instruction memory for execution
	 * @param s SymbolTable containing data memory for execution
	 * @param traceOn Switch determining if trace data is logged to a file
	 * @param filename File to log trace data to
	 */
	public void InterpretQuads(QuadTable q, SymbolTable s, boolean traceOn, String filename) {
		// TODO: implement logging

		// Vars needed to execute instructions
		int[] currInstruction;
		char dstSymUsage;
		int math1, math2;
		int input;
		Scanner inputScanner = new Scanner(System.in);

		while (programCounter < Constants.MAX_QUAD) {
			// Read current instruction from QuadTable
			currInstruction = q.GetQuad(programCounter);
			String instrName = opTable.LookupCode(currInstruction[0]);
			dstSymUsage = s.GetUsage(currInstruction[3]); // don't change symbol usage by default

			// Increment PC, overwritten if branch chosen
			programCounter++;

			// Execute current instruction
			switch (instrName) {
			case "STOP": // terminate execution
				programCounter = Constants.MAX_QUAD;
				break;
			case "DIV": // *op3 = *op1 / *op2
				math1 = s.GetInteger(currInstruction[1]);
				math2 = s.GetInteger(currInstruction[2]);

				s.UpdateSymbol(currInstruction[3], dstSymUsage, math1 / math2);
				break;
			case "MUL": // *op3 = *op1 * *op2
				math1 = s.GetInteger(currInstruction[1]);
				math2 = s.GetInteger(currInstruction[2]);

				s.UpdateSymbol(currInstruction[3], dstSymUsage, math1 * math2);
				break;
			case "SUB": // *op3 = *op1 - *op2
				math1 = s.GetInteger(currInstruction[1]);
				math2 = s.GetInteger(currInstruction[2]);

				s.UpdateSymbol(currInstruction[3], dstSymUsage, math1 - math2);
				break;
			case "ADD": // *op3 = *op1 + *op2
				math1 = s.GetInteger(currInstruction[1]);
				math2 = s.GetInteger(currInstruction[2]);

				s.UpdateSymbol(currInstruction[3], dstSymUsage, math1 + math2);
				break;
			case "MOV": // *op3 = *op1
				s.UpdateSymbol(currInstruction[3], dstSymUsage, s.GetInteger(currInstruction[1]));
				break;
			case "PRINT": // display *op3 name and value
				System.out.println("Symbol name: " + s.GetSymbol(currInstruction[3]) +
				                   ", Symbol value: " + s.GetInteger(currInstruction[3]));
				break;
			case "READ":                        // *op3 = next user input int
				System.out.println("> ");       // prompt
				input = inputScanner.nextInt(); // read int
				s.UpdateSymbol(currInstruction[3], dstSymUsage, input);
				break;
			case "JMP": // pc = op3
				programCounter = currInstruction[3];
				break;
			case "JZ": // if *op1 =0 0, pc = op3
				if (s.GetInteger(currInstruction[1]) == 0) {
					programCounter = currInstruction[3];
				}
				break;
			case "JP": // if *op1 > 0
				if (s.GetInteger(currInstruction[1]) > 0) {
					programCounter = currInstruction[3];
				}
				break;
			case "JN": // if *op1 < 0
				if (s.GetInteger(currInstruction[1]) < 0) {
					programCounter = currInstruction[3];
				}
				break;
			case "JNZ": // if *op1 != 0
				if (s.GetInteger(currInstruction[1]) != 0) {
					programCounter = currInstruction[3];
				}
				break;
			case "JNP": // if *op1 <= 0
				if (s.GetInteger(currInstruction[1]) <= 0) {
					programCounter = currInstruction[3];
				}
				break;
			case "JNN": // if *op1 >= 0
				if (s.GetInteger(currInstruction[1]) >= 0) {
					programCounter = currInstruction[3];
				}
				break;
			case "JINDR": // pc = *op3
				programCounter = s.GetInteger(currInstruction[3]);
				break;

			default: // unrecognized instruction
				throw new IllegalArgumentException("QuadTable contained an unrecognized operation");
			}
		}

		// clean up Scanner object
		inputScanner.close();
	}

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
