package classifier;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import cmd.General;
import core.DataSet;
import core.Machine;

import core.OutFile;
import libsvm.*;
import libsvm.svm.*;
import mat.Vec;

public class SVM extends Machine {
    class SupportVector {
        double _alpha;
        svm_node[] _sv;
    }

    svm_parameter _param;
    svm_node[] _temp;
    ArrayList<SupportVector> _sv_list = new ArrayList<SupportVector>();
    double[] _b;
    int[] _sv_margin;

    public SVM() {
        _param = new svm_parameter();

    }

    public void build() {
        // default values
        _param.shrinking = 1; // do shrinking;
        _param.svm_type = svm_parameter.C_SVC;// for classificaton.
        _param.eps = 1e-3;
        _param.probability = 0;
        _param.nr_weight = 0;
        _param.weight_label = new int[0];
        _param.weight = new double[0];
        _param.nu = 0.5;
        _param.p = 0.1;
        _param.probability = 0;

        _param.cache_size = Double.parseDouble(General.get("-cache"));

        int degree = Integer.parseInt(General.get("-degree"));
        if (degree > 0) {
            _param.kernel_type = svm_parameter.POLY;
            _param.degree = degree;
            _param.gamma = Double.parseDouble(General.get("-g"));
            _param.coef0 = Double.parseDouble(General.get("-b"));

            OutFile.printf("build SVM with ploynomial kernel gamma:%.2f b: %.2f degree: %d\n", _param.gamma,
                    _param.coef0, _param.degree);
        } else {
            _param.kernel_type = svm_parameter.RBF;

            _param.C = Math.pow(2, Double.parseDouble(General.get("-C")));
            _param.gamma = Math.pow(2, Double.parseDouble(General.get("-g")));
            OutFile.printf("build SVM with RBF kernel with gamma: %.2f C: %.2f\n",
                    _param.gamma, _param.C);

        }

        _sv_list.clear();
        _b = new double[_n_outputs];
        _sv_margin = new int[_n_outputs];

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

            // OutFile.printf(feature);

            svm_node[] x;

            int n_nodes = feature.length, index = 0;
            for (j = 0; j < n_nodes; j++) {
                // is not zero?
                if (Math.abs(feature[j]) > 1e-6) {
                    _temp[index] = new svm_node();
                    // feature index starts with 1.
                    _temp[index].index = j + 1;
                    _temp[index].value = feature[j];
                    index++;
                }
            }

            x = new svm_node[index];
            System.arraycopy(_temp, 0, x, 0, index);

            prob.x[i] = x;
        }

        int label;

        for (j = 0; j < _n_outputs; j++) {
            train_data.set_class_index(j);
            for (i = 0; i < n_examples; i++) {
                label = train_data.get_label(i);
                if (label > 0) {
                    prob.y[i] = +1;
                } else {
                    prob.y[i] = -1;
                }
            }

            // 0 <= alpha_i <= Cp for y_i = 1
            // 0 <= alpha_i <= Cn for y_i = -1
            decision_function fun = svm.svm_train_one(prob, _param, _param.C,
                    _param.C);

            for (i = 0; i < n_examples; i++) {
                // non-support vector.
                if (Math.abs(fun.alpha[i]) < 1e-6)
                    continue;

                SupportVector sv = new SupportVector();
                sv._alpha = fun.alpha[i];
                sv._sv = prob.x[i];

                _sv_list.add(sv);
            }

            _sv_margin[j] = _sv_list.size();

            // rho = - b;
            _b[j] = -fun.rho;

            train_data.reset_class_index();
        }

        OutFile.printf("Total SV: %d\n", _sv_list.size());

        return 0f;
    }

    public double[] forward(double[] input) {
        svm_node[] x;
        int i, j;

        int n_nodes = input.length, index = 0;
        for (j = 0; j < n_nodes; j++) {
            // is not zero?
            if (Math.abs(input[j]) > 1e-6) {
                // feature index starts with 1.
                _temp[index] = new svm_node();
                _temp[index].index = j + 1;
                _temp[index].value = input[j];
                index++;
            }
        }

        x = new svm_node[index];
        System.arraycopy(_temp, 0, x, 0, index);

        double[] outputs = new double[_n_outputs];
        int begin = 0, end;
        double sum = 0f;
        for (i = 0; i < _n_outputs; i++) {
            end = _sv_margin[i];

            sum = 0;
            for (j = begin; j < end; j++) {
                SupportVector sv = _sv_list.get(j);
                sum += sv._alpha * svm.eval_kernel(x, sv._sv, _param);
            }

            outputs[i] = sum + _b[i];

            begin = end;
        }

        return outputs;

    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _param = (svm_parameter) in.readObject();
        _sv_margin = (int[]) in.readObject();
        _b = (double[]) in.readObject();
        _sv_list = (ArrayList<SupportVector>) in.readObject();

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_param);
        out.writeObject(_sv_margin);
        out.writeObject(_b);
        out.writeObject(_sv_list);
    }

}
