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

public class Transaction extends Page{
	/*private String TID;
	private String TimeStamp;
	private String CompanyAbbreviation;
	private String Type;
	private String Price;
	private String Quantity;*/
	private Boolean isAdmin;
	private Frame f;
	private ArrayList<ArrayList<String>> TransactionData;
	
	private String[] NAMES = {"Insert", "Edit"};
	
	public Transaction(Frame fr){
		f = fr;
	}
	
	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString("Stock Exchange Database", 20, 40);

		g.setColor(Color.DARK_GRAY);
		g.setFont(f.text);
		g.drawString("Transactions", 10, 90);

		//g.fillRect(560, Frame.border+10, 90, 30);
		//g.setColor(Color.LIGHT_GRAY);
		//g.drawString("Log Out", 568, Frame.border+30);

		for(int i = 0; i < NAMES.length; i++) {
			g.setColor(Color.DARK_GRAY);
			g.drawRect(10, Frame.border+60+50*i, Frame.WIDTH-20, 50);
			g.drawString(NAMES[i], 35, Frame.border+90+50*i);
			g.fillRect(560, Frame.border+70+50*i, 90, 30);
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("--Go->", 580, Frame.border+90+50*i);
		}
		/*g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);

		int gx = 30, gy = 90;
		int gw = 300, gh = 200;
		g.setColor(Color.DARK_GRAY);
		g.drawRect(gx,gy,gw,gh);

		g.setFont(f.title);
		g.drawRect(Frame.WIDTH-330, 320, 300, 210);
		g.setFont(f.text);*/
	}
	
	public void getTransactionData(){
		isAdmin = f.getAdmin();
		System.out.println("isAdmin "+isAdmin);
		try{
			String sqlStatement;
			if(isAdmin){
				sqlStatement = "SELECT * FROM [Transaction] ORDER BY TimeStamp DESC";
			}
			else {
				sqlStatement = "SELECT * FROM [Transaction] WHERE [User] = '"
									+ Main.username + "' ORDER BY TimeStamp DESC";
			}
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
		return true;
	}
	
	public void requestEdit(){
		getTransactionData();
		String[] names = new String[TransactionData.size()];
		for(int i = 0; i < TransactionData.size(); i++){
			/*"BUY 3 APPL stocks at $4/stock"*/
			/*TODO: change stocks to stock if only 1 stock exchanged*/
			if (isAdmin){
				names[i] = "User "+TransactionData.get(i).get(6)+ " with TID [" +TransactionData.get(i).get(0)+"]: "
						+TransactionData.get(i).get(3)+" "+TransactionData.get(i).get(5)+" "+
						TransactionData.get(i).get(2)+" stocks at $"+TransactionData.get(i).get(4)+
						"/stock ";
			} else {
				names[i] = "["+TransactionData.get(i).get(0)+"]: "+TransactionData.get(i).get(3)+" "+TransactionData.get(i).get(5)+" "+
							TransactionData.get(i).get(2)+" stocks at $"+TransactionData.get(i).get(4)+
							"/stock ";
			}
		}
		String s;
		if(names.length != 0){
			s = (String) JOptionPane.showInputDialog(null, "Transaction Selection",
				"Transaction Selection", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
		} else {
			s = null;
			if (isAdmin){
				JOptionPane.showMessageDialog(null, "No transactions in the database yet.");
			} else {
				JOptionPane.showMessageDialog(null, "You haven't made any transactions yet.");
			}
		}
		if(s == null){
			return;
		}
		String myTID = s.substring(s.indexOf('[')+1, s.indexOf(']'));
		int currIndex = -1;
		for(int i = 0; i < TransactionData.size(); i++)
			if(TransactionData.get(i).get(0).equals(myTID)){
				currIndex = i;
				break;
			}
		if (currIndex < 0){
			System.out.println("transaction data doesn't exist for this entry");
		}
		requestUpdate(myTID, currIndex);		
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
		if(isAdmin){
			JTextField f5 = new JTextField();
			Object[] messageAdmin = {
					"Company Abbreviation:", f1,
					"Type:", f2,
					"Price: $", f3,
					"Quantity:", f4,
					"User:", f5
			};
			int option = JOptionPane.showConfirmDialog(null, messageAdmin, "Create Transaction.", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION)
				insertTransaction(f5.getText(), f1.getText(),f2.getText(),f3.getText(),Integer.parseInt(f4.getText()));
		} else {
			int option = JOptionPane.showConfirmDialog(null, message, "Create Transaction.", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION)
				insertTransaction(Main.username, f1.getText(),f2.getText(),f3.getText(),Integer.parseInt(f4.getText()));
		}
	}
	
	
	public void requestUpdate(String myTID, int i){
		if(i<0){
			System.out.println("leaving");
			return;
		}
		JTextField f1 = new JTextField(TransactionData.get(i).get(2));
		JTextField f2 = new JTextField(TransactionData.get(i).get(3));
		JTextField f3 = new JTextField(TransactionData.get(i).get(4));
		JTextField f4 = new JTextField(TransactionData.get(i).get(5));
		Object[] message = {
				"Company Abbreviation:", f1,
				"Type:", f2,
				"Price:", f3,
				"Quantity:", f4
		};
		if (isAdmin) {
			JTextField f5 = new JTextField(TransactionData.get(i).get(6));
			Object[] messageAdmin = {
					"Company Abbreviation:", f1,
					"Type:", f2,
					"Price:", f3,
					"Quantity:", f4,
					"User:", f5
			};
			int option = JOptionPane.showConfirmDialog(null, messageAdmin, "Edit Transaction.", JOptionPane.OK_CANCEL_OPTION);
	
			if (option == JOptionPane.OK_OPTION)
				editTransaction(f5.getText(), Integer.parseInt(myTID), f1.getText(), f2.getText(), f3.getText(), Integer.parseInt(f4.getText()));
			else 
				getData();
		}
		else {
			int option = JOptionPane.showConfirmDialog(null, message, "Edit Transaction.", JOptionPane.OK_CANCEL_OPTION);
	
			if (option == JOptionPane.OK_OPTION)
				editTransaction(Main.username, Integer.parseInt(myTID), f1.getText(), f2.getText(), f3.getText(), Integer.parseInt(f4.getText()));
			else 
				getData();
		}
	}
	
	public void insertTransaction(String user, String Abb, String Type, String Price, int Quantity){
		try{
			if (user.length() <= 0 || Abb.length() <= 0 || Type.length() <= 0 || Price.length() <= 0 
					|| Quantity <= 0){
				JOptionPane.showMessageDialog(null,"Insert unsuccessful. Check to ensure input is complete"
						+ " and all numbers are positive");
			}
			
			String sqlStatement = "{ ? = call CreateTransaction(?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, user);
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
	
	public void editTransaction(String user, int TID, String CompanyAbbreviation, String Type, String Price, int Quantity ){

		try{
			String sqlStatement = "{ ? = call EditTransaction(?,?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setInt(2, TID);
			proc.setString(3,user);
			proc.setString(4, CompanyAbbreviation);
			proc.setString(5, Type);
			proc.setString(6, Price);
			proc.setInt(7, Quantity);
			proc.execute();
			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: edit failed");
			}else{
				JOptionPane.showMessageDialog(null, "Edit completed.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}

	@Override
	public void click(int x, int y) {
		if(in(560, 130, 90, 30, x, y)) {
			requestInsert();
		}
		if(in(560, 180, 90, 30, x, y)) {
			requestEdit();
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
