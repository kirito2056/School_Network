
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
    private static int uniqueId; // 번호
    private ArrayList<ClientThread> clientThreads; // 클라이언트 배열
    private SimpleDateFormat simpleDateFormat; // 특정 문자열 포맷으로 날짜 표현
    private int port; // 서버 포트
    private boolean isRunning; // 서버 실행 여부
    private String notif = " *** ";


    public Server(int port) {
        this.port = port;
        this.simpleDateFormat = new SimpleDateFormat("HH:mm:ss"); // 시/분/초
        this.clientThreads = new ArrayList<>();
    }

    public void start() {
        isRunning = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            display("[서버 시작]");
            while (isRunning) {
                Socket socket = serverSocket.accept(); // 클라이언트 요청 수락
                if (!isRunning) break;
                ClientThread clientThread = new ClientThread(socket);
                clientThreads.add(clientThread);
                clientThread.start(); // 스레드 시작
            }

        } catch (IOException e) {
            display("서버 예외 발생 " + e);
        }
        System.out.println("[서버 종료]");
    }

    public void stop() {
        isRunning = false;
    }

    private void display(String message) {
        String time = simpleDateFormat.format(new Date()) + " " + message; // HH:mm:ss message
        System.out.println(time);
    }


    // synchronized - Thread 동기화(Thread-Safe)
    // 접속된 모든 클라이언트에게 Message전달
    private synchronized boolean broadcast(String message) {
        String time = simpleDateFormat.format(new Date());
        String msg = time + " " + message;

        // 클라이언트의 연결이 끊어진 경우가 있을 수 있기 때문에 역순으로 조회
        for (int i = clientThreads.size() - 1; i >= 0; i--) {
            ClientThread clientThread = clientThreads.get(i);
            boolean rs = clientThread.writeMsg(msg);
            if (!rs) {
                clientThreads.remove(i);
                display("Client : " + clientThread.userName + " 연결 종료.");
            }
        }
        return true;
    }

    // 로그아웃
    private synchronized void remove(int id) {
        String disconnectedClient = "";
//        for (int i = 0; i < clientThreads.size(); ++i) {
        for (ClientThread clientThread : clientThreads) {
            if (clientThread.id == id) {
                disconnectedClient = clientThread.getUsername();
                clientThreads.remove(clientThread);
                break;
            }
        }
        broadcast(notif + disconnectedClient + "님이 채팅방을 나갔습니다." + notif);
    }

    // 클라이언트 스레드
    class ClientThread extends Thread {
        // the socket to get messages from client
        Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        // 고유 아이디
        int id;
        String userName;
        // 메시지 타입
        ChatMessage chatMessage;
        boolean keepGoing;

        public ClientThread(Socket socket) {
            id = ++uniqueId;
            keepGoing = true;
            this.socket = socket;
            display("클라이언트 " + socket.getInetAddress() + ":" + socket.getPort() + " 접속");
            try {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                userName = (String) inputStream.readObject();
                broadcast(notif + userName + "님이 채팅방에 들어오셨습니다." + notif);
            } catch (IOException e) {
                display(" " + e);
            }catch (ClassNotFoundException e2){

            }
        }

        public String getUsername() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        @Override
        public void run() {
            while (keepGoing) {
                try {
                    chatMessage = (ChatMessage) inputStream.readObject();
                } catch (IOException e) {
                    display(userName + "님 예외 발생 " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                String message = chatMessage.getMessage();
                switch (chatMessage.getType()) {
                    case MESSAGE:
                        broadcast(userName + " : " + message);
                        break;
                    case LOGOUT:
                        display(userName + " 로그아웃.");
                        keepGoing = false;
                        break;
                }
            } // while
            remove(id); // 현재 Thread 관리 삭제
            close();
        }

        // close everything
        public void close() {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
            }
        }

        // client output stream
        private boolean writeMsg(String msg) {
            // 연결 여부 확인
            if (!socket.isConnected()) {
                close();
                return false;
            }
            try {
                outputStream.writeObject(msg);
            } catch (IOException e) {
                display(notif + "메시지 전송 에러 " + userName + notif);
                display(e.toString());
            }
            return true;
        }
    }
}

