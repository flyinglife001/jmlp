package preprocess;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import cmd.General;
import core.Machine;
import core.DataSet;

import core.Utility;
import core.OutFile;

public class MeanMissing extends Machine {
    private double[] _means;

    public MeanMissing() {
        OutFile.printf("preprocessing the data input by filling missing value with mean value\n");
    }

    public void build() {

        _means = new double[_n_inputs];
        refresh();

    }

    public void refresh() {
        Utility.assign(_means, 0.0f);
    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        int i, j, n_examples = train_data._n_rows;
        double[] feature;
        double value;
        int real_examples[] = new int[_n_inputs];

        for (i = 0; i < n_examples; i++) {
            feature = train_data.get_X(i);
            for (j = 0; j < _n_inputs; j++) {
                value = feature[j];

                if (Double.isNaN(value))
                    continue;

                _means[j] += value;
                real_examples[j]++;

            }
        }

        for (i = 0; i < _n_inputs; i++) {
            _means[i] /= real_examples[i];
        }

        return 0.0f;
    }

    public double[] forward(double[] input) {
        double[] outputs = new double[_n_inputs];
        for (int i = 0; i < _n_inputs; i++) {
            if (Double.isNaN(input[i])) {
                outputs[i] = _means[i];
            }
        }

        return outputs;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _means = (double[]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_means);
    }

}
