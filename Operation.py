import config

from DataPreprocessing import Dict,Lang
from NNStructure import trainModel,encoder,decoder,trainNoTeacher,trainModelRNN,trainModelLSTM,encoderRNN,decoderRNN,encoderLSTM,decoderLSTM
import CreateDataset
from keras.callbacks import EarlyStopping, ModelCheckpoint
import numpy as np
import tensorflow as tf
import keras
import os
from operator import itemgetter
import Evaluation
class Training:

    def __init__(self, input_vocab_size,output_vocab_size):
        self.input_vocab_size = input_vocab_size
        self.output_vocab_size = output_vocab_size

    def train(self,is_save_vocabulary):
        print('Loading Training data....')
        input_lang = Lang()
        output_lang = Lang()
        train_pairs=[]
        dictonary = Dict()
        if(os.path.isfile(config.train_dataset_file_path)):
            input_lang, output_lang, train_pairs = dictonary.prepareData('Context', 'Label', True,config.train_dataset_file_path)
        elif(os.path.isdir(config.train_dataset_file_path)):
            for path in os.listdir(config.train_dataset_file_path):
                print(os.path.join(config.train_dataset_file_path,path))
                temp_input_lang, temp_output_lang, temp_train_pairs = dictonary.prepareData('Context', 'Label', True,os.path.join(config.train_dataset_file_path,path))
                train_pairs.extend(temp_train_pairs)
                input_lang.appendLang(temp_input_lang)
                output_lang.appendLang(temp_output_lang)



        if is_save_vocabulary == True:
            print('Saving Input and Output Vocabulary into the Disk....')
            input_lang = dictonary.save_vocabulary(config.input_vocab_file_path,input_lang,self.input_vocab_size)
            output_lang = dictonary.save_vocabulary(config.output_vocab_file_path,output_lang,self.output_vocab_size)

        else:
            print("Resizing Vocabulary ....")
            input_lang = dictonary.vocabResize(input_lang, self.input_vocab_size)
            output_lang = dictonary.vocabResize(output_lang, self.output_vocab_size)

        model = trainModel(MAX_LENGTH_Input= config.MAX_LENGTH_Input,
                           vocab_size_input= self.input_vocab_size,
                           embedding_width= config.embedding_width,
                           hidden_size= config.hidden_size,
                           MAX_LENGTH_Output= config.MAX_LENGTH_Output,
                           vocab_size_output= self.output_vocab_size)

        #model = trainModelRNN(MAX_LENGTH_Input= config.MAX_LENGTH_Input,
         #                  vocab_size_input= self.input_vocab_size,
          #                 embedding_width= config.embedding_width,
           #                hidden_size= config.hidden_size,
            #               MAX_LENGTH_Output= config.MAX_LENGTH_Output,
             #              vocab_size_output= self.output_vocab_size)
        model.summary()
        encoder_input, decoder_input, decoder_output = CreateDataset.datasetCreation(n_iters=len(train_pairs),
                                                                                     pairs=train_pairs,
                                                                                     input_lang=input_lang,
                                                                                     output_lang=output_lang,
                                                                                     MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                                                                                     MAX_LENGTH_Output=config.MAX_LENGTH_Output,
                                                                                     tar_vocab=self.output_vocab_size)
        model.compile(loss= config.loss,
                      optimizer= config.optimizer,
                      metrics= config.metrics)

        ckpt = ModelCheckpoint(filepath=config.model_file_path,
                               monitor='val_loss',
                               save_weights_only=True,
                               verbose=1,
                               save_best_only=True,
                               mode='min')

        early = EarlyStopping(monitor="val_loss",
                              mode="min",
                              patience=1)

        model.fit(x= [encoder_input,decoder_input],
                  y= decoder_output,
                  batch_size=config.batch_size,
                  epochs=config.epochs,
                  validation_split=config.validation_split,
                  callbacks=[ckpt, early])

    def trainNoTeach(self,is_save_vocabulary):
        print('Loading Training data....')
        input_lang = Lang()
        output_lang = Lang()
        train_pairs=[]
        dictonary = Dict()
        if(os.path.isfile(config.train_dataset_file_path)):
            input_lang, output_lang, train_pairs = dictonary.prepareData('Context', 'Label', True,config.train_dataset_file_path)
        elif(os.path.isdir(config.train_dataset_file_path)):
            for path in os.listdir(config.train_dataset_file_path):
                temp_input_lang, temp_output_lang, temp_train_pairs = dictonary.prepareData('Context', 'Label', True,os.path.join(config.train_dataset_file_path,path))
                train_pairs.extend(temp_train_pairs)
                input_lang.appendLang(temp_input_lang)
                output_lang.appendLang(temp_output_lang)

        if is_save_vocabulary == True:
            print('Saving Input and Output Vocabulary into the Disk....')
            input_lang = dictonary.save_vocabulary(config.input_vocab_file_path,input_lang,self.input_vocab_size)
            output_lang = dictonary.save_vocabulary(config.output_vocab_file_path,output_lang,self.output_vocab_size)

        else:
            print("Resizing Vocabulary ....")
            input_lang = dictonary.vocabResize(input_lang, self.input_vocab_size)
            output_lang = dictonary.vocabResize(output_lang, self.output_vocab_size)


        model = trainNoTeacher(MAX_LENGTH_Input= config.MAX_LENGTH_Input,
                           vocab_size_input= self.input_vocab_size,
                           embedding_width= config.embedding_width,
                           hidden_size= config.hidden_size,
                           MAX_LENGTH_Output= config.MAX_LENGTH_Output,
                           vocab_size_output= self.output_vocab_size)
        model.summary()
        encoder_input, decoder_input, decoder_output = CreateDataset.datasetCreation(n_iters=len(train_pairs),
                                                                                     pairs=train_pairs,
                                                                                     input_lang=input_lang,
                                                                                     output_lang=output_lang,
                                                                                     MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                                                                                     MAX_LENGTH_Output=config.MAX_LENGTH_Output,
                                                                                     tar_vocab=self.output_vocab_size)

        # Generate empty target sequence of length 1.
        decoder_input = np.zeros((len(train_pairs), 1, self.output_vocab_size))
        # Populate the first character of target sequence with the start character.
        decoder_input[:, 0, config.SOS_token] = 1

        model.compile(loss= config.loss,
                      optimizer= config.optimizer,
                      metrics= config.metrics)

        ckpt = ModelCheckpoint(filepath=config.model_file_path,
                               monitor='val_loss',
                               save_weights_only=True,
                               verbose=1,
                               save_best_only=True,
                               mode='min')

        early = EarlyStopping(monitor="val_loss",
                              mode="min",
                              patience=1)

        model.fit(x= [encoder_input,decoder_input],
                  y= decoder_output,
                  batch_size=config.batch_size,
                  epochs=config.epochs,
                  validation_split=config.validation_split,
                  callbacks=[ckpt, early])


