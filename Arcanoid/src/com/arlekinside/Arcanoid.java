package com.arlekinside;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.lang.Thread;

public class Arcanoid implements Runnable{
	public static boolean gameIsAlive = true;
	private static JFrame frame;
	public static final int FRAME_WIDTH = 660;
	public static final int FRAME_HEIGHT = 800;
	public static int gameSpeed = 3000;
	public static byte goodGame = 0;
	private static ArrayList<Thread> t = new ArrayList<Thread>();
	private static int numOfThreads = 0;
	public static void main(String[] args) {
		GameBuilder();
	  }	
	private static void GameBuilder() { //создание фрейма
		frame = new JFrame("Arcanoid");
	    frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
	    frame.add(new Paint());
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
	    frame.addKeyListener(new Deck());
	    frame.setVisible(true);
	    Balls b;
	    for(int i = 0; i < Balls.getNumOfBalls();i++) {
    		Balls.ball.add(new Balls());
    	}
	    Rectangles.make();
		    while(gameIsAlive) {
		    	//using balls
		    	for(int i = 0; i < Balls.getNumOfBalls();i++) {
		    		b = Balls.ball.get(i);
		    		b.setBallX(b.getBallX() + b.getDeltaX());
		    		b.setBallY(b.getBallY() + b.getDeltaY());
		    		killChecker(b);
		    		b.boardChecker();
		    		if(b.getBallY() > Arcanoid.FRAME_HEIGHT & Balls.getNumOfBalls() > 1) {
		    			Balls.setNumOfBalls(Balls.getNumOfBalls()-1);
		    			Balls.ball.remove(i);
		    			i--;
		    		}
					if(b.getBallY()+b.getSize() == Deck.y & b.getBallX() >= Deck.x & b.getBallX() <= (Deck.x + Deck.width)) {
						b.setDeltaY(-b.getDeltaY());
					}
		    	}
		    	//LootBoxes life
		    	for(LootBoxes box: LootBoxes.lb) {
					if(box.isAlive) {
						if(box.y == Arcanoid.FRAME_HEIGHT ) {
							box.isAlive = false;
						}
						if(box.y == Deck.y & ((box.x >= Deck.x & box.x + box.WIDTH <= Deck.x + Deck.width) ||(box.x <= Deck.x & box.x + box.WIDTH > Deck.x) || (box.x < Deck.x + Deck.width & box.x + box.WIDTH >= Deck.x + Deck.width))) {
							Arcanoid.threadManager(true);
							box.isAlive = false;
						}
						box.y+=2;
					}
				}
				if(Rectangles.aliveNum == 0) {
					gameIsAlive = false;
					goodGame = 2;
				}
		    	frame.repaint();
		    	try {
		    	    TimeUnit.MICROSECONDS.sleep(gameSpeed);
		    	}catch(Exception ex) {}	
		    }
	    	frame.repaint();
	}
	private static void killChecker(Balls b) {
		Rectangles r;
		for(int i = 0; i< 60; i++) {
			r = Rectangles.rects.get(i);
			//South hit
			if(r.getIsAlive() == true & b.getDeltaY() < 0 & b.getBallY() == (r.y + r.getHeight()) & b.getBallX() >= r.x & b.getBallX() <= (r.x + r.getWidth())){
				r.checkNumOfHits(r);
				if(r.getIsAlive() == false) {
					if((byte)(Math.random() * 5) < 10) {
						LootBoxes.makeBox(r.x + r.getWidth()/4,r.y + r.getHeight());
					}
				}
				b.setDeltaY(-b.getDeltaY());
				frame.repaint();
			}
			//North hit
			if(r.getIsAlive() == true &  b.getDeltaY() > 0 & (b.getBallY() + b.getSize()) == r.y & b.getBallX() >= r.x & b.getBallX() <= (r.x + r.getWidth())){
				r.checkNumOfHits(r);
				if(r.getIsAlive() == false) {
					if((byte)(Math.random() * 5) < 10) {
						LootBoxes.makeBox(r.x + r.getWidth()/4,r.y + r.getHeight());
					}
				}
				b.setDeltaY(-b.getDeltaY());
				frame.repaint();
			}
			//West hit
			if(r.getIsAlive() == true & b.getDeltaX() > 0 & (b.getBallX() + b.getSize()) == r.x & b.getBallY() >= r.y & b.getBallY() <= (r.y + r.getHeight())){
				r.checkNumOfHits(r);
				if(r.getIsAlive() == false) {
					if((byte)(Math.random() * 5) < 10) {
						LootBoxes.makeBox(r.x + r.getWidth()/4,r.y + r.getHeight());
					}
				}
				b.setDeltaX(-b.getDeltaX());
				frame.repaint();
			}
			//East hit
			if(r.getIsAlive() == true & b.getDeltaX() < 0 & b.getBallX() == (r.x + r.getWidth()) & b.getBallY() >=r.y & b.getBallY() <= (r.y + r.getHeight())){
				r.checkNumOfHits(r);
				if(r.getIsAlive() == false) {
					if((byte)(Math.random() * 5) < 10) {
						LootBoxes.makeBox(r.x + r.getWidth()/4,r.y + r.getHeight());
					}
				}
				b.setDeltaX(-b.getDeltaX());
				frame.repaint();
			}
			
		}
		
	}
	private static void threadManager(boolean w) {
		if(w) {
			Arcanoid.t.add(new Thread(new Arcanoid()));
			Arcanoid.t.get(numOfThreads).start();
			Arcanoid.numOfThreads++;
		}else {
			Arcanoid.t.remove(0);
			Arcanoid.numOfThreads--;
		}
	}
	public void run() {
		byte b = LootBoxes.doBonus();
		try {
			TimeUnit.SECONDS.sleep(5);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		LootBoxes.returnDefaults(b);
		threadManager(false);
	}
}
//boxes` with bonuses class
class LootBoxes{
	Rectangles r = Rectangles.rects.get(0);
	public final int WIDTH = r.getWidth()/2;
	public final int HEIGHT = r.getHeight()/3;
	public boolean isAlive = true;
	public Color color;
	public int x;
	public int y;
	public static float timeCounter;
	public static boolean bonusStatus = false;
	public static String timer = "";
	public static ArrayList<LootBoxes> lb = new ArrayList<LootBoxes>();
	public static void makeBox(int rectX, int rectY) {
		LootBoxes aBox = new LootBoxes();
		aBox.color = Color.green;
		aBox.x = rectX;
		aBox.y = rectY;
		lb.add(aBox);
	}
	public void run() {
	}
	public static void returnDefaults(byte b) {
				switch(b) {
				case 0:
					for(Balls ball: Balls.ball) {
						ball.setSize(ball.getSize()/2);
					}
					break;
				case 1: 
					Deck.width=80;
					break;
				case 2:
					Deck.color = Color.yellow;
					Deck.width = 80;
					Deck.x = (Arcanoid.FRAME_WIDTH - Deck.width)/2;
					break;
				case 3:
					Arcanoid.gameSpeed = 3000;
					break;
				case 4:
					Balls.power = 1;
					Balls.color = Color.white;
					break;
				}
	}
	public static byte doBonus() {
		
		switch((int) (Math.random() * 4)) {
		case 0:
			for(Balls ball: Balls.ball) {
				if(ball.getSize()<13) {
					ball.setSize(2*ball.getSize());
				}
			}
			bonusStatus = true;
			timeCounter = 0;
			return 0;
		case 1: 
			if(Deck.width < 160) {
				Deck.width*=2;
			}
			bonusStatus = true;
			timeCounter = 0;
			return 1;
		case 2:
			Deck.color = Color.blue;
			Deck.x = -1000;
			Deck.width = 10 * Arcanoid.FRAME_WIDTH;
			bonusStatus = true;
			timeCounter = 0;
			return 2;
		case 3:
			Arcanoid.gameSpeed = 2000;
			bonusStatus = true;
			timeCounter = 0;
			return 3;
		case 4:
			Balls.power = 2;
			Balls.color = Color.red;
			bonusStatus = true;
			timeCounter = 0;
			return 4;
		default: 
			return -1;
		}
	}
}
class Rectangles{ //класс со всеми клетками
	private boolean isAlive = true;
	public static int aliveNum = 60;
	private int numOfHits;
	private final int WIDTH = Arcanoid.FRAME_WIDTH/15;
	private final int HEIGHT = WIDTH/2;
	private Color color;
	public int x = 0;
	public int y = 0;
	public static ArrayList<Rectangles> rects = new ArrayList<Rectangles>();
	public int getWidth() {
		return WIDTH;
	}
	public boolean getIsAlive() {
		return isAlive;
	}
	public void setIsAlive(boolean a) {
		isAlive = a;
	}
	public int getHeight() {
		return HEIGHT;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color c) {
		color = c;
	}
	
