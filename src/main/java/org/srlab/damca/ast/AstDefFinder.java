package org.srlab.damca.ast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/*** Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */

import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;
import org.srlab.damca.completioner.MethodCallEntity;
import org.srlab.damca.completioner.MethodDeclarationEntity;

public class AstDefFinder extends VoidVisitorAdapter<Void> {

	//definingMethodCallEntity  is a method call, super method call, or class instance creation that create the method
	private MethodCallEntity definingMethodCallEntity; 
	private DefinitionType definitionType; // definition type
	private List<MethodCallEntity> methodCallEntities; // methods that are called on the receiver variable

	private final String varname; // variable name
	private Position position;	
	private final MethodDeclaration methodDeclaration; // method declaration containing the method call
	private final JavaParserFacade javaParserFacade;

	private BufferedWriter logbw;
	
	public DefinitionType getDefinitionType() {
		return definitionType;
	}

	public AstDefFinder(final String _varname, Position _position, MethodDeclaration _methodDeclaration,
			JavaParserFacade _javaParserFacade, BufferedWriter logbw) {
		this.varname = _varname;
		this.position = _position; // position of the node that triggers this AstdefFinder
		this.javaParserFacade = _javaParserFacade;
		this.methodDeclaration = _methodDeclaration;
		this.methodCallEntities = new LinkedList();

		this.definingMethodCallEntity = null;
		this.definitionType = DefinitionType.UNKNOWN;

		this.logbw = logbw;

		this.methodDeclaration.accept(this, null);
	}

	public MethodCallEntity getDefiningMethodCallEntity() {
		return definingMethodCallEntity;
	}

	public String getVarname() {
		return varname;
	}

	public Position getPosition() {
		return position;
	}

	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public JavaParserFacade getJavaParserFacade() {
		return javaParserFacade;
	}

	public List<MethodCallEntity> getMethodCallEntities() {
		return methodCallEntities;
	}

	private void registerMethodCall(MethodCallEntity _methodCallEntity) {
		this.methodCallEntities.add(_methodCallEntity);
	}

	private void registerMethodCallAndDefinition(DefinitionType _definitionType, MethodCallEntity _methodCallEntity,
			Position newPosition) {
		if (_definitionType == DefinitionType.PARAMETER) {
			this.definitionType = _definitionType;
		} else if (this.definingMethodCallEntity == null && newPosition.isBefore(position)) {
			this.definitionType = _definitionType;
			this.definingMethodCallEntity = _methodCallEntity;
		} else if (this.definingMethodCallEntity != null && newPosition.isBefore(position)) {
			Position curDefiningPosition = this.definingMethodCallEntity.getPosition();
			if (newPosition.isAfter(curDefiningPosition)) {
				this.definingMethodCallEntity = _methodCallEntity;
				this.definitionType = _definitionType;
			}
		}
	}

	private boolean receiverExpressionMatchesVarname(final Expression exp) {
		if (exp == null) {
			return false;
		}

		// both simple name and qualified name are name expressions
		if (exp.isNameExpr()) {
			NameExpr nameExpr = (NameExpr) exp;
			return matchesVarName(nameExpr.getName());
		} else if (exp.isThisExpr()) {
			// do we look for this?
			return isThis();
		} else
			return false;
	}

	private boolean matchesVarName(final SimpleName node) {
		if (node == null) {
			return false;
		}
		final String name = node.asString();
		return varname.equals(name);
	}

	private boolean isThis() {
		return "this".equals(varname) || "".equals(varname);
	}

	private boolean isReceiverThis(final MethodCallExpr mi) {
		// standard case:
		if (mi.getScope().isPresent() == false && !isStatic(mi)) {
			return true;
		}
		// qualified call: this.method()
		if (mi.getScope().isPresent() == true && mi.getScope().get() instanceof ThisExpr) {
			return true;
		}
		return false;
	}

	private boolean isStatic(final MethodCallExpr methodCallExpr) {
		SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = javaParserFacade.solve(methodCallExpr);
		if (resolvedMethodDeclaration.isSolved()) {
			return resolvedMethodDeclaration.getCorrespondingDeclaration().isStatic();
		}
		return false;
	}

