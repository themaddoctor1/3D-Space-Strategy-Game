import entities.*;
import entities.celestialBodies.*;
import entities.ships.Ship;
import entities.ships.shipTools.orders.*;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.JFrame;
import main.*;
import physics.Orbit;

/*
 * This code is a template for creating shapes on a 3D canvas. The base code
 * includes the required data to draw a circle in 3D.
 */

/**
 *
 * @author Christopher
 */
public class Tester extends Applet {
    //This object instantiation creates an object of this class that is used
    //in creating the applet. This is done because this class is the applet
    //(it extends the Applet class). It is static because it is used by the
    //public STATIC void main(String[] args) method.
    private static Tester applet = new Tester();
    //Creates an instance of the Graphics class. Don't worry, this does not need
    //a value; it'll ask to implement the Graphics class if it is set to be
    //equal to new Graphics().
    private static Graphics graphics;
    private static JFrame frame;
    
    private static int WIDTH = 800, HEIGHT = 600;
    private static boolean debug = true;
    private static double XZ_ROT = Math.toRadians(30), Y_ROT = Math.toRadians(30);
    private static double x = 0, y = 0, z = 0;
    
    //For value n, one pixel will represent n meters
    private static double pixelMeterRatio = 5;
    
    
    public Tester(){
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        //Creates a JFrame with a title
        frame = new JFrame("3D Viewer");
        //Puts the Tester object into thhe JFrame
	frame.add(applet);
        //Sets the size of the applet to be 800 pixels wide  by 600 pixels high
	frame.setSize(WIDTH, HEIGHT);
        //Makes the applet visible
	frame.setVisible(true);
        //Sets the applet so that it can't be resized
        frame.setResizable(false);
        //This will make the program close when the red X in the top right is
        //clicked on
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        /* Tests Ships and Missiles
        EntityList.addShip(new Ship(200,0,0,10,0,0,0,1,0));
        EntityList.addShip(new Ship(-200,0,0,10,1,1,35,1,0));
        
        EntityList.getShip(1).giveOrders(new Attack(true, false, false, EntityList.getShip(0)));
        */ 
        
        /* Tests Black Holes
        EntityList.addCelestialBody(new BlackHole(0,0,0,20));
         */
        
        /*
        EntityList.addCelestialBody(new CelestialBody(0,0,0,15082956259426847.662141779788839,10));
        EntityList.addCelestialBody(new CelestialBody(100,0,0,100,10));
        
        Orbit o = new Orbit(100,Math.toRadians(90),0,Math.toRadians(90),EntityList.getCelestialBody(0));
        EntityList.getCelestialBody(1).putIntoOrbit(o);
        */
        
        EntityList.loadFormation(0, false);
        
        /* Tests CelestialBody collisions
        EntityList.addCelestialBody(new CelestialBody(100,0,0,1,10));
        EntityList.addCelestialBody(new CelestialBody(-100,0,0,1,10));
        EntityList.addCelestialBody(new CelestialBody(0,0,100,1,10));
        EntityList.addCelestialBody(new CelestialBody(0,0,-100,1,10));
        */
        int count = 0;
        //An infinite loop that only stops running when the Applet is closed
        while(true){
            //Outputs the contents of the screen
            printScreen();
            XZ_ROT += Math.toRadians(0);
            CycleRunner.executeCycle();
            count++;
            if(count%32 == 0){
                Thread.sleep(0,1);
            }
        }
        
    }
    
    /**
     * This methods is used to call the update() method. To be honest, I am
     * not sure of how this is, but I know that the way it does this prevents
     * flickering images.
     * @throws InterruptedException
     */
    public static void printScreen() throws InterruptedException{
        applet.repaint();
    }
    
    public void update(Graphics g) {
        Image image = null;
        if (image == null) {
            image = createImage(this.getWidth(), this.getHeight());
            graphics = image.getGraphics();
        }
        graphics.setColor(getBackground());
        graphics.fillRect(0,  0,  this.getWidth(),  this.getHeight());
        graphics.setColor(getForeground());
        paint(graphics);
        g.drawImage(image, 0, 0, this);
    }
    
    
    /**
     * This method outputs the shapes and stuff onto the applet
     */
    public void paint(Graphics g) {
        //Creates a better version of the graphics class
        Graphics2D g2 = (Graphics2D) g;
        
        //Debug mode that draws the origin
        if(debug){
            //X Axis
            g2.setColor(Color.red);
            g2.drawLine(frame.getWidth()/2 - (int)(Math.cos(XZ_ROT) * 200),  (frame.getHeight()/2 - 18) + (int)(Math.sin(XZ_ROT) * 200 * Math.sin(Y_ROT)), frame.getWidth()/2 + (int)(Math.cos(XZ_ROT) * 200), (frame.getHeight()/2 - 18) - (int)(Math.sin(XZ_ROT) * 200 * Math.sin(Y_ROT)));

            //Y Axis
            g2.setColor(Color.blue);
            g2.drawLine(frame.getWidth()/2 - (int)(Math.sin(XZ_ROT) * 200),  (frame.getHeight()/2 - 18) - (int)(Math.cos(XZ_ROT) * 200 * Math.sin(Y_ROT)), frame.getWidth()/2 + (int)(Math.sin(XZ_ROT) * 200), (frame.getHeight()/2 - 18) + (int)(Math.cos(XZ_ROT) * 200 * Math.sin(Y_ROT)));

            //Z Axis
            g2.setColor(Color.green);
            g2.drawLine(frame.getWidth()/2, (frame.getHeight()/2 - 18) + (int)(200 * Math.cos(Y_ROT)), frame.getWidth()/2, (frame.getHeight()/2 - 18) - (int)(200 * Math.cos(Y_ROT)));
            
            g2.setColor(Color.black);
        }
        
        ArrayList<Entity> list = EntityList.getEntityList();
        for(Entity e : list){
            drawEntity(g2, e);
        }
        
        
        
    }
    
