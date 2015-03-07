package reduction;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

import mat.Vec;
import cmd.General;
import core.DataSet;
import core.Machine;
import core.OutFile;
import core.Utility;

public class LDA extends Machine implements java.io.Externalizable {
    private double[][] _eig_mat;

    public LDA() {
    }

    public void build() {
        OutFile.printf("reduce the data input by LDA to %d dimension\n", _n_outputs);

        _eig_mat = new double[_n_inputs][_n_outputs];
        refresh();
    }

    public void refresh() {
        Utility.assign(_eig_mat, 0f);
    }

    public double train(DataSet train_data) {
        _n_inputs = train_data._n_cols;
        int n_class = train_data._n_classes;
        _n_outputs = Math.min(_n_inputs, n_class - 1);
        build();

        // train_data.print_dat_format();

        DoubleMatrix Sw = DoubleMatrix.zeros(_n_inputs, _n_inputs);
        DoubleMatrix Sb = DoubleMatrix.zeros(_n_inputs, _n_inputs);

        int[] n_classes = new int[n_class];
        double[][] mean_for_class = new double[n_class][_n_inputs];
        double[] mean = new double[_n_inputs];

        int n_examples = train_data._n_rows, i, j;

        for (i = 0; i < n_examples; i++) {
            double[] X = train_data.get_X(i);
            int label = train_data.get_label(i);

            for (j = 0; j < _n_inputs; j++) {
                mean_for_class[label][j] += X[j];
                mean[j] += X[j];
            }

            n_classes[label]++;
        }

        double[][] diff_class_mean = new double[n_class][_n_inputs];
        for (j = 0; j < _n_inputs; j++) {
            mean[j] /= 1.0 * n_examples;// compute the total mean value;

            for (i = 0; i < n_class; i++) {
                if (n_classes[i] == 0) {
                    mean_for_class[i][j] = 0;
                } else {
                    mean_for_class[i][j] /= 1.0 * n_classes[i];
                }

                diff_class_mean[i][j] = mean_for_class[i][j] - mean[j];
            }
        }

        // OutFile.printf(mean_for_class);

        for (i = 0; i < n_class; i++) {
            double[] diff = diff_class_mean[i];

            //Sb = Sb + c_i (v_i - mean)(v_i - mean);
            Sb.addi(new DoubleMatrix(Vec.outer_dot(diff, diff)).muli(n_classes[i]));
        }

//		 for(i = 0; i < _n_inputs; i++)
//			{
//				for(j = 0; j < _n_inputs; j++)
//				{
//					OutFile.printf("%f ",Sb.get(i,j));
//				}
//				OutFile.printf("\n");
//			}


        for (i = 0; i < n_examples; i++) {
            double[] diff = Vec.minus(train_data.get_X(i),
                    mean_for_class[train_data.get_label(i)]);

            //Sw = Sw + diff * diff
            Sw.addi(new DoubleMatrix(Vec.outer_dot(diff, diff)));
        }

        DoubleMatrix[] eig_sw = Eigen.symmetricEigenvectors(Sw);


//        for(i = 0; i < _n_inputs; i++)
//		{
//			for(j = 0; j < _n_inputs; j++)
//			{
//				OutFile.printf("%f ",eig_sw[0].get(i,j));
//			}
//			OutFile.printf("\n");
//		}

        double s;
        for (j = 0; j < _n_inputs; j++) {
            s = eig_sw[1].get(j, j);

            //OutFile.printf("%f\n",s);

            if (s > General.SMALL_CONST) {
                s = 1.0 / Math.sqrt(s);
            } else {
                s = 0;
            }

            for (i = 0; i < _n_inputs; i++) {
                eig_sw[0].put(i, j, eig_sw[0].get(i, j) * s);
            }
        }

        //eig_sw[0].print();

//        OutFile.printf(" sf.\n");
//        for(i = 0; i < _n_inputs; i++)
//		{
//			for(j = 0; j < _n_inputs; j++)
//			{
//				OutFile.printf("%f ",eig_sw[0].get(i,j));
//			}
//			OutFile.printf("\n");
//		}

        //eig_sw[0].print();

        Sb = eig_sw[0].transpose().mmul(Sb).mmul(eig_sw[0]);

//        OutFile.printf(" sb.\n");
//        for(i = 0; i < _n_inputs; i++)
//		{
//			for(j = 0; j < _n_inputs; j++)
//			{
//				OutFile.printf("%f ",Sb.get(i,j));
//			}
//			OutFile.printf("\n");
//		}


        DoubleMatrix[] eig_sb = Eigen.symmetricEigenvectors(Sb);

        double sum = 0;
        for (i = 0; i < _n_inputs; i++) {
            sum += eig_sb[1].get(i, i);
        }
        OutFile.printf("eig value: \n");
        for (i = 0; i < _n_inputs; i++) {
            OutFile.printf("%f ", eig_sb[1].get(i, i) / sum);
        }
        OutFile.printf("\n");

        eig_sw[0].mmuli(eig_sb[0]);

//		//eig_vec[0].print();
//		for(i = 0; i < _n_inputs; i++)
//		{
//			for(j = 0; j < _n_inputs; j++)
//			{
//				OutFile.printf("%f ",eig_sb[1].get(i,j));
//			}
//			OutFile.printf("\n");
//		}
        //OutFile.printf("%d %d outputs: %d\n",eig_vec[0].rows,eig_vec[0].columns, _n_outputs);

        for (i = 0; i < _n_outputs; i++) {
            for (j = 0; j < _n_inputs; j++) {
                _eig_mat[j][i] = eig_sw[0].get(j, _n_inputs - 1 - i);
            }
        }

        return 0.0f;
    }

    public double[] forward(double[] input) {
        double[] outputs = new double[_n_outputs];
        int i, j;
        for (j = 0; j < _n_outputs; j++) {
            double sum = 0;
            for (i = 0; i < _n_inputs; i++) {
                sum += input[i] * _eig_mat[i][j];
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
        build();

        _eig_mat = (double[][]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_inputs);
        out.writeInt(_n_outputs);

        out.writeObject(_eig_mat);
    }

}
