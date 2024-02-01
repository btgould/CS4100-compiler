package com.bgould.compiler.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Class for utility functions related to Strings and printing
 */
public class StringUtils {
	/**
	 * Prints to the named file with the required error catching
	 *
	 * @param filename The file to print to
	 * @param toWrite The String to write to the file
	 */
	public static void PrintToFile(String filename, String toWrite) {
		try {
			FileOutputStream outputStream = new FileOutputStream(filename);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

			bufferedWriter.write(toWrite);
			bufferedWriter.newLine();

			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Appends spaces as necessary to make the given input the desired length. If
	 * the input is longer than the desired length, no changes are made (i.e. this
	 * will never truncate a String)
	 *
	 * @param input The string to pad
	 * @param length The length to pad to
	 * @return A String starting with the given input and containing as many
	 *     trailing spaces as necessary to reach the given length
	 */
	public static String PadToLength(String input, int length) {
		int padLen = length - input.length();
		padLen = Math.max(padLen, 0);

		return input + " ".repeat(padLen);
	}
}
