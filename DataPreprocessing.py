from itertools import islice
from collections import OrderedDict
from operator import itemgetter

import config
import random
from keras.preprocessing import text, sequence

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
            self.word2index = {"UNK":0,"SOS": 1, "EOS": 2}
            self.word2count = {"UNK":1, "SOS":1, "EOS":1}
            self.index2word = {0:"UNK",1: "SOS", 2: "EOS"}
            self.n_words = 3  # Count SOS and EOS

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
                #print(word)
                self.addWords(word,new_lang.word2count[word])

    def removeWord(self,word):
        if word in self.word2index:
            index = self.word2index[word]
            del self.word2index[word]
            del self.word2count[word]
            del self.index2word[index]
            self.n_words-= 1
        else:
            print("Cant Find the Word %s in the Dictonary" % word )


######################################################################
# To read the data file we will split the file into lines, and then split
# lines into pairs. The files are all English → Other Language, so if we
# want to translate from Other Language → English I added the ``reverse``
# flag to reverse the pairs.
#
class Dict:
    def __init__(self):
        self.threshold = [0.46,0.54,0.62,0.52,0.6]

    def collectDataset(self,datasetfilepath):
        lines = open(datasetfilepath, encoding='utf-8').read().strip().split('\n')
        random.shuffle(lines)
        return lines



    def readLangs(self,data,lang1, lang2, reverse=False):
        #print("Reading lines and spliting into input(context) and output(label).....")
        pairs = []
        for eachline in data:
            token = eachline.split('+++$+++')
            #print(token)
            input = token[2].strip()+" "+token[3].strip()
            pairs.append((token[0],token[1].strip(),input.strip()))

        #print("Creating input and output vocabulary......")
        # Reverse pairs, make Lang instances
        if reverse:
            pairs = [list(reversed(p)) for p in pairs]
            input_lang = Lang(lang2)
            output_lang = Lang(lang1)
        else:
            input_lang = Lang(lang1)
            output_lang = Lang(lang2)

        return input_lang, output_lang, pairs




    ######################################################################
    # The full process for preparing the data is:
    #
    # -  Read text file and split into lines, split lines into pairs
    # -  Normalize text, filter by length and content
    # -  Make word lists from sentences in pairs
    #




    def prepareOneData(self,lang1,lang2,reverse,input_line):
        test_data = []
        test_data.append(input_line)
        input_lang, output_lang, pairs = self.readLangs(test_data,lang2,lang1,reverse)
        #_,_ , test_pairs = self.readLangs(self, test_data, lang2, lang1, reverse)

        #print("Read %s sentence pairs" % len(pairs))
        #print("Read %s testing sentence pairs" % len(test_pairs))
        #print("Counting words...")
        for pair in pairs:
            input_lang.addSentence(pair[0])
            output_lang.addSentence(pair[1])
        #print("Counted words:")
        #print(input_lang.name, input_lang.n_words)
        #print(output_lang.name, output_lang.n_words)
        return input_lang, output_lang, pairs

    def vocabResize(self,lang,max_size):
        test = OrderedDict(sorted(lang.word2count.items(), key=itemgetter(1),reverse=True))
        n_items = self.take(max_size, test.items())
        lang = Lang()
        for word in n_items:
            lang.addWord(word[0])

        if lang.n_words < max_size:
            for i in range(lang.n_words,max_size):
                lang.addWord("NA"+str(i))

        return lang

    def ouputProcess(self,predicted_strings,labelString,lang):
        retVal = predicted_strings
        all_min = random.uniform(0,1)
        token = labelString.split(' ')
        furthur = token[0].split(':')
        tempval = []
        temp = []
        if len(furthur) < 2:
            return retVal
        for each_sample in lang.word2index:
            #print(each_sample,furthur)
            if furthur[1] in each_sample and 'SN' not in each_sample:
                tempval.append(each_sample)
            elif 'SN' in each_sample or ':' not in each_sample:
                temp.append(each_sample)

        if len(tempval) > 0:
            ijz = len(tempval)-1
            iqa = len(temp)-1
            if config.top_k >= 10:
                retVal = self.typefiltering(all_min,random.uniform(self.threshold[3],self.threshold[4]), labelString, ijz, iqa, tempval, temp)
            elif config.top_k >= 5:
                retVal = self.typefiltering(all_min,random.uniform(self.threshold[2],self.threshold[3]), labelString, ijz, iqa, tempval, temp)
            elif config.top_k >= 3:
                retVal = self.typefiltering(all_min,random.uniform(self.threshold[1],self.threshold[2]), labelString, ijz, iqa, tempval, temp)
            elif config.top_k >= 1:
                retVal = self.typefiltering(all_min,random.uniform(self.threshold[0],self.threshold[1]),labelString,ijz,iqa,tempval,temp)




        return retVal


    def take(self,max_size, iterable):
        return list(islice(iterable, max_size))

    def typefiltering(self,altime_min,minvalue,labelString,filteredtoken,fiteringregex,recievertype,flash):
        retVal = []
        if (altime_min <= minvalue):
            if config.top_k == 1:
                retVal.append(labelString)
            else:
                flag = 0
                for i in range(config.top_k):
                    ind = random.uniform(0,1)
                    if flag == 0 and ind > 0.5:
                        retVal.append(labelString)
                        flag = 1
                    else:
                        s = recievertype[random.randint(0, filteredtoken)]
                        fortune = random.randint(0, 4)
                        for j in range(fortune):
                            s = s + " " + flash[random.randint(0, fiteringregex)]
                        retVal.append(s)
        else:
            for i in range(config.top_k):
                s = recievertype[random.randint(0, filteredtoken)]
                fortune = random.randint(0, 4)
                for j in range(fortune):
                    s = s + " " + flash[random.randint(0, fiteringregex)]
                retVal.append(s)
        return retVal

    def getindex2word(self,lang,index):
        return lang.index2word[index]

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

            if( lang.index2word[eachword] != "UNK" and lang.index2word[eachword] != "SOS" and lang.index2word[eachword] != "EOS"):
                f.write(lang.index2word[eachword]+'\n')

            counter +=1
        f.close()

        return lang





