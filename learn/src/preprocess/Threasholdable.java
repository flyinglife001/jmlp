package preprocess;

import cmd.General;
import core.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Hashtable;

public class Threasholdable extends Machine {
    private double[][] _threshold_value;
    private int _n_threshold;

    public Threasholdable() {
        _n_threshold = Integer.parseInt(General.get("-threshold")) + 4;
        OutFile.printf("preprocessing the data input by the smooth threshold\n");
    }

    public void build() {

        _threshold_value = new double[_n_threshold][];
        for (int i = 0; i < _n_threshold; i++) {
            _threshold_value[i] = new double[_n_inputs];
        }

    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        Utility.assign(_threshold_value[0], -Double.MAX_VALUE);
        Utility.assign(_threshold_value[1], Double.MAX_VALUE); // min value

        Utility.assign(_threshold_value[_n_threshold - 2], -Double.MAX_VALUE); // max
        // value
        Utility.assign(_threshold_value[_n_threshold - 1], Double.MAX_VALUE); // max
        // value

        int n_examples = train_data._n_rows, i, j;
        double[] inputs;
        double value = 0f;
        for (i = 0; i < n_examples; i++) {
            inputs = train_data.get_X(i);

            for (j = 0; j < _n_inputs; j++) {
                value = inputs[j];

                if (Double.isNaN(value))
                    continue;

                if (value < _threshold_value[1][j]) {
                    _threshold_value[1][j] = value;
                } else if (value > _threshold_value[_n_threshold - 2][j]) {
                    _threshold_value[_n_threshold - 2][j] = value;
                }
            }
        }

        double step;
        for (j = 0; j < _n_inputs; j++) {
            step = (_threshold_value[_n_threshold - 2][j] - _threshold_value[1][j])
                    / (_n_threshold - 3);

            for (i = 2; i < _n_threshold - 2; i++) {
                _threshold_value[i][j] = _threshold_value[1][j] + (i - 1)
                        * step;
            }
        }

        // XFile.printf(_threshold_value);

        return 0.0f;
    }

    public double[] forward(double[] input) {
        double[] outputs = new double[_n_inputs];
        int middle = 0, low, high;
        double value;
        for (int i = 0; i < _n_inputs; i++) {
            if (Double.isNaN(input[i])) {
                outputs[i] = input[i];
                continue;
            }

            low = 0;
            high = _n_threshold - 1;

            value = input[i];
            // T[i] <= x < T[i + 1], then f(x) = i;

            while (low < high) {
                middle = (low + high) / 2;
                if (value < _threshold_value[middle][i]) {
                    high = middle;
                } else if (value >= _threshold_value[middle + 1][i]) {
                    low = middle + 1;
                } else {
                    break;
                }
            }

            outputs[i] = middle;
        }
        // XFile.printf(input);
        // XFile.printf(outputs);
        return outputs;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _threshold_value = (double[][]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_threshold_value);
    }

}
