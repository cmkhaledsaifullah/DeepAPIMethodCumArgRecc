import tensorflow as tf,config

config.init()

def BiLSTM(units,MAX_LENGTH_Input,embedding_width):
    if tf.test.is_gpu_available():
        return tf.keras.layers.Bidirectional(tf.keras.layers.CuDNNLSTM(num_units=units,
                                                                       bias_regularizer = tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                                      l2=config.l2_regularization),
                                                                       kernel_regularizer= tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                                       l2=config.l2_regularization),
                                                                       activity_regularizer = tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                                          l2=config.l2_regularization),
                                                                       recurrent_regularizer = tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                                           l2=config.l2_regularization),
                                                                       return_sequences=True,
                                                                       return_state = True),
                                             input_shape=(MAX_LENGTH_Input,embedding_width))
    else:
        return tf.keras.layers.Bidirectional(tf.keras.layers.LSTM(num_units=units,
                                                                  bias_regularizer = tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                                 l2=config.l2_regularization),
                                                                  kernel_regularizer= tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                                  l2=config.l2_regularization),
                                                                  activity_regularizer = tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                                     l2=config.l2_regularization),
                                                                  recurrent_regularizer = tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                                      l2=config.l2_regularization),
                                                                  return_sequences=True,
                                                                  return_state = True),
                                             input_shape=(MAX_LENGTH_Input, embedding_width))


class Encoder(tf.keras.Model):
    def __init__(self, vocab_size, max_length, embedding_dim, enc_units, batch_sz):
        super(Encoder, self).__init__()

        #variables, parameters and hyper parameters defination
        self.batch_sz = batch_sz
        self.max_length = max_length
        self.enc_units = enc_units


        #layers definations
        self.embedding = tf.keras.layers.Embedding(input_dim= vocab_size,
                                                   output_dim=embedding_dim,
                                                   mask_zero = True,
                                                   input_length= max_length,
                                                   embeddings_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                      l2=config.l2_regularization))
        self.dropout = tf.keras.layers.Dropout(rate= config.dropout)
        self.BiLSTM = BiLSTM(units = self.enc_units,
                             MAX_LENGTH_Input = max_length,
                             embedding_width = embedding_dim)



    def call(self, x, hidden):
        x = self.embedding(x)
        x = self.dropout(x)
        output,forward_h, forward_c, backward_h, backward_c = self.BiLSTM(x)
        state_h = tf.keras.layers.Concatenate()([forward_h, backward_h])
        state_c = tf.keras.layers.Concatenate()([forward_c, backward_c])
        state = tf.keras.layers.Concatenate()([state_h, state_c])
        return output, state

    def initialize_hidden_state(self):
        initial_state = tf.expand_dims(tf.zeros((self.enc_units)),axis=0)
        return [initial_state]*2




