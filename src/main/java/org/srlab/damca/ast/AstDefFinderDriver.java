package org.srlab.damca.ast;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.srlab.damca.config.Config;

public class AstDefFinderDriver {
	
	private String repositoryPath;

	public List<String> collectSourceFiles(File file){
		List<String> fileList = new LinkedList();
		if(file.isDirectory()) {
			for(File f:file.listFiles()) {
				fileList.addAll(this.collectSourceFiles(f));
			}
		}else if(file.isFile() && file.getName().endsWith(".java")){
			fileList.add(file.getAbsolutePath());
		}
		return fileList;
	}
	
	public void run(BufferedWriter logbw) throws IOException {
		List<String> fileList = this.collectSourceFiles(new File(this.repositoryPath));
		logbw.write("Total Collected Files: "+fileList.size());
		logbw.newLine();
		for(String file:fileList) {
			//first convert the file to compilation unit
			CompilationUnit cu;
			try {
				cu = JavaParser.parse(new FileInputStream(file));
				
				for(TypeDeclaration typeDeclaration:cu.getTypes()) {
					for(Object obj:typeDeclaration.getMethods()) {
						if(obj instanceof MethodDeclaration) {
							MethodDeclaration md = (MethodDeclaration)obj;
							md.accept(new AstDefFinderTestVisitor(cu,"",logbw),null);
						}
					}	
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public AstDefFinderDriver(String _repositoryPath) {
		this.repositoryPath = _repositoryPath;
	}
	
	public static void main(String args[]) throws IOException {
        BufferedWriter logbw = new BufferedWriter(new FileWriter(new File("log.txt"),true));
		//System.out.println("Running ASTDefFinder");
        logbw.write("=============================================================");
        logbw.write("Running ASTDefFinder");
        logbw.newLine();
		AstDefFinderDriver astDefFinderDriver = new AstDefFinderDriver(Config.TEST_REPOSITORY_PATH);
		astDefFinderDriver.run(logbw);
	}
}
