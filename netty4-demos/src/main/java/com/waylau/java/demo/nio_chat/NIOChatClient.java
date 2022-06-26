package com.waylau.java.demo.nio_chat;



import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class NIOChatClient {
    private static Logger log = LoggerFactory.getLogger(NIOChatClient.class);
    private Selector      selector = null;
    private SocketChannel client   = null;
    private Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) throws IOException {
        new NIOChatClient().session();
    }

    public NIOChatClient() throws IOException{
        selector = Selector.open();
        //连接远程主机的IP和端口
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8080);
        client = SocketChannel.open(inetSocketAddress);
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        log.info("本地地址：{} 连接服务端：{}  状态：{}"
                ,client.getLocalAddress().toString()
                ,inetSocketAddress.toString()
                ,client.isConnected()
        );
    }

    public void session(){
        //开辟一个新线程从服务器端读数据
        new Reader().start();
        //开辟一个新线程往服务器端写数据
        new Writer().start();
    }

    private class Writer extends Thread{

        @Override
        public void run() {
            try{
                //在主线程中 从键盘读取数据输入到服务器端
                Scanner scan = new Scanner(System.in);
                while(scan.hasNextLine()){
                    String line = scan.nextLine();
                    if("".equals(line)) continue; //不允许发空消息
//		            client.register(selector, SelectionKey.OP_WRITE);
                    client.write(charset.encode(line));//client既能写也能读，这边是写
                }
                scan.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    private class Reader extends Thread {
        public void run() {
            try {
                while(true) {
                    int readyChannels = selector.select();
                    if(readyChannels == 0) continue;
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();  //可以通过这个方法，知道可用通道的集合
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                    while(keyIterator.hasNext()) {
                        SelectionKey key = (SelectionKey) keyIterator.next();
                        keyIterator.remove();
                        process(key);
                    }
                }
            }
            catch (IOException io){

            }
        }

        private void process(SelectionKey key) throws IOException {
            if(key.isReadable()){
                //使用 NIOServerDemoBak 读取 Channel中的数据，这个和全局变量client是一样的，因为只注册了一个SocketChannel
                //client既能写也能读，这边是读
                SocketChannel sc = (SocketChannel)key.channel();
                log.info("isReadable: {} 来自IP地址为：{} ",key.isReadable(),client.getRemoteAddress());
                ByteBuffer buff = ByteBuffer.allocate(1024);
                String content = "";
                while(sc.read(buff) > 0)
                {
                    buff.flip();
                    content += charset.decode(buff);
                }
                if(StringUtil.isNullOrEmpty(content.toString())){
                    key.cancel();
                }else{
                    log.info("来自服务端的消息：{}",content);
                    key.interestOps(SelectionKey.OP_READ);
                }

            }
        }
    }
}

