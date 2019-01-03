package org.srlab.damca.completioner;

import java.util.List;

import org.srlab.damca.node.ParameterContent;

public class ModelEntry {
	private MethodCallEntity methodCallEntity;
	private List<ParameterContent> parameterContentList;
	private String neighborList;
	private String lineContent;
	private String prevMethod;
	private String prevMethodArg;

	public ModelEntry(MethodCallEntity methodCallEntity, List<ParameterContent> parameterContentList, String neighborList, String lineContent,String prevMethod,String prevMethodArg) {
		super();
		this.methodCallEntity = methodCallEntity;
		this.parameterContentList = parameterContentList;
		this.neighborList = neighborList;
		this.lineContent = lineContent;
		this.prevMethod = prevMethod;
		this.prevMethodArg = prevMethodArg;
	}

	public MethodCallEntity getMethodCallEntity() {
		return methodCallEntity;
	}

	public void setMethodCallEntity(MethodCallEntity methodCallEntity) {
		this.methodCallEntity = methodCallEntity;
	}

	
	public List<ParameterContent> getParameterContentList() {
		return parameterContentList;
	}

	public void setParameterContentList(List<ParameterContent> parameterContentList) {
		this.parameterContentList = parameterContentList;
	}

	public String getNeighborList() {
		return neighborList;
	}

	public void setNeighborList(String neighborList) {
		this.neighborList = neighborList;
	}

	public String getLineContent() {
		return lineContent;
	}

	public void setLineContent(String lineContent) {
		this.lineContent = lineContent;
	}

	public String getPrevMethod() {
		return prevMethod;
	}

	public void setPrevMethod(String prevMethod) {
		this.prevMethod = prevMethod;
	}

	public String getPrevMethodArg() {
		return prevMethodArg;
	}

	public void setPrevMethodArg(String prevMethodArg) {
		this.prevMethodArg = prevMethodArg;
	}

	@Override
	public String toString() {
		return "ParameterModelEntry [methodCallEntity=" + methodCallEntity + ", parameterContentList="
				+ parameterContentList + ", neighborList=" + neighborList + ", lineContent=" + lineContent + "]";
	}

}
