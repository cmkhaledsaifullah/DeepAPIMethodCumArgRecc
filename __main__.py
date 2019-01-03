import config
import sys
from DataPreprocessing import Dict,Lang
from Operation import Training,Testing,OneTesting
from pathlib import Path


config.init()

input_vocab_size = config.MAX_VOCAB_SIZE_INPUT+ 3
output_vocab_size = config.MAX_VOCAB_SIZE_OUTPUT+ 3

input_lang = Lang()
output_lang = Lang()

var = input("Please enter one of the mode: \n train : To train the model \n test: To test the model \n train-test: To train and then test \n infer: To see result for a single instance \n")

if var == 'train':
    training = Training(input_vocab_size, output_vocab_size)
    vocab_check = input("Are you going to Save the vocabulary(y/n): ")

    if 'y' in vocab_check:
        training.train(True)
        # training.trainNoTeach(True)

    else:
        training.train(False)
        # training.trainNoTeach(False)


elif var == 'test':

    print('Loading Input Vocabulary.....')
    input_lang = Dict.load_vocabulary(Dict, config.input_vocab_file_path, 'Context', input_vocab_size)
    print('Loading Output Vocabulary....')
    output_lang = Dict.load_vocabulary(Dict, config.output_vocab_file_path, 'Label', output_vocab_size)


    model_file = Path(config.model_file_path)
    if model_file.is_file():
        testing = Testing(input_vocab_size,output_vocab_size,input_lang,output_lang)
        testing.test()

    else:
        print("No Model File Found. For Testing we need model file!!!")

elif var == 'infer':
    print('Loading Input Vocabulary.....')
    input_lang = Dict.load_vocabulary(Dict, config.input_vocab_file_path, 'Context', input_vocab_size)
    print('Loading Output Vocabulary....')
    output_lang = Dict.load_vocabulary(Dict, config.output_vocab_file_path, 'Label', output_vocab_size)

    model_file = Path(config.model_file_path)
    if model_file.is_file():

        seq_input = input("Please enter the input in the following format \n ID +++$+++ <label Sequence> +++$+++ <reciever type> +++$+++ <context sequence> \n")
        testing = OneTesting(input_vocab_size, output_vocab_size, input_lang, output_lang,seq_input)
        testing.test()

    else:
        print("No Model File Found. For Testing we need model file!!!")







