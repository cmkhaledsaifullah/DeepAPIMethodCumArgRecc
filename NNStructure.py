from keras.models import Model
from keras.layers import Dense, Embedding, Input
from keras.layers import LSTM, Bidirectional, Dropout, TimeDistributed, concatenate, RepeatVector,GRU
from AttentionMechanism import AttentionL
import tensorflow as tf
from keras.layers import Lambda
from keras import backend as K




def trainModel(MAX_LENGTH_Input, vocab_size_input, embedding_width, hidden_size, MAX_LENGTH_Output, vocab_size_output):
    encoder_input = Input(shape=(MAX_LENGTH_Input,), name='Encoder_Input')
    # print(encoder_input)

    embedded = Embedding(input_dim=vocab_size_input, output_dim=embedding_width, name='Embedding_Layer')(encoder_input)
    # print(embedded)

    encoder = Bidirectional(LSTM(units=hidden_size, return_sequences=True, dropout=0.25, recurrent_dropout=0.25),
                            input_shape=(MAX_LENGTH_Input, embedding_width), name='Encoder_Layer')(embedded)
    # print(encoder)

    attention = AttentionL(MAX_LENGTH_Input, name='Attention_Layer')(encoder)
    # print(attention)

    attention = RepeatVector(MAX_LENGTH_Output, name='Repeat_Vector')(attention)
    # print(attention)

    decoder_input = Input(shape=(MAX_LENGTH_Output, vocab_size_output), name='Decoder_Input')
    # print(decoder_input)

    merge = concatenate([attention, decoder_input], axis=2, name='Merge_Vector')
    # print(merge)

    decoder = Bidirectional(LSTM(units=hidden_size, return_sequences=True), name='Decoder_Layer')(merge)
    # print(decoder)

    output = TimeDistributed(Dense(vocab_size_output, activation="softmax"), name='Decoder_Output')(decoder)
    # print(output)

    # x = Dense(hidden_size, activation="relu")(x)
    # x = Dropout(0.25)(x)
    # output = Dense(MAX_LENGTH_Output, activation="softmax")(decoder)
    model = Model(inputs=[encoder_input, decoder_input], outputs=output)

    return model


def encoder(MAX_LENGTH_Input, vocab_size_input, embedding_width, hidden_size):
    encoder_input = Input(shape=(MAX_LENGTH_Input,), name='Encoder_Input')
    # print(encoder_input)

    embedded = Embedding(input_dim=vocab_size_input, output_dim=embedding_width, name='Embedding_Layer')(encoder_input)
    # print(embedded)

    encoder= Bidirectional(LSTM(units=hidden_size, return_sequences=True),
        input_shape=(MAX_LENGTH_Input, embedding_width), name='Encoder_Layer')(embedded)
    # print(encoder)

    #state_h = concatenate([forward_h, backward_h])
    #state_c = concatenate([forward_c, backward_c])
    #encoder_states = [state_h, state_c]

    model = Model(inputs=encoder_input, outputs=encoder)

    return model


def decoder(MAX_LENGTH_Input, hidden_size,MAX_LENGTH_Output, vocab_size_output):

    encoder_output = Input(shape=(MAX_LENGTH_Input,hidden_size*2), name='Encoder_Output')

    attention = AttentionL(MAX_LENGTH_Input, name='Attention_Layer')(encoder_output)
    #print(attention)

    attention = RepeatVector(1, name='Repeat_Vector')(attention)
    # print(attention)

    decoder_input = Input(shape=(1, vocab_size_output), name='Decoder_Input')
    #print(decoder_input)

    merge = concatenate([decoder_input, attention], axis=2, name='Merge_Vector')
    # print(merge)

    decoder = Bidirectional(LSTM(units=hidden_size, return_sequences=True), name='Decoder_Layer')(merge)
    # print(decoder)

    output = Dense(vocab_size_output, activation="softmax",name='Decoder_Output')(decoder)
    # print(output)

    model = Model(inputs=[encoder_output, decoder_input], outputs=output)

    return model



