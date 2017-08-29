package photoparty;

import classes.Event;
import classes.Impressao;
import classes.Instagram;
import classes.MyDefaultTableModel;
import classes.Photo;
import classes.Webservice;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class FrmPrincipal extends javax.swing.JFrame {

    private final Event evento;
    private String hashtag;
    private Timer timer;
    private Timer timerAutomatico;
    private Timer timerPrinter;

    private DefaultTableModel modelTblEnviadas;
    private MyDefaultTableModel modelTblImpressas;
    private final DefaultListModel modelListaImpressoras;

    private FrmTelao frame;

    private final String dirFotosEnviadas = "FotosEnviadas/";
    private final String dirFotosFila = "FotosFila/";
    private final String dirFotosImpressas = "FotosImpressas/";
    private final String dirFotosTelao = "FotosTelao/";

    private final List<String> listaFotosEnviadas;

    private Impressao listaImpressoras;
    private List<String> print;
    private String[] printsSelecteds;
    private int totPrints = 0;
    private final int printCurrent = 0;
    private int lastPrinterPrinted = 0;
    private final int current_image = 0;

    private boolean automatico = false;
    private boolean temTelao = false;
    private boolean temImpressao = false;
    private int qtdeImpressas = 0;
    private int totFotos = 0;

    private GraphicsDevice gd;

    private final static Logger LOG = Logger.getLogger(FrmPrincipal.class.getName());
    private final Object lock = new Object();
    private CountDownLatch waitForDevices;

    private Thread[] threadFila;
    private final int TIME_HANDLER = 3000;
    private Timer timerSleep;
    private boolean SLEEP = false;
    public FilaPrinter fila;

    public FrmPrincipal() {
        initComponents();

        this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("Assets/logo_postprint.png")).getImage());

        evento = new Event();
        listaFotosEnviadas = new ArrayList<String>();
        modelListaImpressoras = new DefaultListModel();

        File dirEnviadas = new File(dirFotosEnviadas);
        if (dirEnviadas.exists() == false) {
            dirEnviadas.mkdir();
        }
        File dirImpressas = new File(dirFotosImpressas);
        if (dirImpressas.exists() == false) {
            dirImpressas.mkdir();
        }

        File dirFila = new File(dirFotosFila);
        if (dirFila.exists() == false) {
            dirFila.mkdir();
        }

        File dirTelao = new File(dirFotosTelao);
        if (dirTelao.exists() == false) {
            dirTelao.mkdir();
        }

        lstImpressoras.setModel(modelListaImpressoras);
        String[] colunas = new String[]{"Foto", "Nome"};
        modelTblEnviadas = new DefaultTableModel(null, colunas);

        tblFotoEnviadas.setModel(modelTblEnviadas);
        tblFotoEnviadas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblFotoEnviadas.getColumnModel().getColumn(1).setPreferredWidth(350);

        tblFotoEnviadas.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && automatico == false) {
                    int row = tblFotoEnviadas.getSelectedRow();
                    e.consume();

                    Object[] obj = new Object[3];
                    obj[0] = tblFotoEnviadas.getValueAt(row, 0);
                    obj[1] = tblFotoEnviadas.getValueAt(row, 1).toString();
                    if (temImpressao) {
                        obj[2] = "NA FILA";
                    } else {
                        obj[2] = "NO TELÃO";
                    }

                    modelTblImpressas.addRow(obj);
                    modelTblEnviadas.removeRow(row);

                    EnviarParaImpressao(obj[1].toString());

                    JOptionPane.showMessageDialog(null, "Mandou para Impressão");
                }
            }
        });

        String[] colunas2 = new String[]{"Foto", "Nome", "Status"};
        modelTblImpressas = new MyDefaultTableModel(null, colunas2);

        tblFotosImpressas.setModel(modelTblImpressas);
        tblFotosImpressas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblFotosImpressas.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblFotosImpressas.getColumnModel().getColumn(2).setPreferredWidth(100);

        tblFotosImpressas.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    int row = tblFotosImpressas.getSelectedRow();
                    e.consume();

                    if (tblFotosImpressas.getValueAt(row, 2).toString().equals("NO TELÃO") == false) {
                        if (tblFotosImpressas.getValueAt(row, 2).toString().equals("ERRO") == true && btnStartPrint.isEnabled() == false) {
                            JOptionPane.showMessageDialog(null, "Pare o processo para realizar esta ação!");
                        } else {
                            int dialogResult = JOptionPane.showConfirmDialog(null, "Deseja realmente reimprimir esta foto?", "Atenção", JOptionPane.YES_NO_OPTION);
                            if (dialogResult == JOptionPane.YES_OPTION) {
                                EnviarParaImpressao(tblFotosImpressas.getValueAt(row, 1).toString());
                                tblFotosImpressas.setValueAt("NA FILA", row, 2);
                            }
                        }
                    }
                }
            }

        });

        tblEvento.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblEvento.isEnabled() == true) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();

                    FrmEvent frm = new FrmEvent(FrmPrincipal.this, true);
                    frm.id_event = target.getValueAt(row, 0).toString();
                    frm.setLocationRelativeTo(null);
                    frm.setVisible(true);
                }
            }

        });

        tblFotoEnviadas.getTableHeader().setVisible(false);
        tblFotosImpressas.getTableHeader().setVisible(false);

        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(null);
    }

    private void loadFotosTelao() {
        try {
            String outputDirectory = dirFotosTelao + hashtag + "/";

            File dirTelao = new File(outputDirectory);
            if (dirTelao.exists() == false) {
                dirTelao.mkdir();
            }

            File dir = new File(outputDirectory);
            if (dir.isDirectory()) {
                File arquivos[] = dir.listFiles();
                ImageIcon image;
                for (File arquivo : arquivos) {
                    if (arquivo.getName().indexOf(".jpg") > 0 || arquivo.getName().indexOf(".JPG") > 0) {
                        if (listaFotosEnviadas.contains(arquivo.getName()) == false) {
                            String pathFile = arquivo.getAbsolutePath();
                            image = new ImageIcon(pathFile);
                            Image scaledImage = image.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                            image.setImage(scaledImage);
                            Object[] obj = new Object[3];
                            obj[0] = image;
                            obj[1] = arquivo.getName();
                            obj[2] = "NO TELÃO";
                            listaFotosEnviadas.add(arquivo.getName());
                            modelTblImpressas.addRow(obj);
                        }
                    }
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Problema ao exibir foto no telão error: " + e.getMessage());
        }
    }

    private void loadPrinteds() {
        try {
            qtdeImpressas = 0;
            String outputDirectory = dirFotosImpressas + hashtag + "/";

            File dirImpressas = new File(outputDirectory);
            if (dirImpressas.exists() == false) {
                dirImpressas.mkdir();
            }

            File dir = new File(outputDirectory);
            if (dir.isDirectory()) {
                File arquivos[] = dir.listFiles();
                Arrays.sort(arquivos, new Comparator() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                            return +1;
                        } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                ImageIcon image;
                for (File arquivo : arquivos) {
                    if (arquivo.getName().indexOf(".jpg") > 0 || arquivo.getName().indexOf(".JPG") > 0) {
                        if (listaFotosEnviadas.contains(arquivo.getName()) == false) {
                            String pathFile = arquivo.getAbsolutePath();
                            String imagePath = pathFile.replaceAll("FotosImpressas", "FotosEnviadas");
                            image = new ImageIcon(imagePath);
                            Image scaledImage = image.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                            image.setImage(scaledImage);
                            Object[] obj = new Object[3];
                            obj[0] = image;
                            obj[1] = arquivo.getName();
                            obj[2] = "IMPRESSA";
                            qtdeImpressas++;
                            listaFotosEnviadas.add(arquivo.getName());
                            modelTblImpressas.addRow(obj);
                        }
                    }
                }
            }
            lblQtdeFotos.setText("Fotos Impressas: " + String.valueOf(qtdeImpressas) + " / " + totFotos);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Problema ao exibir fotos impressas. Error: " + e.getMessage());
        }

    }

    private void loadNaFila() {
        try {
            String outputDirectory = dirFotosFila + hashtag + "/";

            File dirImpressas = new File(outputDirectory);
            if (dirImpressas.exists() == false) {
                dirImpressas.mkdir();
            }

            File dir = new File(outputDirectory);
            if (dir.isDirectory()) {
                File arquivos[] = dir.listFiles();
                Arrays.sort(arquivos, new Comparator() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                            return +1;
                        } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                ImageIcon image;

                for (File arquivo : arquivos) {
                    if (arquivo.getName().indexOf(".jpg") > 0 || arquivo.getName().indexOf(".JPG") > 0) {
                        if (listaFotosEnviadas.contains(arquivo.getName()) == false) {
                            String pathFile = arquivo.getAbsolutePath();
                            String imagePath = pathFile.replaceAll("FotosFila", "FotosEnviadas");
                            image = new ImageIcon(imagePath);
                            Image scaledImage = image.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                            image.setImage(scaledImage);
                            Object[] obj = new Object[3];
                            obj[0] = image;
                            obj[1] = arquivo.getName();
                            obj[2] = "NA FILA";
                            listaFotosEnviadas.add(arquivo.getName());
                            modelTblImpressas.addRow(obj);
                        }
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Problema ao carregar fila de fotos. Error: " + e.getMessage());
        }
    }

    private void startPrint() {
        loadPrinteds();
        loadNaFila();
        loadFotosTelao();

        ProcessoThread thread = new ProcessoThread();
        thread.action = "importFotos";
        thread.start();

        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent e) {
                ProcessoThread thread = new ProcessoThread();
                thread.action = "importFotos";
                thread.start();

                loadFotosEnviadas();
            }
        };
        timer = new Timer(12000, action);//Importa fotos do instagram a cada 10 segundosx  
        timer.start();

        ActionListener action2 = new ActionListener() {
            @Override
            public void actionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent e) {

                (new Thread() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            if (tblFotoEnviadas.getRowCount() > 0) {
                                if (tblFotoEnviadas.getValueAt(0, 1).toString().length() > 0) {

                                    final Object[] obj = new Object[3];
                                    obj[0] = tblFotoEnviadas.getValueAt(0, 0);
                                    obj[1] = tblFotoEnviadas.getValueAt(0, 1).toString();
                                    if (temImpressao == true) {
                                        obj[2] = "NA FILA";
                                    } else {
                                        obj[2] = "NO TELÃO";
                                    }

                                    if (modelTblEnviadas.getRowCount() > 0) {
                                        modelTblEnviadas.removeRow(0);
                                    }

                                    if (btnStartEvento.getText().equals("Parar Evento")) {
                                        boolean existe = false;
                                        int tot_impressas = modelTblImpressas.getRowCount();
                                        for (int x = 0; x < tot_impressas; x++) {
                                            if (modelTblImpressas.getValueAt(x, 1).equals(obj[1].toString())) {
                                                existe = true;
                                            }
                                        }
                                        if (existe == false && hashtag != null && timerAutomatico.isRunning()) {
                                            EnviarParaImpressao(obj[1].toString());
                                            modelTblImpressas.addRow(obj);

                                        }
                                    } else {
                                        if (modelTblImpressas.getRowCount() > 0) {
                                            modelTblImpressas.removeRow(0);
                                        }
                                    }
                                }
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(FrmPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }).start();
            }
        };

        timerAutomatico = new Timer(10000, action2);
        if (automatico) {

            timerAutomatico.start();
        }

        ActionListener action3 = new ActionListener() {

            @Override
            public void actionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent e) {
            }
        };

        timerPrinter = new Timer(TIME_HANDLER, action3);
        timerPrinter.start();

        SLEEP = false;
        lastPrinterPrinted = 0;
    }

    private void stopPrint() {
        if (timer != null) {
            if (timer.isRunning()) {
                timer.stop();
            }
            if (automatico) {
                if (timerAutomatico.isRunning()) {
                    timerAutomatico.stop();
                }
            }
            timerPrinter.stop();
            if (timerSleep != null) {
                if (timerSleep.isRunning()) {
                    timerSleep.stop();
                }
            }
        }
        SLEEP = false;
        lastPrinterPrinted = 0;
    }

    private Photo getPhoto(String nameFile) {
        String[] info = nameFile.split("-");
        Webservice ws = new Webservice();
        return ws.getInfoPhoto(info[1].replace(".jpg", ""));
    }

    private void EnviarParaImpressao(String nameFile) {
        Photo photo = new Photo();

        try {
            File arquivo = new File(dirFotosEnviadas + hashtag + "/" + nameFile);
            if (temTelao) {
                String orig = dirFotosEnviadas + hashtag + "/" + nameFile;
                String dest = dirFotosTelao + hashtag + "/" + nameFile;
                InputStream in = new FileInputStream(orig);
                OutputStream out = new FileOutputStream(dest);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
            if (temImpressao) {
                photo = getPhoto(nameFile);
                photo.nome_evento = lblNomeEvento.getText();
                photo.image_evento = evento.getLogo_event();
                photo.createImageFromTemplate(arquivo, dirFotosFila + hashtag + "/", evento.getId_print_template());
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Problema ao Enviar para Impressão. Error: " + ex.getMessage());
        }
    }

    private void loadFotosEnviadas() {
        File dir = new File(dirFotosEnviadas + hashtag + "/");
        if (dir.isDirectory()) {
            File arquivos[] = dir.listFiles();
            Arrays.sort(arquivos, new Comparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                        return +1;
                    } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            for (File arquivo : arquivos) {
                if (arquivo.getName().indexOf(".jpg") > 0 || arquivo.getName().indexOf(".JPG") > 0) {
                    if (!arquivo.getName().contains("_baixando.jpg")) {
                        if (listaFotosEnviadas.contains(arquivo.getName()) == false) {
                            ImageIcon image;
                            try {
                                image = new ImageIcon(arquivo.getAbsolutePath());
                                Image scaledImage = image.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                                image.setImage(scaledImage);
                                Object[] obj = new Object[2];
                                obj[0] = image;
                                obj[1] = arquivo.getName();
                                boolean existe = false;
                                int tot_enviadas = modelTblEnviadas.getRowCount();

                                for (int x = 0; x < tot_enviadas; x++) {
                                    if (modelTblEnviadas.getValueAt(x, 1).equals(obj[1].toString())) {
                                        existe = true;
                                    }
                                }
                                if (existe == false) {
                                    modelTblEnviadas.addRow(obj);
                                    listaFotosEnviadas.add(arquivo.getName());
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Problema ao carregar fotos. Error: " + ex.getMessage());
                                Logger.getLogger(FrmPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }
    }

    public void loadEventos() {
        String[][] dados = evento.listEvents();
        String[] colunas = new String[]{"Código", "Nome", "Hashtag", "Data", "Impressão", "Telão", "Automatico", "Qtde Fotos"};
        DefaultTableModel model = new DefaultTableModel(dados, colunas) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblEvento.setModel(model);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblEvento = new javax.swing.JTable();
        btnAddEvento = new javax.swing.JButton();
        btnStartEvento = new javax.swing.JButton();
        pnlImpressoes = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblFotoEnviadas = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblFotosImpressas = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstImpressoras = new javax.swing.JList();
        btnStartPrint = new javax.swing.JButton();
        btnStopPrint = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lblNomeEvento = new javax.swing.JLabel();
        lblQtdeFotos = new javax.swing.JLabel();
        btnExibirTelao = new javax.swing.JButton();
        btnAtualizarPrints = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Post&Print");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 24)); // NOI18N
        jLabel1.setText("Seus Eventos");

        tblEvento.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblEvento.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(tblEvento);

        btnAddEvento.setText("Adicionar Evento");
        btnAddEvento.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnAddEvento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddEventoActionPerformed(evt);
            }
        });

        btnStartEvento.setText("Iniciar Evento");
        btnStartEvento.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnStartEvento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartEventoActionPerformed(evt);
            }
        });

        pnlImpressoes.setBorder(javax.swing.BorderFactory.createTitledBorder("Impressões"));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Fotos Enviadas"));

        tblFotoEnviadas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblFotoEnviadas.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tblFotoEnviadas.setRowHeight(80);
        tblFotoEnviadas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(tblFotoEnviadas);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 472, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Impressas / Telão"));

        tblFotosImpressas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblFotosImpressas.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tblFotosImpressas.setRowHeight(80);
        tblFotosImpressas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(tblFotosImpressas);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane4)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Selecione as Impressoras"));

        lstImpressoras.setEnabled(false);
        jScrollPane2.setViewportView(lstImpressoras);

        btnStartPrint.setBackground(new java.awt.Color(51, 204, 0));
        btnStartPrint.setText("Iniciar Processo");
        btnStartPrint.setBorderPainted(false);
        btnStartPrint.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnStartPrint.setEnabled(false);
        btnStartPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartPrintActionPerformed(evt);
            }
        });

        btnStopPrint.setBackground(new java.awt.Color(255, 0, 0));
        btnStopPrint.setForeground(new java.awt.Color(255, 255, 255));
        btnStopPrint.setText("Parar Processo");
        btnStopPrint.setBorderPainted(false);
        btnStopPrint.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnStopPrint.setEnabled(false);
        btnStopPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopPrintActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .add(btnStartPrint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, btnStopPrint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnStartPrint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(btnStopPrint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel2.setText("Evento:");

        lblNomeEvento.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        lblNomeEvento.setText("Nome do Evento");

        lblQtdeFotos.setText("Fotos Impressas:");

        org.jdesktop.layout.GroupLayout pnlImpressoesLayout = new org.jdesktop.layout.GroupLayout(pnlImpressoes);
        pnlImpressoes.setLayout(pnlImpressoesLayout);
        pnlImpressoesLayout.setHorizontalGroup(
            pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlImpressoesLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnlImpressoesLayout.createSequentialGroup()
                        .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(pnlImpressoesLayout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lblNomeEvento, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnlImpressoesLayout.createSequentialGroup()
                        .add(lblQtdeFotos)
                        .add(217, 217, 217))
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        pnlImpressoesLayout.setVerticalGroup(
            pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlImpressoesLayout.createSequentialGroup()
                .add(pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnlImpressoesLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(lblNomeEvento))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlImpressoesLayout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(lblQtdeFotos)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        btnExibirTelao.setText("Exibir Telão");
        btnExibirTelao.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnExibirTelao.setEnabled(false);
        btnExibirTelao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExibirTelaoActionPerformed(evt);
            }
        });

        btnAtualizarPrints.setText("Procurar Impressoras");
        btnAtualizarPrints.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnAtualizarPrints.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarPrintsActionPerformed(evt);
            }
        });

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/post_print.jpg"))); // NOI18N

        jLabel4.setText("Versão 2.0");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(pnlImpressoes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(btnAtualizarPrints, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(0, 0, Short.MAX_VALUE)
                                .add(btnExibirTelao, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(btnStartEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(btnAddEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jScrollPane1))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(btnAtualizarPrints, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 184, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnAddEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnStartEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnExibirTelao, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlImpressoes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddEventoActionPerformed
        // TODO add your handling code here:
        FrmEvent frm = new FrmEvent(this, true);
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);

    }//GEN-LAST:event_btnAddEventoActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // TODO add your handling code here:
        this.loadEventos();
        if (frame != null) {
            frame.toFront();
        }
    }//GEN-LAST:event_formWindowActivated

    private void btnStartEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartEventoActionPerformed
        // TODO add your handling code here:

        if (btnStartEvento.getText().equals("Iniciar Evento")) {
            if (preparaEvento()) {

            }
        } else if (btnStartEvento.getText().equals("Parar Evento")) {
            paraEvento();

        }
    }//GEN-LAST:event_btnStartEventoActionPerformed

    private boolean preparaEvento() {

        if (tblEvento.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(null, "Por favor, selecione um evento na tabela acima");
            return false;
        }

        int row = tblEvento.getSelectedRow();

        if (evento.loadEvent(tblEvento.getValueAt(row, 0).toString()) == false) {
            return false;
        }

        if (evento.getLogo_event().length() > 0) {
            if (!new File(evento.getLogo_event()).exists()) {
                JOptionPane.showMessageDialog(null, "ATENÇÃO! O Logo do Evento não foi encontrado, verifique [" + evento.getLogo_event() + "]");
                return false;
            }
        }

        if (tblEvento.getValueAt(row, 5).toString().equals("Sim")) {
            btnExibirTelao.setEnabled(true);
            temTelao = true;
        } else {
            temTelao = false;
        }

        if (tblEvento.getValueAt(row, 4).toString().equals("Sim")) {
            temImpressao = true;
        } else {
            temImpressao = false;
        }

        totFotos = Integer.parseInt(tblEvento.getValueAt(row, 7).toString());
        lblQtdeFotos.setText("Fotos Impressas: ? / " + tblEvento.getValueAt(row, 7).toString());

        lstImpressoras.setEnabled(true);
        btnStartPrint.setEnabled(true);
        btnStopPrint.setEnabled(false);

        btnStartEvento.setText("Parar Evento");
        btnStartEvento.setBackground(Color.RED);
        btnStartEvento.setForeground(Color.WHITE);
        btnStartEvento.setOpaque(true);
        btnStartEvento.setBorderPainted(false);

        lblNomeEvento.setText(tblEvento.getValueAt(row, 1).toString());
        hashtag = tblEvento.getValueAt(row, 2).toString();

        if (tblEvento.getValueAt(row, 6).toString().equals("Sim")) {
            automatico = true;
        } else {
            automatico = false;
        }

        File dirEnviadas = new File(dirFotosEnviadas + hashtag + "/");
        if (dirEnviadas.exists() == false) {
            dirEnviadas.mkdir();
        }

        File dirImpressas = new File(dirFotosImpressas + hashtag + "/");
        if (dirImpressas.exists() == false) {
            dirImpressas.mkdir();
        }

        File dirFila = new File(dirFotosFila + hashtag + "/");
        if (dirFila.exists() == false) {
            dirFila.mkdir();
        }

        if (temTelao) {
            File dirTelao = new File(dirFotosTelao + hashtag + "/");
            if (dirTelao.exists() == false) {
                dirTelao.mkdir();
            }
        }

        tblEvento.setEnabled(false);

        return true;
    }

    private boolean paraEvento() {
        btnExibirTelao.setEnabled(false);

        lstImpressoras.setEnabled(false);
        btnStartPrint.setEnabled(false);
        btnStopPrint.setEnabled(false);
        btnStartEvento.setText("Iniciar Evento");

        btnStartEvento.setBackground(new Color(238, 238, 238));
        btnStartEvento.setForeground(Color.BLACK);
        btnStartEvento.setOpaque(false);
        btnStartEvento.setBorderPainted(true);

        lblNomeEvento.setText("Nome do Evento");
        hashtag = null;

        tblEvento.setEnabled(true);

        qtdeImpressas = 0;
        lblQtdeFotos.setText("Fotos Impressas: ");

        stopPrint();

        automatico = false;

        listaFotosEnviadas.clear();
        if (modelTblEnviadas != null) {
            if (modelTblEnviadas.getRowCount() > 0) {
                for (int i = modelTblEnviadas.getRowCount() - 1; i > -1; i--) {
                    modelTblEnviadas.removeRow(i);
                }
            }
        }
        if (tblFotosImpressas != null) {
            if (modelTblImpressas.getRowCount() > 0) {
                for (int i = modelTblImpressas.getRowCount() - 1; i > -1; i--) {
                    modelTblImpressas.removeRow(i);
                }
            }
        }
        try {
            File dirFila = new File(dirFotosFila + hashtag + "/");
            dirFila.delete();
        } catch (Exception e) {
            System.out.println("Deleta Pasta Fila: " + e.getMessage());
        }

        if (frame != null) {
            frame.setVisible(false);
            if (frame.timerPhotos != null) {
                frame.timerPhotos.cancel();
                frame.timerPhotos = null;
            }
        }
        return true;
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void btnExibirTelaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExibirTelaoActionPerformed
        // TODO add your handling code here:
        try {
            setupFullScreen();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_btnExibirTelaoActionPerformed

    private void createFrameAtLocation(Point p, FrmTelao frame) {

        frame.setLocation(p);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        if (gd.isFullScreenSupported()) {
            System.out.println("Fullscreen permited");

            frame.setVisible(false);
            frame.dispose();
            frame.setUndecorated(true);
            frame.setIgnoreRepaint(true);
            frame.requestFocusInWindow();
            frame.setVisible(true);
            frame.setAlwaysOnTop(true);
            frame.initTimer();

        }

    }

    public void setupFullScreen() {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                Point p1 = null;
                Point p2 = null;
                for (GraphicsDevice gd2 : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    if (p1 == null) {
                        p1 = gd2.getDefaultConfiguration().getBounds().getLocation();
                    } else if (p2 == null) {
                        p2 = gd2.getDefaultConfiguration().getBounds().getLocation();
                        gd = gd2;
                    }
                }
                if (p2 == null) {
                    p2 = p1;
                }

                if (frame == null) {
                    frame = new FrmTelao();

                }

                frame.hashtag = hashtag;
                frame.dirFotosTelao = dirFotosTelao;
                frame.pathLogoEvento = evento.getLogo_event();

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

                InputMap im = frame.painel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
                ActionMap am = frame.painel.getActionMap();
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "getMeOutOfHere");
                am.put("getMeOutOfHere", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            gd.setFullScreenWindow(null);
                        } finally {
                            frame.setVisible(false);
                            if (frame.timerPhotos != null) {
                                frame.timerPhotos.cancel();
                                frame.timerPhotos = null;
                            }
                        }
                    }
                });

                createFrameAtLocation(p2, frame);

            }
        });
    }


    private void btnAtualizarPrintsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarPrintsActionPerformed

        Impressao impressora = new Impressao();
        print = impressora.detectaImpressoras();
        int i = 0;
        for (String p : print) {
            i = i + 1;
            modelListaImpressoras.addElement(p);
        }

    }//GEN-LAST:event_btnAtualizarPrintsActionPerformed

    private void btnStartPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartPrintActionPerformed
        totPrints = lstImpressoras.getSelectedIndices().length;
        printsSelecteds = new String[totPrints];

        if (totPrints == 0 && temImpressao == true) {
            JOptionPane.showMessageDialog(null, "Selecione a impressora do evento");
            return;
        }

        for (int i = 0; i < totPrints; i++) {
            printsSelecteds[i] = lstImpressoras.getSelectedValues()[i].toString();
            System.out.println(printsSelecteds[i]);
            String namePrinter[] = printsSelecteds[i].split("-");
        }

        threadFila = new Thread[totPrints];

        btnStartPrint.setEnabled(false);
        btnStopPrint.setEnabled(true);
        lstImpressoras.setEnabled(false);
        startPrint();

    }//GEN-LAST:event_btnStartPrintActionPerformed

    private void btnStopPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopPrintActionPerformed
        // TODO add your handling code here:

        btnStartPrint.setEnabled(true);
        btnStopPrint.setEnabled(false);
        lstImpressoras.setEnabled(true);
        stopPrint();
    }//GEN-LAST:event_btnStopPrintActionPerformed

    class ProcessoThread extends Thread {

        public String action;

        public void run() {
            if (action.equals("importFotos")) {
                Instagram.importPhotosInstagram(hashtag, dirFotosEnviadas);
            }
        }
    }

    public class FilaPrinter implements Runnable {

        public String fileName = "";
        public Impressao printer;

        public FilaPrinter(Impressao impressao, String image) {
            fileName = image;
            printer = impressao;
        }

        @Override
        public void run() {

            if (printer.imprime(fileName)) {
                qtdeImpressas++;
                if (qtdeImpressas == totFotos) {
                    JOptionPane.showMessageDialog(null, "ATENÇÃO! Quantidade de fotos impressas atingiu a quantidade de fotos total do Evento");
                }
                lblQtdeFotos.setText("Fotos Impressas: " + String.valueOf(qtdeImpressas) + " / " + totFotos);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new FrmPrincipal().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddEvento;
    private javax.swing.JButton btnAtualizarPrints;
    private javax.swing.JButton btnExibirTelao;
    private javax.swing.JButton btnStartEvento;
    private javax.swing.JButton btnStartPrint;
    private javax.swing.JButton btnStopPrint;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblNomeEvento;
    private javax.swing.JLabel lblQtdeFotos;
    private javax.swing.JList lstImpressoras;
    private javax.swing.JPanel pnlImpressoes;
    private javax.swing.JTable tblEvento;
    private javax.swing.JTable tblFotoEnviadas;
    private javax.swing.JTable tblFotosImpressas;
    // End of variables declaration//GEN-END:variables
}
