package mat;

import core.OutFile;

public class Vec {
    public static double sum(double[] x) {
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }
        return sum;
    }

    public static double[] normalize(double[] vec) {
        int len = vec.length, i;
        double std = 0, mean = 0, value;
        for (i = 0; i < len; i++) {
            value = vec[i];
            std += value * value;
            mean += value;
        }

        mean /= len;

        std = (std - len * mean * mean) / (len - 1);

        //OutFile.printf("mean: %f std: %f\n",mean,std);

        std = Math.sqrt(std + 10);

        for (i = 0; i < len; i++) {
            vec[i] = (vec[i] - mean) / std;
        }
        return vec;
    }

    public static double distance(double[] x, double[] y) {
        assert (x.length <= y.length);

        double sum = 0, diff;
        for (int i = 0; i < x.length; i++) {
            diff = x[i] - y[i];
            sum += diff * diff;
        }
        return sum;
    }

    public static double[] plus(double[] x, double[] y) {
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] + y[i];
        }
        return z;
    }

    public static void plus_equal(double[] x, double[] y) {
        for (int i = 0; i < x.length; i++) {
            x[i] += y[i];
        }
    }

    public static double[] minus(double[] x, double[] y) {
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] - y[i];
        }
        return z;
    }

    public static void minus_equal(double[] x, double[] y) {
        for (int i = 0; i < x.length; i++) {
            x[i] -= y[i];
        }
    }

    public static double dot(double[] x, double[] y) {
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i] * y[i];
        }
        return sum;
    }

    public static double[][] outer_dot(double[] x, double[] y) {
        int i, j, x_len = x.length, y_len = y.length;
        double[][] mat = new double[x_len][y_len];
        for (i = 0; i < x_len; i++) {
            for (j = 0; j < y_len; j++) {
                mat[i][j] = x[i] * y[j];
            }
        }

        return mat;
    }

    /**
     * vec(x) = vec(x)*s
     */
    public static void scale(double[] x, double s) {
        for (int i = 0; i < x.length; i++) {
            x[i] *= s;
        }
    }

    /**
     * vec(x) = vec(x) + s * vec(y)
     */
    public static void plus_scale(double[] x, double s, double[] y) {
        for (int i = 0; i < x.length; i++) {
            x[i] += s * y[i];
        }
    }


    public static void main(String[] args) {
        double[] vec = new double[]{1, 2, 4, 5, 6, 7, 8, 9};


        OutFile.printf(Vec.normalize(vec));
    }


}