	public static void make() { // Заполнение массива экземплярами класса Rectangles
		int spaceX = 0;
		int spaceY = 100;
		for(int index = 0; index < 60; index++) {
			rects.add(new Rectangles());
			if(index == 15 || index == 30 || index == 45) {
				spaceX = 0;
				spaceY+=rects.get(index).HEIGHT;
			}
			if(index < 15) {
				rects.get(index).setColor(Color.DARK_GRAY);
				rects.get(index).numOfHits=3;
			}else if(index < 30) {
				rects.get(index).setColor(Color.cyan);
				rects.get(index).numOfHits=1;
			}else if(index < 45) {
				rects.get(index).setColor(Color.orange);
				rects.get(index).numOfHits=2;
			}else if(index < 60) {
				rects.get(index).setColor(Color.red);
				rects.get(index).numOfHits=1;
			}
			rects.get(index).x = spaceX;
			rects.get(index).y = spaceY;
			spaceX+=rects.get(index).getWidth();
		}
	}
	public void checkNumOfHits(Rectangles r) {
		r.numOfHits-=Balls.power;
		if(numOfHits<=0) {
			r.isAlive = false;
			r.color = Color.black;
			aliveNum--;
		}
		switch(numOfHits){
		case 1:
			r.color = Color.red;
			break;
		case 2:
			r.color = Color.pink;
			break;
		}
	}
}
class Deck implements KeyListener{
	public static int width = 80;
	public static final int HEIGHT = 10;
	public static int x = 310;
	public static int y = Arcanoid.FRAME_HEIGHT - 100;
	public static Color color = Color.yellow;
	public void keyReleased(KeyEvent e) {
		
	}
	public void keyTyped(KeyEvent e) {
	
	}
	public void keyPressed(KeyEvent e) {
		int k = e.getKeyCode();
		if(k == KeyEvent.VK_A ^ k == KeyEvent.VK_LEFT) {
			Deck.x-=100;
		}
		if(k == KeyEvent.VK_D ^ k == KeyEvent.VK_RIGHT) {
			Deck.x+=100;
		}
	}
}
class Balls{
	public static ArrayList<Balls> ball = new ArrayList<Balls>();
	public static Color color = Color.white;
	public static byte power = 1;
	private static int numOfBalls = 2;
	private int deltaX = 0;
	private int deltaY = 0;
	private int ballX = 300;
	private int ballY = 300;
	private int size = 12;
	public void boardChecker() {
		if(ballX <= 0 || ballX >= Arcanoid.FRAME_WIDTH - size) {
			deltaX = -deltaX;
		}
		if(ballY <= 0) {
			deltaY = -deltaY;
		}
		if(ballY > Arcanoid.FRAME_HEIGHT & numOfBalls == 1) {
			Arcanoid.gameIsAlive = false;
			Arcanoid.goodGame = 1;
		}
	}
	public void setSize(int s) {
		size = s;
	}
	public int getSize() {
		return size;
	}
	public static void setNumOfBalls(int num) {
		numOfBalls = num;
	}
	public static int getNumOfBalls() {
		return numOfBalls;
	}
	public void setDeltaX(int x) {
		deltaX = x;
	}
	public int getDeltaX() {
		return deltaX;
	}
	public void setDeltaY(int y) {
		deltaY = y;
	}
	public int getDeltaY() {
		return deltaY;
	}
	public void setBallX(int x) {
		ballX = x;
	}
	public int getBallX() {
		return ballX;
	}
	public void setBallY(int y) {
		ballY = y;
	}
	public int getBallY() {
		return ballY;
	}
	Balls(){
		ballY = 200;
		deltaY = 1;
		if (1==(int)(Math.random()*2)){
			ballX = (int)(Math.random()*175)*2 + 20;
			deltaX = (int)(Math.random() * 1) + 1;
		}else {
			ballX = (int)(Math.random()*170)*2 + 340;
			deltaX = -(int)(Math.random() * 1) - 1;
		}
	}
	
}
class Paint extends JPanel{
	Arcanoid a = new Arcanoid();
	
