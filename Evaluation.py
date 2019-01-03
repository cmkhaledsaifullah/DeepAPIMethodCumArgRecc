import config
from nltk.translate.bleu_score import sentence_bleu,SmoothingFunction

def evaluate(output):
    correct_pred = 0
    null_pred = 0
    incorrect_pred = 0
    blue_score=0
    mrr = 0;
    chencherry = SmoothingFunction()
    for each_output in output:
        if each_output[0] in each_output[1]:
            correct_pred += 1
            mrr += (1/(each_output[1].index(each_output[0])+1))
        elif len(each_output[1]) == 0:
            null_pred += 1
        else:
            incorrect_pred +=1
        candidate = each_output[0].split(' ')
        reference = []
        for each_pred in each_output[1]:
            temp = each_pred.split(' ')
            reference.append(temp)

        if(len(each_output[1]) == 0):
            blue_score += 0
        else:
            if(len(candidate)>= 4):
                blue_score += sentence_bleu(reference,candidate,weights=(0.25,0.25,0.25,0.25),smoothing_function=chencherry.method4)
            elif (len(candidate)>= 3):
                blue_score += sentence_bleu(reference, candidate, weights=(0.33, 0.33, 0.33, 0),smoothing_function=chencherry.method3)
            elif (len(candidate)>= 2):
                blue_score += sentence_bleu(reference, candidate, weights=(0.5, 0.5, 0, 0),smoothing_function=chencherry.method2)
            else:
                blue_score += sentence_bleu(reference, candidate, weights=(1, 0, 0, 0),smoothing_function=chencherry.method1)


    precision = (correct_pred/(correct_pred+incorrect_pred))
    recall = (correct_pred/(correct_pred+null_pred))
    fmeasure = ((2*precision*recall)/(precision+recall))
    blue_score = blue_score/len(output)
    mrr = mrr/len(output)

    print()
    print("Top %s Reccomendation:" %(config.top_k))
    print("Precision: %s" %(precision))
    print("Recall: %s" %(recall))
    print("F-1 Measure: %s" % (fmeasure))
    print("Bleu Score: %s" % (blue_score))
    print("MRR: %s" % (mrr))

