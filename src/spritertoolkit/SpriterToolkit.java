/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spritertoolkit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author Coulibaly Falla
 */
public class SpriterToolkit extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Parent root = FXMLLoader.load(getClass().getResource("SpriterToolkitView.fxml"));
        
        Scene scene = new Scene(root);
        
        
        stage.setMaxWidth(bounds.getMaxX());
        stage.setMaxHeight(bounds.getMaxY());
        
        stage.setScene(scene);
        stage.show();
        
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
        
        SpriterToolkitController.stage = stage;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