class Decoder(tf.keras.Model):
    def __init__(self, vocab_size, max_length ,embedding_dim, dec_units, batch_sz):
        super(Decoder, self).__init__()

        # variables, parameters and hyper parameters defination
        self.batch_sz = batch_sz
        self.max_length = max_length
        self.dec_units = dec_units

        # layers definations
        self.embedding = tf.keras.layers.Embedding(input_dim= vocab_size,
                                                   output_dim=embedding_dim,
                                                   mask_zero = True,
                                                   input_length= max_length,
                                                   embeddings_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                                      l2=config.l2_regularization))
        self.dropout = tf.keras.layers.Dropout(rate= config.dropout)
        self.BiLSTM = BiLSTM(units = self.enc_units,
                             MAX_LENGTH_Input = max_length,
                             embedding_width = embedding_dim)
        self.fc = tf.keras.layers.Dense(units=vocab_size,
                                        activation="softmax",
                                        bias_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                     l2=config.l2_regularization),
                                        kernel_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                       l2=config.l2_regularization),
                                        activity_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                         l2=config.l2_regularization))

        # used for attention
        self.W1 = tf.keras.layers.Dense(units = self.dec_units,
                                        bias_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                     l2=config.l2_regularization),
                                        kernel_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                       l2=config.l2_regularization),
                                        activity_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                         l2=config.l2_regularization))
        self.W2 = tf.keras.layers.Dense(units = self.dec_units,
                                        bias_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                     l2=config.l2_regularization),
                                        kernel_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                       l2=config.l2_regularization),
                                        activity_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                         l2=config.l2_regularization))
        self.V = tf.keras.layers.Dense(units = 1,
                                        bias_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                     l2=config.l2_regularization),
                                        kernel_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                       l2=config.l2_regularization),
                                        activity_regularizer=tf.keras.regularizers.l1_l2(l1=config.l1_regularization,
                                                                                         l2=config.l2_regularization))

    def call(self, x, hidden, enc_output):
        '''
        Bahdanau attention:
        FC = Fully connected (dense) layer
        EO = Encoder output
        H = hidden state
        X = input to the decoder
        And the pseudo-code:

        score = FC(tanh(FC(EO) + FC(H)))
        attention weights = softmax(score, axis = 1).

        Softmax by default is applied on the last axis but here we want to apply it on the 1st axis,
        since the shape of score is (batch_size, max_length, 1). Max_length is the length of our input.
        Since we are trying to assign a weight to each input, softmax should be applied on that axis.

        context vector = sum(attention weights * EO, axis = 1).

        Same reason as above for choosing axis as 1.

        embedding output = The input to the decoder X is passed through an embedding layer.
        merged vector = concat(embedding output, context vector)

        This merged vector is then given to the Bi-LSTM
        The shapes of all the vectors at each step have been specified in the comments in the code:

        :param x: input to the decoder
        :param hidden: hidden states of encoder
        :param enc_output: output of hidden layers
        :return:
        '''

        # enc_output shape == (batch_size, max_length, hidden_size*2)
        # hidden shape == (batch_size, hidden size*4)
        # hidden_with_time_axis shape == (batch_size, 1, hidden size*4)
        # we are doing this to perform addition to calculate the score
        hidden_with_time_axis = tf.expand_dims(hidden, 1)

        # score shape == (batch_size, max_length, 1)
        # we get 1 at the last axis because we are applying tanh(FC(EO) + FC(H)) to self.V
        score = self.V(tf.nn.tanh(self.W1(enc_output) + self.W2(hidden_with_time_axis)))
        # attention_weights shape == (batch_size, max_length, 1)
        attention_weights = tf.nn.softmax(score, axis=1)
        # context_vector shape after sum == (batch_size, hidden_size*2)
        context_vector = attention_weights * enc_output
        context_vector = tf.reduce_sum(context_vector, axis=1)
        # x shape after passing through embedding == (batch_size, 1, embedding_dim)
        x = self.embedding(x)

        x = self.dropout(x)
        # x = tf.cast(x, dtype= tf.float32)
        # x shape after concatenation == (batch_size, 1, embedding_dim + hidden_size*2)
        x = tf.concat([tf.expand_dims(context_vector, 1), x], axis=-1)

        # passing the concatenated vector to the GRU
        output, forward_h, forward_c, backward_h, backward_c = self.BiLSTM(x)
        state_h = tf.keras.layers.Concatenate()([forward_h, backward_h])
        state_c = tf.keras.layers.Concatenate()([forward_c, backward_c])
        state = tf.keras.layers.Concatenate()([state_h, state_c])

        # output shape == (batch_size, 1, hidden_size*2)
        output = tf.reshape(output, (-1, output.shape[2]))
        # output shape after reshape == (batch_size*1, hidden_size*2)

        output = self.dropout(output)

        x = self.fc(output)
        # x shape == (batch_size * 1, vocab)

        return x, state, attention_weights

    def initialize_hidden_state(self):
        return tf.zeros((self.batch_sz ,self.dec_units))