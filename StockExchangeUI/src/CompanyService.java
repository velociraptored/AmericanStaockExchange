import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class CompanyService {
	private Frame f;
	
	private String CompanyName;
	private String Abbreviation;
	private String CompanyCountry;
	private String CompanyIndustry;
	private String CompanyFounding;
	private int CompanyNumber, inx;
	private ArrayList<Date> OpenDate = new ArrayList<Date>();
	private ArrayList<Double> OpenPrice = new ArrayList<Double>(),
			HighPrice = new ArrayList<Double>(),
			LowPrice = new ArrayList<Double>(),
			ClosePrice = new ArrayList<Double>(),
			AdjClose = new ArrayList<Double>();
	private ArrayList<Integer> Volume = new ArrayList<Integer>();
	private double MaxVal, MinVal;
	private ArrayList<ArrayList<String>> CompanyData;
	
	public CompanyService(Frame fr){
		f = fr;
	}
	
	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString(CompanyName+" ("+Abbreviation+")", 20, 40);

		int gx = 30, gy = 90;
		int gw = 300, gh = 200;
		g.setColor(Color.DARK_GRAY);
		g.drawRect(gx,gy,gw,gh);
		if(OpenDate.size()>0){
			drawGraph(gx,gy,gw,gh,g);
		}else{
			g.drawLine(gx,gy,gx+gw,gy+gh);
		}

		g.setFont(f.title);
		g.drawRect(Frame.WIDTH-330, 320, 300, 210);
		g.drawString(CompanyName, Frame.WIDTH-330+10, 360);
		g.setFont(f.text);
		g.drawString(Abbreviation, Frame.WIDTH-330+10, 400);
		g.drawString(CompanyCountry, Frame.WIDTH-330+10, 430);
		g.drawString(CompanyIndustry, Frame.WIDTH-330+10, 460);
		g.drawString("Since "+CompanyFounding, Frame.WIDTH-330+10, 490);
	}
	
	public void drawGraph(int x, int y, int w, int h, Graphics g){
		g.setFont(f.text);
		g.setColor(Color.BLUE);
		double small = MinVal*5/6;
		double large = MaxVal*11/10;
		double mult = h/(large-small);
		double div = 1.0*w/OpenDate.size();
		for(int i = 0; i < OpenDate.size(); i++){
			int[] py = {y+h, y+(int)(h - mult*(OpenPrice.get(i)-small)), y+(int)(h - mult*(ClosePrice.get(i)-small)), y+h};
			int[] px = {(int)(x+div*i), (int)(x+div*i), (int)(x+div*(i+1)), (int)(x+div*(i+1))};
			Polygon p = new Polygon(px,py,4);
			g.fillPolygon(p);
		}
		g.setColor(Color.DARK_GRAY);
		if(f.mx < x+w && f.mx > x && f.my < y+h && f.my > y)
			inx = f.mx-x;

		int index = (int)(inx/div);
		double per = 1.0*(inx-index*div)/div;
		double value = (int)(100*((1-per)*OpenPrice.get(index)+per*ClosePrice.get(index)))/100.0;
		int yy = y+(int)(h - mult*(value-small));
		g.drawString("$"+value, x+w+33, yy+7);

		g.drawLine(x+inx, y, x+inx, y+h+30);
		g.drawLine(x, yy, x+w+30, yy);
		g.drawRect(x+w+30, yy-15, 80, 30);

		g.drawRect(x, y+h+30, w, 210);
		g.drawString("Date: "+OpenDate.get(index),			x+3, y+h+52);
		g.drawString("Open Price: "+OpenPrice.get(index), 	x+3, y+h+82);
		g.drawString("High Price: "+HighPrice.get(index), 	x+3, y+h+112);
		g.drawString("Low Price: "+LowPrice.get(index), 	x+3, y+h+142);
		g.drawString("Close Price: "+ClosePrice.get(index), x+3, y+h+172);
		g.drawString("Adj Close: "+AdjClose.get(index), 	x+3, y+h+202);
		g.drawString("Volume: "+Volume.get(index), 			x+3, y+h+232);
	}
	
	public void getCompanyData(){
		try{
			String sqlStatement = "SELECT * FROM Company";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			int AbbrIndex = rs.findColumn("Abbreviation");
			int NameIndex = rs.findColumn("Name");
			int CtryIndex = rs.findColumn("Country");
			int IndsIndex = rs.findColumn("Industry");
			int YearIndex = rs.findColumn("Year");
			ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
			while(rs.next()){
				ArrayList<String> company = new ArrayList<String>();
				company.add(rs.getString(AbbrIndex));
				company.add(rs.getString(NameIndex));
				company.add(rs.getString(CtryIndex));
				company.add(rs.getString(IndsIndex));
				company.add(rs.getString(YearIndex));
				ret.add(company);
			}
			CompanyData = ret;
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to fetch company data.");
			ex.printStackTrace();
		}
	}
	
	public boolean getData(){
		getCompanyData();
		String[] names = new String[CompanyData.size()];
		for(int i = 0; i < CompanyData.size(); i++)
			names[i] = CompanyData.get(i).get(1)+" ("+CompanyData.get(i).get(0)+")";
		String s = (String) JOptionPane.showInputDialog(null, "Choose a company to view.",
				"Company Selection", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
		if(s == null){
			return false;
		}
		Abbreviation = s.substring(s.indexOf('(')+1, s.indexOf(')'));
		for(int i = 0; i < CompanyData.size(); i++)
			if(CompanyData.get(i).get(0).equals(Abbreviation)){
				CompanyNumber = i;
				break;
			}
		CompanyName = CompanyData.get(CompanyNumber).get(1);
		CompanyCountry = CompanyData.get(CompanyNumber).get(2);
		CompanyIndustry = CompanyData.get(CompanyNumber).get(3);
		CompanyFounding = CompanyData.get(CompanyNumber).get(4);

		try{
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
		}
		return true;
	}
}
