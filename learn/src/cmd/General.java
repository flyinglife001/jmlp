package cmd;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Map.Entry;

import core.InFile;
import core.OutFile;

public class General {
    public static double SMALL_CONST = 1e-6f;

    private static Hashtable<String, String> option_dict = new Hashtable<String, String>();
    public static Hashtable<String, String> store_dict = new Hashtable<String, String>();
    public static ArrayList<String> options = new ArrayList<String>();
    public static int class_loc = 0;

    private static HashSet<String> optimizable_dict = new HashSet<String>();
    private static ArrayList<String> optimizable_para = new ArrayList<String>();


    public static void set_default() {
        String file_name = "options";
        String text_line;
        int len;
        try {
            InFile.set(file_name);
            String[] split_str;
            while ((text_line = InFile.get_line()) != null) {
                text_line = text_line.trim();

                //delete the non-option.
                if (!text_line.startsWith("-"))
                    continue;

                split_str = text_line.split("\\|");

                options.add(text_line);
                option_dict.put(split_str[0], split_str[1]);


                len = split_str.length;
                //judge it is optimizable or not?
                if (len >= 4 && split_str[3].equals("true")) {
                    optimizable_dict.add(split_str[0]);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        //set the enforced parameter if the parameter is not assigned.
        store_dict.put("-machine", "rmh");

    }


    public static String[] get_opts(String opt_name) {
        //need to filter more than 1 parameter from some options, such as
        //optimal the machine parameters in much optional values.
        //note the string in 'split' method is the regular expressions. ':' represent ':'.
        //String[] opts = store_dict.get(opt_name).split("\\|");
        //OutFile.printf("%s\n", store_dict.get(opt_name));

        String[] opts = store_dict.get(opt_name).split(";");

        int i;
        for (i = 0; i < opts.length; i++) {
            opts[i] = opts[i].trim();
            //OutFile.printf(opts[i] + "\n");
        }
        return opts;
    }

    public static void put(String opt_name, String opt_value) {
        if (!option_dict.containsKey(opt_name)) {
            OutFile.error("the invalid option name %s\n", opt_name);
        }

        option_dict.put(opt_name, opt_value);
    }

    public static void store(String opt_name, String opt_value) {
        store_dict.put(opt_name, opt_value);
    }

    public static String get(String opt_name) {
        return option_dict.get(opt_name);
    }

    public static void add_optimize(String para) {
        if (optimizable_dict.contains(para)) {
            optimizable_para.add(para);
        }
    }

    public static ArrayList<String> get_optimize() {
        return optimizable_para;
    }

    public static void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeObject(option_dict);
        out.writeObject(store_dict);
    }

    @SuppressWarnings("unchecked")
    public static void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        option_dict = (Hashtable<String, String>) in.readObject();
        store_dict = (Hashtable<String, String>) in.readObject();
    }

}
