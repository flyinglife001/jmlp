package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.JSplitPane;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import core.InFile;
import core.OutFile;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ApplicationFrame extends JFrame implements java.io.Externalizable {

    // The main content of pane.
    private JPanel contentPane;
    // button for opening the data-set.
    private JButton open_button;

    // parent dir of the data-set for the response of the leaf node in JTree.
    private String parent_dir;
    // the current directory on the data-set.
    private String model_file = "", current_dir = "";

    // contain the classify_results filtered from the output
    private Hashtable<String, String> classify_results = new Hashtable<String, String>();
    // contain the misclassify file name.
    private HashSet<String> error_results = new HashSet<String>();

    // main multi-thread worker
    private SwingWorker<Void, Void> worker;

    /**
     * provide the three status of the open dialog and show the dialog with only
     * file, or only directory, or both.
     */
    public enum OpenFilter {
        ONLY_FILE, ONLY_DIR, DIR_AND_FILE
    }

    ;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ApplicationFrame frame = new ApplicationFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Create the frame.
     */
    public ApplicationFrame() {
        setTitle("ML-RAP");
        /*
		 * set the parameter of the close operation in order to response the
		 * window listener on the close and open operation.
		 */
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setBounds(50, 10, 1208, 724);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JSplitPane updownsplitPane = new JSplitPane();
        updownsplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        contentPane.add(updownsplitPane);

        JSplitPane leftright_splitPane = new JSplitPane();
        leftright_splitPane.setDividerLocation(200);
        updownsplitPane.setRightComponent(leftright_splitPane);

        JScrollPane left_scrollPane = new JScrollPane();
        left_scrollPane
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        left_scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        leftright_splitPane.setLeftComponent(left_scrollPane);

        final JTabbedPane right_tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        leftright_splitPane.setRightComponent(right_tabbedPane);

        JPanel show_pane = new JPanel();
        right_tabbedPane.addTab("Show", null, show_pane, null);
        show_pane.setLayout(new BorderLayout(0, 0));

        JPanel panel_2 = new JPanel();
        show_pane.add(panel_2, BorderLayout.NORTH);

        final JLabel lbl_information = new JLabel("Predict Output");
        lbl_information.setHorizontalAlignment(SwingConstants.LEFT);
        panel_2.add(lbl_information);

        JScrollPane scrollPane_1 = new JScrollPane();
        show_pane.add(scrollPane_1, BorderLayout.CENTER);

        final JTextArea text_textArea = new JTextArea();
        text_textArea.setLineWrap(true);
        text_textArea.setFont(new Font("Monospaced", Font.BOLD, 15));
        scrollPane_1.setViewportView(text_textArea);

        JScrollPane output_scrollPane = new JScrollPane();
        output_scrollPane
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        output_scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        right_tabbedPane.addTab("Output", null, output_scrollPane, null);

        final JTextArea output_textArea = new JTextArea();
        output_textArea.setFont(new Font("Monospaced", Font.BOLD, 15));
        output_textArea.setEditable(false);
        output_scrollPane.setViewportView(output_textArea);

        final JTree file_tree = new JTree();
        file_tree.setModel(new DefaultTreeModel(
                new DefaultMutableTreeNode("root") {
                    {
                    }
                }
        ));
        file_tree
                .setToolTipText("list the directory");

        file_tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        file_tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                JTree tree = (JTree) e.getSource();
                DefaultMutableTreeNode selectNode = (DefaultMutableTreeNode) tree
                        .getLastSelectedPathComponent();

                // when no node is selected, the procedure will return.
                if (selectNode == null)
                    return;

                if (!selectNode.isLeaf())
                    return;

                // get the all nodes from the root to the current node.
                TreeNode[] nodes = selectNode.getPath();

                // remove the '\' if exists.
                StringBuilder sb;
                if (parent_dir.endsWith("\\")) {
                    sb = new StringBuilder(parent_dir.substring(0, parent_dir
                            .length() - 1));
                } else {
                    sb = new StringBuilder(parent_dir);
                }

                // join the node to get the full path name for the response of
                // the leaf node.
                for (TreeNode node : nodes) {
                    sb.append("\\");
                    sb.append(node.toString());
                }

                // OutFile.printf("sb: %s\n",sb.toString());

                right_tabbedPane.setSelectedIndex(0);

                String file_name = sb.toString();

                if (file_name.endsWith(".dat") || file_name.endsWith(".svm")) {
                    ArrayList<String> lines = InFile.read_lines(sb.toString());
                    int i = 0;

                    sb = new StringBuilder();

                    for (String line : lines) {
                        //insert the classify results if finishing classifying.
                        if (!classify_results.isEmpty())
                            sb.append(classify_results.get(Integer
                                    .toString(i)));
                        sb.append(" ");
                        sb.append(line);
                        sb.append("\n");
                        i++;
                    }

                    text_textArea.setText(sb.toString());
                } else {
                    text_textArea.setText(InFile.read_text(file_name));
                    // OutFile.printf("response: %s\n",file_name);

                    //show the classify results in the label control.
                    if (classify_results.containsKey(file_name)) {
                        lbl_information
                                .setText(classify_results.get(file_name));
                    }
                }

            }
        });
        left_scrollPane.setViewportView(file_tree);

        JPanel panel = new JPanel();
        updownsplitPane.setLeftComponent(panel);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        open_button = new JButton("Open test file or dir");
        open_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                File file = open_file_dialog(OpenFilter.DIR_AND_FILE,
                        new String[]{".dat", ".svm"});

                error_results.clear();

                if (file == null)
                    return;

                final String name = file.getAbsolutePath();

                new SwingWorker<Void, Void>() {
                    protected Void doInBackground() {
                        file_tree.setModel(build_dir_tree(name, ""));
                        return null;
                    }
                }.execute();

                current_dir = name;

            }
        });

        final JButton btnmodel_Button = new JButton(
                "Open model file...");

        btnmodel_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                File name = open_file_dialog(OpenFilter.DIR_AND_FILE, new String[]{".mdl"});
                if (name == null)
                    return;

                model_file = name.getAbsolutePath();
            }
        });

        panel.add(btnmodel_Button);
        panel.add(open_button);

        final JButton run_button = new JButton("Run");

        run_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // set focus to output_textArea.
                right_tabbedPane.setSelectedIndex(1);
                run_button.setEnabled(false);
                output_textArea.setText("");

                //construct the java running.
                final StringBuilder cmd_sb = new StringBuilder(
                        "java -jar learn.jar -Xms512m -Xmx1440m");
                cmd_sb.append(" -test_mode ");
                cmd_sb.append(model_file);
                cmd_sb.append(" -test ");
                cmd_sb.append(current_dir);
                cmd_sb.append(" -verbose 1");


                System.out.printf("begin to excute: \n%s\n", cmd_sb.toString());

				/*The use of SwingWorker<T,V>: T: the return typ of doInBackground method.
				 * V: the type of immediate result used by publish method. 
				 * doInBackground method call publish method and store it in List<V>. The process
				 * method will process the results.
				 * */

                worker = new SwingWorker<Void, Void>() {

                    protected Void doInBackground() {

                        classify_results.clear();
                        error_results.clear();

                        String console_output = "";
                        try {

                            // OutFile.printf(cmd_sb.toString() + "\n");
                            Process process = Runtime.getRuntime().exec(
                                    cmd_sb.toString());


                            if (isCancelled()) {
                                process.destroy();
                                process = null;

                                OutFile.printf("the thread will interrupt...\n");
                                return null;
                            }

                            //get the output of the running.
                            BufferedReader bufferedReader = new BufferedReader(
                                    new InputStreamReader(process
                                            .getInputStream()));
                            while ((console_output = bufferedReader.readLine()) != null) {
                                if (isCancelled()) {
                                    process.destroy();
                                    process = null;

                                    OutFile
                                            .printf("the thread will interrupt...\n");
                                    break;
                                }

                                //OutFile.printf(console_output + "\n");

                                if (console_output.startsWith("@")) {
                                    //OutFile.printf(console_output + "\n");
                                    // retrieval the classify results.
                                    int end = console_output.indexOf(" ");
                                    String key = console_output.substring(1,
                                            end);
                                    String value = console_output
                                            .substring(end + 1);

                                    //OutFile.printf("key: %s value: %s\n",key,value);

                                    classify_results.put(key, value);
                                    if (value.startsWith("**")) {
                                        //OutFile.printf("error: %s\n",key);
                                        error_results.add(key);
                                    }
                                } else {
                                    publish(console_output + "\n");
                                }
                            }
                            // process.waitFor();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        return null;
                    }


                    protected Void publish(String output) {
                        output_textArea.append(output);
                        return null;
                    }


                    protected void done() {
                        run_button.setEnabled(true);

                        if (!current_dir.isEmpty()) {
                            file_tree.setModel(build_dir_tree(current_dir, ""));
                            file_tree.setCellRenderer(new TreeCellRenderer());
                        }
                    }
                };

                worker.execute();
            }

        });
        panel.add(run_button);

        JButton btnCancelButton = new JButton("Cancel");
        btnCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (worker != null && !worker.isCancelled()) {
                    worker.cancel(true);
                    worker = null;
                    run_button.setEnabled(true);
                }
            }
        });
        panel.add(btnCancelButton);

        right_tabbedPane.setSelectedIndex(1);

        addWindowListener(new WindowAdapter() {

            public void windowOpened(WindowEvent e) {
                ObjectInputStream in = null;
                try {
                    in = new ObjectInputStream(new FileInputStream("frame_ini"));
                    readExternal(in);
                    in.close();
                } catch (Exception exp) {
                }
                new SwingWorker<Void, Void>() {


                    protected Void doInBackground() throws Exception {
                        file_tree.setModel(build_dir_tree(current_dir, ""));
                        // TODO Auto-generated method stub
                        return null;
                    }

                }.execute();
            }


            public void windowClosed(WindowEvent e) {
                ObjectOutputStream out = null;
                try {
                    out = new ObjectOutputStream(new FileOutputStream(
                            "frame_ini"));
                    writeExternal(out);
                    out.close();
                } catch (Exception exp) {
                }
            }
        });

    }

    class TreeCellRenderer extends DefaultTreeCellRenderer {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded,
                    leaf, row, hasFocus);

            DefaultMutableTreeNode render_node = (DefaultMutableTreeNode) value;
            TreeNode[] nodes = render_node.getPath();

            StringBuilder sb;
            if (parent_dir.endsWith("\\")) {
                sb = new StringBuilder(parent_dir.substring(0, parent_dir
                        .length() - 1));
            } else {
                sb = new StringBuilder(parent_dir);
            }

            for (TreeNode node : nodes) {
                sb.append("\\");
                sb.append(node.toString());

            }

            //OutFile.printf("node: %s\n",sb.toString());

            //set the background of the node to red if mis-classified.
            if (error_results.contains(sb.toString())) {
                setForeground(Color.RED);
            }

            return this;
        }

    }

    ;

    /**
     * open the file selection dialog. </br>
     *
     * @param open_status the filter type of the open dialog, @see OpenFileter </br>
     * @param fileters    the set of the filters e.g. .dat or .svm </br>
     * @return the selected File object otherwise return null.
     */
    public final File open_file_dialog(final OpenFilter open_status,
                                       final String[] filters) {
        JFileChooser chooser = new JFileChooser();
        // choose the directory for loading the text.

        switch (open_status) {
            case ONLY_FILE:
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
            case DIR_AND_FILE:
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                break;
            case ONLY_DIR:
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                break;
        }

        File current_file = new File(current_dir);
        //set the current directory.
        chooser.setCurrentDirectory(current_file.getParentFile());
        //set the selection to the last selected file.
        chooser.setSelectedFile(current_file);

        chooser.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                String file_name = f.getAbsolutePath();

                if (filters == null)
                    return true;

                for (String s : filters) {
                    if (file_name.endsWith(s))
                        return true;
                }

                if (open_status != OpenFilter.ONLY_FILE && f.isDirectory())
                    return true;
                // TODO Auto-generated method stub
                return false;
            }


            public String getDescription() {
                // TODO Auto-generated method stub
                return "data file or directory";
            }

        });

        int r = chooser.showDialog(null, "Open");
        if (r == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }

        return null;
    }


    public DefaultTreeModel build_dir_tree(String dir_name, String filter) {
        File dir = new File(dir_name);
        parent_dir = dir.getParent();

        return new DefaultTreeModel(list_dir(dir, filter));
    }

    private DefaultMutableTreeNode list_dir(File dir, String suffix) {
        if (dir.isFile()) {
            String path = dir.getName();
            if (suffix.equals("") || path.endsWith(suffix)) {

                DefaultMutableTreeNode tree_node = new DefaultMutableTreeNode(
                        path);

                //OutFile.printf("%s\n",tree_node.getUserObject().toString());

                return tree_node;
            } else {
                return null;
            }
        } else {
            File[] file_array = dir.listFiles();

            DefaultMutableTreeNode tree_node = new DefaultMutableTreeNode(dir
                    .getName()), sub_node;
            // tree_node.setUserObject(dir.getAbsolutePath());

            for (int i = 0; i < file_array.length; i++) {
                sub_node = list_dir(file_array[i], suffix);
                if (sub_node != null) {
                    tree_node.add(sub_node);
                }
            }

            return tree_node;
        }
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        // TODO Auto-generated method stub
        this.current_dir = (String) in.readObject();

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeObject(this.current_dir);
    }
}
