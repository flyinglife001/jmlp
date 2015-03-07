package cmd;

import classifier.*;
import reduction.*;
import text.TFIDF;
import core.*;
import core.DataSet.LabelFormat;
import preprocess.*;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import mat.Vec;

import core.Utility;

public class RunMode {
    private static DataSet _data;

    public static enum MachineType {
        kmeans, whiten, mlp, kpca, nb, knn, tfidf, libsvm, svm, sgdsvm, lda, dmh, dtr,
        norm, scale, rmh, miss, thr, mce, pca, rbmm
    }

    ;

    public static Hashtable<String, MachineType> machine_table = new Hashtable<String, MachineType>();

    public static void set_machines() {
        machine_table.put("scale", MachineType.scale);
        machine_table.put("dmh", MachineType.dmh);
        machine_table.put("dtr", MachineType.dtr);
        machine_table.put("norm", MachineType.norm);
        machine_table.put("rmh", MachineType.rmh);
        machine_table.put("miss", MachineType.miss);
        machine_table.put("thr", MachineType.thr);
        machine_table.put("mce", MachineType.mce);
        machine_table.put("pca", MachineType.pca);
        machine_table.put("lda", MachineType.lda);
        machine_table.put("svm", MachineType.svm);
        machine_table.put("libsvm", MachineType.libsvm);
        machine_table.put("tfidf", MachineType.tfidf);
        machine_table.put("knn", MachineType.knn);
        machine_table.put("nb", MachineType.nb);
        machine_table.put("kpca", MachineType.kpca);
        machine_table.put("mlp", MachineType.mlp);
        machine_table.put("whiten", MachineType.whiten);
        machine_table.put("kmeans", MachineType.kmeans);

    }

    public static Machine register(String machine_str) {
        Machine machine = null;
        if (!machine_table.containsKey(machine_str)) {
            OutFile
                    .error("can not find out %s in machine table\n",
                            machine_str);
        }

        switch (machine_table.get(machine_str)) {
            case dmh:
                machine = new AdaBoostDMH();
                break;
            case mce:
                machine = new MCE();
                break;
            case dtr:
                machine = new MultiStump();
                break;
            case norm:
                machine = new MeanNorm();
                break;
            case rmh:
                machine = new AdaBoostRMH();
                break;
            case scale:
                machine = new Scale();
                break;
            case miss:
                machine = new MeanMissing();
                break;
            case thr:
                machine = new Threasholdable();
                break;
            case pca:
                machine = new PCA();
                break;
            case tfidf:
                machine = new TFIDF();
                break;
            case lda:
                machine = new LDA();
                break;
            case svm:
                machine = new SVM();
                _data._label_format = LabelFormat.onehot;
                break;
            case libsvm:
                machine = new LibSVM();
                _data._label_format = LabelFormat.onehot;
                break;
            case knn:
                machine = new KNN();
                break;
            case nb:
                machine = new NaiveBayes();
                break;
            case kpca:
                machine = new KPCA();
                break;
            case mlp:
                machine = new MLP();
                break;
            case whiten:
                machine = new Whiten();
                break;
            case kmeans:
                machine = new KmeansCluster();
                break;
            default:
                OutFile.error("there is no such machine %s", machine_str);
        }
        return machine;
    }

