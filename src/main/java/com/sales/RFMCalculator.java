/*
 * Copyright (c) 2026 Swagat Samal
 * Licensed under CC BY-NC-ND 4.0
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.sales;

import java.sql.*;
import java.util.*;

public class RFMCalculator {

    public List<double[]> getRFMFeatures() throws Exception {
        List<double[]> features = new ArrayList<>();
        String sql = "SELECT recency_days, frequency, monetary FROM rfm_scores";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            while (rs.next()) {
                features.add(new double[]{
                        rs.getDouble("recency_days"),
                        rs.getDouble("frequency"),
                        rs.getDouble("monetary")
                });
            }
        }
        return features;
    }

    public List<String[]> getCustomerMeta() throws Exception {
        List<String[]> meta = new ArrayList<>();
        String sql = "SELECT customer_id, name, region, segment FROM rfm_scores";
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            while (rs.next()) {
                meta.add(new String[]{
                        rs.getString("customer_id"),
                        rs.getString("name"),
                        rs.getString("region"),
                        rs.getString("segment")
                });
            }
        }
        return meta;
    }

    public void exportARFF(List<double[]> features) throws Exception {
        new java.io.File("output").mkdirs();
        try (java.io.PrintWriter pw = new java.io.PrintWriter("output/rfm_data.arff")) {
            pw.println("@relation RFM_Customers");
            pw.println("@attribute recency  NUMERIC");
            pw.println("@attribute frequency NUMERIC");
            pw.println("@attribute monetary  NUMERIC");
            pw.println("@data");
            for (double[] r : features)
                pw.printf("%.0f,%.0f,%.2f%n", r[0], r[1], r[2]);
        }
        System.out.println("  ARFF exported → output/rfm_data.arff");
    }
}