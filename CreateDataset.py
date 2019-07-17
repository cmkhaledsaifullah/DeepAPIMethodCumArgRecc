import config
from keras.preprocessing import sequence

def indexesFromSentence(lang, sentence):
    numVec = []
    for word in sentence.split(' '):
        if word.strip() == '':
            continue
        if(word in lang.word2index):
            numVec.append(lang.word2index[word])
        else:
            numVec.append(config.UNK_token)

    return numVec


def tensorFromSentence(lang, sentence):
    indexes = indexesFromSentence(lang, sentence)
    indexes.append(config.EOS_token)
    return indexes


def tensorsFromPair(pair,input_lang,output_lang):
    encoder_input = tensorFromSentence(input_lang, pair[2].strip())
    decoder_output = tensorFromSentence(output_lang, pair[1].strip())
    return (encoder_input,decoder_output)




def datasetCreation(n_iters,pairs,input_lang,output_lang, MAX_LENGTH_Input,MAX_LENGTH_Output):
    training_pairs = []
    for i in range(n_iters):
        training_pairs.append(tensorsFromPair(pairs[i], input_lang, output_lang))

    encoder_input = []
    decoder_output = []
    for iter in range(1, n_iters + 1):
        training_pair = training_pairs[iter - 1]
        encoder_input.append(training_pair[0])
        decoder_output.append(training_pair[1])


    X_Input = sequence.pad_sequences(encoder_input,
                                     maxlen=MAX_LENGTH_Input,
                                     padding='post',
                                     truncating='post',
                                     value=config.PADDED_Token)
    Y_Output = sequence.pad_sequences(decoder_output,
                                      maxlen=MAX_LENGTH_Output,
                                      padding='post',
                                      truncating='post',
                                      value=config.PADDED_Token)

    return X_Input, Y_Output

