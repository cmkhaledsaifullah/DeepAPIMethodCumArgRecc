package org.srlab.damca.completioner;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.srlab.damca.binding.TypeDescriptor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MethodDeclarationEntity implements Serializable {

	private String name;
	private String qualifiedName;
	private List<ParameterEntity> parameterList;
	private TypeDescriptor returnType;
	private AccessSpecifier accessSpecifier;
	private boolean isConstructor;
	private boolean isAbstract;
	private boolean isStatic;
	private boolean isDefault;
	
	//a method can have a return type or not
	public MethodDeclarationEntity(boolean _isConstructor, boolean _isAbstract, boolean _isStatic, boolean _isDefault, String _name, String _qualifiedName, ArrayList<ParameterEntity> parameterList, Optional<TypeDescriptor> returnType, Optional<AccessSpecifier> _accessSpecifier) {
		super();
		this.isConstructor = _isConstructor;
		this.isAbstract = _isAbstract;
		this.isStatic = _isStatic;
		this.isDefault = _isDefault;
		this.name = _name;
		this.qualifiedName = _qualifiedName;
		this.parameterList = parameterList;
		this.returnType = (returnType.isPresent()?returnType.get():null);
		this.accessSpecifier = (_accessSpecifier.isPresent()?_accessSpecifier.get():null);
	}

	public boolean isConstructor() {
		return isConstructor;
	}

	public String getTypeQualifiedName() {
		int index = this.qualifiedName.lastIndexOf('.');
		return this.qualifiedName.substring(0,index);
	}
	public void setConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}

	public String getName() {
		return name;
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public List<ParameterEntity> getParameterList() {
		return parameterList;
	}
	
	public static MethodDeclarationEntity get(ResolvedMethodDeclaration resolvedMethodDeclaration, JavaParserFacade jpf, BufferedWriter logbw) throws Exception{
		String name = resolvedMethodDeclaration.getName();
		String qualifiedName = resolvedMethodDeclaration.getQualifiedName();
							
		ArrayList<ParameterEntity> parameterList = new ArrayList();
		
		for(int i=0;i<resolvedMethodDeclaration.getNumberOfParams();i++) {
			ResolvedParameterDeclaration parameterDeclration = resolvedMethodDeclaration.getParam(i);
			ResolvedType resolvedType = parameterDeclration.getType();
			TypeDescriptor parameterTypeDescriptor = new TypeDescriptor(resolvedType);
			try {
				Optional<String> parameterName = Optional.of(parameterDeclration.getName());
				ParameterEntity parameterEntity = new ParameterEntity(parameterName,i,parameterTypeDescriptor);
				parameterList.add(parameterEntity);
			}catch(java.lang.UnsupportedOperationException unsupportedException) {
				Optional<String> parameterName = Optional.empty();
				ParameterEntity parameterEntity = new ParameterEntity(parameterName,i,parameterTypeDescriptor);
				parameterList.add(parameterEntity);
			}
		}
		logbw.write("Method: "+resolvedMethodDeclaration.getSignature());
		logbw.newLine();
        logbw.write("Class: "+resolvedMethodDeclaration.getClassName()+" "+resolvedMethodDeclaration.getPackageName());
        logbw.newLine();
		
		Optional<TypeDescriptor> returnTypeDescriptor = Optional.empty();
		if(resolvedMethodDeclaration.getReturnType()!=null) {
			ResolvedType resolvedType = resolvedMethodDeclaration.getReturnType();
			returnTypeDescriptor = Optional.of(new TypeDescriptor(resolvedType));
		}
		
		//determine access specifier
		Optional<AccessSpecifier> optionalAccessSpecifier = Optional.empty();
		try {
			optionalAccessSpecifier = Optional.of(resolvedMethodDeclaration.accessSpecifier());
		}catch(java.lang.UnsupportedOperationException unsupportedException) {
			//this operation is not supported for byte code currently
		}
		return new MethodDeclarationEntity(false, resolvedMethodDeclaration.isAbstract(),
				resolvedMethodDeclaration.isStatic(), 
				resolvedMethodDeclaration.isDefaultMethod(), 
				name, 
				qualifiedName, 
				parameterList, 
				returnTypeDescriptor,
				optionalAccessSpecifier);
	}
	
	public static MethodDeclarationEntity get(ResolvedConstructorDeclaration resolvedConstructorDeclaration, JavaParserFacade jpf,BufferedWriter logbw) throws IOException {
		String name = resolvedConstructorDeclaration.getName();
		String qualifiedName = resolvedConstructorDeclaration.getQualifiedName();
		ArrayList<ParameterEntity> parameterList = new ArrayList();
		
		for(int i=0;i<resolvedConstructorDeclaration.getNumberOfParams();i++) {
			ResolvedParameterDeclaration parameterDeclration = resolvedConstructorDeclaration.getParam(i);
			ResolvedType resolvedType = parameterDeclration.getType();
			TypeDescriptor parameterTypeDescriptor = new TypeDescriptor(resolvedType);
			try {
				Optional<String> parameterName = Optional.of(parameterDeclration.getName());
				ParameterEntity parameterEntity = new ParameterEntity(parameterName,i,parameterTypeDescriptor);
				parameterList.add(parameterEntity);
			}catch(java.lang.UnsupportedOperationException unsupportedException) {
				Optional<String> parameterName = Optional.empty();
				ParameterEntity parameterEntity = new ParameterEntity(parameterName,i,parameterTypeDescriptor);
				parameterList.add(parameterEntity);
			}
			
		}
		logbw.write("Method: "+resolvedConstructorDeclaration.getSignature());
        logbw.newLine();
        logbw.write("Class: "+resolvedConstructorDeclaration.getClassName()+" "+resolvedConstructorDeclaration.getPackageName());
        logbw.newLine();
		
		//constructor declaration does not have a return type
		Optional<TypeDescriptor> returnTypeDescriptor = Optional.empty();
		
		//determine access specifier
		Optional<AccessSpecifier> optionalAccessSpecifier = Optional.empty();
		try {
			optionalAccessSpecifier = Optional.of(resolvedConstructorDeclaration.accessSpecifier());
		}catch(java.lang.UnsupportedOperationException unsupportedException) {
			//this operation is not supported for byte code currently
		}
		return new MethodDeclarationEntity(true,false,false,false, name, qualifiedName, parameterList, returnTypeDescriptor,optionalAccessSpecifier);
	}
	
	public static void main(String[] args) {
		String str = "java.util.List.clear";
		int index = str.lastIndexOf('.');
		System.out.println("Return Type: "+str.substring(0,index));
	}
	
	
	public AccessSpecifier getAccessSpecifier() {
		return accessSpecifier;
	}

	
	@Override
	public String toString() {
		return "MethodDeclarationEntity [name=" + name + ", qualifiedName=" + qualifiedName + ", parameterList="
				+ parameterList + ", returnType=" + returnType + ", accessSpecifier=" + accessSpecifier
				+ ", isConstructor=" + isConstructor + ", isAbstract=" + isAbstract + ", isStatic=" + isStatic
				+ ", isDefault=" + isDefault + "]";
	}

	public String getSignature(){
		String parameterString = "";
		
		for (final ParameterEntity parameter : this.getParameterList()) {
			parameterString += parameter.getTypeDescriptor().getTypeQualifiedName()+ ", ";
		}
		if (this.parameterList.size() > 0) {
			parameterString = parameterString.substring(0,
					parameterString.length() - 2);
		}
		return this.name + "(" + parameterString + ")";
	}
}
