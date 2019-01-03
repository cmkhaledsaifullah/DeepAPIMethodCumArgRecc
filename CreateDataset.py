import tensorflow as tf
import config
from keras.preprocessing import text, sequence
import numpy as np

def indexesFromSentence(lang, sentence):
    numVec = []
    for word in sentence.split(' '):
        if(word in lang.word2index):
            numVec.append(lang.word2index[word])
        else:
            numVec.append(config.UNK_token)

    return numVec


def tensorFromSentence(lang, sentence, decoder_input_status):
    indexes = []
    if decoder_input_status == True:
        indexes.append(config.SOS_token)
        indexes.extend(indexesFromSentence(lang, sentence))
        #print(indexes)
    else:
        indexes = indexesFromSentence(lang, sentence)
        indexes.append(config.EOS_token)
    #print(indexes)
    return indexes


def tensorsFromPair(pair,input_lang,output_lang):
    encoder_input = tensorFromSentence(input_lang, pair[0].strip(),False)
    decoder_output = tensorFromSentence(output_lang, pair[1].strip(),False)
    decoder_input = tensorFromSentence(output_lang, pair[1].strip(), True)
    return (encoder_input, decoder_input,decoder_output)



def createOneVector(sequence,MAX_LENGTH,vocab_size):
    oneVec =np.zeros((MAX_LENGTH,vocab_size))
    if(len(sequence) > MAX_LENGTH):
        del sequence[MAX_LENGTH:]
    iter = 0
    for token in sequence:
        oneVec[iter][token] = 1
        iter = iter+1

    return oneVec


def datasetCreation(n_iters,pairs,input_lang,output_lang, MAX_LENGTH_Input,MAX_LENGTH_Output,tar_vocab):
    #print("Creating Corpus.....")
    training_pairs = []
    for i in range(n_iters):
        training_pairs.append(tensorsFromPair(pairs[i], input_lang, output_lang))

    #print("Creating One hot vector for decoder input...")
    encoder_input = []
    decoder_input = []
    decoder_output = []
    for iter in range(1, n_iters + 1):
        training_pair = training_pairs[iter - 1]
        encoder_input.append(training_pair[0])
        decoder_input.append(training_pair[1])
        decoder_output.append(training_pair[2])

    #print("Padding the encoder input, decoder input and decoder output.......")
    X_Input = sequence.pad_sequences(encoder_input, maxlen=MAX_LENGTH_Input, padding='post', truncating='post')
    Y_Input = sequence.pad_sequences(decoder_input, maxlen=MAX_LENGTH_Output,padding='post', truncating='post')
    Y_Output = sequence.pad_sequences(decoder_output, maxlen=MAX_LENGTH_Output, padding='post', truncating='post')

    sess = tf.InteractiveSession()

    #print("Creating One Hot Vector for decoder input and decoder output.......")
    Target_Input = np.empty(shape=(len(pairs), MAX_LENGTH_Output, tar_vocab), dtype=np.int8)
    Target_Output = np.empty(shape=(len(pairs), MAX_LENGTH_Output, tar_vocab), dtype=np.int8)

    #print("Populating decoder input vector.........")
    index = 0
    for each_label in Y_Input:
        if len(each_label) > MAX_LENGTH_Output:
            del each_label[MAX_LENGTH_Output:]

        a = np.zeros(shape=(MAX_LENGTH_Output, tar_vocab))
        i = 0
        for each_number in each_label:
            a[i, each_number] = 1
            i = i + 1

        Target_Input[index] = a
        index = index + 1

    #print("Populating decoder output vector.........")
    index = 0
    for each_label in Y_Output:
        if len(each_label) > MAX_LENGTH_Output:
            del each_label[MAX_LENGTH_Output:]

        a = np.zeros(shape=(MAX_LENGTH_Output,tar_vocab))
        i = 0
        for each_number in each_label:
            a[i,each_number] = 1
            i = i+1
        Target_Output[index] = a
        index = index+1

    #print("Data Preprocessing Done")
    return X_Input,Target_Input,Target_Output
