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

public class Broker extends Page {
	private Frame f;
	private String Username;
	private String BID;
	private String Name;
	private String InterestRate;
	private ArrayList<ArrayList<String>> BrokerData;
	private ArrayList<ArrayList<String>> BrokerPart;
	
	public Broker(Frame fr){
		this.f=fr;
	}
	
	public void draw_page(Graphics g){
		if(Username==null){
			g.setColor(Color.lightGray);
			g.setFont(f.title);
			g.drawString("Broker Details", 20, 40);
			g.drawLine(20, 60, 650, 60);
			g.setFont(f.text);
			g.drawString("You don't hire any broker.", 20, 100);
			g.drawString("You can hire another broker.", 20, 140);
			g.drawString("Here is the Broker Information:", 20, 180);
			g.drawString("BID      Name           InterestRate", 20, 220);
			for(int i=0;i<BrokerData.size();i++){
				g.drawString(BrokerData.get(i).get(0)+"   "+BrokerData.get(i).get(1)+"   "+
						BrokerData.get(i).get(2),20,260+i*40);
				g.drawRect(600, 240+i*40, 60, 20);
				g.drawString("HIRE", 610, 257+i*40);
			}
			
		}else{
			g.setColor(Color.lightGray);
			g.setFont(f.title);
			g.drawString("Broker Details", 20, 40);
			g.drawLine(20, 60, 650, 60);
			g.setFont(f.text);
			g.drawString("You("+Username+") have already hired "+Name+".", 20, 100);
			g.drawString("The BID of "+Name+" is "+this.BID, 20, 140);
			g.drawString("You can fire current broker.", 20, 180);
			g.drawRect(600, 160, 60, 20);
			g.drawString("FIRE",610,177);
			g.drawString("You can hire new broker.", 20, 220);
			for(int i=0;i<BrokerPart.size();i++){
				g.drawString(BrokerPart.get(i).get(0)+"   "+BrokerPart.get(i).get(1)+"   "+
						BrokerPart.get(i).get(2),20,260+i*40);
				g.drawRect(600, 240+i*40, 60, 20);
				g.drawString("HIRE", 610, 257+i*40);
			
			}
		}
		
	}
	
	public void getAllBroker(){
		try{
			BrokerData=new ArrayList<ArrayList<String>>();
			String sqlStatement = "SELECT * FROM Broker";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			while(rs.next()){
				ArrayList<String> brok = new ArrayList<String>();
				BID = rs.getString(1);
				Name = rs.getString(2);
				InterestRate = rs.getString(3);
				brok.add(BID);
				brok.add(Name);
				brok.add(InterestRate);
				BrokerData.add(brok);
			}
			
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Your don't hire any broker yet.");
			ex.printStackTrace();
		}
		
	}
	
	public void getPartBroker(){
		try{
			BrokerPart=new ArrayList<ArrayList<String>>();
			String sqlStatement = "SELECT distinct b.BID, b.Name, b.InterestRate "
					+ "FROM Broker b RIGHT join Hire h on h.BID=b.BID Where b.BID!=(SELECT BID FROM HIRE WHERE Username='"+Main.username+"')";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			while(rs.next()){
				ArrayList<String> brok = new ArrayList<String>();
				BID = rs.getString(1);
				Name = rs.getString(2);
				InterestRate = rs.getString(3);
				brok.add(BID);
				brok.add(Name);
				brok.add(InterestRate);
				BrokerPart.add(brok);
			}
			
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Your don't hire any broker yet.");
			ex.printStackTrace();
		}
		
		
	}
	
	public void getBrokerData(){
		try{
			String sqlStatement = "SELECT b.BID, b.Name, b.InterestRate,h.Username FROM Broker b join Hire h on h.BID=b.BID WHERE h.Username= '" + Main.username + "'" ;
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			if(rs.next()){
				BID = rs.getString(1);
				Name = rs.getString(2);
				InterestRate = rs.getString(3);
				Username = rs.getString(4);
			}

		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Your don't hire any broker yet.");
			ex.printStackTrace();
		}
		
	}
	
	public void click(int x, int y){
		if(in(600,160,60,20,x,y)){
			fire();
		}
		if(Username==null){
			for(int i=0;i<BrokerData.size();i++){
				if(in(600, 240+i*40, 60, 20,x,y))
					hire();
			}
		}else{
			for(int i=0;i<BrokerPart.size();i++){
				if(in(600, 240+i*40, 60, 20,x,y))
					hire();
			}
		}
		
	}
	public boolean in(int bx, int by, int bw, int bh, int x, int y) {
		if(x < bx || x > bx + bw)
			return false;
		if(y < by || y > by + bh)
			return false;
		return true;
	}
	
	public void hire(){
		JTextField f1 = new JTextField();
		JTextField f2 = new JTextField();
		
		Object[] message = {
				"Username:", f1,
				"BID:", f2,
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Hire current broker", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION){
			try{
				hireBroker(f1.getText(),f2.getText());
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(null, "Year must be an integer.");
			}
			getPartBroker();
			getAllBroker();
			getBrokerData();
		} 
		
	}

	public void fire(){
		JTextField f1 = new JTextField();
		JTextField f2 = new JTextField();
		
		Object[] message = {
				"Username:", f1,
				"BID:", f2,
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Fire current broker", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION){
			try{
				fireBroker(f1.getText(),f2.getText());
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(null, "Year must be an integer.");
			}
			getPartBroker();
			getAllBroker();
			getBrokerData();
		}
		
	}
	
	public void hireBroker(String Username,String BID){
		try{
			String sqlStatement = "{ ? = call HireBroker(?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Username);
			proc.setString(3, BID);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Broker already hired in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Broker hired.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}
	
	public void fireBroker(String Username,String BID){
		try{
			String sqlStatement = "{ ? = call FireBroker(?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Username);
			proc.setString(3, BID);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Broker already fired in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Broker fired.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}
	

}
