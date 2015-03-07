package kernel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import mat.Vec;

import core.OutFile;

public class GaussianKernel extends Kernel {
    private double _std;

    public GaussianKernel(double std) {
        _std = std;
    }

    public double eval(double[] x, double[] y) {
        double dist = Vec.distance(x, y);
        return Math.exp(-dist * Math.pow(2.0, _std));
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _std = in.readDouble();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeDouble(_std);
    }

}
