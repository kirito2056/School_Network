import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;

public class ClientProcess {
    public static void main(String[] args) {
        try {
            Scanner scan = new Scanner(System.in);
            String userName = "";
            int port = 11000; // 서버 포트

            System.out.println("이름을 입력해 주세요 : ");
            userName = scan.nextLine();

//            InetAddress inetAddress = InetAddress.getByAddress(new byte[]{(byte) 10, (byte) 80, (byte) 163, (byte) 163});
            InetAddress inetAddress = InetAddress.getLoopbackAddress();
            InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);

            Client client = new Client(socketAddress, userName);
            if (!client.start())
                return;

            System.out.println("\n안녕하세요. 오픈 채팅방에 오신 것을 환영합니다.");
            System.out.println("사용 방법");
            System.out.println("\t1. 기본 메시지의 경우 모든 유저에게 전달됩니다.");
            System.out.println("\t2. 개인 메시지의 경우 '@수신(받는 userName)<space>송신(보내는 userName)' ex) @User1 User2 안녕하세요.");
            System.out.println("\t3. 로그아웃 시 'LOGOUT'을 입력");
            System.out.println();

            while (true) {
                System.out.print("> ");
                String msg = scan.nextLine();
                if (msg.equalsIgnoreCase("LOGOUT")) { // 로그아웃 입력 시 (대소문자 구분 x)
                    client.sendMessage(new ChatMessage(ChatMessageType.LOGOUT, ""));
                    System.out.println("[로그아웃]");
                    break;
                } else {
                    client.sendMessage(new ChatMessage(ChatMessageType.MESSAGE, msg));
                }
            }
            client.disconnect();
            scan.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
