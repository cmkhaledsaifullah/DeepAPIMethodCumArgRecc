package org.srlab.damca.completioner;

import java.io.Serializable;

import org.srlab.damca.node.ParameterContent;

public class ParameterDescriptor implements Serializable{
	
	//Information specific to method call where parameter reside
	private String methodName;
	private String methodSignature;
    private String receiverQualifiedTypeName;
    private int numOfParameters;
	
    //Information regarding source code location
    private int methodCallExprPosition;
    private int parameterExprPosition;
    private String filePath;
    
    //Information regarding the parameter
    private int    parameterOrder;
	private String parameterExpressionType;
	private String absStringRep;
	private ParameterContent parameterContent;
		
	//Other information
	private int id;	
	private String neighborList;
	private String lineContent;
	
	public static int count = 0;
		
	public ParameterDescriptor(String methodName, 
			String methodSignature, 
			String receiverQualifiedTypeName,
			int numOfParameters, 
			int methodCallExprPosition, 
			int parameterExprPosition, 
			String filePath,
			int parameterOrder, 
			String parameterExpressionType, 
			String absStringRep, 
			ParameterContent parameterContent,
			String neighborList, 
			String lineContent) {
		super();
		this.methodName = methodName;
		this.methodSignature = methodSignature;
		this.receiverQualifiedTypeName = receiverQualifiedTypeName;
		this.numOfParameters = numOfParameters;
		this.methodCallExprPosition = methodCallExprPosition;
		this.parameterExprPosition = parameterExprPosition;
		this.filePath = filePath;
		this.parameterOrder = parameterOrder;
		this.parameterExpressionType = parameterExpressionType;
		this.absStringRep = absStringRep;
		this.parameterContent = parameterContent;
		this.id = count++;
		this.neighborList = neighborList;
		this.lineContent = lineContent;
	}

		/*private void setParameterContent(MethodInvocation methodInvocation, MethodDeclaration methodDeclaration, Expression parameterExpression){
		if(parameterExpression instanceof SimpleName){
			parameterContent = new SimpleNameContent(methodInvocation,methodDeclaration, (SimpleName)parameterExpression);
		}
		else if(parameterExpression instanceof QualifiedName){
			parameterContent = new QualifiedNameContent(methodInvocation,methodDeclaration, (QualifiedName)parameterExpression);		
		}
		else if(parameterExpression instanceof StringLiteral){
			parameterContent = new StringLiteralContent(methodInvocation,methodDeclaration, (StringLiteral)parameterExpression);
			
		}
		else if(parameterExpression instanceof NumberLiteral){
			parameterContent = new NumberLiteralContent(methodInvocation,methodDeclaration, (NumberLiteral)parameterExpression);
				
		}
		else if(parameterExpression instanceof NullLiteral){
			parameterContent = new NullLiteralContent(methodInvocation,methodDeclaration, (NullLiteral)parameterExpression);
				
		}
		else if(parameterExpression instanceof ThisExpression){
			parameterContent = new ThisExpressionContent(methodInvocation,methodDeclaration, (ThisExpression)parameterExpression);
					
		}
		else if(parameterExpression instanceof MethodInvocation){
			parameterContent = new MethodInvocationContent(methodInvocation,methodDeclaration, (MethodInvocation)parameterExpression);
					
		}
		else if(parameterExpression instanceof CastExpression){
			parameterContent = new CastExpressionContent(methodInvocation,methodDeclaration, (CastExpression)parameterExpression);
					
		}
		else if(parameterExpression instanceof ClassInstanceCreation){
			parameterContent = new ClassInstanceCreationContent(methodInvocation,methodDeclaration, (ClassInstanceCreation)parameterExpression);		
		}
		else{
			parameterContent = new UnknownContent(methodInvocation,methodDeclaration,parameterExpression);				
		}
	}*/

		public int getId() {
			return id;
		}

		public String getMethodName() {
			return methodName;
		}

		public String getMethodSignature() {
			return methodSignature;
		}

		public String getReceiverQualifiedTypeName() {
			return receiverQualifiedTypeName;
		}

		public int getNumOfParameters() {
			return numOfParameters;
		}

		public int getMethodCallExprPosition() {
			return methodCallExprPosition;
		}

		public int getParameterExprPosition() {
			return parameterExprPosition;
		}

		public int getParameterOrder() {
			return parameterOrder;
		}

		public String getParameterExpressionType() {
			return parameterExpressionType;
		}

		public String getAbsStringRep() {
			return absStringRep;
		}

		public ParameterContent getParameterContent() {
			return parameterContent;
		}

		public String getNeighborList() {
			return neighborList;
		}

		public String getLineContent() {
			return lineContent;
		}

		@Override
		public String toString() {
			return "ParameterDescriptor [methodName=" + methodName + ", methodSignature=" + methodSignature
					+ ", receiverQualifiedTypeName=" + receiverQualifiedTypeName + ", numOfParameters="
					+ numOfParameters + ", methodCallExprPosition=" + methodCallExprPosition
					+ ", parameterExprPosition=" + parameterExprPosition + ", filePath=" + filePath
					+ ", parameterOrder=" + parameterOrder + ", parameterExpressionType=" + parameterExpressionType
					+ ", absStringRep=" + absStringRep + ", parameterContent=" + parameterContent + ", id=" + id
					+ ", neighborList=" + neighborList + ", lineContent=" + lineContent + "]";
		}
}
