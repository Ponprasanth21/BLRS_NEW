package com.bornfire.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	Connection conn;

	public Connection getConnection() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

			conn = DriverManager.getConnection("jdbc:oracle:thin:@122.165.217.18:1522/BAJ", "BAJ", "baj");

		} catch (SQLException sqlexcp) {
			sqlexcp.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;
	}
}
