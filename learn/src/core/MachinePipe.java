package core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import cmd.General;
import cmd.RunMode;

public class MachinePipe implements java.io.Externalizable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private ArrayList<Machine> _machines = new ArrayList<Machine>();

    public MachinePipe() {

    }

    public void build() {
        // it retrieval the parameters from store_dict(it only read!)
        String[] names = General.get_opts("-machine");
        Machine machine;
        int j;
        for (j = 0; j < names.length; j++) {
            // register the machines
            machine = RunMode.register(names[j]);
            _machines.add(machine);
        }
    }

    public double train(DataSet data) {
        //OutFile.printf("the dataset: %d %d\n",data._n_rows,data._n_cols);

        int i, j;
        ArrayList<String> opt_para = General.get_optimize();
        int n_para = opt_para.size();
        String[][] opt_values = new String[n_para][];
        int[] number_opt = new int[n_para];
        for (i = 0; i < n_para; i++) {
            // obtain the value for the optimize parameters.
            String[] para_values = General.get_opts(opt_para.get(i));
            opt_values[i] = para_values;
            number_opt[i] = para_values.length;
            // XFile.printf("the string: %s\n",opt_para.get(i));
        }

        // it retrieval the parameters from store_dict(it only read!)
        String[] names = General.get_opts("-machine");

        ArrayList<int[]> comb = new ArrayList<int[]>();
        if (n_para > 0)
            Utility.gen_combination(number_opt, comb);

        String[] optimize_settings = new String[n_para];
        double max_value = 0f, measure_value;

        String[] eval_names = General.get("-eval").split(";");

        Machine machine;

        if (comb.size() > 1) {
            for (j = 0; j < n_para; j++) {
                optimize_settings[j] = opt_values[j][comb.get(0)[j]];
            }

            for (i = 0; i < comb.size(); i++) {

                OutFile.printf("set the para => ");
                for (j = 0; j < n_para; j++) {
                    OutFile.printf("%s:%s ", opt_para.get(j),
                            opt_values[j][comb.get(i)[j]]);
                    General.put(opt_para.get(j), opt_values[j][comb.get(i)[j]]);
                }
                OutFile.printf("\n");

                // note clear the machines.
                _machines.clear();
                for (j = 0; j < names.length; j++) {
                    // register the machines
                    machine = RunMode.register(names[j]);
                    _machines.add(machine);
                }

                //data format is related to the machine
                data.set_label_format();

                KFold kfold = new KFold(data, 3);

                // OutFile.printf("the number of the examples---: %d\n",data._n_rows);
                measure_value = kfold.hold_out(this, eval_names);
                if (measure_value > max_value) {
                    max_value = measure_value;
                    for (j = 0; j < n_para; j++) {
                        optimize_settings[j] = opt_values[j][comb.get(i)[j]];
                    }
                }
            }

            // retrain the machine by the optimize parameters.
            for (j = 0; j < n_para; j++) {
                General.put(opt_para.get(j), optimize_settings[j]);
            }

            _machines.clear();
            for (j = 0; j < names.length; j++) {
                // register the machines
                machine = RunMode.register(names[j]);

                _machines.add(machine);
            }

            //note set the data format;
            data.set_label_format();
        } else {
            // note clear the machines.
            _machines.clear();
            for (j = 0; j < names.length; j++) {
                // register the machines
                machine = RunMode.register(names[j]);
                _machines.add(machine);
            }

            //note set the data format;
            data.set_label_format();

            //data.print_dat_format();
        }


        //OutFile.printf("the dataset: %d %d\n",data._n_rows,data._n_cols);

        train_process(data);

        if (comb.size() > 1) {
            OutFile.printf("the best result:  %f => ", max_value);
            for (j = 0; j < n_para; j++) {
                OutFile
                        .printf(" %s:%s ", opt_para.get(j),
                                optimize_settings[j]);
            }
            OutFile.printf("\n");
        }

        return 0.f;
    }

    public double train_process(DataSet data) {
        // OutFile.printf("the number of the examples: %d\n",data._n_rows);
        int i, len = _machines.size() - 1;
        Machine machine;

        for (i = 0; i < len; i++) {
            machine = _machines.get(i);

            // train the data
            machine.train(data);
            machine.test(data);

            //data.print_dat_format();
        }

        // the last machine is classification or regression or ranking

        _machines.get(len).train(data);

        // reset the X;
        data.reset_XY();
        return 0.0f;
    }

    public void test(DataSet data) {
        //data.print_dat_format();
        int len = _machines.size(), i;
        for (i = 0; i < len; i++) {
            _machines.get(i).test(data);
        }
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        build();
        int len = _machines.size(), i;
        for (i = 0; i < len; i++) {
            _machines.get(i).readExternal(in);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        int len = _machines.size(), i;
        for (i = 0; i < len; i++) {
            _machines.get(i).writeExternal(out);
        }
    }


}
