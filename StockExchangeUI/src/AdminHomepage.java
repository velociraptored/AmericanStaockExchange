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

public class AdminHomepage extends Page {
	public Frame f;

	private static final Random RANDOM = new SecureRandom();
	private static final Base64.Encoder enc = Base64.getEncoder();

	private String Username;
	private String FName;
	private String MID;
	private String LName;
	private int page = 1;
	private int lastPage = 0;
	private Graphics gr;
	ArrayList<ArrayList<String>> listUsers = new ArrayList<ArrayList<String>>();

	public AdminHomepage(Frame fr) {
		this.f = fr;
	}

	public void draw_page(Graphics g) {
		getData();
		gr = g;
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT - Frame.border);

		g.setFont(f.title);
		g.drawString("Users", 20, 40);

		g.drawString(FName + " " + MID + " " + LName, Frame.WIDTH / 2, 40);

		g.setColor(Color.DARK_GRAY);
		g.setFont(f.text);
		g.drawRect(Frame.WIDTH - 150, 80, 80, 20);
		g.fillRect(Frame.WIDTH - 150, 80, 80, 20);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString("Add User", Frame.WIDTH - 148, 95);
		g.setFont(f.smallText);

		g.setColor(Color.DARK_GRAY);

		if (listUsers.size() > 0) {
			g.setFont(f.text);
			// g.drawLine(x1, y1, x2, y2);
			if (listUsers.size() > 15) {
				lastPage = (int) Math.ceil(((double) listUsers.size() / 15));
				g.drawString("Page " + page + " of " + lastPage, Frame.WIDTH - 600, 100);
				if (page > 1) {
					g.setColor(Color.DARK_GRAY);
					g.drawRect(Frame.WIDTH - 650, 80, 15, 15);
					g.fillRect(Frame.WIDTH - 650, 80, 15, 15);
					g.setColor(Color.LIGHT_GRAY);
					g.drawString("<", Frame.WIDTH - 650, 95);
				}
				if (page < lastPage) {
					g.setColor(Color.DARK_GRAY);
					g.drawRect(Frame.WIDTH - 500, 80, 15, 15);
					g.fillRect(Frame.WIDTH - 500, 80, 15, 15);
					g.setColor(Color.LIGHT_GRAY);
					g.drawString(">", Frame.WIDTH - 500, 95);
				}
				for (int i = 0; i < Math.min(15, listUsers.size() - 15 * (page - 1)); i++) {
					g.setFont(f.smallText);
					g.setColor(Color.DARK_GRAY);
					String name = listUsers.get(i + (page - 1) * 15).get(3) + ", "
							+ listUsers.get(i + (page - 1) * 15).get(1) + " "
							+ listUsers.get(i + (page - 1) * 15).get(2);
					g.drawString(listUsers.get(i + (page - 1) * 15).get(0), Frame.WIDTH - 650, 140 + 25 * i);
					g.drawString(name, Frame.WIDTH - 550, 140 + 25 * i);
					g.drawString(listUsers.get(i + (page - 1) * 15).get(4), Frame.WIDTH - 350, 140 + 25 * i);
					g.drawRect(Frame.WIDTH - 150, 125 + 25 * i, 60, 20);
					g.drawRect(Frame.WIDTH - 75, 125 + 25 * i, 60, 20);
					g.fillRect(Frame.WIDTH - 150, 125 + 25 * i, 60, 20);
					g.fillRect(Frame.WIDTH - 75, 125 + 25 * i, 60, 20);
					g.setColor(Color.LIGHT_GRAY);
					g.setFont(f.text);
					g.drawString("Edit", Frame.WIDTH - 138, 140 + 25 * i);
					g.drawString("Delete", Frame.WIDTH - 71, 140 + 25 * i);
				}
			} else {
				for (int i = 0; i < listUsers.size(); i++) {
					g.setFont(f.smallText);
					g.setColor(Color.DARK_GRAY);
					String name = listUsers.get(i).get(3) + ", " + listUsers.get(i).get(1) + " "
							+ listUsers.get(i).get(2);
					g.drawString(listUsers.get(i).get(0), Frame.WIDTH - 650, 140 + 25 * i);
					g.drawString(name, Frame.WIDTH - 550, 140 + 25 * i);
					g.drawString(listUsers.get(i).get(4), Frame.WIDTH - 350, 140 + 25 * i);
					g.drawRect(Frame.WIDTH - 150, 125 + 25 * i, 60, 20);
					g.drawRect(Frame.WIDTH - 75, 125 + 25 * i, 60, 20);
					g.fillRect(Frame.WIDTH - 150, 125 + 25 * i, 60, 20);
					g.fillRect(Frame.WIDTH - 75, 125 + 25 * i, 60, 20);
					g.setColor(Color.LIGHT_GRAY);
					g.setFont(f.text);
					g.drawString("Edit", Frame.WIDTH - 138, 140 + 25 * i);
					g.drawString("Delete", Frame.WIDTH - 71, 140 + 25 * i);
				}
			}
		}
	}

	public boolean getData() {
		Username = Main.username;
		try {
			String sqlStatement = "SELECT * FROM [User] WHERE Username = '" + Username + "'";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			rs.next();
			FName = rs.getString("FName");
			MID = rs.getString("MID");
			LName = rs.getString("LName");
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to fetch user data.");
			ex.printStackTrace();
			return false;
		}

		try {
			String sqlStatement = "SELECT * FROM [User]";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			listUsers.clear();
			while (rs.next()) {
				ArrayList<String> users = new ArrayList<String>();
				users.add(rs.getString("Username"));
				users.add(rs.getString("FName"));
				users.add(rs.getString("MID"));
				users.add(rs.getString("LName"));
				users.add(rs.getString("Email"));
				users.add("" + rs.getInt("NetGain"));
				listUsers.add(users);
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to fetch user data.");
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public void requestInsert() {
		JTextField f1 = new JTextField();
		JPasswordField f2 = new JPasswordField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		JTextField f5 = new JTextField();
		JTextField f6 = new JTextField();
		Object[] message = { "Username:", f1, "Password:", f2, "First Name:", f3, "Middle Initial:", f4, "Last Name:",
				f5, "Email:", f6, };
		int option = JOptionPane.showConfirmDialog(null, message, "Insert User Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			insertUser(f1.getText(), f2.getPassword().toString(), f3.getText(), f4.getText(), f5.getText(),
					f6.getText());
	}

	private void insertUser(String username, String password, String fname, String mid, String lname, String email) {
		byte[] salt;
		String hash;		
		if(password.length()>0){
			salt = getNewSalt();
			hash = hashPassword(salt, password);
		}else{
			salt = null;
			hash = "";
		}
		try {
			String sqlStatement = "{ ? = call InsertUser(?,?,?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, username);
			proc.setString(3, hash);
			proc.setString(4, getStringFromBytes(salt));
			proc.setString(5, fname);
			proc.setString(6, mid);
			proc.setString(7, lname);
			proc.setString(8, email);
			proc.execute();

			int status = proc.getInt(1);
			if (status == 1) {
				JOptionPane.showMessageDialog(null, "ERROR: User already exists in the database.");
			} else {
				JOptionPane.showMessageDialog(null, "User inserted.");
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}

	public void requestUpdate(String username) {
		JPasswordField f2 = new JPasswordField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		JTextField f5 = new JTextField();
		JTextField f6 = new JTextField();

		f2.requestFocus();
		String sqlStatement = "SELECT * FROM [User] WHERE [username] = '" + username + "'";
		PreparedStatement proc;
		try {
			proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			rs.next();
			f3.setText(rs.getString("FName"));
			f4.setText(rs.getString("MID"));
			f5.setText(rs.getString("LName"));
			f6.setText(rs.getString("Email"));

		} catch (SQLException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

		Object[] message = { username, "Password:", f2, "First Name:", f3, "Middle Initial:", f4, "Last Name:", f5,
				"Email:", f6, };
		int option = JOptionPane.showConfirmDialog(null, message, "Update User Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			updateUser(username, f2.getPassword().toString(), f3.getText(), f4.getText(), f5.getText(), f6.getText());
	}

	private void updateUser(String username, String password, String fname, String mid, String lname, String email) {
		byte[] salt;
		String hash;		
		if(password.length()>0){
			salt = getNewSalt();
			hash = hashPassword(salt, password);
		}else{
			salt = null;
			hash = "";
		}
		System.out.println(hash);
		System.out.println(getStringFromBytes(salt));

		try {
			String sqlStatement = "{ ? = call editUser(?,?,?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, username);
			proc.setString(3, hash);
			proc.setBytes(4, salt);
			proc.setString(5, fname);
			proc.setString(6, mid);
			proc.setString(7, lname);
			proc.setString(8, email);
			proc.execute();

			int status = proc.getInt(1);
			// if (status == 1) {
			// JOptionPane.showMessageDialog(null, "ERROR: User does not exist
			// in the database.");
			// } else {
			// JOptionPane.showMessageDialog(null, "User updated.");
			// }
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}

	public void requestDelete(String username) {
		JPasswordField f1 = new JPasswordField();
		Object[] message = { username, "Password", f1 };
		int option = JOptionPane.showConfirmDialog(null, message, "Delete User Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			deleteUser(username, f1.getPassword().toString());
	}

	private void deleteUser(String username, String password) {
		try {

			String sqlStatement2 = "SELECT [Password], Salt FROM [User] WHERE Username = '" + username + "'";
			PreparedStatement proc2 = f.DBCon.getConnection().prepareStatement(sqlStatement2);
			ResultSet rs = proc2.executeQuery();
			rs.next();
			String realHash = rs.getString("Password");
			byte[] salt = rs.getBytes("Salt");
			System.out.println(realHash);
			System.out.println(getStringFromBytes(salt));
			String testHash = hashPassword(salt, password);
			System.out.println(testHash);

			if (realHash.equals(testHash)) {
				String sqlStatement = "{ ? = call deleteUser(?) }";
				CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
				proc.registerOutParameter(1, Types.INTEGER);
				proc.setString(2, username);
				proc.execute();
				int status = proc.getInt(1);
				if (status == 1) {
					JOptionPane.showMessageDialog(null, "ERROR: User does not exist in the database.");
				} else {
					JOptionPane.showMessageDialog(null, "User deleted.");
				}
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: Incorrect Password.");
			}

		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}

	public void decrementPage() {
		if (page > 1)
			page--;
		draw_page(gr);
	}

	public void incrementPage() {
		if (page < lastPage)
			page++;
		draw_page(gr);
	}

	@Override
	public void click(int x, int y) {
		if (in(Frame.WIDTH - 650, 80, 15, 15, x, y)) {
			decrementPage();
		} else if (in(Frame.WIDTH - 500, 80, 15, 15, x, y)) {
			incrementPage();
		} else if (in(Frame.WIDTH - 150, 80, 60, 20, x, y)) {
			requestInsert();
		} else {
			for (int i = 0; i < Math.min(15, listUsers.size() - 15 * (page - 1)); i++) {
				if (in(Frame.WIDTH - 150, 125 + 25 * i, 60, 20, x, y)) {
					requestUpdate(listUsers.get(i + (page - 1) * 15).get(0));
				} else if (in(Frame.WIDTH - 75, 125 + 25 * i, 60, 20, x, y)) {
					requestDelete(listUsers.get(i + (page - 1) * 15).get(0));
				}
			}
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
