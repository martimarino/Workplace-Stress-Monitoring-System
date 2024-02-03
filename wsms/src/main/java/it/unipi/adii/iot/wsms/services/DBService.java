package it.unipi.adii.iot.wsms.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DBService {
	private static final Logger logger = LogManager.getLogger(DBService.class.getName());

	public static final DBService db_Service = DBService.getInstance();

	private final static String DB_DEFAULT_IP = "localhost";
	private final static String DB_DFAULT_PORT = "3306";
	private final static String DB_USER = "root";
	private final static String DB_PASSWORD = "root";
	private final static String DB_NAME = "wsms";

	private static Connection conn = null;
	private static DBService instance = null;

	private DBService() {}

	public static DBService getInstance() {
		if (instance == null) {
			instance = new DBService();
		}
		return instance;
	}

	private static void getConnection() {

		String connStr = "jdbc:mysql://" + DB_DEFAULT_IP + ":" + DB_DFAULT_PORT + "/" + DB_NAME +
				"?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET";
		if(conn == null) {
			try {
				// DriverManager: The basic service for managing a set of JDBC drivers.
				conn = DriverManager.getConnection(connStr,
						DB_USER,
						DB_PASSWORD);
				//The Driver Manager provides the connection specified in the parameter string
				if (conn == null) {
					logger.warn("DB connection not created");
				}else{
					System.out.println("DB connection succesfully created");
				}
			} catch (SQLException se) {
				logger.error("DB connection failed.", se);
				conn = null;
			}
		}

	}

	public void cleanDB() {
		cleanSensor();
		cleanObservation();
	}

	public void cleanSensor(){

		String query =  "DELETE FROM sensor";
		getConnection();
		try (PreparedStatement ps = conn.prepareStatement(query);)
		{
			ps.executeUpdate();
		} catch (SQLException se) {
			logger.error("DB not cleaned: ", se);
		}

	}

	public void cleanObservation(){

		String query =  "DELETE FROM observation";
		getConnection();
		try (PreparedStatement ps = conn.prepareStatement(query);)
		{
			ps.executeUpdate();
		} catch (SQLException se) {
			logger.error("DB observations not cleaned: ", se);
		}

	}

	public boolean addSensor(String nodeId, String dataType) {
		String query = "INSERT INTO sensor (nodeId, dataType) VALUES (?, ?);";
		System.out.println("NodeId: " + nodeId + ", DataType: " + dataType);
		boolean success = true;
		getConnection();
		try (PreparedStatement ps = conn.prepareStatement(query);)
		{
			ps.setString(1, nodeId);
			ps.setString(2, dataType);
			int insertedRow = ps.executeUpdate();
			if(insertedRow < 1) {
				logger.warn("Something wrong during in adding sensor");
				success = false;
			}

		} catch (SQLException se) {
			logger.error("Error in the insert sensor query! ", se);
			success = false;
		}

		return success;
	}

	public boolean deleteSensor (String nodeId, String dataType) {
		String query =  "DELETE FROM sensor where nodeId = ? and dataType = ?";
		boolean success = true;
		getConnection();
		try (PreparedStatement ps = conn.prepareStatement(query);)
		{
			ps.setString(1, nodeId);
			ps.setString(2, dataType);
			int insertedRow = ps.executeUpdate();
			if(insertedRow < 1) {
				logger.warn("Something wrong during add sensor");
				success = false;
			}

		} catch (SQLException se) {
			logger.error("Error in the delete sensor query! ", se);
			success = false;
		}

		return success;
	}

	public static boolean addObservation(String nodeId, int value) {

		String query = "INSERT INTO observation (sensor, value, timestamp) VALUES (?, ?, ?);";
		boolean success = true;
		getConnection();
		try (PreparedStatement ps = conn.prepareStatement(query);)
		{
			ps.setString(1, nodeId);
			ps.setInt(2, value);
			Date actualDate = new Date();
			Timestamp ts = new Timestamp(actualDate.getTime());
			ps.setTimestamp(3, ts);
			int insertedRow = ps.executeUpdate();
			if(insertedRow < 1) {
				logger.warn("Something wrong adding observation!");
				success = false;
			}

		} catch (SQLException se) {
			logger.error("Error in the add observation query! ", se);
			success = false;
		}
		return success;
	}


	public boolean updateSensorState(String sensor, short status) {
		String query = "UPDATE sensor SET active=? WHERE nodeId=?;";
		boolean success = true;
		getConnection();
		try (PreparedStatement ps = conn.prepareStatement(query);)
		{
			ps.setShort(1, status);
			ps.setString(2, sensor);
			int insertedRow = ps.executeUpdate();
			if(insertedRow < 1) {
				logger.warn("Something wrong during add observation!");
				success = false;
			}

		} catch (SQLException se) {
			logger.error("Error in the add observation query! ", se);
			success = false;
		}
		return success;
	}

	public boolean checkSensorExistence(String sensor) {
		String query = "SELECT nodeId FROM sensor WHERE nodeId=?;";
		boolean success = false;
		getConnection();
		try (PreparedStatement ps = conn.prepareStatement(query);)
		{
			ps.setString(1, sensor);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				success = true;
			}

		} catch (SQLException se) {
			logger.error("Error in the check sensor existence query! ", se);
			success = true;
		}
		return success;
	}

}
