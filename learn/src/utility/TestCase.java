package utility;

import core.OutFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Created by pal on 2015-01-03.
 */
public class TestCase {
    public static void test_bat(String bat_name) {
        try {
            Process process = Runtime.getRuntime().exec(bat_name);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process
                            .getInputStream()));
            String console_output;
            while ((console_output = bufferedReader.readLine()) != null) {
                OutFile.printf("%s\n", console_output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        String s = "nb_text.bat"; //test the Naive Bayes classifier in 20newsgroup.
        //String s = "mlp_text.bat";//test mlp (multi-layer perceptron) in 20NG.
        //String s = "svm_text.bat";//test svm in 20newsgroup.
        //String s = "cross_fold_svm_iris.bat";//test kfold in iris with svm model.
        //String s = "cross_fold_rmh_iris.bat";//test kfold in rmh with rmh model.
        //String s = "cross_fold_dmh_iris.bat";//test kfold in rmh with dmh model.
        //String s = "batch_repeat_dmh.bat";//repeat to run 5-fold svm on multiple datasets.
        //String s = "valid_svm_glass.bat";//model selection for the svm model on glass.
        //String s = "valid_svmpara_glass.bat";//valid svm model on glass with the optimal parameters.
        //String s = "preprocess_mlp_glass.bat";//test preprocess (norm and scale) on glass.
        //String s = "train_nb_20ng.bat";//train the naive bayes on 20 ng dataset.
        //String s = "test_nb_20ng.bat";//test the naive bayes on 20 ng dataset.
        //String s = "lda_svm_glass.bat";//test the lda on glass for the feature reduction.
        //String s = "pca_rmh_iris.bat";//test the pca on glass for the feature reduction.
        //String s = "cross_fold_rmh_chinese.bat";//test the chinese text.
        //String s = "cross_fold_svm_chinese.bat";//test the chinese text.
        test_bat(s);
    }
}
