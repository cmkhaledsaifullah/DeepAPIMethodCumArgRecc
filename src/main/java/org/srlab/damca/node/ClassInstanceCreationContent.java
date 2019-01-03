package org.srlab.damca.node;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;

import java.io.BufferedWriter;
import java.io.IOException;

public class ClassInstanceCreationContent extends ParameterContent{
	private String name;
	private String scope;
	private String scopeTypeQualifiedName;
	private String typeQualifiedName;
	public ClassInstanceCreationContent(ObjectCreationExpr objectCreationExpression, BufferedWriter logbw) throws IOException {
		super(objectCreationExpression);
		this.typeQualifiedName = null;
		this.name = objectCreationExpression.toString();
		this.absStringRep = this.getStringRep(objectCreationExpression,logbw);
		
		ResolvedType resolvedType = JSSConfigurator.getInstance(logbw).getJpf().getType(objectCreationExpression.getType());
		TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
		this.typeQualifiedName = typeDescriptor.getTypeQualifiedName();
		
		if(objectCreationExpression.getScope().isPresent()) {
			Expression expression = objectCreationExpression.getScope().get();
			JavaParserFacade jpf = JSSConfigurator.getInstance(logbw).getJpf();
			SymbolReference<? extends ResolvedValueDeclaration>  srResolvedValueDeclaration  = jpf.solve(expression);
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				resolvedType = resolvedValueDeclaration.getType();
				typeDescriptor = new TypeDescriptor(resolvedType);
				this.scope = objectCreationExpression.getScope().get().toString();
				this.scopeTypeQualifiedName = typeDescriptor.getTypeQualifiedName();
				this.parent = ParameterContent.get(objectCreationExpression.getScope().get());
			}
		}
		else {
			this.parent = null;
			this.scope = null;
			this.scopeTypeQualifiedName =null;
		}
		this.absStringRep = "new "+ typeQualifiedName+"( )";
	}
	
	public String getName() {
		return name;
	}

	public String getScope() {
		return scope;
	}

	public String getScopeTypeQualifiedName() {
		return scopeTypeQualifiedName;
	}

	public String getTypeQualifiedName() {
		return typeQualifiedName;
	}

	public void print(){
		System.out.print("ClassInstanceCreationContent [name=" + name + ", scope=" + scope + ", scopeTypeQualifiedName="
				+ scopeTypeQualifiedName + ", typeQualifiedName=" + typeQualifiedName + ", absStringRep=" + absStringRep
				+ "]");
	}
}
