/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spritertoolkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import static spritertoolkit.SpriteSheetUtils.MAPPING;

/**
 *
 * @author Coulibaly Falla
 */
public class SpriterToolkitController implements Initializable {
    
    @FXML private HBox rootView;
    @FXML private VBox settingsVB;
    @FXML private TextField spriteName;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField widthField;
    @FXML private TextField heightField;
    @FXML private TextField filenameField;
    @FXML private TextArea formatPreview;
    @FXML private FlowPane previewsViewport;
    @FXML private ImageView preview;
    @FXML private ChoiceBox mappingChoice;
    @FXML private ChoiceBox exportFormat;
    @FXML private Label mappingOption1Label;
    @FXML private Label mappingOption2Label;
    @FXML private TextField mappingParam1Field;
    @FXML private TextField mappingParam2Field;
    
    private static final String CLASS_NAME = SpriterToolkitController.class.getName();
    private static final String OPTIONS[] = 
            new String[]{"Rows:", "Columns:", "Correction:", "Merge Distance:"};
    private static final int DEFAULT_CORRECTION = 1;
    private static final int DEFAULT_MERGE_DISTANCE = 2;
    private static final int DEFAULT_ROWS = 1;
    private static final int DEFAULT_COLUMNS = 1;
    
    private MAPPING mapping;
    private Image spriteSheet;
    private FileChooser fileChooser;
    private DirectoryChooser dirChooser;
    private SpriteView[] spriteViews;
    private int activeSpriteID = -1;
    private boolean imageHasChanged = true;
    private boolean dataHasChanged = false;
    private boolean spritesheetIsMapped = false;
    private String spriteSheetPath;
    private String jsonOutput = null;
    private String xmlOutput = null;
    private String textOutput = null;
    
    public static Stage stage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        fileChooser = new FileChooser();
        dirChooser = new DirectoryChooser();
        
        //Make sure we always have enough room to display the settings panel
        rootView.widthProperty().addListener((e) ->{
            settingsVB.setMinWidth(rootView.getWidth() * 0.36);
        });
        
        mappingChoice.setOnAction((event)->{
            String mappingOption = (String)mappingChoice.getValue();
            int index = mappingOption.equals("Grid") ? 0 : 2;
            
            mappingOption1Label.setText(OPTIONS[index]);
            mappingOption2Label.setText(OPTIONS[index + 1]);
        });
        
        spriteName.setOnAction((event)->{
            String name = spriteName.getText();
            
            if(!name.isEmpty()){
                spriteViews[activeSpriteID].setSpritename(name);
                updateExportFormatPreview();
            }
        });
        
