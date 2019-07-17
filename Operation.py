import config,CreateDataset,os,Evaluation,time, numpy as np, tensorflow as tf,operator
from DataPreprocessing import Vocab,Lang
from NNStructure import Encoder,Decoder
from DataPostProcess import DataPostProcessing

class Training:

    def __init__(self, input_vocab_size,output_vocab_size):

        tf.compat.v1.enable_eager_execution()

        #Data Preprocessing
        self.input_vocab_size = input_vocab_size
        self.output_vocab_size = output_vocab_size
        self.input_lang = Lang('Context')
        self.output_lang = Lang('Label')
        self.dictonary = Vocab()

        # Neural Networks model and optimizer
        self.encoder = Encoder(vocab_size=self.input_vocab_size,
                               max_length=config.MAX_LENGTH_Input,
                               embedding_dim=config.embedding_width,
                               enc_units=config.hidden_size,
                               batch_sz=config.batch_size)
        self.decoder = Decoder(vocab_size=self.output_vocab_size,
                               max_length=config.MAX_LENGTH_Output,
                               embedding_dim=config.embedding_width, dec_units=config.hidden_size,
                               batch_sz=config.batch_size,
                               test_status=False)
        self.optimizer = tf.compat.v1.train.AdamOptimizer(learning_rate=config.learning_Rate)

        config.init()

    def train(self,is_save_vocabulary):
        '''
        Step 1: Preprocess the raw data:
        1.1 create dictonary/damca_vocabulary from the text, make two language one for the input(context,previous code tokens) and other for the output(method call sequence)
        1.2 Resize the dictonary/damca_vocabulary based on the frequency of the word
        1.3 Convert textual sequence into the numeric tensors based on the id of the the word in the dictonary

        Step 2: Define Buffer size and create batched damca_dataset , loss function and checkpoints

        Step 3: Training uptu the number of epoch


        :param is_save_vocabulary: Boolean value representing whether the user wants to save damca_vocabulary/dictonary
        :return:
        '''

        print('Loading Training data....')
        train_pairs=[]
        #when path of training damca_dataset is a file
        if(os.path.isfile(config.train_dataset_file_path)):
            self.input_lang, self.output_lang, train_pairs = self.dictonary.prepareData(lang1='Context',
                                                                                        lang2='Label',
                                                                                        datasetfilepath=config.train_dataset_file_path)
        #when path of training damca_dataset is a folder
        elif(os.path.isdir(config.train_dataset_file_path)):
            for each_path in os.listdir(config.train_dataset_file_path):
                #print(os.path.join(config.train_dataset_file_path,path))
                if each_path.__contains__('readme.md') or each_path.__contains__('.ipynb_checkpoints'):
                    continue
                temp_input_lang, temp_output_lang, temp_train_pairs = self.dictonary.prepareData(lang1='Context',
                                                                                                 lang2='Label',
                                                                                                 datasetfilepath=os.path.join(config.train_dataset_file_path,each_path))
                train_pairs.extend(temp_train_pairs)
                self.input_lang.appendLang(temp_input_lang)
                self.output_lang.appendLang(temp_output_lang)


        if is_save_vocabulary == True:
            print('Saving Input and Output Vocabulary into the Disk....')
            self.input_lang = self.dictonary.save_vocabulary(vocab_path=config.input_vocab_file_path,
                                                             lang=self.input_lang,
                                                             max_size=config.MAX_VOCAB_SIZE_INPUT)
            self.output_lang = self.dictonary.save_vocabulary(vocab_path=config.output_vocab_file_path,
                                                              lang=self.output_lang,
                                                              max_size=config.MAX_VOCAB_SIZE_OUTPUT)

        else:
            print("Resizing Vocabulary ....")
            self.input_lang = self.dictonary.vocabResize(lang=self.input_lang,
                                                         max_size=config.MAX_VOCAB_SIZE_INPUT)
            self.output_lang = self.dictonary.vocabResize(lang=self.output_lang,
                                                          max_size=config.MAX_VOCAB_SIZE_OUTPUT)

        encoder_input,  decoder_output = CreateDataset.datasetCreation(n_iters=len(train_pairs),
                                                                       pairs=train_pairs,
                                                                       input_lang=self.input_lang,
                                                                       output_lang=self.output_lang,
                                                                       MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                                                                       MAX_LENGTH_Output=config.MAX_LENGTH_Output)


        #Define Buffer size. And then shuffle them based on number of batch(N_BATCH) and create batched damca_dataset.
        BUFFER_SIZE = len(encoder_input)
        train_size = int(0.9 * BUFFER_SIZE)
        val_size = int(0.1 * BUFFER_SIZE)
        N_BATCH = BUFFER_SIZE // config.batch_size
        dataset = tf.data.Dataset.from_tensor_slices((encoder_input, decoder_output)).shuffle(BUFFER_SIZE)
        train_dataset = dataset.take(train_size)
        val_dataset = dataset.skip(val_size)
        train_dataset = train_dataset.batch(config.batch_size, drop_remainder=True)


        #Define loss function: Sparse softmax cross entropy
        def loss_function(actual, pred):
            #mask return zero for the padded token in the actual label
            mask = 1 - np.equal(actual, config.PADDED_Token)
            loss_ = tf.nn.sparse_softmax_cross_entropy_with_logits(labels=actual, logits=pred)

            # Average over actual sequence lengths.
            cross_entropy = tf.reduce_sum(loss_, axis=0)
            actual_mask = tf.reduce_sum(mask)
            actual_mask = tf.cast(actual_mask, tf.float32)
            if actual_mask > 0:
                cross_entropy /= actual_mask
            else:
                cross_entropy = actual_mask

            return cross_entropy


        #The checkpoint for the model to be saved after fixed iteration
        checkpoint_prefix = os.path.join(config.checkpoints_folder_path, "ckpt")
        checkpoint = tf.train.Checkpoint(optimizer=self.optimizer,
                                         encoder=self.encoder,
                                         decoder=self.decoder)


        # Training
        hidden = self.encoder.initialize_hidden_state()
        for epoch in range(config.epochs):
            start = time.time()
            total_loss = 0

            for (batch, (enc_inp,dec_out)) in enumerate(train_dataset):
                loss = 0
                with tf.GradientTape() as tape:
                    enc_output, enc_hidden = self.encoder(enc_inp, hidden)

                    dec_hidden = enc_hidden

                    dec_input = tf.expand_dims([config.SOS_token ]* config.batch_size, 1)
                    # Teacher forcing - feeding the target as the next input
                    for t in range(0, dec_out.shape[1]):
                        # passing enc_output to the decoder
                        predictions, dec_hidden, _ = self.decoder(dec_input, dec_hidden, enc_output)


                        loss += loss_function(dec_out[:, t], predictions)


                        # using teacher forcing
                        dec_input = tf.expand_dims(dec_out[:, t], 1)

                batch_loss = (loss / int(dec_out.shape[1]))

                total_loss += batch_loss

                variables = self.encoder.variables + self.decoder.variables

                gradients = tape.gradient(loss, variables)

                self.optimizer.apply_gradients(zip(gradients, variables))


                if batch % 100 == 0:
                    print('Epoch {} Batch {} Loss {:.4f}'.format(epoch + 1,
                                                                 batch,
                                                                 batch_loss.numpy()))
            # saving (checkpoint) the model every 2 epochs
            if (epoch + 1) % 20 == 0:
                checkpoint.save(file_prefix=checkpoint_prefix)

            print('Epoch {} Loss {:.4f}'.format(epoch + 1,
                                                total_loss / N_BATCH))
            print('Time taken for 1 epoch {} sec\n'.format(time.time() - start))


