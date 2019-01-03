package org.srlab.damca.node;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;

import java.io.BufferedWriter;
import java.io.IOException;

public class BooleanLiteralContent extends ParameterContent {
	private String name;

	public BooleanLiteralContent(BooleanLiteralExpr nl, BufferedWriter logbw) throws IOException {
		super(nl);
		name = nl.toString();
		this.absStringRep = this.getStringRep(nl,logbw);
		this.partlyAbsStringRep = nl.toString();
		this.parent = null;
	}

	public String getName() {
		return name;
	}

	public void print() {
		System.out.print("BOOLEAN LITERAL: " + "Name: " + this.getName() + " Abstract Rep: " + this.getAbsStringRep());
	}
}
