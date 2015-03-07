package utility;

import java.util.ArrayList;

import core.InFile;
import core.OutFile;


public class DataUtility {

    public static void read_uci(String file_name) {
        int fold_pos = file_name.lastIndexOf('\\');
        String out_file = file_name.substring(0, fold_pos) + "\\train_data.dat";
        OutFile.set(out_file);
        ArrayList<String> lines = InFile.read_lines(file_name);
        int i;
        for (String s : lines) {
            String[] line_str = s.split("[,\n\t]");
            StringBuilder convert_str = new StringBuilder(line_str[line_str.length - 1]);
            for (i = 0; i < line_str.length - 1; i++) {
                convert_str.append(" ");
                convert_str.append(line_str[i]);
            }
            OutFile.printf("%s\n", convert_str.toString());
        }
        OutFile.close();

        OutFile.printf(out_file);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String file_name = "D:\\UCI\\classification\\glass\\glass.data";
        read_uci(file_name);
    }

}
