/*
 * NOTES ON SHIP MASSES:
 * Rotation speeds will be established so that the speed will be 5 degree/second
 * when the ship weighs 750 million kilograms
 * For a ship of that mass, the acceleration rate for firing engines will be 40 m/s^2
 * and it will have a health level of 100 units.
 */
 
package entities.ships;
 
import entities.ships.shipTools.orders.*;
import entities.*;
import entities.projectiles.*;
import entities.projectile_launchers.*;
import java.util.ArrayList;
import main.*;
 
/**
 *
 * @author Christopher Hittner
 */
public class Ship extends Entity implements ControlSystem{
    protected final double maxHealth;
    protected double health;
    
    protected double XZ_ROT = 0, Y_ROT = 0;
    protected double XZ_RotSpeed = 0, Y_RotSpeed = 0;
    protected double XZ_ROT_Target = 0, Y_ROT_Target = 0;
    
    protected boolean rotationTarget = false;
     
    protected double throttle = 0;
    
    //Warp Drive Stuff
    protected double warpCharge = 0;
    protected final double warpCapacity = 600 * CycleRunner.cyclesPerSecond, warpMinimum = 100 * CycleRunner.cyclesPerSecond;
    protected boolean warpCharging = false, warping = false;
    protected int warpMode = 0;
    
    //The weapons on the Ship
    protected Railgun[] railguns;
    protected LaserGun[] lasers;
    protected MissileBattery[] missiles;
    
    //The list of orders
    private ArrayList<Order> orders = new ArrayList<>();
    
    
    /**
     * @param X The x-coordinate
     * @param Y The x-coordinate
     * @param Z The x-coordinate
     * @param M The mass
     * @param R The radius/size of the ship's hit box
     * @param Railguns The number of Railguns the ship will have
     */
    public Ship(double X, double Y, double Z, double R, int Railguns, int Lasers, int Missiles){
        super(X, Y, Z, (4/3 * Math.PI * Math.pow(R, 3) * 40), R);
        //Outfits the ship with a railguns
        railguns = new Railgun[Railguns];
        for(int i = 0; i < railguns.length; i++){
            railguns[i] = new Railgun();
        }
        
        mass += 5000 * Railguns; 
        
        
        //Outfits the ship with laser guns
        lasers = new LaserGun[Lasers];
        for(int i = 0; i < lasers.length; i++){
            lasers[i] = new LaserGun();
        }
        
        mass += 50 * Lasers; 
        
        
        //Outfits the ship with missile launchers
        missiles = new MissileBattery[Missiles];
        for(int i = 0; i < missiles.length; i++){
            missiles[i] = new MissileBattery();
        }
        
        mass += 500 * Missiles; 
        
        
        //Sets the maximum and current health
        maxHealth = Math.sqrt(mass/75000);
        health = maxHealth;
    }
     
    public void move(){
        autopilot();
        rotate();
        accelerate();
        super.move();
        chargeWarpDrive();
        warp();
        weaponCycle();
    }
     
    
    //--------------------------------------------------------------------------
    //Rotation
    //--------------------------------------------------------------------------
     
    /**
     *
     * @param XZ The ratio amount of XZ to rotate by
     * @param Y The ratio amount of Y to rotate by
     * @param magnitude The percent of the ship's overall rotation ability to rotate by
     */
    public void setRotation(double XZ, double Y, double magnitude){
        double originalMagnitude = Math.sqrt(Math.pow(XZ, 2) + Math.pow(Y, 2));
        double horiz = (XZ/originalMagnitude) * (magnitude/100);
        double vert = (Y/originalMagnitude) * (magnitude/100);
        if(XZ == 0 && Y == 0){
            XZ_RotSpeed = 0;
            Y_RotSpeed = 0;
        } else {
            XZ_RotSpeed = 5.0 * Math.toRadians((7500000000.0/mass) * horiz)/CycleRunner.cyclesPerSecond;
            Y_RotSpeed = 5.0 * Math.toRadians((7500000000.0/mass) * vert)/CycleRunner.cyclesPerSecond;
        }
    }
     
