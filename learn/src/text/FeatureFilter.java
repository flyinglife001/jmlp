package text;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import cmd.General;

import core.DataSet;
import core.OutFile;
import core.Utility;
import core.Utility.StringValue;

public class FeatureFilter {
    private Hashtable<String, Integer> _feature_freq = new Hashtable<String, Integer>();
    private int[] _class_freq;
    private int _n_docs;
    private int _n_classes, _n_features = 100, _method = 0;
    private ArrayList<StringValue> _feature_set = new ArrayList<StringValue>();
    private TextReader _text_reader;

    public FeatureFilter(TextReader text_reader) {
        _n_features = Integer.parseInt(General.get("-n_select"));

        String method_name = General.get("-select");
        //String method_name = "ig";

        _method = 0;

        if (method_name.equals("ig")) {
            _method = 0;
        }

        _text_reader = text_reader;
    }

    public void count_freq() {
        _n_classes = _text_reader._class_map.size();
        _class_freq = new int[_n_classes];

        ArrayList<Hashtable<String, Integer>> data_table = _text_reader._data_table;
        _n_docs = data_table.size();

        ArrayList<int[]> y = _text_reader._y;

        int i = 0, label;
        int cnt;
        //consider each document.
        for (Hashtable<String, Integer> feature : data_table) {
            // count P(C)
            label = y.get(i)[0];
            _class_freq[label]++;
            String class_str = Integer.toString(label);

            for (String words : feature.keySet()) {
                // count P(w)
                if (_feature_freq.containsKey(words)) {
                    cnt = _feature_freq.get(words).intValue();
                } else {
                    // add the words to feature_set.
                    _feature_set.add(new StringValue(words, 0));
                    cnt = 0;
                }
                _feature_freq.put(words, new Integer(cnt + 1));

                // count P(w,c)
                StringBuilder sb = new StringBuilder(words);
                sb.append(":");
                sb.append(class_str);
                String pairs = sb.toString();

                if (_feature_freq.containsKey(pairs)) {
                    cnt = _feature_freq.get(pairs).intValue();
                } else {
                    cnt = 0;
                }

                _feature_freq.put(pairs, new Integer(cnt + 1));
            }

            i++;
        }

//		OutFile.set("test.txt");
//		for(String s: _feature_freq.keySet())
//		{
//			OutFile.printf("%s %d\n",s,_feature_freq.get(s).intValue());
//		}
//		OutFile.close();

    }

    public void select_df(int threshold) {
        // default threshold parameter 2.
        int threshold_value = 3;
        if (threshold >= 0) {
            threshold_value = threshold;
        }

        // remove the low frequency words, note that use the iterator and
        // other method leading an error.
        Iterator<StringValue> iter = _feature_set.iterator();

        while (iter.hasNext()) {
            StringValue sv = iter.next();
            if (_feature_freq.get(sv._index).doubleValue() < threshold_value) {
                iter.remove();
            }
        }

    }

    public void select_long(int threshold) {
        // default threshold parameter 3.
        int threshold_value = 3;
        if (threshold >= 0) {
            threshold_value = threshold;
        }

        // remove the low frequency words, note that use the iterator and
        // other method leading an error.
        Iterator<StringValue> iter = _feature_set.iterator();

        while (iter.hasNext()) {
            StringValue sv = iter.next();
            if (sv._index.length() < threshold_value) {
                iter.remove();
            }
        }

    }

    public ArrayList<String> select() {
        count_freq();

        select_long(-1);

        for (StringValue sv : _feature_set) {
            switch (_method) {
                case 0:
                    sv._value = select_ig(sv._index);
                    //OutFile.printf("%f\n",sv._value);
                    break;
                case 1:
                    sv._value = select_chi(sv._index);
                    break;
            }
        }

        //OutFile.set("test.txt");
//		OutFile.printf(_class_freq);
//		for(StringValue s: _feature_set)
//		{
//			OutFile.printf("%f\n",s._value);
//		}

        Collections.sort(_feature_set, Collections.reverseOrder());

        ArrayList<String> feature_name = new ArrayList<String>();

        int n_features = Math.min(_n_features, _feature_set.size()), i;

        //OutFile.set("test.txt");
        for (i = 0; i < n_features; i++) {
            //OutFile.printf("%s %f\n", _feature_set.get(i)._index, _feature_set.get(i)._value);
            feature_name.add(_feature_set.get(i)._index);
        }
        //OutFile.close();


        return feature_name;
    }


    private double select_chi(String key) {
        double score = 0f;

        return score;
    }

    private int get_num_feature(String feature) {
        if (_feature_freq.containsKey(feature)) {
            return _feature_freq.get(feature).intValue();
        } else {
            return 0;
        }
    }

    private int get_num_fea_label(String feature, int label) {
        StringBuilder sb = new StringBuilder(feature);
        sb.append(":");
        sb.append(Integer.toString(label));
        String pairs = sb.toString();
        if (_feature_freq.containsKey(pairs)) {
            return _feature_freq.get(pairs).intValue();
        } else {
            return 0;
        }
    }

    private double select_ig(String key) {
        double score = 0f;
        int feature_in_doc = get_num_feature(key);
        int feature_notin_doc = _n_docs - feature_in_doc;
        int i;
        for (i = 0; i < _n_classes; i++) {
            int class_doc = _class_freq[i];
            int feature_class_doc = get_num_fea_label(key, i);
            if (feature_class_doc != 0) {
                score += 1.0 * feature_class_doc / class_doc
                        * Math.log(1.0 * feature_class_doc / feature_in_doc);
            }

            int feature_class_not_doc = class_doc - feature_class_doc;
            if (feature_class_not_doc != 0) {
                score += 1.0 * feature_class_not_doc / class_doc
                        * Math.log(1.0 * feature_class_not_doc
                        / feature_notin_doc);
            }

        }

        return score;
    }

    // public void

    public static void main(String[] args) {
        String name = "D:/ml-rap-dist/test";

        TextReader tr = new TextReader(true);
        ArrayList<String> file_names = Utility.traverse(name, "");

        tr.read(file_names);
        FeatureFilter fs = new FeatureFilter(tr);
        fs.select();

        //OutFile.printf("%s\n",System.getProperty("file.encoding"));
    }

}
