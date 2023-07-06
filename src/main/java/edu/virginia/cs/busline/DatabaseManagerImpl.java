package edu.virginia.cs.hw6;

import com.sun.jdi.connect.spi.Connection;
import org.sqlite.ExtendedCommand;
import org.sqlite.SQLiteException;

import javax.xml.transform.Result;
import java.sql.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseManagerImpl implements DatabaseManager {

    java.sql.Connection connection;
    public ApiStopReader stopreader = new ApiStopReader();
    public List<Stop> listofstops = new ArrayList<>();
    public List<BusLine> listofbusline = new ArrayList<>();

    public ConfigSingleton config = ConfigSingleton.getInstance();

    public String path = config.getDatabaseFilename();

    @Override
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (SQLException e) {
            throw new IllegalStateException("Error: database is already connected");
        }
    }

    @Override
    public void createTables() throws SQLException {
        if(connection == null) {
            throw new IllegalStateException("Error : DATABASE IS NOT CONNECTED");
        }else if (connection.isClosed()){
            throw new IllegalStateException("Error :Connection is closed.");
        }
        try {
            PreparedStatement createStopTable = connection.prepareStatement("CREATE TABLE  Stops(ID INTEGER NOT NULL PRIMARY KEY, Name VARCHAR(255) NOT NULL, Latitude DOUBLE NOT NULL, Longitude DOUBLE NOT NULL)");
            createStopTable.executeUpdate();
            PreparedStatement createBusLineTable = connection.prepareStatement("CREATE TABLE  BusLines(ID INTEGER NOT NULL PRIMARY KEY, IsActive BOOLEAN NOT NULL, LongName VARCHAR(255) NOT NULL, ShortName VARCHAR(255) NOT NULL)");
            createBusLineTable.executeUpdate();
            PreparedStatement createRouteTable = connection.prepareStatement("CREATE TABLE Routes(ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , BusLineID INTEGER REFERENCES BusLines(ID) ON DELETE CASCADE NOT NULL, StopID INTEGER REFERENCES Stops(ID) ON DELETE CASCADE NOT NULL, 'Order' INTEGER NOT NULL)");
            createRouteTable.executeUpdate();
        } catch (SQLiteException e) {
            if (e.toString().contains("table Stops already exists")) {
                throw new IllegalStateException("Table Stops already exists");
            }
            else if (e.toString().contains("table BusLines already exists")) {
                throw new IllegalStateException("Table BusLines already exists");
            }
            else if (e.toString().contains("table Routes already exists")) {
                throw new IllegalStateException("Table Routes already exists");
            }
            else{
                throw new SQLException(e);
            }
        }
    }

    @Override
    public void clear() throws SQLException {
        if(connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }else if (connection.isClosed()){
            throw new IllegalStateException("Connection is closed.");
        }
        try {
            Statement statement = connection.createStatement();
            String deletestop = "DELETE FROM Stops";
            String deletebusline = "DELETE FROM BusLines";
            String deleteroutes = "DELETE FROM Routes";
            statement.executeUpdate(deletebusline);
            statement.executeUpdate(deleteroutes);
            statement.executeUpdate(deletestop);
            statement.close();
        }
        catch(SQLiteException e ){
            if(e.toString().contains("no such table: Routes")){
                throw new IllegalStateException("Cannot clear. Table Route is missing");
            }
            else if(e.toString().contains("no such table: BusLines")) {
                throw new IllegalStateException("Cannot clear. Table BusLines is missing");
            }
            else if (e.toString().contains("no such table: Stops")) {
                throw new IllegalStateException("Cannot clear. Table Stops is missing");
            }
            else {
                throw new SQLException(e);
            }
        }
    }

    @Override
    public void deleteTables() throws SQLException {
        if(connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }
        else if (connection.isClosed()){
            throw new IllegalStateException("Connection is closed.");
        }
        Statement statement = connection.createStatement();
        try {
            String deletestop = "DROP TABLE Stops";
            statement.executeUpdate(deletestop);
            String deleteroutes = "DROP TABLE Routes";
            statement.executeUpdate(deleteroutes);
            String deletebuslines = "DROP TABLE BusLines";
            statement.executeUpdate(deletebuslines);
        }
        catch (SQLiteException e) {
            if(e.toString().contains("no such table: Routes")){
                throw new IllegalStateException("Cannot delete. Table Route is missing");
            }
            else if(e.toString().contains("no such table: BusLines")) {
                throw new IllegalStateException("Cannot delete.Table BusLines is missing");
            }
            else if (e.toString().contains("no such table: Stops")) {
                throw new IllegalStateException("Cannot delete. Table Stops is missing");
            }
            else {
                throw new SQLException(e);
            }
        }
    }

    @Override
    public void addStops(List<Stop> stopList) throws SQLException {
        if (stopList.size() == 0) {
            throw new IllegalStateException("Stop doesn't exist");
        }

        if(connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }
        else if (connection.isClosed()){
            throw new IllegalStateException("Connection is closed.");
        }

        for (int i = 0; i < stopList.size(); i++) {
            Statement statement = connection.createStatement();
            String query = String.format("""
                            INSERT INTO Stops (ID, Name, Latitude,Longitude)
                                VALUES (%d, "%s", %f,%f);
                            """, stopList.get(i).getId(), stopList.get(i).getName()
                    , stopList.get(i).getLatitude(), stopList.get(i).getLongitude());
            try {
                statement.execute(query);
                statement.close();
            }
            catch (SQLiteException e) {
                if (e.toString().contains("no such table: Stops")) {
                    throw new IllegalStateException("Cannot add data. Table Stops is missing");
                }
                else if (e.toString().toLowerCase().contains("primary key constraint failed")) {
                    throw new IllegalArgumentException("Stop is already in the list");
                }
                else{
                    throw new SQLException(e);
                }
            }
        }
    }

    @Override
    public List<Stop> getAllStops() throws SQLException {
        if(connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }
        else if (connection.isClosed()){
            throw new IllegalStateException("Connection is closed.");
        }
        try {
            String stopquery = "SELECT * FROM Stops";
            Statement stopstatement = connection.createStatement();
            ResultSet stoprs = stopstatement.executeQuery(stopquery);
            if(stoprs.next() == false){
                throw new SQLException("The Stop table is empty. Need to populate Stop table first before adding busline data");
            }
            String query = "SELECT * FROM Stops";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("ID");
                String name = rs.getString("Name");
                Double latitude = rs.getDouble("Latitude");
                Double longitude = rs.getDouble("Longitude");
                Stop stopobject = new Stop(id, name, latitude, longitude);
                listofstops.add(stopobject);
            }
        }catch (SQLiteException e) {
            if (e.toString().contains("no such table: Stops")) {
                throw new IllegalStateException("Cannot retrieve data. Table Stops is missing");
            }
        }
            return listofstops;
    }

    @Override
    public Stop getStopByID(int id) throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }
        else if (connection.isClosed()) {
            throw new IllegalStateException("Connection is closed.");
        }
        Stop stopobject = null;
        try {
            String stopquery = "SELECT * FROM Stops";
            Statement stopstatement = connection.createStatement();
            ResultSet stoprs = stopstatement.executeQuery(stopquery);
            if(stoprs.next() == false) {
                throw new SQLException("The Stop table is empty. Need to populate Stop table first before adding busline data");
            }
            String query = "SELECT * FROM Stops";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                int tableid = rs.getInt("ID");
                if (id == tableid) {
                    String name = rs.getString("Name");
                    Double latitude = rs.getDouble("Latitude");
                    Double longitude = rs.getDouble("Longitude");
                    stopobject = new Stop(id, name, latitude, longitude);
                }

            }
        }
        catch (SQLiteException e) {
            if (e.toString().contains("no such table: Stops")) {
                throw new IllegalStateException("Cannot retrieve data. Table Stops is missing");
            }
        }
        if(stopobject ==null) {
            throw new IllegalArgumentException("This is a not valid stop");
        }
        return stopobject;
    }

    @Override
    public Stop getStopByName(String substring) throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }
        else if (connection.isClosed()) {
            throw new IllegalStateException("Connection is closed.");
        }
        Stop stopobject = null;
        try {
            String stopquery = "SELECT * FROM Stops";
            Statement stopstatement = connection.createStatement();
            ResultSet stoprs = stopstatement.executeQuery(stopquery);
            if(stoprs.next() == false) {
                throw new SQLException("The Stop table is empty. Need to populate Stop table first before adding busline data");
            }
            String query = "SELECT * FROM Stops";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                String name = rs.getString("Name");
                System.out.println("name "+ name.toLowerCase() + "substring " + substring.toLowerCase());
                if (name.toLowerCase().contains(substring.toLowerCase())) {
                    int tableid = rs.getInt("ID");
                    Double latitude = rs.getDouble("Latitude");
                    Double longitude = rs.getDouble("Longitude");
                    stopobject = new Stop(tableid, name, latitude, longitude);
                    break;
                }
            }
        }
        catch (SQLiteException e){
                if (e.toString().contains("no such table: Stops")) {
                    throw new IllegalStateException("Cannot retrieve data. Table Stops is missing");
                }
                else{
                    throw new SQLException(e);
                }
            }
        if(stopobject == null) {
            throw new IllegalArgumentException("This is a not valid stop");
        }
        return stopobject;
    }

    @Override
    public void addBusLines(List<BusLine> busLineList) throws SQLException {
        if (busLineList.size() == 0) {
            throw new IllegalStateException("BusLine doesn't exist");
        }
        if (connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }
        else if (connection.isClosed()) {
            throw new IllegalStateException("Connection is closed.");
        }
        try {
            String stopquery = "SELECT * FROM Stops";
            Statement stopstatement = connection.createStatement();
            ResultSet stoprs = stopstatement.executeQuery(stopquery);
            if(stoprs.next() == false) {
                throw new SQLException("The Stop table is empty. Need to populate Stop table first before adding busline data");
            }

        }
        catch(SQLiteException e){
            throw new IllegalStateException("Stop table is missing. Need to create and populate stop table before adding busline data");
        }
        //adding onto the route table

        for (int j = 0; j < busLineList.size(); j++) {
            Statement statement1 = connection.createStatement();
            for (int k = 0; k < busLineList.get(j).getRoute().size(); k++) {
                String routequery = String.format("""
                        INSERT INTO Routes( BusLineID, StopID, "Order")
                            VALUES ( %d, %d, %d);
                        """, busLineList.get(j).getId(), busLineList.get(j).getRoute().get(k).getId(), k);
                try {
                    statement1.execute(routequery);
                    statement1.close();
                }
                catch(SQLiteException e ) {
                    if (e.toString().toLowerCase().contains("primary key constraint failed")) {
                        throw new IllegalArgumentException("Busline data is already in the list");
                    }
                    else if (e.toString().contains("no such table: Routes")) {
                        throw new IllegalStateException("Table Route is missing. Create Route table first before adding Busline data.");
                    }
                    else{
                        throw new SQLException(e);
                    }
                }
            }
        }
        //adding onto the busline table
        for (int i = 0; i < busLineList.size(); i++) {
            Statement statement = connection.createStatement();
            String query = String.format("""
                            INSERT INTO BusLines (ID, IsActive, LongName,ShortName)
                                VALUES (%d, %b, "%s","%s");
                            """, busLineList.get(i).getId(), busLineList.get(i).isActive()
                    , busLineList.get(i).getLongName(), busLineList.get(i).getShortName());
            try {
                statement.execute(query);
                statement.close();
            }
            catch (SQLiteException e) {
                if (e.toString().toLowerCase().contains("primary key constraint failed")) {
                    throw new IllegalArgumentException("Busline data is already in the list");
                }
                else if (e.toString().contains("no such table: BusLines")) {
                    throw new IllegalStateException("Table busline is missing. Create BusLines table before adding busline data.");
                }
                else{
                    throw new SQLException(e);
                }
            }
        }

    }

    @Override
    public List<BusLine> getBusLines() throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }
        else if (connection.isClosed()) {
            throw new IllegalStateException("Connection is closed.");
        }

        try {
            String query = "SELECT * FROM BusLines";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            String checkstop = "SELECT * FROM Stops";
            Statement checkstop1 = connection.createStatement();
            ResultSet checkstop2 = checkstop1.executeQuery(checkstop);
            String checkRoutes = "SELECT * FROM Routes";
            Statement checkroutes1 = connection.createStatement();
            ResultSet checkroutes2 = checkroutes1.executeQuery(checkRoutes);
            //need to add routes
            while (rs.next()) {
                int id = rs.getInt("ID");
                boolean name = rs.getBoolean("IsActive");
                String longname = rs.getString("LongName");
                String shortname = rs.getString("ShortName");
                Route newroute = new Route();
                String routequery = "SELECT * FROM Routes";
                Statement statement1 = connection.createStatement();
                ResultSet rs1 = statement1.executeQuery(routequery);

                while (rs1.next()) {
                    int buslineId = rs1.getInt("BusLineID");
                    if (buslineId == id) {
                        int stopid = rs1.getInt("StopID");
                        String stopquery = "SELECT * FROM Stops";
                        Statement statement2 = connection.createStatement();
                        ResultSet rs2 = statement2.executeQuery(stopquery);

                        while (rs2.next()) {
                            int stoptableid = rs2.getInt("ID");
                            if (stoptableid == stopid) {
                                String stopname = rs2.getString("Name");
                                double latitude = rs2.getDouble("Latitude");
                                double longitude = rs2.getDouble("Longitude");
                                Stop stopobject = new Stop(stoptableid, stopname, latitude, longitude);
                                newroute.addStop(stopobject);
                            }
                        }
                    }
                }
                BusLine busobject = new BusLine(id, name, longname, shortname, newroute);
                listofbusline.add(busobject);
            }
        }catch (SQLiteException e) {
            if(e.toString().contains("no such table: Routes")){
                throw new IllegalStateException(" Table Route is missing");
            }
            else if(e.toString().contains("no such table: BusLines")) {
                throw new IllegalStateException("Table BusLines is missing");
            }
            else if (e.toString().contains("no such table: Stops")) {
                throw new IllegalStateException("Table Stops is missing");
            }
            else
                throw new SQLException(e.toString());
        }
        return listofbusline;
    }

    @Override
    public BusLine getBusLineById(int id) throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        }
        else if (connection.isClosed()) {
            throw new IllegalStateException("Connection is closed.");
        }
        BusLine busobject = null;
        try {
            String checkifemptyquery = "SELECT * FROM BusLines";
            Statement checkifemptystatement = connection.createStatement();
            ResultSet checkifempty = checkifemptystatement.executeQuery(checkifemptyquery);
            if(checkifempty.next() == false) {
                throw new IllegalStateException("The Busline table is empty");
            }
            String query = "SELECT * FROM BusLines";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            //need to add routes

            while (rs.next()) {
                int buslineid = rs.getInt("ID");
                if (id == buslineid) {
                    boolean name = rs.getBoolean("IsActive");
                    String longname = rs.getString("LongName");
                    String shortname = rs.getString("ShortName");
                    Route newroute = new Route();
                    String routequery = "SELECT * FROM Routes";
                    Statement statement1 = connection.createStatement();
                    ResultSet rs1 = statement1.executeQuery(routequery);
                    while (rs1.next()) {
                        int buslineId = rs1.getInt("BusLineID");
                        if (buslineId == id) {
                            int stopid = rs1.getInt("StopID");
                            String stopquery = "SELECT * FROM Stops";
                            Statement statement2 = connection.createStatement();
                            ResultSet rs2 = statement2.executeQuery(stopquery);
                            while (rs2.next()) {
                                int stoptableid = rs2.getInt("ID");
                                if (stoptableid == stopid) {
                                    String stopname = rs2.getString("Name");
                                    double latitude = rs2.getDouble("Latitude");
                                    double longitude = rs2.getDouble("Longitude");
                                    Stop stopobject = new Stop(stoptableid, stopname, latitude, longitude);
                                    newroute.addStop(stopobject);
                                }
                            }
                        }
                    }
                    busobject = new BusLine(id, name, longname, shortname, newroute);
                }

            }
        }catch(SQLiteException e) {
            if (e.toString().contains("no such table: BusLines")) {
                throw new IllegalStateException("Table BusLines is missing");
            }
        }
        if(busobject == null) {
            throw new IllegalArgumentException("No busline with that ID exists");
        }
        return busobject;
    }


    @Override
    public BusLine getBusLineByLongName(String longName) throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        } else if (connection.isClosed()) {
            throw new IllegalStateException("Connection is closed.");
        }
        BusLine busobject = null;
        try {
            String checkquery = "SELECT * FROM BusLines";
            Statement checkstatement = connection.createStatement();
            ResultSet checkrs = checkstatement.executeQuery(checkquery);
            if(checkrs.next() == false) {
                throw new IllegalStateException("The Busline table is empty");
            }
            String query = "SELECT * FROM BusLines";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()) {
                String longname = rs.getString("LongName");
                if(longname.toLowerCase().equals(longName.toLowerCase())) {
                    boolean name = rs.getBoolean("IsActive");
                    int buslineid = rs.getInt("ID");
                    String shortname = rs.getString("ShortName");
                    Route newroute = new Route();
                    String routequery = "SELECT * FROM Routes";
                    Statement statement1 = connection.createStatement();
                    ResultSet rs1 = statement1.executeQuery(routequery);
                    while (rs1.next()) {
                        int buslineId = rs1.getInt("BusLineID");
                        if (buslineId == buslineid) {
                            int stopid = rs1.getInt("StopID");
                            String stopquery = "SELECT * FROM Stops";
                            Statement statement2 = connection.createStatement();
                            ResultSet rs2 = statement2.executeQuery(stopquery);
                            while (rs2.next()) {
                                int stoptableid = rs2.getInt("ID");
                                if (stoptableid == stopid) {
                                    String stopname = rs2.getString("Name");
                                    double latitude = rs2.getDouble("Latitude");
                                    double longitude = rs2.getDouble("Longitude");
                                    Stop stopobject = new Stop(stoptableid, stopname, latitude, longitude);
                                    newroute.addStop(stopobject);

                                }
                            }
                        }
                    }
                    busobject = new BusLine(buslineid, name, longname, shortname, newroute);
                }
            }
        }
        catch(SQLiteException e) {
            if (e.toString().contains("no such table: BusLines")) {
                throw new IllegalStateException("Table BusLines is missing");
            }
        }
        if(busobject == null){
            throw new IllegalArgumentException("No busline with that name exists");
        }
        return busobject;
    }

    @Override
    public BusLine getBusLineByShortName(String shortName) throws SQLException {
        if (connection == null) {
            throw new IllegalStateException("DATABASE IS NOT CONNECTED");
        } else if (connection.isClosed()) {
            throw new IllegalStateException("Connection is closed.");
        }
        BusLine busobject = null;
        try {
            String checkquery = "SELECT * FROM BusLines";
            Statement checkstatement = connection.createStatement();
            ResultSet checkrs = checkstatement.executeQuery(checkquery);
            if(checkrs.next() == false){
                throw new IllegalStateException("The Busline table is empty");
            }
            String query = "SELECT * FROM BusLines";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            //need to add routes

            while (rs.next()) {
                String shortname = rs.getString("ShortName");
                if (shortname.toLowerCase().equals(shortName.toLowerCase())) {
                    boolean name = rs.getBoolean("IsActive");
                    int buslineid = rs.getInt("ID");
                    String longname = rs.getString("LongName");
                    Route newroute = new Route();
                    String routequery = "SELECT * FROM Routes";
                    Statement statement1 = connection.createStatement();
                    ResultSet rs1 = statement1.executeQuery(routequery);
                    while (rs1.next()) {
                        int buslineId = rs1.getInt("BusLineID");
                        if (buslineId == buslineid) {
                            int stopid = rs1.getInt("StopID");
                            String stopquery = "SELECT * FROM Stops";
                            Statement statement2 = connection.createStatement();
                            ResultSet rs2 = statement2.executeQuery(stopquery);
                            while (rs2.next()) {
                                int stoptableid = rs2.getInt("ID");
                                if (stoptableid == stopid) {
                                    String stopname = rs2.getString("Name");
                                    double latitude = rs2.getDouble("Latitude");
                                    double longitude = rs2.getDouble("Longitude");
                                    Stop stopobject = new Stop(stoptableid, stopname, latitude, longitude);
                                    newroute.addStop(stopobject);
                                }
                            }
                        }
                    }
                    busobject = new BusLine(buslineid, name, longname, shortname, newroute);
                }

            }
        }catch(SQLiteException e) {
            if (e.toString().contains("no such table: BusLines")) {
                throw new IllegalStateException("Table BusLines is missing");
            }
        }if(busobject == null){
            throw new IllegalArgumentException("No busline with that name exists");
        }
        return busobject;
    }

    @Override
    public void disconnect() {
        try {
            if(connection == null){
                throw new IllegalStateException("Nothing to disconnect. connection was not connected from the start");
            }
            connection.close();
        } catch (SQLException e) {
            if(e.toString().contains("\"this.connection\" is null"))
                throw new IllegalStateException("connection was not connected from the start");
        }catch(NullPointerException e ){
            if(e.toString().contains("\"this.connection\" is null"))
                throw new IllegalStateException("connection was not connected from the start");
        }
    }
}