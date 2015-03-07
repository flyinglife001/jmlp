package reduction;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

import kernel.*;
import cmd.General;
import core.*;

public class KPCA extends Machine implements java.io.Externalizable {
    private Kernel _kernel = null;
    double[][] _data_X;
    private double[][] _linear_mat;
    private int _n_rows;
    private double[] _mean_K;
    private double _elem_avg_K;

    public KPCA() {
    }

    public void build() {
        OutFile.printf("reduce the data input by Kernel PCA to %d dimension\n",
                _n_outputs);

        int degree = Integer.parseInt(General.get("-degree"));
        if (degree <= 0) {
            double std = Double.parseDouble(General.get("-g"));
            _kernel = new GaussianKernel(std);
        } else {
            double a = Double.parseDouble(General.get("-a"));
            double b = Double.parseDouble(General.get("-b"));
            _kernel = new PolynomialKernel(a, b, degree);
        }

        _linear_mat = new double[_n_rows][_n_outputs];
        _mean_K = new double[_n_rows];
    }

    public double train(final DataSet train_data) {
        _n_inputs = train_data._n_cols;
        _n_rows = train_data._n_rows;
        _n_outputs = Math.min(_n_rows,
                Integer.parseInt(General.get("-n_extract")));
        build();

        int n_examples = _n_rows, i, j;

        // compute the kernel matrix;
        final DoubleMatrix kernel_mat = new DoubleMatrix(n_examples, n_examples);

        _data_X = new double[n_examples][];
        double elem_value;
        _elem_avg_K = 0;

        for (i = 0; i < _n_rows; i++) {


            double[] X = train_data.get_X(i);
            _data_X[i] = X;


            for (j = 0; j < _n_rows; j++) {
                elem_value = _kernel.eval(X, train_data.get_X(j));
                kernel_mat.put(i, j, elem_value);

                _mean_K[j] += elem_value;
                _elem_avg_K += elem_value;
            }

        }

        _elem_avg_K /= (n_examples * n_examples);
        mat.Vec.scale(_mean_K, 1.0 / n_examples);


        for (i = 0; i < _n_rows; i++) {

            for (j = 0; j < _n_rows; j++) {
                elem_value = kernel_mat.get(i, j);
                elem_value = elem_value - _mean_K[i] - _mean_K[j]
                        + _elem_avg_K;
                kernel_mat.put(i, j, elem_value);
            }

        }

        final DoubleMatrix[] eig_vec = Eigen.symmetricEigenvectors(kernel_mat);

        // normalize the length of the eig vector a_i with \sqrt{\lambda_i};

        for (j = 0; j < _n_outputs; j++) {


            int id = _n_rows - 1 - j;
            double normalize = Math.sqrt(eig_vec[1].get(id, id));

            for (i = 0; i < _n_rows; i++) {
                _linear_mat[i][j] = eig_vec[0].get(i, id) / normalize;
            }

        }

        // OutFile.printf(_linear_mat);

        return 0.0f;
    }

    public double[] forward(double[] input) {
        int i, j;
        double[] outputs = new double[_n_outputs];

        DoubleMatrix kernel_dot = new DoubleMatrix(_n_rows, 1);
        double elem_value;
        double sum_elem = 0;
        for (i = 0; i < _n_rows; i++) {
            elem_value = _kernel.eval(input, _data_X[i]);
            kernel_dot.put(i, 0, elem_value);
            sum_elem += elem_value;
        }
        sum_elem /= _n_rows;
        for (i = 0; i < _n_rows; i++) {
            elem_value = kernel_dot.get(i, 0);
            kernel_dot.put(i, 0, elem_value - sum_elem - _mean_K[i]
                    + _elem_avg_K);
        }

        for (j = 0; j < _n_outputs; j++) {
            double sum = 0;
            for (i = 0; i < _n_rows; i++) {
                sum += _linear_mat[i][j] * kernel_dot.get(i, 0);
            }
            outputs[j] = sum;
        }

        return outputs;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_inputs = in.readInt();
        _n_outputs = in.readInt();
        _n_rows = in.readInt();
        _elem_avg_K = in.readDouble();
        build();

        _kernel.readExternal(in);
        _linear_mat = (double[][]) in.readObject();
        _data_X = (double[][]) in.readObject();
        _mean_K = (double[]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);
        out.writeInt(_n_rows);
        out.writeDouble(_elem_avg_K);

        _kernel.writeExternal(out);
        out.writeObject(_linear_mat);
        out.writeObject(_data_X);
        out.writeObject(_mean_K);
    }
}
