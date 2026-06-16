/*
 * Copyright (c) 2026 Swagat Samal
 * Licensed under CC BY-NC-ND 4.0
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.sales;

import java.sql.*;
import java.io.*;
import java.util.*;

public class ProductRecommender {

    public List<String> recommendForCustomer(int customerId, int topN) throws Exception {
        Set<Integer> bought = getCustomerProducts(customerId);
        if (bought.isEmpty()) return Collections.emptyList();

        Map<Integer, Integer> scoreMap = new HashMap<>();
        Map<Integer, String>  nameMap  = new HashMap<>();

        String idList = bought.toString().replaceAll("[\\[\\]]", "");
        String sql = "SELECT product_a, product_b, co_purchase_count,"
                + " product_a_name, product_b_name"
                + " FROM product_pairs"
                + " WHERE product_a IN (" + idList + ")"
                + " OR product_b IN (" + idList + ")"
                + " ORDER BY co_purchase_count DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            while (rs.next()) {
                int    pA    = rs.getInt("product_a");
                int    pB    = rs.getInt("product_b");
                int    score = rs.getInt("co_purchase_count");
                nameMap.put(pA, rs.getString("product_a_name"));
                nameMap.put(pB, rs.getString("product_b_name"));
                if (bought.contains(pA) && !bought.contains(pB))
                    scoreMap.merge(pB, score, Integer::sum);
                if (bought.contains(pB) && !bought.contains(pA))
                    scoreMap.merge(pA, score, Integer::sum);
            }
        }
        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<Integer,Integer>comparingByValue().reversed())
                .limit(topN)
                .map(e -> nameMap.getOrDefault(e.getKey(), "Product#" + e.getKey())
                        + " (score:" + e.getValue() + ")")
                .collect(java.util.stream.Collectors.toList());
    }

    private Set<Integer> getCustomerProducts(int id) throws Exception {
        Set<Integer> set = new HashSet<>();
        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(
                     "SELECT DISTINCT product_id FROM transactions WHERE customer_id = " + id)) {
            while (rs.next()) set.add(rs.getInt("product_id"));
        }
        return set;
    }

    public void runForAllCustomers() throws Exception {
        System.out.println("\n=== PRODUCT RECOMMENDATIONS ===");
        new File("output").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter("output/recommendations.csv"));
             Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(
                     "SELECT customer_id, name FROM customers ORDER BY customer_id")) {
            pw.println("customer_id,name,recommendations");
            while (rs.next()) {
                int    cid  = rs.getInt("customer_id");
                String name = rs.getString("name");
                List<String> recs = recommendForCustomer(cid, 3);
                if (!recs.isEmpty()) {
                    System.out.printf("  %-22s → %s%n", name, String.join(" | ", recs));
                    pw.printf("%d,%s,\"%s\"%n", cid, name, String.join("; ", recs));
                }
            }
        }
        System.out.println("  Saved → output/recommendations.csv");
    }
}