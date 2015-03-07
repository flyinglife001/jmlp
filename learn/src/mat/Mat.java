package mat;


public class Mat {
    public static double[][] plus(double[][] x, double[][] y) {
        int rows = x.length, cols = x[0].length, i, j;
        double[][] z = new double[rows][cols];
        for (i = 0; i < rows; i++) {
            for (j = 0; j < cols; j++) {
                z[i][j] = x[i][j] + y[i][j];
            }
        }
        return z;
    }

    public static void plus_equal(double[][] x, double[][] y) {
        int rows = x.length, cols = x[0].length, i, j;
        for (i = 0; i < rows; i++) {
            for (j = 0; j < cols; j++) {
                x[i][j] += y[i][j];
            }
        }
    }

    public static double[][] minus(double[][] x, double[][] y) {
        int rows = x.length, cols = x[0].length, i, j;
        double[][] z = new double[rows][cols];
        for (i = 0; i < rows; i++) {
            for (j = 0; j < cols; j++) {
                z[i][j] = x[i][j] - y[i][j];
            }
        }
        return z;
    }

    public static void minus_equal(double[][] x, double[][] y) {
        int rows = x.length, cols = x[0].length, i, j;
        for (i = 0; i < rows; i++) {
            for (j = 0; j < cols; j++) {
                x[i][j] -= y[i][j];
            }
        }
    }

    public static double dot(double[][] x, double[][] y) {
        double sum = 0;
        int rows = x.length, cols = x[0].length, i, j;
        for (i = 0; i < rows; i++) {
            for (j = 0; j < cols; j++) {
                sum += x[i][j] * y[i][j];
            }
        }
        return sum;
    }

    /**
     * vec(x) = vec(x)*s
     */
    public static void scale(double[][] x, double s) {
        int rows = x.length, cols = x[0].length, i, j;
        for (i = 0; i < rows; i++) {
            for (j = 0; j < cols; j++) {
                x[i][j] *= s;
            }
        }
    }

    /**
     * vec(x) = vec(x) + s * vec(y)
     */
    public static void plus_scale(double[][] x, double s, double[][] y) {
        int rows = x.length, cols = x[0].length, i, j;

        for (i = 0; i < rows; i++) {
            for (j = 0; j < cols; j++) {
                x[i][j] += s * y[i][j];
            }
        }
    }

    /**
     *
     * */


}
