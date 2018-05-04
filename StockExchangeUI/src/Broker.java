import java.awt.Color;
import java.awt.Graphics;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class Broker {
	private Frame f;
	private String Username;
	private String BID;
	private String Name;
	private String InterestRate;
	public Broker(Frame fr){
		this.f=fr;
	}
	
	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString(this.Username+"already hired "+this.Name+" ("+this.BID+")", 20, 40);
		
		int gx = 30, gy = 90;
		int gw = 300, gh = 200;
		g.setColor(Color.DARK_GRAY);
		g.drawRect(gx,gy,gw,gh);

		g.setFont(f.title);
		g.drawRect(Frame.WIDTH-330, 320, 300, 210);
		if(this.InterestRate==null){
			g.drawString("Current InterestRate of this broker is not available.", Frame.WIDTH-330+10, 360);
		}else{
			g.drawString(this.InterestRate, Frame.WIDTH-330+10, 360);
		}

		g.setFont(f.text);
	}
	
	public void getBrokerData(){
		try{
			String sqlStatement = "select * from Broker b join Hire h on h.BID=b.BID where h.Username=" + Main.username;
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			
			BID = rs.getString("BID");
			Name = rs.getString("Name");
			InterestRate = rs.getString("InterestRate");
			Username = rs.getString("Username");
			
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Your doesn't hire any broker.");
			ex.printStackTrace();
		}
		
	}

}
