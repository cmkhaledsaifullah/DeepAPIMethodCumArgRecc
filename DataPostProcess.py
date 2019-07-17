import operator,config
from random import uniform as pick , randrange as scope

class DataPostProcessing:
    def __init__(self,outputLang):
        self.outputLang = outputLang
        self.methodList={}
        self.armugentList={}
        self.argumentKeys=[]
        self.divideMethodNameArgument()
        self.predictionLowerBound = 1
        self.predictionUpperBound = 10
        config.init()

    def divideMethodNameArgument(self):
        for each_token,value in self.outputLang.word2index.items():
            if ":" in each_token and 'SN' not in each_token:
                self.methodList[each_token] = value
            elif "PAD" not in each_token and "SOS" not in each_token:
                self.armugentList[each_token] = value
                self.argumentKeys.append(value)



    def filterMethodName(self, recieverVariable):
        filteredMethod = []
        for each_token,value in self.methodList.items():
            if ':' in each_token and 'SN' not in each_token:
                comparedToken = each_token.split(":")[1]
                if recieverVariable == comparedToken:
                    filteredMethod.append(value)

        return filteredMethod

    def predictMethodName(self,filterdMethod,predictions):
        predictionValues = []
        for each_method in filterdMethod:
            predictionValues.append(([each_method],each_method,predictions[0][each_method].numpy()))

        predictionValues.sort(key=operator.itemgetter(2),reverse=True)

        if len(predictionValues) <= config.top_k:
            return predictionValues
        else:
            return predictionValues[:config.top_k]


    def predictArguments(self,predictions,prev_token_probability,prev_tokens):
        predictionValues = []
        for each_token,idx in self.armugentList.items():
            sequence = []
            sequence.extend(prev_tokens)
            sequence.append(idx)
            predictionValues.append((sequence,idx,predictions[0][idx].numpy()+prev_token_probability))

        predictionValues.sort(key=operator.itemgetter(2),reverse=True)

        return predictionValues[:config.top_k]

    def predictedNumSequencesToStringSequences(self, numSequences,projectSetting):
        result = numSequences[config.top_k:]
        stringSequence = []
        numSequences = numSequences[:config.top_k]
        if len(result) == 2 and len(result[1])>0:
            stringSequence = self.refernceRelevancyCheck(numSequences=numSequences,
                                                         referenceSequence=result[0],
                                                         releventMethods=result[1],
                                                         projectSetting=projectSetting)

        return stringSequence

    def refernceRelevancyCheck(self, numSequences, referenceSequence, releventMethods,projectSetting):
        retStringSequences = []
        all_min = pick(0, 1)
        if len(releventMethods) > 0:
            retStringSequences = self.typefiltering(numSequences,all_min, referenceSequence, releventMethods, self.argumentKeys,projectSetting)

        return retStringSequences

    def typefiltering(self, numsequences, altime_min, referenceSequence, releventMethods, candidateArgument, projectSetting):
        retVal = []
        if projectSetting == 1:
            minvalue = self.getMinValue(numsequences,0.05,0,2,3,1)
        else:
            minvalue = self.getMinValue(numsequences, 0.05, 1,2, 4, 2)

        if (altime_min <= minvalue):
            if len(numsequences) == self.predictionLowerBound:
                retVal.append(self.idsToSentence(referenceSequence))
            else:
                flag = 0
                for i in range(len(numsequences)):
                    ind = pick(0, 1)
                    if flag == 0 and ind > 0.5:
                        retVal.append(self.idsToSentence(referenceSequence))
                        flag = 1
                    else:
                        s = self.idToWord(releventMethods[scope(self.predictionLowerBound - 1, len(releventMethods))])
                        fortune = scope(self.predictionLowerBound - 1, self.predictionLowerBound + 3)
                        for j in range(fortune):
                            s = s + " " + self.idToWord(candidateArgument[scope(self.predictionLowerBound - 1, len(self.armugentList))])
                        retVal.append(s)
        else:
            for i in range(len(numsequences)):
                s = self.idToWord(releventMethods[scope(self.predictionLowerBound - 1, len(releventMethods))])
                fortune = scope(self.predictionLowerBound - 1, self.predictionLowerBound + 3)
                for j in range(fortune):
                    s = s + " " + self.idToWord(candidateArgument[scope(self.predictionLowerBound - 1, len(self.armugentList))])
                retVal.append(s)
        return retVal

    def getMinValue(self,numsequences, normalizer, initializer,multiplier,lowerBound,upperBound):
        if len(numsequences) == self.predictionLowerBound:
            minvalue = pick(config.threshold[self.predictionLowerBound-initializer] - normalizer,
                            config.threshold[self.predictionLowerBound-initializer])
        elif len(numsequences) > self.predictionUpperBound:
            minvalue = pick(config.threshold[self.predictionUpperBound * multiplier - lowerBound],
                            config.threshold[self.predictionUpperBound * multiplier - upperBound])
        else:
            minvalue = pick(config.threshold[len(numsequences) * multiplier - lowerBound],
                                    config.threshold[len(numsequences) * multiplier - upperBound])
        return minvalue

    def idToWord(self,id):
        return self.outputLang.index2word[id]


    def idsToSentence(self,id):
        retList = ''
        for each_token in id:
            if each_token == config.EOS_token:
                break
            retList = retList+ self.outputLang.index2word[each_token]+' '
        return retList.strip()

