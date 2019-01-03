package org.srlab.damca.binding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.CollectionContext;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import org.srlab.damca.config.Config;


//the job this class is to configure java symbol solver and tell and where to look for types
public class JSSConfigurator {
	public static JSSConfigurator self = null;
	private CombinedTypeSolver combinedTypeSolver;
	private JavaParserFacade jpf;	
	
	private JSSConfigurator(BufferedWriter logbw) throws IOException {
		this.combinedTypeSolver = null;
		this.jpf = null;
		this.init(Config.REPOSITORY_PATH,Config.EXTERNAL_DEPENDENCY_PATH,logbw);
	}
	
	public void clear() {
		//the objective is to clear any previously collected binding information
		JavaParserFacade.clearInstances();
		this.combinedTypeSolver = null;
		this.jpf = null;
	}
	
	public void init(String rootPath, String dependencyPath, BufferedWriter logbw) throws IOException {
		
		//Step-1: Create a combined type solver
		this.combinedTypeSolver = new CombinedTypeSolver();		

		Path path = Paths.get(rootPath);
		ProjectRoot projectRoot = 
			    new CollectionContext(new SymbolSolverCollectionStrategy())
			    .collect(path);
		logbw.write("Source Roots: "+projectRoot.getSourceRoots().size());
		logbw.newLine();
		
		//step-2: add java parser type solver
		for(int i=0;i<projectRoot.getSourceRoots().size();i++) {
			SourceRoot sourceroot = projectRoot.getSourceRoots().get(i);
			logbw.write("["+i+"] "+sourceroot.getRoot());
			logbw.newLine();
			combinedTypeSolver.add(new JavaParserTypeSolver(sourceroot.getRoot())); 
		}	
		
		//step-3: add reflection type solver
		combinedTypeSolver.add(new ReflectionTypeSolver()); 
		
		//step-4: collect all jar files in the source folder
		List<File> jarFileList = this.collectJarFiles(new File(rootPath));
        logbw.write("Collected Source Jar Files: "+jarFileList.size());
        logbw.newLine();
		for(File jarFile:jarFileList) {
			try {
                logbw.write("Jar: "+jarFile.getAbsolutePath());
                logbw.newLine();
				combinedTypeSolver.add(new JarTypeSolver(jarFile.getAbsolutePath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//step-5: collect jar files from dependency folder, if any
		List<File> dependencyJarFileList = this.collectJarFiles(new File(dependencyPath));
        logbw.write("Collected Dependency Jar Files: "+dependencyJarFileList.size());
        logbw.newLine();
		for(File jarFile:dependencyJarFileList) {
			try {
                logbw.write("Jar: "+jarFile.getAbsolutePath());
                logbw.newLine();
				combinedTypeSolver.add(new JarTypeSolver(jarFile.getAbsolutePath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//step-6: create an instance of JavaParserFacade
		this.jpf = JavaParserFacade.get(this.getCombinedTypeSolver());
	}
	
	public List<File> collectJarFiles(File file){

		List<File> jarFileList = new LinkedList();
		if(file.isDirectory() && file.listFiles()!=null) {
			for(File f:file.listFiles()) {
				if(f.isFile() && f.getName().endsWith(".jar")) {
					jarFileList.add(f);
				}
				else if(f.isDirectory()){
					jarFileList.addAll(collectJarFiles(f));
				}
			}
		}
		else if(file.isFile()&& file.getName().endsWith(".jar")) {
			jarFileList.add(file);
		}
		return jarFileList;
	}
	public static JSSConfigurator getInstance(BufferedWriter logbw) throws IOException {
		if(self==null){
			self = new JSSConfigurator(logbw);
		}
		return self;
	}
	
	public JavaParserFacade getJpf() {
		return jpf;
	}

	public CombinedTypeSolver getCombinedTypeSolver() {
		return combinedTypeSolver;
	}

	//public static void main(String args[]) {
	//	JSSConfigurator.getInstance().init(Config.REPOSITORY_PATH,Config.EXTERNAL_DEPENDENCY_PATH);
	//}
}