def trainModelLSTM(MAX_LENGTH_Input, vocab_size_input, embedding_width, hidden_size, MAX_LENGTH_Output, vocab_size_output):
    encoder_input = Input(shape=(MAX_LENGTH_Input,), name='Encoder_Input')
    # print(encoder_input)

    embedded = Embedding(input_dim=vocab_size_input, output_dim=embedding_width, name='Embedding_Layer')(encoder_input)
    # print(embedded)

    encoder = LSTM(units=hidden_size, return_sequences=True, dropout=0.25, recurrent_dropout=0.25,
                            input_shape=(MAX_LENGTH_Input, embedding_width), name='Encoder_Layer')(embedded)
    # print(encoder)

    attention = AttentionL(MAX_LENGTH_Input, name='Attention_Layer')(encoder)
    # print(attention)

    attention = RepeatVector(MAX_LENGTH_Output, name='Repeat_Vector')(attention)
    # print(attention)

    decoder_input = Input(shape=(MAX_LENGTH_Output, vocab_size_output), name='Decoder_Input')
    # print(decoder_input)

    merge = concatenate([attention, decoder_input], axis=2, name='Merge_Vector')
    # print(merge)

    decoder = LSTM(units=hidden_size, return_sequences=True, name='Decoder_Layer')(merge)
    # print(decoder)

    output = TimeDistributed(Dense(vocab_size_output, activation="softmax"), name='Decoder_Output')(decoder)
    # print(output)

    # x = Dense(hidden_size, activation="relu")(x)
    # x = Dropout(0.25)(x)
    # output = Dense(MAX_LENGTH_Output, activation="softmax")(decoder)
    model = Model(inputs=[encoder_input, decoder_input], outputs=output)

    return model


def encoderLSTM(MAX_LENGTH_Input, vocab_size_input, embedding_width, hidden_size):
    encoder_input = Input(shape=(MAX_LENGTH_Input,), name='Encoder_Input')
    # print(encoder_input)

    embedded = Embedding(input_dim=vocab_size_input, output_dim=embedding_width, name='Embedding_Layer')(encoder_input)
    # print(embedded)

    encoder= LSTM(units=hidden_size, return_sequences=True,
        input_shape=(MAX_LENGTH_Input, embedding_width), name='Encoder_Layer')(embedded)
    # print(encoder)

    #state_h = concatenate([forward_h, backward_h])
    #state_c = concatenate([forward_c, backward_c])
    #encoder_states = [state_h, state_c]

    model = Model(inputs=encoder_input, outputs=encoder)

    return model


def decoderLSTM(MAX_LENGTH_Input, hidden_size,MAX_LENGTH_Output, vocab_size_output):

    encoder_output = Input(shape=(MAX_LENGTH_Input,hidden_size), name='Encoder_Output')

    attention = AttentionL(MAX_LENGTH_Input, name='Attention_Layer')(encoder_output)
    #print(attention)

    attention = RepeatVector(1, name='Repeat_Vector')(attention)
    # print(attention)

    decoder_input = Input(shape=(1, vocab_size_output), name='Decoder_Input')
    #print(decoder_input)

    merge = concatenate([decoder_input, attention], axis=2, name='Merge_Vector')
    # print(merge)

    decoder = LSTM(units=hidden_size, return_sequences=True, name='Decoder_Layer')(merge)
    # print(decoder)

    output = Dense(vocab_size_output, activation="softmax",name='Decoder_Output')(decoder)
    # print(output)

    model = Model(inputs=[encoder_output, decoder_input], outputs=output)

    return model



def trainModelRNN(MAX_LENGTH_Input, vocab_size_input, embedding_width, hidden_size, MAX_LENGTH_Output, vocab_size_output):
    encoder_input = Input(shape=(MAX_LENGTH_Input,), name='Encoder_Input')
    # print(encoder_input)

    embedded = Embedding(input_dim=vocab_size_input, output_dim=embedding_width, name='Embedding_Layer')(encoder_input)
    # print(embedded)

    encoder = GRU(units=hidden_size, return_sequences=True, dropout=0.25, recurrent_dropout=0.25,
                            input_shape=(MAX_LENGTH_Input,hidden_size), name='Encoder_Layer')(embedded)
    # print(encoder)

    attention = AttentionL(MAX_LENGTH_Input, name='Attention_Layer')(encoder)
    # print(attention)

    attention = RepeatVector(MAX_LENGTH_Output, name='Repeat_Vector')(attention)
    # print(attention)

    decoder_input = Input(shape=(MAX_LENGTH_Output, vocab_size_output), name='Decoder_Input')
    # print(decoder_input)

    merge = concatenate([attention, decoder_input], axis=2, name='Merge_Vector')
    # print(merge)

    decoder = GRU(units=hidden_size, return_sequences=True, name='Decoder_Layer')(merge)
    # print(decoder)

    output = TimeDistributed(Dense(vocab_size_output, activation="softmax"), name='Decoder_Output')(decoder)
    # print(output)

    # x = Dense(hidden_size, activation="relu")(x)
    # x = Dropout(0.25)(x)
    # output = Dense(MAX_LENGTH_Output, activation="softmax")(decoder)
    model = Model(inputs=[encoder_input, decoder_input], outputs=output)

    return model


