-- Copyright (c) 2026 Swagat Samal. All rights reserved.
-- Licensed under CC BY-NC-ND 4.0

CREATE DATABASE sales_db;
USE sales_db;

-- TABLE 1: customers
CREATE TABLE customers (
                           customer_id  INT          NOT NULL AUTO_INCREMENT,
                           name         VARCHAR(100) NOT NULL,
                           region       VARCHAR(50)  NOT NULL,
                           segment      ENUM('B2B','B2C','Enterprise') NOT NULL,
                           created_at   DATE         NOT NULL,
                           PRIMARY KEY (customer_id)
);

-- TABLE 2: products
CREATE TABLE products (
                          product_id   INT           NOT NULL AUTO_INCREMENT,
                          name         VARCHAR(100)  NOT NULL,
                          category     VARCHAR(50)   NOT NULL,
                          price        DECIMAL(10,2) NOT NULL,
                          cost         DECIMAL(10,2) NOT NULL,
                          PRIMARY KEY (product_id)
);

-- TABLE 3: transactions
CREATE TABLE transactions (
                              txn_id       INT           NOT NULL AUTO_INCREMENT,
                              customer_id  INT           NOT NULL,
                              product_id   INT           NOT NULL,
                              quantity     INT           NOT NULL,
                              amount       DECIMAL(10,2) NOT NULL,
                              txn_date     DATE          NOT NULL,
                              PRIMARY KEY (txn_id),
                              CONSTRAINT fk_customer FOREIGN KEY (customer_id)
                                  REFERENCES customers(customer_id) ON DELETE CASCADE,
                              CONSTRAINT fk_product  FOREIGN KEY (product_id)
                                  REFERENCES products(product_id)  ON DELETE CASCADE
);

-- VIEW 1: rfm scores — recency, frequency, monetary per customer
CREATE VIEW rfm_scores AS
SELECT
    c.customer_id,
    c.name,
    c.region,
    c.segment,
    DATEDIFF(CURDATE(), MAX(t.txn_date))  AS recency_days,
    COUNT(t.txn_id)                          AS frequency,
    SUM(t.amount)                            AS monetary
FROM customers c
         JOIN transactions t ON c.customer_id = t.customer_id
GROUP BY c.customer_id, c.name, c.region, c.segment;

-- VIEW 2: product pairs — co-purchase matrix for recommendations
CREATE VIEW product_pairs AS
SELECT
    t1.product_id                          AS product_a,
    t2.product_id                          AS product_b,
    COUNT(DISTINCT t1.customer_id)         AS co_purchase_count,
    p1.name                                AS product_a_name,
    p2.name                                AS product_b_name,
    p1.category                            AS category_a,
    p2.category                            AS category_b
FROM transactions t1
         JOIN transactions t2
              ON  t1.customer_id = t2.customer_id   -- same customer
                  AND t1.product_id  < t2.product_id    -- avoid (A,B) and (B,A) duplicates
         JOIN products p1 ON t1.product_id = p1.product_id
         JOIN products p2 ON t2.product_id = p2.product_id
GROUP BY t1.product_id, t2.product_id, p1.name, p2.name, p1.category, p2.category
ORDER BY co_purchase_count DESC;

-- VIEW 3: region demand — powers demand by location + trends
CREATE VIEW region_demand AS
SELECT
    c.region,
    p.category,
    p.name                               AS product_name,
        YEAR(t.txn_date)                     AS yr,
        MONTH(t.txn_date)                    AS mo,
        SUM(t.quantity)                      AS total_units,
        SUM(t.amount)                        AS total_revenue,
        COUNT(DISTINCT t.customer_id)        AS unique_buyers
        FROM transactions t
        JOIN customers c ON t.customer_id = c.customer_id
        JOIN products  p ON t.product_id  = p.product_id
        GROUP BY c.region, p.category, p.name, yr, mo
        ORDER BY c.region, total_revenue DESC;

-- Verify Tables
SHOW TABLES;