package com.waylau.java.demo.nio_chat;



import com.waylau.java.demo.nio.NonBlokingEchoServer;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 功能1： 客户端通过Java NIO连接到服务端，支持多客户端的连接
 */
public class NIOChatServer {

    private static Logger log = LoggerFactory.getLogger(NIOChatServer.class);

    private int port        = 8080;
    private Charset charset = Charset.forName("UTF-8");
    private Selector selector = null;


    public static void main(String[] args) throws IOException {
        new NIOChatServer(8080).listen();
    }

    public NIOChatServer(int port) throws IOException{
        this.port = port;
        ServerSocketChannel server = ServerSocketChannel.open();
                            server.bind(new InetSocketAddress(this.port));
                            server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        log.info("服务已启动，监听端口是：{}",this.port);
    }

    /*
     * 开始监听
     */
    public void listen() throws IOException{
        while(true) {
            int wait = selector.select();
            if(wait == 0) continue;
            Set<SelectionKey> keys = selector.selectedKeys();  //可以通过这个方法，知道可用通道的集合
            Iterator<SelectionKey> iterator = keys.iterator();
            while(iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();
                process(key);
            }
        }

    }


    public void process(SelectionKey key) throws IOException {

        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel)key.channel();
            SocketChannel       client = server.accept();
            log.info("新客户端连接，IP地址为 :{}",client.getRemoteAddress().toString());
            //非阻塞模式
            client.configureBlocking(false);
            //注册选择器，并设置为读取模式，收到一个连接请求，然后起一个SocketChannel，并注册到selector上，之后这个连接的数据，就由这个SocketChannel处理
            client.register(selector, SelectionKey.OP_READ);
            //将此对应的channel设置为准备接受其他客户端请求
            key.interestOps(SelectionKey.OP_ACCEPT);
            client.write(charset.encode("欢迎欢迎！"));
        }
        //处理来自客户端的数据读取请求
        if(key.isReadable()){

            //返回该SelectionKey对应的 Channel，其中有数据需要读取
            SocketChannel client  = (SocketChannel)key.channel();
            log.info("isReadable: {} 来自IP地址为：{} ",key.isReadable(),client.getRemoteAddress());
            ByteBuffer      buff  = ByteBuffer.allocate(100);
            StringBuilder content = new StringBuilder();
            try{
                while(client.read(buff) > 0) {
                    buff.flip();
                    content.append(charset.decode(buff));
                }
                if(StringUtil.isNullOrEmpty(content.toString())){
                    key.cancel();
                }else{
                    log.info("收到来自IP地址为：{} 的消息: {}",client.getRemoteAddress(),content);
                    //将此对应的channel设置为准备下一次接受数据
                    key.interestOps(SelectionKey.OP_READ);
                }
            }catch (IOException io){
                key.cancel();
                if(key.channel() != null) {
                    key.channel().close();
                }
            }

            if(content.length() > 0) {
                  client.write(charset.encode(content.toString()));
            }

        }
    }


//    public void broadCast(SocketChannel client, String content) throws IOException {
//        //广播数据到所有的SocketChannel中
//        for(SelectionKey key : selector.keys()) {
//            Channel targetchannel = key.channel();
//            //如果client不为空，不回发给发送此内容的客户端
//            if(targetchannel instanceof SocketChannel && targetchannel != client) {
//                SocketChannel target = (SocketChannel)targetchannel;
//                target.write(charset.encode(content));
//            }
//        }
//    }

}

