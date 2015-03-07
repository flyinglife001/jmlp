package classifier;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import cmd.General;
import core.Utility.IntValue;
import core.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.Math;


import mat.Vec;

public class MCE extends Machine implements java.io.Externalizable {
    private int _n_clusters, _sum_clusters;
    private double[][] _centers;
    private IntValue _min, _rival;
    private Hashtable<String, String> _option_dict;

    public MCE() {
        _n_clusters = Integer.parseInt(General.get("-num"));
        _min = new IntValue(0, 0f);
        _rival = new IntValue(0, 0f);
    }

    public void build() {
        _sum_clusters = _n_clusters * _n_outputs;
        _centers = new double[_sum_clusters][];
        for (int i = 0; i < _sum_clusters; i++) {
            _centers[i] = new double[_n_inputs];
        }


    }

    private void closest_rival(double[] input, int target) {
        _min._value = _rival._value = Double.MAX_VALUE;
        _min._index = _rival._index = 0;
        int i;
        double dist;
        for (i = 0; i < _sum_clusters; i++) {
            dist = Vec.distance(input, _centers[i]);
            if (i / _n_clusters == target) {
                if (dist < _min._value) {
                    _min._value = dist;
                    _min._index = i;
                }
            } else {
                if (dist < _rival._value) {
                    _rival._value = dist;
                    _rival._index = i;
                }
            }
        }
    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        int seed = Integer.parseInt(General.get("-seed"));
        int verbose = Integer.parseInt(General.get("-verbose"));
        Random rand = new Random(seed);

        int i, j, n_examples = train_data._n_rows, iter;

        ArrayList<ArrayList<Integer>> cluster_data = new ArrayList<ArrayList<Integer>>();

        for (i = 0; i < _n_outputs; i++) {
            cluster_data.add(new ArrayList<Integer>());
        }

        float variance = 0;

        // construct the dataset for each classes.
        for (i = 0; i < n_examples; i++) {
            cluster_data.get(train_data.get_label(i)).add(new Integer(i));
        }

        KmeansCluster kmeans = new KmeansCluster();
        for (i = 0; i < _n_outputs; i++) {
            train_data.push_subset(cluster_data.get(i));
            variance += kmeans.train(train_data);

            for (j = 0; j < _n_clusters; j++) {
                Utility.copy(_centers[i * _n_clusters + j], kmeans._centers[j]);
                // OutFile.printf(kmeans._centers[j]);
            }
            train_data.pop_subset();
        }

        variance /= _n_outputs;

        OutFile.printf("avarage kmeans variance: %f %f\n", variance, 2 / variance);

        double regular = Float.parseFloat(General.get("-regular"))
                / variance;
        double xi = 2 / variance;

        double rate0 = 0.2 * Float.parseFloat(General.get("-slr")) / xi;
        int max_iter = Integer.parseInt(General.get("-siter"));

        ArrayList<Integer> mix_subset = Utility.shuffle(n_examples, rand);

        double error_num = 0f, measure, loss, coef, rate;
        double[] inputs;
        int target, id;
        for (iter = 0; iter < max_iter; iter++) {
            error_num = 0;
            for (i = 0; i < n_examples; i++) {
                rate = rate0 - rate0 * (iter * n_examples + i)
                        / (max_iter * n_examples);
                id = mix_subset.get(i);
                inputs = train_data.get_X(id);
                target = train_data.get_label(id);
                closest_rival(inputs, target);

                if (_min._value > _rival._value) {
                    error_num += 1;
                }

                if (iter < max_iter) {
                    measure = _min._value - _rival._value;
                    loss = 1.0 / (1 + Math.exp(-measure * xi));
                    coef = loss * (1 - loss) * xi;
                    if (measure < -2.0 / xi)
                        continue;

                    //OutFile.printf("loss: %f measure: %f coef: %f\n",loss,-measure,coef);

                    for (j = 0; j < _n_inputs; j++) {
                        _centers[_min._index][j] += (coef + regular) * rate
                                * (inputs[j] - _centers[_min._index][j]);
                        _centers[_rival._index][j] -= coef * rate
                                * (inputs[j] - _centers[_rival._index][j]);
                    }
                }
            }
            error_num /= n_examples;

            if (iter % 10 == 0 && verbose > 0)
                OutFile.printf("%d cycle error rate: %f\n", iter, error_num);

            if (error_num < General.SMALL_CONST)
                break;
        }

        OutFile.printf("the final error rate is: %f\n", error_num);

        return error_num;
    }

    public double[] forward(double[] input) {
        double[] outputs = new double[_n_outputs];
        double dist_min, dist;
        int base;

        //OutFile.printf("output the distance: \n");

        for (int i = 0; i < _n_outputs; i++) {
            base = i * _n_clusters;
            dist_min = Double.MAX_VALUE;
            for (int j = 0; j < _n_clusters; j++) {
                dist = Vec.distance(input, _centers[base + j]);

                //OutFile.printf("%f ",dist);

                if (dist < dist_min)
                    dist_min = dist;
            }

            //OutFile.printf("\n");

            outputs[i] = -dist_min;
        }

        //OutFile.printf("%d\n",Utility.arg_max(outputs));

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