    /**
     * Performs rotation based on specified values
     */
    public void rotate(){
        XZ_ROT += XZ_RotSpeed/CycleRunner.cyclesPerSecond;
        Y_ROT += Y_RotSpeed/CycleRunner.cyclesPerSecond;
        
        //Corrections in case the ship rotates beyond a certain degree value
        if(Y_ROT > Math.toRadians(90.0)){
            Y_ROT -= Math.toRadians(180.0);
            Y_ROT_Target -= Math.toRadians(180.0);
            Y_RotSpeed *= -1.0;
            XZ_ROT += Math.toRadians(180.0);
            XZ_ROT_Target += Math.toRadians(180.0);
        } else if(Y_ROT < Math.toRadians(-90.0)){
            Y_ROT += Math.toRadians(180.0);
            Y_ROT_Target += Math.toRadians(180.0);
            Y_RotSpeed *= -1.0;
            XZ_ROT += Math.toRadians(180);
            XZ_ROT_Target += Math.toRadians(180.0);
        }
        if(XZ_ROT >= Math.toRadians(360.0)){
            XZ_ROT -= Math.toRadians(360.0);
            XZ_ROT_Target -= Math.toRadians(360.0);
        } else if(XZ_ROT < 0.0){
            XZ_ROT += Math.toRadians(360.0);
            XZ_ROT_Target += Math.toRadians(360.0);
        }
    }
     
    public void setRotationTarget(double XZ, double Y){
        XZ_ROT_Target = XZ;
        Y_ROT_Target = Y;
        rotationTarget = true;
    }
    
    
    //--------------------------------------------------------------------------
    //Autopilot
    //--------------------------------------------------------------------------
    
    
    public void autopilot(){
        //Use of Orders
        String order;
        try{
            order = orders.get(0).getOrder();
        } catch(Exception e){
            order = "";
        }
        
        //This is added as a countermeasure against any crashes that could occur
        //due to attempting to creating substrings of blank or null integers
        if(order.equals("") || order.equals(null)){
            order = "(   )";
        }
        
        //This will make sure that if a Ship doesn't have rotation orders or
        //acceleration orders, it won't accelerate
        setAcceleration(0);
        setRotationTarget(XZ_ROT,Y_ROT);
        
        
        if(order.substring(0,5).equals("(ACC)")){
            //This is an acceleration order
            setAcceleration(Double.parseDouble(order.substring(5)));
            if(!orders.get(0).getStatus()){
                orders.remove(0);
            }
        } else if(order.substring(0,5).equals("(ROT)")){
            //This is an rotation order
            int border = orders.indexOf("-");
            double XZ = Double.parseDouble(order.substring(5, border));
            double Y = Double.parseDouble(order.substring(border + 1));
            setRotationTarget(XZ, Y);
            if(Math.abs(Y_ROT - Y) < Math.toRadians(500/CycleRunner.cyclesPerSecond) && Math.abs(XZ_ROT - XZ) < Math.toRadians(500/CycleRunner.cyclesPerSecond)){
                orders.remove(0);
            }
        }
        for(Order o : orders){
            //Tests for an Attack order. The Railgun will have to be coded
            //separately, as the Ship must point directly AT the target
            if(o instanceof Attack){
                order = o.getOrder();
                
                boolean fireMissile = (order.substring(6, 7).equals("T"));
                boolean fireLaser = (order.substring(7, 8).equals("T"));
                
                long targ = Long.parseLong(order.substring(9));
                
                if(fireMissile){
                    for(int i = 0; i < missiles.length; i++){
                        fireMissiles(i,targ);
                    }
                }
                
                if(fireLaser){
                    for(int i = 0; i < lasers.length; i++){
                        fireLasers(i,targ);
                    }
                }
                
                break;
            }
        }
        
        
        
        if(rotationTarget){
            int xz = 0;
            int y = 0;
            
            if(Math.abs(XZ_ROT - XZ_ROT_Target) < Math.toRadians(500/CycleRunner.cyclesPerSecond)){
                xz = 0;
            } else if(XZ_ROT < XZ_ROT_Target){
                xz = 1;
            } else if(XZ_ROT > XZ_ROT_Target){
                xz = -1;
            }
            
            if(Math.abs(Y_ROT - Y_ROT_Target) < Math.toRadians(500/CycleRunner.cyclesPerSecond)){
                y = 0;
            } else if(Y_ROT < Y_ROT_Target){
                y = 1;
            } else if(Y_ROT > Y_ROT_Target){
                y = -1;
            }
            
            setRotation(xz, y, 100);
            
            if(xz == 0 && y == 0){
                rotationTarget = false;
            }
            
        }
    }
 
    
    //--------------------------------------------------------------------------
    //Acceleration
    //--------------------------------------------------------------------------
    
