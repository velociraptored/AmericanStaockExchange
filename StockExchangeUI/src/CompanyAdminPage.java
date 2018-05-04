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

public class CompanyAdminPage {
	private Frame f;
	private ArrayList<String> CompanyNames, CompanyAbbreviations, CompanyCountries, CompanyIndustries;
	private ArrayList<Integer> CompanyYears;
	private int page = 0, num_pages = 0;

	public CompanyAdminPage(Frame fr){f = fr;}

	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString("Manage Companies", 20, 40);

		g.setColor(Color.DARK_GRAY);
		g.setFont(f.text);
		g.fillRect(10, Frame.border+15, 20, 20);
		g.fillRect(168, Frame.border+15, 20, 20);
		g.drawString("Page "+page+" out of "+num_pages, 35, 90);
		
		for(int i = 0; i < 10 && i+10*page < CompanyNames.size(); i++) {
			g.drawRect(10, Frame.border+10, Frame.WIDTH-20, 50);
		}

		g.setColor(Color.LIGHT_GRAY);
		drawTriangle(10, Frame.border+15, true, g);
		drawTriangle(168, Frame.border+15, false, g);
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
				CompanyNames.add(rs.getString(AbbrIndex));
				CompanyAbbreviations.add(rs.getString(NameIndex));
				CompanyCountries.add(rs.getString(CtryIndex));
				CompanyIndustries.add(rs.getString(IndsIndex));
				CompanyYears.add(rs.getInt(YearIndex));
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to fetch company data.");
			ex.printStackTrace();
		}
	}

	// Database Write Operations
	public void requestInsert(){
		JTextField f1 = new JTextField();
		JTextField f2 = new JTextField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		JTextField f5 = new JTextField();
		Object[] message = {
				"Company Name:", f1,
				"Abbreviation:", f2,
				"Country:", f3,
				"Industry:", f4,
				"Year:", f5,
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Insert Company Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			insertCompany(f2.getText(),f1.getText(),f3.getText(),f4.getText(),Integer.parseInt(f5.getText()));
	}
	public void requestUpdate(){
		JTextField f1 = new JTextField();
		JTextField f2 = new JTextField();
		JTextField f3 = new JTextField();
		JTextField f4 = new JTextField();
		JTextField f5 = new JTextField();
		Object[] message = {
				"Abbreviation:", f1,
				"Company Name:", f2,
				"Country:", f3,
				"Industry:", f4,
				"Year:", f5,
		};
		int option = JOptionPane.showConfirmDialog(null, message, "Update Company Data.", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION)
			updateCompany(f1.getText(),f2.getText(),f3.getText(),f4.getText(),f5.getText());
	}
	public void requestDelete(){
		String[] names = new String[CompanyNames.size()];
		for(int i = 0; i < CompanyNames.size(); i++)
			names[i] = CompanyNames.get(i)+" ("+CompanyAbbreviations.get(i)+")";
		String s = (String) JOptionPane.showInputDialog(null, "Delete Company Data.",
				"Company Selection", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
		if (s != null && s.length() > 0)
			deleteCompany(s.substring(s.indexOf('(')+1, s.indexOf(')')));
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
				JOptionPane.showMessageDialog(null,"ERROR: Company already exists in the database.");
			}else{
				JOptionPane.showMessageDialog(null, "Company info updated.");
			}
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to run query.");
			ex.printStackTrace();
		}catch(Exception ex){
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
