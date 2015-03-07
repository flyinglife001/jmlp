package kernel;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Kernel implements java.io.Externalizable {
    public double eval(double[] x, double[] y) {
        return 0f;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    }
}
