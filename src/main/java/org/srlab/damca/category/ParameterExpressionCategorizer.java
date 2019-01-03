package org.srlab.damca.category;

import com.github.javaparser.ast.expr.*;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.HashMap;


public class ParameterExpressionCategorizer {

	private MultiKeyMap mkMap;
	private MultiKeyMap mkMapExpressionType;
	private boolean enableDebugging;
	private static String ExpressionTypes[]= {"AnnotationExpr", 
		                                      "ArrayAccessExpr", 
		                                      "ArrayCreationExpr", 
		                                      "ArrayInitializerExpr", 
		                                      "AssignExpr",
		                                      "BinaryExpr",
		                                      "CastExpr", 
		                                      "ClassExpr", 
		                                      "ConditionalExpr",
		                                      "EnclosedExpr",
		                                      "FieldAccessExpr", 
		                                      "InstanceOfExpr", 
		                                      "LambdaExpr", 
		                                      "LiteralExpr",
		                                      "BooleanLiteralExpr",
		                                      "CharLiteralExpr",
		                                      "DoubleLiteralExpr",
		                                      "IntegerLiteralExpr",
		                                      "LongLiteralExpr",
		                                      "StringLiteralExpr",
		                                      "MethodCallExpr",
		                                      "MethodReferenceExpr",
		                                      "NameExpr",
		                                      "ObjectCreationExpr", 
		                                      "SuperExpr", 
		                                      "ThisExpr", 
		                                      "TypeExpr", 
		                                      "UnaryExpr", 
		                                      "VariableDeclarationExpr", 
		                                      "Name",
		                                      "SimpleName"
    };
	private HashMap<Integer,String> hmIBindingKind; 
	private HashMap<String,Long> hmParameterExpressionCounter;     //the number of same
			
	/*protected void groupByMethodName(ITypeBinding tb, MethodInvocation methodInvocation, Expression expression, int position){
		if(this.mkMap.containsKey(tb.getQualifiedName(), methodInvocation.getName().getFullyQualifiedName(),position)){
			ArrayList<String> list = (ArrayList<String>) this.mkMap.get(tb.getQualifiedName(),methodInvocation.getName().getFullyQualifiedName(),position);
			list.add(expression.toString());
			ArrayList<String> expTypeList = (ArrayList<String>) this.mkMapExpressionType.get(tb.getQualifiedName(),methodInvocation.getName().getFullyQualifiedName(),position);
			expTypeList.add(this.getExpressionType(expression));
		}
		else{
			ArrayList<String> list = new ArrayList();
			list.add(expression.toString());
			ArrayList<String> expTypeList = new ArrayList();
			expTypeList.add(this.getExpressionType(expression));
	
			this.mkMap.put(tb.getQualifiedName(),methodInvocation.getName().getFullyQualifiedName(),position,list);
			this.mkMapExpressionType.put(tb.getQualifiedName(),methodInvocation.getName().getFullyQualifiedName(),position,expTypeList);
		}
	}*/
	
	public ParameterExpressionCategorizer(boolean _enableDebugging) {
		this.mkMap = new MultiKeyMap();
		this.mkMapExpressionType = new MultiKeyMap();
		this.enableDebugging = _enableDebugging;
		this.hmParameterExpressionCounter = new HashMap();
		for(String exp:ExpressionTypes){
			this.hmParameterExpressionCounter.put(exp,0L);
		}
	}
	
