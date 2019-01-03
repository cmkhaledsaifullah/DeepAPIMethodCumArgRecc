package org.srlab.damca.completioner;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import org.srlab.damca.binding.JSSConfigurator;
import org.srlab.damca.binding.TypeDescriptor;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Resolver
{
    public static TypeDescriptor resolveSimpleName(Node node)
    {
        SimpleName exp = (SimpleName) node;
        //System.out.println("hello");
        TypeDescriptor typeDescriptor = null;
        try {
            JavaParserFacade jpf = JSSConfigurator.getInstance(new BufferedWriter(new FileWriter("test.txt"))).getJpf();
            SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(exp);
            if(srResolvedValueDeclaration.isSolved()) {
                ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
                ResolvedType resolvedType = resolvedValueDeclaration.getType();
                typeDescriptor = new TypeDescriptor(resolvedType);
                //System.out.println(node+" VAR["+typeDescriptor.getTypeQualifiedName()+"]");
            }
        }
        catch (Exception ex) {
            if (ex.getMessage().contains("Unsolved symbol")) {
                //System.out.println("TYPE["+node+"]");
            }
        }
        return typeDescriptor;
    }

    public static TypeDescriptor resolveScope(Expression scope)
    {
        NameExpr sn = (NameExpr)scope;
        TypeDescriptor typeDescriptor = null;
        try {
            JavaParserFacade jpf = JSSConfigurator.getInstance(new BufferedWriter(new FileWriter("test.txt"))).getJpf();
            SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(sn);
            if(srResolvedValueDeclaration.isSolved()) {
                ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
                ResolvedType resolvedType = resolvedValueDeclaration.getType();
                typeDescriptor = new TypeDescriptor(resolvedType);

            }
        }
        catch (Exception ex) {
            if (ex.getMessage().contains("Unsolved symbol")) {
                //System.out.println("TYPE["+node+"]");
            }
        }
        return typeDescriptor;
    }

    public static String resolveReturnType(MethodCallExpr m)
    {
        String returnType = null;
        try {
            //Reolve the type binding for method decleration
            SymbolReference<ResolvedMethodDeclaration> srResolvedMethodDeclaration = JSSConfigurator.getInstance(new BufferedWriter(new FileWriter("test.txt"))).getJpf().solve(m);

            if (srResolvedMethodDeclaration.isSolved() && srResolvedMethodDeclaration.getCorrespondingDeclaration() != null) {

                ResolvedMethodDeclaration resolvedMethodDeclaration = srResolvedMethodDeclaration.getCorrespondingDeclaration();
                returnType = resolvedMethodDeclaration.getReturnType().describe();
            }
        }
        catch (Exception ex)
        {

        }
        return returnType;
    }
}
