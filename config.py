import torch


def init():
    global train_dataset_file_path, \
        test_dataset_file_path,\
        input_vocab_file_path, \
        output_vocab_file_path, \
        test_dataset_output_file_path,\
        MAX_LENGTH_Input, \
        MAX_LENGTH_Output, \
        SOS_token,\
        EOS_token, \
        UNK_token,\
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
        reccurent_dropout,\
        loss,\
        optimizer,\
        metrics,\
        dict_size,\
        model_file_path, \
        MAX_VOCAB_SIZE_INPUT, \
        MAX_VOCAB_SIZE_OUTPUT,\
        top_k

    #All File Path
    train_dataset_file_path = 'dataset/train_dataset'

    test_dataset_file_path = 'dataset/test_dataset/2.txt'

    input_vocab_file_path = 'vocabulary/input.vocab'

    output_vocab_file_path = 'vocabulary/output.vocab'

    model_file_path = "model.h5"

    test_dataset_output_file_path = "results/netbeans_bilstm_c3_attn_bs_top_10_cross_2.txt"


    #Vocabualry and COrpus related parameter
    MAX_LENGTH_Input = 40

    MAX_LENGTH_Output = 5

    MAX_VOCAB_SIZE_INPUT = 50000

    MAX_VOCAB_SIZE_OUTPUT = 5000

    UNK_token = 0

    SOS_token = 1

    EOS_token = 2


    #Training related parameter
    learning_Rate = 0.01

    hidden_size = 512

    embedding_width = 264

    batch_size = 64

    epochs = 10

    validation_split = 0.1

    dropout = 0.25

    reccurent_dropout = 0.25

    loss = 'categorical_crossentropy'

    optimizer = 'rmsprop'

    metrics = ['accuracy']

    top_k = 10

