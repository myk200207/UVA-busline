package edu.virginia.cs.hw6;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class test {

    public static void main(String[] args) throws IOException, SQLException {
        ConfigSingleton single = ConfigSingleton.getInstance();
        ApiStopReader test = new ApiStopReader();
        List<Stop> stops = test.getStops();
        ApiBusLineReader buslineTest = new ApiBusLineReader();
        List<BusLine> buslines = buslineTest.getBusLines();

        //database test
        DatabaseManagerImpl databasemanager = new DatabaseManagerImpl();
        databasemanager.connect();

    //    databasemanager.deleteTables();
        databasemanager.createTables();
//        databasemanager.clear();
        databasemanager.addStops(stops);

        //testing get allstops;
//        List<Stop> getallstopsize = databasemanager.getAllStops();
//
//        for(int i = 0 ; i<getallstopsize.size(); i++){
//            System.out.println(getallstopsize.get(i).getName());
//        }
        //testing stopbyID
  //      stopobjecttest(databasemanager.getStopByID(4235106));
        //testing stopbyname
 //       stopobjecttest(databasemanager.getStopByName("alderman rd"));
       databasemanager.addBusLines(buslines);
//        System.out.println(databasemanager.getBusLines().size());
        databasemanager.disconnect();
   //            System.out.println(databasemanager.getBusLineById(4013468));
    //    System.out.println(databasemanager.getBusLineByLongName("GOLD LINE"));
//        System.out.println(databasemanager.getBusLineByShortName(""));
//        Stop getstopbyname = databasemanager.getStopByName("Alderman Rd @ Gooch/Dillard (Southbound)");
        //testing stopbyname
       //stopobjecttest(getstopbyname);
        //testing getbusshortname
            //buslineobject(getshortname);
//        testing getbuslongname
            //buslineobject(getbuslongnametest);
        //testing getbusinesbyid
        //buslineobject(busobject);


        //List<BusLine> listofbus2 = databasemanager.getBusLines();
//        for(int i = 0; i<listofbus2.size();i++){
//            for(int j = 0 ; j<listofbus2.get(i).getRoute().size();j++){
//                System.out.println(listofbus2.get(i).getRoute().get(j).getId());
//            }
//
//        }
        //testsing getallstops.
//        List<Stop> listofstops = databasemanager.getAllStops();
//        for(int i = 0 ; i <listofstops.size();i++){
//            stopobjecttest(listofstops.get(i));
//        }


        //busline test
//        for(int i = 0; i<buslines.size();i++) {
//            System.out.println("busline id" + buslines.get(i).getId());
//            System.out.println(buslines.get(i).getLongName());
//            System.out.println(buslines.get(i).getShortName());
//            for(int j = 0; j<buslines.get(i).getRoute().size();j++){
//                System.out.println("routeid " + buslines.get(i).getRoute());
//                System.out.println("routename " + buslines.get(i).getRoute().get(j).getName());
//            }
//        }
            //list of stops
//        for (int i = 0; i < stops.size(); i++) {
//            System.out.println(stops.get(i).getId());
//            System.out.println(stops.get(i).getName());
//            System.out.println(stops.get(i).getLongitude());
//            System.out.println(stops.get(i).getLatitude());
//
//        }
//        System.out.println("busline " + single.getBusLinesURL());
//        System.out.println("busstop " +single.getBusStopsURL());
//        System.out.println("getdatabase " + single.getDatabaseFilename());


        }

    private static void stopobjecttest(Stop getstopbyname) {
        System.out.println("stopid" + getstopbyname.getId());
        System.out.println("stopname " + getstopbyname.getName());
        System.out.println("stoplat" + getstopbyname.getLatitude());
        System.out.println("stoplong" +getstopbyname.getLongitude());
    }

    private static void buslineobject(BusLine getbuslongnametest) {
        System.out.println(getbuslongnametest.getId());
        System.out.println(getbuslongnametest.getLongName());
        System.out.println(getbuslongnametest.getShortName());
        for(int i = 0; i< getbuslongnametest.getRoute().size(); i++){
            System.out.println("stopid "+ getbuslongnametest.getRoute().get(i).getId());
            System.out.println("stopname " + getbuslongnametest.getRoute().get(i).getName());
            System.out.println("stoplatitude "+ getbuslongnametest.getRoute().get(i).getLatitude());
            System.out.println("stoplongtidue " + getbuslongnametest.getRoute().get(i).getLongitude());

        }
    }


}

