package core;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.Map.Entry;

import text.FeatureFilter;
import text.TextReader;

/**
 The dataset utility is used for loading the dataset, print the dataset.
 */

public class DataSet implements java.io.Externalizable {

    public static enum LabelFormat {
        onehot, oneone, _null
    }

    ;

    // share the all attribute by the stack. Important design for the efficient
    // implementation.

    // stack for the selection of the examples.
    private Stack<ArrayList<Integer>> _subset_stack = new Stack<ArrayList<Integer>>();

    // pointer to the current examples;
    public ArrayList<Integer> _select_frames = new ArrayList<Integer>();

    // save X as backup for the cascade of the machines.
    private ArrayList<double[]> _X_backup = new ArrayList<double[]>();
    private ArrayList<int[]> _y_backup = new ArrayList<int[]>();

    public LabelFormat _label_format = LabelFormat._null;

    // feature vector
    public ArrayList<double[]> _X = new ArrayList<double[]>();

    // target vector
    public ArrayList<int[]> _y = new ArrayList<int[]>();
    public ArrayList<double[]> _weights = new ArrayList<double[]>();
    public int _n_rows = 0, _n_cols = 0, _n_classes = 0, _class_index = 0;

    // map the string class to integer.
    public Hashtable<String, Integer> _class_map = new Hashtable<String, Integer>();
    public String[] _inv_class_map;
    // use for real application such as image or text categorization.
    public ArrayList<String> _files = new ArrayList<String>();
    public ArrayList<String> _feature_name = new ArrayList<String>();

    public boolean _is_train = true;

    public DataSet(boolean is_train) {
        _is_train = is_train;
    }

    public DataSet(boolean is_train, int n_cols) {
        _is_train = is_train;
        _n_cols = n_cols;
    }

    public DataSet(ArrayList<double[]> X, ArrayList<int[]> y) {
        _X = X;
        _y = y;
        _n_rows = _X.size();
        _n_cols = _X.get(0).length;

        int i;
        int[] label;
        _n_classes = 0;
        for (i = 0; i < _n_rows; i++) {
            _select_frames.add(new Integer(i));
            _X_backup.add(_X.get(i));
            if (y != null) {
                label = _y.get(i);
                if (label[0] > _n_classes)
                    _n_classes = label[0];

                _y_backup.add(label);
            }
            _weights.add(null);
        }

        _n_classes++;
        _subset_stack.push(_select_frames);
    }


    public String get_class_name(int y) {
        if (y < _inv_class_map.length)
            return _inv_class_map[y];
        else
            return "0";
    }

    public String get_fileid(int f_index) {
        return _files.get(_select_frames.get(f_index).intValue());
    }

    public int get_label(int id_no) {
        // OutFile.printf("%d  org: %d:\n",id_no,_select_frames.get(id_no).intValue());
        // OutFile.printf(_y.get(_select_frames.get(id_no).intValue()));

        return _y.get(_select_frames.get(id_no).intValue())[_class_index];
    }

    public int[] get_y(int id_no) {

        return _y.get(_select_frames.get(id_no).intValue());
    }

    public double[] get_X(int id_no) {
        return _X.get(_select_frames.get(id_no).intValue());
    }

    public void set_y(int id_no, int[] y) {
        _y.set(_select_frames.get(id_no).intValue(), y);
    }

    public double[] get_weight(int id_no) {
        return _weights.get(_select_frames.get(id_no).intValue());
    }

    public void clear() {
        // reset the number
        _n_rows = 0;
        _select_frames.clear();
        _X.clear();
        _y.clear();
    }

    /**
     * the data cols will change, but the row probably may be changed.
     */
    public void set_XY(ArrayList<double[]> X, ArrayList<int[]> y) {
        _n_cols = X.get(0).length;
        _n_rows = X.size();

        int i;
        for (i = 0; i < _n_rows; i++) {
            _X.set(_select_frames.get(i).intValue(), X.get(i));
        }

        if (y != null) {
            for (i = 0; i < _n_rows; i++) {
                _y.set(_select_frames.get(i).intValue(), y.get(i));
            }
        }
    }

    // it only change the content of the data.
    public void reset_XY() {
        // recovery the X and the y;
        for (int i = 0; i < _X_backup.size(); i++) {
            _X.set(i, _X_backup.get(i));
            _y.set(i, _y_backup.get(i));
        }
        _n_cols = _X.get(0).length;
    }

