package test;

import domain.delivery.DeliveryReport;

public class testDeliveryReport {
    public static void run() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║      PRUEBA DE REPORTES DE ENTREGAS                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Crear reporte
        DeliveryReport report = new DeliveryReport("Reporte de Prueba de Entregas");

        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("FASE 1: Agregar entradas al reporte");
        System.out.println("═══════════════════════════════════════════════════════════");

        report.addEntry("Primera entrega completada");
        report.addEntryWithTimestamp("Segunda entrega completada");
        report.addEntryWithSimulatedTimestamp("Tercera entrega completada", 2.5);
        report.addEntryWithSimulatedTimestamp("Cuarta entrega completada", 5.75);

        System.out.println("[INFO] Entradas agregadas: " + report.getEntryCount());
        System.out.println("[INFO] Resumen del reporte:");
        System.out.println(report.getSummary());

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("FASE 2: Guardar reporte en formato texto");
        System.out.println("═══════════════════════════════════════════════════════════");

        report.saveEntry("reports/test_report.txt");

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("FASE 3: Guardar reporte con timestamp en nombre");
        System.out.println("═══════════════════════════════════════════════════════════");

        String timestampedPath = report.saveWithTimestamp("reports/report_timestamped");
        System.out.println("[INFO] Archivo guardado: " + timestampedPath);

        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("FASE 4: Agregar más entradas y anexar");
        System.out.println("═══════════════════════════════════════════════════════════");

        report.addEntry("Quinta entrega completada");
        report.addEntry("Sexta entrega completada");
        report.appendToFile("reports/test_report.txt");

        System.out.println("\n[OK] Prueba de reportes completada exitosamente");
        System.out.println("[INFO] Verifica los archivos generados en el directorio 'reports/'");
    }
}

