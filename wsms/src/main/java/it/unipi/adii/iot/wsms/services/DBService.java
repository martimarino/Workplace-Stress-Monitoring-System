package it.unipi.adii.iot.wsms.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class DBService {
    private final static Logger logger = LogManager.getLogger(DBService.class);
	
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
	            // DriverManager for managing a set of JDBC drivers.
	            conn = DriverManager.getConnection(connStr,	DB_USER, DB_PASSWORD);

	            if (conn == null) {
	                logger.warn("DB connection not created");
	            }
	        } catch (SQLException se) {
	            logger.error("DB connection failed.", se);
	            conn = null;
	        }
    	}
		
    }
    
    public void cleanDB() {
	System.out.println("Cleaning db..");
    	String[] queries = {"DELETE FROM sensor"};
    	getConnection();
	
	for(String q : queries) {
	    	try (PreparedStatement ps = conn.prepareStatement(q);)
	    	{
	    		ps.executeUpdate();
		} catch (SQLException se) {
			logger.error("DB not cleaned: ", se);
		}
	}
    }
    
    public boolean addSensor(String nodeId, String dataType) {
    	String query = "INSERT INTO sensor (nodeId, dataType) VALUES (?, ?);";
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
        	logger.error("Error in the inserting query! ", se);
        	success = false;
        }

		return success;
    }
    
    public boolean deleteSensor (String nodeId, String dataType) {
    	String query = "DELETE FROM sensors where nodeId = ? and dataType = ?";
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
        	logger.error("Error in the deleting query! ", se);
        	success = false;
        }
        
		return success;
    }
    
    public boolean addObservation(String sensor, int value, long timestamp) {
    	String query = "INSERT INTO observations (sensor, value, timestamp) VALUES (?, ?, ?);";
    	boolean success = true;
    	getConnection();
    	try (PreparedStatement ps = conn.prepareStatement(query);) 
    	{
    		ps.setString(1, sensor);
    		ps.setInt(2, value);
    		ps.setLong(3, timestamp);
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
    	String query = "UPDATE temperature SET status=? WHERE nodeId=?;";
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
