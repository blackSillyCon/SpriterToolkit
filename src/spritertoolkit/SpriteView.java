/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spritertoolkit;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Coulibaly Falla
 */
public class SpriteView extends ImageView{
    
    private final int id;
    private Sprite sprite;
    
    public SpriteView(int id, Sprite sprite, Image data)
    {
        super(data);
        this.id = id;
        this.sprite = sprite;
    }

    public String getSpriteName() 
    { 
        return sprite.getName(); 
    }

    public void setSpritename(String name) 
    { 
        this.sprite.setName(name);
    }

    public int getIndex() 
    { 
        return id; 
    }
    
    public int getSpriteX(){
        return sprite.getX();
    }
    
    public int getSpriteY(){
        return sprite.getY();
    }
    
    public int getSpriteWidth(){
        return sprite.getWidth();
    }
    
    public int getSpriteHeight(){
        return sprite.getHeight();
    }
}
