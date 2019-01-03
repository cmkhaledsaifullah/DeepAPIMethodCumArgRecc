package org.srlab.damca.node;

import java.io.BufferedWriter;
import java.io.IOException;

import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;

public class QualifiedNameContent extends ParameterContent {

	private String name;
	private String identifier;
	private String typeQualifiedName;
	private String scope;
	private String scopeTypeQualifiedName;
	
	public QualifiedNameContent(FieldAccessExpr fieldAccessExpr,BufferedWriter logbw) throws IOException {
		super(fieldAccessExpr);
		this.name = fieldAccessExpr.toString();
		this.scope = null;
		this.scopeTypeQualifiedName = null;
		logbw.write("FieldAccessScope: "+fieldAccessExpr.getScope()+" "+(fieldAccessExpr.getScope() instanceof ClassExpr));
		logbw.newLine();
		
		this.identifier = fieldAccessExpr.getName().getIdentifier();
		SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = JSSConfigurator.getInstance(logbw).getJpf().solve(fieldAccessExpr.getName());
		ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
		ResolvedType resolvedType = resolvedValueDeclaration.getType();
		TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
		this.typeQualifiedName = typeDescriptor.getTypeQualifiedName();
		this.absStringRep = this.getStringRep(fieldAccessExpr,logbw);
        logbw.write("Abstract String Rep in QualifiedNameContent: "+fieldAccessExpr.getScope());
        logbw.newLine();
		
		JavaParserFacade jpf = JSSConfigurator.getInstance(logbw).getJpf();
		srResolvedValueDeclaration = jpf.solve(fieldAccessExpr.getScope().asNameExpr());
		if (srResolvedValueDeclaration.isSolved()) {
			resolvedValueDeclaration = srResolvedValueDeclaration
					.getCorrespondingDeclaration();
			resolvedType = resolvedValueDeclaration.getType();
			typeDescriptor = new TypeDescriptor(resolvedType);
			this.scope = fieldAccessExpr.getScope().toString();
			this.scopeTypeQualifiedName = typeDescriptor.getTypeQualifiedName();
            logbw.write("TypeDescriptor is called...");
            logbw.newLine();
			this.parent = get(fieldAccessExpr.getScope());
		}else {
			this.scopeTypeQualifiedName = fieldAccessExpr.getScope().toString();
            logbw.write("FieldAccessScope: ::::"+"TypeExpr: "+(fieldAccessExpr.getScope() instanceof TypeExpr));
            logbw.newLine();
            logbw.write("FieldAccessScope: ::::"+"NameExpr: "+(fieldAccessExpr.getScope() instanceof NameExpr));
            logbw.newLine();
            logbw.write("FieldAccessScope: ::::"+"ClassExpr: "+(fieldAccessExpr.getScope() instanceof ClassExpr));
            logbw.newLine();
			NameExpr nameExpr = fieldAccessExpr.getScope().asNameExpr();
		}
	}

	public String getName() {
		return name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getTypeQualifiedName() {
		return typeQualifiedName;
	}

	public String getScope() {
		return scope;
	}

	public String getScopeTypeQualifiedName() {
		return scopeTypeQualifiedName;
	}
	public void print() {
		System.out.println("QualifiedNameContent [name=" + name + ", identifier=" + identifier + ", typeQualifiedName="
				+ typeQualifiedName + ", scope=" + scope + ", scopeTypeQualifiedName=" + scopeTypeQualifiedName
				+ ", absStringRep=" + absStringRep + "]");
	}
}
