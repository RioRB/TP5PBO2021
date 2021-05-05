/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author Fauzan
 */
public class dbConnection {
    public static Connection con;
    public static Statement stm;
    
    public void connect(){//untuk membuka koneksi ke database
        try {
            String url ="jdbc:mysql://localhost/db_gamepbo";
            String user="root";
            String pass="";
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url,user,pass);
            stm = con.createStatement();
            System.out.println("koneksi berhasil;");
        } catch (Exception e) {
            System.err.println("koneksi gagal" +e.getMessage());
        }
    }
    
    public DefaultTableModel readTable(){
        
        DefaultTableModel dataTabel = null;
        try{
            Object[] column = {"No", "Username", "Score"};
            connect();
            dataTabel = new DefaultTableModel(null, column);
            String sql = "Select * FROM highscore ORDER BY score DESC";
            ResultSet res = stm.executeQuery(sql);
            
            int no = 1;
            while(res.next()){
                Object[] hasil = new Object[3];
                hasil[0] = no;
                hasil[1] = res.getString("Username");
                hasil[2] = res.getString("Score");
                no++;
                dataTabel.addRow(hasil);
            }
        }catch(Exception e){
            System.err.println("Read gagal " +e.getMessage());
        }
        
        return dataTabel;
    }
    
    public void addData(String username, int poin){
        connect();
        
         
        String queryName = "SELECT * FROM highscore WHERE Username='"+username+"'";
         
       try{
        //  System.out.println(queryName);
          stm = con.createStatement();
          ResultSet res = stm.executeQuery(queryName);
          res.next();
          int hasil = res.getInt("Score");
        //  System.out.println(hasil);
          if(poin > hasil){
              String sql = "UPDATE highscore SET Score='"+poin+"' WHERE Username='"+username+"'";
              stm.executeUpdate(sql);
            //  System.out.println(sql);
          }  
       } catch(SQLException ex){
           String sql = "INSERT INTO highscore (Username, Score) VALUES ('"+username+"', "+poin+")";
            try {
                stm.executeUpdate(sql);
            } catch (SQLException ex1) {
                Logger.getLogger(dbConnection.class.getName()).log(Level.SEVERE, null, ex1);
            }
       }
    }
}
