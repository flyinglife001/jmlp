package cmd;

import java.lang.Math;

import cmd.RunMode;
import core.InFile;
import core.OutFile;

import java.util.ArrayList;
import java.text.DecimalFormat;

import mat.Vec;

public class BatchLearn {
    public static void main(String[] args) {
        long begin_time = System.currentTimeMillis();

        //read the default setting from option file.
        General.set_default();
        int i, j;

        // it can't be changed once it has store the value.
        for (i = 0; i < args.length; i += 2) {
            General.store(args[i], args[i + 1]);
            General.put(args[i], args[i + 1]);
            General.add_optimize(args[i]);
        }

        String result_file = General.get("-result");
        if (result_file.length() == 0)
            result_file = "result.txt";

        String file_name = General.get("-file");
        String test_name = General.get("-test");
        if (file_name.length() == 0) {
            OutFile.error("you have not assign the train file!");
        }

        String old_str, old_test = "";

        try {
            int end_index = Math.max(file_name.lastIndexOf('\\'), file_name
                    .lastIndexOf('/'));
            String sub_name = file_name.substring(0, end_index - 1);
            int begin_index = Math.max(sub_name.lastIndexOf('\\'), sub_name
                    .lastIndexOf('/'));

            old_str = file_name.substring(begin_index + 1, end_index);

            if (test_name.length() > 0) {
                end_index = Math.max(test_name.lastIndexOf('\\'), test_name
                        .lastIndexOf('/'));
                sub_name = test_name.substring(0, end_index - 1);
                begin_index = Math.max(sub_name.lastIndexOf('\\'), sub_name
                        .lastIndexOf('/'));
                old_test = test_name.substring(begin_index + 1, end_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String data_file = General.get("-dataset");

        // it will forbid the output of the running information.
        General.put("-verbose", "-1");

        double[][] results;
        ArrayList<String> data_names = new ArrayList<String>();
        if (data_file.length() > 0) {
            try {
                InFile.set(data_file);
                String text_line;
                while ((text_line = InFile.get_line()) != null) {
                    text_line = text_line.trim();
                    if (text_line.length() == 0)
                        continue;

                    data_names.add(text_line);
                }
                InFile.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            int len = data_names.size();
            results = new double[len][];
            String replace_name, name;
            for (i = 0; i < len; i++) {
                name = data_names.get(i);
                replace_name = file_name.replaceFirst(old_str, name);
                General.put("-file", replace_name);
                if (test_name.length() > 0) {
                    replace_name = test_name.replaceFirst(old_test, name);
                    General.put("-test", replace_name);
                }

                results[i] = RunMode.train_mode(null);
            }
        } else {
            data_names.add(old_str);
            results = new double[1][];
            results[0] = RunMode.train_mode(null);
        }

        // save the format: **.**
        DecimalFormat df = new DecimalFormat("00.00");
        double[] sum = new double[results[0].length];

        String[] eval_names = General.get("-eval").split(";");

        OutFile.set(result_file);
        OutFile.printf("%-10s", "data");
        for (j = 0; j < eval_names.length; j++) {
            OutFile.printf("%6s ", eval_names[j]);
        }
        OutFile.printf("\n");

        for (i = 0; i < results.length; i++) {
            Vec.plus_equal(sum, results[i]);
            String name = data_names.get(i);
            if (name.length() > 8)
                name = name.substring(0, 8);

            OutFile.printf("%-10s", name);
            for (j = 0; j < results[i].length; j++) {
                OutFile.printf("%s ", df.format(results[i][j]));
            }
            OutFile.printf("\n");
        }

        Vec.scale(sum, 1.0 / results.length);

        // write the average of all dataset.
        OutFile.printf("%-10s", "avg:");
        for (j = 0; j < sum.length; j++) {
            OutFile.printf("%s ", df.format(sum[j]));
        }
        OutFile.printf("\n");
        OutFile.close();

        OutFile.printf("average on all dataset is:");
        for (j = 0; j < sum.length; j++) {
            OutFile.printf("%s=%4.2f ", eval_names[j], sum[j]);
        }


        int repeats = Integer.parseInt(General.get("-repeat"));

        double total_seconds = (System
                .currentTimeMillis() - begin_time) / (1000f * repeats);
        double total_hours = total_seconds / 3600;

        OutFile.printf("\n%d times average running time is: %4.2f seconds or %4.3f hours\n", repeats, total_seconds,
                total_hours);
    }
}
