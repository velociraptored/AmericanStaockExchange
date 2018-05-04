import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class IndexAdminPage extends Page{
	private Frame f;
	private ArrayList<String> IndexNames, IndexCountries;
	private ArrayList<Integer> IndexIDs, IndexSizes;
	private int page = 0, num_pages;

	public IndexAdminPage(Frame fr){f = fr;}

	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString("Manage Indexes", 20, 40);

		if(page > 0) {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(10, Frame.border+15, 20, 20);
			g.setColor(Color.LIGHT_GRAY);
			drawTriangle(10, Frame.border+15, false, g);
		}
		if(page < num_pages - 1) {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(168, Frame.border+15, 20, 20);
			g.setColor(Color.LIGHT_GRAY);
			drawTriangle(168, Frame.border+15, true, g);
		}

		g.setColor(Color.DARK_GRAY);
		g.setFont(f.text);
		g.drawString("Page "+(page+1)+" out of "+num_pages, 35, 90);

		g.fillRect(530, Frame.border+10, 120, 30);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString("Add Index", 543, Frame.border+30);

		for(int i = 0; i < 8 && i+8*page < IndexNames.size(); i++) {
			g.setColor(Color.DARK_GRAY);
			g.drawRect(10, Frame.border+60+50*i, Frame.WIDTH-20, 50);
			g.drawString(IndexNames.get(i+8*page), 35, Frame.border+90+50*i);
			g.fillRect(480, Frame.border+70+50*i, 70, 30);
			g.fillRect(560, Frame.border+70+50*i, 90, 30);
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("Edit", 498, Frame.border+90+50*i);
			g.drawString("Delete", 575, Frame.border+90+50*i);
		}
	}

	public void drawTriangle(int x, int y, boolean up, Graphics g){
		int[] px = {x+5, x+15, x+10};
		int[] py1 = {y+5, y+5, y+15};
		int[] py2 = {y+15, y+15, y+5};
		Polygon p;
		if(up)
			p = new Polygon(px, py2, 3);
		else
			p = new Polygon(px, py1, 3);
		g.fillPolygon(p);
	}

	public void getIndexData(){
		IndexIDs = new ArrayList<Integer>();
		IndexNames = new ArrayList<String>();
		IndexSizes = new ArrayList<Integer>();
		IndexCountries = new ArrayList<String>();
		try{
			String sqlStatement = "SELECT * FROM [Index]";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			int IDIndex = rs.findColumn("IndexID");
			int NameIndex = rs.findColumn("IndexName");
			int SizeIndex = rs.findColumn("TotalNumberOfStocks");
			int CtryIndex = rs.findColumn("Country");
			while(rs.next()){
				IndexIDs.add(rs.getInt(IDIndex));
				IndexNames.add(rs.getString(NameIndex));
				IndexSizes.add(rs.getInt(SizeIndex));
				IndexCountries.add(rs.getString(CtryIndex));
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to fetch index data.");
			ex.printStackTrace();
		}
		num_pages = IndexIDs.size()/8;
		if(IndexIDs.size() % 8 != 0)
			num_pages++;
	}

	public void click(int x, int y){
		if(page > 0 && in(10, Frame.border+15, 20, 20, x, y))
			page--;
		else if(page < num_pages - 1 && in(168, Frame.border+15, 20, 20, x, y))
			page++;
		else if(in(500, Frame.border+10, 150, 30, x, y))
			requestInsert();
		for(int i = 0; i < 8 && i+8*page < IndexNames.size(); i++){
			if(in(480, Frame.border+70+50*i, 70, 30, x, y))
				requestUpdate(i+8*page);
			if(in(560, Frame.border+70+50*i, 90, 30, x, y))
				requestDelete(i+8*page);
		}
	}
	public boolean in(int bx, int by, int bw, int bh, int x, int y) {
		if(x < bx || x > bx + bw)
			return false;
		if(y < by || y > by + bh)
			return false;
		return true;
	}

	// Database Write Operations
	public void requestInsert(){
		JTextField f1 = new JTextField();
		f1.setDocument(new JTextFieldLimit(20));
		JTextField f2 = new JTextField();
		f2.setDocument(new JTextFieldLimit(12));
		JTextField f3 = new JTextField();
		f3.setDocument(new JTextFieldLimit(20));
		Object[] message = {
				"Index Name:", f1,
				"Total Number of Stocks:", f2,
				"Country:", f3
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Insert Index Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION){
			try{
				insertIndex(f1.getText(),f2.getText(),f3.getText());
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(null, "Stock number must be an integer.");
			}
			getIndexData();
		}
	}
	public void requestUpdate(int index){
		if(index >= IndexIDs.size())
			return;
		Integer ID = IndexIDs.get(index);
		JTextField f1 = new JTextField();
		f1.setDocument(new JTextFieldLimit(20));
		JTextField f2 = new JTextField();
		f2.setDocument(new JTextFieldLimit(12));
		JTextField f3 = new JTextField();
		f3.setDocument(new JTextFieldLimit(20));
		Object[] message = {
				"Index Name:", f1,
				"Total Number of Stocks:", f2,
				"Country:", f3
		};
		f1.setText(IndexNames.get(index));
		f2.setText(""+IndexSizes.get(index));
		f3.setText(IndexCountries.get(index));
		int option = JOptionPane.showConfirmDialog(null, message, "Update "+IndexNames.get(index)+" Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION){
			updateIndex(ID,f1.getText(),f2.getText(),f3.getText());
			getIndexData();
		}
	}
	public void requestDelete(int index){
		if(index >= IndexIDs.size())
			return;
		int ret = JOptionPane.showConfirmDialog(null, "Delete "+IndexNames.get(index)+"?", "CompanyDeletion", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(ret == JOptionPane.OK_OPTION){
			deleteIndex(IndexIDs.get(index));
			getIndexData();
		}
	}
	public void insertIndex(String name, String size, String country){
		try{
			String sqlStatement = "{ ? = call InsertIndex(?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, name);
			proc.setInt(3, Integer.parseInt(size));
			proc.setString(4, country);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Index already exists in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Index inserted.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}
	public void updateIndex(int ID, String name, String size, String country){
		int num = 1;
		String base = "{ ? = call EditIndex(@IndexID = ?,";
		if(name.length()>0)
			base = base + "@IndexName = ?,";
		if(size.length()>0)
			base = base + "@TotalStocks = ?,";
		if(country.length()>0)
			base = base + "@Country = ?,";
		base = base.substring(0, base.length()-1);
		base = base + ") }";
		try{
			String sqlStatement = base;
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setInt(2, ID);
			num = 3;
			if(name.length()>0)
				proc.setString(num++, name);
			if(size.length()>0)
				proc.setInt(num++, Integer.parseInt(size));
			if(country.length()>0)
				proc.setString(num++, country);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: This index does not exist in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Index info updated.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}catch(NumberFormatException ex){
			JOptionPane.showMessageDialog(null, "Stock number must be an integer.");
		}
	}
	public void deleteIndex(int ID){
		try{
			String sqlStatement = "{ ? = call DeleteIndex(@IndexID = ?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setInt(2, ID);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Index does not exist in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Index deleted.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}
}