    /**
     * Sets the value of the throttle to determine how quickly the ship will accelerate
     * @param thrustPercent The throttle level, in percent
     */
    public void setAcceleration(double thrustPercent){
        if(thrustPercent >= 0 && thrustPercent <= 100)
            throttle = thrustPercent;
    }
     
    /**
     * Accelerates the ship in the direction it is pointing
     */
    public void accelerate() {
        double force = 30 * 750000000.0 * throttle/100;
        velX += Math.cos(XZ_ROT) * Math.cos(Y_ROT) * (force/mass)/CycleRunner.cyclesPerSecond;
        velZ += Math.sin(XZ_ROT) * Math.cos(Y_ROT) * (force/mass)/CycleRunner.cyclesPerSecond;
        velY += Math.sin(Y_ROT) * (force/mass)/CycleRunner.cyclesPerSecond;
    }
    
    
    //--------------------------------------------------------------------------
    //Warp Drive
    //--------------------------------------------------------------------------
    
    
    /**
     * Sets the warp mode of the Ship (in multiples of the speed of light)
     * @param level
     */
    public void setWarp(int level){
        if(level > 0){
            warpMode = level;
        }
    }
    
    /**
     * Determines whether the Ship object is prepping for a warp jump
     * @param b
     */
    public void setWarpPrep(boolean b){
        warpCharging = b;
    }
    
    /**
     * Powers the warp drive up if it can be done
     */
    public void chargeWarpDrive(){
        if(((warpMode == 0 && warpCharging) || warpCharge < warpMinimum)){
            
            warpCharge += (10000000/mass) * 200;
            
            if(warpCharge > warpCapacity){
                warpCharge = warpCapacity;
            }
            
        } else if(warpMode > 0){
            
            warpCharging = false;
            warpCharge -= Math.pow(warpMode,2) * (mass/10000000);
            
            if(warpCharge < 0){
                warpCharge = 0;
            }
            
        }
    }
    
    
    /**
     * Performs a frame of the warp jump in the physics of the game
     */
    public void warp(){
        if(!warping){
            if(warpCharge >= warpMinimum && warpMode > 0){
                warping = true;
            }
        } else {
            if(warpMode == 0){
                warping = false;
            }
        }
        
        //Error check that keeps people from using an infinitely large warp power
        if(Math.pow(warpMode,2) > warpCharge){
            warpMode = (int)(Math.sqrt(warpCharge));
        }
        
        if(warping){
            x += (c * warpMode) * Math.cos(XZ_ROT) * Math.cos(Y_ROT) / CycleRunner.cyclesPerSecond;
            y += (c * warpMode) * Math.sin(Y_ROT) / CycleRunner.cyclesPerSecond;
            z += (c * warpMode) * Math.sin(XZ_ROT) * Math.cos(Y_ROT) / CycleRunner.cyclesPerSecond;
        }
    }
    
    
    //---------------------------------------------------------------------------
    //Collision stuff
    //---------------------------------------------------------------------------
    
