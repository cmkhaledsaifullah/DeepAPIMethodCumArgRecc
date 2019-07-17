def init():
    global \
        top_k, \
        train_dataset_file_path, \
        test_dataset_file_name,\
        test_dataset_file_path,\
        input_vocab_file_path, \
        output_vocab_file_path, \
        checkpoints_folder_path, \
        test_dataset_output_file_name, \
        test_dataset_output_file_path, \
        evaluation_file_path, \
        MAX_LENGTH_Input, \
        MAX_LENGTH_Output, \
        MAX_VOCAB_SIZE_INPUT, \
        MAX_VOCAB_SIZE_OUTPUT, \
        SOS_token,\
        EOS_token, \
        UNK_token, \
        PADDED_Token,\
        learning_Rate,\
        hidden_size,\
        embedding_width,\
        batch_size,\
        epochs,\
        validation_split,\
        dropout, \
        threshold,\
        l1_regularization,\
        l2_regularization,\
        gpu_id


    #Predicion parameter
    top_k = 10

    #All File Path
    root_folder = '/home/khaledkucse/Project/backup/damca'

    train_dataset_file_path = root_folder+'/dataset/train_dataset/'

    test_dataset_file_name = 'eclipse_10'

    test_dataset_file_path = root_folder+'/dataset/test_dataset/'+test_dataset_file_name+'.txt'

    input_vocab_file_path = root_folder+'/vocabulary/input.vocab'

    output_vocab_file_path = root_folder+'/vocabulary/output.vocab'

    checkpoints_folder_path = root_folder+'/training_checkpoints/'

    test_dataset_output_file_path = root_folder+'/result/'+test_dataset_file_name+'_top_'+str(top_k)+'.txt'

    evaluation_file_path = root_folder+'/evaluation/evaluation_results.txt'


    #Vocabualry and COrpus related parameter
    MAX_LENGTH_Input = 40

    MAX_LENGTH_Output = 5

    MAX_VOCAB_SIZE_INPUT = 50000

    MAX_VOCAB_SIZE_OUTPUT = 5000

    UNK_token = 1

    SOS_token = 2

    EOS_token = 3

    PADDED_Token = 0


    #Training related parameter
    learning_Rate = 0.01

    hidden_size = 256

    embedding_width = 128

    batch_size = 64

    epochs = 100

    validation_split = 0.1

    dropout = 0.2

    threshold = [0.3, 0.5, 0.38, 0.57, 0.45, 0.62, 0.48, 0.64, 0.5, 0.66, 0.52, 0.67, 0.53, 0.68, 0.55, 0.7, 0.56, 0.72, 0.6, 0.75]

    l1_regularization = 0.0001

    l2_regularization = 0.0001

    #GPU_ID when GPU is available:
    gpu_id = '1'

