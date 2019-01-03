package org.srlab.damca.category;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.srlab.damca.config.Config;

public class ParameterCollector {
	
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
		ParameterExpressionCategorizer parameterExpressionCategorizer = new ParameterExpressionCategorizer(true);
		int counter = 0;
		for(String file:fileList) {
			logbw.write("Progress: "+ (counter++)+"/"+fileList.size());
			logbw.newLine();
			//first convert the file to compilation unit
			CompilationUnit cu;
			try {
				cu = JavaParser.parse(new FileInputStream(file));
				ParameterCategoryVisitor visitor = new ParameterCategoryVisitor(cu, parameterExpressionCategorizer,logbw);
				cu.accept(visitor,null);
		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		parameterExpressionCategorizer.print();
	}
	
	public ParameterCollector(String _repositoryPath) {
		this.repositoryPath = _repositoryPath;
	}
	
	public static void main(String args[]) {
		ParameterCollector parameterCollector = new ParameterCollector(Config.TEST_REPOSITORY_PATH);
		//parameterCollector.run();
	}
}
