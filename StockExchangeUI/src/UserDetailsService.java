import java.awt.Color;
import java.awt.Graphics;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class UserDetailsService extends Page {
	private Frame f;

	private static final Random RANDOM = new SecureRandom();
	private static final Base64.Encoder enc = Base64.getEncoder();

	private String Username;
	private String FName;
	private String MID;
	private String LName;
	private String Email;
	private int NetGain;
	private int Admin;

	ArrayList<ArrayList<String>> listStocks = new ArrayList<ArrayList<String>>();

	public UserDetailsService(Frame fr) {
		this.f = fr;
	}

	public void draw_page(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT - Frame.border);

		g.setFont(f.title);
		g.drawString(FName + " " + MID + " " + LName, 20, 40);

		g.setColor(Color.DARK_GRAY);
		g.setFont(f.text);
		if (Email == null) {
			Email = "";
		}
		g.drawString(Username, 50, 100);
		g.drawString(Email, 50, 130);
		g.drawString("Total Profits: $" + NetGain, 50, 160);
		if (Admin == 0)
			g.drawString("Access level: Standard User", 50, 190);
		else
			g.drawString("Access level: Administrator", 50, 190);

		g.drawRect(Frame.WIDTH - 150, 80, 80, 20);
		g.fillRect(Frame.WIDTH - 150, 80, 80, 20);
		g.drawRect(Frame.WIDTH - 250, 80, 80, 20);
		g.fillRect(Frame.WIDTH - 250, 80, 80, 20);

		g.setColor(Color.LIGHT_GRAY);
		g.drawString("Delete", Frame.WIDTH - 137, 95);
		g.drawString("Edit", Frame.WIDTH - 225, 95);

		
		g.setColor(Color.DARK_GRAY);
		if (listStocks.size() > 0) {
			g.setFont(f.title);
			g.drawString("Stocks", 50, 300);
			g.setFont(f.text);
			g.drawString("Abbr.", 50, 350);
			g.drawString("Company", 150, 350);
			g.drawString("Quantity", 350, 350);
			for (int i = 0; i < listStocks.size(); i++) {
				g.drawString(listStocks.get(i).get(0), 50, 380 + 25 * i);
				g.drawString(listStocks.get(i).get(1), 150, 380 + 25 * i);
				g.drawString(listStocks.get(i).get(2), 350, 380 + 25 * i);
			}
		}

	}

	public boolean getUserData() {
		Username = Main.username;
		try {
			String sqlStatement = "SELECT * FROM [User] WHERE Username = '" + Username + "'";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			rs.next();
			FName = rs.getString("FName");
			MID = rs.getString("MID");
			LName = rs.getString("LName");
			Email = rs.getString("Email");
			NetGain = rs.getInt("NetGain");
			Admin = rs.getInt("Admin");
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to fetch user data.");
			ex.printStackTrace();
			return false;
		}

		try {
			String sqlStatement = "SELECT * FROM UserStocks JOIN Company ON CompanyAbbreviation = Abbreviation "
					+ "WHERE Username = '" + Username + "'";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			listStocks.clear();
			while (rs.next()) {
				ArrayList<String> stocks = new ArrayList<String>();
				stocks.add(rs.getString("CompanyAbbreviation"));
				stocks.add(rs.getString("Name"));
				stocks.add(rs.getString("Quantity"));
				listStocks.add(stocks);
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to fetch user data.");
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public void requestUpdate() {
		JPasswordField f2 = new JPasswordField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		JTextField f5 = new JTextField();
		JTextField f6 = new JTextField();
		f3.setText(FName);
		f4.setText(MID);
		f5.setText(LName);
		f6.setText(Email);

		Object[] message = { Username, "Password:", f2, "First Name:", f3, "Middle Initial:", f4, "Last Name:", f5, "Email:",
				f6, };
		int option = JOptionPane.showConfirmDialog(null, message, "Update User Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			updateUser(f2.getPassword().toString(), f3.getText(), f4.getText(), f5.getText(), f6.getText());
	}

	private void updateUser(String password, String fname, String mid, String lname, String email) {
		byte[] salt = getNewSalt();
		String hash = hashPassword(salt, password);
		try {
			String sqlStatement = "{ ? = call updateUser(?,?,?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Username);
			proc.setString(3, getStringFromBytes(salt));
			proc.setString(4, hash);
			proc.setString(5, fname);
			proc.setString(6, mid);
			proc.setString(7, lname);
			proc.setString(8, email);
			proc.execute();

			int status = proc.getInt(1);
			if (status == 1) {
				JOptionPane.showMessageDialog(null, "ERROR: User does not exist in the database.");
			} else {
				JOptionPane.showMessageDialog(null, "User updated.");
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}

	public void requestDelete() {
		JTextField f1 = new JTextField();
		Object[] message = { Username, "Password", f1 };
		int option = JOptionPane.showConfirmDialog(null, message, "Delete User Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			deleteUser(f1.getText());
	}

	private void deleteUser(String password) {
		try {
			String sqlStatement = "{ ? = call deleteUser(?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Username);
			proc.setString(3, password);
			proc.execute();

			int status = proc.getInt(1);
			if (status == 1) {
				JOptionPane.showMessageDialog(null, "ERROR: User does not exist in the database.");
			} else {
				JOptionPane.showMessageDialog(null, "User deleted.");
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}

	@Override
	public void click(int x, int y) {
		if (in(Frame.WIDTH - 150, 80, 80, 20, x, y)) {
			requestDelete();
		} else if (in(Frame.WIDTH - 250, 80, 80, 20, x, y)) {
			requestUpdate();
		}
	}

	public boolean in(int bx, int by, int bw, int bh, int x, int y) {
		if (x < bx || x > bx + bw)
			return false;
		if (y < by || y > by + bh)
			return false;
		return true;
	}

	public byte[] getNewSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return salt;
	}

	public String getStringFromBytes(byte[] data) {
		return enc.encodeToString(data);
	}

	public String hashPassword(byte[] salt, String password) {

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

}