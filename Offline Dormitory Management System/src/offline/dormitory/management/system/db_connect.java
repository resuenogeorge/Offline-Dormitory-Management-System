package offline.dormitory.management.system;

import java.sql.Connection;
import java.sql.DriverManager;

public class db_connect {
    
    static Connection con = null;
    
    public static Connection getConnection(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
             con = DriverManager.getConnection("jdbc:mysql://localhost:3306/dormitory_db", "root","");           
        }catch(Exception e){
            e.printStackTrace(); 
        }
        return con;
    }

    
    }