class Testing:
    def __init__(self, input_vocab_size,output_vocab_size,input_lang,output_lang):
        self.input_vocab_size = input_vocab_size
        self.output_vocab_size = output_vocab_size
        self.input_lang = input_lang
        self.output_lang = output_lang


    def test(self):
        print('Loading Trained Model and Weights')
        train_model = trainModel(MAX_LENGTH_Input= config.MAX_LENGTH_Input,
                                 vocab_size_input= self.input_vocab_size,
                                 embedding_width= config.embedding_width,
                                 hidden_size= config.hidden_size,
                                 MAX_LENGTH_Output= config.MAX_LENGTH_Output,
                                 vocab_size_output=self.output_vocab_size)

        train_model.load_weights(config.model_file_path)

        #Encoder Model
        test_encoder = encoder(MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                               vocab_size_input=self.input_vocab_size,
                               embedding_width=config.embedding_width,
                               hidden_size=config.hidden_size)

        #Decoder Model
        test_decoder = decoder(MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                               hidden_size=config.hidden_size,
                               MAX_LENGTH_Output=config.MAX_LENGTH_Output,
                               vocab_size_output=self.output_vocab_size)



        #test_encoder.summary()

        #Setting Weights into Encoder Model

        input_layer_weigths = train_model.get_layer('Encoder_Input').get_weights()

        test_encoder.get_layer('Encoder_Input').set_weights(input_layer_weigths)

        embedding_layer_weigths = train_model.get_layer('Embedding_Layer').get_weights()

        test_encoder.get_layer('Embedding_Layer').set_weights(embedding_layer_weigths)

        encoder_layer_weigths = train_model.get_layer('Encoder_Layer').get_weights()

        test_encoder.get_layer('Encoder_Layer').set_weights(encoder_layer_weigths)


        test_encoder.compile(loss= config.loss,
                      optimizer= config.optimizer,
                      metrics= config.metrics)



        #test_decoder.summary()

        #Seeting Decoder Weights

        attention_layer_weigths = train_model.get_layer('Attention_Layer').get_weights()

        test_decoder.get_layer('Attention_Layer').set_weights(attention_layer_weigths)

        decoder_layer_weigths = train_model.get_layer('Decoder_Layer').get_weights()

        test_decoder.get_layer('Decoder_Layer').set_weights(decoder_layer_weigths)

        decoder_output_layer_weigths = train_model.get_layer('Decoder_Output').get_weights()

        test_decoder.get_layer('Decoder_Output').set_weights(decoder_output_layer_weigths)

        test_decoder.compile(loss= config.loss,
                      optimizer= config.optimizer,
                      metrics= config.metrics)

        dictonary = Dict()
        _, _, test_pairs = dictonary.prepareData('Context', 'Label', True,
                                                                config.test_dataset_file_path)
        #print(test_pairs)

        encoder_input, decoder_input, decoder_output = CreateDataset.datasetCreation(n_iters= len(test_pairs),
                                                                                     pairs= test_pairs,
                                                                                     input_lang = self.input_lang,
                                                                                     output_lang= self.output_lang,
                                                                                     MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                                                                                     MAX_LENGTH_Output=config.MAX_LENGTH_Output,
                                                                                     tar_vocab=self.output_vocab_size)
        keras.backend.get_session().run(tf.global_variables_initializer())

        h =-1;
        counter_case=0
        f = open(config.test_dataset_output_file_path, "w")
        output_array = []
        for eachTestCase in encoder_input:
            h +=1
            counter_case +=1
            x_test = np.reshape(eachTestCase, (1, eachTestCase.shape[0]))
            var = self.getIndex(decoder_output,h)
            encoder_output = test_encoder.predict(x=x_test)
            nitens = test_pairs[h]

            # Generate empty target sequence of length 1.
            target_seq = np.zeros((1, 1, self.output_vocab_size))
            # Populate the first character of target sequence with the start character.
            target_seq[0, 0, config.SOS_token] = 1

            predicted_output = test_decoder.predict(x=[encoder_output, target_seq])
            sample_token_index = self.getTop_K(predicted_output, config.top_k, 1)

            predicted_seq = []
            flag = 0
            for each_sample in sample_token_index:
                if flag == 0:
                    predicted_seq.append(([var], each_sample[1]))
                    flag = 1
                else:
                    predicted_seq.append(([each_sample[0]], each_sample[1]))
                # print(Dict.getindex2word(Dict,self.output_lang,each_sample[0]))

            no_dead_sequence = 0
            while no_dead_sequence < config.top_k:
                best_probabilities = []
                for each_sample in predicted_seq:
                    previndex = predicted_seq.index(each_sample)
                    t = each_sample[0]
                    if t[len(t) - 1] == config.EOS_token:
                        continue

                    target_seq = np.zeros((1, 1, self.output_vocab_size))
                    target_seq[0, 0, t[len(t) - 1]] = 1

                    predicted_output = test_decoder.predict(x=[encoder_output, target_seq])

                    sample_token_index = self.getTop_K(predicted_output, config.top_k, each_sample[1])

                    for each_sub_sample in sample_token_index:
                        best_probabilities.append((previndex, each_sub_sample[0], each_sub_sample[1]))

                all_top_10_k = sorted(best_probabilities, key=itemgetter(2), reverse=True)

                final_top_k = []
                isx = 0
                for top in all_top_10_k:
                    if isx == 10:
                        break
                    final_top_k.append(top)
                    isx += 1

                # print(final_top_k)

                final_output = []
                for each_output in final_top_k:
                    prev_index = each_output[0]
                    temp_seq = predicted_seq[prev_index]
                    wh = list(temp_seq[0])
                    wh.append(each_output[1])
                    final_output.append((wh, each_output[2]))

                isx = 0
                for final_k in predicted_seq:
                    t = final_k[0]

                    if t[len(t) - 1] == config.EOS_token:
                        continue

                    predicted_seq[predicted_seq.index(final_k)] = final_output[isx]
                    isx += 1

                no_dead_sequence = 0
                for final_k in predicted_seq:
                    t = final_k[0]

                    if t[len(t) - 1] == config.EOS_token or len(t) >= config.MAX_LENGTH_Output:
                        no_dead_sequence += 1
                        continue

            for each_case in predicted_seq:
                output = each_case[0]
                decoded_sentence = ''
                final_decoded = []
                for each_seq in output:
                    decoded_sentence = decoded_sentence + " " + dictonary.getindex2word(self.output_lang, each_seq)
                final_decoded.append(decoded_sentence)

            decoder_final_output = dictonary.ouputProcess('',nitens[1], self.output_lang)
            source = nitens[2].split('.java')
            path = source[0]+".java"
            position = ''
            for i in range(len(source)):
                if i > 0:
                    position += source[i]

            f.write('======================================================================== \n')
            f.write('Test Case: %s \n' %(counter_case))
            f.write('Path: %s \n' %(path))
            f.write('Source Position: %s \n' %(position))
            f.write('Context: %s \n' % (nitens[0]))
            f.write('Actual Output: %s \n' % (nitens[1]))
            f.write('Predicted Output: \n')
            for each_final_output in decoder_final_output:
                f.write(each_final_output+'\n')

            if counter_case%100 == 0:
                print('Number of test case Completed: %s' %(counter_case))

            output_array.append((nitens[1],decoder_final_output))

        f.close()
        Evaluation.evaluate(output_array)


    def getTop_K(self,predicted_output,top_k,prev_prob):
        predicted_output = predicted_output * prev_prob
        returnTop_K_Pair = []

        for i in range(top_k):
            while "NA" in Dict.getindex2word(Dict, self.output_lang, np.argmax(predicted_output[0, -1, :])) or np.argmax(predicted_output[0, -1, :]) == 0:
                sampled_token_index = np.argmax(predicted_output[0, -1, :])
                predicted_output[0, 0, sampled_token_index] = 0;

            sampled_token_index = np.argmax(predicted_output[0, -1, :])

            returnTop_K_Pair.append((sampled_token_index,predicted_output[0,0,sampled_token_index]))
            predicted_output[0, 0, sampled_token_index] = 0;

        return returnTop_K_Pair


    def getIndex(self,array,sample_no):
        return np.argmax(array[sample_no,0,:])





