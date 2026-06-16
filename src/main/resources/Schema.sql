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