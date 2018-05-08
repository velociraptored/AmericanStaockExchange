import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Frame extends JFrame implements MouseMotionListener, MouseListener, KeyListener{
	// Frame variables
	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 700, HEIGHT = 560, border = 60;
	public BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	public Graphics g = img.getGraphics();

	// Database variables and services
	public DBConnection DBCon;
	private CompanyService cs;
	private IndexService is;
	private Broker broker;
	private UserDetailsService ds;
	private CompanyAdminPage cas;
	private IndexAdminPage ias;
	private AdminHomepage ahs;
	private Transaction transact;
	private MainPage mp;
	private Page current_page = null;

	// User interface variables
	public Font title = new Font("Candara", 0, 40);
	public Font text = new Font("Candara", 0, 20);
	public Font smallText = new Font("Candara", 0, 15);

	public int mx = 0, my = 0;

	// Framestate variables
	private boolean stay;
	public String page;

	// Admin privilages
	private boolean admin;
	public void setAdmin(boolean a){admin = a;}

	// Constructor
	public Frame(DBConnection con){
		super("StockDatabase");
		DBCon = con;
		setSize(WIDTH+18, HEIGHT+47);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Graphics2D g2 = (Graphics2D)g;
		RenderingHints rh = new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHints(rh);
		Runnable r = new Runnable() {
			public void run() {
				try {
					DBCon.closeConnection();
				} catch (Exception e) {
					System.out.println("Shutdown hook interrupted.");
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(r));

		// Instantiate new pages
		cs = new CompanyService(this);
		is = new IndexService(this);
		broker=new Broker(this);
		cas = new CompanyAdminPage(this);
		ias = new IndexAdminPage(this);
		ds = new UserDetailsService(this);
		ahs = new AdminHomepage(this);
		transact = new Transaction(this);
	}

	// Framestate management
	public void select_page(int i, String[] names){
		if(i == 1) {
			page = (String) JOptionPane.showInputDialog(null, "Select page to view.", 
					"View Selection", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
		} else {
			page = null;
			mp = new MainPage(this, names);
			mp.getUserData();
			current_page = mp;
			setVisible(true);
			stay = true;
			while(stay){
				g.setColor(Color.DARK_GRAY);
				g.fillRect(0, 0, WIDTH, HEIGHT);
				g.setColor(Color.WHITE);
				if(mp.draw_page(g))
					stay = false;
				this.getGraphics().drawImage(img, 9, 38, null);
				try{Thread.sleep(30);}catch(Exception e){};
			}
		}
	}

	public void run(){
		String[] names = {};
		if(admin){
			String[] temp = {"Company data","Index data","Manage Company Data","Manage Index Data","Broker", "Admin Homepage", "User Details", "Transactions"};
			names = temp;
		}else{
			String[] temp = {"Company data","Index data","Broker", "User Details"};
			names = temp;
		}
		while(true){
			select_page(0, names);
			if(page == null || page.length() == 0)
				System.exit(0);
			if(page.equals("Test")){
				setVisible(true);
				stay = true;
				while(stay){
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, WIDTH, HEIGHT);
					g.setColor(Color.WHITE);
					g.drawString("test", 50, 50);
					this.getGraphics().drawImage(img, 9, 38, null);
					try{Thread.sleep(30);}catch(Exception e){};
				}
				setVisible(false);
			}else if(page.equals("Company data")){
				while(true){
					stay = true;
					if(!cs.getData())
						break;
					setVisible(true);
					while(stay){
						g.setColor(Color.DARK_GRAY);
						g.fillRect(0, 0, WIDTH, HEIGHT);
						cs.draw_page(g);
						this.getGraphics().drawImage(img, 9, 38, null);
						try{Thread.sleep(30);}catch(Exception e){};
					}
					setVisible(false);
				}
			}else if(page.equals("Index data")){
				while(true){
					stay = true;
					if(!is.getData())
						break;
					setVisible(true);
					while(stay){
						g.setColor(Color.DARK_GRAY);
						g.fillRect(0, 0, WIDTH, HEIGHT);
						is.draw_page(g);
						this.getGraphics().drawImage(img, 9, 38, null);
						try{Thread.sleep(30);}catch(Exception e){};
					}
					setVisible(false);
				}
			}else if(page.equals("Broker")){
				stay = true;
				broker.getAllBroker();
				broker.getPartBroker();
				broker.getBrokerData();
				setVisible(true);
				current_page = broker;
				while(stay){
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, WIDTH, HEIGHT);
					broker.draw_page(g);
					this.getGraphics().drawImage(img, 9, 38, null);
					try{Thread.sleep(30);}catch(Exception e){};
				}
				setVisible(false);
			}else if(page.equals("Manage Company Data")){
				stay = true;
				setVisible(true);
				current_page = cas;
				cas.getCompanyData();
				while(stay){
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, WIDTH, HEIGHT);
					cas.draw_page(g);
					this.getGraphics().drawImage(img, 9, 38, null);
					try{Thread.sleep(30);}catch(Exception e){};
				}
				current_page = null;
				setVisible(false);
			}else if(page.equals("Manage Index Data")){
				stay = true;
				setVisible(true);
				current_page = ias;
				ias.getIndexData();
				while(stay){
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, WIDTH, HEIGHT);
					ias.draw_page(g);
					this.getGraphics().drawImage(img, 9, 38, null);
					try{Thread.sleep(30);}catch(Exception e){};
				}
				current_page = null;
				setVisible(false);
			}else if (page.equals("User Details")) {
				stay = true;
				if (!ds.getUserData())
					break;
				if (!stay) {
					setVisible(false);
					page = "";
					break;
				}
				current_page = ds;
				setVisible(true);
				while (stay) {
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, WIDTH, HEIGHT);
					ds.draw_page(g);
					this.getGraphics().drawImage(img, 9, 38, null);
					try{Thread.sleep(30);}catch(Exception e){};
				}
				current_page = null;
				setVisible(false);

			} else if (page.equals("Admin Homepage")) {
				stay = true;
				if (!ahs.getData())
					break;
				if (!stay) {
					setVisible(false);
					page = "";
					break;
				}
				setVisible(true);
				current_page = ahs;
				while (stay) {
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, WIDTH, HEIGHT);
					ahs.draw_page(g);
					this.getGraphics().drawImage(img, 9, 38, null);
					try{Thread.sleep(30);}catch(Exception e){};
				}
				current_page = null;
				setVisible(false);
			} else if(page.equals("Transactions")) {
				stay = true;
				if(!transact.getData())
					break;
				setVisible(true);
				while(stay){
					g.setColor(Color.DARK_GRAY);
					g.fillRect(0, 0, WIDTH, HEIGHT);
					transact.draw_page(g);
					this.getGraphics().drawImage(img, 9, 38, null);
					try{Thread.sleep(30);}catch(Exception e){};
				}
				setVisible(false);
			} else {
				System.out.println("How did you even get here?");
			}
		}
	}

	// Event listeners
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
			stay = false;
		//To be fixed later
		else if (admin && e.getKeyCode() == KeyEvent.VK_N)
			ahs.requestInsert();
		else if (admin && e.getKeyCode() == KeyEvent.VK_E)
			ahs.requestUpdate();
		else if (admin && e.getKeyCode() == KeyEvent.VK_R)
			ahs.requestDelete();
		else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			ahs.decrementPage();
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			ahs.incrementPage();
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		if(current_page != null)
			current_page.click(mx, my);
	}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {
		mx = e.getX() - 9;
		my = e.getY() - 38;
	}

}