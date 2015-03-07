package classifier;

import java.util.PriorityQueue;

import cmd.General;
import core.DataSet;
import core.OutFile;
import core.Machine;
import core.Utility.IntValue;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import mat.Vec;


public class NaiveBayes extends Machine implements java.io.Externalizable {
    double[][] _condition_prob;
    double[] _prior_prob;

    public NaiveBayes() {
    }

    public void build() {
        _condition_prob = new double[_n_outputs][_n_inputs];
        _prior_prob = new double[_n_outputs];

        OutFile.printf("build Naive Bayes classifier ..\n");
    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        // ==============================================================================
        int n_examples = train_data._n_rows;
        int i, label, j;
        for (i = 0; i < n_examples; i++) {
            label = train_data.get_label(i);
            Vec.plus_equal(_condition_prob[label], train_data.get_X(i));
            _prior_prob[label]++;
        }

        for (i = 0; i < _n_outputs; i++) {
            _prior_prob[i] = Math.log((1.0 + _prior_prob[i]) / (n_examples + _n_outputs));

            double[] prob = _condition_prob[i];
            double norm_sum = Vec.sum(prob);
            for (j = 0; j < _n_inputs; j++) {
                prob[j] = Math.log((prob[j] + 1) / (norm_sum + _n_inputs));
            }
        }

        //OutFile.printf(_condition_prob);

        return 0.0f;
    }


    // fill the outputs to Mat* outputs;
    public double[] forward(double[] input) {
        double[] outputs = new double[_n_outputs];
        int i;
        for (i = 0; i < _n_outputs; i++) {
            outputs[i] = _prior_prob[i] + Vec.dot(input, _condition_prob[i]);
        }

        return outputs;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _condition_prob = (double[][]) in.readObject();
        _prior_prob = (double[]) in.readObject();

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);
        out.writeObject(_condition_prob);
        out.writeObject(_prior_prob);
    }

}
