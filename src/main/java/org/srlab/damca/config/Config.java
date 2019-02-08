package org.srlab.damca.config;

import java.io.File;

public class Config {
    //Controlling parameters (Compulsary)
    public static final Boolean IS_DAMCA_COLLECT = Boolean.FALSE;
    public static final Boolean IS_SLP_COLLECT = Boolean.FALSE;
    public static final Boolean IS_SLAMC_COLLECT = Boolean.TRUE;

    //Path to the project (Compulsary)
    private static final String ROOT_FOLDER = "/home/khaledkucse/Project/backup";

    //Path to the input project folder (optional)
    public static final String ROOT_PATH = ROOT_FOLDER + "/dcc_subject_systems";

    //Path to the log file created while program is running (optional)
    public static final String LOG_FILE_PATH = ROOT_FOLDER + "/dcc_log.txt";

    //Path to the folder where dataset files for individual projects are stored (optional)
    public static final String INDIVIDUAL_FILE_PATH = ROOT_FOLDER + "/dcc_models/";

    //Path to the folder where dataset files of DAMCA system are stored (optional)
    public static final String MODEL_ENTRY_OUTPUT_PATH = ROOT_FOLDER + "/dcc_damca_dataset/";

    //Path to the folder where dataset files of SLP system are stored (optional)
    public static final String SLP_DATA_OUTPUT_PATH = ROOT_FOLDER + "/dcc_slp_dataset/";

    //Path to the folder where dataset files of SLAMC system are stored (optional)
    public static final String SLAMC_DATA_OUTPUT_PATH = ROOT_FOLDER + "/dcc_slamc_dataset/";

    //Extension of the files need to be parsed. (optional)
    public static final String[] FILE_EXTENSIONS = {".java"};

    //Libraries that are considered while collecting AST information. (Optional)
    public static final String FRAMEWORKS[] = {"javax.swing.","java.awt.","java.util.","java.io.","java.math.","java.net.","java.nio.","java.lang."};
    //public static final String FRAMEWORKS[] = {"javax.swing."};



    //Parameters for post processing:

    //Name of the DAMCA Recommender Result file
    public static final String OUTPUT_NAME = "eclipse_bilstm_c3_attn_bs_top_10_inter_6.txt";

    //Path of the DAMCA Recommender Result file
    public static final String OUTPUT_PATH = ROOT_FOLDER +"/damca_result" + File.separator + OUTPUT_NAME;

    //Path of the all subject systems
    public static final String REPOSITORY_PATH = ROOT_FOLDER +"/51_subject_systems" +File.separator;

    //Path of the final result file
    public static final String FINAL_OUTPUT_PATH = ROOT_FOLDER +"/damca_result_final" + File.separator + OUTPUT_NAME;


    //Internal parameter (no need to change the value)
    public static final String REPOSITORY_REVISION_PATH = ROOT_PATH + File.separator + OUTPUT_NAME + "_revisions";
    public static final String EXTERNAL_DEPENDENCY_PATH = ROOT_PATH + File.separator + OUTPUT_NAME + "_dependencies";

    //Internal method that is resposible to check whether the method call belongs to the libraries stated at FRAMEWORKS[](no need to change the value)
    public static boolean isInteresting(String qualifiedTypeName) {
        for (String prefix : FRAMEWORKS) {
            if (qualifiedTypeName.startsWith(prefix)) return true;
        }
        return false;
    }

    //Interal method(no need to change the value)
    public static String getRepositoryFolderName() {
        File file = new File(Config.REPOSITORY_PATH);
        return file.getName();
    }
}