	private static Font mainFont = new Font("Times New Roman", Font.PLAIN,72);
	private static Font	timeFont = new Font("Times New Roman", Font.PLAIN,20);
	public void paintComponent(Graphics g) {
		//creating background
		g.setColor(Color.black);
		g.fillRect(0, 0, Arcanoid.FRAME_WIDTH, Arcanoid.FRAME_HEIGHT);
			//creating a deck
					g.setColor(Deck.color);
					g.fillRect(Deck.x, Deck.y, Deck.width, Deck.HEIGHT);
			//creating tales
			for(Rectangles r: Rectangles.rects) {
				if(r.getIsAlive()) {//проверка того, надо ли рисовать клетку
					g.setColor(r.getColor());
					g.fillRect(r.x, r.y, r.getWidth(), r.getHeight());
					g.setColor(Color.white);
					g.drawRect(r.x, r.y, r.getWidth(), r.getHeight());
				}
			}
			for(LootBoxes l: LootBoxes.lb) {
				if(l.isAlive) {
					Graphics2D g2d = (Graphics2D) g.create();
					g2d.setPaint(new GradientPaint(l.x,l.y,Color.white,l.x + l.WIDTH/2,l.y+l.HEIGHT/2,l.color));
					g2d.fillRect(l.x, l.y, l.WIDTH, l.HEIGHT);
				}
			}
			//Creating balls
			g.setColor(Balls.color);
					for(Balls b: Balls.ball) {
						g.fillOval(b.getBallX(), b.getBallY(), b.getSize(),b.getSize());
					}
				g.setColor(Color.white);
				g.setFont(timeFont);
				g.drawRect(0, 0, 50, 30);
				g.drawString(LootBoxes.timer, 20, 20);
		if(!Arcanoid.gameIsAlive & Arcanoid.goodGame == 2) {
			g.setColor(Color.white);
			g.fillRect(0, 0, Arcanoid.FRAME_WIDTH, Arcanoid.FRAME_HEIGHT);
			g.setColor(Color.black);
			g.setFont(mainFont);
			g.drawString("You won!!!!!", 50, Arcanoid.FRAME_HEIGHT/2);
		}else if(!Arcanoid.gameIsAlive & Arcanoid.goodGame == 1){
			g.setColor(Color.black);
			g.fillRect(0, 0, Arcanoid.FRAME_WIDTH, Arcanoid.FRAME_HEIGHT);
			g.setColor(Color.red);
			g.setFont(mainFont);
			g.drawString("You died(((0(0(00", 50, Arcanoid.FRAME_HEIGHT/2);
		}
	}
}