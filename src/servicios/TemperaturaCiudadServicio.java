package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import entidades.TemperaturaCiudad;

public class TemperaturaCiudadServicio {

    public static List<TemperaturaCiudad> getDatos(String nombreArchivo) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            return Files.lines(Paths.get(nombreArchivo))
                    .skip(1)  
                    .map(linea -> linea.split(","))
                    .map(campos -> new TemperaturaCiudad(
                            campos[0],
                            LocalDate.parse(campos[1], formatoFecha),
                            Double.parseDouble(campos[2])
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static Map<String, Double> calcularPromedioPorCiudad(List<TemperaturaCiudad> datos, LocalDate desde, LocalDate hasta) {
        return datos.stream()
            .filter(d -> !d.getFecha().isBefore(desde) && !d.getFecha().isAfter(hasta))
            .collect(Collectors.groupingBy(
                TemperaturaCiudad::getCiudad,
                Collectors.averagingDouble(TemperaturaCiudad::getTemperatura)
            ));
    }

    public static Map<String, Double> identificarCiudadesPorTemperatura(List<TemperaturaCiudad> datos, LocalDate fecha) {
        Map<String, Double> resultado = new LinkedHashMap<>();
    
        System.out.println("Datos filtrados para la fecha: " + fecha);
        datos.stream()
             .filter(d -> d.getFecha().equals(fecha))
             .forEach(d -> System.out.println(d.getCiudad() + " " + d.getTemperatura() + "°C"));
    
        Optional<TemperaturaCiudad> maxTemp = datos.stream()
                .filter(d -> d.getFecha().equals(fecha))
                .max(Comparator.comparingDouble(TemperaturaCiudad::getTemperatura));
    
        Optional<TemperaturaCiudad> minTemp = datos.stream()
                .filter(d -> d.getFecha().equals(fecha))
                .min(Comparator.comparingDouble(TemperaturaCiudad::getTemperatura));
    
        maxTemp.ifPresent(d -> resultado.put("Ciudad más calurosa: " + d.getCiudad(), d.getTemperatura()));
        minTemp.ifPresent(d -> resultado.put("Ciudad menos calurosa: " + d.getCiudad(), d.getTemperatura()));
    
        return resultado;
    } 
    
}
