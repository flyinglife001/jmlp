
java -jar learn.jar -Xms512m -Xmx1440m -train_mode text_learn -file  ..\20news-bydate\20news-bydate-train -test ..\20news-bydate\20news-bydate-test -machine nb   -n_select 1000 -eval accuracy;prec;recall;f1

