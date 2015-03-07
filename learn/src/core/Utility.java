package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.io.File;

import org.jblas.DoubleMatrix;


public class Utility {
    // z = log(exp(x) + exp(y))
    // assume x > y and z = x + log(1 + exp(y - x)), when y - x < - 1e6, then z
    // = x;
    public static double log_add(double x, double y) {
        double z, diff;
        if (y > x) {
            z = y;
            diff = x - y;
        } else {
            z = x;
            diff = y - x;
        }

        if (diff < -1e6) {
            return z;
        } else {
            return z + Math.log(1 + Math.exp(diff));
        }

    }

    public static double[] log_softmax(double[] input) {
        int len = input.length, i;
        double[] output = new double[len];
        double sum = -Double.MAX_VALUE, value;
        for (i = 0; i < len; i++) {
            value = input[i];
            sum = log_add(sum, value);
            output[i] = value;
        }

        for (i = 0; i < len; i++) {
            output[i] -= sum;
        }
        return output;
    }

    public static double[] normalize(double[] x, int n_channels) {
        double mean = 0, var = 0, value;
        int i, len = x.length / n_channels, k;
        if (len % n_channels != 0)
            OutFile.printf(
                    "the len of data needing normalize is not the times of %d\n",
                    n_channels);

        int offset = 0;
        for (k = 0; k < n_channels; k++) {
            for (i = 0; i < len; i++) {
                value = x[i + offset];
                mean += value;
                var += value * value;
            }
            mean /= len;
            var = Math.sqrt((var - len * mean * mean) / (len - 1) + 10);
            for (i = 0; i < len; i++) {
                x[i + offset] = (x[i + offset] - mean) / var;
            }

            offset += len;
        }

        return x;
    }

    public static int arg_max(double[] X) {
        double max = X[0];
        int index = 0;
        for (int i = 0; i < X.length; i++) {
            if (X[i] > max) {
                max = X[i];
                index = i;
            }
        }
        return index;
    }

    public static int arg_max(int[] X) {
        int max = X[0];
        int index = 0;
        for (int i = 0; i < X.length; i++) {
            if (X[i] > max) {
                max = X[i];
                index = i;
            }
        }
        return index;
    }

    public static double max(double[] X) {
        return X[arg_max(X)];
    }

    public static void assign(double[] X, double value) {
        for (int i = 0; i < X.length; i++) {
            X[i] = value;
        }
    }

    public static void assign(float[] X, float value) {
        for (int i = 0; i < X.length; i++) {
            X[i] = value;
        }
    }

    public static void assign(double[][] X, double value) {
        for (int i = 0; i < X.length; i++) {
            assign(X[i], value);
        }
    }

    public static double[] scale(double[] X, double coef) {
        for (int i = 0; i < X.length; i++) {
            X[i] *= coef;
        }
        return X;
    }

    public static void assign(int[] X, int value) {
        for (int i = 0; i < X.length; i++) {
            X[i] = value;
        }
    }

    public static void assign(int[][] X, int value) {
        for (int i = 0; i < X.length; i++) {
            assign(X[i], value);
        }
    }

    public static ArrayList<Integer> shuffle(int n_examples, Random random) {
        ArrayList<Integer> index_set = new ArrayList<Integer>();
        for (int i = 0; i < n_examples; i++) {
            index_set.add(new Integer(i));
        }
        Collections.shuffle(index_set, random);
        return index_set;
    }

    public static double[][] clone(double[][] X) {
        double[][] Y = new double[X.length][];
        for (int i = 0; i < X.length; i++) {
            Y[i] = X[i].clone();
        }
        return Y;
    }

    public static void copy(double[] dest, double[] source) {
        for (int i = 0; i < source.length; i++) {
            dest[i] = source[i];
        }
    }

    public static class IntValue implements Comparable<IntValue> {
        public int _index;
        public double _value;

        public IntValue(int index, double value) {
            _index = index;
            _value = value;
        }

        public int compareTo(IntValue B) {
            if (_value > B._value)
                return 1;
            else if (_value < B._value)
                return -1;
            else
                return 0;
        }
    }

    public static class StringValue implements Comparable<StringValue> {
        public String _index;
        public double _value;

        public StringValue(String index, double value) {
            _index = index;
            _value = value;
        }

        public int compareTo(StringValue B) {
            if (_value > B._value)
                return 1;
            else if (_value < B._value)
                return -1;
            else
                return 0;
        }
    }

    public static ArrayList<String> traverse(String dir_name, String filter) {
        ArrayList<String> files = new ArrayList<String>();
        File dir = new File(dir_name);
        list_dir(dir, files, filter);
        return files;
    }

    public static double sigmoid(double value) {
        return 1 / (1 + Math.exp(-value));
    }

    private static void list_dir(File dir, ArrayList<String> files,
                                 String suffix) {
        if (dir.isFile()) {
            String path = dir.getAbsolutePath();
            if (suffix.equals("") || path.endsWith(suffix)) {
                files.add(path);
            }
        } else {
            File[] file_array = dir.listFiles();
            for (int i = 0; i < file_array.length; i++) {
                list_dir(file_array[i], files, suffix);
            }
        }
    }

    /**
     * the index contains the number for each dimension, such as [1,2,1,1] the
     * algorithm will generate: [0,0,0,0] and [0,1,0,0]
     */

    public static void gen_combination(int[] number_attribute,
                                       ArrayList<int[]> combination) {
        int len = number_attribute.length;
        int[] sequence = new int[len];
        int active_pos = 0;
        while (true) {
            if (sequence[active_pos] == number_attribute[active_pos]) {
                while (sequence[active_pos] == number_attribute[active_pos]) {
                    if (active_pos + 1 != len) {
                        active_pos++;
                        sequence[active_pos]++;// carry 1
                    } else {
                        // cout<<"index: "<<i<<endl;
                        return;
                    }
                }

                for (int j = 0; j < active_pos; j++) {
                    sequence[j] = 0;
                }

                int[] gen_seq = new int[len];
                for (int j = 0; j < len; j++) {
                    gen_seq[j] = sequence[j];
                }
                combination.add(gen_seq);

                active_pos = 0;
                sequence[0]++;
            } else {
                // copy(sequence.begin(),sequence.end(),ostream_iterator<int>(cout," "));
                // cout<<endl;
                int[] gen_seq = new int[len];
                for (int j = 0; j < len; j++) {
                    gen_seq[j] = sequence[j];
                }
                combination.add(gen_seq);

                sequence[0]++;
            }
        }
    }

}
