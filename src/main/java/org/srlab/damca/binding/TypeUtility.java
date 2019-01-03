package org.srlab.damca.binding;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

public class TypeUtility {

	public static boolean isTypeHierarchyMatches(String qualifiedTypeName, ResolvedType rt) {
		TypeDescriptor td = new TypeDescriptor(rt);
		if(qualifiedTypeName.equals(td.getTypeQualifiedName()))
			return true;
		else if(rt.isReferenceType()) {
			ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration =   rt.asReferenceType().getTypeDeclaration();
			for(ResolvedReferenceType rrt: resolvedReferenceTypeDeclaration.getAllAncestors()) {
				if(rrt.getQualifiedName().equals(qualifiedTypeName))
					return true;
			}
		}
		return false;
	}

}
