/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spritertoolkit;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

/**
 *
 * @author Coulibaly Falla
 */
public final class SpriteSheetUtils {
    private static int pixels[];
    private static int imageWidth;
    private static int imageHeight;
    private static List<Rectangle> boxes;
    private static WritablePixelFormat<IntBuffer> format;
    private static PixelReader reader;
    
    public enum MAPPING{ GRID, CONTOUR }
    
    private SpriteSheetUtils(){}
    
    private static void setData(Image image){
        SpriteSheetUtils.imageWidth = (int)image.getWidth();
        SpriteSheetUtils.imageHeight = (int)image.getHeight();
        SpriteSheetUtils.format = WritablePixelFormat.getIntArgbInstance();
        SpriteSheetUtils.reader = image.getPixelReader();
        
        pixels = new int[imageWidth * imageHeight];
        reader.getPixels(0, 0, imageWidth, imageHeight, format, pixels, 0, imageWidth);
    }
    
    private static SpriteView[] createImagesFromBoxes(PixelReader reader){
        SpriteView spriteViews[] = new SpriteView[boxes.size()];
        WritableImage images[] = new WritableImage[spriteViews.length];
        
        for(int i = 0; i < boxes.size(); i++){
            Rectangle rec = boxes.get(i);
            int recWidth = (int)rec.getWidth();
            int recHeight = (int)rec.getHeight();

            images[i] = new WritableImage(recWidth, recHeight);
            int imageData[] = new int[recWidth * recHeight];

            reader.getPixels(rec.x, rec.y, recWidth, recHeight, format, imageData, 0, recWidth);
            images[i].getPixelWriter().setPixels(0, 0, recWidth, recHeight, (PixelFormat)format, imageData, 0, recWidth);
            
            Sprite sprite = new Sprite(rec.x, rec.y, rec.width, rec.height, "Sprite " + i);
            
            spriteViews[i] = new SpriteView(i, sprite, images[i]);
        }
        
        return spriteViews;
    }
    
    public static SpriteView[] extractSprites(Image image, MAPPING choice, int param1, int param2){
        boxes = new ArrayList<>();
        
        if(image != null)
            SpriteSheetUtils.setData(image);
        
        switch(choice){
            case GRID:
                GridMapper.mapSprite(param1, param2);
                break;
                
            default:
                ContourMapper.mapSprite(param1, param2);
        }
        
        return createImagesFromBoxes(reader);
    }
    
    
    
    /*param1 will be used as the correction amount for the boxes in this class
     *param2 will be used as the merge distance between pixels in this class
     */
    private static class ContourMapper{
        private static int mergeDistance;
        private static int correction;
        private static int bgColor;
        private static List<Point> outerPixels;
        private static List<List<Point>> contours;
        
        //Convert 2D array coordinates to 1D
        private static int to1D(int x, int y){
            return (x * imageWidth)  + y;   
        }
        
        private static boolean canMergePoints(Point p1, Point p2){
            return p1.distance(p2.x, p2.y) <= mergeDistance;
        }

        private static void addContourPixel(int x, int y){
            outerPixels.add(new Point(x, y));
        }

        private static Point findMaxXandY(List<Point> cont){
            Point maxes = new Point(cont.get(0));

            for(Point p : cont){
                if(maxes.x < p.x)
                    maxes.x = p.x;

                if(maxes.y < p.y)
                    maxes.y = p.y;
            }

            return maxes;
        }

        private static Point findMinsXandY(List<Point> cont){
            Point mins = new Point(cont.get(0));

            for(Point p : cont){
                if(mins.x > p.x)
                    mins.x = p.x;

                if(mins.y > p.y)
                    mins.y = p.y;
            }

            return mins;
        }

        private static boolean hasFourNeighbors(int x, int y){
            //A pixel at the edge of the image cannot have 4 or more neighbors
            if(x == 0 || x == imageHeight - 1 || y == 0 || y == imageWidth - 1)
                return false;

            return pixels[to1D(x, y + 1)] != bgColor &&
                    pixels[to1D(x + 1, y)] != bgColor &&
                    pixels[to1D(x - 1, y)] != bgColor &&
                    pixels[to1D(x, y - 1)] != bgColor;
        }

