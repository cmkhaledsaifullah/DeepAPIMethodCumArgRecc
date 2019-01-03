# DeepAPIMethodCumArgRecc(DAMCA)
This is the full implementation of Deep API Method Cum Argument (DAMCA) Reccomendation. The program takes java project as input, collect AST information though the DAMCA Context Colelctor, take the ASTs as input at DAMCA Reccomendation and provide a list of method calls with arguments as the final output of the system.

## Getting Started
DAMCA is a Deep learning based method with argument reccomendation system. It uses Bi-Directional Long Short Term Memory (Bi-LSTM) based deep neural network to infer the method call sequence. Sequnce to sequence modelling (Encoder-decoder) is used where the input sequences are the previous code with thier syntactical and semantical information and output sequences are method call along with the arguments in a sequences. The detail technique along with the motivation, significance, novalty, related works and evaluation with current systems can be found at the following paper:

[Not publised Yet]

### Contents: 
There are three branches in this repository.

contextcollector: DAMCA Context Colelctor is the tool built in java with the help of symbol solver api of java parser that is responsible to collect the AST information of a java projects. It means it collect the input data from a project for the DAMCA Reccomendation tool. It also collect information for related works(SLP and SLAMC).

reccomender: The tool based on python scripts which implements Bi-directional LSTM based Encoder Decoder with Attention and Beam Search. It takes context returned from DAMCA COntext Collector. Keras library is used at the backend for devoloping the deep neural network.

javareccomender: An optional java based reccomender is developed. deeplearning4j is used at the backend for developing the encoder-decoder structure. The tool has no attention layer in it.

## Built With

* [JavaParser](http://javaparser.org/) - Java based parsing library
* [Keras](https://keras.io/) - Python library based on tensorflow for deep learning task
* [DeepLearning4j](https://deeplearning4j.org/) - Java framework for deep learning

## Authors

* **C M Khaled Saifullah** - *Initial work* - [khaledkucse](https://github.com/khaledkucse)

## License

This project is licensed under the GNU General Public License v3.0- see the [LICENSE](LICENSE) file for details

