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
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.routing.util.EncodingManager;
import java.io.*;
import java.util.Collection;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import java.util.Arrays;

// Extend HttpServlet class
public class Servlet2 extends HttpServlet {

    private GraphHopper graphHopper;

    @Override
    public void init() throws ServletException {
        // Do required initialization
        System.out.println("init done");
        graphHopper = new GraphHopper().setGraphHopperLocation("C:\\Users\\panikas\\Desktop\\diploma") // "gh-car"
                .setEncodingManager(new EncodingManager("car")) // "car"
                .setOSMFile("europe_germany_berlin.osm") // "germany-lastest.osm.pbf"
                .forServer();
        graphHopper.importOrLoad();

    }

    @Override
    public void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        // Set response content type
        //response.setContentType("text/html");

        // Actual logic goes here.
//      PrintWriter out = response.getWriter();
//      out.println( "test");
        String[][] info = new String[5][];

        for (int i = 0; i < 5; i++) {
            info[i] = request.getParameterValues("data[" + i + "][]");

        }
        
        double[][] coordinates = new double[5][2];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println(info[i][j]);
                coordinates[i][j] = Double.parseDouble(info[i][j]);
                
            }
            System.out.print("\n");
        }

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 2).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
                .setStartLocation(Location.newInstance("0")).setType(type).setReturnToDepot(true).build();

        Service[] services = new Service[5];
        for (int i = 0; i < 4; i++) {
            services[i] = Service.Builder.newInstance(Integer.toString(i+1)).addSizeDimension(0, 0).setLocation(Location.newInstance(Integer.toString(i+1))).build();
        }

        GHRequest Grequest = null;
        GHResponse route = null;
        double[][] DistanceMatrix = new double[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Grequest = new GHRequest(Double.parseDouble(info[i][0]), Double.parseDouble(info[i][1]), Double.parseDouble(info[j][0]), Double.parseDouble(info[j][1]));
                Grequest.setWeighting("fastest");
                Grequest.setVehicle("car");
                route = graphHopper.route(Grequest);
                try (PrintWriter out = new PrintWriter("C:\\Users\\panikas\\Desktop\\diploma\\code\\Demos\\target\\Demos-1.0-SNAPSHOT\\file"+i+j+".gpx")) {
                    out.print(route.getBest().getInstructions().createGPX("Graphhopper", new Date().getTime(), false, false, true, false));
                } catch (FileNotFoundException ex) {
                    System.out.println("exception filenotfound");
                }
                DistanceMatrix[i][j] = route.getBest().getDistance();

            }
        }

        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                //costMatrixBuilder.addTransportTime(Integer.toString(i), Integer.toString(j), dMatrixtime[i][j]);
                costMatrixBuilder.addTransportDistance(Integer.toString(i), Integer.toString(j), DistanceMatrix[i][j]);
            }
        }

        VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).setRoutingCost(costMatrix).addVehicle(vehicle);
        for (int i = 0; i < 4; i++) {
            vrpBuilder.addJob(services[i]);
        }
        
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        int i;
        int j;
        int counter = 0;
        JSONObject point;
        JSONArray  tspRoute = new JSONArray();
        for (VehicleRoute jroute : Solutions.bestOf(solutions).getRoutes())
        {
            TourActivity prevAct = jroute.getStart();
//            System.out.println(prevAct.getLocation().getId());
            for (TourActivity act : jroute.getActivities())
            {
                i = Integer.parseInt(prevAct.getLocation().getId());
                j = Integer.parseInt(act.getLocation().getId());
                point = new JSONObject();
                point.put("id",i);
                //System.out.println(prevAct.getLocation());
//                System.out.println(Integer.parseInt(act.getLocation().getId()));
                
                
                Grequest = new GHRequest(coordinates[i][0], coordinates[i][1], coordinates[j][0], coordinates[j][1]);
                Grequest.setWeighting("fastest");
                Grequest.setVehicle("car");
                route = graphHopper.route(Grequest);
                System.out.println(counter);
                try (PrintWriter out = new PrintWriter("C:\\Users\\panikas\\Desktop\\diploma\\code\\Demos\\target\\Demos-1.0-SNAPSHOT\\file"+counter+".gpx")) {
                    out.print(route.getBest().getInstructions().createGPX("Graphhopper", new Date().getTime(), false, false, true, false));
                } catch (FileNotFoundException ex) {
                    System.out.println("exception filenotfound");
                }
                prevAct = act;
                counter++;
                tspRoute.add(point);
            }
//             System.out.println(jroute.getEnd().getLocation().getId());
               i = Integer.parseInt(prevAct.getLocation().getId());
               j = Integer.parseInt(jroute.getEnd().getLocation().getId());
               
               point = new JSONObject();
               point.put("id",i);
               tspRoute.add(point);
               
               Grequest = new GHRequest(coordinates[i][0], coordinates[i][1], coordinates[j][0], coordinates[j][1]);
                Grequest.setWeighting("fastest");
                Grequest.setVehicle("car");
                route = graphHopper.route(Grequest);
                System.out.println(counter);
                try (PrintWriter out = new PrintWriter("C:\\Users\\panikas\\Desktop\\diploma\\code\\Demos\\target\\Demos-1.0-SNAPSHOT\\file"+counter+".gpx")) {
                    out.print(route.getBest().getInstructions().createGPX("Graphhopper", new Date().getTime(), false, false, true, false));
                } catch (FileNotFoundException ex) {
                    System.out.println("exception filenotfound");
                }
                counter++;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(tspRoute.toString());
        
        System.out.println("done");

    }

    /**
     *
     */
    @Override
    public void destroy() {
        // do nothing.
    }
}
