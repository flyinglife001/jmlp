package text;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import cmd.General;
import core.*;

public class TFIDF extends Machine implements java.io.Externalizable {
    private double[] _idf;

    public TFIDF() {
        OutFile.printf("preprocessing the data input by TFIDF\n");
    }

    public void build() {

        _idf = new double[_n_inputs];
        refresh();
    }

    public void refresh() {
        Utility.assign(_idf, 0);
    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        int i, j, n_examples = train_data._n_rows;

        double[] feature;
        double value;
        for (i = 0; i < n_examples; i++) {
            feature = train_data.get_X(i);

            for (j = 0; j < _n_inputs; j++) {
                value = feature[j];

                if (Double.isNaN(value))
                    continue;

                if (value >= 1) {
                    _idf[j]++;
                }
            }
        }

        //OutFile.printf("df: %d\n",n_examples);
        for (j = 0; j < _n_inputs; j++) {
            value = _idf[j];

            //OutFile.printf("%f ",value);

            if (value >= 1) {
                _idf[j] = Math.log(n_examples / value);
            } else {
                _idf[j] = 0;
            }
        }

        //OutFile.printf("\n");

        return 0f;
    }

    public double[] forward(double[] input) {
        double[] outputs = new double[_n_inputs];
        // normalize the tf.

        //OutFile.printf("in: %d _n_in: %d\n",input.length,_n_inputs);

        double sum = 1e-6;
        int i;
        for (i = 0; i < _n_inputs; i++) {
            if (Double.isNaN(input[i])) {
                outputs[i] = Double.NaN;
                sum += 0;
            } else {
                outputs[i] = input[i] * _idf[i];
                sum += outputs[i] * outputs[i];
            }
        }

        sum = Math.sqrt(sum);

        for (i = 0; i < _n_inputs; i++) {
            if (!Double.isNaN(input[i])) {
                outputs[i] /= sum;
            }
        }

        //OutFile.printf(outputs);

        return outputs;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _idf = (double[]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_idf);
    }

}
