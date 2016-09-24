/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.demos;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import java.io.*;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;
//import java.util.Arrays;

// Extend HttpServlet class
public class Servlet1 extends HttpServlet {
 
  private GraphHopper graphHopper;

  @Override
  public void init() throws ServletException
  {
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
            throws ServletException, IOException
  {
      // Set response content type
      //response.setContentType("text/html");

      // Actual logic goes here.
//      PrintWriter out = response.getWriter();
//      out.println( "test");
     
      String[][] info = new String[2][];

        for (int i = 0; i < 2; i++) 
        {
            info[i] = request.getParameterValues("data["+i+"][]");

        }
        
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 2; j++) 
            {
                System.out.println(info[i][j]);
            }
            System.out.print("\n");
        }
        
        
        
        double[] orig = new double[]{Double.parseDouble(info[0][0]), Double.parseDouble(info[0][1])};
        double[] dest = new double[]{Double.parseDouble(info[1][0]), Double.parseDouble(info[1][1])};
        GHRequest gRequest = null;
        GHResponse route = null;
        gRequest = new GHRequest(orig[0], orig[1], dest[0], dest[1]);
        gRequest.setWeighting("fastest");
        gRequest.setVehicle("car"); // "car"
        route = graphHopper.route(gRequest);
        //"C:\\Users\\panikas\\Desktop\\New folder\\file.gpx"
        try (PrintWriter out1 = new PrintWriter("C:\\Users\\panikas\\Desktop\\diploma\\code\\Demos\\target\\Demos-1.0-SNAPSHOT\\file.gpx")) {
            out1.print(route.getBest().getInstructions().createGPX("Graphhopper",new Date().getTime(),false,false,true,false));
        } catch (FileNotFoundException ex) {
            System.out.println("exception filenotfound");
        }
        
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