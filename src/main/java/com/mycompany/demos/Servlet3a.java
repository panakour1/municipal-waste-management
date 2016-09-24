/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.demos;

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

/**
 *
 * @author panikas
 */
public class Servlet3a extends HttpServlet {

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

        double lat = 0, lng = 0;
        float fullness = 0;
        int locationId, binId, depotId, numOfVehicles;

        JSONObject bins, feature, properties, geometry;
        JSONArray features, coordinates;
        features = new JSONArray();

        Connection conn = null;
        Statement stmt = null;
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
            String sql;
            sql = "SELECT * FROM bins inner join locations on bins.locationId = locations.locationId;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                lat = rs.getDouble("lat");
                lng = rs.getDouble("lng");
                fullness = rs.getFloat("fullness");
                locationId = rs.getInt("locationId");
                binId = rs.getInt("binId");
                //System.out.println(fullness);
                coordinates = new JSONArray();
                coordinates.add(lng);
                coordinates.add(lat);

                geometry = new JSONObject();
                geometry.put("type", "Point");
                geometry.put("coordinates", coordinates);

                properties = new JSONObject();
                properties.put("locationId", locationId);
                properties.put("binId", binId);
                properties.put("fullness", fullness);
                properties.put("type", "bin");

                feature = new JSONObject();
                feature.put("type", "Feature");
                feature.put("geometry", geometry);
                feature.put("properties", properties);

                features.add(feature);

            }

            sql = "SELECT * FROM depots inner join locations on depots.locationId = locations.locationId;";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {

                lat = rs.getDouble("lat");
                lng = rs.getDouble("lng");
                numOfVehicles = rs.getInt("numOfVehicles");
                locationId = rs.getInt("locationId");
                depotId = rs.getInt("depotId");
                //System.out.println(fullness);
                coordinates = new JSONArray();
                coordinates.add(lng);
                coordinates.add(lat);

                geometry = new JSONObject();
                geometry.put("type", "Point");
                geometry.put("coordinates", coordinates);

                properties = new JSONObject();
                properties.put("locationId", locationId);
                properties.put("numOfVehicles", numOfVehicles);
                properties.put("depotId", depotId);
                properties.put("type", "depot");

                feature = new JSONObject();
                feature.put("type", "Feature");
                feature.put("geometry", geometry);
                feature.put("properties", properties);

                features.add(feature);

            }

            rs.close();
            stmt.close();
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
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        bins = new JSONObject();
        bins.put("type", "FeatureCollection");
        bins.put("features", features);

        response.setContentType(
                "application/json");
        response.setCharacterEncoding(
                "UTF-8");
        response.getWriter()
                .write(bins.toString());
        System.out.println(bins.toString());

    }

    @Override
    public void destroy() {
        // do nothing.
    }
}
