import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calendar {

    public static String[] meseci = { "Januar", "Februar", "Marec", "April", "Maj",
                                    "Junij", "Julij", "Avgust", "September", "Oktober", "November", "December"};
    public static ArrayList<Praznik> prazniki;
    public static HashMap<String,String> meseciMap;
    public static HashMap<String,String> meseciInverseMap;
    public static DefaultTableModel model;
    public static JButton buttonIsci;
    public static JTextField textFieldLeto;
    public static JComboBox comboBoxMeseci;
    public static JTextField textFieldDatum;
    public static JButton buttonIsciDatum;
    public static JTable table;
    public static JLabel errorLabel;
    public static JLabel errorLabel2;
    public static JLabel mesecInLetoLabel;
    public static int izbranMesec;
    public static String mesec = "";
    public static String leto = "";

    public static void main(String[] args) {
        setMeseciMap();
        setMeseciInverseMap();
        initJFrame();
        try {
            preberiPraznike();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(prazniki);
        initMonth(9,2021);

        buttonIsci.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vnosLeto = textFieldLeto.getText();
                if(jeLetnicaPrava(vnosLeto)) {
                    initMonth(Integer.parseInt(meseciMap.get(comboBoxMeseci.getSelectedItem())),Integer.parseInt(textFieldLeto.getText()));
                    errorLabel.setVisible(false);
                    errorLabel.setVisible(false);
                } else {
                    errorLabel.setVisible(true);
                    errorLabel2.setVisible(false);
                }            }
        });

        buttonIsciDatum.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String datum = textFieldDatum.getText();
                if(jeDatumPravi(datum)) {
                    String [] deliDatuma = datum.split("\\.");
                    int mesec = Integer.parseInt(deliDatuma[1]) - 1;
                    int leto = Integer.parseInt(deliDatuma[2]);
                    initMonth(mesec,leto);
                    errorLabel.setVisible(false);
                    errorLabel2.setVisible(false);
                }
                else {
                    errorLabel2.setVisible(true);
                    errorLabel.setVisible(false);
                }

            }
        });
    }

    public static void initJFrame(){
        JFrame frame = new JFrame("Koledar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(700,400));
        frame.setSize(700,400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel panelTekst = new JPanel();
        panelTekst.setLayout(new BorderLayout());

        JPanel isciMesecLeto = new JPanel();
        comboBoxMeseci = new JComboBox(meseci);
        isciMesecLeto.add(comboBoxMeseci);
        textFieldLeto = new JTextField(4);
        isciMesecLeto.add(textFieldLeto);
        buttonIsci = new JButton("Išči po letu in mesecu");
        isciMesecLeto.add(buttonIsci);
        panelTekst.add(isciMesecLeto, BorderLayout.PAGE_START);
        JPanel errorPanel = new JPanel();
        errorLabel = new JLabel("Napaka. Letnica mora biti oblike 1234");
        errorLabel.setVisible(false);
        errorPanel.add(errorLabel);
        panelTekst.add(errorPanel, BorderLayout.CENTER);
        panel.add(panelTekst, BorderLayout.LINE_START);

        JPanel panelTekst2 = new JPanel();
        panelTekst2.setLayout(new BorderLayout());

        JPanel isciDatum = new JPanel();
        textFieldDatum = new JTextField(8);
        isciDatum.add(textFieldDatum);
        buttonIsciDatum = new JButton("Išči po datumu");
        isciDatum.add(buttonIsciDatum);
        panelTekst2.add(isciDatum,BorderLayout.PAGE_START);

        JPanel mesecInLeto = new JPanel();
        mesecInLetoLabel = new JLabel(mesec + ", " + leto);
        mesecInLetoLabel.setVisible(false);
        mesecInLeto.add(mesecInLetoLabel);
        panel.add(mesecInLeto, BorderLayout.CENTER);

        JPanel errorPanel2 = new JPanel();
        errorLabel2 = new JLabel("Napaka. Datum mora biti oblike 01.01.2000");
        errorLabel2.setVisible(false);
        errorPanel2.add(errorLabel2);
        panelTekst2.add(errorLabel2, BorderLayout.CENTER);

        panel.add(panelTekst2, BorderLayout.LINE_END);

        String [] columns = {"Pon","Tor","Sre","Čet","Pet","Sob","Ned"};
        model = new DefaultTableModel(null,columns);
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
                                             int columnIndex) {
                JComponent component = (JComponent) super.prepareRenderer(renderer, rowIndex, columnIndex);

                if(getValueAt(rowIndex,columnIndex) != null){
                    int dan = Integer.parseInt(getValueAt(rowIndex,columnIndex).toString());
                    if(jePraznik(dan,izbranMesec + 1)){
                        component.setBackground(Color.ORANGE);
                    }
                    else if(columnIndex == 0) {
                        component.setBackground(Color.RED);
                    } else {
                        component.setBackground(Color.WHITE);
                    }
                } else {
                    component.setBackground(Color.WHITE);
                }

                return component;
            }
        };
        JScrollPane pane = new JScrollPane(table);

        frame.add(panel, BorderLayout.PAGE_START);
        frame.add(pane, BorderLayout.CENTER);

        JPanel legenda = new JPanel();
        JLabel nedeljaLegenda = new JLabel("Nedelja");
        nedeljaLegenda.setForeground(Color.RED);
        legenda.add(nedeljaLegenda);
        JLabel praznikLegenda = new JLabel("Praznik");
        praznikLegenda.setForeground(Color.ORANGE);
        legenda.add(praznikLegenda);

        frame.add(legenda, BorderLayout.PAGE_END);


        frame.setVisible(true);
    }

    public static void setMeseciMap() {
        meseciMap = new HashMap<>();
        meseciMap.put("Januar", "0");
        meseciMap.put("Februar", "1");
        meseciMap.put("Marec","2");
        meseciMap.put("April", "3");
        meseciMap.put("Maj", "4");
        meseciMap.put("Junij", "5");
        meseciMap.put("Julij", "6");
        meseciMap.put("Avgust", "7");
        meseciMap.put("September", "8");
        meseciMap.put("Oktober", "9");
        meseciMap.put("November", "10");
        meseciMap.put("December", "11");
    }

    public static void setMeseciInverseMap() {
        meseciInverseMap = new HashMap<>();
        meseciInverseMap.put("0", "Januar");
        meseciInverseMap.put("1","Februar");
        meseciInverseMap.put("2", "Marec");
        meseciInverseMap.put("3","April");
        meseciInverseMap.put("4","Maj");
        meseciInverseMap.put("5","Junij");
        meseciInverseMap.put("6","Julij");
        meseciInverseMap.put("7","Avgust");
        meseciInverseMap.put("8","September");
        meseciInverseMap.put("9","Oktober");
        meseciInverseMap.put("10","November");
        meseciInverseMap.put("11","December");

    }


    public static void initMonth(int month, int year){
        izbranMesec = month;
        mesec = meseciInverseMap.get(String.valueOf(month));
        leto = String.valueOf(year);
        mesecInLetoLabel.setText(mesec + ", " + leto);
        mesecInLetoLabel.setVisible(true);
        comboBoxMeseci.setSelectedItem(mesec);
        textFieldLeto.setText(leto);

        GregorianCalendar cal = new GregorianCalendar();
        cal.set(GregorianCalendar.MONTH, month);
        cal.set(GregorianCalendar.YEAR, year);
        cal.set(GregorianCalendar.DAY_OF_MONTH, 1);

        int startDay = cal.get(GregorianCalendar.DAY_OF_WEEK);
        int numberOfDays = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        int weeks = cal.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);

        model.setRowCount(0);
        if(startDay == 1)
            startDay = 8;
        model.setRowCount(weeks);

        int i = startDay-2;
        for(int day=1;day<=numberOfDays;day++){
            model.setValueAt(day, i/7 , i%7 );
            i = i + 1;
        }
    }

    public static void preberiPraznike() throws IOException {
        prazniki = new ArrayList<Praznik>();
        Path pot = Path.of("prazniki.txt");
        String prebrano = Files.readString(pot);
        String[] praznikiSplit = prebrano.split(",");
        for (String praznik : praznikiSplit) {
            String[] danInMesec = praznik.split("\\.");
            int dan = Integer.parseInt(danInMesec[0]);
            int mesec = Integer.parseInt(danInMesec[1]);
            prazniki.add(new Praznik(dan,mesec,false));
        }
    }

    public static boolean jePraznik(int dan, int mesec){
        for(Praznik p : prazniki){
            if(dan == p.getDan() && mesec == p.getMesec())
                return true;
        }
        return false;
    }

    public static boolean jeLetnicaPrava(String letnica){
        Pattern pattern = Pattern.compile("^[0-9]{4}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(letnica);
        if(matcher.find()){
            return true;
        } else {
            return false;
        }
    }

    public static boolean jeDatumPravi(String datum){
        Pattern pattern = Pattern.compile("^\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s*$",
                                            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(datum);
        if(matcher.find()){
            return true;
        } else {
            return false;
        }
    }


}