    public void collide(Ship other){
        setWarp(0);
        other.setWarp(0);
        double relVelX = (this.velX) - (other.velX);
        double relVelY = (this.velY) - (other.velY);
        double relVelZ = (this.velZ) - (other.velZ);
        double relVel = Math.sqrt(Math.pow(relVelX,2) + Math.pow(relVelY,2) + Math.pow(relVelY,2));
        
        this.damage(Math.sqrt(other.mass/this.mass) * Math.pow(relVel,4) / this.mass);
        
        other.damage(Math.sqrt(this.mass/other.mass) * Math.pow(relVel,4) / other.mass);
        
        super.collide(other);
    }
    
    public void collide(Entity other){
        setWarp(0);
        double relVelX = this.velX - other.getSpeedX();
        double relVelY = this.velY - other.getSpeedY();
        double relVelZ = this.velZ - other.getSpeedZ();
        double relVel = Math.sqrt(Math.pow(relVelX,2) + Math.pow(relVelY,2) + Math.pow(relVelY,2));
        
        damage(Math.sqrt(other.getMass()/this.mass) * Math.pow(relVel,4) / this.mass);
        
        super.collide(other);
    }
    
    
    public void damage(double damage){
        health -= damage;
    }
    
    
    //--------------------------------
    //Railgun Shooter
    //--------------------------------
    public void fireRailgun(int index){
        try {
            railguns[index].fire(this);
        } catch (Exception ex){
            
        }
    }
    
    //--------------------------------
    //Laser Shooter
    //--------------------------------
    public void fireLasers(int index){
        try {
            lasers[index].fire(this);
        } catch (Exception ex){
            
        }
    }
    public void fireLasers(int index, long target){
        try {
            lasers[index].fire(this,EntityList.getEntity(target));
        } catch (Exception ex){
            
        }
    }
    
    
    //--------------------------------
    //Missile Shooter
    //--------------------------------
    public void fireMissiles(int index){
        try {
            missiles[index].fire(this);
        } catch (Exception ex){
            
        }
    }
    public void fireMissiles(int index, long target){
        try {
            missiles[index].fire(this,EntityList.getEntity(target));
        } catch (Exception ex){
            
        }
    }
    
    
    //--------------------------------
    //Weapon Cycling
    //--------------------------------
    private void weaponCycle() {
        for(Railgun r : railguns){
            r.cycle();
        }
        for(MissileBattery m : missiles){
            m.cycle();
        }
        for(LaserGun l : lasers){
            l.cycle();
        }
            
    }
    
    
    //--------------------------------
    //Orders
    //--------------------------------
    public void giveOrders(Order o){
        orders.add(o);
    }
    
    public void cancelOrders(int index){
        orders.remove(index);
    }
    
    public void stopAttacking(){
        for(Order o : orders){
            if(o instanceof Attack){
                orders.remove(o);
            }
        }
    }
    
    
    //---------------------------
    //Data return methods
    //---------------------------
    
    
    /**
     * Returns the ship's health
     * @return The Ship's health
     */
    public double getHealth(){
        return health;
    }
    
    /**
     * Returns the rotation on the XZ plane
     * @return The XZ Rotation
     */
    public double getXZ_ROT(){
        return XZ_ROT;
    }
    
    /**
     * Returns the rotation on the Y axis
     * @return The Y Rotation
     */
    public double getY_ROT(){
        return Y_ROT;
    }
    /**
     * Returns the rotation speed on the XZ plane
     * @return The XZ Rotation speed
     */
    public double getXZ_RotSpeed(){
        return XZ_RotSpeed;
    }
    
    /**
     * Returns the rotation speed on the Y axis
     * @return The Y Rotation speed
     */
    public double getY_RotSpeed(){
        return Y_RotSpeed;
    }

    
    
    
}

