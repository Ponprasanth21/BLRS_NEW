package com.bornfire.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConnectionManager {

	@Autowired(required = false)
	private DataSource dataSource;

	@Value("${datasrc.url:${spring.datasource.url:jdbc:postgresql://117.247.111.70:5432/blrs}}")
	private String url;

	@Value("${datasrc.username:${spring.datasource.username:blrs_app}}")
	private String username;

	@Value("${datasrc.password:${spring.datasource.password:blrs@123}}")
	private String password;

	@Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
	private String driverClassName;

	private Connection conn;

	public Connection getConnection() {
		try {
			if (dataSource != null) {
				return dataSource.getConnection();
			}

			if (driverClassName != null && !driverClassName.isEmpty()) {
				Class.forName(driverClassName);
			}
			conn = DriverManager.getConnection(url, username, password);

		} catch (SQLException sqlexcp) {
			sqlexcp.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return conn;
	}

	public Connection getConnection(String dbUrl, String user, String pass) {
		try {
			if (driverClassName != null && !driverClassName.isEmpty()) {
				Class.forName(driverClassName);
			}
			return DriverManager.getConnection(dbUrl, user, pass);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