        exportFormat.setOnAction((event)->{
            updateExportFormatPreview();
        });
        
    } 
    
    private boolean isMappingChoiceGrid(){
        return ((String)mappingChoice.getValue()).equals("Grid");
    }
    
    private void updateExportFormatPreview(){
        if(spritesheetIsMapped){
                String choice = (String)exportFormat.getValue();

                if(choice.equals("json")){
                    /*Since JSON is the default choice, we only care about when
                     *the data ha been changed
                     */
                    if(dataHasChanged )
                        jsonOutput = Formatter.formatToJson(spriteViews);
                    formatPreview.setText(jsonOutput);

                } else if (choice.equals("xml")){
                    if( dataHasChanged || null == xmlOutput )
                        xmlOutput = Formatter.formatToXml(spriteViews, spriteSheetPath);
                    formatPreview.setText(xmlOutput);

                } else{
                    if( dataHasChanged || null == textOutput )
                        textOutput = Formatter.formatToText(spriteViews);
                    formatPreview.setText(textOutput);
                }
            }
    }
    
    private void runMapping(){
        int param1 = -1;
        int param2 = -1;
        boolean mappingChoiceIsGrid = isMappingChoiceGrid();
        
        try{
            param1 = Integer.parseInt(mappingParam1Field.getText());
        } catch(NumberFormatException e){
            param1 = mappingChoiceIsGrid ? DEFAULT_ROWS : DEFAULT_CORRECTION;
        } finally {
            if(param1 <= 0)
                param1 = 1;
        }
        
        try{
            param2 = Integer.parseInt(mappingParam2Field.getText());
        } catch(NumberFormatException e){
            param2 = mappingChoiceIsGrid ? DEFAULT_COLUMNS :DEFAULT_MERGE_DISTANCE;
        } finally {
            if(param2 <= 0)
                param2 = 1;
        }
        
        //Keep the UI is updated
        mappingParam1Field.setText(String.valueOf(param1));
        mappingParam2Field.setText(String.valueOf(param2));
        
        mapping = mappingChoiceIsGrid ? MAPPING.GRID : MAPPING.CONTOUR;
        
        //Make a copy of the sheet before you clear the view
        if(spriteSheet == null)
            spriteSheet = ((ImageView)previewsViewport.getChildren().get(0)).getImage();
        
        /*We only want to modify the data used by SpriteSheetUtils when
         *we are dealing with a new image. 
         *"null" is just indicating that the data does not need to be reset
         */
        spriteViews = SpriteSheetUtils.extractSprites(imageHasChanged ? spriteSheet : null, mapping, param1, param2);
    }
    
    private void initSpriteViews(){
        previewsViewport.getChildren().clear();
        
        for(SpriteView spriteView : spriteViews){
            spriteView.setOnMouseClicked((m) ->{
                updateSettingFields(spriteView);
            });
        }
        
        previewsViewport.getChildren().addAll(spriteViews);
        
        updateSettingFields(spriteViews[0]);
        
    }
    
    private void updateSettingFields(SpriteView sprite){
        activeSpriteID = sprite.getIndex();
        spriteName.setText(sprite.getSpriteName());
        xField.setText(String.valueOf(sprite.getSpriteX()));
        yField.setText(String.valueOf(sprite.getSpriteY()));
        widthField.setText(String.valueOf(sprite.getSpriteWidth()));
        heightField.setText(String.valueOf(sprite.getSpriteHeight()));
    }
    
    private void exportSpriteViewAsImage(int index, File folder){
        SpriteView spriteView = spriteViews[index];
        StringBuilder path = new StringBuilder();
        path.append(folder.getPath()).append( "\\");
        path.append(spriteView.getSpriteName()).append(".png");

        try{
            File out = new File(path.toString());
            ImageIO.write(SwingFXUtils.fromFXImage(spriteView.getImage(), null), "png", out);
        } catch (IOException ex){
            Logger.getLogger(CLASS_NAME)
                            .log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void openSpriteSheet(ActionEvent event){
        File file = fileChooser.showOpenDialog(stage);
        
        if(file != null){
            Image image;
            try {
                image = new Image(new FileInputStream(file));
            } catch (Exception ex) {
                Logger.getLogger(CLASS_NAME).log(Level.SEVERE, null, ex);
                return;
            }
            
            spriteSheetPath = file.getPath();
            previewsViewport.getChildren().clear();
            preview.setImage(image);
            previewsViewport.getChildren().add(preview);
            spritesheetIsMapped = false;
            imageHasChanged = true;
            dataHasChanged = true;
        }
    }
    
    @FXML
    private void resetMappingParameters(ActionEvent event){
        int param1;
        int param2;
        boolean mappingChoiceIsGrid = isMappingChoiceGrid();
        
        if (mappingChoiceIsGrid) {
            param1 = DEFAULT_ROWS;
            param2 = DEFAULT_COLUMNS;
        } else {
            param1 = DEFAULT_CORRECTION;
            param2 = DEFAULT_MERGE_DISTANCE;
        }
        
        mappingParam1Field.setText(String.valueOf(param1));
        mappingParam2Field.setText(String.valueOf(param2));
    }
    
    @FXML
    private void mapSpriteSheet(ActionEvent event) {
        if(preview.getImage() != null){
            runMapping();
            initSpriteViews();
            
            spritesheetIsMapped = true;
            imageHasChanged = false;
            dataHasChanged = true;

            updateExportFormatPreview();
        }
    }
    
    @FXML
    private void spriteExporter(ActionEvent event) {
        File folder = dirChooser.showDialog(stage);
        if(folder != null){
            if(spritesheetIsMapped){
                String srcName = ((Button)event.getSource()).getText();
                if( srcName.equals("Export All")){
                        for(int i = 0; i < spriteViews.length; i++)
                            exportSpriteViewAsImage(i, folder);
                } else {
                    exportSpriteViewAsImage(activeSpriteID, folder);
                }
            }
        }
        
            
    }
    
    @FXML
    private void exportSpritesheetData(ActionEvent event){
        File folder = dirChooser.showDialog(stage);
        
        if(folder != null){
            BufferedWriter writer = null;
            String name = filenameField.getText();
            
            if(name.isEmpty()){
                name  = "data";
                filenameField.setText(name);
            }
            
            String fileformat = (String)exportFormat.getValue();
            String filename =  name + "." + fileformat;
            File out = new File(folder.getPath()+ "\\" + filename);
            
            try{
                writer = Files.newBufferedWriter(out.toPath(), Charset.forName("US-ASCII"));
                
                switch(fileformat){
                case "json":
                    writer.write(jsonOutput, 0, jsonOutput.length());
                    break;
                case "xml":
                    writer.write(xmlOutput, 0, xmlOutput.length());
                    break;
                default:
                    writer.write(textOutput, 0, textOutput.length());
                }
            } catch(IOException ex){
                Logger.getLogger(CLASS_NAME)
                        .log(Level.SEVERE, "Error occurred while trying to create/write to the file ", ex);
            } finally{
                if(writer != null){
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        Logger.getLogger(CLASS_NAME)
                                .log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
}
