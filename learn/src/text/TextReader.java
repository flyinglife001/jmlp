package text;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;


import core.InFile;
import core.OutFile;
import core.Utility;

import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;


public class TextReader {
    public Hashtable<String, Integer> _class_map = new Hashtable<String, Integer>();
    public ArrayList<Hashtable<String, Integer>> _data_table = new ArrayList<Hashtable<String, Integer>>();
    private HashSet<String> _en_stopwords = new HashSet<String>();
    private HashSet<String> _ch_stopwords = new HashSet<String>();
    private Stemmer _stemmer = new Stemmer();
    private IKAnalyzer _ik = new IKAnalyzer(true);
    public boolean _is_train = true;
    public int _n_classes = 0;
    public ArrayList<int[]> _y = new ArrayList<int[]>();

    public TextReader(boolean is_train) {
        //read the dictionary separately.
        ArrayList<String> english = InFile.read_lines("./enlish_stopword.dic");
        ArrayList<String> chinese = InFile.read_lines("./chinese_stopword.dic");
        for (String s : english) {
            _en_stopwords.add(s);
        }
        for (String s : chinese) {
            _ch_stopwords.add(s);
        }

        _is_train = is_train;
        _n_classes = 0;

    }


    public void read(ArrayList<String> file_names) {

        for (String name : file_names) {
            String file_name = name.toLowerCase();

            // OutFile.printf("file: %s\n",file_name);

            if (file_name.endsWith(".html")) {
                //String text = InFile.read_text(name);
                //plain_read(new String[] { text });
            } else if (file_name.endsWith(".pdf")) {

            } else if (file_name.endsWith(".doc")) {
            } else {
                //read the text from the sign "Lines:"
                //String text = InFile.read_text(name,"Lines");

                String text = InFile.read_text(name);

                //OutFile.printf("file name: %s\n%s\n",name,text);

                plain_read(new String[]{text});
            }

            extract_label(name);
        }
    }

    private void extract_label(String file_name) {
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
            _y.add(target);
            return;
        }

        if (!_class_map.containsKey(class_name)) {
            label = 0;
        } else {
            label = _class_map.get(class_name).intValue();
        }

        target[0] = label;
        _y.add(target);
        return;
    }

    public void plain_read(String[] text_set) {
        String[] words;

        // OutFile.printf("file: %s\n", file_name);

        Hashtable<String, Integer> features = new Hashtable<String, Integer>();

        // read the content of the file.
        for (String text : text_set) {

            if (text.length() == text.getBytes().length) {
                //split by non-word char.
                words = text.split("[^A-Za-z]+");

                for (String str : words) {
                    str = str.toLowerCase();

                    str = str.trim();

                    if (str.isEmpty())
                        continue;

                    if (_en_stopwords.contains(str))
                        continue;


                    _stemmer.add(str.toCharArray(), str.length());
                    _stemmer.stem();
                    str = _stemmer.toString();

                    //OutFile.printf("%s => %s\n",w,str);

                    if (features.containsKey(str)) {
                        int cnt = features.get(str).intValue() + 1;
                        features.put(str, new Integer(cnt));
                    } else {
                        features.put(str, new Integer(1));
                    }
                }
            } else {
                IKSegmenter iks = new IKSegmenter(new StringReader(text), true);
                Lexeme lex;
                try {
                    String str;
                    while ((lex = iks.next()) != null) {
                        str = lex.getLexemeText();

                        if (_ch_stopwords.contains(str))
                            continue;

                        if (features.containsKey(str)) {
                            int cnt = features.get(str).intValue() + 1;
                            features.put(str, new Integer(cnt));
                        } else {
                            features.put(str, new Integer(1));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

//		for(String s: features.keySet())
//		{
//			OutFile.printf("%s:%d ",s,features.get(s).intValue());
//		}
//		OutFile.printf("\n");

        _data_table.add(features);

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String name = "D:/ml-rap-dist/test/IT";

        TextReader tr = new TextReader(true);
        ArrayList<String> file_names = new ArrayList<String>();
        file_names.add(name);

        tr.read(file_names);

        ArrayList<Hashtable<String, Integer>> table = tr._data_table;

        for (Hashtable<String, Integer> hash : table) {
            for (Entry<String, Integer> entry : hash.entrySet()) {
                OutFile.printf("%s:%d\n", entry.getKey(), entry.getValue()
                        .intValue());
            }
            OutFile.printf("\n");
        }

        ArrayList<int[]> y = tr._y;
        for (int[] target : y) {
            OutFile.printf(target);
        }

        for (Entry<String, Integer> entry : tr._class_map.entrySet()) {
            OutFile.printf("%s:%d\n", entry.getKey(), entry.getValue()
                    .intValue());
        }

        OutFile.printf("the class: %d %d\n", tr._class_map.size(), table.size());

    }

}
