package classifier;

import java.util.ArrayList;

import cmd.General;
import core.DataSet;
import core.OutFile;
import core.Machine;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.Math;

/*** <!-- globalinfo-start -->
 * This program implements the real version of AdaBoost with Hamming loss (AdaBoost.DMH).<br/>
 * <p/> For more information about Adaboost, see:<br/>
 * <br/>
 * Yoav Freund, Robert E. Schapire: Experiments with a new boosting algorithm. In: Thirteenth International Conference on Machine Learning, San Francisco, 148-156, 1996.
 * <p/>
 * <!-- globalinfo-end -->
 * <p/>
 * <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;@ARTICLE{Schapire1999,
 * author = {Robert E. Schapire and Yoram Singer},
 * title = {Improved Boosting Algorithms Using Confidence-rated Predictions},
 * journal = {Machine Learning},
 * year = {1999},
 * volume = {37},
 * pages = {297-336}
 * }
 * </pre>
 * <p/>
 * <!-- technical-bibtex-end -->
 *
 * @author Xiao-Bo Jin (xbjin9801@gmail.com)
 * @version 0.2
 *
 */
public class AdaBoostRMH extends Machine implements java.io.Externalizable {
    private int _iteration, _n_machines, _verbose;
    private String _eval;
    private Machine[] _machines;

    /*
    The construct
     */
    public AdaBoostRMH() {
        _verbose = Integer.parseInt(General.get("-verbose"));
        _eval = General.get("-eval");
        _iteration = Integer.parseInt(General.get("-num"));
    }

    public void build() {

        _machines = new Machine[_iteration];
        String bc_name = General.get("-bc");
        int i;

        for (i = 0; i < _iteration; i++) {
            MultiStump machine = new MultiStump();
            _machines[i] = machine;
        }

        OutFile.printf("build AdaBoost.RMH classifier with %d stump...\n", _iteration);

    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_outputs = train_data._n_classes;
        build();

        // ==============================================================================
        int n_examples = train_data._n_rows, label;
        int i, n, k;
        double r_value = 0, weight_sum, weight_value = 1.0f / (n_examples * _n_outputs);
        Machine sub_machine;
        double[] input, output;

        ArrayList<double[]> weights = new ArrayList<double[]>();
        for (i = 0; i < n_examples; i++) {
            input = new double[_n_outputs];
            for (k = 0; k < _n_outputs; k++) {
                input[k] = weight_value;
            }
            weights.add(input);
        }
        train_data._weights = weights;

        _n_machines = 0;
        for (i = 0; i < _iteration; i++) {
            sub_machine = _machines[i];
            r_value = sub_machine.train(train_data);

            if (_verbose > 0 && i % 10 == 0)
                OutFile.printf("%d sub machine get the r_value: %f\n", i,
                        r_value);

            weight_sum = 0;
            weight_value = 0;

            for (n = 0; n < n_examples; n++) {
                input = train_data.get_X(n);
                label = train_data.get_label(n);

                output = sub_machine.forward(input);
                //OutFile.printf(output);

                //OutFile.printf("label: %d\n",label);

                for (k = 0; k < _n_outputs; k++) {
                    if (k == label) {
                        weight_value = -0.5
                                * Math.log((output[k] / (1 - output[k])));
                    } else {
                        weight_value = 0.5 * Math.log(output[k]
                                / (1 - output[k]));
                    }

                    //OutFile.printf("w: %f %f ",output[k] / (1 - output[k]),weight_value);

                    train_data._weights.get(n)[k] *= Math.exp(weight_value);
                    weight_sum += train_data._weights.get(n)[k];
                }

                //OutFile.printf("\n");

            }

            // normalize the weights
            for (n = 0; n < n_examples; n++) {
                for (k = 0; k < _n_outputs; k++) {
                    train_data._weights.get(n)[k] /= weight_sum;
                    //OutFile.printf("%f ",train_data._weights.get(n)[k]);
                }
                //OutFile.printf("\n");
            }
            _n_machines++;


        }

        return 0.0f;
    }

    // fill the outputs to Mat* outputs;
    public double[] forward(double[] input) {
        double[] outputs = new double[_n_outputs];

        int i, k;
        double[] result;
        for (i = 0; i < _n_machines; i++) {
            result = _machines[i].forward(input);
            for (k = 0; k < _n_outputs; k++) {
                outputs[k] += 0.5 * Math.log(result[k] / (1 - result[k]));
            }
        }

        return outputs;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        _n_machines = in.readInt();
        build();

        int i;
        for (i = 0; i < _n_machines; i++) {
            _machines[i].readExternal(in);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);
        out.writeInt(_n_machines);

        int i;
        for (i = 0; i < _n_machines; i++) {
            _machines[i].writeExternal(out);
        }
    }

}