    public void drawEntity(Graphics2D g2, Entity e){
        if(e instanceof BlackHole){
            g2.setColor(Color.BLACK);
            drawSphere(g2, e.getX(), e.getY(), e.getZ(), 2.0 * e.getRadius(), true);
        } else if(e instanceof Planet){
            Planet p = (Planet) e;
            drawSphere(g2, e.getX(), e.getY(), e.getZ(), e.getRadius(), true);
            drawSphere(g2, e.getX(), e.getY(), e.getZ(), 2.0 * p.getAtmosphereHeight(), false);
        } else {
            drawSphere(g2, e.getX(), e.getY(), e.getZ(), e.getRadius(), false);
        }
    }
    
    
    public void drawSphere(Graphics2D g, double X, double Y, double Z, double R, boolean fill){
        
        
        
        double distX = X-x;
        double distY = Y-y;
        double distZ = Z-z;
        double dist = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2) + Math.pow(distZ, 2));
        
        
        double camX = x + 250 * pixelMeterRatio * (Math.cos(Math.toRadians(90) - XZ_ROT) * Math.cos(Math.toRadians(90) - Y_ROT));
        double camY = y + 250 * pixelMeterRatio * (-Y_ROT);
        double camZ = z + 250 * pixelMeterRatio * (Math.sin(Math.toRadians(90) - XZ_ROT) * Math.cos(Math.toRadians(90) - Y_ROT));
        
        double distCamX = X+camX;
        double distCamY = Y+camY;
        double distCamZ = Z+camZ;
        double distCam = Math.sqrt(Math.pow(distCamX, 2) + Math.pow(distCamY, 2) + Math.pow(distCamZ, 2));
        
        if(dist > 250 && distCam < dist){
            return;
        }
        
        int radius = (int)(250 * R/distCam);
        
        
        if((250 * R/distCam) < 0.5){
            return;
        }
        
        double magnitudeXZ = Math.sqrt(Math.pow((X-x),2) + Math.pow((Z-z),2));
        if(magnitudeXZ == 0){
            if(fill){
                g.fillOval((int)((frame.getWidth()/2) - radius / pixelMeterRatio), (int)((frame.getHeight()/2 - 18) - radius / pixelMeterRatio), (int)(2 * radius / pixelMeterRatio), (int)(2 * radius / pixelMeterRatio));
            } else {
                g.drawOval((int)((frame.getWidth()/2) - radius / pixelMeterRatio), (int)((frame.getHeight()/2 - 18) - radius / pixelMeterRatio), (int)(2 * radius / pixelMeterRatio), (int)(2 * radius / pixelMeterRatio));
            }
            
            return;
        }
        
        double angleXZ = Math.atan((Z-z)/(X-x));
        if((X-x) < 0){
            angleXZ += Math.toRadians(180);
        }
        
        double magnitude = Math.sqrt(Math.pow(magnitudeXZ,2) + Math.pow((Y-y),2));
        double angleY = Math.atan((Y-y)/magnitudeXZ);
        
        
        double ptX = frame.getWidth()/2 - radius / pixelMeterRatio + (Math.cos(angleXZ + XZ_ROT) * magnitudeXZ / pixelMeterRatio);
        double ptY = (frame.getHeight()/2 - 18) - radius / pixelMeterRatio - (Math.sin(angleXZ + XZ_ROT) * magnitudeXZ * Math.sin(Y_ROT)) / pixelMeterRatio - Y * Math.cos(Y_ROT) / pixelMeterRatio;
        
        
        if(fill){
            g.fillOval((int)(ptX), (int)(ptY), (int)(2 * radius / pixelMeterRatio), (int)(2 * radius / pixelMeterRatio));
        } else {
            g.drawOval((int)(ptX), (int)(ptY), (int)(2 * radius / pixelMeterRatio), (int)(2 * radius / pixelMeterRatio));
        }
    }
    
    /**
     * Sets a new width for the Applet
     * @param width
     */
    public void setAppletWidth(int width){
        if(width > 0){
            WIDTH = width;
            frame.setSize(WIDTH, HEIGHT);
        }
    }
    
    /**
     * Sets a new height for the Applet
     * @param height
     */
    public void setAppletHeight(int height){
        if(height > 0){
            HEIGHT = height;
            frame.setSize(WIDTH, HEIGHT);
        }
    }
    
    /**
     * Activates or deactivates the debug mode
     * @param mode
     */
    public void setDebug(boolean mode){
        debug = mode;
    }
    
    
    
    
}
