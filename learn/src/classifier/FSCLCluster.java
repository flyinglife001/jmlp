package classifier;

import cmd.General;
import core.Utility.IntValue;
import core.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Random;

import mat.Vec;

public class FSCLCluster extends Machine {
    private int _n_clusters, _seed, _verbose;
    public double[][] _centers;
    private Random _rand;

    public FSCLCluster() {
        _n_clusters = Integer.parseInt(General.get("-num"));
        _verbose = Integer.parseInt(General.get("-verbose"));
        _seed = Integer.parseInt(General.get("-seed"));
        _rand = new Random(_seed);

    }

    public void build() {
        _centers = new double[_n_clusters][_n_inputs];
        refresh();
    }

    public void refresh() {
        Utility.assign(_centers, 0.0f);
    }

    private void initialize(DataSet data) {
        if (_n_clusters == 1)
            return;

        int n_examples = data._n_rows;
        int m, k;
        double[] input;
        if (n_examples < _n_clusters) {
            if (n_examples == 0)
                return;

            for (m = 0; m < _n_clusters; m++) {
                input = data.get_X(_rand.nextInt(n_examples));
                _centers[m] = input.clone();
            }
        } else {
            int[] di = new int[_n_clusters];// index of orginal data for each
            // cluster
            boolean OK;
            for (m = 0; m < _n_clusters; m++) {
                do {
                    // Grant the same random genertor.
                    di[m] = _rand.nextInt(n_examples);
                    // see if it coincides with any former prototype or not
                    OK = true;
                    for (k = 0; k < m; k++) {
                        if (di[m] == di[k]) {
                            OK = false;
                            break;
                        }
                    }
                }
                while (!OK);

                input = data.get_X(di[m]);
                _centers[m] = input.clone();
            }
        }
    }

    public double train(DataSet data) {
        _n_inputs = data._n_cols;
        _n_outputs = data._n_classes;
        build();

        int n, i, n_examples = data._n_rows;

        Utility.assign(_centers, 0.0f);
        double[] input;
        if (_n_clusters == 1) // one cluster, mean of all samples
        {
            for (n = 0; n < n_examples; n++) {
                input = data.get_X(n);
                for (i = 0; i < _n_inputs; i++) {
                    _centers[0][i] += input[i];
                }
            }

            for (i = 0; i < _n_inputs; i++)
                _centers[0][i] /= n_examples;

            double vari = 0;
            for (n = 0; n < n_examples; n++) {
                vari += Vec.distance(data.get_X(n), _centers[0]);
            }
            vari /= n_examples; // variance

            // XFile.printf( "fscl %8.2f\n", vari);
            // centers->printfObject();
            return vari;
        }

        initialize(data);

        double[] frequ = new double[_n_clusters];
        Utility.assign(frequ, 1f);

        ArrayList<Integer> index_set = Utility.shuffle(n_examples, _rand);

        double rate0 = 0.2f, rate, total = 0f;
        IntValue sv;
        for (int cycle = 0; cycle < 50; cycle++) {
            rate = rate0 * (50 - cycle) / 50;
            total = 0f;
            for (n = 0; n < n_examples; n++) {
                input = data.get_X(index_set.get(n));
                sv = sensitive(input, frequ);
                frequ[sv._index]++;
                total += sv._value;
                for (i = 0; i < _n_inputs; i++)
                    _centers[sv._index][i] += rate
                            * (input[i] - _centers[sv._index][i]);
            }
            total /= n_examples;
        }

        //if (_verbose > 0)
        //OutFile.printf("fscl %8.2f\n", total);

        return total;
    }

    private IntValue sensitive(double[] input, double[] frequ) {
        int index = 0, m;
        double dist, dmin = Double.MAX_VALUE;

        for (m = 0; m < _n_clusters; m++) {
            // Multiply the frequency
            dist = Vec.distance(input, _centers[m]) * Math.sqrt(frequ[m]);
            if (dist < dmin) {
                index = m;
                dmin = dist;
            }
        }
        // The renew original vector.
        if (frequ[index] > 0)
            dmin /= Math.sqrt(frequ[index]);

        return new IntValue(index, dmin);
    }

}
