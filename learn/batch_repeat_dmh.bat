
java -jar learn.jar -Xms512m -Xmx1440m -seed 1 -file ..\uci\Iris\train_data.dat -machine dmh -dataset ds_name.txt -eval accuracy;prec;recall;f1 -kfold 5 -repeat 10

