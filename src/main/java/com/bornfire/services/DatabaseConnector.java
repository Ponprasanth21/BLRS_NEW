package com.bornfire.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DatabaseConnector {

	@Autowired(required = false)
	private DataSource dataSource;

	@Autowired(required = false)
	private ConnectionManager connectionManager;

	@Value("${datasrc.url:${spring.datasource.url:jdbc:postgresql://117.247.111.70:5432/blrs}}")
	private String dbUrl;

	@Value("${datasrc.username:${spring.datasource.username:blrs_app}}")
	private String user;

	@Value("${datasrc.password:${spring.datasource.password:blrs@123}}")
	private String password;

	@Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
	private String driverClassName;

	public Connection getConnection() throws SQLException {
		if (dataSource != null) {
			return dataSource.getConnection();
		}
		if (connectionManager != null) {
			Connection conn = connectionManager.getConnection();
			if (conn != null) {
				return conn;
			}
		}
		try {
			if (driverClassName != null && !driverClassName.isEmpty()) {
				Class.forName(driverClassName);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return DriverManager.getConnection(dbUrl, user, password);
	}

	public void insertData(List<String> data) {
		try (Connection connection = getConnection()) {
			String sql = "INSERT INTO att (emp_id) VALUES (?)";

			try (PreparedStatement statement = connection.prepareStatement(sql)) {

				for (String value : data) {
					System.out.println(value);
					statement.setString(1, value);
					statement.addBatch();
				}
				statement.executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}