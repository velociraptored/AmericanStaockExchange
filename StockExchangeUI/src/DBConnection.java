import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
	// Database variables
	private Connection connection = null;
	private String databaseName;
	private String serverName;

	public DBConnection(String serverName, String databaseName) {
		this.serverName = serverName;
		this.databaseName = databaseName;
	}

	// Create connection
	public boolean connect(String user, String pass) {
		String URL = "jdbc:sqlserver://"+serverName+";databaseName="+databaseName+";user="+user+";password={"+pass+"}";
		
		try{
			connection = DriverManager.getConnection(URL);
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public Connection getConnection() {
		return this.connection;
	}
	
	// End the connection
	public void closeConnection() {
		try{
			connection.close();
			System.out.println("Connection ended.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}