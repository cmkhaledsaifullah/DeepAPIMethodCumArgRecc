package org.srlab.damca.category;

import java.io.*;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.node.ParameterContent;


public class ParameterCategoryVisitor extends VoidVisitorAdapter<Void>{

	private CompilationUnit cu;
	private ParameterExpressionCategorizer parameterCategorizer;
	private BufferedWriter logbw;

	public ParameterCategoryVisitor(CompilationUnit _cu, ParameterExpressionCategorizer _paramCategorizer,BufferedWriter logbw) {
		// TODO Auto-generated constructor stub
		this.cu = _cu;
		this.parameterCategorizer = _paramCategorizer;
		this.logbw = logbw;
	}
	
	
	public CompilationUnit getCu() {
		return cu;
	}
		
	public ParameterExpressionCategorizer getParamCategorizer() {
		return parameterCategorizer;
	}


	@Override
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);
        try {
            logbw.write("MethodCallExpr: "+m);
            logbw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
			if(m.getScope().isPresent()) {
				//resolved the method binding
				SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = 
						JSSConfigurator.getInstance(logbw).getJpf().solve(m);
				if(resolvedMethodDeclaration.isSolved()) {
					String methodQualifiedName = resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedName();
					
					//if this is a framework method call and the method has parameter we process it
					if(m.getArguments().size()>0) {
						for(int i=0;i<m.getArguments().size();i++) {
							parameterCategorizer.add(m, m.getArguments().get(i),i);
						}
						
						for(Expression expression:m.getArguments()) {
							ParameterContent parameterContent = ParameterContent.get(expression);
							logbw.write("+++++++++++++++++===Expression: "+expression+"  Parameter Content: "+parameterContent.getStringRep(expression,logbw));
                            logbw.newLine();
							logbw.write("Expression TYpe: "+parameterContent.getParameterExpressionType()+ "  Parameter Content: "+parameterContent.getAbsStringRep());
                            logbw.newLine();
						}
					}
				}
			}
		}
		catch(java.lang.RuntimeException e) {
            try {
                logbw.write("Error in binding method: "+m);
                logbw.newLine();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
