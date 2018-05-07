import java.awt.Color;
import java.awt.Graphics;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class MainPage extends Page{
	private Frame f;
	private String[] names;
	private String welcome_message;
	private boolean b;
	
	public MainPage(Frame fr, String[] n){
		f = fr;
		names = n;
		b = false;
	}
	
	public boolean draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString("Stock Exchange Database", 20, 40);

		g.setColor(Color.DARK_GRAY);
		g.setFont(f.text);
		g.drawString(welcome_message, 10, 90);

		g.fillRect(560, Frame.border+10, 90, 30);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString("Log Out", 568, Frame.border+30);

		for(int i = 0; i < names.length; i++) {
			g.setColor(Color.DARK_GRAY);
			g.drawRect(10, Frame.border+60+50*i, Frame.WIDTH-20, 50);
			g.drawString(names[i], 35, Frame.border+90+50*i);
			g.fillRect(560, Frame.border+70+50*i, 90, 30);
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("--Go->", 580, Frame.border+90+50*i);
		}
		return b;
	}
	
	public boolean getUserData() {
		String FName, LName;
		int Admin;
		try {
			String sqlStatement = "SELECT * FROM [User] WHERE Username = '" + Main.username + "'";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			rs.next();
			FName = rs.getString("FName");
			LName = rs.getString("LName");
			Admin = rs.getInt("Admin");
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Failed to fetch user data.");
			ex.printStackTrace();
			welcome_message = "";
			return false;
		}
		welcome_message = "Welcome "+FName+" "+LName;
		if(Admin == 1)
			welcome_message = welcome_message + " [Administrator]";
		else
			welcome_message = welcome_message + " [Standard User]";
		return true;
	}

	public void click(int x, int y){
		if(in(560, Frame.border+10, 90, 30, x, y)) {
			f.page = null;
			b = true;
		}
		for(int i = 0; i < 8 && i < names.length; i++)
			if(in(560, Frame.border+70+50*i, 90, 30, x, y)) {
				f.page = names[i];
				b = true;
			}
	}
	public boolean in(int bx, int by, int bw, int bh, int x, int y) {
		if(x < bx || x > bx + bw)
			return false;
		if(y < by || y > by + bh)
			return false;
		return true;
	}
}
