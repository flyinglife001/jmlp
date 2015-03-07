package classifier;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Random;

import mat.Vec;

import cmd.General;

import core.DataSet;
import core.Machine;
import core.OutFile;
import core.Utility;

public class MLP extends Machine implements java.io.Externalizable {
    double[][] _W1, _W2;
    double[] _b1, _b2;
    int _n_hidden;
    double[] _activation;

    public MLP() {
    }

    public void build() {
        _n_hidden = Integer.parseInt(General.get("-hidden"));
        _W1 = new double[_n_hidden][_n_inputs];
        _b1 = new double[_n_hidden];

        _W2 = new double[_n_outputs][_n_hidden];
        _b2 = new double[_n_outputs];

        _activation = new double[_n_hidden];

        OutFile.printf("build MLP classifier with %d hidden unit...\n", _n_hidden);
    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        int seed = Integer.parseInt(General.get("-seed"));

        int i, j, t, k;

        Random rand = new Random(seed);
        double r1 = 1 / Math.sqrt(_n_inputs), r2 = 1 / Math.sqrt(_n_hidden);
        //initialize the weights
        for (i = 0; i < _n_hidden; i++) {
            for (j = 0; j < _n_inputs; j++) {
                _W1[i][j] = (rand.nextDouble() * 2 - 1) * r1;
            }
            for (j = 0; j < _n_outputs; j++) {
                _W2[j][i] = (rand.nextDouble() * 2 - 1) * r2;
            }
            _b1[i] = (rand.nextDouble() * 2 - 1) * r1;
        }

        for (j = 0; j < _n_outputs; j++) {
            _b2[j] = (rand.nextDouble() * 2 - 1) * r2;
        }

        // ==============================================================================
        int n_examples = train_data._n_rows, id, label;
        ArrayList<Integer> mix_subset = Utility.shuffle(n_examples, rand);

        double[] outputs;
        double[] X;

        double[] delta_weights = new double[_n_hidden * (_n_inputs + _n_outputs + 1) + _n_outputs];
        double[] delta_hidden = new double[_n_hidden];

        double der_outputs;
        //learning rate
        double lr = Double.parseDouble(General.get("-slr")), current_lr = lr;
        int max_iter = Integer.parseInt(General.get("-siter")), iter = 0;
        double momentum = 0.9;
        double prev_error = 1, error = 0;
        for (iter = 0; iter < max_iter; iter++) {
            prev_error = error;
            error = 0;
            for (t = 0; t < n_examples; t++) {
                id = mix_subset.get(t);
                X = train_data.get_X(id);

                //OutFile.printf(X);

                label = train_data.get_label(id);

                //feed forward
                outputs = forward(X);
                //cll function
                error += -outputs[label];

                //backward propagation to the hidden units and update.

                Utility.assign(delta_hidden, 0);
                k = 0;
                for (i = 0; i < _n_outputs; i++) {
                    if (i == label) {
                        der_outputs = -1 + Math.exp(outputs[i]);
                    } else {
                        der_outputs = Math.exp(outputs[i]);
                    }

                    delta_weights[k] = current_lr * der_outputs + momentum * delta_weights[k];
                    //update the bias b2.
                    _b2[i] -= delta_weights[k++];

                    for (j = 0; j < _n_hidden; j++) {
                        delta_weights[k] = current_lr * der_outputs * _activation[j]
                                + momentum * delta_weights[k];

                        //compute the delta(partial E)/(partial activation)
                        //delta{j} = \sum W_{ij} delta{i};
                        delta_hidden[j] += _W2[i][j] * der_outputs;

                        _W2[i][j] -= delta_weights[k++];
                    }
                }

                //backward propagation to input units and update
                for (i = 0; i < _n_hidden; i++) {
                    der_outputs = delta_hidden[i] * (1 - _activation[i] * _activation[i]);

                    delta_weights[k] = current_lr * der_outputs + momentum * delta_weights[k];
                    _b1[i] -= delta_weights[k++];

                    for (j = 0; j < _n_inputs; j++) {
                        delta_weights[k] = current_lr * der_outputs * X[j] + momentum * delta_weights[k];
                        _W1[i][j] -= delta_weights[k++];
                    }

                    //OutFile.printf(_b2);

                }

            }
            error /= n_examples;

            if (iter % 5 == 0)
                OutFile.printf("the %d iter error: %f...\n", iter, error);

            if (Math.abs(error - prev_error) < General.SMALL_CONST)
                break;

            current_lr = lr / (1 + iter);
        }

        return 0.0f;
    }


    public double[] forward(double[] input) {
        double[] outputs = new double[_n_outputs];
        int i;

        //tanh(W1 x1 + b1)
        for (i = 0; i < _n_hidden; i++) {
            double value = Vec.dot(_W1[i], input) + _b1[i];
            _activation[i] = Math.tanh(Vec.dot(_W1[i], input) + _b1[i]);
        }

        //log softmax (W2 x2 + b2)
        for (i = 0; i < _n_outputs; i++) {
            outputs[i] = Vec.dot(_activation, _W2[i]) + _b2[i];
        }
        outputs = Utility.log_softmax(outputs);

        return outputs;
    }


    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        _n_hidden = in.readInt();
        build();

        _W1 = (double[][]) in.readObject();
        _b1 = (double[]) in.readObject();

        _W2 = (double[][]) in.readObject();
        _b2 = (double[]) in.readObject();

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);
        out.writeInt(_n_hidden);

        out.writeObject(_W1);
        out.writeObject(_b1);

        out.writeObject(_W2);
        out.writeObject(_b2);

    }
}

