
java -jar learn.jar -Xms512m -Xmx1440m -file ..\uci\Iris\train_data.dat -seed 1 -machine rmh   -n_select 1000 -eval accuracy;prec;recall;f1 -kfold 5

