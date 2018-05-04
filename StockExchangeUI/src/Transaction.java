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
	private String currUser;
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
			int User = 					rs.findColumn("User");
			ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
			while(rs.next()){
				ArrayList<String> transaction = new ArrayList<String>();
				transaction.add(rs.getString(TID));
				transaction.add(rs.getString(TimeStamp));
				transaction.add(rs.getString(CompanyAbbreviation));
				transaction.add(rs.getString(Type));
				transaction.add(rs.getString(Price));
				transaction.add(rs.getString(Quantity));
				transaction.add(rs.getString(User));
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
		String[] ops = {"Insert", "Edit"};
		String editInsert = (String) JOptionPane.showInputDialog(null, "Insert Or Edit",
				"Insert or Edit", JOptionPane.QUESTION_MESSAGE, null, ops, ops[0]);
		if(editInsert.equals("Edit")){
			String[] names = new String[TransactionData.size()];
			for(int i = 0; i < TransactionData.size(); i++){
				/*"BUY 3 APPL stocks at $4/stock"*/
				/*TODO: change stocks to stock if only 1 stock exchanged*/
				names[i] = TransactionData.get(i).get(0)+": "+TransactionData.get(i).get(3)+" "+TransactionData.get(i).get(5)+" "+
							TransactionData.get(i).get(2)+" stocks at $"+TransactionData.get(i).get(4)+
							"/stock ";
			}
			String s;
			if(names.length != 0){
				s = (String) JOptionPane.showInputDialog(null, "Transaction Selection",
					"Transaction Selection", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
			} else {
				s = null;
				JOptionPane.showMessageDialog(null, "You haven't made any transactions yet.");
			}
			if(s == null){
				return false;
			}
			String myTID = s.substring(s.indexOf('[')+1, s.indexOf(':'));
			int currIndex;
			for(int i = 0; i < TransactionData.size(); i++)
				if(TransactionData.get(i).get(0).equals(myTID)){
					currIndex = i;
					break;
				}
			
			requestUpdate(myTID);
		} else {
			requestInsert();
		}
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
	
	
	public void requestUpdate(String myTID){
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
		int option = JOptionPane.showConfirmDialog(null, message, "Edit Transaction.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			editTransaction(Integer.parseInt(myTID), f1.getText(), f2.getText(),Integer.parseInt(f3.getText()),Integer.parseInt(f4.getText()));
		else 
			getData();
	}
	
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
				JOptionPane.showMessageDialog(null,"ERROR: insert failed");
			}else{
				JOptionPane.showMessageDialog(null, "Transaction created.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}
	
	public void editTransaction(int TID, String CompanyAbbreviation, String Type, int Price, int Quantity ){

		try{
			String sqlStatement = "{ ? = call EditTransaction(?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setInt(2, TID);
			proc.setString(3, CompanyAbbreviation);
			proc.setString(4, Type);
			proc.setInt(5, Price);
			proc.setInt(6, Quantity);
			proc.execute();
			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: insert failed");
			}else{
				JOptionPane.showMessageDialog(null, "Transaction created.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}

}
