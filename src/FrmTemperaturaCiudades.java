import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import datechooser.beans.DateChooserCombo;
import entidades.TemperaturaCiudad;
import servicios.TemperaturaCiudadServicio;

public class FrmTemperaturaCiudades extends JFrame {

    private JComboBox cmbFecha;
    private DateChooserCombo dccDesde, dccHasta;
    private JTabbedPane tpTemperaturaCiudad;
    private JPanel pnlGrafica;
    private JPanel pnlEstadisticas;
    private JPanel pnlDatos;

    private List<LocalDate> fechas;
    private List<TemperaturaCiudad> datos;

    public FrmTemperaturaCiudades() {

        setTitle("Temperatura por ciudad");
        setSize(700, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JToolBar tb = new JToolBar();

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(getClass().getResource("/iconos/Grafica.png")));
        btnGraficar.setToolTipText("Grafica Temperatura vs Fecha");
        btnGraficar.addActionListener(evt -> btnMostrarGraficoClick());
        tb.add(btnGraficar);

        JButton btnCalcularEstadisticas = new JButton();
        btnCalcularEstadisticas.setIcon(new ImageIcon(getClass().getResource("/iconos/Datos.png")));
        btnCalcularEstadisticas.setToolTipText("Señor usuario, seleccione una fecha, para indicar una cuál fue la ciudad más calurosa y la ciudad menos calurosa" );
        btnCalcularEstadisticas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnCalcularEstadisticasClick();
            }
        });
        tb.add(btnCalcularEstadisticas);

        // Contenedor con BoxLayout (vertical)
        JPanel pnlCambios = new JPanel();
        pnlCambios.setLayout(new BoxLayout(pnlCambios, BoxLayout.Y_AXIS));

        JPanel pnlDatosProceso = new JPanel();
        pnlDatosProceso.setPreferredSize(new Dimension(pnlDatosProceso.getWidth(), 50)); // Altura fija de 100px
        pnlDatosProceso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlDatosProceso.setLayout(null);

        JLabel lblFecha = new JLabel("Fecha");
        lblFecha.setBounds(10, 10, 100, 25);
        pnlDatosProceso.add(lblFecha);

        cmbFecha = new JComboBox();
        cmbFecha.setBounds(110, 10, 100, 25);
        pnlDatosProceso.add(cmbFecha);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(220, 10, 100, 25);
        pnlDatosProceso.add(dccDesde);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(330, 10, 100, 25);
        pnlDatosProceso.add(dccHasta);

        pnlGrafica = new JPanel();
        JScrollPane spGrafica = new JScrollPane(pnlGrafica);

        pnlEstadisticas = new JPanel();

        pnlDatos = new JPanel();

        tpTemperaturaCiudad = new JTabbedPane();
        tpTemperaturaCiudad.addTab("Gráfica", spGrafica);
        tpTemperaturaCiudad.addTab("Estadísticas", pnlEstadisticas);
        tpTemperaturaCiudad.addTab("Datos", pnlDatos);

        // Agregar componentes
        pnlCambios.add(pnlDatosProceso);
        pnlCambios.add(tpTemperaturaCiudad);

        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlCambios, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        String nombreArchivo = System.getProperty("user.dir") + "/src/datos/Temperaturas.csv";
        datos = TemperaturaCiudadServicio.getDatos(nombreArchivo);
    
        fechas = datos.stream()
                      .map(TemperaturaCiudad::getFecha)
                      .distinct()
                      .sorted()
                      .collect(Collectors.toList());
    
        DefaultComboBoxModel<LocalDate> dcm = new DefaultComboBoxModel<>(fechas.toArray(new LocalDate[0]));
        cmbFecha.setModel(dcm);
    
        String[] columnas = {"Ciudad", "Fecha", "Temperatura (°C)"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Evita que las celdas sean editables
            }
        };
    
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
        datos.stream()
             .peek(d -> System.out.println(d.getCiudad() + " " + d.getFecha() + " " + d.getTemperatura()))
             .map(d -> new Object[]{
                 d.getCiudad(),
                 d.getFecha().format(formatoFecha),
                 String.format("%.2f", d.getTemperatura())
             })
             .forEach(modelo::addRow);
    
        JTable tabla = new JTable(modelo);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
    
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblBuscar = new JLabel("Buscar:");
        JTextField txtBuscar = new JTextField(20);
        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);
    
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
    
            private void filtrar() {
                String texto = txtBuscar.getText().trim();
                sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + texto));
            }
        });
    
        JScrollPane scroll = new JScrollPane(tabla);

        pnlDatos.removeAll();
        pnlDatos.setLayout(new BorderLayout());
        pnlDatos.add(panelBusqueda, BorderLayout.NORTH);
        pnlDatos.add(scroll, BorderLayout.CENTER);
        pnlDatos.revalidate();
        pnlDatos.repaint();
    
        tpTemperaturaCiudad.setSelectedIndex(2);
    }
    
    private void btnMostrarGraficoClick() {
        if (dccDesde.getSelectedDate() == null || dccHasta.getSelectedDate() == null) {
            JOptionPane.showMessageDialog(this, "Por favor selecciona ambas fechas.");
            return;
        }
    
        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    
        if (desde != null && hasta != null && !desde.isAfter(hasta)) {
            Map<String, Double> promedios = TemperaturaCiudadServicio.calcularPromedioPorCiudad(datos, desde, hasta);
    
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Double> entry : promedios.entrySet()) {
                dataset.addValue(entry.getValue(), "Temperatura promedio", entry.getKey());
            }
    
            JFreeChart chart = ChartFactory.createBarChart(
                "Promedio de Temperatura por Ciudad",
                "Ciudad",
                "Temperatura (°C)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 400));
    
            pnlGrafica.removeAll();
            pnlGrafica.setLayout(new BorderLayout());
            pnlGrafica.add(chartPanel, BorderLayout.CENTER);
            pnlGrafica.revalidate();
            pnlGrafica.repaint();
    
            tpTemperaturaCiudad.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this, "Por favor selecciona un rango de fechas válido.");
        }
    }

    private void btnCalcularEstadisticasClick() {
        if (cmbFecha.getSelectedIndex() >= 0) {
            LocalDate fecha = (LocalDate) cmbFecha.getSelectedItem();

            tpTemperaturaCiudad.setSelectedIndex(1);

            pnlEstadisticas.removeAll();
            pnlEstadisticas.setLayout(new GridBagLayout());
            JLabel lblTitulo = new JLabel("la ciudad más y la menos calurosa para la fecha "+fecha+": ");
            lblTitulo.setBounds(100, 100, 300, 300);
            lblTitulo.setFont(new Font("Arial", Font.ITALIC, 15));
            lblTitulo.setForeground(new Color(204, 0, 0)); 
            pnlEstadisticas.add(lblTitulo);

            int fila = 0;
            var estadisticas = TemperaturaCiudadServicio.identificarCiudadesPorTemperatura(datos, fecha);

            for (var estadistica : estadisticas.entrySet()) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = fila+1;
                gbc.anchor = GridBagConstraints.WEST;
                pnlEstadisticas.add(new JLabel(estadistica.getKey()), gbc);
                gbc.gridx = 10;
                pnlEstadisticas.add(new JLabel(String.format("%.2f °C", estadistica.getValue())), gbc);
                fila++;
            }
            pnlEstadisticas.revalidate();
            pnlEstadisticas.repaint();
                
        }
    }   

}

