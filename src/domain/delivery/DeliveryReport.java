package domain.delivery;

import interfaces.Reportable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera y gestiona reportes de entregas.
 * Permite agregar entradas y guardarlas en archivos de texto.
 */
public class DeliveryReport implements Reportable {

    /** Lista de entradas del reporte */
    private final List<String> entries;

    /** Título del reporte */
    private String title;

    /** Última ruta donde se guardó el reporte */
    private String lastSavedPath;

    /** Formato de fecha para nombres de archivo */
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /** Formato de fecha para timestamps */
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor del reporte.
     */
    public DeliveryReport() {
        this.entries = new ArrayList<>();
        this.title = "Reporte de Entregas";
    }

    /**
     * Constructor del reporte con título personalizado.
     *
     * @param title Título del reporte
     */
    public DeliveryReport(String title) {
        this.entries = new ArrayList<>();
        this.title = title;
    }

    /**
     * Agrega una entrada al reporte.
     *
     * @param entry Texto de la entrada
     */
    @Override
    public void addEntry(String entry) {
        if (entry != null && !entry.trim().isEmpty()) {
            entries.add(entry);
        }
    }

    /**
     * Agrega una entrada con timestamp.
     *
     * @param entry Texto de la entrada
     */
    public void addEntryWithTimestamp(String entry) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        addEntry("[" + timestamp + "] " + entry);
    }

    /**
     * Agrega una entrada con timestamp simulado (para usar con tiempo simulado).
     *
     * @param entry Texto de la entrada
     * @param simulatedTime Tiempo simulado en horas
     */
    public void addEntryWithSimulatedTimestamp(String entry, double simulatedTime) {
        int hours = (int) simulatedTime;
        int minutes = (int) ((simulatedTime - hours) * 60);
        String timestamp = String.format("SimTime: %02d:%02d", hours, minutes);
        addEntry("[" + timestamp + "] " + entry);
    }

    /**
     * Guarda el reporte en un archivo (formato de texto).
     *
     * @param path Ruta del archivo donde guardar el reporte
     */
    @Override
    public void saveEntry(String path) {
        saveToFile(path, false);
    }

    /**
     * Guarda el reporte en un archivo con opción de anexar.
     *
     * @param path Ruta del archivo donde guardar el reporte
     * @param append true para anexar al archivo existente, false para sobrescribir
     */
    public void saveToFile(String path, boolean append) {
        if (path == null || path.trim().isEmpty()) {
            System.out.println("[WARNING] Ruta de archivo inválida");
            return;
        }

        try {
            // Crear directorio si no existe
            Path filePath = Paths.get(path);
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                System.out.println("[INFO] Directorio creado: " + parentDir);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, append))) {
                if (!append) {
                    // Escribir encabezado solo si no es modo append
                    writer.write("=".repeat(80));
                    writer.newLine();
                    writer.write(title.toUpperCase());
                    writer.newLine();
                    writer.write("Generado: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
                    writer.newLine();
                    writer.write("=".repeat(80));
                    writer.newLine();
                    writer.newLine();
                } else {
                    writer.newLine();
                    writer.write("--- Entradas añadidas: " + LocalDateTime.now().format(TIMESTAMP_FORMAT) + " ---");
                    writer.newLine();
                }

                // Escribir entradas
                if (entries.isEmpty()) {
                    writer.write("No hay entradas en el reporte.");
                    writer.newLine();
                } else {
                    int startIndex = append && lastSavedPath != null && lastSavedPath.equals(path) ? entries.size() - 1 : 0;
                    for (int i = startIndex; i < entries.size(); i++) {
                        writer.write(String.format("%d. %s", i + 1, entries.get(i)));
                        writer.newLine();
                    }
                }

                writer.newLine();
                writer.write("=".repeat(80));
                writer.newLine();
                writer.write("Total de entradas: " + entries.size());
                writer.newLine();
                writer.write("=".repeat(80));

                lastSavedPath = path;
                System.out.println("[OK] Reporte guardado exitosamente en: " + path);
                System.out.println("     Total de entradas: " + entries.size());
                System.out.println("     Modo: " + (append ? "Anexar" : "Sobrescribir"));
            }

        } catch (IOException e) {
            System.out.println("[ERROR] Error al guardar el reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Guarda el reporte con un nombre de archivo que incluye timestamp.
     *
     * @param basePath Ruta base del archivo (sin extensión)
     * @return ruta completa del archivo guardado
     */
    public String saveWithTimestamp(String basePath) {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fullPath = basePath + "_" + timestamp + ".txt";
        saveToFile(fullPath, false);
        return fullPath;
    }

    /**
     * Anexa nuevas entradas al archivo existente.
     *
     * @param path Ruta del archivo donde anexar
     */
    public void appendToFile(String path) {
        saveToFile(path, true);
    }

    /**
     * Limpia todas las entradas del reporte.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Obtiene el número de entradas en el reporte.
     *
     * @return número de entradas
     */
    public int getEntryCount() {
        return entries.size();
    }

    /**
     * Obtiene todas las entradas del reporte.
     *
     * @return lista de entradas
     */
    public List<String> getEntries() {
        return new ArrayList<>(entries); // Retorna copia
    }

    /**
     * Establece el título del reporte.
     *
     * @param title Nuevo título
     */
    public void setTitle(String title) {
        this.title = title != null ? title : "Reporte de Entregas";
    }

    /**
     * Obtiene el título del reporte.
     *
     * @return título del reporte
     */
    public String getTitle() {
        return title;
    }

    /**
     * Obtiene la última ruta donde se guardó el reporte.
     *
     * @return ruta del último archivo guardado, o null si nunca se guardó
     */
    public String getLastSavedPath() {
        return lastSavedPath;
    }

    /**
     * Obtiene un resumen del reporte como String.
     *
     * @return resumen formateado del reporte
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=".repeat(80)).append("\n");
        summary.append(title.toUpperCase()).append("\n");
        summary.append("=".repeat(80)).append("\n");
        summary.append("Total de entradas: ").append(entries.size()).append("\n");
        summary.append("Última guardado: ").append(lastSavedPath != null ? lastSavedPath : "Nunca guardado").append("\n");
        summary.append("=".repeat(80)).append("\n");
        return summary.toString();
    }
}
