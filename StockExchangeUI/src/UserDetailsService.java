import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class UserDetailsService {
	private Frame f;

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
		g.drawString("Total Profits: " + NetGain, 50, 160);
		if (Admin == 0)
			g.drawString("Access level: Standard User", 50, 190);
		else
			g.drawString("Access level: Administrator", 50, 190);

		if (listStocks.size() > 0) {
			g.setFont(f.title);
			g.drawString("Stocks", 50, 250);
			g.setFont(f.text);
			for (int i = 0; i < listStocks.size(); i++) {
				g.drawString(listStocks.get(i).get(0), 50, 300 + 25 * i);
				g.drawString(listStocks.get(i).get(1), 100, 300 + 25 * i);
				g.drawString(listStocks.get(i).get(2), 300, 300 + 25 * i);
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
			System.out.println(FName);
			System.out.println(MID);
			System.out.println(LName);
			System.out.println(Email);
			System.out.println(NetGain);
			System.out.println(Admin);
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

}