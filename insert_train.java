import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;



public class insert_train 
{

   public static void main(String[] args) 
   {
  
  try(
    FileInputStream fstream = new FileInputStream("booking.txt");  
    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
  )
  {
  String line;

  //Read File Line By Line
  while ((line = br.readLine()) != null && !line.equals("#"))  
  {
      StringTokenizer st = new StringTokenizer(line, ", ");
      int train_id = Integer.parseInt(st.nextToken());
      String date =  st.nextToken();
      int num_coaches_ac = Integer.parseInt(st.nextToken());
      int num_coaches_sl = Integer.parseInt(st.nextToken());

        String QUERY = "call insert_train_table(" +train_id+",'"+date +"',"+ num_coaches_ac +","+ num_coaches_sl + ");";
      
         try(Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db", "postgres", "1212");
            Statement stmt = conn.createStatement();
            ) 
            {
              stmt.execute(QUERY);		      
            } 
            catch (SQLException e) {
            e.printStackTrace();
            } 
   }

   fstream.close();
  }
  catch (Exception e) 
  {
    e.printStackTrace();
  }
    
}
  
}