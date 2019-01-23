import operator,config

class DataPostProcessing:
    def __init__(self,outputLang):
        self.outputLang = outputLang
        self.methodList={}
        self.armugentList={}
        self.divideMethodNameArgument()
        config.init()

    def divideMethodNameArgument(self):
        for each_token,value in self.outputLang.word2index.items():
            if ":" in each_token and 'SN' not in each_token:
                self.methodList[each_token] = value
            elif "UNK" not in each_token and "SOS" not in each_token:
                self.armugentList[each_token] = value



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
            predictionValues.append((sequence,idx,predictions[0][idx].numpy()*prev_token_probability))

        predictionValues.sort(key=operator.itemgetter(2),reverse=True)

        return predictionValues[:config.top_k]