    public static void main(String[] args) {
        long begin_time = System.currentTimeMillis();
        int arg_len = args.length;

        if (arg_len < 2)
            OutFile.error("not enough argments! less than 2!\n");

        String mode = args[0];
        String model_file = args[1];

        boolean is_train = true;
        ObjectInputStream in = null;
        if (mode.equals("-train_mode")) {
            General.set_default();
        } else if (mode.equals("-test_mode")) {
            is_train = false;

            try {
                OutFile.printf("load the model file from %s\n", model_file);
                if (!model_file.endsWith(".mdl"))
                    OutFile.error("the model file must be end with .mdl!");

                in = new ObjectInputStream(new FileInputStream(model_file));
                General.readExternal(in);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            OutFile.error("the first two param must be specified: -train_mode or -test_mode and the model file!\n");
        }

        int i;
        for (i = 2; i < args.length; i += 2) {
            General.store(args[i], args[i + 1]);
            General.put(args[i], args[i + 1]);
            General.add_optimize(args[i]);
        }

        ObjectOutputStream out = null;

        if (is_train) {
            try {
                out = new ObjectOutputStream(new FileOutputStream(model_file + ".mdl"));
                General.writeExternal(out);
                OutFile.printf("save the model file in %s.mdl\n", model_file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            train_mode(out);

            try {
                out.close();
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        } else {
            test_mode(in);

            try {
                in.close();
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }

        int repeats = Integer.parseInt(General.get("-repeat"));


        double total_seconds = (System
                .currentTimeMillis() - begin_time) / (1000f * repeats);
        double total_hours = total_seconds / 3600;

        OutFile.printf("\n%d times average running time is: %4.2f seconds or %4.3f hours\n", repeats, total_seconds,
                total_hours);
    }

    public static double[] test_mode(ObjectInputStream in) {
        String test_file = General.get("-test");
        _data = new DataSet(false);
        try {
            _data.readExternal(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        OutFile.printf("load the test file -> %s\n", test_file);
        _data.load_file(test_file);

        set_machines();
        MachinePipe machine_pipe = new MachinePipe();

        int end = test_file.lastIndexOf("\\");
        String out_dir = test_file.substring(0, end);
        String[] eval_names = General.get("-eval").split(";");

        Evaluation eval = new Evaluation(eval_names, out_dir);

        try {
            machine_pipe.readExternal(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        machine_pipe.test(_data);
        double results[] = eval.evaluate(_data);
        Vec.scale(results, 100);

        OutFile.printf("the average test measure: ");
        int j;
        for (j = 0; j < results.length; j++) {
            OutFile.printf("%s=%4.2f ", eval_names[j], results[j]);
        }
        OutFile.printf(" <- %s\n", test_file);

        return results;
    }

    public static double[] train_mode(ObjectOutputStream out) {
        String file = General.get("-file");
        _data = new DataSet(true);
        _data.load_file(file);

        set_machines();

        MachinePipe machine_pipe = new MachinePipe();

        int k_fold = Integer.parseInt(General.get("-kfold"));
        int seeds = Integer.parseInt(General.get("-seed"));
        int repeats = Integer.parseInt(General.get("-repeat")), i, j;

        String[] eval_names = General.get("-eval").split(";");
        int n_meas = eval_names.length;

        double[] results = new double[n_meas];
        double[] average_measure;
        KFold kf = null;
        if (k_fold > 1) {
            for (i = 0; i < repeats; i++) {
                // set the seed for the randomness of the algorithms.
                General.put("-seed", Integer.toString(seeds + i * 10));
                kf = new KFold(_data, k_fold);

                average_measure = kf.cross_fold(machine_pipe, eval_names);

                Vec.plus_equal(results, average_measure);
                OutFile.printf("%d repeat cross fold measure: ", i);
                for (j = 0; j < average_measure.length; j++) {
                    OutFile.printf("%s=%.4f ", eval_names[j], average_measure[j]);
                }
                OutFile.printf("\n");
            }
            Vec.scale(results, 100.0 / repeats);
            OutFile.printf("the average cross fold measure: ");
            for (j = 0; j < results.length; j++) {
                OutFile.printf("%s=%4.2f ", eval_names[j], results[j]);
            }
            OutFile.printf(" <- %s\n", file);
        } else {
            String test_file = General.get("-test");
            DataSet test_data = _data;
            test_data._is_train = false;

            if (test_file.length() != 0) {
                test_data = new DataSet(false);
                OutFile.printf("begin to load the test file...\n");
                // need to copy the class map etc. to test data. It
                // set the variable after loading data to avoid to be covered.
                test_data.copy_model(_data);
                test_data.load_file(test_file);
            } else {
                test_file = file;
            }

            //OutFile.printf("%s\n",test_file);
            int end = test_file.lastIndexOf("\\");
            String out_dir = test_file.substring(0, end);


            Evaluation eval = new Evaluation(eval_names, out_dir);
            for (i = 0; i < repeats; i++) {
                // set the seed for the random of the algorithm
                General.put("-seed", Integer.toString(seeds + i));
                machine_pipe.train(_data);
                machine_pipe.test(test_data);

                average_measure = eval.evaluate(test_data);
                Vec.plus_equal(results, average_measure);

                OutFile.printf("the %d repeat measure: ", i);
                for (j = 0; j < average_measure.length; j++) {
                    OutFile.printf("%s=%.4f ", eval_names[j], average_measure[j]);
                }
                OutFile.printf("\n");

                test_data.reset_XY();// recovery from X.
            }

            Vec.scale(results, 100 / repeats);
            OutFile.printf("the average test measure: ");
            for (j = 0; j < results.length; j++) {
                OutFile.printf("%s=%4.2f ", eval_names[j], results[j]);
            }
            OutFile.printf(" <- %s\n", test_file);

            if (out != null) {
                try {
                    // write the binary stream.
                    _data.writeExternal(out);
                    machine_pipe.writeExternal(out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return results;
    }
}
