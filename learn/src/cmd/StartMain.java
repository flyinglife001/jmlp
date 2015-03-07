package cmd;

import java.util.ArrayList;
import java.util.Hashtable;

import core.OutFile;

public class StartMain {
    public static void main(String[] args) {
        int i, len = args.length;
        if (len == 0) {
            OutFile.error("there is not enough parameters...\n");
        }

        //remove the virtual parameters from args.
        ArrayList<String> user_para = new ArrayList<String>();
        for (i = 0; i < len; ) {
            String s = args[i];
            if (s.equals("-jar")) {
                i += 2;
            } else if (s.startsWith("-Xms") || s.startsWith("-Xmx")) {
                i++;
            } else {
                user_para.add(s);
                i++;
            }
        }


        String[] para_vect = new String[user_para.size()];
        user_para.toArray(para_vect);

//		for(String s:para_vect)
//		{
//			OutFile.printf(s + "\n");
//		}

        //when not saving the model, it will run in the batch mode.
        if (para_vect.length > 0 && (para_vect[0].equals("-train_mode") || para_vect[0].equals("-test_mode"))) {
            //train_mode or test_mode
            RunMode.main(para_vect);
        } else {
            //batch learn mode
            BatchLearn.main(para_vect);
        }
    }
}
