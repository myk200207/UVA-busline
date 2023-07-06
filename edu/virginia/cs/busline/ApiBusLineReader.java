package edu.virginia.cs.hw6;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiBusLineReader implements BusLineReader {
    ConfigSingleton single = ConfigSingleton.getInstance();
    private ApiStopReader listofstops = new ApiStopReader();
    private List<BusLine> busLineLists = new ArrayList<>();
    private List<Stop> listofstops1;
    private HashMap<Integer,JSONArray> mapforbusstops = new HashMap<>();

    @Override
    public List<BusLine> getBusLines() {

        try {
            listofstops1 = listofstops.getStops();

            StringBuilder sb = new StringBuilder();
            StringBuilder sb1 = new StringBuilder();
            URL buslineURL = new URL(single.getBusLinesURL());
            URL busstopURL = new URL(single.getBusStopsURL());
            BufferedReader busStops = new BufferedReader(new InputStreamReader(busstopURL.openStream()));
            BufferedReader busLine = new BufferedReader(new InputStreamReader(buslineURL.openStream()));
            String i;
            String q;
            while ((i = busLine.readLine()) != null) {
                sb.append(i).append("\n");
            }

            while((q = busStops.readLine()) != null) {
                sb1.append(q).append("\n");
            }

            String textforbusline = sb.toString();
            String textforbusstop = sb1.toString();
            JSONObject objectforbusline = new JSONObject(textforbusline);
            JSONObject objectforbusstop = new JSONObject(textforbusstop);

            JSONArray arrayforbusline = (JSONArray) objectforbusline.get("routes");
            JSONArray arrayforbusstop = (JSONArray) objectforbusstop.get("routes");
            for(int e = 0 ; e<arrayforbusstop.length();e++){
                JSONObject key = arrayforbusstop.getJSONObject(e);
                int stopRouteId = Integer.parseInt(key.get("id").toString());
                JSONArray stopRoutestops = (JSONArray) key.get("stops");
                mapforbusstops.put(stopRouteId,stopRoutestops);
            }

            for(int o = 0 ; o < arrayforbusline.length(); o++) {
                JSONObject keys = arrayforbusline.getJSONObject(o);
                int id = Integer.parseInt(keys.get("id").toString());
                String longName = keys.get("long_name").toString();
                String shortName = keys.get("short_name").toString();
                Boolean isActive = keys.getBoolean("is_active");

                if(mapforbusstops.get(id) != null) {
                    if(mapforbusstops.get(id).length() > 0) {
                        List<Stop> stopsforthebus = new ArrayList<>();
                        for(int k = 0; k< mapforbusstops.get(id).length(); k++){
                            int onestop = Integer.parseInt(mapforbusstops.get(id).get(k).toString());
                            for(int u = 0 ; u < listofstops1.size(); u++){
                                if(onestop == listofstops1.get(u).getId()) {
                                    stopsforthebus.add(listofstops1.get(u));
                                }
                            }
                        }
                        Route busroute = new Route(stopsforthebus);
                        BusLine busLineObject = new BusLine(id,isActive,longName,shortName,busroute);
                        busLineLists.add(busLineObject);
                    }
                }
            }
        }catch (MalformedURLException e) {
            System.out.println("Error: not a valid URL");
        }
        catch (IOException e) {
            System.out.println("Error: Unable to read from URL");
        }

        return busLineLists;
    }
}