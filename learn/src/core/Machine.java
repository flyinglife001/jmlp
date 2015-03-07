package core;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

import cmd.General;

public class Machine implements java.io.Externalizable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public int _n_inputs, _n_outputs;
    public double _dummy_input;

    public Machine() {
    }


    public void build() {
    }

    /*arg: train_data: the train data return the loss value*/
    public double train(DataSet train_data) {
        return 0.0f;
    }

    public double[] forward(double[] input) {
        return new double[_n_outputs];
    }

    //save results into the _X of data.
    public void test(DataSet data) {
        int n_examples = data._n_rows;
        ArrayList<double[]> results = new ArrayList<double[]>();
        for (int i = 0; i < n_examples; i++) {
            if (i % 5000 == 0)
                OutFile.printf("begin to forward: NO. %d example\n", i);

            results.add(forward(data.get_X(i)));
        }
        data.set_XY(results, null);

        //OutFile.printf("TFIDF dataset:\n");
        //data.print_dat_format();
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub

    }

}