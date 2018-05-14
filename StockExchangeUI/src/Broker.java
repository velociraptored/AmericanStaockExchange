import java.awt.Color;
import java.awt.Graphics;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class Broker extends Page {
	private Frame f;
	private ArrayList<Integer> BIDs;
	private ArrayList<String> Names;
	private ArrayList<String> InterestRates;
	private ArrayList<Integer> HiredBrokers;
	public Broker(Frame fr){this.f=fr;}

	public void draw_page(Graphics g){
		g.setColor(Color.lightGray);
		g.setFont(f.title);
		g.drawString("Broker Details", 20, 40);
		g.drawLine(20, 60, 650, 60);
		g.setFont(f.text);
		g.drawString("You are able to hire or fire brokers from this screen.", 20, 100);
		g.drawString("Broker Information:", 20, 180);
		g.drawString("BID", 20, 220);
		g.drawString("Name", 75, 220);
		g.drawString("InterestRate", 340, 220);
		for(int i=0;i<BIDs.size();i++){
			g.drawString(""+BIDs.get(i), 20, 260+i*40);
			g.drawString(InterestRates.get(i), 340, 260+i*40);
			g.drawRect(600, 243+i*40, 60, 20);
			if(HiredBrokers.indexOf(BIDs.get(i))==-1) {
				g.drawString(Names.get(i), 75, 260+i*40);
				g.drawString("HIRE", 610, 260+i*40);
			} else {
				g.drawString(Names.get(i)+" [Hired]", 75, 260+i*40);
				g.drawString("FIRE", 610, 260+i*40);
			}
		}
	}

	public void getAllBroker(){
		try{
			BIDs = new ArrayList<Integer>();
			Names = new ArrayList<String>();
			InterestRates = new ArrayList<String>();
			String sqlStatement = "SELECT * FROM Broker";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			while(rs.next()){
				BIDs.add(rs.getInt(1));
				Names.add(rs.getString(2));
				InterestRates.add(rs.getString(3));
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to retrieve Broker data.");
			ex.printStackTrace();
		}
	}

	public void getHiredBrokers(){
		HiredBrokers = new ArrayList<Integer>();
		try{
			String sqlStatement = "SELECT BID FROM Hire WHERE Username = '"+Main.username+"'";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			int BIDcol = rs.findColumn("BID");
			while(rs.next())
				HiredBrokers.add(rs.getInt(BIDcol));
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to retrieve hired brokers.");
			ex.printStackTrace();
		}
	}

	public void click(int x, int y){
		for(int i=0;i<BIDs.size();i++)
			if(in(600, 243+i*40, 60, 20,x,y)) {
				if(HiredBrokers.indexOf(BIDs.get(i))==-1)
					hireBroker(Main.username, BIDs.get(i));
				else
					fireBroker(Main.username, BIDs.get(i));
			}
	}
	public boolean in(int bx, int by, int bw, int bh, int x, int y) {
		if(x < bx || x > bx + bw)
			return false;
		if(y < by || y > by + bh)
			return false;
		return true;
	}

	public void hireBroker(String Username,int BID){
		try{
			String sqlStatement = "{ ? = call HireBroker(?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Username);
			proc.setInt(3, BID);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Broker already hired in the database.");
			}

		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
		getAllBroker();
		getHiredBrokers();
	}

	public void fireBroker(String Username,int BID){
		try{
			String sqlStatement = "{ ? = call FireBroker(?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement); 
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Username);
			proc.setInt(3, BID);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Broker already fired in the database.");
			}

		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
		getAllBroker();
		getHiredBrokers();
	}
}