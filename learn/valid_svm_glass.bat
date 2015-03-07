
java -jar learn.jar -Xms512m -Xmx1440m -train_mode svm_model -file ..\uci\Glass\train_data.dat -C 2;4;6;8;10 -g -1;-3;-5;-7;-9  -machine svm -dataset ds_name.txt -eval accuracy;prec;recall;f1 -kfold 1

