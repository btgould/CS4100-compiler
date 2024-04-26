package com.bgould.compiler;

import com.bgould.compiler.ADT.Syntactic;

public class Main {
	public static void main(String[] args) {
		String filePath = args[0];
		System.out.println("Code Generation SP2024, by Brendan Gould");
		System.out.println("Parsing " + filePath);
		boolean traceon = true; // false;
		Syntactic parser = new Syntactic(filePath, traceon);
		parser.parse();

		System.out.println("Done.");
	}
}
