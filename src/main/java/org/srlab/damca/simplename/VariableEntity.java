package org.srlab.damca.simplename;

import org.srlab.damca.completioner.SourcePosition;

public class VariableEntity {
	/**
     * Variable type. Used to choose the best guess based on scope (Local beats instance beats inherited).
     */
	
	private  String name;
    private  String qualifiedTypeName;
    private  VariableEntityCategory entityCategory;
    private  VariableLocationCategory locationCategory;
    private SourcePosition sourcePosition;
    
    private  boolean autoboxingMatch;
    private  boolean alreadyMatched;
    private  int positionScore;
    private int inheritanceDepth;
	
    public VariableEntity(String qualifiedTypeName, String name, VariableEntityCategory _entityCategory,
    		VariableLocationCategory _locationCategory,
			SourcePosition sourcePosition) {
		super();
		this.name = name;
		this.qualifiedTypeName = qualifiedTypeName;
		this.locationCategory = _locationCategory;
		this.entityCategory  = _entityCategory;
		this.sourcePosition = sourcePosition;
		this.positionScore = -1;
		this.inheritanceDepth = -1;
		this.autoboxingMatch = false;
		this.alreadyMatched  = false;
	}
	public String getQualifiedTypeName() {
		return qualifiedTypeName;
	}
	public void setQualifiedTypeName(String qualifiedTypeName) {
		this.qualifiedTypeName = qualifiedTypeName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public VariableEntityCategory getEntityCategory() {
		return entityCategory;
	}
	
	public VariableLocationCategory getLocationCategory() {
		return locationCategory;
	}
	public SourcePosition getSourcePosition() {
		return sourcePosition;
	}
	public void setSourcePosition(SourcePosition sourcePosition) {
		this.sourcePosition = sourcePosition;
	}
	public boolean isAutoboxingMatch() {
		return autoboxingMatch;
	}
	public void setAutoboxingMatch(boolean autoboxingMatch) {
		this.autoboxingMatch = autoboxingMatch;
	}
	public boolean isAlreadyMatched() {
		return alreadyMatched;
	}
	public void setAlreadyMatched(boolean alreadyMatched) {
		this.alreadyMatched = alreadyMatched;
	}
	public int getPositionScore() {
		return positionScore;
	}
	public void setPositionScore(int positionScore) {
		this.positionScore = positionScore;
	}
	public int getInheritanceDepth() {
		return inheritanceDepth;
	}
	public void setInheritanceDepth(int inheritanceDepth) {
		this.inheritanceDepth = inheritanceDepth;
	}
	@Override
	public String toString() {
		return "VariableEntity [qualifiedTypeName=" + qualifiedTypeName + ", name=" + name + ", entityCategory="
				+ entityCategory + ", locationCategory=" + locationCategory + ", sourcePosition=" + sourcePosition
				+ ", autoboxingMatch=" + autoboxingMatch + ", alreadyMatched=" + alreadyMatched + ", positionScore="
				+ positionScore + ", inheritanceDepth=" + inheritanceDepth + "]";
	}
}
