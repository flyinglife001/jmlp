package classifier;

import cmd.General;
import core.Machine;
import core.OutFile;
import core.Utility;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.Math;

import core.DataSet;
import core.Utility.*;

import java.util.ArrayList;
import java.util.Collections;

public class MultiStump extends Machine implements java.io.Externalizable {
    private boolean _is_discrete = false;
    private double[][] _left_distribution, _right_distribution;
    private double[][] _missing_distribution, _full_distribution;
    private double _threshold = 0.0f;
    private int _index = 0, _verbose;
    private String _eval;

    public MultiStump() {
    }

    public void refresh() {
        _index = 0;
        _threshold = 0.0f;
        for (int i = 0; i < _n_outputs; i++) {
            for (int j = 0; j < 2; j++) {
                _left_distribution[i][j] = 0.0f;
                _right_distribution[i][j] = 0.0f;
                _missing_distribution[i][j] = 0.0f;
            }
        }
    }

    public void build() {
        _verbose = Integer.parseInt(General.get("-verbose"));
        _eval = General.get("-eval");

        _left_distribution = new double[_n_outputs][2];
        _right_distribution = new double[_n_outputs][2];
        _missing_distribution = new double[_n_outputs][2];

        refresh();
    }

    public void set_output_descrete() {
        _is_discrete = true;
    }

    private double weight_margin(double[][] distribution) {
        double sum = 0;
        for (int i = 0; i < _n_outputs; i++) {
            sum -= Math.abs(distribution[i][1] - distribution[i][0]);
        }
        return sum;
    }

    private double bhattacharyya_coef(double[][] distribution) {
        double sum = 0, left, right;
        for (int i = 0; i < _n_outputs; i++) {
            left = distribution[i][0];
            right = distribution[i][1];
            if (left > General.SMALL_CONST && right > General.SMALL_CONST) {
                sum += Math.sqrt(left * right);
            }
        }
        return sum;
    }

    private double find_optimal(DataSet train_data, int dim_index,
                                double best_value) {
        double[][] left_dist = new double[_n_outputs][2];
        double[][] right_dist;
        double[][] missing_dist = new double[_n_outputs][2];
        double[][] sum_dist;

        right_dist = Utility.clone(_full_distribution);

        int missing_num = 0, i, n_examples = train_data._n_rows, label, j;
        ArrayList<IntValue> attribute_list = new ArrayList<IntValue>();

        ArrayList<double[]> weights = train_data._weights;
        double[] weight;
        double value;
        for (i = 0; i < n_examples; i++) {
            label = train_data.get_label(i);

            weight = weights.get(i);
            value = train_data.get_X(i)[dim_index];
            if (Double.isNaN(value))// missing value
            {
                missing_num++;
                for (j = 0; j < _n_outputs; j++) {
                    if (j == label) {
                        missing_dist[j][1] += weight[j];
                        right_dist[j][1] -= weight[j];
                    } else {
                        missing_dist[j][0] += weight[j];
                        right_dist[j][0] -= weight[j];
                    }

                }
            } else {
                IntValue sv = new IntValue(i, value);
                attribute_list.add(sv);
                //OutFile.printf("%d %f\n",i,value);
            }
        }

        sum_dist = Utility.clone(right_dist);

        if (attribute_list.size() > 0)// when all of certain columns are
        // missing, it is empty.
        {
            Collections.sort(attribute_list);
            IntValue last_value = attribute_list
                    .get(attribute_list.size() - 1);
            IntValue tail = new IntValue(last_value._index,
                    last_value._value + 0.01f);
            attribute_list.add(tail);
        }

        int end_index = attribute_list.size() - 1, index;
        double cur_value, next_value, candidate_point, candidate_value, new_best_value = best_value;

        int example_id = 0;
        for (i = 0; i < end_index; i++) {
            cur_value = attribute_list.get(i)._value;
            index = attribute_list.get(i)._index;

            label = train_data.get_label(index);

            weight = weights.get(index);

            for (j = 0; j < _n_outputs; j++) {
                if (label == j) {
                    left_dist[j][1] += weight[j];
                    right_dist[j][1] -= weight[j];
                } else {
                    left_dist[j][0] += weight[j];
                    right_dist[j][0] -= weight[j];
                }
            }

            //OutFile.printf(left_dist);

            //OutFile.printf("label: %d value: %f\n",label,cur_value);

            next_value = attribute_list.get(i + 1)._value;

            if (Double.compare(next_value, cur_value) == 0) {
                //OutFile.printf("omit: %d value: %f\n",i,cur_value);
                continue;
            }

            candidate_point = (cur_value + next_value) / 2.0f;

            if (_is_discrete) {
                candidate_value = weight_margin(left_dist)
                        + weight_margin(right_dist)
                        + weight_margin(missing_dist);
            } else {
                candidate_value = bhattacharyya_coef(left_dist)
                        + bhattacharyya_coef(right_dist)
                        + bhattacharyya_coef(missing_dist);
            }

            //OutFile.printf("cand: %f\n",candidate_value);

            if (candidate_value < new_best_value) {
                new_best_value = candidate_value;
                _left_distribution = Utility.clone(left_dist);
                _right_distribution = Utility.clone(right_dist);
                _index = dim_index;
                _threshold = candidate_point;
                example_id = index;
            }
        }

        // no missing value in training value
        if (missing_num == 0) {
            _missing_distribution = Utility.clone(sum_dist);
        } else {
            _missing_distribution = Utility.clone(missing_dist);
        }

        //OutFile.printf("min_value: %f index: %d\n",new_best_value,example_id);

        return new_best_value;
    }

