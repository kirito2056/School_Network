import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class SocketAddressObject {

    public static void main(String args[]) {
        //#1.Socket Address 객체 생성 ( InetSocketAdress 생성자 사용 )
        try {
            InetAddress ia = InetAddress.getByName("www.google.com");
            int port = 10000;
            InetSocketAddress isa1 = new InetSocketAddress(port);
            InetSocketAddress isa2 = new InetSocketAddress("www.google.com", port);
            InetSocketAddress isa3 = new InetSocketAddress(ia, port);

            System.out.println(isa1);
            System.out.println(isa2);
            System.out.println(isa3);
        }catch (IOException e) {

        }
    }

}