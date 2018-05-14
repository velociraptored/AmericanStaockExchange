import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Main {
	public static String username, password;
	private static int admin;
	private static final Base64.Encoder enc = Base64.getEncoder();

	public static void main(String[] args) {
		// Create connection
		DBConnection DBCon = new DBConnection("golem.csse.rose-hulman.edu", "AmericanStockDatabase");
		// Log in to the server
		boolean status = DBCon.connect("StocksUser", "Password123");
		if(status){
			//JOptionPane.showMessageDialog(null, "Database successfully connected.");
			// Log in to the database
			while(!login(DBCon));
			//admin = 1;
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
		String format = "SELECT Password,Salt,Admin \nFROM [User]\nWHERE Username = ?";
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
		password = pass_field.getPassword().toString();
		try {
			PreparedStatement stmt = DBCon.getConnection().prepareStatement(format);
			stmt.setString(1, username);

			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				admin = rs.getInt(rs.findColumn("Admin"));
				String pass = rs.getString(rs.findColumn("Password"));
				String salt = rs.getString(rs.findColumn("Salt"));
				
				String passHash = hashPassword(salt.getBytes(), password);
				
				if(passHash.equals(pass)){
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
	
	public static String hashPassword(byte[] salt, String password) {

		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKeyFactory f;
		byte[] hash = null;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException e) {
			JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
			e.printStackTrace();
		}
		return getStringFromBytes(hash);
	}

	public static String getStringFromBytes(byte[] data) {
		return enc.encodeToString(data);
	}

	
}