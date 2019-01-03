package org.srlab.damca.node;

import com.github.javaparser.ast.expr.CharLiteralExpr;

import java.io.BufferedWriter;
import java.io.IOException;

public class CharLiteralContent extends ParameterContent {
	private String name;

	public CharLiteralContent(CharLiteralExpr cl, BufferedWriter logbw) throws IOException {
		super(cl);
		name = cl.toString();
		this.absStringRep = this.getStringRep(cl, logbw);
		this.partlyAbsStringRep = cl.toString();
		this.parent = null;
	}

	public String getName() {
		return name;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}

	public void print() {
		System.out.print("Name: " + this.getName() + " Abstract String Rep: " + this.getAbsStringRep());
	}
}
