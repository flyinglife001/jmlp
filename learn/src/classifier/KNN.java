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

/**
 * KNN classifier.</br>
 *
 * @author XiaoBo Jin
 */

public class KNN extends Machine implements java.io.Externalizable {
    private int _K;
    double[][] _prototypes;
    int[] _labels;

    public KNN() {
    }

    public void build() {
        _K = Integer.parseInt(General.get("-num"));
        OutFile.printf("build KNN classifier with %d nearest points..\n", _K);
    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        // ==============================================================================
        int n_examples = train_data._n_rows;
        int i;

        _prototypes = new double[n_examples][];
        _labels = new int[n_examples];
        for (i = 0; i < n_examples; i++) {
            _prototypes[i] = train_data.get_X(i);
            _labels[i] = train_data.get_label(i);
        }

        return 0.0f;
    }


    // fill the outputs to Mat* outputs;
    public double[] forward(double[] input) {
        double[] outputs = new double[_n_outputs];
        int n_examples = _prototypes.length, i;
        if (n_examples <= _K) {
            for (i = 0; i < n_examples; i++) {
                outputs[_labels[i]]++;
            }
            return outputs;
        }

        //build the heap with the least element in the root or the head of the queue.
        PriorityQueue<IntValue> min_queue = new PriorityQueue<IntValue>();
        for (i = 0; i < _K; i++) {
            min_queue.add(new IntValue(_labels[i], -Vec.distance(input, _prototypes[i])));
        }

        for (i = _K; i < n_examples; i++) {
            min_queue.remove();
            min_queue.add(new IntValue(_labels[i], -Vec.distance(input, _prototypes[i])));
        }

        for (IntValue iv : min_queue) {
            outputs[iv._index]++;
        }

        return outputs;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _prototypes = (double[][]) in.readObject();
        _labels = (int[]) in.readObject();

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);
        out.writeObject(_prototypes);
        out.writeObject(_labels);
    }

}
