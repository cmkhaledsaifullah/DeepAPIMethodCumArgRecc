package org.srlab.damca.node;


import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.io.BufferedWriter;
import java.io.IOException;

public class StringLiteralContent extends ParameterContent{
	
	private String name;
	public StringLiteralContent(StringLiteralExpr sl,BufferedWriter logbw) throws IOException {
		super(sl);
		name = sl.toString();
		absStringRep = this.getStringRep(sl,logbw);
		this.partlyAbsStringRep = sl.toString();
		this.parent = null;
	}
	public String getName() {
		return name;
	}
	public String getAbsStringRep() {
		return absStringRep;
	}
	public void print(){
		System.out.print("STRING LITERAL: Name: "+this.getName()+" Abstract String Rep: "+this.getAbsStringRep());
	}
}

