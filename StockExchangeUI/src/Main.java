import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Main {
	public static String username, password;
	private static int admin;
	
	public static void main(String[] args) {
		// Create connection
		DBConnection DBCon = new DBConnection("golem.csse.rose-hulman.edu", "AmericanStockDatabase");
		// Log in to the server
		boolean status = DBCon.connect("StocksUser", "Password123");
		if(status){
			//JOptionPane.showMessageDialog(null, "Database successfully connected.");
			// Log in to the database
			/*while(!login(DBCon));*/admin = 1;
			// Begin program
			Frame f = new Frame(DBCon);
			if(admin == 1)
				f.setAdmin(true);
			f.run();
		}else{
			JOptionPane.showMessageDialog(null, "Failed to connect to database.");
		}
	}

	public static boolean login(DBConnection DBCon){
		String format = "SELECT Password,Admin \nFROM [User]\nWHERE Username = ?";
		JTextField user_field = new JTextField();
		JPasswordField pass_field = new JPasswordField();
		Object[] message = {
				"Username:", user_field,
				"Password:", pass_field
		};
		if(username != null)
			user_field.setText(username);
		int option = JOptionPane.showConfirmDialog(null, message, "Please log in.", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION){
			DBCon.closeConnection();
			System.exit(0);
		}
		username = user_field.getText();
		try {
			PreparedStatement stmt = DBCon.getConnection().prepareStatement(format);
			stmt.setString(1, username);

			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				admin = rs.getInt(rs.findColumn("Admin"));
				String pass = rs.getString(rs.findColumn("Password"));
				if(Arrays.equals(pass.toCharArray(), pass_field.getPassword())){
					password = pass;
					return true;
				}
			}
			JOptionPane.showMessageDialog(null, "Username or password incorrect.");
			return false;
		}
		catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to receive login info.");
			ex.printStackTrace();
			return false;
		}
	}
}