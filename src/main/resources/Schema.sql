-- Copyright (c) 2026 Swagat Samal. All rights reserved.
-- Licensed under CC BY-NC-ND 4.0

DROP DATABASE IF EXISTS sales_db;
CREATE DATABASE sales_db;
USE sales_db;

-- TABLE 1: customers
CREATE TABLE customers (
                           customer_id  INT           NOT NULL AUTO_INCREMENT,
                           name         VARCHAR(100)  NOT NULL,
                           region       VARCHAR(50)   NOT NULL,
                           segment      ENUM('B2B','B2C','Enterprise') NOT NULL,
                           created_at   DATE          NOT NULL,
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
                              CONSTRAINT fk_cust FOREIGN KEY (customer_id)
                                  REFERENCES customers(customer_id) ON DELETE CASCADE,
                              CONSTRAINT fk_prod FOREIGN KEY (product_id)
                                  REFERENCES products(product_id)  ON DELETE CASCADE
);
