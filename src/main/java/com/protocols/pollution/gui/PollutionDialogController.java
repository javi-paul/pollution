package com.protocols.pollution.gui;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import com.api.API;
import com.api.ArduSimTools;
import com.setup.Param;
import com.setup.Text;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PollutionDialogController {

	private final ResourceBundle resources;
	private final Stage stage;
	private final PollutionSimProperties properties;

	@FXML
	private TextField initialLatitude;
	@FXML
	private TextField initialLongitude;
	@FXML
	private TextField initialYaw;
	@FXML
	private TextField altitude;
	@FXML
	private TextField width;
	@FXML
	private TextField length;
	@FXML
	private TextField density;
	@FXML
	private TextField pThreshold;
	@FXML
	private TextField pollutionDataFile;
	@FXML
	private Button dataFileButton;
	@FXML
	private Button okButton;

	public PollutionDialogController(ResourceBundle resources, PollutionSimProperties properties, Stage stage) {
		this.resources = resources;
		this.properties = properties;
		this.stage = stage;
	}

	@FXML
	public void initialize() {
		
		//Filter double and int input
		initialLatitude.setTextFormatter(new TextFormatter<>(ArduSimTools.doubleFilter));
		initialLongitude.setTextFormatter(new TextFormatter<>(ArduSimTools.doubleFilter));
		initialYaw.setTextFormatter(new TextFormatter<>(ArduSimTools.doubleFilter));
		altitude.setTextFormatter(new TextFormatter<>(ArduSimTools.doubleFilter));
		density.setTextFormatter(new TextFormatter<>(ArduSimTools.doubleFilter));
		pThreshold.setTextFormatter(new TextFormatter<>(ArduSimTools.doubleFilter));
		width.setTextFormatter(new TextFormatter<>(integerFilter));
		length.setTextFormatter(new TextFormatter<>(integerFilter));
		
		//Data file
		dataFileButton.setOnAction(e->searchDataFile());
		pollutionDataFile.setDisable(true);
		
		
		okButton.setOnAction(e -> {
			if (ok()) {
				Platform.setImplicitExit(false); // so that the application does not close
				Param.simStatus = Param.SimulatorState.STARTING_UAVS;
				okButton.getScene().getWindow().hide();
			} else {
				ArduSimTools.warnGlobal(Text.LOADING_ERROR, Text.ERROR_LOADING_FXML);
			}
		});

	}
	
	 private void searchDataFile() {
	        FileChooser fileChooser = new FileChooser();
	        fileChooser.setInitialDirectory(new File(API.getFileTools().getResourceFolder().toString() + "/protocols/pollution"));
	        fileChooser.setTitle("Select the .txt file with the simulation values for the sensor");
	        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Data txt file", "*.txt");
	        fileChooser.getExtensionFilters().add(extFilter);
	        File f = fileChooser.showOpenDialog(stage);
	        if(f != null){
	            Path absolute = Paths.get(f.getAbsolutePath());
	            pollutionDataFile.setText(absolute.toString());
	        }else{
	            pollutionDataFile.setText("");
	        }

	    }

	private boolean ok() {
		Properties p = createProperties();
		return properties.storeParameters(p, resources);
	}

	private Properties createProperties(){
		Properties p = new Properties();
		Field[] variables = this.getClass().getDeclaredFields();
		for(Field var:variables){
			String annotation = var.getAnnotatedType().getType().getTypeName();
			if(annotation.contains("javafx")) {

				try {
					Method getValue = null;
					if (annotation.contains("TextField")) {

						getValue = var.get(this).getClass().getMethod("getCharacters");

					}
					if(getValue != null) {
						String value = String.valueOf(getValue.invoke(var.get(this)));
						p.setProperty(var.getName(), value);
					}

				} catch (NoSuchMethodException | SecurityException | IllegalArgumentException
						| IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return p;
	}
	
	private final UnaryOperator<TextFormatter.Change> integerFilter = t -> {
        if (t.isReplaced())
            if(t.getText().matches("[^0-9]"))
                t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));

        if (t.isAdded()) {
            if (t.getText().matches("[^0-9]")) {
                t.setText("");
            }
        }
        return t;
    };
}
