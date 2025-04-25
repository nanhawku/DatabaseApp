import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class testDBMain {

    /**
     * Uncomment this to test out your own connection
     */

//    public static void main(String[] args) {
//        Connection conn = null;
//        String url = "jdbc:mysql://localhost:3306/ncat";
//        String user = "AggieAdmin";
//        String pass = "AggiePride1";
//
//        try{
//           Class.forName("com.mysql.cj.jdbc.Driver");
//
//           System.out.println("Connecting to database...");
//           conn = DriverManager.getConnection(url, user, pass);
//
//           System.out.println("Connected!");
//
//        }
//        catch(ClassNotFoundException e){
//            System.out.println( "MySQL JDBC Driver not found!" );
//        }
//        catch(SQLException e){
//            System.out.println( "Cannot connect to DB " + e.getMessage());
//        }
//
//
//
//    } //end main

} //end class