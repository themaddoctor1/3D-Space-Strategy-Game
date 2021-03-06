/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package client.threads;

import client.gui.menu.GameMenu;
import client.gui.menu.MenuManager;
import client.gui.menu.PregameMenu;
import client.main.Client;
import engine.entities.Entity;
import engine.main.EntityList;
import java.io.Serializable;

/**
 *
 * @author Christopher
 */
public class CycleThread extends Thread{
    
    public CycleThread(){
    }
    
    @Override
    public void run(){
        
        int statusTick = 0, gameTick = 500;
        
        while(true){
            if(!(MenuManager.getMenu() instanceof GameMenu))
                Client.getGUI().redraw();
            
            //System.out.println("EntityList size: " + EntityList.getEntityList().size());
            
            try{
                MenuManager.getMenu().cycle();
            } catch(Exception e){
                
            }
            
            
            
            if(statusTick >= 1000){
                
                try{
                    
                    if(Client.getConnection().listening()){
                        Client.getConnection().sendObject("[SEND][STATUS]");
                    }
                    
                } catch(Exception e){
                    
                }
                
                statusTick = 0;
                
            }
            if(gameTick >= 1000){
                
                try {
                    Serializable request;
                    Client.getGUI().redraw();
                    if(MenuManager.getMenu() instanceof GameMenu){
                        request = "[SEND][ENTITYLIST]";
                    } else if(MenuManager.getMenu() instanceof PregameMenu){
                        request = "[SEND][GAMEMODE]";
                    } else
                        request = null;
                    
                    if(request != null)
                        Client.getConnection().sendObject(request);
                    
                } catch(Exception e){
                    
                }
                
                gameTick = 0;
            }
            
            
            try{
                if(MenuManager.getMenu().connects()){
                    gameTick++;
                    statusTick++;
                }
            } catch(Exception e){
                
            }
        }
        
    }
    
}
