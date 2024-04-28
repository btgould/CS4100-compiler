package com.bgould.compiler;

import java.util.Scanner;

import com.bgould.compiler.ADT.QuadTable;
import com.bgould.compiler.ADT.ReserveTable;
import com.bgould.compiler.ADT.SymbolTable;
import com.bgould.compiler.utils.Constants;
import com.bgould.compiler.utils.StringUtils;

/**
 * Class to execute compiled code using the generated QuadTable and SymbolTable
 */
public class Interpreter {

	public Interpreter() { initializeOpTable(); }

	/**
	 * Executes the program specified by the given QuadTable and SymbolTable
	 *
	 * @param q QuadTable containing instruction memory for execution
	 * @param s SymbolTable containing data memory for execution
	 * @param traceOn Switch determining if trace data is logged to a file
	 * @param filename File to log trace data to
	 */
	public void InterpretQuads(QuadTable q, SymbolTable s, boolean traceOn, String filename) {
		// Vars needed to execute instructions
		programCounter = 0;
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

			if (traceOn) {
				String trace =
					makeTraceString(programCounter, currInstruction[0], currInstruction[1],
				                    currInstruction[2], currInstruction[3]);
				StringUtils.AppendToFile(filename, trace);
			}

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
				String value = "";
				switch (s.GetDataType(currInstruction[3])) {
				case SymbolTable.INTEGER_TYPE:
					value = Integer.valueOf(s.GetInteger(currInstruction[3])).toString();
					break;
				case SymbolTable.REAL_TYPE:
					value = Double.valueOf(s.GetFloat(currInstruction[3])).toString();
					break;
				case SymbolTable.STRING_TYPE:
					value = s.GetString(currInstruction[3]);
					break;
				default:
					throw new RuntimeException("Invalid symbol data type!");
				}
				System.out.println(value);
				break;
			case "READ":                      // *op3 = next user input int
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
	public boolean initializeFactorialTest(SymbolTable sTable, QuadTable qTable) {
		boolean sTableSuccess = true;

		// Initialize data memory for factorial
		sTableSuccess |= (sTable.AddSymbol("n", 'V', 10) != -1);
		sTableSuccess |= (sTable.AddSymbol("i", 'V', 0) != -1);
		sTableSuccess |= (sTable.AddSymbol("product", 'V', 0) != -1);
		sTableSuccess |= (sTable.AddSymbol("1", 'C', 1) != -1);
		sTableSuccess |= (sTable.AddSymbol("$temp", 'V', 0) != -1);

		// Initialize instruction memory for factorial
		qTable.AddQuad(opTable.LookupName("MOV"), 3, 0, 2);   // prod = 1
		qTable.AddQuad(opTable.LookupName("MOV"), 3, 0, 1);   // i = 1
		qTable.AddQuad(opTable.LookupName("SUB"), 1, 0, 4);   // check loop condition
		qTable.AddQuad(opTable.LookupName("JP"), 4, 0, 7);    // exit loop if condition not met
		qTable.AddQuad(opTable.LookupName("MUL"), 2, 1, 2);   // Multiply product by i
		qTable.AddQuad(opTable.LookupName("ADD"), 1, 3, 1);   // Increment i
		qTable.AddQuad(opTable.LookupName("JMP"), 0, 0, 2);   // Restart loop
		qTable.AddQuad(opTable.LookupName("PRINT"), 0, 0, 2); // Print final sum
		qTable.AddQuad(opTable.LookupName("STOP"), 0, 0, 0);  // Stop

		return sTableSuccess;
	}

	/**
	 * Hardcodes the data and instruction memory necessary to execute a simple summation program.
	 *
	 * @param sTable The SymbolTable to hold data memory (assumed non-null)
	 * @param qTable The QuadTable to hold instruction memory (assumed non-null)
	 *
	 * @return true if initialization was performed successfully, false otherwise
	 */
	public boolean initializeSummationTest(SymbolTable sTable, QuadTable qTable) {
		boolean sTableSuccess = true;

		// Initialize data memory for factorial
		sTableSuccess |= (sTable.AddSymbol("n", 'V', 10) != -1);
		sTableSuccess |= (sTable.AddSymbol("i", 'V', 0) != -1);
		sTableSuccess |= (sTable.AddSymbol("sum", 'V', 0) != -1);
		sTableSuccess |= (sTable.AddSymbol("1", 'C', 1) != -1);
		sTableSuccess |= (sTable.AddSymbol("0", 'C', 0) != -1);
		sTableSuccess |= (sTable.AddSymbol("$temp", 'V', 0) != -1);

		// Initialize instruction memory for factorial
		qTable.AddQuad(opTable.LookupName("MOV"), 4, 0, 2);   // sum = 0
		qTable.AddQuad(opTable.LookupName("MOV"), 3, 0, 1);   // i = 1
		qTable.AddQuad(opTable.LookupName("SUB"), 1, 0, 5);   // check loop condition
		qTable.AddQuad(opTable.LookupName("JP"), 5, 0, 7);    // exit loop if condition not met
		qTable.AddQuad(opTable.LookupName("ADD"), 2, 1, 2);   // Increment sum by i
		qTable.AddQuad(opTable.LookupName("ADD"), 1, 3, 1);   // Increment i
		qTable.AddQuad(opTable.LookupName("JMP"), 0, 0, 2);   // Restart loop
		qTable.AddQuad(opTable.LookupName("PRINT"), 0, 0, 2); // Print final sum
		qTable.AddQuad(opTable.LookupName("STOP"), 0, 0, 0);  // Stop

		return sTableSuccess;
	}

	public int opcodeFor(String op) { return opTable.LookupName(op); }

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

	/**
	 * Formats the current state of the interpreter as a nice trace string
	 *
	 * @param pc Program counter
	 * @param opcode opCode of the current operation
	 * @param op1 First arg of the current operation
	 * @param op2 Second arg of the current operation
	 * @param op3 Third arg of the current operation
	 *
	 * @return Nicely formatted string representing the current operation being interpreted
	 */
	private String makeTraceString(int pc, int opcode, int op1, int op2, int op3) {
		String result = "";
		result = "PC = " + String.format("%04d", pc) + ": " +
		         StringUtils.PadToLength(opTable.LookupCode(opcode), 6) +
		         String.format("%02d", op1) + ", " + String.format("%02d", op2) + ", " +
		         String.format("%02d", op3);
		return result;
	}

	private ReserveTable opTable;
	private int programCounter;
}