class Testing:
    def __init__(self, input_vocab_size,output_vocab_size,input_lang,output_lang):
        config.init()
        tf.compat.v1.enable_eager_execution()
        self.input_vocab_size = input_vocab_size
        self.output_vocab_size = output_vocab_size
        self.input_lang = input_lang
        self.output_lang = output_lang

        # Neural Networks model and optimizer
        self.encoder = Encoder(vocab_size=self.input_vocab_size,
                               max_length=config.MAX_LENGTH_Input,
                               embedding_dim=config.embedding_width,
                               enc_units=config.hidden_size,
                               batch_sz=config.batch_size)
        self.decoder = Decoder(vocab_size=self.output_vocab_size,
                               max_length=config.MAX_LENGTH_Output,
                               embedding_dim=config.embedding_width,
                               dec_units=config.hidden_size,
                               batch_sz=config.batch_size,
                               test_status=True)

        self.optimizer = tf.compat.v1.train.AdamOptimizer(learning_rate=config.learning_Rate)


    def test(self,projectSetting):
        checkpoint = tf.train.Checkpoint(optimizer=self.optimizer,
                                         encoder=self.encoder,
                                         decoder=self.decoder)

        checkpoint.restore(save_path=tf.train.latest_checkpoint(config.checkpoints_folder_path))

        self.translate(projectSetting=projectSetting)

    def translate(self,projectSetting):
        results, test_pairs = self.predict(projectSetting=projectSetting)
        outputarray = []

        print('Writing Results at', config.test_dataset_output_file_path)
        f = open(config.test_dataset_output_file_path, "w")
        for i in range(len(test_pairs)):
            f.write('======================================================================== \n')
            f.write('Test Case: %s \n' % (i+1))
            source = test_pairs[i][2].split('.java')
            path = source[0] + ".java"
            position = ''.join(str(e) for e in source[1:])
            f.write('Path: %s \n' % (path))
            f.write('Source Position: %s \n' % (position))
            f.write('Context: %s \n' % (test_pairs[i][0]))
            f.write('Actual Output: %s \n' % (test_pairs[i][1]))
            f.write('Predicted Output: \n')
            for each_result in results[i]:
                f.write(each_result+'\n')
            outputarray.append((test_pairs[i][1],results[i]))
        f.close()
        print('Calculating Evaluation')
        Evaluation.evaluate(outputarray)

    def predict(self,projectSetting):
        '''
        1. Load the test Data and create pairs: location, context, label
        2. Create dataset for testing as encoder input and decoder output
        3. Run the test
        4. Write the prediction in the result directory and evaluation result in evaluation directory
        :return: A list of prediction. Each item cotains method name and arguments in a string.
        '''
        print('Loading test damca_dataset from', config.test_dataset_file_path)
        dictonary = Vocab()
        _, _, test_pairs = dictonary.prepareData(lang1='Context',
                                                 lang2='Label',
                                                 datasetfilepath=config.test_dataset_file_path)

        print('Creating Dataset for neural network....')
        encoder_input, decoder_output = CreateDataset.datasetCreation(n_iters=len(test_pairs),
                                                                      pairs=test_pairs,
                                                                      input_lang=self.input_lang,
                                                                      output_lang=self.output_lang,
                                                                      MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                                                                      MAX_LENGTH_Output=config.MAX_LENGTH_Output)

        print('Testing Begins.........')
        results = []
        dataPostProcessing = DataPostProcessing(outputLang=self.output_lang)

        for i in range(len(encoder_input)):
            enc_input = tf.convert_to_tensor(value=encoder_input[i],
                                             dtype=tf.int32)
            enc_input = tf.expand_dims(enc_input,
                                       axis=0)
            reciever_variable = self.input_lang.index2word[encoder_input[i][0]]

            filteredMethod = dataPostProcessing.filterMethodName(reciever_variable)


            hidden = [tf.zeros((1, config.hidden_size))]
            enc_out, enc_hidden = self.encoder(enc_input, hidden)

            dec_hidden = enc_hidden
            dec_input = tf.expand_dims([config.SOS_token], 0)

            predictions, dec_hidden, attention_weights = self.decoder(dec_input, dec_hidden, enc_out)

            predicted_sequences = dataPostProcessing.predictMethodName(filteredMethod, predictions)

            t = 1
            while t < config.MAX_LENGTH_Output:
                all_possible_sequences = []
                for each_name in predicted_sequences:
                    dec_input = tf.expand_dims([each_name[1]], 0)
                    predictions, dec_hidden, attention_weights = self.decoder(dec_input, dec_hidden, enc_out)
                    all_possible_sequences.extend(
                        dataPostProcessing.predictArguments(predictions, each_name[1], each_name[0]))

                all_possible_sequences.sort(key=operator.itemgetter(2), reverse=True)

                all_possible_sequences = all_possible_sequences[:config.top_k]

                predicted_sequences = all_possible_sequences

                t = t + 1
            predicted_sequences.append(decoder_output[i])
            predicted_sequences.append(filteredMethod)
            results.append(dataPostProcessing.predictedNumSequencesToStringSequences(predicted_sequences,projectSetting))
        print('Testing Ends.')
        return results, test_pairs








