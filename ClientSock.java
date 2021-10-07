import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.net.UnknownHostException;

public class ClientSock {
  public static void main(String[] args) {
    try{
      Socket cs = new Socket("localhost", 8888);
      ClientListener clientListener = new ClientListener(new DataInputStream(cs.getInputStream()));
      ClientWriter clientWriter = new ClientWriter(new DataOutputStream(cs.getOutputStream()));
      
       Thread tListener = new Thread(clientListener);
       Thread tWriter = new Thread(clientWriter);

      tListener.start();
      tWriter.start();

    } catch(EOFException e){
      System.out.println("Error EOF");
    } catch(UnknownHostException e){
      System.out.println("Host desconhecido");
    } catch(IOException e){
      System.out.println("IOException");
    }
  }
}