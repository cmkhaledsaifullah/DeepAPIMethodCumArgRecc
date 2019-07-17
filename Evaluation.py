import config
from nltk.translate.bleu_score import sentence_bleu,SmoothingFunction

def evaluate(actualPredList):
    correct_pred = 0
    null_pred = 0
    incorrect_pred = 0
    precision = 0.0
    recall = 0.0
    fscore = 0.0
    blue_score=0.0
    mrr = 0.0
    chencherry = SmoothingFunction()
    for each_actual_pred in actualPredList:
        if each_actual_pred[0] in each_actual_pred[1]:
            correct_pred += 1
            mrr += (1/(each_actual_pred[1].index(each_actual_pred[0])+1))
        elif len(each_actual_pred[1]) == 0:
            null_pred += 1
        else:
            incorrect_pred +=1
        actual_candidate = each_actual_pred[0].split(' ')
        pred_reference = []
        for each_pred in each_actual_pred[1]:
            temp = each_pred.split(' ')
            pred_reference.append(temp)

        if(len(each_actual_pred[1]) == 0):
            blue_score += 0
        else:
            if(len(actual_candidate)>= 4):
                blue_score += sentence_bleu(pred_reference,actual_candidate,weights=(0.25,0.25,0.25,0.25),smoothing_function=chencherry.method4)
            elif (len(actual_candidate)>= 3):
                blue_score += sentence_bleu(pred_reference, actual_candidate, weights=(0.33, 0.33, 0.33, 0),smoothing_function=chencherry.method3)
            elif (len(actual_candidate)>= 2):
                blue_score += sentence_bleu(pred_reference, actual_candidate, weights=(0.5, 0.5, 0, 0),smoothing_function=chencherry.method2)
            else:
                blue_score += sentence_bleu(pred_reference, actual_candidate, weights=(1, 0, 0, 0),smoothing_function=chencherry.method1)


    if (correct_pred+incorrect_pred) != 0:
        precision = (correct_pred/(correct_pred+incorrect_pred))
    if (correct_pred+null_pred) != 0:
        recall = (correct_pred/(correct_pred+null_pred))
    if (precision+recall) != 0:
        fscore = ((2*precision*recall)/(precision+recall))
    blue_score = blue_score/len(actualPredList)
    mrr = mrr/len(actualPredList)
    evaluation_text =''.join( "===================================================================\n"+\
                      "File: "+config.test_dataset_file_name+"\n"+\
                      "Top "+ str(config.top_k) +" Reccomendation:"+"\n"+\
                      "Precision: "+str(precision)+"\n"+\
                      "Recall: "+str(recall)+"\n"+\
                      "F-1 Score: "+str(fscore)+"\n"+\
                      "BLEU Score: "+str(blue_score)+"\n"+\
                      "MRR: "+str(mrr)+"\n"+\
                      "===================================================================\n")
    print(evaluation_text)
    print("Writting evaluation report at", config.evaluation_file_path)
    f = open(config.evaluation_file_path, "a+")
    f.write(evaluation_text)
    f.close()

