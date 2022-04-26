package com.protocols.pollution.gui;

import com.api.API;
import com.api.ArduSimTools;
import com.api.formations.FormationFactory;
import com.setup.Text;
import com.api.formations.Formation;
import com.api.masterslavepattern.safeTakeOff.TakeOffAlgorithm;
import com.protocols.pollution.logic.PollutionParam;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class PollutionSimProperties {
	
	public double initialLatitude;
	public double initialLongitude;
	public double initialYaw;
	public double altitude;
	public int width;
	public int length;
	public double density;
	public double pThreshold;
	public String pollutionDataFile;	

	public boolean storeParameters(Properties guiParams, ResourceBundle fileParams) {
        // First check if there are parameters set in the file who are not accessed by the gui
        Properties parameters = new Properties();
        // the file always consist of all the parameters but sometimes the value could be different because it is set in the GUI
        for(String key :fileParams.keySet()){
            if(guiParams.containsKey(key)){
                String guiValue = guiParams.getProperty(key);
                parameters.setProperty(key,guiValue);
            }else{
                String fileValue = fileParams.getString(key);
                parameters.setProperty(key,fileValue);
            }
        }
        Iterator<Object> itr = parameters.keySet().iterator();
        // get all the fields in this class
        Field[] variables = this.getClass().getDeclaredFields();
        Map<String,Field> variablesDict = new HashMap<>();
        for(Field var:variables){variablesDict.put(var.getName(),var);}

        // loop through all the parameters in the file
        while(itr.hasNext()){
            String key = itr.next().toString();
            String value = parameters.getProperty(key);
            if(!variablesDict.containsKey(key)){continue;}
            Field var = variablesDict.get(key);
            // set the value of the variable
            try {
                String type = var.getType().toString();
                //System.out.println(var.getName() + "\t" + type + "\t" + value);
                if(type.equals("int")){
                    var.setInt(this,Integer.parseInt(value));
                }else if(type.equals("double")){
                    var.setDouble(this,Double.parseDouble(value));
                }else if(type.contains("java.lang.String")){
                    var.set(this,value);
                }else if(type.contains("java.io.File")) {
                    var.set(this, new File(API.getFileTools().getResourceFolder() + File.separator + value));
                }else if(type.contains("Formation")){
                    var.set(this, FormationFactory.newFormation(Formation.Layout.valueOf(value.toUpperCase())));
                }else if(type.contains("TakeOffAlgorithm")){
                    var.set(this,TakeOffAlgorithm.getAlgorithm(value));
                }else{
                    ArduSimTools.warnGlobal(Text.LOADING_ERROR, Text.ERROR_STORE_PARAMETERS + type);
                    return false;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }
        if(specificCheckVariables()){
            setSimulationParameters();
            return true;
        }else{
            return false;
        }
    }

	private void setSimulationParameters() {
		PollutionParam.initialLatitude = initialLatitude;
		PollutionParam.initialLongitude = initialLongitude;
		PollutionParam.initialYaw = initialYaw;
		PollutionParam.altitude = altitude;
		PollutionParam.width = width;
		PollutionParam.length = length;
		PollutionParam.density = density;
		PollutionParam.pThreshold = pThreshold;
		PollutionParam.pollutionDataFile = pollutionDataFile;	
		
	}

	private boolean specificCheckVariables() {
		if(altitude <= 0)return false;
        if(width <= 0) return false;
        if(length <= 0) return false;
        if(density <=0) return false;
        if(pThreshold < 0) return false;
        if(pollutionDataFile.length() == 0) return false;

        return true;
	}

}
