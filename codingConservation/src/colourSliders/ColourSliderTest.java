package colourSliders;


import colourSliders.ColourArray.ColourArrayType;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
 
public class ColourSliderTest extends Application {
	
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Create the colour slider pane. 
     */
    private Pane createColourSliderPane() {
    	
    	BorderPane borderPane = new BorderPane(); 
    	
    
    	
    	//create two range sliders, one vertical and one horizontal. 
    	ColourRangeSlider cSlider1 = new ColourRangeSlider(0, 100, 10, 90); 
    	cSlider1.setOrientation(Orientation.VERTICAL);
    	cSlider1.setPadding(new Insets(5,5,5,5));
    	cSlider1.setShowTickLabels(true);
    	cSlider1.setShowTickMarks(true);
    	
    	//unused
//    	ColourRangeSlider cSlider2 = new ColourRangeSlider(0, 100, 10, 90); 
//    	cSlider2.setOrientation(Orientation.HORIZONTAL);
//    	cSlider2.setPadding(new Insets(5,5,5,5));
//    	cSlider2.setShowTickLabels(true);
//    	cSlider2.setShowTickMarks(true);

    	//create combo box to select colours. 
    	ComboBox<String> colourSelection = new ComboBox<String>();
    	colourSelection.setPadding(new Insets(5,5,5,5));

    	for (int i=0; i<ColourArrayType.values().length; i++) {
    		colourSelection.getItems().add(ColourArray.getName(ColourArrayType.values()[i])); 
    	}
    	
    	VBox cSliderHolder = new VBox(); 
    	cSliderHolder.setPadding(new Insets(20,5,5,5));
    	cSliderHolder.setSpacing(15);
    	cSliderHolder.setAlignment(Pos.CENTER);
    	VBox.setVgrow(cSlider1, Priority.ALWAYS);
    	cSliderHolder.getChildren().addAll(cSlider1); 

    	colourSelection.setOnAction((action)->{
    		cSlider1.setColourArrayType(ColourArrayType.values()
    				[colourSelection.getSelectionModel().getSelectedIndex()]);
//    		cSlider2.setColourArrayType(ColourArrayType.values()
//    				[colourSelection.getSelectionModel().getSelectedIndex()]);
    	});
    	colourSelection.getSelectionModel().select(0);

    	
    	borderPane.setTop(colourSelection); 
    	borderPane.setCenter(cSliderHolder);
    	BorderPane.setAlignment(colourSelection, Pos.CENTER);
    	
    	return borderPane;   	
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello Colour Slider");
        
        StackPane root = new StackPane();
        root.getChildren().add(createColourSliderPane());
        primaryStage.setScene(new Scene(root, 200, 600));
        primaryStage.show();
    }
}
