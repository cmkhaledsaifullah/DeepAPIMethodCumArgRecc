package org.srlab.damca.simplename;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;
import org.srlab.damca.completioner.SourcePosition;

/*
 * Given a method declaration and a source position, this class collect all the variables declared or used prior to the 
 * specified position. This include variables in the method declaration, parameter, field and inherited field variables
 */
public class SimpleNameCollector extends VoidVisitorAdapter<Void> {

	private SourcePosition origin; // we need to collect variables that appears before this location
	private MethodDeclaration methodDeclaration;

	private List<VariableEntity> localVariableDeclarationEntities;
	private List<VariableEntity> localVariableDeclarationOrAssignedEntities;
	
	private List<VariableEntity> fieldVariableEntities;
	private List<VariableEntity> inheritedVariableEntities;
	private List<VariableEntity> parameterVariableEntities;
	private List<VariableEntity> usedVariableEntities;
	private BufferedWriter logbw;

	public SimpleNameCollector(MethodDeclaration _methodDeclaration, SourcePosition _sourcePosition, BufferedWriter logbw) {
		this.methodDeclaration = _methodDeclaration;
		this.origin = _sourcePosition;
		this.localVariableDeclarationEntities = new LinkedList();
		this.localVariableDeclarationOrAssignedEntities = new LinkedList();
		this.fieldVariableEntities = new LinkedList();
		this.inheritedVariableEntities = new LinkedList();
		this.parameterVariableEntities = new LinkedList();
		this.usedVariableEntities = new LinkedList();
		this.logbw = logbw;
	}

