import org.junit.internal.runners.statements.ExpectException;

import java.sql.Connection;
import java.sql.DriverManager;

public class con {
    //database url, username password

    private static final  String url = "jbc:mysql://localhost:3306/Farm_Management_System";
    private static final String username ="root";
    private static final String password = "3399";

    public static Connection getConnection(){
        Connection  conn = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection(url, username, password);
        }catch(Exception e){
            e.printStackTrace();
        }
        return conn;
    }

}
