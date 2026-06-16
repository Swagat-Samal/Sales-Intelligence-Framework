/*
 * Copyright (c) 2026 Swagat Samal
 * Licensed under CC BY-NC-ND 4.0
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.sales;

import weka.clusterers.SimpleKMeans;
import weka.core.*;
import java.util.*;

public class MLEngine {

    public int[] clusterCustomers(List<double[]> data) throws Exception {

        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute("recency"));
        attrs.add(new Attribute("frequency"));
        attrs.add(new Attribute("monetary"));

        Instances dataset = new Instances("RFMData", attrs, data.size());
        for (double[] row : data)
            dataset.add(new DenseInstance(1.0, row));

        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(4);
        kmeans.setSeed(42);
        kmeans.setMaxIterations(500);
        kmeans.buildClusterer(dataset);

        Instances centroids = kmeans.getClusterCentroids();
        System.out.println("  Cluster centroids (Recency | Frequency | Monetary):");
        for (int i = 0; i < centroids.numInstances(); i++) {
            Instance c = centroids.instance(i);
            System.out.printf("    Cluster %d: R=%.1f  F=%.1f  M=%.2f%n",
                    i, c.value(0), c.value(1), c.value(2));
        }

        int[] assignments = new int[data.size()];
        for (int i = 0; i < dataset.numInstances(); i++)
            assignments[i] = kmeans.clusterInstance(dataset.instance(i));

        return assignments;
    }
}