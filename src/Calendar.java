import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calendar {

    public static final String[] meseci = { "Januar", "Februar", "Marec", "April", "Maj",
                                    "Junij", "Julij", "Avgust", "September", "Oktober", "November", "December"};
    public static final String[] dnevi = {"Pon","Tor","Sre","Čet","Pet","Sob","Ned"};
    public static ArrayList<Praznik> prazniki;
    public static HashMap<String,String> meseciMap;
    public static HashMap<String,String> meseciInverseMap;
    public static DefaultTableModel tabelaModel;
    public static JTable tabela;
    public static JButton buttonIsci;
    public static JTextField textFieldLeto;
    public static JComboBox comboBoxMeseci;
    public static JTextField textFieldDatum;
    public static JButton buttonIsciDatum;
    public static JLabel isciMesecLetoErrorLabel;
    public static JLabel isciDatumErrorLabel;
    public static JLabel mesecInLetoLabel;
    public static int izbranMesec;
    public static int izbranoLeto;
    public static String mesec = "";
    public static String leto = "";

    public static void main(String[] args) {
        mapiranjeMeseci();
        postaviGUI();
        try {
            preberiPraznike();
        } catch (IOException e) {
            System.out.println("Napaka pri branju datoteke s prazniki");
        }
        pridobiTrenutniMesecInLeto();
        izpisiIzbraniMesec(izbranMesec,izbranoLeto);
        nastaviActionListenerjeZaGumbe();
    }

    public static void postaviGUI() {
        // Postavitev glavnega okvirja, nastavljene dimenzije, pozicija na ekranu in layout
        JFrame frame = new JFrame("Koledar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800,400));
        frame.setSize(700,400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        //panel, ki ima pod sabo ostale panele
        JPanel panelGlavni = new JPanel();
        panelGlavni.setLayout(new BorderLayout());

        //panel za zgornji levi kot (combobox, input in gumb)
        JPanel panelZgorajLevo = new JPanel();
        panelZgorajLevo.setLayout(new BorderLayout());

        //dodana polja na panel
        JPanel isciMesecLeto = new JPanel();
        comboBoxMeseci = new JComboBox(meseci);
        isciMesecLeto.add(comboBoxMeseci);
        textFieldLeto = new JTextField(4);
        isciMesecLeto.add(textFieldLeto);
        buttonIsci = new JButton("Išči po mesecu in letu");
        isciMesecLeto.add(buttonIsci);
        panelZgorajLevo.add(isciMesecLeto, BorderLayout.PAGE_START);

        //Panel za napake pri iskanju po mesecu in letu
        JPanel isciMesecLetoErrorPanel = new JPanel();
        isciMesecLetoErrorLabel = new JLabel("Napaka pri obliki letnice (primer 2000)");
        isciMesecLetoErrorLabel.setVisible(false);
        isciMesecLetoErrorPanel.add(isciMesecLetoErrorLabel);
        panelZgorajLevo.add(isciMesecLetoErrorPanel, BorderLayout.CENTER);

        panelGlavni.add(panelZgorajLevo, BorderLayout.LINE_START);

        //panel za zgornji desni kot
        JPanel panelZgorajDesno = new JPanel();
        panelZgorajDesno.setLayout(new BorderLayout());

        //dodana polja na panel
        JPanel isciDatum = new JPanel();
        textFieldDatum = new JTextField(8);
        isciDatum.add(textFieldDatum);
        buttonIsciDatum = new JButton("Išči po datumu");
        isciDatum.add(buttonIsciDatum);
        panelZgorajDesno.add(isciDatum,BorderLayout.PAGE_START);

        //panel za napake pri iskanju po datumu
        JPanel isciDatumErrorPanel = new JPanel();
        isciDatumErrorLabel = new JLabel("Napaka pri datumu ali obliki (primer: 01.01.2000)");
        isciDatumErrorLabel.setVisible(false);
        isciDatumErrorPanel.add(isciDatumErrorLabel);
        panelZgorajDesno.add(isciDatumErrorLabel, BorderLayout.CENTER);

        panelGlavni.add(panelZgorajDesno, BorderLayout.LINE_END);

        //panel za izpis trenutno izbranega meseca in leta
        JPanel mesecInLeto = new JPanel();
        mesecInLetoLabel = new JLabel(mesec + ", " + leto);
        mesecInLetoLabel.setVisible(false);
        mesecInLeto.add(mesecInLetoLabel);
        panelGlavni.add(mesecInLeto, BorderLayout.CENTER);

        //priprava glavne tabele
        tabelaModel = new DefaultTableModel(null,dnevi);
        tabela = new JTable(tabelaModel) {
            //override metode prepareRenderer, da lahko nedelje in praznike obravamo z drugo barvo
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
                                             int columnIndex) {
                JComponent component = (JComponent) super.prepareRenderer(renderer, rowIndex, columnIndex);

                //preverjamo polja, kjer imamo označen dan in znotraj določimo al je ta dan praznik/nedelja ali pa pustimo polje belo
                if (getValueAt(rowIndex,columnIndex) != null) {
                    int dan = Integer.parseInt(getValueAt(rowIndex,columnIndex).toString());
                    if (jePraznik(dan,izbranMesec + 1, izbranoLeto)) {
                        component.setBackground(Color.ORANGE);
                    }
                    else if (columnIndex == 6) {
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
        JScrollPane pane = new JScrollPane(tabela);

        frame.add(panelGlavni, BorderLayout.PAGE_START);
        frame.add(pane, BorderLayout.CENTER);

        //panel za legendo spodaj
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

    /**
     *  Dodana action listenerja za oba gumba
     */
    public static void nastaviActionListenerjeZaGumbe() {
        //listener za gumb, ki išče po mesecu in letu - preverjamo tudi, če je vnos pravi
        // če ni pravi se izpiše napake, če je, zbriše vse napake iz panela
        buttonIsci.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String vnosLeto = textFieldLeto.getText();
                String vnosMesec = meseciMap.get(comboBoxMeseci.getSelectedItem());
                if (jeLetnicaPrava(vnosLeto)) {
                    izpisiIzbraniMesec(Integer.parseInt(vnosMesec),Integer.parseInt(vnosLeto));
                    isciMesecLetoErrorLabel.setVisible(false);
                } else {
                    isciMesecLetoErrorLabel.setVisible(true);
                }
                isciDatumErrorLabel.setVisible(false);
            }
        });

        //listener za gumb, ki išče po datumu - preverjamo tudi, če je vnos pravi
        // če ni pravi se izpiše napake, če je, zbriše vse napake iz panela
        buttonIsciDatum.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String datum = textFieldDatum.getText();
                if (jeDatumPravi(datum)) {
                    String [] deliDatuma = datum.split("\\.");
                    int dan = Integer.parseInt(deliDatuma[0]);
                    int mesec = Integer.parseInt(deliDatuma[1]) - 1;
                    int leto = Integer.parseInt(deliDatuma[2]);
                    //če je vnesen 29 feb in leto ni prestopno, izpišemo napako
                    if (dan == 29 && mesec == 1 && leto % 4 != 0) {
                        isciDatumErrorLabel.setVisible(true);
                    } else {
                        izpisiIzbraniMesec(mesec,leto);
                        isciDatumErrorLabel.setVisible(false);
                    }
                    isciMesecLetoErrorLabel.setVisible(false);
                }
                else {
                    isciDatumErrorLabel.setVisible(true);
                    isciMesecLetoErrorLabel.setVisible(false);
                }

            }
        });
    }

    /**
     * Inicializacija mapov za mapiranje med stevilko in imenom meseca
     */
    public static void mapiranjeMeseci(){
        setMeseciMap();
        setMeseciInverseMap();
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

    /**
     * Pridobimo trenutni mesec in let, da se prikaže pred izbiro
     */
    public static void pridobiTrenutniMesecInLeto(){
        GregorianCalendar koledar = new GregorianCalendar();
        izbranMesec = koledar.get(GregorianCalendar.MONTH);
        izbranoLeto = koledar.get(GregorianCalendar.YEAR);
    }

    /**
     * Metoda za izpis mesec in leta, ki smo ga izbrali.
     *
     * @param mesecIzbran
     * @param letoIzbrano
     */
    public static void izpisiIzbraniMesec(int mesecIzbran, int letoIzbrano){
        //izpisemo kateri mesec in leto sta prikazana, nastavimo na te vrednosti tudi combo box in input
        izbranMesec = mesecIzbran;
        izbranoLeto = letoIzbrano;
        mesec = meseciInverseMap.get(String.valueOf(mesecIzbran));
        leto = String.valueOf(letoIzbrano);
        mesecInLetoLabel.setText(mesec + ", " + leto);
        mesecInLetoLabel.setVisible(true);
        comboBoxMeseci.setSelectedItem(mesec);
        textFieldLeto.setText(leto);

        //natavimo koledar na izbran mesec in leto
        GregorianCalendar koledar = new GregorianCalendar();
        koledar.set(GregorianCalendar.MONTH, mesecIzbran);
        koledar.set(GregorianCalendar.YEAR, letoIzbrano);
        koledar.set(GregorianCalendar.DAY_OF_MONTH, 1);

        //pridobimo s katerim dnem se mesec začne, koliko dni in tednov ima
        int zacetniDan = koledar.get(GregorianCalendar.DAY_OF_WEEK);
        int steviloDniMesec = koledar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        int steviloTednov = koledar.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);

        //resetiramo tabelo
        tabelaModel.setRowCount(0);

        //ker so stevilke pri GregorianCalendar od 1-7 s tem, da je nedelja ozančena z 1,
        // jo mormo dati na konec tedna za pravilen izpis dni
        if(zacetniDan == 1)
            zacetniDan = 8;
        tabelaModel.setRowCount(steviloTednov);

        //v določena polja tabele dodamo številko dneva meseca
        int i = zacetniDan - 2;
        for (int d = 1; d <= steviloDniMesec; d++) {
            tabelaModel.setValueAt(d,i/7, i%7);
            i = i + 1;
        }
    }

    /**
     * V tej metodi iz datoteke preberemo vse praznike in jih shranimo v listo.
     *
     * @throws IOException
     */
    public static void preberiPraznike() throws IOException {
        //preberemo vse praznike v en string in jih splitamo po vejici
        prazniki = new ArrayList<>();
        Path pot = Path.of("src/prazniki.txt");
        String prebrano = Files.readString(pot);
        String[] praznikiSplit = prebrano.split(",");

        // preverimo če se praznik ponavlja in odstranimo znak za ponavljanje
        for (String praznik : praznikiSplit) {
            boolean sePonavlja = false;
            if (praznik.endsWith("P")) {
                sePonavlja = true;
                praznik = praznik.substring(0,praznik.length() - 2);
            }

            //pridobimo vse dele datuma in dodamo praznik v listo objektov Praznik
            String[] danInMesecInLeto = praznik.split("\\.");
            int dan = Integer.parseInt(danInMesecInLeto[0]);
            int mesec = Integer.parseInt(danInMesecInLeto[1]);
            int leto = Integer.parseInt(danInMesecInLeto[2]);
            prazniki.add(new Praznik(dan,mesec,leto,sePonavlja));
        }
    }

    /**
     * Preverimo, če je datum praznik.
     * Če se ponavalja gledamo samo dan in mesec, drugače celoten datum.
     *
     * @param dan
     * @param mesec
     * @param leto
     * @return
     */
    public static boolean jePraznik(int dan, int mesec, int leto) {
        for (Praznik p : prazniki) {
            if (p.sePonavlja) {
                if (dan == p.getDan() && mesec == p.getMesec())
                    return true;
            } else {
                if (dan == p.getDan() && mesec == p.getMesec() && leto == p.getLeto())
                    return true;
            }
        }
        return false;
    }

    /**
     * Metoda, ki preverja ali je letnica točno iz 4 številk
     *
     * @param letnica
     * @return
     */
    public static boolean jeLetnicaPrava(String letnica){
        Pattern pattern = Pattern.compile("^[0-9]{4}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(letnica);
        return matcher.find();
    }

    /**
     * Metoda preverja ali je datum prave oblike.
     *
     * @param datum
     * @return
     */
    public static boolean jeDatumPravi(String datum){
        Pattern pattern = Pattern.compile("^\\s*(3[01]|[12][0-9]|0?[1-9])\\.(1[012]|0?[1-9])\\.((?:19|20)\\d{2})\\s*$",
                                            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(datum);
        return matcher.find();
    }
}