    public void set_class_index(int c) {
        // only suitable to the common dataset.
        // OutFile.printf("begin to set index %d\n",c);
        _class_index = c;
        ArrayList<Integer> useful = new ArrayList<Integer>();
        int i = 0, label;

        for (i = 0; i < _n_rows; i++) {
            label = get_label(i);

            if (label >= 0) {
                useful.add(new Integer(i));
            }
        }

        push_subset(useful);// collect the useful element of the dataset.

        // OutFile.printf("%d the example: %d\n",_class_index,_n_classes);
    }

    public void reset_class_index() {
        pop_subset();
        _class_index = 0;
    }

    // @overwrite the subset in the procedure. the data row will change.
    public void push_subset(ArrayList<Integer> subset) {
        ArrayList<Integer> map_set = new ArrayList<Integer>();
        int n_lists = subset.size();

        int i;
        for (i = 0; i < n_lists; i++) {
            map_set.add(_select_frames.get(subset.get(i)));// map the id to //
            // its real id;
        }
        _select_frames = map_set;
        _n_rows = _select_frames.size();
        _subset_stack.push(_select_frames);
    }

    public void pop_subset() {
        _subset_stack.pop();
        _select_frames = _subset_stack.lastElement();
        _n_rows = _select_frames.size();
    }

    public void set_label_format() {
        // set the different label format for multiple binary classifier or rank
        // problems .etc.
        switch (_label_format) {
            case onehot:
                set_onehot_format();
                break;
            case oneone:
                set_oneone_format();
                break;
            default:
                break;
        }
    }

    private void set_onehot_format() {
        int[][] format_mat = new int[_n_classes][_n_classes];

        Utility.assign(format_mat, 0);
        int i, label;
        for (i = 0; i < format_mat.length; i++) {
            format_mat[i][i] = 1;
        }

        // XFile.printf(format_mat);
        for (i = 0; i < _n_rows; i++) {
            label = get_label(i);
            set_y(i, format_mat[label]);

            // OutFile.printf(get_y(i));
        }
    }

    private void set_oneone_format() {
        if (_n_classes == 2)
            return;

        int[][] format_mat = new int[_n_classes][_n_classes * (_n_classes - 1)
                / 2];
        Utility.assign(format_mat, -1);
        int i, j, col = 0, label;
        for (i = 0; i < format_mat.length - 1; i++) {
            for (j = i + 1; j < format_mat.length; j++) {
                format_mat[i][col] = 0;
                format_mat[j][col] = 1;
                col++;
            }
        }
        // XFile.printf(format_mat);
        for (i = 0; i < _n_rows; i++) {
            label = get_label(i);
            set_y(i, format_mat[label]);
        }
    }

    public void copy_model(DataSet data) {
        _n_rows = data._n_rows;
        _n_cols = data._n_cols;
        _n_classes = data._n_classes;
        _class_index = data._class_index;
        _is_train = data._is_train;
        _label_format = data._label_format;
        _class_map = data._class_map;
        _inv_class_map = data._inv_class_map;
        _feature_name = data._feature_name;
    }

    public void load_file(String file_name) {
        int index = file_name.lastIndexOf(".");
        String suffix = file_name.substring(index + 1);
        File file = new File(file_name);

        if (file.isDirectory()) {
            ArrayList<String> file_names = Utility.traverse(file_name, "");

            // check the file type is image or text?
            String check_file = file_names.get(file_names.size() / 2);
            load_txts(file_names);

        } else if (suffix.equals("dat")) {
            load_dat(file_name);
        } else if (suffix.equals("svm")) {
            load_svm(file_name);
        } else {
            OutFile.error("%s format can not support...", suffix);
        }

        int i;
        if (_is_train) {
            _inv_class_map = new String[_n_classes];

            i = 0;
            for (Entry<String, Integer> entry : _class_map.entrySet()) {
                _inv_class_map[entry.getValue().intValue()] = entry.getKey();
                // OutFile.printf(key + "\n");
                i++;
            }
        }

        // initial sub-stack.

        _select_frames.clear();
        if (file.isDirectory()) {
            for (i = 0; i < _n_rows; i++) {
                _select_frames.add(i);
            }
        } else {
            for (i = 0; i < _n_rows; i++) {
                _select_frames.add(i);
                _files.add(Integer.toString(i));
            }
        }
        _subset_stack.push(_select_frames);

    }

