def init():
    global train_dataset_file_path, \
        test_dataset_file_path,\
        input_vocab_file_path, \
        output_vocab_file_path, \
        test_dataset_output_file_path, \
        checkpoints_folder_path, \
        MAX_LENGTH_Input, \
        MAX_LENGTH_Output, \
        SOS_token,\
        EOS_token, \
        UNK_token, \
        PADDED_Token,\
        teacher_forcing_ratio,\
        learning_Rate,\
        hidden_size,\
        print_every,\
        plot_every,\
        embedding_width,\
        batch_size,\
        epochs,\
        validation_split,\
        n_layers,\
        dropout, \
        l1_regularization,\
        l2_regularization,\
        loss,\
        optimizer,\
        metrics,\
        dict_size,\
        model_file_path, \
        MAX_VOCAB_SIZE_INPUT, \
        MAX_VOCAB_SIZE_OUTPUT,\
        top_k, \
        which_implementation, \
        gpu_id

    #All File Path
    root_folder = '/home/khaledkucse/Project/backup/damca'
    train_dataset_file_path = root_folder+'/dataset/train_dataset'

    test_dataset_file_path = root_folder+'/dataset/test_dataset/1.txt'

    input_vocab_file_path = root_folder+'/vocabulary/input.vocab'

    output_vocab_file_path = root_folder+'/vocabulary/output.vocab'

    checkpoints_folder_path = root_folder+'/training_checkpoints'

    model_file_path = root_folder+"/model/model.h5"

    test_dataset_output_file_path = root_folder+"/result/eclipse_bilstm_c3_attn_bs_top_10_cross_1.txt"


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

    l1_regularization = 0.0001

    l2_regularization = 0.0001

    loss = 'categorical_crossentropy'

    optimizer = 'rmsprop'

    metrics = ['accuracy']

    top_k = 10

    #Options: keras, tf_estimator
    which_implementation = 'tf_estimator'

    #GPU_ID when GPU is available:
    gpu_id = "1"

