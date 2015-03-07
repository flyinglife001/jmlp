package core;

import java.io.*;
import java.util.ArrayList;

import org.jblas.DoubleMatrix;


public class OutFile {
    private static PrintStream out = System.out;

    public static void set(String file_name) {
        try {
            out = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(file_name)));
        } catch (IOException e) {
            error("construct the stream %s error", file_name);
        }
    }

    public static void close() {
        out.flush();
        out.close();
        out = System.out;
    }

    public static void printf(String format, Object... args) {
        out.printf(format, args);
    }

    public static void printf(String str) {
        out.print(str);
    }

    public static void error(String format, Object... args) {
        printf(format, args);
        System.exit(0);
    }

    public static void error(String str) {
        printf(str);
        System.exit(0);
    }

    public static void printf(double[] X) {
        for (int i = 0; i < X.length; i++) {
            printf("%f ", X[i]);
        }
        printf("\n");
    }

    public static void printf(float[] X) {
        for (int i = 0; i < X.length; i++) {
            printf("%f ", X[i]);
        }
        printf("\n");
    }

    public static void printf(int[] X) {
        for (int i = 0; i < X.length; i++) {
            printf("%d ", X[i]);
        }
        printf("\n");
    }

    public static void printf(int[][] X) {
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[i].length; j++) {
                printf("%d ", X[i][j]);
            }
            printf("\n");
        }
        printf("\n");
    }

    public static void printf(double[][] X) {
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[i].length; j++) {
                printf("%f ", X[i][j]);
            }
            printf("\n");
        }
        printf("\n");
    }

    public static void printf(ArrayList<Integer> list) {
        int i;
        for (i = 0; i < list.size(); i++) {

            OutFile.printf("%s ", list.get(i).toString());
        }
        OutFile.printf("\n");
    }

    public static void printf(DoubleMatrix a) {
        int i, j;
        for (i = 0; i < a.rows; i++) {
            for (j = 0; j < a.columns; j++) {
                OutFile.printf("%f ", a.get(i, j));
            }
            OutFile.printf("\n");
        }
    }

}
