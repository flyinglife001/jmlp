
java -jar learn.jar -Xms512m -Xmx1440m -train_mode pca_rmh -seed 1 -file ..\uci\Iris\train_data.dat -machine pca;rmh -n_extract 2 -eval accuracy;prec;recall;f1 -kfold 1