    private int map_str_name(String class_name) {
        int label = 0;
        if (_is_train && !_class_map.containsKey(class_name)) {
            _class_map.put(class_name, new Integer(_n_classes));
            label = _n_classes;
            _n_classes++;
            return label;
        }

        if (!_class_map.containsKey(class_name)) {
            label = 0;
        } else {
            label = _class_map.get(class_name).intValue();
        }

        return label;
    }


    private int[] extract_label(String file_name) {
        int end = file_name.lastIndexOf("\\", file_name.length() - 1);
        int pos = file_name.lastIndexOf("\\", end - 1);

        int[] target = new int[1];

        String class_name;
        if (pos > -1) {
            class_name = file_name.substring(pos + 1, end);
        } else {
            class_name = "0";
        }

        // OutFile.printf("f:%s c:%s\n",file_name,class_name);

        int label = 0;
        if (_is_train && !_class_map.containsKey(class_name)) {
            _class_map.put(class_name, new Integer(_n_classes));
            label = _n_classes;
            _n_classes++;

            target[0] = label;

            return target;
        }

        if (!_class_map.containsKey(class_name)) {
            label = 0;
        } else {
            label = _class_map.get(class_name).intValue();
        }

        target[0] = label;

        return target;
    }

    private void load_txts(ArrayList<String> file_names) {
        // OutFile.printf("_n_cols:%d\n",_class_map.);

        TextReader tr = null;
        if (_is_train) {
            tr = new TextReader(true);
            tr.read(file_names);
            FeatureFilter fs = new FeatureFilter(tr);
            _feature_name = fs.select();
            _class_map = tr._class_map;
            _n_cols = _feature_name.size();
            _n_classes = tr._n_classes;
        } else {
            tr = new TextReader(false);
            tr._class_map = _class_map;
            tr.read(file_names);
        }

        // construct the X

        int j;

        ArrayList<Hashtable<String, Integer>> data_table = tr._data_table;

        _n_rows = 0;
        _y = tr._y;

        for (Hashtable<String, Integer> feature : data_table) {
            double[] feat_vect = new double[_n_cols];
            for (j = 0; j < _n_cols; j++) {
                String word = _feature_name.get(j);

                if (feature.containsKey(word)) {
                    feat_vect[j] = feature.get(word).intValue();
                    //OutFile.printf("%s %f ",word,feat_vect[j]);
                }
            }
            //OutFile.printf("\n");

            _X.add(feat_vect);
            _X_backup.add(feat_vect);
            _y_backup.add(_y.get(_n_rows));
            _weights.add(null);
            _n_rows++;
        }

        _files = file_names;

        OutFile.printf("load file-> rows: %d cols: %d classes: %d\n", _n_rows,
                _n_cols, _n_classes);

    }

    private void load_svm(String file_name) {
        int i, j, n_missing = 0;
        int f_len, n_features;

        ArrayList<String> features = InFile.read_lines(file_name);
        String[] str_array;
        String[] features_str;
        String classes_str;

        _n_rows = 0;

        // used for temporally storing sparse svm format.
        ArrayList<double[]> temp_space = new ArrayList<double[]>();

        double[] feature;
        for (String line : features) {
            str_array = line.split("\\s+");

            // judge it has no labels.

            classes_str = str_array[0].trim();

            f_len = str_array.length - 1;
            feature = new double[2 * f_len];

            for (j = 0; j < f_len; j++) {
                features_str = str_array[j + 1].trim().split(":");
                // store the feature index.
                n_features = Integer.parseInt(features_str[0]);
                feature[2 * j] = n_features;

                if (features_str[1].equals("?")) {
                    feature[2 * j + 1] = Double.NaN;
                    n_missing++;
                } else {
                    feature[2 * j + 1] = Double.parseDouble(features_str[1]);
                }
            }

            if (_is_train && feature[feature.length - 2] > _n_cols)
                _n_cols = (int) feature[feature.length - 2];

            temp_space.add(feature);

            int[] label = new int[1];
            label[0] = map_str_name(classes_str);

            _y.add(label);
            _y_backup.add(label);

            // add the space of the weights;
            _weights.add(null);
            _n_rows++;
        }

        for (i = 0; i < _n_rows; i++) {
            feature = temp_space.get(i);
            double[] x_vector = new double[_n_cols];

            f_len = feature.length;
            for (j = 0; j < f_len; j += 2) {
                x_vector[(int) feature[j] - 1] = feature[j + 1];
            }

            OutFile.printf("\n");

            _X.add(x_vector);
            _X_backup.add(x_vector);
        }

        // of all labels examples
        _subset_stack.push(_select_frames);

        OutFile.printf(
                "load %s \nfile-> rows: %d cols: %d classes: %d missing: %d\n",
                file_name, _n_rows, _n_cols, _n_classes, n_missing);
    }

