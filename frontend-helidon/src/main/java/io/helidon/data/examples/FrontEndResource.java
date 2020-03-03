/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.data.examples;

import org.eclipse.microprofile.opentracing.Traced;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Path("/")
@ApplicationScoped
@Traced
public class FrontEndResource {

    String inventoryLocation = "1469 WEBSTER ST,San Francisco,CA";
    String routingInfo = "{\"type\":\"LineString\",\"coordinates\":[[-74.00501,40.70583],[-74.00457,40.70549],[-74.00447,40.70541],[-74.00418,40.70559],[-74.00386,40.70579],[-74.00361,40.70595],[-74.00346,40.70605],[-74.00335,40.70611],[-74.00318,40.70621],[-74.00231,40.7067],[-74.00274,40.70722],[-74.00311,40.70767],[-74.00336,40.708],[-74.00345,40.70808],[-74.00407,40.70745],[-74.00412,40.70757],[-74.00433,40.70783],[-74.00477,40.70841],[-74.00505,40.70876],[-74.00513,40.70885],[-74.00524,40.70893],[-74.00532,40.70899],[-74.00547,40.70909],[-74.00643,40.70956],[-74.00705,40.70987],[-74.00774,40.71022],[-74.00906,40.71089],[-74.01046,40.71153],[-74.01013,40.71209],[-74.00967,40.71274],[-74.00927,40.71326],[-74.00902,40.71359],[-74.00885,40.71381],[-74.0084,40.71437],[-74.00795,40.71494],[-74.00755,40.71544],[-74.00882,40.71602],[-74.0092,40.71619],[-74.00911,40.71692],[-74.00906,40.71726],[-74.009,40.7176],[-74.00894,40.71793],[-74.00888,40.71827],[-74.00882,40.71864],[-74.00875,40.71903],[-74.0087,40.7193],[-74.00858,40.71996],[-74.00847,40.72065],[-74.00842,40.72089],[-74.00837,40.7212],[-74.00834,40.72133],[-74.00823,40.72198],[-74.00812,40.72264],[-74.00801,40.72328],[-74.00795,40.72365],[-74.00793,40.72376],[-74.00786,40.72382],[-74.00777,40.72388],[-74.00773,40.72392],[-74.00771,40.72393],[-74.00745,40.72412],[-74.00736,40.72417],[-74.00728,40.72424],[-74.00723,40.72429],[-74.0071,40.72441],[-74.00703,40.7245]]}";

