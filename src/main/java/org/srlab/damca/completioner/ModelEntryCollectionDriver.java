/*
This is the main file for collecting contexts for every method call information. In this file, the main function execute all the actions.

*/


package org.srlab.damca.completioner;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.srlab.damca.config.Config;
import org.srlab.damca.node.ParameterContent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModelEntryCollectionDriver {


    // Path of the root
    private static String repositoryPath;

    //list of Model file for each context
    private static List<ModelEntry> modelEntryList;

    //list of SLP context
    private static List<List<String>> slp_context;

    //list of Model file for each SLAMC context
    private static List<SLAMCContext> slamc_contexts;

    public static List<String> collectSourceFiles(File file) {
        List<String> fileList = new ArrayList<>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                fileList.addAll(collectSourceFiles(f));
            }
        } else if (file.isFile() && file.getName().endsWith(".java")) {
            fileList.add(file.getAbsolutePath());
        }
        return fileList;
    }

    private static List<String> collect_subject_systems(File file) {
        List<String> subjectsystems = new ArrayList<>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                subjectsystems.add(f.getAbsolutePath());
            }
        } else if (file.isFile()) {
            subjectsystems.add(file.getAbsolutePath());
        }
        return subjectsystems;
    }

    public static long run(BufferedWriter logbw, String subject_system_path) throws IOException {

        List<String> subject_systems = collect_subject_systems(new File(subject_system_path));

        long idx = 1;
        for (String system_path : subject_systems) {
            String[] token = system_path.split("/");
            modelEntryList = new ArrayList<>();
            slp_context = new ArrayList<>();
            slamc_contexts = new ArrayList<>();
            System.out.println("Collecting Context for: " + system_path);
            logbw.write("==================================================================================");
            logbw.newLine();
            logbw.write("Subjet System: " + system_path);
            logbw.newLine();
            logbw.write("==================================================================================");
            logbw.newLine();


            //Collect all sources files in a directory
            List<String> fileList = collectSourceFiles(new File(system_path));

            logbw.write("Total Collected Files: " + fileList.size());
            logbw.newLine();

            int counter = 0;
            for (String file : fileList) {
                //first convert the file to compilation unit
                logbw.write("Progress: " + (counter++) + "/" + fileList.size());
                logbw.newLine();
                CompilationUnit cu;
                try {
                    cu = JavaParser.parse(new FileInputStream(file));

                    //resolve the types of each compilation unit
                    for (TypeDeclaration typeDeclaration : cu.getTypes()) {
                        // iterate for each method in the compilation units
                        for (Object obj : typeDeclaration.getMethods()) {
                            // Take every method declaration body and visit in there
                            if (obj instanceof MethodDeclaration) {
                                MethodDeclaration md = (MethodDeclaration) obj;
                                MethodCallExprVisitor methodCallExprVisitor = new MethodCallExprVisitor(cu,modelEntryList,slp_context,slamc_contexts, file, logbw, token[token.length - 1]);
                                //The program jump to MethodCallExprVisitor Class
                                md.accept(methodCallExprVisitor, null);

                                if(Config.IS_DAMCA_COLLECT)
                                    modelEntryList.addAll(methodCallExprVisitor.getModelEntryList());
                                if (Config.IS_SLP_COLLECT)
                                    slp_context.addAll(methodCallExprVisitor.getSLPContext());
                                if(Config.IS_SLAMC_COLLECT)
                                    slamc_contexts = methodCallExprVisitor.getSLAMCContexts();

                                idx = methodCallExprVisitor.getCounter();
                            }
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                }
            }
            long temp = modelEntryList.size();
            logbw.write("Total Number of instances for " + token[token.length - 1] + " subject system is: " + temp);
            logbw.newLine();
            logbw.write("==================================================================================");
            logbw.newLine();

            if(Config.IS_DAMCA_COLLECT)
                idx = writeContext(modelEntryList, token[token.length - 1], idx);
            if(Config.IS_SLP_COLLECT)
                writeSlpCotext(slp_context, token[token.length - 1]);
            if(Config.IS_SLAMC_COLLECT)
                writeSlamcCotext(slamc_contexts,token[token.length - 1]);
            System.out.println("==================================================================================");
        }

        return idx;


    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public List<ModelEntry> getModelEntryList() {
        return modelEntryList;
    }

    public ModelEntryCollectionDriver(String _repositoryPath) {
        this.repositoryPath = _repositoryPath;
        this.modelEntryList = new ArrayList<>();
        this.slamc_contexts = new ArrayList<>();
    }

    public static void main(String args[]) {
        //File f = new File(Config.MODEL_ENTRY_OUTPUT_PATH);
        //if(f.exists())
        //	f.delete();

        String subject_system_path = Config.ROOT_PATH;


        System.out.println("All Process and the workflows are stored in log.txt file.");

        try {
            BufferedWriter logbw = new BufferedWriter(new FileWriter(new File(Config.LOG_FILE_PATH)));

            // The run function
            long total_instances = ModelEntryCollectionDriver.run(logbw, subject_system_path);


            System.out.println("Context Collection Done. ");
            logbw.write("Total Collected Model Entry List: " + total_instances);
            logbw.newLine();

            logbw.close();
        } catch (IOException ex) {
            System.err.println("Error occur while collecting context. Please check log file!!!");
        }

        System.out.println("All process for context collection is Done");

    }


    //Retrive the context infomation and writein the output file
    public static long writeContext(List<ModelEntry> modelEntryList, String subjectSystemName, long last_id) {
        System.out.println("Writting the context in the predifined style at " + Config.MODEL_ENTRY_OUTPUT_PATH);
        System.out.println("Individual dataset can be found at " + Config.INDIVIDUAL_FILE_PATH + subjectSystemName + ".txt");
        long count = last_id;
        try {
            BufferedWriter bw;


            Random rand = new Random();

            BufferedWriter bwLocal;

            if (new File(Config.INDIVIDUAL_FILE_PATH + subjectSystemName + ".txt").exists())
                bwLocal = new BufferedWriter(new FileWriter(Config.INDIVIDUAL_FILE_PATH + subjectSystemName + ".txt", true));
            else
                bwLocal = new BufferedWriter(new FileWriter(Config.INDIVIDUAL_FILE_PATH + subjectSystemName + ".txt"));

            for (ModelEntry modelEntry : modelEntryList) {

                int i = rand.nextInt(10) + 1;

                if (new File(Config.MODEL_ENTRY_OUTPUT_PATH).exists())
                    bw = new BufferedWriter(new FileWriter(Config.MODEL_ENTRY_OUTPUT_PATH + i + ".txt", true));
                else
                    bw = new BufferedWriter(new FileWriter(Config.MODEL_ENTRY_OUTPUT_PATH + i + ".txt"));


                StringBuffer sbParameter = new StringBuffer("");
                for (ParameterContent parameterContent : modelEntry.getParameterContentList()) {
                    sbParameter.append(parameterContent.getAbsStringRep());
                    sbParameter.append(" ");
                }

                String output = "";
                try {
                    output = "ID:" + count;
                    output = output.trim() + " +++$+++ " + modelEntry.getMethodCallEntity().getMethodDeclarationEntity().getName() + ":" + modelEntry.getMethodCallEntity().getReceiverQualifiedName();
                    output = output.trim() + " " + sbParameter.toString();
                    output = output.trim() + " +++$+++ " + modelEntry.getMethodCallEntity().getReceiverQualifiedName();
                    output = output.trim() + " +++$+++ " + modelEntry.getPrevMethod();
                    output = output.trim() + " " + modelEntry.getPrevMethodArg();
                    output = output.trim() + " " + modelEntry.getLineContent();
                    output = output.trim() + " " + modelEntry.getNeighborList();


                } catch (Exception nullex) {
                    continue;
                }

                bw.write(output.trim());
                bw.newLine();
                bwLocal.write(output.trim());
                bwLocal.newLine();
                count++;
                bw.close();
            }
            bwLocal.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return count;
    }


    private static void writeSlpCotext(List<List<String>> slp_context, String subjectSystemName) {
        System.out.println("Writting the context of SLP at " + Config.SLP_DATA_OUTPUT_PATH);
        System.out.println("Individual SLP dataset can be found at " + Config.INDIVIDUAL_FILE_PATH + subjectSystemName + "_slp.txt");
        try {
            BufferedWriter bw;

            Random rand = new Random();

            BufferedWriter bwLocal = new BufferedWriter(new FileWriter(new File(Config.INDIVIDUAL_FILE_PATH + subjectSystemName + "_slp.txt")));
            for (List<String> context : slp_context) {
                int i = rand.nextInt(10) + 1;
                if (new File(Config.SLP_DATA_OUTPUT_PATH).exists())
                    bw = new BufferedWriter(new FileWriter(Config.SLP_DATA_OUTPUT_PATH + i + ".txt", true));
                else
                    bw = new BufferedWriter(new FileWriter(Config.SLP_DATA_OUTPUT_PATH + i + ".txt"));

                for (String eachContext : context) {
                    bw.write(eachContext.trim() + " ");
                    bwLocal.write(eachContext.trim() + " ");
                }
                bw.newLine();
                bwLocal.newLine();
                bw.close();
            }
            bwLocal.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void writeSlamcCotext(List<SLAMCContext> slamc_context, String subjectSystemName) {
        System.out.println("Writting the context of SLAMC at " + Config.SLAMC_DATA_OUTPUT_PATH);
        System.out.println("Individual SLAMC dataset can be found at " + Config.INDIVIDUAL_FILE_PATH + subjectSystemName + "_slamc.txt");
        try {
            BufferedWriter bw;

            Random rand = new Random();

            BufferedWriter bwLocal = new BufferedWriter(new FileWriter(new File(Config.INDIVIDUAL_FILE_PATH + subjectSystemName + "_slamc.txt")));
            for (SLAMCContext context : slamc_context) {
                int i = rand.nextInt(10) + 1;
                if (new File(Config.SLAMC_DATA_OUTPUT_PATH).exists())
                    bw = new BufferedWriter(new FileWriter(Config.SLAMC_DATA_OUTPUT_PATH + i + ".txt", true));
                else
                    bw = new BufferedWriter(new FileWriter(Config.SLAMC_DATA_OUTPUT_PATH + i + ".txt"));

                try {
                    bw.write(context.getMethodCallExp().trim() + " ");
                    bwLocal.write(context.getMethodCallExp().trim() + " ");
                    for (String parameter : context.getParameters()) {
                        bw.write(parameter.trim() + " ");
                        bwLocal.write(parameter.trim() + " ");
                    }


                    bw.write("+++$+++ " + context.getContext());
                    bwLocal.write("+++$+++ " + context.getContext());
                    bw.newLine();
                    bwLocal.newLine();
                }
                catch (Exception ex)
                {
                    continue;
                }

                bw.close();
            }
            bwLocal.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}



/*bw.write("MethodName:ReceiverType> "+modelEntry.getMethodCallEntity().getMethodDeclarationEntity().getName()+":"+modelEntry.getMethodCallEntity().getReceiverQualifiedName());
				bw.newLine();
				bw.write("ReceiverType> "+modelEntry.getMethodCallEntity().getReceiverQualifiedName());
				bw.newLine();
				bw.write("ParameterList> "+sbParameter.toString());
				bw.newLine();
				bw.write("Previous Methods> "+modelEntry.getPrevMethod());
				bw.newLine();
				bw.write("SorroundingContext> "+modelEntry.getNeighborList());
				bw.newLine();
				bw.write("LineContext> "+modelEntry.getLineContent());
				bw.newLine();*/
