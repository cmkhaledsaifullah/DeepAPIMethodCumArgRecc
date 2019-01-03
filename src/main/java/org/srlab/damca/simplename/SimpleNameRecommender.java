package org.srlab.damca.simplename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class SimpleNameRecommender {

	private SimpleNameCollector simpleNameCollector;
	private String varName;
	private String varQualifiedTypeName;
	
	public SimpleNameRecommender(SimpleNameCollector _simpleNameCollector, String _varName, String _varQualifiedTypeName) {
		super();
		this.simpleNameCollector = _simpleNameCollector;
		this.varName = _varName;
		this.varQualifiedTypeName = _varQualifiedTypeName;
	}
	public boolean isUsed(VariableEntity ve) {
		for(VariableEntity variableEntity:this.simpleNameCollector.getUsedVariableEntities()){
			if(ve.getQualifiedTypeName().equals(variableEntity.getQualifiedTypeName()) && 
					ve.getName().equals(variableEntity.getName()))
				return true;
				
		}
		return false;
	}
	
	private List<VariableEntity> filter(List<VariableEntity> list, String targetQualifiedTypeName){
		List<VariableEntity> filteredList = new ArrayList();
		for(VariableEntity ve:list) {
			if(ve.getQualifiedTypeName().equals(targetQualifiedTypeName))
				filteredList.add(ve);
		}
		return filteredList;
	}
	public List<String> recommend() {
		int positionScore = 0;
		
		//Step-1: collect variables
		ArrayList<VariableEntity> variableEntityList = new ArrayList();
		variableEntityList.addAll(this.filter(simpleNameCollector.getLocalVariableDeclarationOrAssignedEntities(),this.varQualifiedTypeName));
		variableEntityList.addAll(filter(simpleNameCollector.getParameterVariableEntities(),this.varQualifiedTypeName));
		variableEntityList.addAll(this.filter(simpleNameCollector.getFieldVariableEntities(),this.varQualifiedTypeName));
		variableEntityList.addAll(filter(simpleNameCollector.getInheritedVariableEntities(),this.varQualifiedTypeName));
		
		//Step-2: determine which variable have already been used and assign position score
		for(VariableEntity variableEntity:variableEntityList) {
			variableEntity.setPositionScore(positionScore++);
			if(this.isUsed(variableEntity)) {
				variableEntity.setAlreadyMatched(true);
			}
		}
		
		//Step-3 sort the list
		System.out.println("VarName: "+varName);
		Collections.sort(variableEntityList,new VariableMatchComparator(this.varName));
		
		//Step-4:remove duplicates
		HashSet<String> hsDuplicate = new HashSet();
		List<String> variableNameList = new ArrayList();
		
		for(VariableEntity variableEntity:variableEntityList) {
			String key = variableEntity.getName()+":"+variableEntity.getQualifiedTypeName();
			if(!hsDuplicate.contains(key)){
				variableNameList.add(key);
				hsDuplicate.add(key);
			}
		}
		return variableNameList;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
