import config,os
import tensorflow as tf
from DataPreprocessing import Vocab,Lang
from Operation import Training,Inferring,Testing


config.init()

#GPU ID for the machine wher GPU is available.
if tf.test.is_gpu_available():
    os.environ["CUDA_VISIBLE_DEVICES"] = config.gpu_id

input_vocab_size = config.MAX_VOCAB_SIZE_INPUT+ config.EOS_token+1
output_vocab_size = config.MAX_VOCAB_SIZE_OUTPUT+ config.EOS_token+1

input_lang = Lang('Context')
output_lang = Lang('Label')

var = input("Enter one of the modes: \n"
            " train : To train the model \n"
            " test: To test the model \n"
            " train-test: To train and then test \n"
            " infer: To see result for a single instance \n")

if var == 'train':

    training = Training(input_vocab_size = input_vocab_size,
                        output_vocab_size = output_vocab_size)

    vocab_check = input("Are you going to Save the damca_vocabulary(y/n): ")

    if 'y' in vocab_check:
        training.train(is_save_vocabulary=True)

    else:
        training.train(is_save_vocabulary=False)


elif var == 'test':
    project_settings = 0
    while project_settings != '1' and project_settings != '2':
        project_settings = input("Enter which settings you wanna test?:\n"
                                 " 1. Intra-Project Settings\n"
                                 " 2. Cross-Project Settings\n")


    print('Loading Input Vocabulary.....')
    input_lang = Vocab.load_vocabulary(Vocab,
                                       datapath = config.input_vocab_file_path,
                                       langName = 'Context',
                                       max_size = input_vocab_size)
    print('Loading Output Vocabulary....')
    output_lang = Vocab.load_vocabulary(Vocab,
                                        datapath = config.output_vocab_file_path,
                                        langName = 'Label',
                                        max_size = output_vocab_size)

    print("Implementing Tensorflow Estimator Version.....")
    testing = Testing(input_vocab_size = input_vocab_size,
                        output_vocab_size = output_vocab_size,
                        input_lang = input_lang,
                        output_lang = output_lang)
    testing.test(projectSetting=project_settings)


elif var == 'infer':
    print('Loading Input Vocabulary.....')
    input_lang = Vocab.load_vocabulary(Vocab,
                                       datapath = config.input_vocab_file_path,
                                       langName = 'Context',
                                       max_size = input_vocab_size)
    print('Loading Output Vocabulary....')
    output_lang = Vocab.load_vocabulary(Vocab,
                                        datapath = config.output_vocab_file_path,
                                        langName = 'Label',
                                        max_size = output_vocab_size)

    seq_input = input("Please enter the input in the following format: \n"
                      " ID +++$+++ <label Sequence> +++$+++ <reciever type> +++$+++ <context sequence> \n")

    inferring = Inferring(input_vocab_size=input_vocab_size,
                          output_vocab_size=output_vocab_size,
                          input_lang=input_lang,
                          output_lang=output_lang,
                          input_seq=seq_input)


    inferring.infer()







