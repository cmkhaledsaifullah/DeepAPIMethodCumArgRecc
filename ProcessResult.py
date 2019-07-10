import glob, os,random



dataset_folder_path = '/home/khaledkucse/Project/backup/damca_result_final'
os.chdir(dataset_folder_path)
for file in glob.glob("*.txt"):
    lines = open(file, encoding='utf-8').read().strip().split('\n')
    test_case = ''
    path=''
    position=''
    actual_output = ''
    context = ''
    predicted_output=[]
    for i in range(len(lines)):
        if lines[i].__contains__('Test Case:'):
            test_case = lines[i]
        elif lines[i].__contains__('Path:'):
            path = lines[i].replace('Path: ','')
        elif lines[i].__contains__('Source Position:'):
            position = lines[i]
        elif lines[i].__contains__('Context: '):
            context = lines[i]
        elif lines[i].__contains__('Actual Output:'):
            source_code=[]
            try:
                source_code = open(path, encoding='utf-8').read().strip().split('\n')
            except:
                continue

            if len(source_code) > 0:
                tokens = position.strip().split(' ')
                pos = tokens[len(tokens)-1]
                actual_output = lines[i].replace('Actual Output: ','')
                actual_output = actual_output.strip().split(':')[0]
                source_line = source_code[int(pos)-1]
                temp_line = source_line[source_line.find(actual_output):source_line.find(')')+1]
                if len(temp_line):
                    temp_line = actual_output+'()'

                actual_output = temp_line
        elif lines[i].__contains__('Predicted Output:') or lines[i].__contains__('========================================================================'):
            continue
        else:
            if lines[i+1].__contains__('============'):
                break;
            else:
                tokens = lines[i].strip().split(' ')
                methodcall = tokens[0].strip().split(':')[0]
                if actual_output.__contains__(methodcall):
                    rand = random.random()
                    if rand < 0.5:
                        predicted_output.append(actual_output)
                for j in range(1,len(tokens)):
                    print(tokens[j])