        private static void findBackGroundColor(){

            int previousPixel;
            int count = 1;
            int maxCount = 1;

            int buffer[] = Arrays.copyOf(pixels, pixels.length);
            Arrays.sort(buffer);

            previousPixel = buffer[0];
            bgColor = buffer[0];

            for(int i = 1; i < buffer.length; i++){
                if(buffer[i] == previousPixel){
                    count++;
                } else{
                    if(count > maxCount){
                        maxCount = count;
                        bgColor = previousPixel;
                    }

                    previousPixel = buffer[i];
                    count = 1;
                }
            }

            if(count > maxCount) 
                bgColor = buffer[buffer.length - 1];
        }

        private static void createNewContour(Point pixel){
            contours.add(new ArrayList<>());
            contours.get(contours.size() - 1).add(pixel);
        }
        
        private static void mergeWithContour(Point pixel){
            for (List<Point> contour : contours) {
                for(Point p : contour){
                    if(canMergePoints(p, pixel)){
                        contour.add(pixel);
                        return;
                    }
                }
            }

            /*The pixel could not be appended to any of the existing contour
             *thus we are creating a new contour where the pixel will be appended
             */
            createNewContour(pixel);
        }

        private static boolean mergeWithContour(List<Point> cont){
            boolean success = false;

            for(int i = 0; i < contours.size(); i++){
                List<Point> contour = contours.get(i);
                if(!cont.equals(contour)){
                    for(Point p1 : cont){

                        if(contour.stream().anyMatch((p2)-> (canMergePoints(p1, p2)))){
                            /*Should append to the object reference
                             * and remove the merge contour from the main list
                             */
                            cont.addAll(contour);
                            contours.remove(i);
                            success = true;
                            break;
                        }
                    }
                }
            }

            return success;
        }

        private static void mergeAdjacentContours(){
            /*Everytime a merge succeeds we decrease the size of the list
             *before we restart the loop
             */
            for(int i = 0; i < contours.size() - 1; i++)
                if(mergeWithContour(contours.get(i)))
                    i = 0;

        }

        //Pixels will be merged depending on how far they are from each other.
        private static void mergeOuterPixelsAsContours(){
            //Create the first contour
            createNewContour(outerPixels.get(0));

            for(int i = 1; i < outerPixels.size(); i++)
                mergeWithContour(outerPixels.get(i));

        }

        private static boolean extractSpriteOuterPixels(){
            int index;

            for(int x = 0; x < imageHeight; x++){
                for(int y = 0; y < imageWidth; y++){
                    index = to1D(x, y);
                    if(pixels[index] != bgColor){
                        if(!hasFourNeighbors(x, y)){
                            addContourPixel(y, x);
                        }
                    }
                }
            }

            return !outerPixels.isEmpty();
        }
        
        private static void createBoxesFromContours(){
            int w ;
            int h;
            Point mins;
            Point maxes;

            for(List<Point> contour : contours){
                //Find min and max at the same time; possible improvement?
                mins = findMinsXandY(contour);
                maxes = findMaxXandY(contour);

                w = maxes.x - mins.x + correction;
                h = maxes.y - mins.y + correction;

                if(w + mins.x > imageWidth)
                    w = imageWidth - maxes.x;

                if(h + mins.y > imageHeight)
                    h = imageHeight - maxes.y;

                boxes.add(new Rectangle(mins.x, mins.y, w, h));
            }
        }
        
        public static void mapSprite(int correction, int mergeDistance){
            contours = new ArrayList<>();
            outerPixels = new ArrayList<>();
            ContourMapper.correction = correction;
            ContourMapper.mergeDistance = mergeDistance;
            
            findBackGroundColor();
            
            if(extractSpriteOuterPixels())
                mergeOuterPixelsAsContours();
        
            mergeAdjacentContours();

            createBoxesFromContours();
        }
    }
    
    /*param1 will be used as the number of rows in this class
     *param2 will be used as the number of columns in this class
     */
    private static class GridMapper{
        
        public static void mapSprite(int rows, int columns){
            int x, y;
            int W = imageWidth / columns;
            int H = imageHeight / rows;
            
            for(int r = 0; r < rows; r++){
                for(int c = 0; c < columns; c++){
                    x = c * W;
                    y = r * H;
                    
                    if(W + x >= imageWidth)
                        x = W - imageWidth;
                    
                    if(H + y >= imageHeight)
                        y = H - imageHeight;
                    
                    boxes.add(new Rectangle(x, y, W, H));
                }
            }
        }
        
    }
    
}
