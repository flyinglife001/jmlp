package core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import core.DataSet.LabelFormat;
import core.Utility.IntValue;
import cmd.General;

public class Evaluation {
    private int _verbose;
    private String _output_dir;
    private String[] _method;
    private int _n_examples;

    // confusion matrix: row=> true label col=> predict label
    private int[][] _con_mat;

    public Evaluation(String[] method, String output_name) {
        _output_dir = output_name;
        _method = method;

        _verbose = Integer.parseInt(General.get("-verbose"));
    }

    private void confusion_matrix(DataSet test_data) {
        int n_classes = test_data._n_classes;
        _con_mat = new int[n_classes][n_classes];

        _n_examples = test_data._n_rows;

        // the row is target, the col is outputs
        int n_examples = test_data._n_rows, label, target;

        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < n_examples; i++) {

            //OutFile.printf(test_data.get_X(i));

            label = Utility.arg_max(test_data.get_X(i));

            if (test_data._label_format == LabelFormat._null) {
                target = test_data.get_label(i);
            } else {
                target = test_data.get_y(i)[0];
            }

            _con_mat[target][label]++;

            sb.append("Predict ");
            sb.append(test_data.get_class_name(label));
            sb.append(" True ");
            sb.append(test_data.get_class_name(target));
            sb.append("\n");

        }

        //OutFile.printf(_con_mat);

        if (_output_dir.length() > 0) {
            OutFile.set(_output_dir + "\\outputs.txt");
            OutFile.printf(sb.toString());
            OutFile.printf(_con_mat);
            System.out.printf("predict => %s\\outputs.txt\n", _output_dir);
            OutFile.close();
        }

    }

    private double precision() {
        double value = 0.0f;
        int len = _con_mat.length, i, j;
        double prec;
        for (j = 0; j < len; j++) {
            prec = General.SMALL_CONST;
            for (i = 0; i < len; i++) {
                prec += _con_mat[i][j];
            }
            prec = _con_mat[j][j] / prec;

            value += prec;
        }

        value /= len;
        return value;
    }

    private double recall() {
        double value = 0.0f;
        int len = _con_mat.length, i, j;
        double rec;
        for (i = 0; i < len; i++) {
            rec = General.SMALL_CONST;
            for (j = 0; j < len; j++) {
                rec += _con_mat[i][j];
            }
            rec = _con_mat[i][i] / rec;

            value += rec;
        }

        value /= len;
        return value;
    }

    private double f1() {
        double value = 0.0f;
        int len = _con_mat.length, i, j;
        double[] rec = new double[len];
        double[] prec = new double[len];
        Utility.assign(prec, General.SMALL_CONST);

        double temp, mat_value;
        for (i = 0; i < len; i++) {
            temp = General.SMALL_CONST;
            for (j = 0; j < len; j++) {
                mat_value = _con_mat[i][j];
                temp += mat_value;
                prec[j] += mat_value;
            }
            rec[i] = _con_mat[i][i] / temp;
        }


        for (i = 0; i < len; i++) {
            prec[i] = _con_mat[i][i] / prec[i];
            value += 2 * rec[i] * prec[i] / (General.SMALL_CONST + prec[i] + rec[i]);
        }

        value /= len;
        return value;
    }

    private double accuracy() {
        double value = 0.0f;

        int i, len = _con_mat.length;
        for (i = 0; i < len; i++) {
            value += _con_mat[i][i];
        }

        value /= _n_examples;

        // OutFile.printf("the results:\n" + out_str);

        return value;
    }

    private void get_classify_results(DataSet data) {
        int j;
        int n_examples = data._n_rows, label, target;

        for (j = 0; j < n_examples; j++) {
            // OutFile.printf(data.get_X(j));

            label = Utility.arg_max(data.get_X(j));
            if (data._label_format == LabelFormat._null) {
                target = data.get_label(j);
            } else {
                target = data.get_y(j)[0];
            }

            StringBuilder sb = new StringBuilder("@");

            String fname = data.get_fileid(j);
            sb.append(fname);
            sb.append(" ");

            if (target != label) {
                sb.append("**");
            }

            sb.append("Predict ");
            sb.append(data.get_class_name(label));
            sb.append(" True ");
            sb.append(data.get_class_name(target));
            sb.append("\n");

            OutFile.printf(sb.toString());
        }
    }

    public double[] evaluate(DataSet test_data) {
        int len = _method.length, j;

        confusion_matrix(test_data);

        double[] avg_meas = new double[len];
        for (j = 0; j < len; j++) {
            String name = _method[j];
            String[] sub_str = name.split("@");
            int k_position = -1;
            if (sub_str.length > 1) {
                k_position = Integer.parseInt(name.split("@")[1]);
            }

            if (name.equals("accuracy")) {
                avg_meas[j] = accuracy();
                OutFile.printf("measure the dataset by accuracy=%.4f\n",
                        avg_meas[j]);
            } else if (name.equals("prec")) {
                avg_meas[j] = precision();
                OutFile.printf("measure the dataset by avg-prec=%.4f\n",
                        avg_meas[j]);
            } else if (name.equals("recall")) {
                avg_meas[j] = recall();
                OutFile.printf("measure the dataset by recall=%.4f\n",
                        avg_meas[j]);
            } else if (name.equals("f1")) {
                avg_meas[j] = f1();
                OutFile.printf("measure the dataset by f1=%.4f\n",
                        avg_meas[j]);
            } else {
                OutFile.error(
                        "you use the unspecified %s method for evaluation\n",
                        name);
                avg_meas[j] = 0;
            }
        }


        if (_verbose > 0)
            get_classify_results(test_data);

        return avg_meas;
    }
}
