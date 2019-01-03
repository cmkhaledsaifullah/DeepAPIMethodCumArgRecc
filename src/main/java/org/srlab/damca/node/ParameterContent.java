package org.srlab.damca.node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;
import org.srlab.damca.category.ParameterExpressionCategorizer;

public class ParameterContent implements Serializable{
	protected String stringParamNode;
	protected ParameterContent parent;
	private String parameterExpressionType;
	protected String absStringRep;
	protected String partlyAbsStringRep;
	private static BufferedWriter logbw;

	public ParameterContent(BufferedWriter logbw) {
        this.logbw = logbw;
	}

	public String getStringParamNode() {
		return stringParamNode;
	}

	public void print() throws IOException {
		//System.out.println("String Param Node: " + this.stringParamNode);
		logbw.write("String Param Node: " + this.stringParamNode);
		logbw.newLine();
	}
	
	public String getAbsStringRep() {
		return absStringRep;
	}

	public ParameterContent getParent() {
		return parent;
	}

	public String getParameterExpressionType() {
		return parameterExpressionType;
	}

	public String getPartlyAbsStringRep() {
		return partlyAbsStringRep;
	}

	public ParameterContent(Expression expression) {
		this.parameterExpressionType = ParameterExpressionCategorizer.getParameterExpressionType(expression);
	}
	public static ParameterContent get(Expression expression) throws IOException {
		if (expression instanceof StringLiteralExpr) {
			return new StringLiteralContent((StringLiteralExpr)expression,logbw);
		} else if (expression instanceof NullLiteralExpr) {
			return new NullLiteralContent((NullLiteralExpr)expression,logbw);
		} else if (expression instanceof BooleanLiteralExpr) {
			return new BooleanLiteralContent((BooleanLiteralExpr)expression,logbw);
		} 
		else if(expression instanceof DoubleLiteralExpr) {
			return new NumberLiteralContent((DoubleLiteralExpr)expression,logbw);
		}
		else if(expression instanceof LongLiteralExpr) {
			return new NumberLiteralContent((LongLiteralExpr)expression,logbw);
		}
		
		else if(expression instanceof IntegerLiteralExpr) {
			return new NumberLiteralContent((IntegerLiteralExpr)expression,logbw);
		}
		else if (expression instanceof CharLiteralExpr) {
			return new CharLiteralContent((CharLiteralExpr)expression,logbw);
		} 
		else if(expression instanceof NameExpr) {
			return new NameExprContent((NameExpr)expression,logbw);
		}
		else if(expression instanceof FieldAccessExpr) {
			return new QualifiedNameContent((FieldAccessExpr)expression,logbw);
		}
		else if(expression instanceof ObjectCreationExpr) {
			return new ClassInstanceCreationContent((ObjectCreationExpr)expression,logbw);
		}
		else if(expression instanceof CastExpr) {
			return new CastExpressionContent((CastExpr)expression,logbw);
		}
		else if(expression instanceof MethodCallExpr) {
			return new MethodInvocationContent((MethodCallExpr)expression,logbw);
		}
		else if(expression instanceof ThisExpr) {
			return new ThisExpressionContent((ThisExpr)expression,logbw);
		}else {
			throw new RuntimeException("Unknown Expression Exception Type");
		}
	}

