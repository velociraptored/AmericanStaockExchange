import java.awt.Color;
import java.awt.Graphics;
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
	private String NetGain;
	private String Admin;
	
	public UserDetailsService(Frame fr){
		this.f =fr;	
	}
	
	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString(this.Username+" ("+FName+ " " + MID + " " + LName+")", 20, 40);

		int gx = 30, gy = 90;
		int gw = 300, gh = 200;
		g.setColor(Color.DARK_GRAY);
		g.drawRect(gx,gy,gw,gh);
	
		g.setFont(f.title);
		g.drawRect(Frame.WIDTH-330, 320, 300, 210);
		g.drawString(Email, Frame.WIDTH-330+10, 360);
		g.setFont(f.text);
		g.drawString(Email, Frame.WIDTH-330+10, 400);
		g.drawString("Total Profits: "+NetGain, Frame.WIDTH-330+10, 430);
		if(Admin.equals(0))
			g.drawString("Access level: Standard User", Frame.WIDTH-330+10, 460);
		else
			g.drawString("Access level: Administrator", Frame.WIDTH-330+10, 460);
	}
	
	public void getUserData(){
		try{
			String sqlStatement = "SELECT * FROM User WHERE Username = " + Main.username;
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			
			FName = rs.getString("FName");
			MID = rs.getString("MID");
			LName = rs.getString("LName");
			Email = rs.getString("Email");
			NetGain = rs.getString("NetGain");
			Admin = rs.getString("Admin");
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to fetch company data.");
			ex.printStackTrace();
		}
		
		try{
			String sqlStatement = "SELECT * FROM UserStocks WHERE Username = " + Main.username;
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			
			while(rs.next()){
				
			}
			
			FName = rs.getString("FName");
			MID = rs.getString("MID");
			LName = rs.getString("LName");
			Email = rs.getString("Email");
			NetGain = rs.getString("NetGain");
			Admin = rs.getString("Admin");
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to fetch company data.");
			ex.printStackTrace();
		}
	}
	
	
}
