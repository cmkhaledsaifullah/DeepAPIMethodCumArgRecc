package org.srlab.damca.completioner;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.srlab.damca.config.Config;
import org.srlab.damca.ast.AstDefFinderTestVisitor;

public class SimpleNameCollectorDriver {
	
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
							/*System.out.println("Method m: "+md.getDeclarationAsString());
							SimpleNameCollector sn = new SimpleNameCollector(md,null);
							//sn.collectParameters();
							sn.collectLocalVariables();
							for(VariableEntity ve: sn.getParameterVariableEntities()) {
								System.out.println(ve);
							}*/
							
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
	
	public SimpleNameCollectorDriver(String _repositoryPath) {
		this.repositoryPath = _repositoryPath;
	}
	
	public static void main(String args[]) {
		SimpleNameCollectorDriver parameterCollector = new SimpleNameCollectorDriver(Config.REPOSITORY_PATH);
		//parameterCollector.run();
		/*ArrayList<Integer> list = new ArrayList();
		list.add(5);
		list.add(3);
		list.add(2);
		Collections.sort(list,new Comparator<Integer>(){

			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
			return o2-o1;	
			}
		});
		
		System.out.println(list);*/
	}
}