	//we are assuming that method call has a receiver type
	public Optional<String> getReceiverTypeQualifiedName(MethodCallExpr methodCallExpr) throws IOException {
		try {
			SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade
					.solve(methodCallExpr);
			TypeDescriptor receiverTypeDescriptor = null;
			if (methodCallExpr.getScope().isPresent()) {
				SymbolReference<? extends ResolvedValueDeclaration> sr = JSSConfigurator.getInstance(logbw).getJpf().solve(methodCallExpr.getScope().get());
				if(sr.isSolved()) {
					ResolvedValueDeclaration resolvedValueDeclaration = sr.getCorrespondingDeclaration();
					receiverTypeDescriptor = new TypeDescriptor(resolvedValueDeclaration.getType());
					return Optional.of(receiverTypeDescriptor.getTypeQualifiedName());
				}
			}
		}catch(Exception e) {
			logbw.write("Error in resolving receiver type: "+methodCallExpr);
            logbw.newLine();
		}
		return Optional.empty();
	}
	
	@Override
	public void visit(AssignExpr assignExpr, Void arg) {
		// TODO Auto-generated method stub
		super.visit(assignExpr, arg);

		final Expression lhs = assignExpr.getTarget();
		if (lhs == null) {
			return;
		}

		if (lhs.isNameExpr()) {
			final SimpleName n = lhs.asNameExpr().getName();
			if (!matchesVarName(n)) {
				return;
			}
			return;
		}
		final Expression rhs = assignExpr.getValue();
		try {
			evaluateRightHandSideExpression(rhs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// called only if left hand side was a match.
	private void evaluateRightHandSideExpression(final Expression expression) throws IOException {
		if (expression.isCastExpr()) {
			// re-evaluate using the next expression:
			evaluateRightHandSideExpression(expression.asCastExpr().getExpression());
		} else if (expression.isMethodCallExpr()) {
			// x = some().method().call()
			SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade
					.solve(expression.asMethodCallExpr());
			TypeDescriptor receiverTypeDescriptor = null;
			if (resolvedMethodDeclaration.isSolved()) {
				MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
				boolean isSuper = false;
				boolean isThis = false;
				if (methodCallExpr.getScope().isPresent()) {
					SymbolReference<? extends ResolvedValueDeclaration> sr = JSSConfigurator.getInstance(logbw).getJpf().solve(methodCallExpr.getScope().get());
					if(sr.isSolved()) {
						ResolvedValueDeclaration resolvedValueDeclaration = sr.getCorrespondingDeclaration();
						receiverTypeDescriptor = new TypeDescriptor(resolvedValueDeclaration.getType());
					}
					if (methodCallExpr.getScope().get() instanceof SuperExpr) {
						isSuper = true;
					} else if (methodCallExpr.getScope().get() instanceof ThisExpr) {
						isThis = true;
					}
				} else {
					isThis = true;
				}

				MethodDeclarationEntity methodDeclarationEntity = null;
				try {
					methodDeclarationEntity = MethodDeclarationEntity
							.get(resolvedMethodDeclaration.getCorrespondingDeclaration(), javaParserFacade,logbw);
					Optional<String> optional = this.getReceiverTypeQualifiedName(methodCallExpr);
					MethodCallEntity methodCallEntity = new MethodCallEntity(optional,isSuper,
							isThis, expression.asMethodCallExpr().getName().getBegin(), methodDeclarationEntity);
					this.registerMethodCallAndDefinition(DefinitionType.METHOD_RETURN, methodCallEntity,
							expression.asMethodCallExpr().getName().getBegin().get());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		} else if (expression.isObjectCreationExpr()) {
			SymbolReference<ResolvedConstructorDeclaration> resolvedConstructorDeclaration = this.javaParserFacade
					.solve(expression.asObjectCreationExpr());
			if (resolvedConstructorDeclaration.isSolved()) {
				MethodDeclarationEntity methodDeclarationEntity = MethodDeclarationEntity
						.get(resolvedConstructorDeclaration.getCorrespondingDeclaration(), javaParserFacade,logbw);
				Optional<String> optional = Optional.empty();
				MethodCallEntity methodCallEntity = new MethodCallEntity(optional, false,
						false, expression.asObjectCreationExpr().getType().getBegin(), methodDeclarationEntity);
				this.registerMethodCallAndDefinition(DefinitionType.NEW, methodCallEntity,
						expression.asObjectCreationExpr().getType().getBegin().get());
			}
		} else if (expression.isNameExpr()) {
			// e.g. int j=anotherValue;
			// some alias thing...
			// it might be that we found an assignment before and this simpleName is just
			// "$missing". Then ignore this
			if (definitionType == null) {
				SimpleName simpleName = (SimpleName) expression.asNameExpr().getName();
				SymbolReference<? extends ResolvedValueDeclaration> resolvedValueDeclaration = this.javaParserFacade
						.solve(expression.asNameExpr());
				if (resolvedValueDeclaration.isSolved()) {
					this.registerMethodCallAndDefinition(DefinitionType.LOCAL, null, expression.getBegin().get());
				}
			}
		}
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		if (n.isThis() && varname.equals("this") || varname.equals("")) {
			SymbolReference<? extends ResolvedConstructorDeclaration> resolvedConstructorDeclaration = this.javaParserFacade
					.solve(n);
			if (resolvedConstructorDeclaration.isSolved()) {
                MethodDeclarationEntity methodDeclarationEntity = null;
                try {
                    methodDeclarationEntity = MethodDeclarationEntity
                            .get(resolvedConstructorDeclaration.getCorrespondingDeclaration(), javaParserFacade,logbw);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Optional<String> optional = Optional.empty();
				MethodCallEntity methodCallEntity = new MethodCallEntity(optional, false,
						true, n.getBegin(), methodDeclarationEntity);
				this.registerMethodCall(methodCallEntity);
			}
		} else if (varname.equals("super")) {
			SymbolReference<? extends ResolvedConstructorDeclaration> resolvedConstructorDeclaration = this.javaParserFacade
					.solve(n);
			if (resolvedConstructorDeclaration.isSolved()) {
                MethodDeclarationEntity methodDeclarationEntity = null;
                try {
                    methodDeclarationEntity = MethodDeclarationEntity
                            .get(resolvedConstructorDeclaration.getCorrespondingDeclaration(), javaParserFacade,logbw);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Optional<String> optional = Optional.empty();
				MethodCallEntity methodCallEntity = new MethodCallEntity(optional, true,
						false, n.getBegin(), methodDeclarationEntity);
				this.registerMethodCall(methodCallEntity);
			}
		}
	}

	@Override
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);
		boolean isSuper = false;
		boolean isThis = false;

		if (m.getScope().isPresent() && m.getScope().get() instanceof NameExpr) {
			NameExpr nameExpr = m.getScope().get().asNameExpr();
			if (this.matchesVarName(nameExpr.getName())) {
				//definitionType = DefinitionType.METHOD_RETURN;
				SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade.solve(m);
				if (m.getScope().get() instanceof SuperExpr) {
					isSuper = true;
				} else if (m.getScope().get() instanceof ThisExpr) {
					isThis = true;
				} else {
					isThis = true;
				}
				if (resolvedMethodDeclaration.isSolved()) {
					MethodDeclarationEntity methodDeclarationEntity;
					try {
						methodDeclarationEntity = MethodDeclarationEntity
								.get(resolvedMethodDeclaration.getCorrespondingDeclaration(), javaParserFacade,logbw);
						Optional<String> optional = this.getReceiverTypeQualifiedName(m);
						MethodCallEntity methodCallEntity = new MethodCallEntity(optional,
								isSuper, isThis, m.getName().getBegin(), methodDeclarationEntity);
						this.registerMethodCall(methodCallEntity);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else if (m.getScope().isPresent() && m.getScope().get() instanceof SuperExpr && varname.equals("super")) {
			//definitionType = DefinitionType.METHOD_RETURN;
			if (m.getScope().get() instanceof SuperExpr) {
				isSuper = true;
			} else if (m.getScope().get() instanceof ThisExpr) {
				isThis = true;
			} else {
				isThis = true;
			}
			SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade.solve(m);
			if (resolvedMethodDeclaration.isSolved()) {
				MethodDeclarationEntity methodDeclarationEntity;
				try {
					methodDeclarationEntity = MethodDeclarationEntity
							.get(resolvedMethodDeclaration.getCorrespondingDeclaration(), javaParserFacade,logbw);
					Optional<String> optional = Optional.empty();
					MethodCallEntity methodCallEntity = new MethodCallEntity(optional, isSuper,
							isThis, m.getName().getBegin(), methodDeclarationEntity);
					this.registerMethodCall(methodCallEntity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		} else if (isThis()) {
			//definitionType = DefinitionType.METHOD_RETURN;
			SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade.solve(m);
			if (resolvedMethodDeclaration.isSolved()) {
				MethodDeclarationEntity methodDeclarationEntity;
				try {
					methodDeclarationEntity = MethodDeclarationEntity
							.get(resolvedMethodDeclaration.getCorrespondingDeclaration(), javaParserFacade,logbw);
					Optional<String> optional = Optional.empty();
					MethodCallEntity methodCallEntity = new MethodCallEntity(optional, true,
							false, m.getName().getBegin(), methodDeclarationEntity);
					this.registerMethodCall(methodCallEntity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

	@Override
	public void visit(Parameter n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		if (this.matchesVarName(n.getName())) {
			try {
				logbw.write("Its a parameter: for " + varname);
				logbw.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.definitionType = DefinitionType.PARAMETER;
		}
	}

	private void evaluateVariableDeclarationFragment(final VariableDeclarator f) throws IOException {

		final SimpleName name = f.getName();
		if (matchesVarName(name) && f.getInitializer().isPresent()) {
			final Expression expression = f.getInitializer().get();
			if (expression != null) {
				evaluateRightHandSideExpression(expression);
			}
		}
	}

	public void visit(VariableDeclarationExpr node, Void arg) {
		for (VariableDeclarator v : node.getVariables()) {
			try {
				evaluateVariableDeclarationFragment(v);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void print() throws IOException {
		logbw.write("Variable Name: " + this.varname + "Position: "+this.getPosition());
		logbw.newLine();
		logbw.write("Defining Method Call Entity: "+definingMethodCallEntity);
		logbw.newLine();
		logbw.write("Definition Type: "+this.definitionType);
		logbw.newLine();
		logbw.write("Method Called On Receiver Variables:" );
		logbw.newLine();
		for(MethodCallEntity mce:this.methodCallEntities) {
			logbw.write(mce.toString());
			logbw.newLine();
		}
	}
	/**
	 * Specifies how the variable under examination was defined (field, parameter,
	 * by method return...).
	 */
	public enum DefinitionType {
		/**
		 * indicates that the variable was defined by a method return value, e.g, int x
		 * = p.getX();
		 */
		METHOD_RETURN,
		/**
		 * indicates that the variable was defined by a constructor call, e.g, Point p =
		 * new Point(x,y);
		 */
		NEW,
		/**
		 * indicates that the variable was defined by a field declaration
		 */
		FIELD,
		/**
		 * indicates that the variable was declared as a parameter of the enclosing
		 * method.
		 */
		PARAMETER,
		/**
		 * indicates that the variable represents "this"
		 */
		THIS,
		/**
		 * indicates that the variable was defined as a local variable. Usually this
		 * value gets replaced by more specific values {@link #NEW}, or
		 * {@link #METHOD_RETURN} if possible.
		 */
		LOCAL,
		/**
		 * indicates that the variable was defined in an unexpected or unsupported and
		 * yet unhandled way.
		 */
		UNKNOWN,
	}

    }
