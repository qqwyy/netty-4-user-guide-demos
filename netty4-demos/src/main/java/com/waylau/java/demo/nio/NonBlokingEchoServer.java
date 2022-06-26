/**
 * Welcome to https://waylau.com
 */
package com.waylau.java.demo.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Non Bloking Echo Server.
 * 
 * @since 1.0.0 2019年9月28日
 * @author <a href="https://waylau.com">Way Lau</a>
 */
public class NonBlokingEchoServer {

	private static Logger log = LoggerFactory.getLogger(NonBlokingEchoServer.class);

	public static int DEFAULT_PORT = 8080;

	public static void main(String[] args) {
		int port;

		try {
			port = Integer.parseInt(args[0]);
		} catch (RuntimeException ex) {
			port = DEFAULT_PORT;
		}

		ServerSocketChannel serverChannel;
		Selector selector;
		try {
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(port);
			serverChannel.bind(address);
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);

			log.info("NonBlokingEchoServer已启动，端口：{}" ,port);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}

		while (true) {
			try {
				selector.select();
			} catch (IOException e) {
				System.out.println("NonBlockingEchoServer异常!" + e.getMessage());
			}
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			while (iterator.hasNext()) {
				log.info("循环遍历step1  keys   数量:{}", readyKeys.size());
				SelectionKey key = iterator.next();
				iterator.remove();//为什么要删除呢 ？？
				log.info("循环遍历step2  keys   数量:{}", readyKeys.size());
				try {
					// 可连接
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel socketChannel = server.accept();

						System.out.println("NonBlokingEchoServer接受客户端的连接：" + socketChannel);

						// 设置为非阻塞
						socketChannel.configureBlocking(false);

						// 客户端注册到Selector
						SelectionKey clientKey = socketChannel.register(selector,
								SelectionKey.OP_WRITE | SelectionKey.OP_READ);

						// 分配缓存区
						ByteBuffer buffer = ByteBuffer.allocate(100);
						clientKey.attach(buffer);
					}

					// 可读
					if (key.isReadable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						client.read(output);

						System.out.println(client.getRemoteAddress() + " -> NonBlokingEchoServer：" + output.toString());

						key.interestOps(SelectionKey.OP_WRITE);
					}

					// 可写
					if (key.isWritable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						output.flip();
						client.write(output);

						System.out.println("NonBlokingEchoServer  -> " + client.getRemoteAddress() + "：" + output.toString());

						output.compact();

						key.interestOps(SelectionKey.OP_READ);
					}
				} catch (IOException ex) {
					key.cancel();
					try {
						key.channel().close();
					} catch (IOException cex) {
						System.out.println("NonBlockingEchoServer异常!" + cex.getMessage());
					}
				}


			}
		}
	}

}
