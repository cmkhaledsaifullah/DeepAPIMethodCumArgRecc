package org.srlab.damca.completioner;

import java.io.Serializable;
import java.util.Optional;

import org.srlab.damca.binding.TypeDescriptor;

public class ParameterEntity implements Serializable{

	private String name;
	private int position;
	private TypeDescriptor typeDescriptor;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}
	public ParameterEntity(Optional<String> _name, int _position, TypeDescriptor _typeDescriptor) {
		this.name = _name.isPresent()?_name.get():null;
		this.position = _position;
		this.typeDescriptor = _typeDescriptor;
	}
	@Override
	public String toString() {
		return "ParameterEntity [name=" + name + ", position=" + position + ", typeDescriptor=" + typeDescriptor + "]";
	}
}
