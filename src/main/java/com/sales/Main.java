/*
 * Copyright (c) 2026 Swagat Samal
 * Licensed under CC BY-NC-ND 4.0
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.sales;

import java.sql.Connection;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("com.github.fommil")
                .setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("weka")
                .setLevel(java.util.logging.Level.OFF);

        System.out.println("===========================================");
        System.out.println("   SALES INTELLIGENCE FRAMEWORK v1.0");
        System.out.println("===========================================");

        try {
            System.out.println("\n[1/6] Connecting to MySQL...");
            Connection conn = DBConnection.getConnection();
            System.out.println("      Connected successfully.");

            System.out.println("\n[2/6] Loading RFM data...");
            RFMCalculator rfm       = new RFMCalculator();
            List<double[]> features = rfm.getRFMFeatures();
            List<String[]> meta     = rfm.getCustomerMeta();
            rfm.exportARFF(features);
            System.out.printf("      Loaded %d customer records.%n", features.size());

            System.out.println("\n[3/6] Running K-Means (k=4)...");
            MLEngine ml     = new MLEngine();
            int[] clusters  = ml.clusterCustomers(features);
            String[] labels = {"PREMIUM", "GROWTH", "AT-RISK", "LOST"};

            System.out.println("\n[4/6] Detecting market gaps...");
            try { new GapDetector().detectGaps(); }
            catch (Exception e) { System.out.println("  GapDetector error: " + e.getMessage()); }

            System.out.println("\n[5/6] Revenue forecast...");
            try { new RevenueForecaster().forecast(); }
            catch (Exception e) { System.out.println("  Forecaster error: " + e.getMessage()); }

            System.out.println("\n[6/6] Product recommendations...");
            try { new ProductRecommender().runForAllCustomers(); }
            catch (Exception e) { System.out.println("  Recommender error: " + e.getMessage()); }

            System.out.println("\n[BONUS] Region analysis...");
            try { new RegionAnalyser().runAll(); }
            catch (Exception e) { System.out.println("  RegionAnalyser error: " + e.getMessage()); }

            System.out.println("\nExporting results...");
            try {
                exportCSV(meta, features, clusters, labels);
                ChartExporter.export(clusters, labels, features, meta);
            } catch (Exception e) { System.out.println("  Export error: " + e.getMessage()); }

            conn.close();
            System.out.println("\n========== Run complete ==========");
            System.out.println("Check output/ folder for all files + dashboard.html");

        } catch (Exception e) {
            System.err.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void exportCSV(List<String[]> meta, List<double[]> features,
                                  int[] clusters, String[] labels) throws Exception {
        new java.io.File("output").mkdirs();
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.FileWriter("output/results.csv"))) {
            pw.println("customer_id,name,region,segment,recency,frequency,monetary,cluster,label");
            for (int i = 0; i < meta.size(); i++) {
                String[] m = meta.get(i);
                double[] f = features.get(i);
                pw.printf("%s,%s,%s,%s,%.0f,%.0f,%.2f,%d,%s%n",
                        m[0], m[1], m[2], m[3],
                        f[0], f[1], f[2],
                        clusters[i], labels[clusters[i]]);
            }
        }
        System.out.println("  Saved → output/results.csv");
    }
}
