
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class GroupNIOServer {
    
    private final Integer PORT = 8888;
    
    private ServerSocketChannel serverSocketChannel;
    
    private Selector selector;
    
    public GroupNIOServer() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    public void listen() throws IOException{
        while(true) {
            int count = selector.select();
            if (count > 0) {
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey selectionKey = selectedKeys.next();
                    if (selectionKey.isAcceptable()) {
                        SocketChannel channel = serverSocketChannel.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        System.out.println(channel.getRemoteAddress() + " 已上线");
                    }
                    if (selectionKey.isReadable()) {
                        read(selectionKey);
                    }
                    selectedKeys.remove();
                }
            }
        }
    }
    
    private void read(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            int count = channel.read(byteBuffer);
            if (count > 0) {
                String message = new String(byteBuffer.array());
                System.out.println(" from 客户端：" +message);
                forward(message, channel);
            }
        }catch (Exception e){
            System.out.println(channel.getRemoteAddress() + " 已离线");
            key.cancel();
            channel.close();
        }
    }

    private void forward(String message,SocketChannel self) throws IOException{
        Iterator<SelectionKey> keys = selector.keys().iterator();
        while(keys.hasNext()){
            SelectionKey next = keys.next();
            Channel channel = next.channel();
            if(channel instanceof SocketChannel && channel != self){
                SocketChannel socketChannel = (SocketChannel)channel;
                message = self.getRemoteAddress()+ " 说："+message;
                ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
                socketChannel.write(byteBuffer);
            }
        }
    }


    public static void main(String[] args) throws IOException{
        GroupNIOServer server = new GroupNIOServer();
        server.listen();
    }
}