	public static String getParameterExpressionType(Expression exp) {
		if(exp instanceof AnnotationExpr){
			return "AnnotationExpr";
		}
		else if(exp instanceof ArrayAccessExpr){
			return "ArrayAccessExpr";
		}
		else if(exp instanceof ArrayCreationExpr){
			return "ArrayCreationExpr";
		}
		else if(exp instanceof ArrayInitializerExpr){
			return "AnnotationExpr";
		}
		else if(exp instanceof AssignExpr){
			return "ArrayInitializerExpr";
		}
		else if(exp instanceof BinaryExpr){
			return "BinaryExpr";
		}
		else if(exp instanceof CastExpr){
			return "CastExpr";
		}
		else if(exp instanceof ClassExpr){
			return "ClassExpr";
		}
		else if(exp instanceof ConditionalExpr){
			return "ConditionalExpr";
		}
		else if(exp instanceof EnclosedExpr){
			return "EnclosedExpr";
		}
		else if(exp instanceof FieldAccessExpr){
			return "FieldAccessExpr";
		}
		else if(exp instanceof InstanceOfExpr){
			return "InstanceOfExpr";
		}
		else if(exp instanceof LambdaExpr){
			return "LambdaExpr";
		}
		else if(exp instanceof LiteralExpr){
			if(exp instanceof BooleanLiteralExpr){
				return "BooleanLiteralExpr";
			}
			else if(exp instanceof CharLiteralExpr){
				return "CharLiteralExpr";
			}
			else if(exp instanceof DoubleLiteralExpr){
				return "DoubleLiteralExpr";
			}
			else if(exp instanceof IntegerLiteralExpr){
				return "IntegerLiteralExpr";
			}
			else if(exp instanceof LongLiteralExpr){
				return "LongLiteralExpr";
			}
			else if(exp instanceof StringLiteralExpr){
				return "StringLiteralExpr";
			}
			else if(exp instanceof CharLiteralExpr){
				return "CharLiteralExpr";
			}
			else {
				throw new RuntimeException("Cannot find the expression type of this argument: "+exp);
			}
		}
		
		else if(exp instanceof MethodCallExpr){
			return "MethodCallExpr";
		}
		else if(exp instanceof MethodReferenceExpr){
			return "MethodReferenceExpr";
		}
		else if(exp instanceof NameExpr){
			return "NameExpr";
		}
		else if(exp instanceof ObjectCreationExpr){
			return "ObjectCreationExpr";
		}
		else if(exp instanceof SuperExpr){
			return "SuperExpr";
		}
		else if(exp instanceof ThisExpr){
			return "ThisExpr";
		}
		else if(exp instanceof TypeExpr){
			return "TypeExpr";
		}
		else if(exp instanceof UnaryExpr){
			return "UnaryExpr";
		}
		else if(exp instanceof VariableDeclarationExpr){
			return "VariableDeclarationExpr";
		}
		else {
			throw new RuntimeException("Cannot find the expression type of this argument: "+exp);
		}
	}
	
