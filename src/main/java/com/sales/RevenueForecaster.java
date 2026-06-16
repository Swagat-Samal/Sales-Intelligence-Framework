/*
 * Copyright (c) 2026 Swagat Samal
 * Licensed under CC BY-NC-ND 4.0
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.sales;

import weka.classifiers.functions.LinearRegression;
import weka.core.*;
import java.sql.*;
import java.util.*;

public class RevenueForecaster {

    public void forecast() throws Exception {
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute("month_index"));
        attrs.add(new Attribute("revenue"));

        Instances trainData = new Instances("MonthlyRevenue", attrs, 20);
        trainData.setClassIndex(1);

        String sql = "SELECT MONTH(txn_date) AS mo, YEAR(txn_date) AS yr,"
                + " SUM(amount) AS total"
                + " FROM transactions"
                + " GROUP BY yr, mo"
                + " ORDER BY yr, mo";

        try (Connection con = DBConnection.getConnection();
             Statement st   = con.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            int idx = 1;
            while (rs.next()) {
                double[] vals = { idx++, rs.getDouble("total") };
                trainData.add(new DenseInstance(1.0, vals));
            }
        }

        if (trainData.numInstances() < 2) {
            System.out.println("  Not enough monthly data to forecast yet.");
            return;
        }

        LinearRegression lr = new LinearRegression();
        lr.buildClassifier(trainData);

        int lastIdx = trainData.numInstances();
        System.out.println("\n=== REVENUE FORECAST (next 3 months) ===");
        for (int i = 1; i <= 3; i++) {
            Instance future = new DenseInstance(1.0, new double[]{ lastIdx + i, 0 });
            future.setDataset(trainData);
            System.out.printf("  Month +%d : %.2f%n", i, lr.classifyInstance(future));
        }
    }
}