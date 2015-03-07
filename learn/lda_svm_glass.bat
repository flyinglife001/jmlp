
java -jar learn.jar -Xms512m -Xmx1440m -train_mode lda_rmh -seed 1 -file ..\uci\Iris\train_data.dat -machine lda;rmh -degree 0 -eval accuracy;prec;recall;f1 -kfold 1

