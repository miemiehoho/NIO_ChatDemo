package client;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author miemiehoho
 * @date 2022/2/24 19:58
 */
// 客户端
public class ChatClient {

    // 客户端启动方法
    public void startClient(String name) throws Exception {
        // 连接服务器端
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8000);
        SocketChannel socketChannel = SocketChannel.open(address);

        // 接受服务器端的消息
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        // 创建一个线程
        new Thread(() -> {
            try {
                receiveMsg(selector);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 向服务器端发送消息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            if (msg.length() > 0) {
                socketChannel.write(Charset.forName("UTF-8").encode(name + ": " + msg));
            }
        }

    }

    private void receiveMsg(Selector selector) throws Exception {
        for (; ; ) {
            if (selector.select() == 0) {
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (selectionKey.isReadable()) {
                    readOperator(selector, selectionKey);
                }
            }
        }
    }

    private void readOperator(Selector selector, SelectionKey selectionKey) throws Exception {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int readLength = socketChannel.read(buffer);
        String msg = "";
        while (readLength > 0) {
            buffer.flip();
            msg += Charset.forName("UTF-8").decode(buffer);
            buffer.clear();
            readLength = socketChannel.read(buffer);
        }
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println(msg);
    }


}
