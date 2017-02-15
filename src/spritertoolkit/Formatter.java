/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spritertoolkit;

/**
 *
 * @author Coulibaly Falla
 */
public class Formatter{
    private static final int DEFAULT_BUFFER_SIZE = 512;
    
    private static Object[] spriteDataToVarArgs(SpriteView spriteView) {
        return new Object[]{
            spriteView.getSpriteName(),
            spriteView.getSpriteX(),
            spriteView.getSpriteY(),
            spriteView.getSpriteWidth(),
            spriteView.getSpriteHeight()
        };
    }
    
    private static String toText(SpriteView spriteView){
        return String.format("%s,%d,%d,%d,%d\n", spriteDataToVarArgs(spriteView));
    }
    
    private static String toJsonObject(SpriteView spriteView){
        String object = "\n\t{\n\t\t\"name\":%s,\n\t\t\"x\":%d,\n\t\t\"y\":%d,\n\t\t\"width\":%d,\n\t\t\"height\":%d\n\t}\n";
        
        Object fieldsValues[] = spriteDataToVarArgs(spriteView);
        
        return String.format(object, fieldsValues);
    }
    
    private static String toXmlNode(SpriteView spriteView){
        String node = "\t<SubTexture name=\"%s\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"/>\n";
        
        Object attributesValues[] = spriteDataToVarArgs(spriteView);
        
        return String.format(node, attributesValues);
    }
    
    public static String formatToJson(SpriteView spriteViews[]){
        StringBuilder jsonOutput = new StringBuilder(DEFAULT_BUFFER_SIZE);

        jsonOutput.append("[");

        for(SpriteView spriteView : spriteViews)
            jsonOutput.append(toJsonObject(spriteView));

        jsonOutput.append("]");

        return jsonOutput.toString();
    }

    public static String formatToXml(SpriteView spriteViews[], String spriteSheetPath){
        StringBuilder xmlOutput = new StringBuilder(DEFAULT_BUFFER_SIZE);

        xmlOutput.append("<TextureAtlas imagePath=").append(spriteSheetPath).append(">\n");

        for(SpriteView spriteView : spriteViews)
            xmlOutput.append(toXmlNode(spriteView));
        

        xmlOutput.append("</TextureAtlas>");

        return xmlOutput.toString();
    }

    public static String formatToText(SpriteView spriteViews[]){
        StringBuilder textOutput = new StringBuilder(DEFAULT_BUFFER_SIZE);

        for(SpriteView spriteView : spriteViews)
            textOutput.append(toText(spriteView));

        return textOutput.toString();
    }
}
