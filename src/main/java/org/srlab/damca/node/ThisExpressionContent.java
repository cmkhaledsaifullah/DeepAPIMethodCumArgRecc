package org.srlab.damca.node;

import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;

import java.io.BufferedWriter;
import java.io.IOException;

public class ThisExpressionContent extends ParameterContent{
	private String name;
	private String classQualifier;
	private String classQualifiedName;
	private String thisQualifiedName;
	private String absStringRep;
	public ThisExpressionContent(ThisExpr thisExpr,BufferedWriter logbw) throws IOException {
		super(thisExpr);
		JavaParserFacade jpf = JSSConfigurator.getInstance(logbw).getJpf();
		name = thisExpr.toString();
		if(thisExpr.getClassExpr().isPresent()) {
			this.classQualifier = thisExpr.getClassExpr().get().toString();
			
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(thisExpr.getClassExpr().get());
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				ResolvedType resolvedType = resolvedValueDeclaration.getType();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				this.classQualifiedName = typeDescriptor.getTypeQualifiedName();
			}
			this.parent = get(thisExpr.getClassExpr().get());
		}
		else {
			this.classQualifier = null;
			this.classQualifiedName = null;
			this.parent = null;
		}
		
		SymbolReference<? extends ResolvedTypeDeclaration> srResolvedTypeDeclaration  = jpf.solve(thisExpr);
		if(srResolvedTypeDeclaration.isSolved()) {
			ResolvedTypeDeclaration resolvedTypeDeclaration = srResolvedTypeDeclaration.getCorrespondingDeclaration();
			this.thisQualifiedName = resolvedTypeDeclaration.getQualifiedName();
		}
	
		if(this.classQualifier!=null)
			this.absStringRep = this.classQualifier+"."+"this";
		else {
			this.absStringRep = "this";		
		}
	}
	
	public String getName() {
		return name;
	}
	public String getAbsStringRep() {
		return absStringRep;
	}
	
	public String getClassQualifier() {
		return classQualifier;
	}
	public String getClassQualifiedName() {
		return classQualifiedName;
	}
	public String getThisQualifiedName() {
		return thisQualifiedName;
	}
	public void print(){
		System.out.print("THIS Name: "+this.getName()+" ClassQualifier: "+this.getClassQualifier()+" ClassQN: "+this.getClassQualifiedName()+" AbsStrRep: "+this.getAbsStringRep());
	}
}
