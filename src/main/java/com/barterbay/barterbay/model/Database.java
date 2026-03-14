package com.barterbay.barterbay.model;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    public static Connection connect() {

        try {
            return DriverManager.getConnection("jdbc:sqlite:barterbay.db");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}