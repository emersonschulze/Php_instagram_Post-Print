package photoparty;

import classes.Event;
import classes.Impressora;
import classes.Instagram;
import classes.MyDefaultTableModel;
import classes.Photo;
import classes.Webservice;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
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

    private Event evento;
    private Instagram insta;
    private String hashtag;

    private Timer timer;
    private Timer timerAutomatico;
    private Timer timerPrinter;

    private DefaultTableModel modelTblBaixadas;
    private MyDefaultTableModel modelTblImpressas;
    private MyDefaultTableModel modelTblTelao;
    private DefaultListModel modelListaImpressoras;

    private FrmTelao frame;

    private String dirFotosEnviadas = "FotosEnviadas/";
    private String dirFotosFila = "FotosFila/";
    private String dirFotosImpressas = "FotosImpressas/";
    private String dirFotosTelao = "FotosTelao/";

    private List<String> listaFotos;

    private String[] printsSelecteds;
    private int totPrints = 0;
    private int printCurrent = 0;
    private int lastPrinterPrinted = 0;
    private int current_image = 0;

    private boolean automatico = false;
    private boolean temTelao = false;
    private boolean temImpressao = false;
    private int qtdeBaixadas = 0;
    private int qtdeTelao = 0;
    private int qtdeUtilizadas = 0;
    private int qtdeImpressas = 0;
    private int totFotos = 0;

    private GraphicsDevice gd;

    private static final Logger LOG = Logger.getLogger(FrmPrincipal.class.getName());

    private Object lock = new Object();
    private CountDownLatch waitForDevices;

    private Thread[] threadFila;
    private int TIME_HANDLER = 3000;
    private Timer timerSleep;
    private boolean SLEEP = false;
    public FilaPrinter fila;

    /**
     * Creates new form FrmPrincipal
     */
    public FrmPrincipal() {
        initComponents();

        popularCombo();

        this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("Assets/logo_postprint.png")).getImage());

        evento = new Event();
        //insta = new Instagram();

        listaFotos = new ArrayList<>();
       
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

        String[] colunas = new String[]{"Foto", "Nome"};
        modelTblBaixadas = new DefaultTableModel(null, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class getColumnClass(int column) {
                if (column == 0) {
                    return ImageIcon.class;
                }
                return getValueAt(0, column).getClass();
            }
        };

        tableFotosBaixadas.setModel(modelTblBaixadas);
        tableFotosBaixadas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableFotosBaixadas.getColumnModel().getColumn(1).setPreferredWidth(350);

        tableFotosBaixadas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableFotosBaixadas.getSelectedRow();
                    e.consume();

                    Object[] obj = new Object[3];
                    obj[0] = tableFotosBaixadas.getValueAt(row, 0);
                    obj[1] = tableFotosBaixadas.getValueAt(row, 1).toString();
                    if (temImpressao) {
                        obj[2] = "IMPRIMINDO";
                        modelTblImpressas.addRow(obj);
                        sendToPrint(obj[1].toString());
                        qtdeImpressas++;
                        lbQtdImpressas.setText(String.valueOf(qtdeImpressas) + " / " + totFotos);
                    }
                    if (temTelao) {
                        obj[2] = "TELÃO";
                        modelTblTelao.addRow(obj);
                        qtdeTelao++;
                        lbQtdTelao.setText(String.valueOf(qtdeTelao));
                    }

                    modelTblBaixadas.removeRow(row);
                    qtdeUtilizadas++;
                    lbTotalUtilizadas.setText(String.valueOf(qtdeUtilizadas));
                }
            }
        });

        String[] colunas3 = new String[]{"Foto", "Nome"};
        modelTblTelao = new MyDefaultTableModel(null, colunas3);
        tabelaFotosTelao.setModel(modelTblTelao);
        tabelaFotosTelao.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabelaFotosTelao.getColumnModel().getColumn(1).setPreferredWidth(300);

        String[] colunas2 = new String[]{"Foto", "Nome", "Status"};
        modelTblImpressas = new MyDefaultTableModel(null, colunas2);
        tabelaFotosImpressas.setModel(modelTblImpressas);
        tabelaFotosImpressas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabelaFotosImpressas.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabelaFotosImpressas.getColumnModel().getColumn(2).setPreferredWidth(100);

        tabelaFotosImpressas.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabelaFotosImpressas.getSelectedRow();
                    e.consume();
                    int dialogResult = JOptionPane.showConfirmDialog(null, "Deseja realmente reimprimir esta foto?", "Atenção", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        sendToPrint(tabelaFotosImpressas.getValueAt(row, 1).toString());
                        tabelaFotosImpressas.setValueAt("IMPRIMINDO", row, 2);
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

        tableFotosBaixadas.getTableHeader().setVisible(false);
        tabelaFotosImpressas.getTableHeader().setVisible(false);
        tabelaFotosTelao.getTableHeader().setVisible(false);

        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(null);
    }

    private void loadFotosTelao() {
        try {
            String outputDirectory = dirFotosTelao + hashtag + "/";

            File dir = new File(outputDirectory);
            if (dir.isDirectory()) {
                File arquivos[] = dir.listFiles();
                ImageIcon image;
                for (File arquivo : arquivos) {
                    if (arquivo.getName().indexOf(".jpg") > 0 || arquivo.getName().indexOf(".JPG") > 0) {
                        if (listaFotos.contains(arquivo.getName()) == false) {
                            String pathFile = arquivo.getAbsolutePath();
                            image = new ImageIcon(pathFile);
                            Image scaledImage = image.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                            image.setImage(scaledImage);
                            Object[] obj = new Object[3];
                            obj[0] = image;
                            obj[1] = arquivo.getName();
                            qtdeTelao++;
                            listaFotos.add(arquivo.getName());
                            modelTblTelao.addRow(obj);
                            lbQtdTelao.setText(String.valueOf(qtdeTelao));
                        }
                    }
                }
            }

        } catch (Exception e) {
           
        }
    }

    private void loadFotosImpressas() {
        try {
            String outputDirectory = dirFotosImpressas + hashtag + "/";

            File dir = new File(outputDirectory);
            if (dir.isDirectory()) {
                File arquivos[] = dir.listFiles();
                Arrays.sort(arquivos, (Object o1, Object o2) -> {
                    if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                        return +1;
                    } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                ImageIcon image;
                for (File arquivo : arquivos) {
                    if (arquivo.getName().indexOf(".jpg") > 0 || arquivo.getName().indexOf(".JPG") > 0) {
                        if (listaFotos.contains(arquivo.getName()) == false) {
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
                            lbQtdImpressas.setText(String.valueOf(qtdeImpressas));
                            listaFotos.add(arquivo.getName());
                            modelTblImpressas.addRow(obj);
                            modelTblBaixadas.removeRow(Arrays.hashCode(obj));
                            lbQtdImpressas.setText(String.valueOf(qtdeImpressas) + " / " + totFotos);
                        }
                    }
                }
            }

            lbQtdImpressas.setText(String.valueOf(qtdeImpressas) + " / " + totFotos);
        } catch (Exception e) {
           
        }
    }

    private void loadFotosAImprimir() {
        try {
            String outputDirectory = dirFotosFila + hashtag + "/";
           
            File dir = new File(outputDirectory);
            if (dir.isDirectory()) {
                File arquivos[] = dir.listFiles();
                Arrays.sort(arquivos, (Object o1, Object o2) -> {
                    if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                        return +1;
                    } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                ImageIcon image;
                for (File arquivo : arquivos) {
                    if (arquivo.getName().indexOf(".jpg") > 0 || arquivo.getName().indexOf(".JPG") > 0) {
                        if (listaFotos.contains(arquivo.getName()) == false) {
                            String pathFile = arquivo.getAbsolutePath();
                            String imagePath = pathFile.replaceAll("FotosFila", "FotosEnviadas");
                            image = new ImageIcon(imagePath);
                            Image scaledImage = image.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                            image.setImage(scaledImage);
                            Object[] obj = new Object[3];
                            obj[0] = image;
                            obj[1] = arquivo.getName();
                            obj[2] = "IMPRIMIR";
                            qtdeImpressas++;
                            listaFotos.add(arquivo.getName());
                            modelTblImpressas.addRow(obj);
                            lbQtdImpressas.setText(String.valueOf(qtdeImpressas) + " / " + totFotos);
                        }
                    }
                }
            }

            lbQtdImpressas.setText(String.valueOf(qtdeImpressas)+" / "+totFotos);
        } catch (Exception e) {
        }
    }

    private void startPrint() {

        buscaFotosInstagram();

        ActionListener action2 = (@SuppressWarnings("unused") java.awt.event.ActionEvent e) -> {
            //System.out.println("Entrou no timer");
            
            (new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        if (tableFotosBaixadas.getRowCount() > 0) {
                            if (tableFotosBaixadas.getValueAt(0, 1).toString().length() > 0) {
                                //System.out.println("Mandou para impressora");
                                
                                final Object[] obj = new Object[3];
                                obj[0] = tableFotosBaixadas.getValueAt(0, 0);
                                obj[1] = tableFotosBaixadas.getValueAt(0, 1).toString();
                                if (temImpressao == true) {
                                    obj[2] = "NA FILA";
                                } else {
                                    obj[2] = "NO TELÃO";
                                }
                                
                                if (modelTblBaixadas.getRowCount() > 0) {
                                    modelTblBaixadas.removeRow(0);
                                }
                                //Thread.sleep(1000);
                                if (btnStartStopEvento.getText().equals("Parar Evento")) {
                                    boolean existe = false;
                                    int tot_impressas = modelTblImpressas.getRowCount();
                                    for (int x = 0; x < tot_impressas; x++) {
                                        if (modelTblImpressas.getValueAt(x, 1).equals(obj[1].toString())) {
                                            existe = true;
                                        }
                                    }
                                    if (existe == false && hashtag != null && timerAutomatico.isRunning()) {
                                        sendToPrint(obj[1].toString());
                                        modelTblImpressas.addRow(obj);
                                        //System.out.println("Adicionou a lista de Impressão: "+obj[1].toString());
                                    }
                                } else {
                                    
                                }
                                if (modelTblImpressas.getRowCount() > 0) {
                                    modelTblImpressas.removeRow(0);
                                }
                            }
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FrmPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
        };

        timerAutomatico = new Timer(10000, action2);
        if (automatico) {
            //System.out.println("Mandou para impressora");
            timerAutomatico.start();
        }

        ActionListener action3 = (@SuppressWarnings("unused") java.awt.event.ActionEvent e) -> {
        };
        timerPrinter = new Timer(TIME_HANDLER, action3);
        timerPrinter.start();

        SLEEP = false;
        lastPrinterPrinted = 0;
    }

    private void buscaFotosInstagram() {
        ProcessoThread thread = new ProcessoThread();
        thread.action = "importFotos";
        thread.start();
        
        ActionListener action;
        action = (@SuppressWarnings("unused") java.awt.event.ActionEvent e) -> {
            loadFotosBaixadas();
        };
        
        timer = new Timer(12000, action);//Importa fotos do instagram a cada 10 segundosx
        timer.start();
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

    private void sendToPrint(String nameFile) {
        Impressora printer = new Impressora();
        Photo photo;

        try {
            //System.out.println("NOME DO ARQUIVO: "+nameFile);
            File arquivo = new File(dirFotosEnviadas + hashtag + "/" + nameFile);
            if (temTelao) {
                String orig = dirFotosEnviadas + hashtag + "/" + nameFile;
                String dest = dirFotosTelao + hashtag + "/" + nameFile;
                OutputStream out;
                try (InputStream in = new FileInputStream(orig)) {
                    out = new FileOutputStream(dest);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                out.close();
            }
            if (temImpressao) {
                photo = getPhoto(nameFile);
                photo.nome_evento = lblNomeEvento.getText();
                photo.image_evento = evento.getLogo_event();
                photo.createImageFromTemplate(arquivo, dirFotosFila + hashtag + "/", evento.getId_print_template());

                printer.selecionaImpressoras(comboImpressoras.getSelectedItem().toString());
                printer.imprime(arquivo.getPath());
            }
        } catch (IOException ex) {
        }

    }

    private void loadFotosBaixadas() {
        File dir = new File(dirFotosEnviadas + hashtag + "/");
        if (dir.isDirectory()) {
            File arquivos[] = dir.listFiles();
            Arrays.sort(arquivos, (Object o1, Object o2) -> {
                if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                    return +1;
                } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                    return -1;
                } else {
                    return 0;
                }
            });

            for (File arquivo : arquivos) {
                if (arquivo.getName().indexOf(".jpg") > 0 || arquivo.getName().indexOf(".JPG") > 0) {
                    if (!arquivo.getName().contains("_baixando.jpg")) {
                        if (listaFotos.contains(arquivo.getName()) == false) {
                            ImageIcon image;
                            try {
                                image = new ImageIcon(arquivo.getAbsolutePath());
                                Image scaledImage = image.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                                image.setImage(scaledImage);
                                Object[] obj = new Object[2];
                                obj[0] = image;
                                obj[1] = arquivo.getName();
                                boolean existe = false;
                                int tot_enviadas = modelTblBaixadas.getRowCount();
                                for (int x = 0; x < tot_enviadas; x++) {
                                    if (modelTblBaixadas.getValueAt(x, 1).equals(obj[1].toString())) {
                                        existe = true;
                                    }
                                }
                                if (existe == false) {
                                    qtdeBaixadas++;
                                    modelTblBaixadas.addRow(obj);
                                    listaFotos.add(arquivo.getName());
                                    lbQtdBaixadas.setText(String.valueOf(qtdeBaixadas));
                                }
                            }catch (Exception ex) {
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
        btnStartStopEvento = new javax.swing.JButton();
        pnlImpressoes = new javax.swing.JPanel();
        labelBaixadas = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableFotosBaixadas = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        lbQtdBaixadas = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lbTotalUtilizadas = new javax.swing.JLabel();
        labelImpresa = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabelaFotosImpressas = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        lbQtdImpressas = new javax.swing.JLabel();
        labelTelao = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        lbQtdTelao = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tabelaFotosTelao = new javax.swing.JTable();
        btnExibirTelao = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        comboImpressoras = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        lblNomeEvento = new javax.swing.JLabel();

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

        btnStartStopEvento.setText("Iniciar Evento");
        btnStartStopEvento.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnStartStopEvento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartStopEventoActionPerformed(evt);
            }
        });

        pnlImpressoes.setBorder(javax.swing.BorderFactory.createTitledBorder("Impressões"));

        labelBaixadas.setBorder(javax.swing.BorderFactory.createTitledBorder("Fotos Instagram"));

        tableFotosBaixadas.setModel(new javax.swing.table.DefaultTableModel(
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
        tableFotosBaixadas.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableFotosBaixadas.setRowHeight(80);
        tableFotosBaixadas.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane3.setViewportView(tableFotosBaixadas);

        jLabel5.setText("Total de fotos baixadas:");

        lbQtdBaixadas.setText("0");

        jLabel8.setText("Utilizadas:");

        lbTotalUtilizadas.setText("0");

        org.jdesktop.layout.GroupLayout labelBaixadasLayout = new org.jdesktop.layout.GroupLayout(labelBaixadas);
        labelBaixadas.setLayout(labelBaixadasLayout);
        labelBaixadasLayout.setHorizontalGroup(
            labelBaixadasLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 408, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(labelBaixadasLayout.createSequentialGroup()
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lbQtdBaixadas, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel8)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lbTotalUtilizadas, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        labelBaixadasLayout.setVerticalGroup(
            labelBaixadasLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(labelBaixadasLayout.createSequentialGroup()
                .add(labelBaixadasLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(lbQtdBaixadas)
                    .add(jLabel8)
                    .add(lbTotalUtilizadas))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 281, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        labelImpresa.setBorder(javax.swing.BorderFactory.createTitledBorder("Fotos Impressas"));

        tabelaFotosImpressas.setModel(new javax.swing.table.DefaultTableModel(
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
        tabelaFotosImpressas.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tabelaFotosImpressas.setRowHeight(80);
        tabelaFotosImpressas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(tabelaFotosImpressas);

        jLabel6.setText("Total de fotos impressas:");

        lbQtdImpressas.setText("0");

        org.jdesktop.layout.GroupLayout labelImpresaLayout = new org.jdesktop.layout.GroupLayout(labelImpresa);
        labelImpresa.setLayout(labelImpresaLayout);
        labelImpresaLayout.setHorizontalGroup(
            labelImpresaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 418, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(labelImpresaLayout.createSequentialGroup()
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lbQtdImpressas, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        labelImpresaLayout.setVerticalGroup(
            labelImpresaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(labelImpresaLayout.createSequentialGroup()
                .add(labelImpresaLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbQtdImpressas))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 282, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4))
        );

        labelTelao.setBorder(javax.swing.BorderFactory.createTitledBorder("Fotos Telão"));
        labelTelao.setPreferredSize(new java.awt.Dimension(430, 330));

        jLabel7.setText("Total de fotos no telão:");

        lbQtdTelao.setText("0");

        tabelaFotosTelao.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelaFotosTelao.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tabelaFotosTelao.setRowHeight(80);
        tabelaFotosTelao.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane6.setViewportView(tabelaFotosTelao);
        tabelaFotosTelao.getAccessibleContext().setAccessibleParent(pnlImpressoes);

        org.jdesktop.layout.GroupLayout labelTelaoLayout = new org.jdesktop.layout.GroupLayout(labelTelao);
        labelTelao.setLayout(labelTelaoLayout);
        labelTelaoLayout.setHorizontalGroup(
            labelTelaoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(labelTelaoLayout.createSequentialGroup()
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lbQtdTelao, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
        );
        labelTelaoLayout.setVerticalGroup(
            labelTelaoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(labelTelaoLayout.createSequentialGroup()
                .add(labelTelaoLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbQtdTelao))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout pnlImpressoesLayout = new org.jdesktop.layout.GroupLayout(pnlImpressoes);
        pnlImpressoes.setLayout(pnlImpressoesLayout);
        pnlImpressoesLayout.setHorizontalGroup(
            pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlImpressoesLayout.createSequentialGroup()
                .add(4, 4, 4)
                .add(labelBaixadas, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(labelImpresa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelTelao, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        pnlImpressoesLayout.setVerticalGroup(
            pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlImpressoesLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(labelTelao, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pnlImpressoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(labelBaixadas, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 330, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(labelImpresa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(3, 3, 3))
        );

        labelTelao.getAccessibleContext().setAccessibleName("Fotos Telao");

        btnExibirTelao.setText("Exibir Telão");
        btnExibirTelao.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnExibirTelao.setEnabled(false);
        btnExibirTelao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExibirTelaoActionPerformed(evt);
            }
        });

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Assets/post_print.jpg"))); // NOI18N

        jLabel4.setText("Versão 3.0");

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Selecionar Impressora"));
        jPanel3.setLayout(new java.awt.GridLayout(1, 0));

        jPanel3.add(comboImpressoras);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel2.setText("Evento:");

        lblNomeEvento.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        lblNomeEvento.setText("Nome do Evento");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(jLabel2)
                                .add(9, 9, 9)
                                .add(lblNomeEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 624, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(btnExibirTelao, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 122, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnStartStopEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnAddEvento))
                            .add(pnlImpressoes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(0, 0, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel4)
                            .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(jScrollPane1))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(jLabel1)
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(6, 6, 6)
                        .add(jLabel4)
                        .add(9, 9, 9)
                        .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 184, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(37, 37, 37)
                        .add(jLabel2))
                    .add(layout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(btnAddEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(btnStartStopEvento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(btnExibirTelao, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(60, 60, 60)
                        .add(pnlImpressoes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(37, 37, 37)
                        .add(lblNomeEvento))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddEventoActionPerformed
        
        FrmEvent frm = new FrmEvent(this, true);
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);

    }//GEN-LAST:event_btnAddEventoActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
       
        this.loadEventos();
        if (frame != null) {
            frame.toFront();
        }
    }//GEN-LAST:event_formWindowActivated

    private void btnStartStopEventoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartStopEventoActionPerformed
        if (btnStartStopEvento.getText().equals("Iniciar Evento")) {
            if (preparaEvento()) {
                buscaFotosInstagram();
                loadFotosBaixadas();
                loadFotosAImprimir();
                loadFotosImpressas();
                loadFotosTelao();
            }
        } else if (btnStartStopEvento.getText().equals("Parar Evento")) {
            paraEvento();
        }
    }//GEN-LAST:event_btnStartStopEventoActionPerformed

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
        lbQtdImpressas.setText(tblEvento.getValueAt(row, 7).toString());

        btnStartStopEvento.setText("Parar Evento");
        btnStartStopEvento.setBackground(Color.RED);
        btnStartStopEvento.setForeground(Color.WHITE);
        btnStartStopEvento.setOpaque(true);
        btnStartStopEvento.setBorderPainted(false);

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
        btnStartStopEvento.setText("Iniciar Evento");

        btnStartStopEvento.setBackground(new Color(238, 238, 238));
        btnStartStopEvento.setForeground(Color.BLACK);
        btnStartStopEvento.setOpaque(false);
        btnStartStopEvento.setBorderPainted(true);

        lblNomeEvento.setText("Nome do Evento");
        hashtag = null;
        automatico = false;
        tblEvento.setEnabled(true);
        qtdeImpressas = 0;
        qtdeBaixadas = 0;
        qtdeTelao = 0;
        lbQtdBaixadas.setText("0");
        lbQtdTelao.setText("0");
        lbQtdImpressas.setText("0");
        lbTotalUtilizadas.setText("0");
        limpaTabelas();
        stopPrint();
       
         
        if (frame != null) {
            frame.setVisible(false);
            if (frame.timerPhotos != null) {
                frame.timerPhotos.cancel();
                frame.timerPhotos = null;
            }
        }
        return true;
    }

    private void limpaTabelas() {
       
        if (modelTblBaixadas != null) {
            if (modelTblBaixadas.getRowCount() > 0) {
                for (int i = modelTblBaixadas.getRowCount() - 1; i > -1; i--) {
                    modelTblBaixadas.removeRow(i);
                }
            }
        }
        if (modelTblImpressas != null) {
            if (modelTblImpressas.getRowCount() > 0) {
                for (int i = modelTblImpressas.getRowCount() - 1; i > -1; i--) {
                    modelTblImpressas.removeRow(i);
                }
            }
        }
        if (modelTblTelao != null) {
            if (modelTblTelao.getRowCount() > 0) {
                for (int i = modelTblTelao.getRowCount() - 1; i > -1; i--) {
                    modelTblTelao.removeRow(i);
                }
            }
        }
       
        listaFotos.clear();
        tabelaFotosImpressas.removeAll();
        tabelaFotosTelao.removeAll();
        tableFotosBaixadas.removeAll();
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void btnExibirTelaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExibirTelaoActionPerformed
       
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

        EventQueue.invokeLater(() -> {
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
        });
    }

    private void popularCombo() throws HeadlessException {
        Impressora printer = new Impressora();
        final List<String> listImpressoras = printer.detectaImpressoras();

        if (listImpressoras.isEmpty()) {
            JOptionPane.showMessageDialog(null, "ATENÇÃO! Não foi encontrado uma impressora instalada ");
        }

        listImpressoras.forEach((impressora) -> {
            comboImpressoras.addItem(impressora);
        });
    }

    class ProcessoThread extends Thread {

        public String action;

        @Override
        public void run() {
            if (action.equals("importFotos")) {
                Instagram.importPhotosInstagram(hashtag, dirFotosEnviadas);
            }
        }
    }

    public class FilaPrinter implements Runnable {

        public String fileName = "";
        public Impressora printer;
        private MyDefaultTableModel table;

        public FilaPrinter(Impressora impressao, String image) {
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
                lbQtdImpressas.setText(String.valueOf(qtdeImpressas) + " / " + totFotos);
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new FrmPrincipal().setVisible(true);
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddEvento;
    private javax.swing.JButton btnExibirTelao;
    private javax.swing.JButton btnStartStopEvento;
    private javax.swing.JComboBox<String> comboImpressoras;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JPanel labelBaixadas;
    private javax.swing.JPanel labelImpresa;
    private javax.swing.JPanel labelTelao;
    private javax.swing.JLabel lbQtdBaixadas;
    private javax.swing.JLabel lbQtdImpressas;
    private javax.swing.JLabel lbQtdTelao;
    private javax.swing.JLabel lbTotalUtilizadas;
    private javax.swing.JLabel lblNomeEvento;
    private javax.swing.JPanel pnlImpressoes;
    private javax.swing.JTable tabelaFotosImpressas;
    private javax.swing.JTable tabelaFotosTelao;
    private javax.swing.JTable tableFotosBaixadas;
    private javax.swing.JTable tblEvento;
    // End of variables declaration//GEN-END:variables
}
