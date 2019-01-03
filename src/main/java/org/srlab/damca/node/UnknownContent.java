package org.srlab.damca.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;

import java.io.BufferedWriter;
import java.io.IOException;

public class UnknownContent extends ParameterContent{
	private String name;
	private String absStringRep;
	private String typeQualifiedName;
	public UnknownContent(MethodCallExpr mi, MethodDeclaration md, Expression expression,BufferedWriter logbw) throws IOException {
		super(logbw);
		name = expression.toString();
		this.absStringRep ="unknown";
		this.typeQualifiedName = null;
		
		JavaParserFacade jpf = JSSConfigurator.getInstance(logbw).getJpf();
		SymbolReference<? extends ResolvedValueDeclaration>  srResolvedValueDeclaration  = jpf.solve(expression);
		if(srResolvedValueDeclaration.isSolved()) {
			ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedValueDeclaration.getType();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			this.typeQualifiedName = typeDescriptor.getTypeQualifiedName();
		}
	}
	public String getName() {
		return name;
	}
	
	public String getTypeQualifiedName() {
		return typeQualifiedName;
	}
	public void print(){
		System.out.print("Name: "+this.getName()+" TypeQualifiedName: "+this.getTypeQualifiedName()+ " Abstract String Rep: "+this.getAbsStringRep());
	}
}
