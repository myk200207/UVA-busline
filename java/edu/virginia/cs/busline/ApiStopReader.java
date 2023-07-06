package edu.virginia.cs.hw6;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ApiStopReader implements StopReader {
    ConfigSingleton single = ConfigSingleton.getInstance();
    private List<Stop> stopList = new ArrayList<>();
    @Override
    public List<Stop> getStops() {
        try {
            StringBuilder sb = new StringBuilder();
            URL url = new URL(single.getBusStopsURL());
            BufferedReader read = new BufferedReader(new InputStreamReader(url.openStream()));
            String i;

            while ( (i = read.readLine()) != null) {
                sb.append(i).append("\n");
            }

            String text = sb.toString();
            JSONObject o = new JSONObject(text);
            JSONArray stopping = (JSONArray) o.get("stops");

            for ( int h = 0 ; h< stopping.length();h++) {
                JSONObject keys = stopping.getJSONObject(h);
                String id = keys.get("id").toString();
                String name = keys.get("name").toString();
                Object position = keys.get("position");
                JSONArray positions = (JSONArray) position;

                Stop newstop = new Stop(Integer.parseInt(id), name, Double.parseDouble(positions.get(0).toString()), Double.parseDouble(positions.get(1).toString()));

                stopList.add(newstop);
            }

            read.close();
        }catch (MalformedURLException e) {
            System.out.println("Error: not a valid URL");
        }
        catch (IOException e) {
            System.out.println("Error: Unable to read from URL");
        }
        return stopList;
    }
}