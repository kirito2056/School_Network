import java.net.*;
import java.io.*;
import java.util.*;


//The Client that can be run as a console
public class Client {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;
    private String username;
    private InetSocketAddress socketAddress;
    private String notif = " *** ";
    private boolean keepGoing;

    public Client(InetSocketAddress socketAddress, String username) {
        this.socketAddress = socketAddress; // 서버 IP, Port
        this.username = username; // 사용자 이름
    }

    public boolean start() {
        keepGoing = true;
        try {
            socket = new Socket(socketAddress.getAddress(), socketAddress.getPort());
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            return false;
        }

        // 서버 수신 스레드 생성 및 시작 (익명)
        new Thread() {
            @Override
            public void run() {
                while (keepGoing) {
                    try {
                        String msg = (String) inputStream.readObject();
                        System.out.println(msg);
                        System.out.print("> ");
                    } catch (IOException e) {
                        display(notif + " 서버 종료 " + notif);
                        break;
                    } catch (ClassNotFoundException e2){

                    }
                }
            }
        }.start();

        try {
            outputStream.writeObject(username); // 클라이언트 이름 전송
        } catch (IOException e) {
            display("이름 전송 예외 " + e);
            disconnect();
            return false;
        }
        return true;
    }

    private void display(String msg) {
        System.out.println(msg);
    }

    public void sendMessage(ChatMessage msg) {
        try {
            outputStream.writeObject(msg);
        } catch (IOException e) {
            display("전송 예외 : " + e);
        }
    }

    // 연결 종료
    public void disconnect() {
        try {
            keepGoing = false;
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

