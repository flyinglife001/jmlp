package core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class InFile {
    private static BufferedReader in = null;

    public static void set(String file_name) throws IOException {
        in = new BufferedReader(new FileReader(file_name));
    }

    public static void set(String file_name, String coding) throws IOException {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(
                file_name), coding));
    }

    public static String get_line() throws IOException {
        return in.readLine();
    }


    /**
     * read the text and use the default encoding GBK.
     */
    public static String read_text(String file_name) {
        StringBuilder sb = new StringBuilder();
        String str;
        int i;
        try {
            InFile.set(file_name, "GBK");
            while ((str = in.readLine()) != null) {
                sb.append(str);
                sb.append("\n");
            }
            InFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static String read_text(String file_name, String begin_str) {
        StringBuilder sb = new StringBuilder();
        String str;

        try {
            InFile.set(file_name);

            while ((str = in.readLine()) != null) {
                if (str.startsWith(begin_str)) {
                    break;
                }
            }

            while ((str = in.readLine()) != null) {
                sb.append(str);
                sb.append("\n");
            }
            InFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static String read_line(String file_name) {
        String str;
        try {
            InFile.set(file_name);
            str = in.readLine().trim();
            InFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }

        return str;
    }

    public static ArrayList<String> read_lines(String file_name) {
        ArrayList<String> lines = new ArrayList<String>();
        String str;
        int i;
        try {
            InFile.set(file_name);
            while ((str = in.readLine()) != null) {
                str = str.trim();
                if (str.isEmpty())
                    continue;

                lines.add(str);
            }
            InFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lines;
    }

    public static void close() throws IOException {
        in.close();
    }

}
