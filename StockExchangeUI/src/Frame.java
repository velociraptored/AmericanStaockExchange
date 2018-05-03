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
	private Broker broker;
	private UserDetailsService ds;

	// User interface variables
	public Font title = new Font("Candara", 0, 40);
	public Font text = new Font("Candara", 0, 20);
	public int mx = 0, my = 0;

	// Framestate variables
	private boolean stay;

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
		cs = new CompanyService(this);
		broker=new Broker(this);
	}

	// Framestate management
	public void run(){
		String[] names = {"Company data","Test","Broker"};
		while(true){
			String page = (String) JOptionPane.showInputDialog(null, "Select page to view.", 
					"View Selection", JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
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
			}else if(page.equals("Broker")){
				while(true){
					stay = true;
					setVisible(true);
					while(stay){
						g.setColor(Color.DARK_GRAY);
						g.fillRect(0, 0, WIDTH, HEIGHT);
						broker.draw_page(g);
						this.getGraphics().drawImage(img, 9, 38, null);
						try{Thread.sleep(30);}catch(Exception e){};
					}
					setVisible(false);
				}
				
			}
			else{
				System.out.println("How did you even get here?");
			}
		}
	}

	// Event listeners
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
			stay = false;
		else if(admin && e.getKeyCode() == KeyEvent.VK_I)
			cs.requestInsert();
		else if(admin && e.getKeyCode() == KeyEvent.VK_U)
			cs.requestUpdate();
		else if(admin && e.getKeyCode() == KeyEvent.VK_D)
			cs.requestDelete();
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {
		mx = e.getX() - 9;
		my = e.getY() - 38;
	}

}