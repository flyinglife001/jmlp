
java -jar learn.jar -Xms512m -Xmx1440m -file ..\uci\Glass\train_data.dat -num 5  -machine scale;mlp -eval accuracy;prec;recall;f1 -kfold 5 -repeat 10