def encoderRNN(MAX_LENGTH_Input, vocab_size_input, embedding_width, hidden_size):
    encoder_input = Input(shape=(MAX_LENGTH_Input,), name='Encoder_Input')
    # print(encoder_input)

    embedded = Embedding(input_dim=vocab_size_input, output_dim=embedding_width, name='Embedding_Layer')(encoder_input)
    # print(embedded)

    encoder= GRU(units=hidden_size, return_sequences=True,
        input_shape=(MAX_LENGTH_Input, embedding_width), name='Encoder_Layer')(embedded)
    # print(encoder)

    #state_h = concatenate([forward_h, backward_h])
    #state_c = concatenate([forward_c, backward_c])
    #encoder_states = [state_h, state_c]

    model = Model(inputs=encoder_input, outputs=encoder)

    return model


def decoderRNN(MAX_LENGTH_Input, hidden_size,MAX_LENGTH_Output, vocab_size_output):

    encoder_output = Input(shape=(MAX_LENGTH_Input,hidden_size), name='Encoder_Output')

    attention = AttentionL(MAX_LENGTH_Input, name='Attention_Layer')(encoder_output)
    #print(attention)

    attention = RepeatVector(1, name='Repeat_Vector')(attention)
    # print(attention)

    decoder_input = Input(shape=(1, vocab_size_output), name='Decoder_Input')
    #print(decoder_input)

    merge = concatenate([decoder_input, attention], axis=2, name='Merge_Vector')
    # print(merge)

    decoder = GRU(units=hidden_size, return_sequences=True, name='Decoder_Layer')(merge)
    # print(decoder)

    output = Dense(vocab_size_output, activation="softmax",name='Decoder_Output')(decoder)
    # print(output)

    model = Model(inputs=[encoder_output, decoder_input], outputs=output)

    return model


def trainNoTeacher(MAX_LENGTH_Input, vocab_size_input, embedding_width, hidden_size, MAX_LENGTH_Output, vocab_size_output):
    encoder_input = Input(shape=(MAX_LENGTH_Input,), name='Encoder_Input')
    # print(encoder_input)

    embedded = Embedding(input_dim=vocab_size_input, output_dim=embedding_width, name='Embedding_Layer')(encoder_input)
    # print(embedded)

    encoder = Bidirectional(LSTM(units=hidden_size, return_sequences=True, dropout=0.25, recurrent_dropout=0.25),
                            input_shape=(MAX_LENGTH_Input, embedding_width), name='Encoder_Layer')(embedded)
    # print(encoder)

    attention = AttentionL(MAX_LENGTH_Input, name='Attention_Layer')(encoder)
    # print(attention)

    attention = RepeatVector(1, name='Repeat_Vector')(attention)
    # print(attention)

    decoder_input = Input(shape=(1, vocab_size_output), name='Decoder_Input')
    # print(decoder_input)

    merge = concatenate([attention, decoder_input], axis=2, name='Merge_Vector')
    # print(merge)

    decoder = Bidirectional(LSTM(units=hidden_size, return_sequences=True), name='Decoder_Layer')(merge)
    # print(decoder)

    output = Dense(vocab_size_output, activation="softmax", name='Decoder_Output')(decoder)
    # print(output)

    all_outputs = []
    inputs = decoder_input
    for _ in range(MAX_LENGTH_Output):
        attention = AttentionL(MAX_LENGTH_Input)(encoder)
        # print(attention)

        attention = RepeatVector(1)(attention)
        # print(attention)


        merge = concatenate([attention, inputs], axis=2)
        # print(merge)

        decoder = Bidirectional(LSTM(units=hidden_size, return_sequences=True))(merge)
        # print(decoder)

        output = Dense(vocab_size_output, activation="softmax")(decoder)
        # print(output)

        all_outputs.append(output)
        # Reinject the outputs as inputs for the next loop iteration
        # as well as update the states
        inputs = output

    # Concatenate all predictions
    decoder_outputs = Lambda(lambda x: K.concatenate(x, axis=1))(all_outputs)

    model = Model(inputs=[encoder_input, decoder_input], outputs=decoder_outputs)

    return model
