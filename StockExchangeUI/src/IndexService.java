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

public class IndexService {
	private Frame f;
	
	private String IndexName;
	private String IndexSize;
	private String IndexCountry;
	private int IndexID, inx, IndexNum;
	private ArrayList<Date> OpenDate = new ArrayList<Date>();
	private ArrayList<Double> OpenPrice = new ArrayList<Double>(),
			HighPrice = new ArrayList<Double>(),
			LowPrice = new ArrayList<Double>(),
			ClosePrice = new ArrayList<Double>(),
			AdjClose = new ArrayList<Double>(),
			ChangeRate = new ArrayList<Double>();
	private ArrayList<Integer> Volume = new ArrayList<Integer>();
	private double MaxVal, MinVal;
	private ArrayList<ArrayList<String>> IndexData;
	
	public IndexService(Frame fr){
		f = fr;
	}
	
	public void draw_page(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, Frame.border, Frame.WIDTH, Frame.HEIGHT-Frame.border);

		g.setFont(f.title);
		g.drawString(IndexName, 20, 40);

		int gx = 30, gy = 90;
		int gw = 300, gh = 200;
		g.setColor(Color.DARK_GRAY);
		g.drawRect(gx,gy,gw,gh);
		if(OpenDate.size()>0){
			drawGraph(gx,gy,gw,gh,g);
		}else{
			g.drawLine(gx,gy,gx+gw,gy+gh);
		}

		g.drawRect(30, 455, 560, 60);
		g.setFont(f.text);
		g.drawString("Total Number of Stocks: "+IndexSize, 33, 480);
		g.drawString("Country: "+IndexCountry, 33, 505);
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
		g.drawRect(x+w+30, yy-15, 100, 30);

		g.drawRect(x, y+h+30, w*2-40, 125);
		g.drawString("Date: "+OpenDate.get(index),			x+3, y+h+52);
		g.drawString("Open Price: "+OpenPrice.get(index), 	x+3, y+h+82);
		g.drawString("High Price: "+HighPrice.get(index), 	x+3, y+h+112);
		g.drawString("Low Price: "+LowPrice.get(index), 	x+3, y+h+142);
		g.drawString("Close Price: "+ClosePrice.get(index), x+250, y+h+52);
		g.drawString("Adj Close: "+AdjClose.get(index), 	x+250, y+h+82);
		g.drawString("Volume: "+Volume.get(index), 			x+250, y+h+112);
		g.drawString("Change Rate: "+ChangeRate.get(index), x+250, y+h+142);
	}
	
	public void getIndexData(){
		try{
			String sqlStatement = "SELECT * FROM [Index]";
			PreparedStatement proc = f.DBCon.getConnection().prepareStatement(sqlStatement);
			ResultSet rs = proc.executeQuery();
			int IDIndex   = rs.findColumn("IndexID");
			int NameIndex = rs.findColumn("IndexName");
			int SizeIndex = rs.findColumn("TotalNumberOfStocks");
			int CtryIndex = rs.findColumn("Country");
			ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
			while(rs.next()){
				ArrayList<String> index = new ArrayList<String>();
				index.add(rs.getString(IDIndex));
				index.add(rs.getString(NameIndex));
				index.add(rs.getString(SizeIndex));
				index.add(rs.getString(CtryIndex));
				ret.add(index);
			}
			IndexData = ret;
		}catch(SQLException ex){
			JOptionPane.showMessageDialog(null, "Failed to fetch company data.");
			ex.printStackTrace();
		}
	}
	
	public boolean getData(){
		getIndexData();
		String[] names = new String[IndexData.size()];
		for(int i = 0; i < IndexData.size(); i++)
			names[i] = IndexData.get(i).get(1);
		String s = (String) JOptionPane.showInputDialog(null, "Choose an index to view.",
				"Index Selection", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
		if(s == null){
			return false;
		}
		for(int i = 0; i < IndexData.size(); i++)
			if(s.equals(IndexData.get(i).get(1))) {
				IndexNum = i;
				break;
			}
		IndexID = Integer.parseInt(IndexData.get(IndexNum).get(0));
		IndexName = IndexData.get(IndexNum).get(1);
		IndexSize = IndexData.get(IndexNum).get(2);
		IndexCountry = IndexData.get(IndexNum).get(3);

		try{
			String sqlStatement = "{ ? = call GetIndexHistory(?) }";
			CallableStatement proc = f.DBCon.getConnection().prepareCall(sqlStatement);
			proc.registerOutParameter(1, Types.INTEGER);
			proc.setInt(2, IndexID);
			proc.execute();
			ResultSet rs = proc.getResultSet();

			OpenDate.clear();
			OpenPrice.clear();
			HighPrice.clear();
			LowPrice.clear();
			ClosePrice.clear();
			AdjClose.clear();
			Volume.clear();
			ChangeRate.clear();
			if(rs == null){
				int ret = proc.getInt(1);
				if(ret == 1)
					JOptionPane.showMessageDialog(null,"ERROR: Index does not exist");
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
				int CRI = rs.findColumn("ChangeRate");
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
					ChangeRate.add(rs.getDouble(CRI));
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
