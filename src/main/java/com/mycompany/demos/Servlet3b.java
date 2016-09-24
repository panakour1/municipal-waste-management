/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.demos;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.routing.util.EncodingManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.sql.*;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import java.util.Collection;
import java.util.ArrayList;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

/**
 *
 * @author panikas
 */
public class Servlet3b extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        final String DB_URL = "jdbc:mysql://localhost:3306/garbagecollectionv2";
        final String USER = "root";
        final String PASS = "1234";

        double existingLocationLat = 0, existingLocationLng = 0;
        int existingLocationId, newLocationId;
        //float fullness = 0;


        //System.out.println(request.getParameter("action"));
        Connection conn = null;
        Statement stmt = null;
        Statement stmt2 = null;


        

        try {
            //STEP 2: Register JDBC driver
            System.out.println("Loading Driver...");
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            String sql;

            if (request.getParameter("action").equals("add")) {

                sql = "insert into locations (lat,lng) values (" + request.getParameter("lat") + "," + request.getParameter("lng") + ");";
                //System.out.println(sql);
                stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs2 = stmt.getGeneratedKeys();
                rs2.next();
                newLocationId = rs2.getInt(1);
                rs2.close();
                //System.out.println(newBinId);

                GraphHopper graphHopper = new GraphHopper().setGraphHopperLocation("C:\\Users\\panikas\\Desktop\\diploma") // "gh-car"
                        .setEncodingManager(new EncodingManager("car")) // "car"
                        .setOSMFile("europe_germany_berlin.osm") // "germany-lastest.osm.pbf"
                        .forServer();
                graphHopper.importOrLoad();

                GHRequest Grequest = null;
                GHResponse route = null;

                sql = "SELECT * FROM locations WHERE locationId != " + newLocationId + ";";
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    existingLocationLat = rs.getDouble("lat");
                    existingLocationLng = rs.getDouble("lng");
                    existingLocationId = rs.getInt("locationId");
                    Grequest = new GHRequest(existingLocationLat, existingLocationLng, Double.parseDouble(request.getParameter("lat")), Double.parseDouble(request.getParameter("lng")));
                    Grequest.setWeighting("fastest");
                    Grequest.setVehicle("car");
                    route = graphHopper.route(Grequest);
                    try (PrintWriter out = new PrintWriter("C:\\Users\\panikas\\Desktop\\diploma\\code\\Demos\\target\\Demos-1.0-SNAPSHOT\\locations\\route" + existingLocationId + "_" + newLocationId + ".gpx")) {
                        out.print(route.getBest().getInstructions().createGPX("Graphhopper", new Date().getTime(), false, false, true, false));
                    } catch (FileNotFoundException ex) {
                        System.out.println("exception filenotfound");
                    }
                    sql = "insert into distances (originId,destinationId,distance) values (" + existingLocationId + "," + newLocationId + "," + route.getBest().getTime() + ");";
                    stmt2.executeUpdate(sql);

                    Grequest = new GHRequest(Double.parseDouble(request.getParameter("lat")), Double.parseDouble(request.getParameter("lng")), existingLocationLat, existingLocationLng);
                    Grequest.setWeighting("fastest");
                    Grequest.setVehicle("car");
                    route = graphHopper.route(Grequest);
                    try (PrintWriter out = new PrintWriter("C:\\Users\\panikas\\Desktop\\diploma\\code\\Demos\\target\\Demos-1.0-SNAPSHOT\\locations\\route" + newLocationId + "_" + existingLocationId + ".gpx")) {
                        out.print(route.getBest().getInstructions().createGPX("Graphhopper", new Date().getTime(), false, false, true, false));
                    } catch (FileNotFoundException ex) {
                        System.out.println("exception filenotfound");
                    }
                    sql = "insert into distances (originId,destinationId,distance) values (" + newLocationId + "," + existingLocationId + "," + route.getBest().getTime() + ");";
                    stmt2.executeUpdate(sql);

                }

                if (request.getParameter("type").equals("bin")) {
                    sql = "insert into bins (locationId,fullness) values (" + newLocationId + "," + request.getParameter("fullness") + ");";
                    stmt.executeUpdate(sql);
                } else if (request.getParameter("type").equals("depot")) {
                    sql = "insert into depots (locationId,numOfVehicles) values (" + newLocationId + "," + request.getParameter("numOfVehicles") + ");";
                    stmt.executeUpdate(sql);
                }

                rs.close();

            } else if (request.getParameter("action").equals("delete")) {
                //System.out.println(request.getParameter("id"));
                sql = "delete from locations where locationId=" + request.getParameter("locationId") + ";";
                stmt.executeUpdate(sql);

            } else if (request.getParameter("action").equals("update")) {
                //System.out.println(request.getParameter("id"));
                //System.out.println(request.getParameter("fullness"));
                sql = "update bins set fullness=" + request.getParameter("fullness") + " where binId=" + request.getParameter("binId") + ";";
                stmt.executeUpdate(sql);
            } else if (request.getParameter("action").equals("optimise")) {

                String[] colours = {"#3333ff", "#ff33cc", "#ff6600", "#cc6600", "#cccc00"};
                Collection<Location> locations = new ArrayList<Location>();

                //new problem builder
                VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
                vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

                //add vehicles
                sql = "SELECT * FROM depots;";
                ResultSet rs = stmt.executeQuery(sql);
                VehicleType type = VehicleTypeImpl.Builder.newInstance("garbageCollector").addCapacityDimension(0, 1000).build();
                VehicleImpl vehicle;
                Location currentLocation;
                int locationId, numOfVehicles, depotId;

                while (rs.next()) {

                    locationId = rs.getInt("locationId");
                    numOfVehicles = rs.getInt("numOfVehicles");
                    depotId = rs.getInt("depotId");
                    currentLocation = Location.newInstance(Integer.toString(locationId));
                    for (int i = 0; i < numOfVehicles; i++) {
                        vehicle = VehicleImpl.Builder.newInstance(depotId + "_" + i)
                                .setStartLocation(currentLocation).setType(type).setReturnToDepot(true).build();
                        vrpBuilder.addVehicle(vehicle);
                    }
                    locations.add(currentLocation);

                }

                sql = "SELECT * FROM bins where fullness > 50;";
                rs = stmt.executeQuery(sql);
                Service currentService;
                float fullness;
                while (rs.next()) {

                    locationId = rs.getInt("locationId");
                    fullness = rs.getFloat("fullness");
                    currentLocation = Location.newInstance(Integer.toString(locationId));
                    currentService = Service.Builder.newInstance("service" + Integer.toString(locationId)).addSizeDimension(0, (int) fullness)
                            .setLocation(currentLocation).build();
                    vrpBuilder.addJob(currentService);
                    locations.add(currentLocation);
                }

                //distance matrix
                VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);

                float distance;
                for (Location origin : locations) {
                    //System.out.println(location.getId());
                    costMatrixBuilder.addTransportDistance(origin.getId(), origin.getId(), 0);
                    for (Location destination : locations) {
                        //System.out.println(origin.getId());
                        // System.out.println(destination.getId());

                        if (origin.getId() != destination.getId()) {
                            sql = "SELECT distance FROM distances where originId = " + origin.getId() + " and destinationId = " + destination.getId() + ";";
                            rs = stmt.executeQuery(sql);
                            rs.next();
                            distance = rs.getFloat("distance");
                            costMatrixBuilder.addTransportDistance(origin.getId(), destination.getId(), distance);
                        }
                    }
                }

                VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();
                vrpBuilder.setRoutingCost(costMatrix);
                VehicleRoutingProblem vrp = vrpBuilder.build();
                VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
                Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
                SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

                //System.out.println("optimise");
                JSONArray files,routes;
                JSONObject route,bins,bin,solution;
                routes = new JSONArray();
                bins = new JSONObject();
                solution = new JSONObject();

                int routeCounter = 0;
                int position;
                for (VehicleRoute jroute : Solutions.bestOf(solutions).getRoutes()) {
                    TourActivity prevAct = jroute.getStart();
                    route = new JSONObject();
                    files = new JSONArray();
                    position = 1;

                    for (TourActivity act : jroute.getActivities()) {

                        files.add("locations\\route" + prevAct.getLocation().getId() + "_" + act.getLocation().getId() + ".gpx");
                        bin = new JSONObject();
                        bin.put("colour",colours[routeCounter]);
                        bin.put("position",position);
                        bins.put(act.getLocation().getId(),bin);
                        position++;
                        prevAct = act;
                    }
                    files.add("locations\\route" + prevAct.getLocation().getId() + "_" + jroute.getEnd().getLocation().getId() + ".gpx");
                    route.put("files", files);
                    route.put("colour", colours[routeCounter]);
                    routes.add(route);
                    routeCounter++;
                }
                
                solution.put("routes",routes);
                solution.put("bins",bins);
                System.out.println(solution.toString());
                response.setContentType(
                        "application/json");
                response.setCharacterEncoding(
                        "UTF-8");
                response.getWriter()
                        .write(solution.toString());

            }

            stmt.close();
            stmt2.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }
            try {
                if (stmt2 != null) {
                    stmt2.close();
                }
            } catch (SQLException se3) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

//        JSONObject name = new JSONObject();
//        JSONArray names = new JSONArray();
//
//        name.put(
//                "name", "foo1");
//        name.put(
//                "surname", "bar1");
//
//        names.add(name);
//        name = new JSONObject();
//
//        name.put(
//                "name", "foo2");
//        name.put(
//                "surname", "bar2");
////        names.add(name);
//        response.setContentType(
//                "application/json");
//        response.setCharacterEncoding(
//                "UTF-8");
//        response.getWriter()
//                .write(name.toString());
//        System.out.println("done");
        //System.out.println(names.toString());
        //System.out.println(request.getParameter("action"));
        //System.out.println("blah");
    }

    @Override
    public void destroy() {
        // do nothing.
    }
}
