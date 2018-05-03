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

public class Transaction {
	/*private String TID;
	private String TimeStamp;
	private String CompanyAbbreviation;
	private String Type;
	private String Price;
	private String Quantity;*/
	
	private int TransactionNumber;
	private Frame f;
	private ArrayList<ArrayList<String>> TransactionData;
	
	public Transaction(Frame fr){
		f = fr;
	}
	
	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);

		int gx = 30, gy = 90;
		int gw = 300, gh = 200;
		g.setColor(Color.DARK_GRAY);
		g.drawRect(gx,gy,gw,gh);

		g.setFont(f.title);
		g.drawRect(Frame.WIDTH-330, 320, 300, 210);
		g.setFont(f.text);
	}
	
	public void getTransactionData(){
		try{
			String sqlStatement = "SELECT * FROM [Transaction] WHERE [User] = '" 
									+ Main.username + "' ORDER BY TimeStamp DESC";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			int TID = 					rs.findColumn("TID");
			int TimeStamp =				rs.findColumn("TimeStamp");//date
			int CompanyAbbreviation = 	rs.findColumn("CompanyAbbreviation");
			int Type =					rs.findColumn("Type");
			int Price = 				rs.findColumn("Price");
			int Quantity = 				rs.findColumn("Quantity");
			ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
			while(rs.next()){
				ArrayList<String> transaction = new ArrayList<String>();
				transaction.add(rs.getString(TID));
				transaction.add(rs.getString(TimeStamp));
				transaction.add(rs.getString(CompanyAbbreviation));
				transaction.add(rs.getString(Type));
				transaction.add(rs.getString(Price));
				transaction.add(rs.getString(Quantity));
				ret.add(transaction);
			}
			TransactionData = ret;
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to fetch transaction data.");
			ex.printStackTrace();
		}
	}
	
	public boolean getData(){
		getTransactionData();
		String[] names = new String[TransactionData.size()];
		for(int i = 0; i < TransactionData.size(); i++){
			/*"BUY 3 APPL stocks at $4/stock"*/
			/*TODO: change stocks to stock if only 1 stock exchanged*/
			names[i] = TransactionData.get(i).get(3)+" "+TransactionData.get(i).get(5)+" "+
						TransactionData.get(i).get(2)+" stocks at $"+TransactionData.get(i).get(4)+
						"/stock ";
		}
		String s;
		if(names.length != 0){
			s = (String) JOptionPane.showInputDialog(null, "Choose a transaction to view or edit.",
				"Transaction Selection", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
		} else {
			s = null;
			JOptionPane.showMessageDialog(null, "You haven't made any transactions yet.");
		}
		if(s == null){
			return false;
		}
		/*TID = s.substring(s.indexOf('(')+1, s.indexOf(')'));
		for(int i = 0; i < TransactionData.size(); i++)
			if(TransactionData.get(i).get(0).equals(TID)){
				TransactionNumber = i;
				break;
			}//does this work for tid?
		TimeStamp = TransactionData.get(TransactionNumber).get(1);
		CompanyAbbreviation = TransactionData.get(TransactionNumber).get(2);
		Type = TransactionData.get(TransactionNumber).get(3);
		Price = TransactionData.get(TransactionNumber).get(4);
		Quantity = TransactionData.get(TransactionNumber).get(5);*/

		/*try{
			String sqlStatement = "{ ? = call GetStockHistory(?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Abbreviation);
			proc.execute();
			ResultSet rs = proc.getResultSet();

			OpenDate.clear();
			OpenPrice.clear();
			HighPrice.clear();
			LowPrice.clear();
			ClosePrice.clear();
			AdjClose.clear();
			Volume.clear();
			if(rs == null){
				int ret = proc.getInt(1);
				if(ret == 1)
					JOptionPane.showMessageDialog(null,"ERROR: Company does not exist");
			}else{
				MinVal = Double.MAX_VALUE;
				MaxVal = Double.MIN_VALUE;
				int ODI = rs.findColumn("OpenDate");
				int OPI = rs.findColumn("OpenPrice");
				int HPI = rs.findColumn("HighPrice");
				int LPI = rs.findColumn("LowPrice");
				int CPI = rs.findColumn("ClosePrice");
				int ACI = rs.findColumn("AdjClose");
				int VI = rs.findColumn("Volume");
				double val;
				while(rs.next()){
					OpenDate.add(rs.getDate(ODI));
					val = rs.getDouble(OPI);
					if(val < MinVal)
						MinVal = val;
					if(val > MaxVal)
						MaxVal = val;
					OpenPrice.add(val);
					HighPrice.add(rs.getDouble(HPI));
					LowPrice.add(rs.getDouble(LPI));
					val = rs.getDouble(CPI);
					if(val < MinVal)
						MinVal = val;
					if(val > MaxVal)
						MaxVal = val;
					ClosePrice.add(val);
					AdjClose.add(rs.getDouble(ACI));
					Volume.add(rs.getInt(VI));
				}
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
			return false;
		}*/
		return true;
	}
	
	public void requestInsert(){
		JTextField f1 = new JTextField();
		JTextField f2 = new JTextField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		Object[] message = {
				"Company Abbreviation:", f1,
				"Type:", f2,
				"Price: $", f3,
				"Quantity:", f4
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Create Transaction.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			insertTransaction(f1.getText(),f2.getText(),f3.getText(),Integer.parseInt(f4.getText()));
	}
	
	
	/*public void requestUpdate(){
		JTextField f1 = new JTextField();
		JTextField f2 = new JTextField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		Object[] message = {
				"Company Abbreviation:", f1,
				"Type:", f2,
				"Price:", f3,
				"Quantity:", f4
		};
	}*/
	
	public void insertTransaction(String Abb, String Type, String Price, int Quantity){
		try{
			String sqlStatement = "{ ? = call CreateTransaction(?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Main.username);
			proc.setString(3, Abb);
			proc.setString(4, Type);
			proc.setString(5, Price);
			proc.setInt(6, Quantity);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Company does not exist.");
			}else{
				JOptionPane.showMessageDialog(null, "Transaction created.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}
	/*public void updateCompany(String Abb, String Type, String Price, String Quantity){
		int num = 1;
		//TODO: make drop down for Company and Type
		String base = "{ ? = call CreateTransaction(@User = " + Main.username + ",";
		if(Abb.length()>0)
			base = base + "@CompanyAbbreviation = ?,";
		if(Type.length()>0)
			base = base + "@Type = ?,";
		if(Price.length()>0)
			base = base + "@Price = ?,";
		if(Quantity.length()>0)
			base = base + "@Quantity = ?,";
		base = base.substring(0, base.length()-1);
		base = base + ") }";
		try{
			String sqlStatement = base;
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Abb);
			num = 3;
			if(Abb.length()>0)
				proc.setString(num++, Abb);
			if(Type.length()>0)
				proc.setString(num++, Type);
			if(Price.length()>0)
				proc.setString(num++, Price);//TODO: cast as money
			if(Quantity.length()>0)
				proc.setInt(num++, Integer.parseInt(Quantity));
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Transaction already exists in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Transcation info updated.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,
					"Format: company (string), type (string), price (money), qauntity (int)");
		}
	}*/
}
