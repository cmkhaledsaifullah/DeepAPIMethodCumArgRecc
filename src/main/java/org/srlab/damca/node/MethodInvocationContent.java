package org.srlab.damca.node;

import java.io.BufferedWriter;
import java.io.IOException;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;

public class MethodInvocationContent extends ParameterContent{
	
	private String name;
	private String methodName;
	private String receiver;
	private String receiverTypeQualifiedName;
	private String absStringRep;
	
	public MethodInvocationContent(MethodCallExpr mi, BufferedWriter logbw) throws IOException {
		super(mi);
		this.name = mi.toString();
		this.methodName = mi.getName().getIdentifier();
		this.absStringRep = this.getStringRep(mi,logbw);

		this.receiver = null;
		this.receiverTypeQualifiedName = null;
		
		if(mi.getScope().isPresent()) {
			this.receiver = mi.getScope().toString();
			JavaParserFacade jpf = JSSConfigurator.getInstance(logbw).getJpf();
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(mi.getScope().get());
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				ResolvedType resolvedType = resolvedValueDeclaration.getType();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				this.receiverTypeQualifiedName = typeDescriptor.getTypeQualifiedName();
			}
			this.parent = ParameterContent.get(mi.getScope().get());
		}
	}

	public String getName() {
		return name;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getReceiver() {
		return receiver;
	}

	public String getReceiverTypeQualifiedName() {
		return receiverTypeQualifiedName;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}

	@Override
	public String toString() {
		return "MethodInvocationContent [name=" + name + ", methodName=" + methodName + ", receiver=" + receiver
				+ ", receiverTypeQualifiedName=" + receiverTypeQualifiedName + ", absStringRep=" + absStringRep + "]";
	}
	
	
}
