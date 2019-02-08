package org.srlab.damca.simplename;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.srlab.damca.completioner.SourcePosition;
import org.srlab.damca.config.Config;

public class SimpleNameCollectorDriver {
	
	private String outputPath;

	public List<String> collectSourceFiles(File file){
		List<String> fileList = new LinkedList();
		if(file.isDirectory()) {
			for(File f:file.listFiles()) {
				fileList.addAll(this.collectSourceFiles(f));
			}
		}else if(file.isFile() && file.getName().endsWith(".java")){
			fileList.add(file.getAbsolutePath());
		}
		else
        {
            System.err.println("The file, "+file.getAbsolutePath()+", cannnot be used!!!");
        }
		return fileList;
	}

	public List<OutputEntity> getSourceFile(BufferedWriter logbw)
    {
        List<OutputEntity> outputEntities = new ArrayList<OutputEntity>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.outputPath));
            String sCurrentLine = br.readLine();
            while((sCurrentLine = br.readLine())!=null)
            {
                String testCase = "";
                String sourcePath = "";
                String sourcePosition = "";
                String context = "";
                String actualOutput = "";
                int sourceLine = 0;
                int sourceColumn = 0;
                List<String> predictedOutput = new ArrayList<String>();
                if(sCurrentLine.startsWith("Test Case:"))
                {
                    testCase = sCurrentLine;
                    sCurrentLine = br.readLine();
                }
                else
                {
                    while(!sCurrentLine.startsWith("Test Case:"))
                        sCurrentLine = br.readLine();
                    testCase = sCurrentLine;
                    sCurrentLine = br.readLine();
                }
                if (sCurrentLine.startsWith("Path:"))
                {
                    String[] token = sCurrentLine.split(":");
                    if(!token[1].trim().startsWith("/"))
                        continue;
                    int index = token[1].trim().indexOf("repository");
                    sourcePath = token[1].trim().substring(index);
                    if(sourcePath.contains("eclipse-sourceBuild-srcIncluded-3.7.2"))
                    {
                        sourcePath = sourcePath.replaceFirst("repository/eclipse-sourceBuild-srcIncluded-3.7.2", Config.REPOSITORY_PATH+"eclipse-3.7.2");
                    }
                    else {
                        sourcePath=sourcePath.replaceFirst("repository",Config.REPOSITORY_PATH+"netbeans");
                    }

                    File file = new File(sourcePath);
                    if(!file.exists()) {
                        sCurrentLine = br.readLine();
                        String[] brokern_line = sCurrentLine.split(" ");
                        String broken_path = brokern_line[2];
                        for (int start = 3; start <= brokern_line.length - 4; start++)
                            broken_path += " " + brokern_line[start];
                        sourcePath = sourcePath + broken_path + ".java";
                        file = new File(sourcePath);
                        if (!file.exists())
                        {
                            System.err.println(sourcePath);
                            continue;
                        }
                        sCurrentLine = brokern_line[brokern_line.length-3]+" "+brokern_line[brokern_line.length-2]+" "+brokern_line[brokern_line.length-1];
                    }
                    else
                    {
                        sCurrentLine = br.readLine();
                    }
                }
                if (sCurrentLine.startsWith("Source Position:"))
                {
                    sourcePosition = sCurrentLine;
                    String[] tokens = sCurrentLine.split(" ");
                    sourceLine = Integer.parseInt(tokens[tokens.length-1]);
                    sourceColumn = Integer.parseInt(tokens[tokens.length-3]);
                    sCurrentLine = br.readLine();
                }
                if (sCurrentLine.startsWith("Context:"))
                {
                    context = sCurrentLine;
                    sCurrentLine = br.readLine();
                }
                if (sCurrentLine.startsWith("Actual Output:"))
                {
                    actualOutput = sCurrentLine;
                    sCurrentLine = br.readLine();
                }
                if (sCurrentLine.startsWith("Predicted Output:"))
                {
                    String denormalized = "";
                    while((sCurrentLine = br.readLine())!=null  && !sCurrentLine.startsWith("===="))
                    {
                        if (sCurrentLine.contains("SN:")) {
                            String[] tokens = sCurrentLine.split(" ");
                            denormalized = tokens[0];
                            for(int param = 1; param<tokens.length;param++)
                            {
                                if(tokens[param].contains("SN:"))
                                {
                                    SourcePosition sourcePosition1 = new SourcePosition(sourceLine,sourceColumn);
                                    denormalizeSimpleName(new File(sourcePath),sourcePosition1,logbw);
                                }
                                else
                                    denormalized = denormalized+" "+ tokens[param];
                            }
                            predictedOutput.add(denormalized);
                        }
                        else
                            predictedOutput.add(sCurrentLine);
                    }
                }

                OutputEntity outputEntity = new OutputEntity(testCase,sourcePath,sourcePosition,context,actualOutput,predictedOutput);

                outputEntities.add(outputEntity);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputEntities;
    }

    public void denormalizeSimpleName(File file, SourcePosition sourcePosition, BufferedWriter logbw)
    {
        CompilationUnit cu;
        try {
            cu = JavaParser.parse(new FileInputStream(file));

            for (TypeDeclaration typeDeclaration : cu.getTypes()) {
                for (Object obj : typeDeclaration.getMethods()) {
                    if (obj instanceof MethodDeclaration) {
                        MethodDeclaration md = (MethodDeclaration) obj;
                        System.out.println("Method m: " + md.getDeclarationAsString());
                        SimpleNameCollector sn = new SimpleNameCollector(md, sourcePosition, logbw);
                        sn.run();
                        //sn.collectParameters();
                        //sn.collectLocalVariables();
                        for (VariableEntity ve : sn.getParameterVariableEntities()) {
                            System.out.println(ve);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
	
	public void run(BufferedWriter logbw) {
	    List<OutputEntity> outputEntities =  getSourceFile(logbw);
	    writeOutputs(Config.FINAL_OUTPUT_PATH,outputEntities);
		/*List<String> fileList = this.collectSourceFiles(new File(this.outputPath));
		System.out.println("Total Collected Files: "+fileList.size());
		for(String file:fileList) {
			//first convert the file to compilation unit
			CompilationUnit cu;
			try {
				cu = JavaParser.parse(new FileInputStream(file));
				
				for(TypeDeclaration typeDeclaration:cu.getTypes()) {
					for(Object obj:typeDeclaration.getMethods()) {
						if(obj instanceof MethodDeclaration) {
							MethodDeclaration md = (MethodDeclaration)obj;
							System.out.println("Method m: "+md.getDeclarationAsString());
							SimpleNameCollector sn = new SimpleNameCollector(md,null,logbw);
							sn.run();
							//sn.collectParameters();
							//sn.collectLocalVariables();
							for(VariableEntity ve: sn.getParameterVariableEntities()) {
								System.out.println(ve);
							}
							
							//md.accept(new SimpleNameRecommendationTestVisitor(cu,"",logbw),null);
						}
					}	
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
                e.printStackTrace();
            }
        }*/
	}
	public SimpleNameCollectorDriver(String outputPath) {
		this.outputPath = outputPath;
	}
	
	public static void main(String args[]) {
		SimpleNameCollectorDriver parameterCollector = new SimpleNameCollectorDriver(Config.OUTPUT_PATH);
		try {
			parameterCollector.run(new BufferedWriter(new FileWriter(new File(Config.LOG_FILE_PATH))));
		} catch (IOException e) {
			e.printStackTrace();
		}
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


    public void writeOutputs(String outputPath, List<OutputEntity> outputEntities)
    {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(outputPath));
            for (OutputEntity outputEntity:outputEntities)
            {
                bw.write("======================================================================== \n");
                bw.write(outputEntity.getTestCase()+"\n");
                bw.write("Path: "+outputEntity.getSourcePath()+"\n");
                bw.write(outputEntity.getSourcePosition()+"\n");
                bw.write(outputEntity.getContext()+"\n");
                bw.write(outputEntity.getActualOutput()+"\n");
                bw.write("Predicted Output:\n");
                for (String predicted: outputEntity.getPredictedOutput())
                    bw.write(predicted+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
