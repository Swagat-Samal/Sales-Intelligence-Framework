/*
 * Copyright (c) 2026 Swagat Samal
 * Licensed under CC BY-NC-ND 4.0
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.sales;

import java.sql.*;
import java.io.*;

public class RegionAnalyser {

    public void printTopProductsByRegion() throws Exception {
        String sql = "SELECT region, product_name, category,"
                + " SUM(total_revenue) AS rev,"
                + " SUM(total_units) AS units"
                + " FROM region_demand"
                + " GROUP BY region, product_name, category"
                + " ORDER BY region, rev DESC";

        System.out.println("\n=== TOP PRODUCTS BY REGION ===");
        String cur = "";
        int rank = 0;

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            while (rs.next()) {
                String region = rs.getString("region");
                if (!region.equals(cur)) {
                    cur = region;
                    rank = 0;
                    System.out.printf("%n  [ %s ]%n", region);
                }
                if (rank++ < 3)
                    System.out.printf("    #%d %-22s Rev:%,10.0f  Units:%3d%n",
                            rank,
                            rs.getString("product_name"),
                            rs.getDouble("rev"),
                            rs.getInt("units"));
            }
        }
    }

    public void printGrowthRates() throws Exception {
        String sql = "SELECT r1.region,"
                + " r1.monthly_rev AS current_rev,"
                + " r2.monthly_rev AS prev_rev,"
                + " ROUND(((r1.monthly_rev - r2.monthly_rev) / r2.monthly_rev) * 100, 1) AS growth_pct"
                + " FROM ("
                + "   SELECT region, SUM(total_revenue) AS monthly_rev"
                + "   FROM region_demand"
                + "   WHERE yr = YEAR(CURDATE()) AND mo = MONTH(CURDATE()) - 1"
                + "   GROUP BY region"
                + " ) r1"
                + " JOIN ("
                + "   SELECT region, SUM(total_revenue) AS monthly_rev"
                + "   FROM region_demand"
                + "   WHERE yr = YEAR(CURDATE()) AND mo = MONTH(CURDATE()) - 2"
                + "   GROUP BY region"
                + " ) r2 ON r1.region = r2.region"
                + " ORDER BY growth_pct DESC";

        System.out.println("\n=== REGIONAL GROWTH RATES ===");
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-12s  Current:%,9.0f  Prev:%,9.0f  Growth:%+.1f%%%n",
                        rs.getString("region"),
                        rs.getDouble("current_rev"),
                        rs.getDouble("prev_rev"),
                        rs.getDouble("growth_pct"));
            }
            if (!found)
                System.out.println("  Not enough monthly data for growth comparison.");
        }
    }

    public void exportReport() throws Exception {
        String sql = "SELECT region, product_name, category,"
                + " SUM(total_revenue) AS revenue,"
                + " SUM(total_units) AS units"
                + " FROM region_demand"
                + " GROUP BY region, product_name, category"
                + " ORDER BY region, revenue DESC";

        new File("output").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter("output/region_report.csv"));
             Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            pw.println("region,product_name,category,revenue,units");
            while (rs.next())
                pw.printf("%s,%s,%s,%.2f,%d%n",
                        rs.getString("region"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getDouble("revenue"),
                        rs.getInt("units"));
        }
        System.out.println("  Saved → output/region_report.csv");
    }

    public void runAll() throws Exception {
        printTopProductsByRegion();
        printGrowthRates();
        exportReport();
    }
}