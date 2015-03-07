package classifier;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import cmd.General;
import core.DataSet;
import core.Machine;
import core.OutFile;
import libsvm.*;
import mat.Vec;

public class LibSVM extends Machine {
    svm_parameter _param;
    svm_model _model;
    svm_node[] _temp;
    boolean _need_dat_format;

    public LibSVM() {
        _param = new svm_parameter();

    }

    public void build() {
        // default values
        _param.shrinking = 1; //do shrinking;
        _param.svm_type = svm_parameter.C_SVC;//for classificaton.
        _param.eps = 1e-3;
        _param.probability = 0;
        _param.nr_weight = 0;
        _param.weight_label = new int[0];
        _param.weight = new double[0];
        _param.nu = 0.5;
        _param.p = 0.1;
        _param.probability = 0;

        _param.cache_size = Double.parseDouble(General.get("-cache"));

        _param.C = Math.pow(2, Double.parseDouble(General.get("-C")));

        int degree = Integer.parseInt(General.get("-degree"));
        if (degree > 0) {
            _param.kernel_type = svm_parameter.POLY;
            _param.degree = degree;
            _param.gamma = Double.parseDouble(General.get("-g"));
            _param.coef0 = Double.parseDouble(General.get("-b"));

            OutFile.printf("build SVM with ploynomial kernel gamma:%.2f b: %.2f degree: %d\n", _param.gamma,
                    _param.coef0, _param.degree);
        } else if (degree <= 0) {
            _param.kernel_type = svm_parameter.RBF;

            if (degree == 0) {
                _param.gamma = 1. / _n_inputs;
                OutFile.printf("Warning: you need to set -degree with the negative value except the default setting..\n");
                OutFile.printf("build SVM with RBF kernel with gamma: %.2f C: %.2f\n",
                        _param.gamma, _param.C);
            } else {
                _param.gamma = Math.pow(2, Double.parseDouble(General.get("-g")));
                OutFile.printf("build SVM with RBF kernel with gamma: %.2f C: %.2f\n",
                        _param.gamma, _param.C);
            }


        }

        _temp = new svm_node[_n_inputs];
    }


    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        int n_examples = train_data._n_rows, i, j;
        double[] feature;

        svm_problem prob = new svm_problem();
        prob.l = n_examples;
        prob.x = new svm_node[n_examples][];
        prob.y = new double[n_examples];

        for (i = 0; i < n_examples; i++) {
            feature = train_data.get_X(i);

            //OutFile.printf(feature);

            svm_node[] x;
            //whether it is svm format or not?
            if (Double.isInfinite(feature[0])) {
                int n_nodes = (feature.length - 1) / 2;
                x = new svm_node[n_nodes];
                for (j = 0; j < n_nodes; j++) {
                    x[j] = new svm_node();
                    x[j].index = (int) feature[2 * j + 1];
                    x[j].value = feature[2 * j + 2];
                }
            } else {
                int n_nodes = feature.length, index = 0;
                for (j = 0; j < n_nodes; j++) {
                    //is not zero?
                    if (Math.abs(feature[j]) > 1e-6) {
                        _temp[index] = new svm_node();
                        //feature index starts with 1.
                        _temp[index].index = j + 1;
                        _temp[index].value = feature[j];
                        index++;
                    }
                }

                x = new svm_node[index];
                System.arraycopy(_temp, 0, x, 0, index);
            }

            prob.x[i] = x;
            prob.y[i] = (double) train_data.get_label(i);

        }

        String error_msg = svm.svm_check_parameter(prob, _param);

        if (error_msg != null) {
            System.err.print("ERROR: " + error_msg + "\n");
            System.exit(1);
        }

        _model = svm.svm_train(prob, _param);

        OutFile.printf("Total SV: %d\n", _model.l);

        return 0f;
    }

    public double[] forward(double[] input) {
        svm_node[] x;
        int j;
        if (Double.isInfinite(input[0])) {
            int n_nodes = (input.length - 1) / 2;
            x = new svm_node[n_nodes];
            for (j = 0; j < n_nodes; j++) {
                x[j] = new svm_node();
                x[j].index = (int) input[2 * j + 1];
                x[j].value = input[2 * j + 2];
            }
        } else {
            int n_nodes = input.length, index = 0;
            for (j = 0; j < n_nodes; j++) {
                //is not zero?
                if (Math.abs(input[j]) > 1e-6) {
                    //feature index starts with 1.
                    _temp[index] = new svm_node();
                    _temp[index].index = j + 1;
                    _temp[index].value = input[j];
                    index++;
                }
            }

            x = new svm_node[index];
            System.arraycopy(_temp, 0, x, 0, index);
        }

//		for(int i = 0; i < x.length; i++)
//		{
//			OutFile.printf("%d:%f ",x[i].index,x[i].value);
//		}
//		OutFile.printf("\n");

        return svm.svm_predict_classify(_model, x);

    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _param = (svm_parameter) in.readObject();
        _model = (svm_model) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_param);
        out.writeObject(_model);
    }

}