	//node is the method call, exp represents the parameter and position represents parameter position
	public void add(MethodCallExpr node, Expression exp, int position){
		
		//this.groupByMethodName(tb,node, exp, position);
		
		if(exp instanceof AnnotationExpr){
			if(enableDebugging) System.out.println("AnnotaionExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(AnnotationExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(AnnotationExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(AnnotationExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(AnnotationExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof ArrayAccessExpr){
			if(enableDebugging) System.out.println("ArrayAccessExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(ArrayAccessExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(ArrayAccessExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(ArrayAccessExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(ArrayAccessExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof ArrayCreationExpr){
			if(enableDebugging) System.out.println("ArrayCreationExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(ArrayCreationExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(ArrayCreationExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(ArrayCreationExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(ArrayCreationExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof ArrayInitializerExpr){
			if(enableDebugging) System.out.println("ArrayInitializerExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(ArrayInitializerExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(ArrayInitializerExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(ArrayInitializerExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(ArrayInitializerExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof AssignExpr){
			if(enableDebugging) System.out.println("AssignExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(AssignExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(AssignExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(AssignExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(AssignExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof BinaryExpr){
			if(enableDebugging) System.out.println("BinaryExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(BinaryExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(BinaryExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(BinaryExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(BinaryExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof CastExpr){
			if(enableDebugging) System.out.println("CastExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(CastExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(CastExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(CastExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(CastExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof ClassExpr){
			if(enableDebugging) System.out.println("ClassExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(ClassExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(ClassExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(ClassExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(ClassExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof ConditionalExpr){
			if(enableDebugging) System.out.println("ConditionalExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(ConditionalExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(ConditionalExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(ConditionalExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(ConditionalExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof EnclosedExpr){
			if(enableDebugging) System.out.println("EnclosedExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(EnclosedExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(EnclosedExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(EnclosedExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(EnclosedExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof FieldAccessExpr){
			if(enableDebugging) System.out.println("FieldAccessExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(FieldAccessExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(FieldAccessExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(FieldAccessExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(FieldAccessExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof InstanceOfExpr){
			if(enableDebugging) System.out.println("InstanceOfExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(InstanceOfExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(InstanceOfExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(InstanceOfExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(InstanceOfExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof LambdaExpr){
			if(enableDebugging) System.out.println("LambdaExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(LambdaExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(LambdaExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(LambdaExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(LambdaExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof LiteralExpr){
			if(enableDebugging) System.out.println("LiteralExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(LiteralExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(LiteralExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(LiteralExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(LiteralExpr.class.getSimpleName(), 1L);			
			}
			
			if(exp instanceof BooleanLiteralExpr){
				if(enableDebugging) System.out.println("BooleanLiteralExpr: "+exp);
				
				if(this.hmParameterExpressionCounter.containsKey(BooleanLiteralExpr.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(BooleanLiteralExpr.class.getSimpleName());
					this.hmParameterExpressionCounter.put(BooleanLiteralExpr.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(BooleanLiteralExpr.class.getSimpleName(), 1L);			
				}
			}
			else if(exp instanceof CharLiteralExpr){
				if(enableDebugging) System.out.println("CharLiteralExpr: "+exp);
				
				if(this.hmParameterExpressionCounter.containsKey(CharLiteralExpr.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(CharLiteralExpr.class.getSimpleName());
					this.hmParameterExpressionCounter.put(CharLiteralExpr.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(CharLiteralExpr.class.getSimpleName(), 1L);			
				}
			}
			else if(exp instanceof DoubleLiteralExpr){
				if(enableDebugging) System.out.println("DoubleLiteralExpr: "+exp);
				
				if(this.hmParameterExpressionCounter.containsKey(DoubleLiteralExpr.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(DoubleLiteralExpr.class.getSimpleName());
					this.hmParameterExpressionCounter.put(DoubleLiteralExpr.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(DoubleLiteralExpr.class.getSimpleName(), 1L);			
				}
			}
			else if(exp instanceof IntegerLiteralExpr){
				if(enableDebugging) System.out.println("IntegerLiteralExpr: "+exp);
				
				if(this.hmParameterExpressionCounter.containsKey(IntegerLiteralExpr.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(IntegerLiteralExpr.class.getSimpleName());
					this.hmParameterExpressionCounter.put(IntegerLiteralExpr.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(IntegerLiteralExpr.class.getSimpleName(), 1L);			
				}
			}
			else if(exp instanceof LongLiteralExpr){
				if(enableDebugging) System.out.println("LongLiteralExpr: "+exp);
				
				if(this.hmParameterExpressionCounter.containsKey(LongLiteralExpr.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(LongLiteralExpr.class.getSimpleName());
					this.hmParameterExpressionCounter.put(LongLiteralExpr.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(LongLiteralExpr.class.getSimpleName(), 1L);			
				}
			}
			else if(exp instanceof StringLiteralExpr){
				if(enableDebugging) System.out.println("StringLiteralExpr: "+exp);
				
				if(this.hmParameterExpressionCounter.containsKey(StringLiteralExpr.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(StringLiteralExpr.class.getSimpleName());
					this.hmParameterExpressionCounter.put(StringLiteralExpr.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(StringLiteralExpr.class.getSimpleName(), 1L);			
				}
			}
			else if(exp instanceof CharLiteralExpr){
				if(enableDebugging) System.out.println("CharLiteralExpr: "+exp);
				
				if(this.hmParameterExpressionCounter.containsKey(CharLiteralExpr.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(CharLiteralExpr.class.getSimpleName());
					this.hmParameterExpressionCounter.put(CharLiteralExpr.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(CharLiteralExpr.class.getSimpleName(), 1L);			
				}
			}
			
		}
		
		else if(exp instanceof MethodCallExpr){
			if(enableDebugging) System.out.println("MethodCallExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(MethodCallExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(MethodCallExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(MethodCallExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(MethodCallExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof MethodReferenceExpr){
			if(enableDebugging) System.out.println("MethodReferenceExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(MethodReferenceExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(MethodReferenceExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(MethodReferenceExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(MethodReferenceExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof NameExpr){
			if(enableDebugging) System.out.println("NameExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(NameExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(NameExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(NameExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(NameExpr.class.getSimpleName(), 1L);			
			}
			if((exp.asNameExpr().getChildNodes().size()==1) && 
					((exp.asNameExpr().getChildNodes().get(0)) instanceof SimpleName)) {
				if(enableDebugging) System.out.println("SimpleName: "+exp);
				if(this.hmParameterExpressionCounter.containsKey(SimpleName.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(SimpleName.class.getSimpleName());
					this.hmParameterExpressionCounter.put(SimpleName.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(SimpleName.class.getSimpleName(), 1L);			
				}
			}
			else if((exp.asNameExpr().getChildNodes().size()==1) && 
					((exp.asNameExpr().getChildNodes().get(0)) instanceof Name)) {
				if(enableDebugging) System.out.println("Name: "+exp);
				if(this.hmParameterExpressionCounter.containsKey(Name.class.getSimpleName())){
					long count = this.hmParameterExpressionCounter.get(Name.class.getSimpleName());
					this.hmParameterExpressionCounter.put(Name.class.getSimpleName(), (count+1));
				}
				else{
					this.hmParameterExpressionCounter.put(Name.class.getSimpleName(), 1L);			
				}
			}
		}
		else if(exp instanceof ObjectCreationExpr){
			if(enableDebugging) System.out.println("ObjectCreationExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(ObjectCreationExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(ObjectCreationExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(ObjectCreationExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(ObjectCreationExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof SuperExpr){
			if(enableDebugging) System.out.println("SuperExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(SuperExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(SuperExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(SuperExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(SuperExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof ThisExpr){
			if(enableDebugging) System.out.println("ThisExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(ThisExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(ThisExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(ThisExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(ThisExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof TypeExpr){
			if(enableDebugging) System.out.println("TypeExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(TypeExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(TypeExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(TypeExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(TypeExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof UnaryExpr){
			if(enableDebugging) System.out.println("UnaryExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(UnaryExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(UnaryExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(UnaryExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(UnaryExpr.class.getSimpleName(), 1L);			
			}
		}
		else if(exp instanceof VariableDeclarationExpr){
			if(enableDebugging) System.out.println("VariableDeclarationExpr: "+exp);
			
			if(this.hmParameterExpressionCounter.containsKey(VariableDeclarationExpr.class.getSimpleName())){
				long count = this.hmParameterExpressionCounter.get(VariableDeclarationExpr.class.getSimpleName());
				this.hmParameterExpressionCounter.put(VariableDeclarationExpr.class.getSimpleName(), (count+1));
			}
			else{
				this.hmParameterExpressionCounter.put(VariableDeclarationExpr.class.getSimpleName(), 1L);			
			}
		}
		else {
			throw new RuntimeException("Cannot find the expression type of this argument: "+exp);
		}
	}

	public void print(){
		int totalParameters = 0;
		for(String exp:ExpressionTypes){
			if(hmParameterExpressionCounter.get(exp)>0){
				if(!(exp.equals("LiteralExpr")) && !(exp.equals("Name"))&&!(exp.equals("SimpleName")))
					totalParameters+=hmParameterExpressionCounter.get(exp);
			}
		}
		
		for(String exp:ExpressionTypes){
			if(hmParameterExpressionCounter.get(exp)>0){
				double percentage = (hmParameterExpressionCounter.get(exp)/(totalParameters*1.0f))*100;
				System.out.println("Expression: "+exp+"   Count: "+hmParameterExpressionCounter.get(exp)+"  Percentage: "+percentage);
			}
		}

		System.out.println("Total Parameters: "+totalParameters);
	}
	
	/*public void printParametersGroupByMethodName(){
		int histogram[] = new int[5000];
		int CASES_SIMPLENAME_OTHERS = 0;
		int HISTOGRAM_SIZE = 5000;

		//initialize the histogram array
		for(int histogramIndex=0;histogramIndex<HISTOGRAM_SIZE;histogramIndex++){
			histogram[histogramIndex]=0;
		}
		
		System.out.println("Printing Parameters By Grouping Method Name");
		Set set = this.mkMap.keySet();
		Iterator it = set.iterator();
		System.out.println("Type,"+",Method"+",Position"+",item");
		
		while(it.hasNext()){
			MultiKey mkKey = (MultiKey) it.next();
			ArrayList<Expression> list = (ArrayList<Expression>) this.mkMap.get(mkKey.getKey(0),mkKey.getKey(1), mkKey.getKey(2));
			ArrayList<String> expTypeList = (ArrayList<String>)  this.mkMapExpressionType.get(mkKey.getKey(0),mkKey.getKey(1), mkKey.getKey(2));
			
			System.out.println(mkKey.getKey(0)+","+mkKey.getKey(1)+","+mkKey.getKey(2)+","+list.size());
			histogram[list.size()]++;
			int count=0;
			HashMap hm = new HashMap();
			for(String str:expTypeList){
				hm.put(str, str);
			}
			
			boolean simpleNameFound = false;
			boolean othersFound = false;
			for(String str:new ArrayList<String>(hm.keySet())){
				if(str.startsWith("SimpleName")){
					simpleNameFound = true;
				}
				else{
					othersFound =true;
				}
			}
			if(simpleNameFound==true && othersFound==true){
				CASES_SIMPLENAME_OTHERS++;
			}
			
			//System.out.println("ExpressionTypes: "+(new ArrayList(hm.keySet())));
			//for(Expression e:list){
			//	System.out.println("Exp: "+e.toString()+"   Type: "+expTypeList.get(count));
			//	count++;
			//}
			//System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		
		System.out.println("Total distinct methods: "+this.mkMap.keySet().size());
		System.out.println("Cases where simple name combined with others: "+CASES_SIMPLENAME_OTHERS);
		
		for(int histogramIndex=0;histogramIndex<HISTOGRAM_SIZE;histogramIndex++){
			if(histogram[histogramIndex]!=0){
				System.out.println("Histogram [ "+histogramIndex+"]: "+histogram[histogramIndex]);
			}
		}
	}*/
	public boolean isEnableDebugging() {
		return enableDebugging;
	}
	public void setEnableDebugging(boolean enableDebugging) {
		this.enableDebugging = enableDebugging;
	}
	public MultiKeyMap getMkMap() {
		return mkMap;
	}
	public MultiKeyMap getMkMapExpressionType() {
		return mkMapExpressionType;
	}
	public static String[] getExpressionTypes() {
		return ExpressionTypes;
	}

}
