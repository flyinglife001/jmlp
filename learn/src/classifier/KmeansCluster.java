package classifier;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


import mat.Vec;

import cmd.General;
import core.*;

public class KmeansCluster extends Machine implements java.io.Externalizable {
    private int _verbose;
    public double[][] _centers;
    private int bc_machine;

    public KmeansCluster() {
    }

    public void build() {
        _verbose = Integer.parseInt(General.get("-verbose"));
        _centers = new double[_n_outputs][_n_inputs];
        Utility.assign(_centers, 0.0f);
        String bc_name = General.get("-bc");
        if (bc_name.equals("hard")) {
            bc_machine = 1;
            OutFile.printf("build kmeans with hard assignment with %d outputs\n", _n_outputs);
        } else if (bc_name.equals("triangle")) {
            bc_machine = 2;
            OutFile.printf("build kmeans with triangle assignment with %d outputs\n", _n_outputs);
        } else {
            bc_machine = 0;
            OutFile.printf("build kmeans for mce classifier with %d cluster\n", _n_outputs);
        }
    }


    public double train(DataSet data) {
        _n_inputs = data._n_cols;
        _n_outputs = Integer.parseInt(General.get("-num"));

        build();

        FSCLCluster fscl = new FSCLCluster();
        double variance = fscl.train(data);

        _centers = Utility.clone(fscl._centers);

        if (_n_outputs == 1)
            return variance;

        int index, m, i, n, n_examples = data._n_rows;
        int[] subnum = new int[_n_outputs]; // number of samples in a cluster
        int[] label = new int[n_examples];

        boolean END = false;
        double[] inputs, outputs;
        int cluster_no, cycle;
        for (cycle = 0; cycle < 100; cycle++) // at most 100 sweeps
        {
            END = true; // remains 1 if no label is changed
            variance = 0.0f; // total square error
            for (n = 0; n < n_examples; n++) {
                inputs = data.get_X(n);
                outputs = forward(inputs);
                index = (int) outputs[0];
                variance += outputs[1];
                if (index != label[n]) // cluster label is changed
                {
                    END = false;
                    label[n] = index;
                }
            }
            variance /= n_examples; // average variance

            if (END) // no label changed
                break;

            // update the cluster centers to be centers of attracted samples
            Utility.assign(_centers, 0.0f);
            Utility.assign(subnum, 0);

            for (n = 0; n < n_examples; n++) {
                inputs = data.get_X(n);
                cluster_no = label[n];
                for (i = 0; i < _n_inputs; i++)
                    _centers[cluster_no][i] += inputs[i];
                subnum[cluster_no]++;
            }

            for (m = 0; m < _n_outputs; m++) {
                if (subnum[m] == 0)
                    continue;
                for (i = 0; i < _n_inputs; i++)
                    _centers[m][i] /= subnum[m];
            }

            if (cycle % 5 == 0 && _verbose > 0) {
                OutFile.printf("kmeans %2d  %8.2f\n", cycle, variance);
            }
        }

        //if (_verbose > 0)
        //OutFile.printf("kmeans variance: %8.2f\n", variance);

        return variance;
    }

    // fill the outputs to Mat* outputs;
    public double[] forward(double[] input) {
        double[] outputs;
        if (_n_outputs < 2) {
            outputs = new double[2];
        } else {
            outputs = new double[_n_outputs];
        }

        double dmin = Double.MAX_VALUE, dist;

        int index = 0, m;
        double avg_z = 0, value;

        for (m = 0; m < _n_outputs; m++) {

            dist = Vec.distance(input, _centers[m]);
            if (dist < dmin) {
                dmin = dist;
                index = m;
            }

            value = Math.sqrt(dist);
            outputs[m] = value;
            avg_z += value;
        }
        avg_z /= _n_outputs;

        switch (bc_machine) {
            case 1:
                Utility.assign(outputs, 0);
                outputs[index] = 1;
                return outputs;
            case 2:
                for (m = 0; m < _n_outputs; m++) {
                    value = avg_z - outputs[m];
                    if (value < 0) {
                        outputs[m] = 0;
                    } else {
                        outputs[m] = value;
                    }
                }
                break;
            default:
                outputs[0] = index;
                outputs[1] = dmin;
        }


        return outputs;
    }


    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        build();

        _centers = (double[][]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_centers);
    }
}
