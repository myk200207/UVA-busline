package edu.virginia.cs.hw6;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ConfigSingleton {
    private static final String configurationFileName = "config.json";
    private static ConfigSingleton instance;
    private String busStopsURL;
    private String busLinesURL;
    private String databaseName;

    private ConfigSingleton()  {
        setFieldsFromJSON();
    }

    public static ConfigSingleton getInstance()  {
        if (instance == null) {
            instance = new ConfigSingleton();
        }
        return instance;
    }

    public String getBusStopsURL() {
        return busStopsURL;
    }

    public String getBusLinesURL() {
        return busLinesURL;
    }

    public String getDatabaseFilename() {
        return databaseName;
    }

    private void setFieldsFromJSON() {
        StringBuilder sb = new StringBuilder();
        ClassLoader classLoader = getClass().getClassLoader();
        try {
        String filename = classLoader.getResource("edu.virginia.cs.hw6/" + configurationFileName).getFile();
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = br.readLine();
            }

            String text = sb.toString();
            JSONObject o = new JSONObject(text);
            JSONObject endpoints = (JSONObject)o.get("endpoints");
            databaseName = o.getString("database");
            busLinesURL = (String) endpoints.get("lines");
            busStopsURL = (String) endpoints.get("stops");

        }
        catch(IOException e) {
            System.out.println("Error: unable to read JSON file");
        }
        catch(NullPointerException e ) {
            if(e.toString().contains("java.lang.ClassLoader.getResource(String)")) {
                throw new IllegalStateException("The file name is not correct,missing,or corrupt");
            }
        }

        //TODO: Population the three fields from the config.json file
    }
}