    @Path("/")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String home() {
        return getFullPage("");
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/placeorder")
    public String placeorder(@QueryParam("orderid") String orderid,
                             @QueryParam("itemid") String itemid) {
        System.out.println("-----> FrontEnd placeorder orderid:" + orderid + " itemid:" + itemid);
        try {
            URL url = new URL("http://order.msdataworkshop:8080/placeOrder?orderid=" + orderid +
                    "&itemid=" + itemid);
            String json = makeRequest(url);
            System.out.println("FrontEndResource.placeorder json:" + json);
            return getFullPage(json);
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/showorder")
    public String showorder(@QueryParam("orderid") String orderid) {
        System.out.println("-----> FrontEnd showorder orderid:" + orderid);
        try {
            URL url = new URL("http://order.msdataworkshop:8080/showorder");
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/orderservicecall")
    public String orderservicecall( @QueryParam("test") String test) {
        System.out.println("-----> FrontEnd orderservicecall test:" + test);
        try {
            URL url = new URL("http://order.msdataworkshop:8080/" +test);
            String requestString = makeRequest(url);
            System.out.println("-----> FrontEnd orderservicecall requestString:" + requestString);
            return getFullPage(requestString);
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }


    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/streamingservicetest")
    public String sendTestStreamOrders(@QueryParam("numberofitemstostream") int numberofitemstostream) {
        try {
            URL url = new URL("http://order.msdataworkshop:8080/sendTestStreamOrders?numberofitemstostream=" + numberofitemstostream);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/executeonorderpdb")
    public String orderadmin(@QueryParam("sql") String sql, @QueryParam("orderuser") String user, @QueryParam("orderpassword") String password) {
        try {
            System.out.println("-----> FrontEnd orderadmin sql = [" + sql + "], user = [" + user + "], password = [" + password + "]");
            sql = URLEncoder.encode(sql, "UTF-8");
            String urlString = "http://orderadmin.msdataworkshop:8080/execute?sql=" + sql + "&user=" + user + "&password=" + password;
            System.out.println("FrontEndResource.orderadmin urlString:" + urlString);
            URL url = new URL( urlString);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/executeoninventorypdb")
    public String helidonatpinventory(@QueryParam("sql") String sql, @QueryParam("inventoryuser") String user, @QueryParam("inventorypassword") String password) {
        try {
            System.out.println("-----> FrontEnd helidonatpinventory: [" + sql + "], user = [" + user + "], password = [" + password + "]");
            sql = URLEncoder.encode(sql, "UTF-8");
            String urlString = "http://inventoryadmin.msdataworkshop:8080/execute?sql=" + sql + "&user=" + user + "&password=" + password;
            System.out.println("FrontEndResource.inventoryadmin urlString:" + urlString);
            URL url = new URL( urlString);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/inventoryservicetest")
    public String inventoryservicetest(@QueryParam("test") String test) {
        try {
            if (test.equals("setupTablesQueuesAndPropagation")) {
                return "setupTablesQueuesAndPropagation complete"; //todo make appropriate calls on inventory.msdataworkshop and order.msdataworkshop
            } else   if (test.equals("createDBUsers")) {
                return "createDBUsers complete"; //todo make appropriate calls on inventory.msdataworkshop and order.msdataworkshop
            }
            URL url = new URL("http://inventory.msdataworkshop:8080/" + test);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/supplierservicecall")
    public String supplierservicecall(@QueryParam("test") String test, @QueryParam("itemid") String itemid) {
        try {
            URL url = new URL("http://supplier.msdataworkshop:8080/supplier/" + test + "?itemid=" + itemid);
            return getFullPage(makeRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/deliveryservicetest")
    public String deliveryservicetest(@QueryParam("orderid") String orderid) {
        System.out.println("-----> FrontEnd deliveryservicetest orderid = [" + orderid + "]");
        //todo lookup orderid
        try {
            URL url = new URL("https://150.136.177.253:7002/chrest/Visualize.jsp");
            return getFullPage(makePostRequest(url));
        } catch (IOException e) {
            e.printStackTrace();
            return home();
        }
    }

    private String makePostRequest(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = routingInfo.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
            return response.toString();
        }
    }

    // todo convert to rest call/annotation
    private String makeRequest(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        InputStream response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response)) {
            return scanner.hasNext() ? scanner.useDelimiter("\\A").next() : "" + response;
        } catch (java.util.NoSuchElementException ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }


    private String getFullPage(String output) {
        return getPageHeader() +
                "<table width=\"100%\" align=\"left\">" +
                "  <tr>" +
                "    <th width=\"50%\" align=\"left\" valign=\"top\">" +
               "<h4>JSON data, AQ transactional event-driven communication, and choreography saga (order and inventory service)</h4>" +
                "<form action=\"placeorder\">" +
                "itemid : <input type=\"text\" name=\"itemid\" size=\"5\" value=\"11\">  " +
                "deliver to : <input type=\"text\" name=\"deliverylocation\" size=\"5\" value=\"780 PANORAMA DR,San Francisco,CA\">  " +
                "orderid : <input type=\"text\" name=\"orderid\"  size=\"10\" value=\"66\"> " +
                "<input type=\"submit\" value=\"place order\"></p>" +
                "</form>" +
                "<h4>Relational data (supplier service)</h4>" +
                "<form action=\"supplierservicecall\">" +
                "itemid : <input type=\"text\" name=\"itemid\" size=\"5\" value=\"11\">  " +
                "<input type=\"submit\" name =\"test\" value=\"addInventory\">" +
                "<input type=\"submit\" name =\"test\" value=\"removeInventory\">" +
                "<input type=\"submit\" name =\"test\" value=\"getinventory\">" +
                "</form>" +
               "<h4>Event sourcing and CQRS (order service)</h4>" +
                "<form action=\"showorder\">" +
                "orderid : <input type=\"text\" name=\"orderid\"  size=\10\" value=\"66\"> " +
                "    <input type=\"submit\" value=\"show order\">" +
                "</form>" +
                "<h4>Spatial data (map service)</h4>" +
//                "<form action=\"deliveryservicetest\">" +
//                "<input type=\"text\" name =\"orderid\" value=\"66\">" +
//                "<input type=\"submit\" name =\"test\" value=\"deliveryDetail\">" +
//                "</form>" +
//                "<form action=\"https://150.136.177.253:7002/chrest/Visualize.jsp\" method=\"POST\" id=\"renderForm\">" +
//                "<textarea cols=\"100\" id = \"geojson\" name=\"geojson\" rows=\"19\">\n" +
//                "{&quot;type&quot;:&quot;LineString&quot;,&quot;coordinates&quot;:[[-74.00501,40.70583],[-74.00457,40.70549],[-74.00447,40.70541],[-74.00418,40.70559],[-74.00386,40.70579],[-74.00361,40.70595],[-74.00346,40.70605],[-74.00335,40.70611],[-74.00318,40.70621],[-74.00231,40.7067],[-74.00274,40.70722],[-74.00311,40.70767],[-74.00336,40.708],[-74.00345,40.70808],[-74.00407,40.70745],[-74.00412,40.70757],[-74.00433,40.70783],[-74.00477,40.70841],[-74.00505,40.70876],[-74.00513,40.70885],[-74.00524,40.70893],[-74.00532,40.70899],[-74.00547,40.70909],[-74.00643,40.70956],[-74.00705,40.70987],[-74.00774,40.71022],[-74.00906,40.71089],[-74.01046,40.71153],[-74.01013,40.71209],[-74.00967,40.71274],[-74.00927,40.71326],[-74.00902,40.71359],[-74.00885,40.71381],[-74.0084,40.71437],[-74.00795,40.71494],[-74.00755,40.71544],[-74.00882,40.71602],[-74.0092,40.71619],[-74.00911,40.71692],[-74.00906,40.71726],[-74.009,40.7176],[-74.00894,40.71793],[-74.00888,40.71827],[-74.00882,40.71864],[-74.00875,40.71903],[-74.0087,40.7193],[-74.00858,40.71996],[-74.00847,40.72065],[-74.00842,40.72089],[-74.00837,40.7212],[-74.00834,40.72133],[-74.00823,40.72198],[-74.00812,40.72264],[-74.00801,40.72328],[-74.00795,40.72365],[-74.00793,40.72376],[-74.00786,40.72382],[-74.00777,40.72388],[-74.00773,40.72392],[-74.00771,40.72393],[-74.00745,40.72412],[-74.00736,40.72417],[-74.00728,40.72424],[-74.00723,40.72429],[-74.0071,40.72441],[-74.00703,40.7245]]}\n" +
//                "</textarea>" +
                "<input type=\"hidden\" id = \"geojson\" name=\"geojson\" value=\"" +
                "{&quot;type&quot;:&quot;LineString&quot;,&quot;coordinates&quot;:[[-74.00501,40.70583],[-74.00457,40.70549],[-74.00447,40.70541],[-74.00418,40.70559],[-74.00386,40.70579],[-74.00361,40.70595],[-74.00346,40.70605],[-74.00335,40.70611],[-74.00318,40.70621],[-74.00231,40.7067],[-74.00274,40.70722],[-74.00311,40.70767],[-74.00336,40.708],[-74.00345,40.70808],[-74.00407,40.70745],[-74.00412,40.70757],[-74.00433,40.70783],[-74.00477,40.70841],[-74.00505,40.70876],[-74.00513,40.70885],[-74.00524,40.70893],[-74.00532,40.70899],[-74.00547,40.70909],[-74.00643,40.70956],[-74.00705,40.70987],[-74.00774,40.71022],[-74.00906,40.71089],[-74.01046,40.71153],[-74.01013,40.71209],[-74.00967,40.71274],[-74.00927,40.71326],[-74.00902,40.71359],[-74.00885,40.71381],[-74.0084,40.71437],[-74.00795,40.71494],[-74.00755,40.71544],[-74.00882,40.71602],[-74.0092,40.71619],[-74.00911,40.71692],[-74.00906,40.71726],[-74.009,40.7176],[-74.00894,40.71793],[-74.00888,40.71827],[-74.00882,40.71864],[-74.00875,40.71903],[-74.0087,40.7193],[-74.00858,40.71996],[-74.00847,40.72065],[-74.00842,40.72089],[-74.00837,40.7212],[-74.00834,40.72133],[-74.00823,40.72198],[-74.00812,40.72264],[-74.00801,40.72328],[-74.00795,40.72365],[-74.00793,40.72376],[-74.00786,40.72382],[-74.00777,40.72388],[-74.00773,40.72392],[-74.00771,40.72393],[-74.00745,40.72412],[-74.00736,40.72417],[-74.00728,40.72424],[-74.00723,40.72429],[-74.0071,40.72441],[-74.00703,40.7245]]}" +
                "\"/>"  +
                "<input type=\"submit\" value=\"Submit\"></form>" +
                "<h4>OCI Streaming Service via Kafka API (orderstreaming service and order service)</h4>" +
                "<form action=\"orderservicecall\">" +
                "# of orders to stream : <input type=\"text\" name=\"numberoforderstostream\"  size=\5\" value=\"5\"> " +
                "<input type=\"submit\" name =\"test\" value=\"produceStreamOrders\">" +
                "<input type=\"submit\" name =\"test\" value=\"consumeStreamOrders\"></form>" +
                "<h4>Helidon Health Checks and OKE Health Probes (order service)</h4>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"health\">" +
                "   <input type=\"submit\" name =\"test\" value=\"health/live\">" +
                "   <input type=\"submit\" name =\"test\" value=\"health/ready\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"ordersetlivenesstofalse\">" +
                "   <input type=\"submit\" name =\"test\" value=\"ordersetdelayforreadiness\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"lastContainerStartTime\"></form>" +
                "<h4>Helidon Metrics and OKE horizontal-autoscaling (order service)</h4>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"metrics\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"startCPUStress\">" +
                "   <input type=\"submit\" name =\"test\" value=\"stopCPUStress\"></form>" +
                "<br> </th>" +
                "    <th width=\"50%\" align=\"left\" valign=\"top\">" +
                "<h4>Setup...</h4>" +
                "<form action=\"adminservicetest\"><input type=\"submit\" name =\"test\" value=\"createDBUsers\"></form>" +
                "<form action=\"orderservicecall\"><input type=\"submit\" name =\"test\" value=\"setupOrderServiceMessaging\"></form>" +
                "<form action=\"inventoryservicetest\"><input type=\"submit\" name =\"test\" value=\"setupInventoryServiceMessaging\"></form>" +
                "<form action=\"inventoryservicetest\"><input type=\"submit\" name =\"test\" value=\"listenForMessages\"></form>" +
                "<form action=\"inventoryservicetest\"><input type=\"submit\" name =\"test\" value=\"setupTablesQueuesAndPropagation\"></form>" +
                "<form action=\"inventoryservicetest\"><input type=\"submit\" name =\"test\" value=\"cleanUpResources\"></form>" +
                "<h4>Cleanup (drain queues and streams, delete tables and JSON/docs, etc.)...</h4>" +
                "<form action=\"executeonorderpdb\" id=\"executeonorderpdb\">" +
                "<textarea form=\"executeonorderpdb\" rows=\"4\" cols=\"50\" name =\"sql\"\">GRANT PDB_DBA TO orderuser identified by orderuserPW</textarea>" +
                "<br>    user : <input type=\"text\" name=\"orderuser\"  size=\"20\" value=\"\"> " +
                "    password : <input type=\"password\" name=\"orderpassword\"  size=\"20\" value=\"\"> " +
                "   <input type=\"submit\" value=\"executeonorderpdb\"></form>" +

                "<form action=\"executeoninventorypdb\" id=\"executeoninventorypdb\">" +
                "<textarea form=\"executeoninventorypdb\"  rows=\"4\" cols=\"50\" name =\"sql\"\">GRANT PDB_DBA TO inventoryuser identified by inventoryuserPW</textarea>" +
                "<br>    user : <input type=\"text\" name=\"inventoryuser\"  size=\"20\" value=\"\"> " +
                "    password : <input type=\"password\" name=\"inventorypassword\"  size=\"20\" value=\"\"> " +
                "   <input type=\"submit\" value=\"executeoninventorypdb\"></form>" +
                "<br>__________________________________________________________________" +
                "<br>Results......." +
                "<br>" + output + "</th>" +
                "  </tr>" +
                "</table>" +
                "</body></html>";
    }


    private String getPageHeader() {
        return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                "    <title>Home</title>" +
                "  <head><style>\n" +
                "body {background-color: powderblue;}" +
                "h4   {color: blue;}" +
                "</style></head>" +
                "  <body><h1 color=\"#BDB76B;\" align=\"left\">" +
                "<a href=\"\" />Intelligent Event-driven Stateful Microservices with Helidon and Autonomous Database on OCI</a></h1>" +
                "</table>";
    }

}
