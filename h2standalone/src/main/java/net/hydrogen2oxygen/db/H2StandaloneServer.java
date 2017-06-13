package net.hydrogen2oxygen.db;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.h2.tools.Server;

public class H2StandaloneServer {

   protected Properties properties;

   protected String propertiesFilePath;

   public H2StandaloneServer(String propertiesFilePath) throws Exception {
      properties = new Properties();
      properties.load(new FileInputStream(propertiesFilePath));
   }

   public void init() throws Exception {

      System.out.println("... initializing a H2 Standalone Server ...");

      String portTcp = getString("h2.portTcp");
      String portWeb = getString("h2.portWeb");
      String databaseName = getString("h2.databaseName");
      String initConfiguration = getString("h2.portTcp");
      String username = getString("h2.username");
      String password = getString("h2.password");
      boolean initDatabase = getBoolean("h2.initDatabase");

      String connectionUrl = getString("h2.connectionUrl");

      if (initDatabase) {
         connectionUrl += initConfiguration;
      }

      connectionUrl = connectionUrl.replaceAll("#PORT#", portTcp).replaceAll("#DATABASENAME#", databaseName);

      System.out.println(String.format(
            "--------------------\nThese are your setting:\nportTcp = %s\nportWeb = %s\ndatabaseName = %s\ninitConfiguration = %s\ninitDatabase = %s\nconnectionUrl = %s;USER=%s;PASSWORD=%s\n--------------------",
            portTcp, portWeb, databaseName, initConfiguration, initDatabase, connectionUrl, username, password));

      String[] tcpConnection = new String[] { "-tcp", "-tcpPort", portTcp };
      String[] webConnection = new String[] { "-web", "-webPort", portWeb };

      // One TcpServer which connects with the in memory database
      Server tcpServer = Server.createTcpServer(tcpConnection).start();
      // and one WebServer providing a gui for the database
      Server webServer = Server.createWebServer(webConnection).start();

      System.out.println("Server started and connection is open.");

      System.out.println("TcpServer URL: jdbc:h2:" + tcpServer.getURL());
      System.out.println("WebServer URL: " + webServer.getURL());

      if (getString("h2.username") != null) {
         Connection conn = DriverManager.getConnection(connectionUrl);
         Statement stmt = conn.createStatement();
         stmt.execute(String.format("create user if not exists %s password '%s'", getString("h2.username"), getString("h2.password")));
         stmt.close();
         conn.close();
      }

      // Open the Gui in the Browser
      if (getBoolean("h2.automaticallyStartGuiInBrowser")) {
         Connection conn = openBrowserGui(connectionUrl);
         conn.close();
      }

      System.out.println("Press [Enter] to stop.");
      System.in.read();

      System.out.println("Stopping server and closing the connection");

      webServer.stop();
      tcpServer.stop();
   }

   public static void main(String[] args) throws Exception {

      if (args.length == 0) {
         System.err.println("Please provide a configuration property file as first argument!");
         System.exit(0);
      }

      H2StandaloneServer h2 = new H2StandaloneServer(args[0]);
      h2.init();
   }

   private Connection openBrowserGui(String connectionUrl) throws ClassNotFoundException, SQLException {

      Class.forName("org.h2.Driver");
      Connection conn = DriverManager.getConnection(connectionUrl);
      Server.startWebServer(conn);
      return conn;
   }

   protected int getInt(String key) {

      return Integer.parseInt((String) properties.get(key));
   }

   protected String getString(String key) {

      return (String) properties.get(key);
   }

   protected boolean getBoolean(String key) {

      return Boolean.parseBoolean((String) properties.get(key));
   }
}

