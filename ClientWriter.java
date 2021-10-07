import java.io.DataOutputStream;
import java.util.Scanner;

public class ClientWriter implements Runnable {
  private DataOutputStream ClientWriter;
  
  public ClientWriter(DataOutputStream clientWriter) {
    ClientWriter = clientWriter;
  }

  @Override
  public void run() {
    try {
      Scanner input = new Scanner(System.in);
      int indexToPutPiece;
      
      while(true){
        indexToPutPiece = input.nextInt();
        ClientWriter.writeInt(indexToPutPiece);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
  
}
