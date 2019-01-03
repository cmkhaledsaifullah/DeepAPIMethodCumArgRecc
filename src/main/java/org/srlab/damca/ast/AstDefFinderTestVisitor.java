package org.srlab.damca.ast;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.config.Config;

import java.io.BufferedWriter;
import java.util.Optional;


public class AstDefFinderTestVisitor extends VoidVisitorAdapter<Void>{

	private CompilationUnit cu;
	private BufferedWriter logbw;
	
	public AstDefFinderTestVisitor(CompilationUnit _cu, String _path, BufferedWriter logbw) {
		// TODO Auto-generated constructor stub
		this.cu = _cu;
		this.logbw = logbw;
	}
	
	
	public CompilationUnit getCu() {
		return cu;
	}
	
	public MethodDeclaration getMethodDeclarationContainer(Node node) {
		Optional<Node> parent = node.getParentNode();
		while(parent.isPresent() && ((parent.get() instanceof MethodDeclaration))==false){
			parent = parent.get().getParentNode();
		}
		if(parent.isPresent() && ((parent.get())instanceof MethodDeclaration)) {
			return (MethodDeclaration)parent.get();
		}
		else return null;
	}
		
	@Override
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);
		MethodDeclaration methodDeclaration = null;
		// collect method declaration
		Optional<Node> parent = m.getParentNode();
		while (parent.isPresent() && !(parent.get() instanceof MethodDeclaration)) {
			parent = parent.get().getParentNode();
		}

		if (parent.isPresent() && parent.get() instanceof MethodDeclaration) {
			methodDeclaration = (MethodDeclaration) parent.get();
		}

		if (m.getScope().isPresent()) {

			// resolved the method binding

			try {
				SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = JSSConfigurator.getInstance(logbw)
						.getJpf().solve(m);
				if (resolvedMethodDeclaration.isSolved()) {
					String methodQualifiedName = resolvedMethodDeclaration.getCorrespondingDeclaration()
							.getQualifiedName();
				// if this is a framework method call and the method has parameter we process it
					if (Config.isInteresting(methodQualifiedName)) {
						
						if(m.getScope().get() instanceof NameExpr) {
							String varname = m.getScope().get().asNameExpr().getName().getIdentifier();
							Position position =m.getScope().get().asNameExpr().getBegin().get();
							logbw.write("+++++++++Method Call Expression: "+m+" +++++++++++++++++++");
                            logbw.newLine();
							AstDefFinder astDefFinder = new AstDefFinder(varname, position,methodDeclaration, JSSConfigurator.getInstance(logbw).getJpf(),logbw);
							astDefFinder.print();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
