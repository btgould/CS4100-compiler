package com.bgould.compiler;

import com.bgould.compiler.ADT.Syntactic;

public class Main {
	public static void main(String[] args) {
		String filePath = args[0];
		boolean traceon = true;
		System.out.println("Brendan Gould, 4267, CS4100, SPRING 2024");
		System.out.println("INPUT FILE TO PROCESS IS: " + filePath);

		Syntactic parser = new Syntactic(filePath, traceon);
		parser.parse();

		System.out.println("Done.");
	}
}
