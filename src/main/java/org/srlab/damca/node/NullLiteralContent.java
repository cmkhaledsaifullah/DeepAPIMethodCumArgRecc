package org.srlab.damca.node;


import com.github.javaparser.ast.expr.NullLiteralExpr;

import java.io.BufferedWriter;
import java.io.IOException;

public class NullLiteralContent extends ParameterContent{
	private String name;
	public NullLiteralContent(NullLiteralExpr nl, BufferedWriter logbw) throws IOException {
		super(nl);
		name = nl.toString();
		this.absStringRep = this.getStringRep(nl,logbw);
		this.partlyAbsStringRep = nl.toString();
		this.parent = null;
	}
	public String getName() {
		return name;
	}
	
	public String getAbsStringRep() {
		return absStringRep;
	}
	public void print(){
		System.out.print("NULL LITERAL: Name: "+this.getName()+" Abstract String Rep: "+this.getAbsStringRep());
	}
}
