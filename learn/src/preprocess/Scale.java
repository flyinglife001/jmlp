package preprocess;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import cmd.General;
import core.*;

public class Scale extends Machine {
    private double[] _min_value, _max_value;

    public Scale() {
        OutFile.printf("preprocessing the data input by Scaling to [-1,+1]\n");
    }

    public void build() {
        _min_value = new double[_n_inputs];
        _max_value = new double[_n_inputs];

    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        Utility.assign(_min_value, Double.MAX_VALUE);
        Utility.assign(_max_value, -Double.MAX_VALUE);

        int n_examples = train_data._n_rows, i, j;
        double[] inputs;
        double value = 0f;

        //OutFile.printf("begin to print input:\n");

        for (i = 0; i < n_examples; i++) {
            inputs = train_data.get_X(i);

            //OutFile.printf(inputs);

            for (j = 0; j < _n_inputs; j++) {
                value = inputs[j];

                if (Double.isNaN(value))
                    continue;

                if (value < _min_value[j]) {
                    _min_value[j] = value;
                } else if (value > _max_value[j]) {
                    _max_value[j] = value;
                }
            }
        }

        for (j = 0; j < _n_inputs; j++)// avoid to zero as denominator
        {
            //OutFile.printf("%d: min: %f max: %f\n",j,_min_value[j],_max_value[j]);

            if (_max_value[j] - _min_value[j] < General.SMALL_CONST) {
                _max_value[j] = 1;
            } else {
                _max_value[j] = _max_value[j] - _min_value[j];
            }
        }

        return 0.0f;
    }

    public double[] forward(double[] input) {
        //OutFile.printf(input);

        double[] outputs = new double[_n_inputs];
        for (int i = 0; i < _n_inputs; i++) {
            if (Double.isNaN(input[i])) {
                outputs[i] = input[i];
            } else {
                outputs[i] = -1.0 + 2 * (input[i] - _min_value[i])
                        / _max_value[i];
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

        _min_value = (double[]) in.readObject();
        _max_value = (double[]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_min_value);
        out.writeObject(_max_value);
    }

}
