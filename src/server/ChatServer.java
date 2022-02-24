package server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @author miemiehoho
 * @date 2022/2/24 19:58
 */
// 网络聊天室服务器端
public class ChatServer {


    // 服务器端启动方法
    private void startServer() throws Exception {
        // 1 创建Selector选择器
        Selector selector = Selector.open();
        // 2 创建 ServerSocketChannel 通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 3 为通道绑定监听端口
        InetSocketAddress port = new InetSocketAddress(8000);
        serverSocketChannel.bind(port);
        // 设置非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 4 channel注册到selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功！");
        // 5 循环，监听新进入的连接
        for (; ; ) {// 实现无限循环的两种方式 for和while，for底层实现更优
            // 获取channel数量
            if (selector.select() == 0) {
                continue;
            }
            // 获取可用的channel
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            // 遍历集合
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                // 移除set中当前key
                iterator.remove();

                // 6 根据就绪状态，调用对应方法实现具体业务操作
                //  6.1 如果 accept状态
                if (selectionKey.isAcceptable()) {

                    acceptOperator(serverSocketChannel, selector);

                    //  6.2 如果可读状态
                } else if (selectionKey.isReadable()) {
                    readOperator(selector, selectionKey);
                }
            }
        }
//        while (true) {
//
//        }

    }

    // 处理可读状态的操作
    private void readOperator(Selector selector, SelectionKey selectionKey) throws Exception {
        // 1 从selectionKey获取已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        // 2 创建buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 3 循环读取客户端信息
        int readLength = socketChannel.read(buffer);
        String msg = "";
        while (readLength > 0) {
            // 切换读写模式
            buffer.flip();
            // 读取内容
            msg += Charset.forName("UTF-8").decode(buffer);
            buffer.clear();
            readLength = socketChannel.read(buffer);
        }
        // 4 channel再次注册到selector，监听可读状态
        socketChannel.register(selector, SelectionKey.OP_READ);
        // 5 把客户端消息广播给其他客户端
        if (msg.length() > 0) {
            System.out.println(msg);
            // 消息广播给其他客户端
            castOtherClient(msg, selector, socketChannel);
        }
    }

    // 广播给其他客户端
    private void castOtherClient(String msg, Selector selector, SocketChannel socketChannel) throws Exception {
        // 1 获取所有已经接入的client
        Set<SelectionKey> keySet = selector.keys();

        // 2 循环向所有channel广播消息，不需要广播给当前channel
        for (SelectionKey selectionKey : keySet) {
            // 获取channel
            Channel targetChannel = selectionKey.channel();
            // 不需要发送给自己
            if (targetChannel instanceof SocketChannel && targetChannel != socketChannel) {
                ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(msg));
            }
        }
    }

    // 处理接入状态的操作
    private void acceptOperator(ServerSocketChannel serverSocketChannel, Selector selector) throws Exception {
        // 1 获取socketchannel
        SocketChannel socketChannel = serverSocketChannel.accept();
        // 2 sc设置非阻塞
        socketChannel.configureBlocking(false);
        // 3 sc注册到selector，并监听可读状态
        socketChannel.register(selector, SelectionKey.OP_READ);
        // 4 通知客户端连接成功
        socketChannel.write(Charset.forName("UTF-8").encode("欢迎进入聊天室，请注意隐私安全！"));
    }


    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        try {
            chatServer.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