	//The problem with Java symbol solver is that if the NameExpr indicates a type name, such as BufferedReder, it can solve the type of BufferedReader
	public String getStringRep(Expression expression,BufferedWriter logbw) throws IOException {
		if (expression instanceof StringLiteralExpr) {
			return "String";
		} else if (expression instanceof NullLiteralExpr) {
			return "NULL";
		} else if (expression instanceof BooleanLiteralExpr) {
			return "Boolean";
		} 
		else if(expression instanceof DoubleLiteralExpr) {
			return "Number";
		}
		else if(expression instanceof LongLiteralExpr) {
			return "Number";	
		}
		
		else if(expression instanceof IntegerLiteralExpr) {
			return "Number";	
		}
		else if(expression instanceof NameExpr) {
			SimpleName sn = expression.asNameExpr().getName(); 
			JavaParserFacade jpf = JSSConfigurator.getInstance(logbw).getJpf();
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(sn);
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				ResolvedType resolvedType = resolvedValueDeclaration.getType();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				return "SN"+":"+typeDescriptor.getTypeQualifiedName();
			}
			else if(Character.isUpperCase(sn.getIdentifier().charAt(0))){
				return sn.getIdentifier();
			}else {
				throw new RuntimeException("Error in parsing NameExpr: "+expression);
			}
		}
		else if(expression instanceof CastExpr) {
			CastExpr ce = (CastExpr)expression;
			ResolvedType resolvedType = JSSConfigurator.getInstance(logbw).getJpf().getType(ce.getType());
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			return "("+typeDescriptor.getTypeQualifiedName()+")"+this.getStringRep(ce.getExpression(),logbw);
		}
		else if(expression instanceof ObjectCreationExpr) {
			ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr)expression;
			if(objectCreationExpr.getScope().isPresent()==false) {
				ResolvedType resolvedType = JSSConfigurator.getInstance(logbw).getJpf().getType(objectCreationExpr.getType());
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				return "new_"+typeDescriptor.getTypeQualifiedName()+"("+")";
			}
			else {
				ResolvedType resolvedType = JSSConfigurator.getInstance(logbw).getJpf().getType(objectCreationExpr.getType());
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				return "new_"+this.getStringRep(objectCreationExpr.getScope().get(),logbw)+"."+typeDescriptor.getTypeQualifiedName()+"("+")";
			}
		}
		else if(expression instanceof MethodCallExpr) {
			MethodCallExpr methodCallExpr = (MethodCallExpr)expression;
			if(((MethodCallExpr) expression).getScope().isPresent()) {
				return this.getStringRep(methodCallExpr.getScope().get(),logbw)+"."+methodCallExpr.getName()+"("+")";
			}
			else {
				return methodCallExpr.getName()+"("+")";
			}
		}
		else if(expression instanceof FieldAccessExpr) {
			
			FieldAccessExpr fieldAccessExpr = (FieldAccessExpr)expression;
			SimpleName simpleName = fieldAccessExpr.getName();
			JavaParserFacade jpf = JSSConfigurator.getInstance(logbw).getJpf();
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = jpf
					.solve(fieldAccessExpr.getName());
			ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedValueDeclaration.getType();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
            logbw.write("FieldAccessExpr in ParameterContent: "+fieldAccessExpr.getScope());
            logbw.newLine();
			if(Character.isUpperCase(fieldAccessExpr.getScope().toString().charAt(0))&& Character.isUpperCase(fieldAccessExpr.getName().getIdentifier().charAt(0))) {
				return fieldAccessExpr.getScope().toString()+"."+fieldAccessExpr.getName().getIdentifier();
			}
			else if (Character.isUpperCase(fieldAccessExpr.getScope().toString().charAt(0))){
				return fieldAccessExpr.getScope().toString()+"."+"SN:"+typeDescriptor.getTypeQualifiedName();
			}
			else return this.getStringRep(fieldAccessExpr.getScope(),logbw)+"."+"SN:"+typeDescriptor.getTypeQualifiedName();
		}
		else if(expression instanceof CharLiteralExpr) {
			return "Char";
		} else if (expression instanceof ThisExpr) {
			ThisExpr thisExpr = (ThisExpr) expression;
			// TODO Auto-generated method stub
			if (thisExpr.getClassExpr().isPresent()) {
				if (thisExpr.getClassExpr().get() instanceof ClassExpr) { // Example: Worls.this
					return thisExpr.getClassExpr().get().toString() + "." + "this";
				} else if (thisExpr.getClassExpr().get() instanceof NameExpr) { // Example: m.this;
					NameExpr nameExpr = (NameExpr) thisExpr.getClassExpr().get();
					JavaParserFacade jpf = JSSConfigurator.getInstance(logbw).getJpf();
					SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = jpf
							.solve(nameExpr);
					if (srResolvedValueDeclaration.isSolved()) {
						ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration
								.getCorrespondingDeclaration();
						ResolvedType resolvedType = resolvedValueDeclaration.getType();
						TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
						return "SN:" + typeDescriptor.getTypeQualifiedName() + "." + "this";
					} else
						return null;
				} else if (thisExpr.getClassExpr().get() instanceof FieldAccessExpr) { // a.b.this
					FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) thisExpr.getClassExpr().get();
					return this.getStringRep(fieldAccessExpr,logbw)+"."+"this";
				} else
					return null;
			}

			return thisExpr.toString();
		}
		else return null;
	}

	/*public  String getStringRep(Node node) {

		if (expression instanceof SimpleName &&((SimpleName) expression).resolveTypeBinding()!=null)
			return ((SimpleName) expression).resolveTypeBinding()
					.getQualifiedName();
		else if (expression instanceof QualifiedName) {
			return ((QualifiedName) expression).toString();
		} else if (expression instanceof NumberLiteral
				|| expression instanceof StringLiteral
				|| expression instanceof NullLiteral
				|| expression instanceof ThisExpression) {
			return expression.toString();
		} else if (expression instanceof ClassInstanceCreation) {
			ClassInstanceCreation ci = ((ClassInstanceCreation) expression);
			return "new " + ci.getType().toString();
		} else if (expression instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) expression;

			if (mi.getExpression() != null && mi.getExpression() instanceof SimpleName) {
				SimpleName sn = (SimpleName) mi.getExpression();

				if (sn.resolveBinding() != null && sn.resolveBinding().getKind() == IBinding.TYPE) {
					return sn.resolveTypeBinding().getQualifiedName() + "."
							+ mi.getName().getFullyQualifiedName();

				} else if (sn.resolveBinding() != null && sn.resolveBinding().getKind() == IBinding.METHOD) {
				} else if (sn.resolveTypeBinding() != null
						&& sn.resolveBinding() != null
						&& sn.resolveBinding().getKind() == IBinding.VARIABLE) {
					return (sn.resolveTypeBinding().getQualifiedName() + "." + mi
							.getName());
				}

			} else if (mi.getExpression() != null && mi.getExpression() instanceof MethodInvocation) {
				return "" + this.getStringRep(mi.getExpression()) + "."
						+ mi.getName();
			}

			return mi.toString();
		} else if (expression instanceof CastExpression) {
			CastExpression cast = (CastExpression) expression;
			return cast.getType().toString()
					+ "."
					+ this.getStringRep(((CastExpression) expression)
							.getExpression());
		} else {
			return expression.toString();
		}
	}*/
}
