package kernel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import mat.Vec;


public class PolynomialKernel extends Kernel {
    private double _a, _b;
    int _degree;

    public PolynomialKernel(double a, double b, int degree) {
        _a = a;
        _b = b;
        _degree = degree;
    }

    public double eval(double[] x, double[] y) {
        double sum = Vec.dot(x, y);

        sum = _a * sum + _b;

        double product = sum;
        for (int t = 1; t < _degree; t++)
            product *= sum;

        return product;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _a = in.readDouble();
        _b = in.readDouble();
        _degree = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeDouble(_a);
        out.writeDouble(_b);
        out.writeInt(_degree);
    }


}
