from collections import OrderedDict
from operator import itemgetter

import config
import random

class Lang:
    def __init__(self, name=None):
        if(name == None):
            self.name = name
            self.word2index = {}
            self.word2count = {}
            self.index2word = {}
            self.n_words = 0

        else:
            self.name = name
            self.word2index = {"PAD":config.PADDED_Token,"UNK":config.UNK_token,"SOS": config.SOS_token, "EOS": config.EOS_token}
            self.word2count = {"PAD":1,"UNK":1, "SOS":1, "EOS":1}
            self.index2word = {config.PADDED_Token:"PAD",config.UNK_token:"UNK",config.SOS_token: "SOS", config.EOS_token: "EOS"}
            self.n_words = config.EOS_token+1  # Count SOS and EOS

    def addSentence(self, sentence):
        for word in sentence.strip().split(' '):
            self.addWord(word.strip())

    def addWord(self, word):
        if word not in self.word2index:
            self.word2index[word] = self.n_words
            self.word2count[word] = 1
            self.index2word[self.n_words] = word
            self.n_words += 1
        else:
            self.word2count[word] += 1

    def addWords(self, word,occurance):
        if word not in self.word2index:
            self.word2index[word] = self.n_words
            self.word2count[word] = occurance
            self.index2word[self.n_words] = word
            self.n_words += 1
        else:
            self.word2count[word] += occurance

    def appendLang(self,new_lang):
        for word in new_lang.word2index:
            if word.strip() != "":
                self.addWords(word,new_lang.word2count[word])

    def removeWord(self,word):
        if word in self.word2index:
            index = self.word2index[word]
            del self.word2index[word]
            del self.word2count[word]
            del self.index2word[index]
            self.n_words -= 1
        else:
            print("Cant Find the Word %s in the Dictonary" % word )



class Vocab:

    def collectDataset(self,datasetfilepath):
        lines = open(datasetfilepath, encoding='utf-8').read().strip().split('\n')
        random.shuffle(lines)
        return lines

    def readLangs(self,data):
        pairs = []
        for eachline in data:
            token = eachline.split('+++$+++')
            input = token[2].strip()+" "+token[3].strip()
            pairs.append((token[0],token[1].strip(),input.strip()))
        return pairs

    def prepareData(self, lang1, lang2, datasetfilepath):
        train_data = self.collectDataset(datasetfilepath = datasetfilepath)
        pairs = self.readLangs(data = train_data)
        input_lang = Lang(lang1)
        output_lang = Lang(lang2)
        for pair in pairs:
            input_lang.addSentence(pair[2])
            output_lang.addSentence(pair[1])
        return input_lang, output_lang, pairs

    def prepareInferData(self, lang1, lang2, input_line):
        test_data = []
        test_data.append(input_line)
        pairs = self.readLangs(data = test_data)
        input_lang = Lang(lang1)
        output_lang = Lang(lang2)
        input_lang.addSentence(pairs[2])
        output_lang.addSentence(pairs[1])
        return input_lang, output_lang, pairs

    def vocabResize(self,lang,max_size):
        lang.removeWord('PAD')
        lang.removeWord('UNK')
        lang.removeWord('SOS')
        lang.removeWord('EOS')
        orderedVocab = OrderedDict(sorted(lang.word2count.items(), key=itemgetter(1),reverse=True))
        n_items = self.sliceDict(max_size=max_size,
                                 iterable=orderedVocab.items())
        lang = Lang(lang.name)
        for word in n_items:
            lang.addWord(word)

        if lang.n_words < max_size:
            for i in range(lang.n_words,max_size):
                lang.addWord("NA"+str(i))

        return lang


    def sliceDict(self, max_size, iterable):
        retList = []
        i=0
        for each_entry in iterable:
            retList.append(each_entry[0])
            i = i + 1
            if i == max_size:
                break
        return retList

    def load_vocabulary(self,datapath,langName,max_size):
        lines = open(datapath, encoding='utf-8').read().strip().split('\n')
        language = Lang(langName)
        for word in lines:
            language.addWord(word.strip())

        if language.n_words < max_size:
            for i in range(language.n_words,max_size):
                language.addWord("NA"+str(i))

        return language

    def save_vocabulary(self,vocab_path,lang,max_size):
        lang = self.vocabResize(lang,max_size)
        f = open(vocab_path, "w")
        counter = 1
        for eachword in lang.index2word:

            if counter > max_size:
                break

            if( lang.index2word[eachword] != "PAD" and lang.index2word[eachword] != "UNK" and lang.index2word[eachword] != "SOS" and lang.index2word[eachword] != "EOS"):
                f.write(lang.index2word[eachword]+'\n')
                counter += 1

        f.close()

        return lang





