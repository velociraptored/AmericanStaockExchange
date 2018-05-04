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

public class CompanyAdminPage extends Page{
	private Frame f;
	private ArrayList<String> CompanyNames, CompanyAbbreviations, CompanyCountries, CompanyIndustries;
	private ArrayList<Integer> CompanyYears;
	private int page = 0, num_pages;

	public CompanyAdminPage(Frame fr){f = fr;}

	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString("Manage Companies", 20, 40);

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

		g.fillRect(500, Frame.border+10, 150, 30);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString("Add Company", 513, Frame.border+30);

		for(int i = 0; i < 8 && i+8*page < CompanyNames.size(); i++) {
			g.setColor(Color.DARK_GRAY);
			g.drawRect(10, Frame.border+60+50*i, Frame.WIDTH-20, 50);
			g.drawString(CompanyNames.get(i+8*page)+" ("+CompanyAbbreviations.get(i+8*page)+")", 35, Frame.border+90+50*i);
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

	public void getCompanyData(){
		CompanyNames = new ArrayList<String>();
		CompanyAbbreviations = new ArrayList<String>();
		CompanyCountries = new ArrayList<String>();
		CompanyIndustries = new ArrayList<String>();
		CompanyYears = new ArrayList<Integer>();
		try{
			String sqlStatement = "SELECT * FROM Company";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			int AbbrIndex = rs.findColumn("Abbreviation");
			int NameIndex = rs.findColumn("Name");
			int CtryIndex = rs.findColumn("Country");
			int IndsIndex = rs.findColumn("Industry");
			int YearIndex = rs.findColumn("Year");
			while(rs.next()){
				CompanyAbbreviations.add(rs.getString(AbbrIndex));
				CompanyNames.add(rs.getString(NameIndex));
				CompanyCountries.add(rs.getString(CtryIndex));
				CompanyIndustries.add(rs.getString(IndsIndex));
				CompanyYears.add(rs.getInt(YearIndex));
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to fetch company data.");
			ex.printStackTrace();
		}
		num_pages = CompanyNames.size()/8;
		if(CompanyNames.size() % 8 != 0)
			num_pages++;
	}

	public void click(int x, int y){
		if(page > 0 && in(10, Frame.border+15, 20, 20, x, y))
			page--;
		else if(page < num_pages - 1 && in(168, Frame.border+15, 20, 20, x, y))
			page++;
		else if(in(500, Frame.border+10, 150, 30, x, y))
			requestInsert();
		for(int i = 0; i < 8 && i+8*page < CompanyNames.size(); i++){
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
		f1.setDocument(new JTextFieldLimit(50));
		JTextField f2 = new JTextField();
		f2.setDocument(new JTextFieldLimit(10));
		JTextField f3 = new JTextField();
		f3.setDocument(new JTextFieldLimit(20));
		JTextField f4 = new JTextField();
		f4.setDocument(new JTextFieldLimit(30));
		JTextField f5 = new JTextField();
		f5.setDocument(new JTextFieldLimit(4));
		Object[] message = {
				"Company Name:", f1,
				"Abbreviation:", f2,
				"Country:", f3,
				"Industry:", f4,
				"Year:", f5,
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Insert Company Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION){
			try{
				insertCompany(f2.getText(),f1.getText(),f3.getText(),f4.getText(),Integer.parseInt(f5.getText()));
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(null, "Year must be an integer.");
			}
			getCompanyData();
		}
	}
	public void requestUpdate(int index){
		if(index >= CompanyNames.size())
			return;
		String abbr = CompanyAbbreviations.get(index);
		JTextField f1 = new JTextField();
		f1.setDocument(new JTextFieldLimit(50));
		JTextField f2 = new JTextField();
		f2.setDocument(new JTextFieldLimit(20));
		JTextField f3 = new JTextField();
		f3.setDocument(new JTextFieldLimit(30));
		JTextField f4 = new JTextField();
		f4.setDocument(new JTextFieldLimit(4));
		Object[] message = {
				"Company Name:", f1,
				"Country:", f2,
				"Industry:", f3,
				"Year:", f4,
		};
		f1.setText(CompanyNames.get(index));
		f2.setText(CompanyCountries.get(index));
		f3.setText(CompanyIndustries.get(index));
		f4.setText(""+CompanyYears.get(index));
		int option = JOptionPane.showConfirmDialog(null, message, "Update "+abbr+" Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION){
			updateCompany(abbr,f1.getText(),f2.getText(),f3.getText(),f4.getText());
			getCompanyData();
		}
	}
	public void requestDelete(int index){
		if(index >= CompanyNames.size())
			return;
		int ret = JOptionPane.showConfirmDialog(null, "Delete "+CompanyAbbreviations.get(index)+"?", "CompanyDeletion", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(ret == JOptionPane.OK_OPTION){
			deleteCompany(CompanyAbbreviations.get(index));
			getCompanyData();
		}
	}
	public void insertCompany(String Abb, String Nm, String cty, String ind, int year){
		try{
			String sqlStatement = "{ ? = call InsertCompany(?,?,?,?,?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Abb);
			proc.setString(3, Nm);
			proc.setString(4, cty);
			proc.setString(5, ind);
			proc.setInt(6, year);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Company already exists in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Company inserted.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}
	public void updateCompany(String Abb, String Nm, String cty, String ind, String year){
		int num = 1;
		String base = "{ ? = call EditCompany(@Abbreviation = ?,";
		if(Nm.length()>0)
			base = base + "@Name = ?,";
		if(cty.length()>0)
			base = base + "@Country = ?,";
		if(ind.length()>0)
			base = base + "@Industry = ?,";
		if(year.length()>0)
			base = base + "@Year = ?,";
		base = base.substring(0, base.length()-1);
		base = base + ") }";
		try{
			String sqlStatement = base;
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Abb);
			num = 3;
			if(Nm.length()>0)
				proc.setString(num++, Nm);
			if(cty.length()>0)
				proc.setString(num++, cty);
			if(ind.length()>0)
				proc.setString(num++, ind);
			if(year.length()>0)
				proc.setInt(num++, Integer.parseInt(year));
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: This company does not exist in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Company info updated.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}catch(NumberFormatException ex){
			JOptionPane.showMessageDialog(null, "Year must be an integer.");
		}
	}
	public void deleteCompany(String Abb){
		try{
			String sqlStatement = "{ ? = call DeleteCompany(@Abbreviation = ?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setString(2, Abb);
			proc.execute();

			int status = proc.getInt(1);
			if(status == 1){
				JOptionPane.showMessageDialog(null,"ERROR: Company does not exist in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Company deleted.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}
	}
}