    public double train(DataSet data) {
        _n_inputs = data._n_cols;
        _n_outputs = data._n_classes;
        build();

        int n_examples = data._n_rows, i, label, j;

        ArrayList<double[]> weights = data._weights;
        double[] weight;

        if (weights == null) {
            double weight_value = 1.0f / (n_examples * _n_outputs);
            weights = new ArrayList<double[]>();
            for (i = 0; i < n_examples; i++) {
                weight = new double[_n_outputs];
                for (j = 0; j < _n_outputs; j++) {
                    weight[j] = weight_value;
                }
                weights.add(weight);
            }
            data._weights = weights;
        }

        _full_distribution = new double[_n_outputs][2];

        for (i = 0; i < n_examples; i++) {
            label = data.get_label(i);

            weight = weights.get(i);
            for (j = 0; j < _n_outputs; j++) {
                if (j == label) {
                    _full_distribution[j][1] += weight[j];
                } else {
                    _full_distribution[j][0] += weight[j];
                }
            }
        }

        //OutFile.printf("full dist:\n");
        //OutFile.printf(_full_distribution);

        double best_value = Double.MAX_VALUE, sum;
        for (i = 0; i < _n_inputs; i++) {
            best_value = find_optimal(data, i, best_value);
        }

        //OutFile.printf(_left_distribution);
        //OutFile.printf(_right_distribution);

//	    if(_verbose > 0)
        //OutFile.printf("dim index: %d threshold: %f loss: %f\n", _index,
        //_threshold, -best_value);

        // normalize and smooth the distribution of the data
        for (i = 0; i < _n_outputs; i++) {
            sum = _left_distribution[i][0] + _left_distribution[i][1] + 2
                    * General.SMALL_CONST;
            _left_distribution[i][0] = (_left_distribution[i][0] + General.SMALL_CONST)
                    / sum;
            _left_distribution[i][1] = (_left_distribution[i][1] + General.SMALL_CONST)
                    / sum;

            sum = _right_distribution[i][0] + _right_distribution[i][1] + 2
                    * General.SMALL_CONST;
            _right_distribution[i][0] = (_right_distribution[i][0] + General.SMALL_CONST)
                    / sum;
            _right_distribution[i][1] = (_right_distribution[i][1] + General.SMALL_CONST)
                    / sum;

            sum = _missing_distribution[i][0] + _missing_distribution[i][1] + 2
                    * General.SMALL_CONST;
            _missing_distribution[i][0] = (_missing_distribution[i][0] + General.SMALL_CONST)
                    / sum;
            _missing_distribution[i][1] = (_missing_distribution[i][1] + General.SMALL_CONST)
                    / sum;
            ;
        }


        //OutFile.printf(_left_distribution);
        //OutFile.printf(_right_distribution);

        return -best_value;
    }

    public double[] forward(double[] input) {
        double[] outputs = new double[_n_outputs];
        double value = input[_index];
        int i;
        double[][] p_mat = null;
        if (Double.isNaN(value)) {
            p_mat = _missing_distribution;
        } else if (value < _threshold) {
            p_mat = _left_distribution;
        } else {
            p_mat = _right_distribution;
        }

        for (i = 0; i < _n_outputs; i++) {
            outputs[i] = p_mat[i][1];
        }

        //OutFile.printf(input);
        //OutFile.printf(outputs);

        // if ndcg, it will output the distribution
        //if (!_eval.equals("accuracy"))
        //return outputs;

        if (_is_discrete) {
            for (i = 0; i < _n_outputs; i++) {
                if (outputs[i] > 0.5) {
                    outputs[i] = 1;
                } else {
                    outputs[i] = -1;
                }
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

        _index = in.readInt();
        _threshold = in.readDouble();

        _left_distribution = (double[][]) in.readObject();
        _right_distribution = (double[][]) in.readObject();
        _missing_distribution = (double[][]) in.readObject();

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeInt(_index);
        out.writeDouble(_threshold);
        out.writeObject(_left_distribution);
        out.writeObject(_right_distribution);
        out.writeObject(_missing_distribution);
    }
}
