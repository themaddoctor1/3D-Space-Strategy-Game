/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package engine.object_wrappers;

import engine.entities.Entity;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Christopher
 */
public class EntityListWrapper implements Serializable{
    private ArrayList<Entity> entities;
    
    public EntityListWrapper(ArrayList<Entity> e){
        entities = e;
    }
    
    public ArrayList<Entity> getContents(){
        return entities;
    }
}
