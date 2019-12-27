import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class GroupNIOClient {

    private final String HOST = "127.0.0.1";
    private final Integer PORT = 8888;

    private Selector selector;
    private SocketChannel socketChannel;

    public GroupNIOClient()throws IOException {
        selector = Selector.open();
        socketChannel= SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(HOST,PORT));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println(socketChannel.getLocalAddress() + " is ok..");
    }

    public void start()throws  IOException{
        while(true) {
            int count = selector.select();
            if (count > 0) {
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey selectionKey = selectedKeys.next();
                    if (selectionKey.isReadable()) {
                        read(selectionKey);
                    }
                    selectedKeys.remove();
                }

            }
        }
    }

    private void read(SelectionKey key)throws IOException{
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel)key.channel();
        int count = channel.read(byteBuffer);
        if(count > 0){
            System.out.println(new String(byteBuffer.array()).trim());
        }
    }

    public void send(String message) throws Exception{
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(byteBuffer);
    }

    public static void main(String[] args) throws Exception{
        final GroupNIOClient client = new GroupNIOClient();

        new Thread(()->{
            try {
                client.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()){
            String message = scanner.nextLine();
            client.send(message);
        }
    }
}