    private void load_dat(String file_name) {
        int n_missing = 0, i, j;

        String header = InFile.read_line(file_name);
        String str_array[] = header.split("\\s+");

        if (_is_train)
            _n_cols = str_array.length - 1;
        else if (_n_cols != str_array.length - 1) {
            OutFile.error("the test file has not the same number of features with the model file...");
        }

        String num_str;

        _n_rows = 0;
        ArrayList<String> lines = InFile.read_lines(file_name);
        for (String line : lines) {
            str_array = line.split("\\s+");

            // OutFile.printf("len: %d\n",str_array.length);
            double[] feature = new double[_n_cols];
            for (j = 0; j < _n_cols; j++) {
                num_str = str_array[j + 1].trim();
                // OutFile.printf("%s\n",num_str);
                if (num_str.equals("?")) {
                    feature[j] = Double.NaN;
                    n_missing++;
                } else {
                    feature[j] = Double.parseDouble(num_str);
                }
            }

            _X.add(feature);
            _X_backup.add(feature);

            int[] label = new int[1];
            label[0] = map_str_name(str_array[0].trim());

            _y.add(label);
            _y_backup.add(label);

            // add the space of the weights;
            _weights.add(null);

            _n_rows++;
        }

        OutFile.printf(
                "load %s \nfile-> rows: %d cols: %d classes: %d missing: %d\n",
                file_name, _n_rows, _n_cols, _n_classes, n_missing);
    }


    // print the format: .dat
    public void print_dat_format() {
        OutFile.printf("%d %d\n", _n_rows, _n_cols);
        int i, j;
        double[] feature;
        for (i = 0; i < _n_rows; i++) {
            //OutFile.printf("%d", get_label(i));

            double sum = 0;
            feature = get_X(i);
            for (j = 0; j < _n_cols; j++) {
                //OutFile.printf(" %f", feature[j]);
                sum += feature[j] * feature[j];
            }

            //OutFile.printf("\n %f\n",sum);

            // OutFile.printf(get_label(i) + "\n");
        }
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        _n_cols = in.readInt();
        _n_classes = in.readInt();
        _class_index = in.readInt();

        _label_format = (LabelFormat) in.readObject();
        _class_map = (Hashtable<String, Integer>) in.readObject();
        _feature_name = (ArrayList<String>) in.readObject();
        _inv_class_map = (String[]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(_n_cols);
        out.writeInt(_n_classes);
        out.writeInt(_class_index);

        out.writeObject(_label_format);
        out.writeObject(_class_map);
        out.writeObject(_feature_name);
        out.writeObject(_inv_class_map);
    }

    public static void main(String[] args) {
        DataSet data = new DataSet(true);
        data.load_file("D:/ml-rap-dist/test");
        // data.load_file("D:\\javaapp\\data\\train.ord");
        // data.load_file("D:\\UCIDataSet\\svmguide1\\train_data.svm");
        // data._label_format = LabelFormat.onehot;
        // data.set_label_format();
        // ArrayList<Integer> subset = new ArrayList<Integer>();
        // subset.add(new Integer(1));
        // subset.add(new Integer(3));
        // data.push_subset(subset);
        // data.print_data();
        // data.pop_subset();
        // String out_file = "D:\\javaapp\\output.ord";
        // String out_file = "D:\\javaapp\\output.dat";
        // OutFile.set(out_file);
        // data.print_ord_format();
        // data.print_dat_format();
        // OutFile.close();
        // data.load_file("D:\\UCIDataSet\\Heart\\train_data.dat");

        //data.load_file("D:\\image_data\\handdigit\\train_data.gry");
        //data.load_file("D:\\image_data\\cifar-10-binary\\cifar-10-batches-bin\\data_batch_1.cif");

        // String file_name =
        // "D:\\image_data\\object\\accordion\\image_0001.jpg";
        // BufferedImage image = UtilImageIO.loadImage(file_name);
        // ShowImages.showWindow(image, "image");

        //String dir = "D:\\image_data\\test";
        //data.load_file(dir);
        data.print_dat_format();
    }

}