class Inferring:

    def __init__(self, input_vocab_size,output_vocab_size,input_lang,output_lang,input_seq):
        self.input_vocab_size = input_vocab_size
        self.output_vocab_size = output_vocab_size
        self.input_lang = input_lang
        self.output_lang = output_lang
        self.input_seq = input_seq

        # Neural Networks model and optimizer
        self.encoder = Encoder(vocab_size=self.input_vocab_size,
                               max_length=config.MAX_LENGTH_Input,
                               embedding_dim=config.embedding_width,
                               enc_units=config.hidden_size,
                               batch_sz=config.batch_size)
        self.decoder = Decoder(vocab_size=self.output_vocab_size,
                               max_length=config.MAX_LENGTH_Output,
                               embedding_dim=config.embedding_width,
                               dec_units=config.hidden_size,
                               batch_sz=config.batch_size,
                               test_status=True)

        self.optimizer = tf.compat.v1.train.AdamOptimizer(learning_rate=config.learning_Rate)

        config.init()
        tf.compat.v1.enable_eager_execution()



    def infer(self):
        checkpoint = tf.train.Checkpoint(optimizer=self.optimizer,
                                         encoder=self.encoder,
                                         decoder=self.decoder)

        checkpoint.restore(save_path=tf.train.latest_checkpoint(config.checkpoints_folder_path))

        self.translate()



    def translate(self):
        result, test_pairs = self.predict()

        path = test_pairs[0][2].split('.java')
        position = ''.join(str(e).strip() for e in path[1:])
        print('Path: {}'.format(path[0] + '.java'))
        print('Source Position: {}'.format(position.strip()))
        print('Context: {}'.format(test_pairs[0][0]))
        print('Actual Output : {}'.format(test_pairs[0][1]))
        print('Predicted translation:')
        for each_sequence in result:
            print(each_sequence)


    def predict(self):
        attention_plot = np.zeros((config.MAX_LENGTH_Output, config.MAX_LENGTH_Input))

        dictonary = Vocab()
        _, _, test_pairs = dictonary.prepareInferData(lang1='Context',
                                                      lang2='Label',
                                                      input_line=self.input_seq)

        encoder_input, decoder_input, decoder_output = CreateDataset.datasetCreation(n_iters=len(test_pairs),
                                                                                     pairs=test_pairs,
                                                                                     input_lang=self.input_lang,
                                                                                     output_lang=self.output_lang,
                                                                                     MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                                                                                     MAX_LENGTH_Output=config.MAX_LENGTH_Output)

        encoder_input = tf.convert_to_tensor(value=encoder_input,
                                             dtype=tf.int32)

        reciever_variable = test_pairs[0][0].split(' ')[0]

        dataPostProcessing = DataPostProcessing(outputLang=self.output_lang)

        filteredMethod = dataPostProcessing.filterMethodName(reciever_variable)

        hidden = [tf.zeros((1, config.hidden_size))]
        enc_out, enc_hidden = self.encoder(encoder_input, hidden)

        dec_hidden = enc_hidden
        dec_input = tf.expand_dims([config.SOS_token], 0)

        predictions, dec_hidden, attention_weights = self.decoder(dec_input, dec_hidden, enc_out)

        # storing the attention weigths to plot later on
        attention_weights = tf.reshape(attention_weights, (-1,))
        attention_plot[0] = attention_weights.numpy()

        predicted_sequences = dataPostProcessing.predictMethodName(filteredMethod, predictions)

        t = 1
        while t < config.MAX_LENGTH_Output:
            all_possible_sequences = []
            for each_name in predicted_sequences:
                dec_input = tf.expand_dims([each_name[1]], 0)
                predictions, dec_hidden, attention_weights = self.decoder(dec_input, dec_hidden, enc_out)
                all_possible_sequences.extend(
                    dataPostProcessing.predictArguments(predictions, each_name[1], each_name[0]))

            all_possible_sequences.sort(key=operator.itemgetter(2), reverse=True)

            all_possible_sequences = all_possible_sequences[:config.top_k]

            predicted_sequences = all_possible_sequences

            t = t + 1

        result = []
        for each_sequence in predicted_sequences:
            sequence = ""
            for each_token in each_sequence[0]:
                if each_token == config.EOS_token:
                    break
                sequence = sequence + self.output_lang.index2word[each_token] + " "
            result.append(sequence)
        return result, test_pairs