	public void run() throws IOException {
		this.collectFieldVariables();
		this.collectParameters();
		this.collectLocalVariables();

		TypeDeclaration td = this.getTypeDeclaration(this.methodDeclaration);
		if (td instanceof ClassOrInterfaceDeclaration) {
			ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) td;
			ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration = JSSConfigurator.getInstance(logbw).getJpf()
					.getTypeDeclaration(classOrInterfaceDeclaration);
			this.collectInheritedVariables(resolvedReferenceTypeDeclaration, 0);
		}
	}

	public void collectParameters() throws IOException {
		for (Parameter parameter : this.methodDeclaration.getParameters()) {
			ResolvedType resolvedType = JSSConfigurator.getInstance(logbw).getJpf().convertToUsage(parameter.getType());
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);

			SourcePosition sourcePosition = null;
			if (parameter.getBegin().isPresent() && parameter.getBegin().get().isBefore(origin)) {
				sourcePosition = new SourcePosition(parameter.getBegin().get());
				VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),
						parameter.getName().getIdentifier(), VariableEntityCategory.PARAMETER,
						VariableLocationCategory.PARAMETER_DECLARATION, sourcePosition);
				this.parameterVariableEntities.add(variableEntity);
				System.out.println(variableEntity);
			}
		}
	}

	private TypeDeclaration getTypeDeclaration(MethodDeclaration md) {
		Node parent = this.methodDeclaration;
		while (parent != null && !(parent instanceof TypeDeclaration)) {
			parent = parent.getParentNode().get();
		}
		if (parent instanceof TypeDeclaration) {
			return (TypeDeclaration) parent;
		} else
			return null;
	}

	public void collectFieldVariables() throws IOException {

		// Step-1: from method declaration collect Type Declaration
		TypeDeclaration td = this.getTypeDeclaration(this.methodDeclaration);
		// Step-2: collect field variables
		if (td != null) {

			List<BodyDeclaration> bdList = td.getMembers();
			for (BodyDeclaration bd : bdList) {
				if (bd instanceof FieldDeclaration) {
					FieldDeclaration fd = (FieldDeclaration) bd;
					for (VariableDeclarator vd : fd.getVariables()) {

						String identifier = vd.getName().getIdentifier();
						SourcePosition sourcePosition = null;
						ResolvedType resolvedType = JSSConfigurator.getInstance(logbw).getJpf().convertToUsage(vd.getType());
						TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);

						if (vd.getBegin().isPresent()) {
							sourcePosition = new SourcePosition(vd.getBegin().get());
						}
						VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),
								identifier, VariableEntityCategory.FIELD, VariableLocationCategory.FIELD_DECLARATION,
								sourcePosition);
						fieldVariableEntities.add(variableEntity);
						System.out.println(variableEntity);
					}
				}
			}
		}
	}

	public void collectLocalVariables() {
		// collect variables that appear in the method body
		this.methodDeclaration.accept(this, null);
		
		Collections.reverse(this.localVariableDeclarationEntities);
		Collections.reverse(this.localVariableDeclarationOrAssignedEntities);
	}

	public void collectInheritedVariables(ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration,
			int inheritanceDepth) {
		for (ResolvedReferenceType rrt : resolvedReferenceTypeDeclaration.getAncestors()) {
			ResolvedReferenceTypeDeclaration rrtd = rrt.getTypeDeclaration();
			if (rrtd.isClass()) {

				// Step-1: collect field variable entites
				ArrayList<VariableEntity> classFieldVariableEntities = new ArrayList();
				for (ResolvedFieldDeclaration rfd : rrtd.getDeclaredFields()) {
					if (rfd.accessSpecifier() == AccessSpecifier.PUBLIC
							|| rfd.accessSpecifier() == AccessSpecifier.PROTECTED) {
						TypeDescriptor typeDescriptor = new TypeDescriptor(rfd.getType());
						VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),
								rfd.getName(), VariableEntityCategory.INHERITED_FIELD,
								VariableLocationCategory.FIELD_DECLARATION, null);
						variableEntity.setInheritanceDepth(inheritanceDepth);
						classFieldVariableEntities.add(variableEntity);
					}
				}

				// Step-2: add field variable entities
				if (classFieldVariableEntities.size() > 0) {
					this.inheritedVariableEntities.addAll(classFieldVariableEntities);
				}

				// Step-3: collect parent class inherited field variables
				this.collectInheritedVariables(rrtd, (inheritanceDepth + 1));
			}
		}
	}

	@Override
	public void visit(VariableDeclarationExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
	}

	@Override
	public void visit(VariableDeclarator variableDeclarator, Void arg) {
		// TODO Auto-generated method stub
		super.visit(variableDeclarator, arg);
		String identifier = variableDeclarator.getName().getIdentifier();

		SourcePosition sourcePosition = null;
        ResolvedType resolvedType = null;
        try {
            resolvedType = JSSConfigurator.getInstance(logbw).getJpf().convertToUsage(variableDeclarator.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);

		if (variableDeclarator.getBegin().isPresent() && variableDeclarator.getBegin().get().isBefore(origin)) {
			sourcePosition = new SourcePosition(variableDeclarator.getBegin().get());
			VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(), identifier,
					VariableEntityCategory.LOCAL, VariableLocationCategory.LOCAL_DECLARATION, sourcePosition);
			this.localVariableDeclarationEntities.add(variableEntity);
			this.localVariableDeclarationOrAssignedEntities.add(variableEntity);
			System.out.println(variableEntity);
		}
	}

	@Override
	public void visit(NameExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);

		SourcePosition sourcePosition = null;
		SimpleName simpleName = n.getName();
		if (simpleName.getBegin().isPresent() && simpleName.getBegin().get().isBefore(origin)) {
			sourcePosition = new SourcePosition(simpleName.getBegin().get());
            SymbolReference<? extends ResolvedValueDeclaration> srResolvedvalueDeclaration = null;
            try {
                srResolvedvalueDeclaration = JSSConfigurator
                        .getInstance(logbw).getJpf().solve(simpleName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (srResolvedvalueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedvalueDeclaration
						.getCorrespondingDeclaration();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedValueDeclaration.getType());

				if (n.getParentNode().isPresent() && n.getParentNode().get() instanceof AssignExpr
						&& ((AssignExpr) n.getParentNode().get()).getTarget() == n) {

					VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),
							simpleName.getIdentifier(), VariableEntityCategory.UNKNOWN,
							VariableLocationCategory.ASSIGNMENT, sourcePosition);
					this.localVariableDeclarationOrAssignedEntities.add(variableEntity);
					System.out.println(variableEntity);
				} else {
					VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),
							simpleName.getIdentifier(), VariableEntityCategory.UNKNOWN,
							VariableLocationCategory.NAME_EXPR, sourcePosition);
					this.usedVariableEntities.add(variableEntity);
					System.out.println(variableEntity);
				}
			}
		}
	}

	@Override
	public void visit(AssignExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	public SourcePosition getOrigin() {
		return origin;
	}

	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public List<VariableEntity> getUsedVariableEntities() {
		return usedVariableEntities;
	}

	public List<VariableEntity> getLocalVariableDeclarationEntities() {
		return localVariableDeclarationEntities;
	}

	public List<VariableEntity> getLocalVariableDeclarationOrAssignedEntities() {
		return localVariableDeclarationOrAssignedEntities;
	}

	public List<VariableEntity> getFieldVariableEntities() {
		return fieldVariableEntities;
	}

	public List<VariableEntity> getInheritedVariableEntities() {
		return inheritedVariableEntities;
	}

	public List<VariableEntity> getParameterVariableEntities() {
		return parameterVariableEntities;
	}
}