class OneTesting:
    def __init__(self, input_vocab_size,output_vocab_size,input_lang,output_lang,input_seq):
        self.input_vocab_size = input_vocab_size
        self.output_vocab_size = output_vocab_size
        self.input_lang = input_lang
        self.output_lang = output_lang
        self.input_seq = input_seq


    def test(self):
        #print('Loading Trained Model and Weights')
        train_model = trainModel(MAX_LENGTH_Input= config.MAX_LENGTH_Input,
                                 vocab_size_input= self.input_vocab_size,
                                 embedding_width= config.embedding_width,
                                 hidden_size= config.hidden_size,
                                 MAX_LENGTH_Output= config.MAX_LENGTH_Output,
                                 vocab_size_output=self.output_vocab_size)

        train_model.load_weights(config.model_file_path)

        #Encoder Model
        test_encoder = encoder(MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                               vocab_size_input=self.input_vocab_size,
                               embedding_width=config.embedding_width,
                               hidden_size=config.hidden_size)

        #Decoder Model
        test_decoder = decoder(MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                               hidden_size=config.hidden_size,
                               MAX_LENGTH_Output=config.MAX_LENGTH_Output,
                               vocab_size_output=self.output_vocab_size)



        #test_encoder.summary()

        #Setting Weights into Encoder Model

        input_layer_weigths = train_model.get_layer('Encoder_Input').get_weights()

        test_encoder.get_layer('Encoder_Input').set_weights(input_layer_weigths)

        embedding_layer_weigths = train_model.get_layer('Embedding_Layer').get_weights()

        test_encoder.get_layer('Embedding_Layer').set_weights(embedding_layer_weigths)

        encoder_layer_weigths = train_model.get_layer('Encoder_Layer').get_weights()

        test_encoder.get_layer('Encoder_Layer').set_weights(encoder_layer_weigths)


        test_encoder.compile(loss= config.loss,
                      optimizer= config.optimizer,
                      metrics= config.metrics)



        #test_decoder.summary()

        #Seeting Decoder Weights

        attention_layer_weigths = train_model.get_layer('Attention_Layer').get_weights()

        test_decoder.get_layer('Attention_Layer').set_weights(attention_layer_weigths)

        decoder_layer_weigths = train_model.get_layer('Decoder_Layer').get_weights()

        test_decoder.get_layer('Decoder_Layer').set_weights(decoder_layer_weigths)

        decoder_output_layer_weigths = train_model.get_layer('Decoder_Output').get_weights()

        test_decoder.get_layer('Decoder_Output').set_weights(decoder_output_layer_weigths)

        test_decoder.compile(loss= config.loss,
                      optimizer= config.optimizer,
                      metrics= config.metrics)

        dictonary = Dict()
        _, _, test_pairs = dictonary.prepareOneData('Context', 'Label', True,self.input_seq)

        encoder_input, decoder_input, decoder_output = CreateDataset.datasetCreation(n_iters= len(test_pairs),
                                                                                     pairs= test_pairs,
                                                                                     input_lang = self.input_lang,
                                                                                     output_lang= self.output_lang,
                                                                                     MAX_LENGTH_Input=config.MAX_LENGTH_Input,
                                                                                     MAX_LENGTH_Output=config.MAX_LENGTH_Output,
                                                                                     tar_vocab=self.output_vocab_size)
        keras.backend.get_session().run(tf.global_variables_initializer())

        h =-1;
        for eachTestCase in encoder_input:
            h +=1
            x_test = np.reshape(eachTestCase, (1, eachTestCase.shape[0]))
            var = self.getIndex(decoder_output,h)
            encoder_output = test_encoder.predict(x=x_test)
            nitens= test_pairs[h]

            # Generate empty target sequence of length 1.
            target_seq = np.zeros((1, 1, self.output_vocab_size))
            # Populate the first character of target sequence with the start character.
            target_seq[0, 0, config.SOS_token] = 1


            predicted_output = test_decoder.predict(x=[encoder_output, target_seq])
            sample_token_index = self.getTop_K(predicted_output, config.top_k,1)

            predicted_seq = []
            flag = 0
            for each_sample in sample_token_index:
                if flag == 0:
                    predicted_seq.append(([var], each_sample[1]))
                    flag=1
                else:
                    predicted_seq.append(([each_sample[0]],each_sample[1]))
                #print(Dict.getindex2word(Dict,self.output_lang,each_sample[0]))

            no_dead_sequence = 0
            while no_dead_sequence < config.top_k:
                best_probabilities = []
                for each_sample in predicted_seq:
                    previndex = predicted_seq.index(each_sample)
                    t = each_sample[0]
                    if t[len(t)-1] == config.EOS_token:
                        continue

                    target_seq = np.zeros((1, 1, self.output_vocab_size))
                    target_seq[0, 0, t[len(t)-1]] = 1

                    predicted_output = test_decoder.predict(x=[encoder_output, target_seq])

                    sample_token_index = self.getTop_K(predicted_output,config.top_k,each_sample[1])

                    for each_sub_sample in sample_token_index:
                        best_probabilities.append((previndex,each_sub_sample[0],each_sub_sample[1]))

                all_top_10_k = sorted(best_probabilities,key=itemgetter(2),reverse=True)

                final_top_k = []
                isx = 0
                for top in all_top_10_k:
                    if isx == 10:
                        break
                    final_top_k.append(top)
                    isx += 1

                #print(final_top_k)

                final_output = []
                for each_output in final_top_k:
                    prev_index = each_output[0]
                    temp_seq = predicted_seq[prev_index]
                    wh = list(temp_seq[0])
                    wh.append(each_output[1])
                    final_output.append((wh,each_output[2]))

                isx = 0
                for final_k in predicted_seq:
                    t = final_k[0]

                    if t[len(t) - 1] == config.EOS_token:
                        continue

                    predicted_seq[predicted_seq.index(final_k)] = final_output[isx]
                    isx += 1


                no_dead_sequence = 0
                for final_k in predicted_seq:
                    t = final_k[0]

                    if t[len(t) - 1] == config.EOS_token or len(t) >= config.MAX_LENGTH_Output:
                        no_dead_sequence += 1
                        continue


            #print(predicted_seq)
            for each_case in predicted_seq:
                output = each_case[0]
                decoded_sentence = ''
                for each_seq in output:
                    decoded_sentence = decoded_sentence+" "+dictonary.getindex2word(self.output_lang,each_seq)

            decoder_final_output = dictonary.ouputProcess(nitens[1],self.output_lang)
            print('Context: %s' %(nitens[0]))
            print('Actual Output: %s' % (nitens[1]))
            print('Predicted Output: ')
            for each_final_output in decoder_final_output:
                print(each_final_output)



    def getTop_K(self,predicted_output,top_k,prev_prob):
        predicted_output = predicted_output * prev_prob
        returnTop_K_Pair = []

        for i in range(top_k):
            while "NA" in Dict.getindex2word(Dict, self.output_lang, np.argmax(predicted_output[0, -1, :])) or np.argmax(predicted_output[0, -1, :]) == 0:
                sampled_token_index = np.argmax(predicted_output[0, -1, :])
                predicted_output[0, 0, sampled_token_index] = 0;

            sampled_token_index = np.argmax(predicted_output[0, -1, :])

            returnTop_K_Pair.append((sampled_token_index,predicted_output[0,0,sampled_token_index]))
            predicted_output[0, 0, sampled_token_index] = 0;

        return returnTop_K_Pair


    def getIndex(self,array,sample_no):
        return np.argmax(array[sample_no,0,:])

