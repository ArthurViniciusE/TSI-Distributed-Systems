import java.io.DataInputStream;

public class ClientListener implements Runnable{
  private DataInputStream din;

  public ClientListener(DataInputStream din) {
    this.din = din;
  }

  @Override
  public void run() {
    try{
      while(true){
        System.out.println(din.readUTF());
      }
    } catch(Exception e){
      e.printStackTrace();
    }
  }
  
}
