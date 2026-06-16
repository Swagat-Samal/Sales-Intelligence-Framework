/*
 * Copyright (c) 2026 Swagat Samal
 * Licensed under CC BY-NC-ND 4.0
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.sales;

import java.sql.*;

public class GapDetector {

    public void detectGaps() throws Exception {

        String sql = "WITH regional AS ("
                + " SELECT c.region, p.category,"
                + " SUM(t.amount) AS revenue,"
                + " COUNT(DISTINCT c.customer_id) AS buyers"
                + " FROM transactions t"
                + " JOIN customers c ON t.customer_id = c.customer_id"
                + " JOIN products p ON t.product_id = p.product_id"
                + " GROUP BY c.region, p.category"
                + "), averages AS ("
                + " SELECT AVG(revenue) AS avg_rev FROM regional"
                + ")"
                + " SELECT r.region, r.category, r.revenue, r.buyers,"
                + " ROUND((r.revenue / a.avg_rev) * 100, 1) AS pct_of_avg"
                + " FROM regional r, averages a"
                + " WHERE r.revenue < a.avg_rev * 0.4"
                + " ORDER BY r.revenue ASC";

        System.out.println("\n=== MARKET GAPS DETECTED ===");
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf(
                        "  Region: %-12s | Category: %-15s | Revenue: %,10.2f | Gap: %.1f%% of avg%n",
                        rs.getString("region"),
                        rs.getString("category"),
                        rs.getDouble("revenue"),
                        rs.getDouble("pct_of_avg"));
            }
            if (!found)
                System.out.println("  No significant gaps found in current data.");
        }
    }
}