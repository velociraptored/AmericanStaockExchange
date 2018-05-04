import java.awt.Color;
import java.awt.Graphics;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class AdminHomepage {
	public Frame f;
	
	private String Username;
	private String FName;
	private String MID;
	private String LName;
	
	ArrayList<ArrayList<String>> listUsers = new ArrayList<ArrayList<String>>();

	
	public AdminHomepage(Frame fr) {
		this.f = fr;
	}
	
	public void draw_page(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT - Frame.border);

		g.setFont(f.title);
		g.drawString(FName + " " + MID + " " + LName, 20, 40);

		// g.setFont(f.title);
		// g.drawRect(Frame.WIDTH - 330, 320, 300, 210);
		// g.drawString(Email, Frame.WIDTH - 330 + 10, 360);
		g.setFont(f.text);
		if (listUsers.size() > 0) {
			g.setFont(f.title);
			g.drawString("Users", Frame.WIDTH - 450, 380);
			g.setFont(f.text);
			// g.drawLine(x1, y1, x2, y2);
			for (int i = 0; i < listUsers.size(); i++) {
				g.drawString(listUsers.get(i).get(0), Frame.WIDTH - 650, 420 + 25 * i);
				g.drawString(listUsers.get(i).get(1), Frame.WIDTH - 600, 420 + 25 * i);
				g.drawString(listUsers.get(i).get(2), Frame.WIDTH - 500, 420 + 25 * i);
				g.drawString(listUsers.get(i).get(3), Frame.WIDTH - 450, 420 + 25 * i);
				g.drawString(listUsers.get(i).get(4), Frame.WIDTH - 400, 420 + 25 * i);
				g.drawString(listUsers.get(i).get(5), Frame.WIDTH - 350, 420 + 25 * i);
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
				users.add(""+rs.getInt("NetGain"));
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
		JTextField f2 = new JTextField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		JTextField f5 = new JTextField();
		JTextField f6 = new JTextField();
		Object[] message = { "Username:", f1, "Password:", f2, "First Name:", f3, "Middle Initial:", f4, "Last Name:",
				f5, "Email:", f6, };
		int option = JOptionPane.showConfirmDialog(null, message, "Insert User Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			insertUser(f1.getText(), f2.getText(), f3.getText(), f4.getText(), f5.getText(), f6.getText());
	}

	private void insertUser(String username, String password, String fname, String mid, String lname, String email) {
		try {
			String sqlStatement = "{ ? = call InsertUser(?,?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, username);
			proc.setString(3, password);
			proc.setString(4, fname);
			proc.setString(5, mid);
			proc.setString(6, lname);
			proc.setString(7, email);
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

	public void requestUpdate() {
		JTextField f1 = new JTextField();
		JTextField f2 = new JTextField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		JTextField f5 = new JTextField();
		JTextField f6 = new JTextField();
		Object[] message = { "Username:", f1, "Password:", f2, "First Name:", f3, "Middle Initial:", f4, "Last Name:",
				f5, "Email:", f6, };
		int option = JOptionPane.showConfirmDialog(null, message, "Update User Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			updateUser(f1.getText(), f2.getText(), f3.getText(), f4.getText(), f5.getText(), f6.getText());
	}

	private void updateUser(String username, String password, String fname, String mid, String lname, String email) {
		try {
			String sqlStatement = "{ ? = call updateUser(?,?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, username);
			proc.setString(3, password);
			proc.setString(4, fname);
			proc.setString(5, mid);
			proc.setString(6, lname);
			proc.setString(7, email);
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
		JTextField f2 = new JTextField();
		Object[] message = { "Username:", f1, "Password", f2};
		int option = JOptionPane.showConfirmDialog(null, message, "Delete User Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			deleteUser(f1.getText(), f2.getText());
	}

	private void deleteUser(String username, String password) {
		try {
			String sqlStatement = "{ ? = call deleteUser(?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, username);
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
}
