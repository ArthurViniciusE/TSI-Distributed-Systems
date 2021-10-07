import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class ServSock {

  public static final int[][] WIN_CONDITIONS = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 },
      { 2, 5, 8 }, { 0, 4, 8 }, { 2, 4, 6 } };
  public static int moveCount = 0;
  public static String status = "Ongoing";
  public static ArrayList<Socket> connectionsList = new ArrayList<>();
  public static ArrayList<DataInputStream> dinList = new ArrayList<>();
  public static ArrayList<DataOutputStream> doutList = new ArrayList<>();
  
  public static boolean isFinished(String[] board) {
    int xCount = 0;
    int oCount = 0;

    for (int i = 0; i < WIN_CONDITIONS.length; i++) {
      for (int j = 0; j < WIN_CONDITIONS[i].length; j++) {
        if (board[WIN_CONDITIONS[i][j]].equalsIgnoreCase(" X")) {
          xCount++;
        } else if (board[WIN_CONDITIONS[i][j]].equalsIgnoreCase(" O")) {
          oCount++;
        }
        if (j == (WIN_CONDITIONS[i].length - 1)) {
          if (xCount == 3) {
            status = "Player 1 has won";
            return true;
          } else if (oCount == 3) {
            status = "Player 2 has won";
            return true;
          } else {
            xCount = 0;
            oCount = 0;
          }
        }
      }
    }
    if (moveCount == 8) {
      status = "Draw!";
      return true;
    }
    return false;
  }

  public static String getFormattedBoard(String[] board){
    String boardState = "\n     TIC-TAC-TOE \n";
    boardState += " +-----+-----+-----+\n";
    for (int i = 1; i <= board.length; i++) {
      boardState += " | " + board[i - 1] + " ";
      if (i % 3 == 0) {
        boardState += " |\n";
        boardState += " +-----+-----+-----+\n";
      }
    }
    return boardState;
  }

  public static void sendMessageToAll(String message) throws IOException{
    for (int i = 0; i < doutList.size(); i++) {
      doutList.get(i).writeUTF(message);
      doutList.get(i).flush();
    }
  }

  public static void sendMessage(String message, int playerIndex) throws IOException{
    doutList.get(playerIndex).writeUTF(message);
    doutList.get(playerIndex).flush();
  }

  public static boolean isPlaceOccupied(String[] board, int indexToPutThePiece){
    if(board[indexToPutThePiece].equals(" X") || board[indexToPutThePiece].equals(" O")){
      return true;
    } else {
      return false;
    }
  }

  public static boolean isIndexValid(int indexToPutThePiece){
    if(indexToPutThePiece >= 0 && indexToPutThePiece < 9){
      return true;
    } else {
      return false;
    }
  }
  public static void main(String[] args) {
    try {
      ServerSocket ss = new ServerSocket(8888);
      System.out.println("Listening for connections...");
      int connectionsCount = 0;
      while (connectionsList.size() < 2) {
        connectionsList.add(ss.accept());
        dinList.add(new DataInputStream(connectionsList.get(connectionsCount).getInputStream()));
        doutList.add(new DataOutputStream(connectionsList.get(connectionsCount).getOutputStream()));
        if(connectionsCount == 0){
          sendMessage("You're connected, waiting for another player to join.", connectionsCount);
          connectionsCount++;
        }
      }
      
      boolean restartGame = false;
      do{
        sendMessageToAll("Everyone is connected, the game will start.");
        String[] gameBoard = new String[9];
        for (int i = 1; i <= 9; i++) {
          gameBoard[i - 1] = " " + i;
        }
        int playerIndex = 0;
        int opponentIndex = 1;
        int indexToPutThePiece;
        while (moveCount < 9) {
          sendMessage("Waiting for player " + (playerIndex + 1) + ".", opponentIndex);
          sendMessage("It's your turn, you're: " + ((playerIndex == 0) ? "X" : "O"), playerIndex);
          sendMessageToAll(getFormattedBoard(gameBoard));
          do{
            indexToPutThePiece = dinList.get(playerIndex).readInt() - 1;
            if(isIndexValid(indexToPutThePiece)){
              if(!isPlaceOccupied(gameBoard, indexToPutThePiece)){
                gameBoard[indexToPutThePiece] = (playerIndex == 0) ? " X" : " O";
                break;
              } else {
                sendMessage("You're trying to put a piece in a place already occupied, try again.", playerIndex);
              }
            } else {
              sendMessage("Type a number between 1 and 9 to fill the board.", playerIndex);
            }
          } while(!isIndexValid(indexToPutThePiece) || isPlaceOccupied(gameBoard, indexToPutThePiece));
          // Clears the console.
          sendMessageToAll("\033[H\033[2J"); // <--These are ANSI escape codes.
          if (moveCount >= 4) {
            if(isFinished(gameBoard)){
              sendMessageToAll(status);
              sendMessageToAll(getFormattedBoard(gameBoard));
              sendMessageToAll("\n The game has ended. Do you wish to play again? Type 1/0 to answer. 1: Yes. 0: No.");
              int[] answer = new int[2];
              for (int i = 0; i < dinList.size(); i++) {
                answer[i] = dinList.get(i).readInt();
              }
              if(answer[0] == 1 && answer[1] == 1){
                restartGame = true;
                moveCount = 0;
                status = "Ongoing";
                break;
              } else {
                sendMessageToAll("The game has ended, one or more players decided to not play again.");
                restartGame = false;
                break;
              }
            };
          }
          if (playerIndex == 0) {
            playerIndex++;
            opponentIndex--;
          } else {
            playerIndex--;
            opponentIndex++;
          }
          moveCount++;
        }
      } while(restartGame);
      for (int i = 0; i < connectionsList.size(); i++) {
        dinList.get(i).close();
        doutList.get(i).close();
        connectionsList.get(i).close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}