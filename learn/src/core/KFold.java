package core;


import java.util.Hashtable;
import java.util.Random;
import java.util.ArrayList;

import mat.Vec;
import core.Utility;
import core.DataSet.LabelFormat;
import cmd.General;

public class KFold {
    private ArrayList<ArrayList<Integer>> _train_data, _test_data;
    public int _kfold = 0;
    private DataSet _data = null;

    public KFold(DataSet data, int kfold) {
        _train_data = new ArrayList<ArrayList<Integer>>();
        _test_data = new ArrayList<ArrayList<Integer>>();
        _kfold = kfold;
        _data = data;

        int i;
        for (i = 0; i < _kfold; i++) {
            ArrayList<Integer> train_set = new ArrayList<Integer>();
            _train_data.add(train_set);

            ArrayList<Integer> test_set = new ArrayList<Integer>();
            _test_data.add(test_set);
        }

    }


    private void get_mean_example(Random random) {
        int n_lists = _data._n_rows;

        ArrayList<Integer> index_set = Utility.shuffle(n_lists, random);
        int i, j, fold_id;
        for (i = 0; i < n_lists; i++) {
            fold_id = i % _kfold;

            Integer index = index_set.get(i).intValue();

            _test_data.get(fold_id).add(index);

            for (j = 0; j < _kfold; j++) {
                if (j != fold_id) {
                    _train_data.get(j).add(index);
                }
            }
        }

//		 for (i = 0; i < _kfold; i++)
//		 {
//			 OutFile.printf(_train_data.get(i));
//		 //OutFile.printf("%d\n", _test_data.get(i).size());
//		 }
    }

    public double hold_fold() {
        return 0f;
    }

    public double hold_out(MachinePipe machine, String[] eval_str) {
        int seed = Integer.parseInt(General.get("-seed"));
        Random random = new Random(seed);
        get_mean_example(random);
        double[] average_mesure;
        Evaluation eval = new Evaluation(eval_str, "");

        _data.push_subset(_train_data.get(0));
        machine.train_process(_data);
        _data.pop_subset();

        _data.push_subset(_test_data.get(0));
        machine.test(_data);
        average_mesure = eval.evaluate(_data);
        _data.pop_subset();

        _data.reset_XY();

        //OutFile.printf("the nexample: %d\n",_data._n_rows);
        //_data.print_dat_format();

        //use the last measure to do model selections.
        return average_mesure[eval_str.length - 1];
    }

    public double[] cross_fold(MachinePipe machines_pipe, String[] eval_str) {

        int seed = Integer.parseInt(General.get("-seed"));
        Random random = new Random(seed);
        get_mean_example(random);

        double[] average_mesure = new double[eval_str.length];
        Evaluation eval = new Evaluation(eval_str, "");
        int i, j;

        for (i = 0; i < _kfold; i++) {
            _data.push_subset(_train_data.get(i));
            machines_pipe.train(_data);
            _data.pop_subset();

            ArrayList<Integer> test_set = _test_data.get(i);
            _data.push_subset(test_set);
            machines_pipe.test(_data);
            Vec.plus_equal(average_mesure, eval.evaluate(_data));
            // average_mesure += eval.evaluate(_data);
            _data.pop_subset();

            _data.reset_XY();// recovery the X;
        }

        Vec.scale(average_mesure, 1.0 / _kfold);
        return average_mesure;
    }

    public static void main(String[] args) {
        DataSet data = new DataSet(true);
        // data.load_data("D:\\javaapp\\data\\train.ord");
        data.load_file("D:\\UCIDataSet\\Glass\\train_data.dat");

        KFold kfold = new KFold(data, 5);
        kfold.get_mean_example(new Random(1));
    }
}
