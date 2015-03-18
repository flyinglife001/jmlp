# jmlp
Java Machine Learning Platform, author: Xiao-Bo Jin  Any question is welcome, please contact: jxb9801 at 126 dot com

Jmlp is a java platform for both of the machine learning experiments and application. I have tested it on the window platform. But it should be applicable in the linux platform due to the cross-platform of Java language. It contains the classical classification algorithm (Discrete AdaBoost.MH, Real AdaBoost.MH, SVM, KNN, MCE,MLP,NB) and feature reduction(KPCA,PCA,Whiten) etc.

You can use it for the machine learning experiment and search the optimal parameters.

It also can excute the text categorization application (Chinese and English).

Final, it contain a GUI for the test procedure.


#Install 

1.  Your version of JDK is required to be higher than 1.7. It also needs the package: [libsvm](http://www.csie.ntu.edu.tw/~cjlin/libsvm/), [jblas](http://mikiobraun.github.io/jblas/), [lucene](http://lucene.apache.org/), [IKAnalyzer](http://code.google.com/p/ik-analyzer/) in the lib.
2.  Copy the files in you directory.
3.  Run ./learn/src/utility/TestCase.java.
4.  Enjoy.

P.S. if you want to run on 20 newsgroup. please download:
the dataset from:  http://qwone.com/~jason/20Newsgroups/20news-bydate.tar.gz
and place them in the location as the following form:
.\20news-bydate\20news-bydate-test\alt.atheism <br />

or run Chinese text corpus, you can visit [Corpus on People's Daily](http://www.icl.pku.edu.cn/icl_res/) for Chinese text categorization.


You also can run .bat in the form of command line on the window platform, or change .bat into .sh and run it on linux platform. But please attention to the location of the dataset file.

#Demo 

The results is obtained by the .bat file listed in the final column. 

===========================================================================================<br/>
nb_text.bat: 
java -jar learn.jar -Xms512m -Xmx1440m -train_mode text_learn -file  ..\20news-bydate\20news-bydate-train -test ..\20news-bydate\20news-bydate-test -machine nb   -n_select 1000 -eval accuracy;prec;recall;f1
<br/>
mlp_text.bat: 
java -jar learn.jar -Xms512m -Xmx1440m -train_mode text_learn -seed 1 -file ..\20news-bydate\20news-bydate-train -test ..\20news-bydate\20news-bydate-test -machine tfidf;mlp -seed 1 -n_select 1000 -eval accuracy;prec;recall;f1
<br/>
svm_text.bat: java -jar learn.jar -Xms512m -Xmx1440m -train_mode text_learn -file ..\20news-bydate\20news-bydate-train -test ..\20news-bydate\20news-bydate-test -machine tfidf;svm   -n_select 1000 -eval accuracy;prec;recall;f1 <br/>
===========================================================================================<br/>
Test text categorization on 20NG(English texts)<br />
classifier	accuracy	prec	recall	f1	bat file<br />
nb	73.10	73.14	72.28	72.29	"nb_text.bat"<br />
mlp	73.75	73.74	73.18	73.28	"mlp_text.bat"<br />
svm	76.09	75.49	75.10	75.01	"svm_text.bat"<br />


Test cross_fold  on iris with svm and rmh<br />
classifier	accuracy	prec	recall	f1	bat file<br />
svm	96.67	95.95	96.68	96.20	"cross_fold_svm_iris.bat"<br />
rmh	94.67	94.93	93.11	93.60	"cross_fold_rmh_iris.bat"<br />
dmh	96.00	95.76	95.21	95.40	"cross_fold_dmh_iris.bat"<br />


Test batch processing on multiple dataset with dmh.<br />
classifier	accuracy	prec	recall	f1	bat file<br />
Diabetes	76.03	73.77	72.42	72.76	"batch_repeat_dmh.bat"<br />
Glass	71.22	64.19	64.33	62.06	<br />
Iris	94.00	94.03	94.27	93.78	<br />
Average 	80.42	77.33	77.00	76.20	<br />


Test model selection on the Glass with svm. log2C and log2gamma were set to {10,8,6,4,2} and {-1,-3,-5,-7,-9}. <br />
the optimal parameter is  -C:8  -g:-7 <br />
training result:  accuracy=0.7757 avg-prec=0.7058  recall=0.7491  f1=0.7259<br />
bat_file is: "valid_svm_glass.bat"<br />
valid the result under the parameter (8,-7) is accuracy=77.57 prec=70.58 recall=74.91 f1=72.59<br />
bat file is: "valid_svmpara_glass.bat"<br />


Preprocess the dataset with none, norm and scale on glass dataset.<br />
glass	accuracy	prec	recall	f1	bat file<br />
none	32.83	05.47	16.67	08.19	"preprocess_mlp_glass.bat"<br />
norm	66.36	62.33	62.10	60.03	<br />
scale	62.61	49.21	49.51	47.58	<br />


Train model and test model<br />
20ng	accuracy	prec	recall	f1	bat file<br />
train	82.90	83.15	82.60	82.74	"train_nb_20ng.bat"<br />
test	73.10	73.14	72.28	72.29	"test_nb_20ng.bat"<br />


Lda and pca training accuracy on iris dataset <br />
glass	accuracy	prec	recall	f1	bat file<br />
pca(2)	100.00	100.00	100.00	100.00	"pca_rmh_iris.bat"<br />
lda	100.00	100.00	100.00	100.00	"lda_rmh_iris.bat"<br />

