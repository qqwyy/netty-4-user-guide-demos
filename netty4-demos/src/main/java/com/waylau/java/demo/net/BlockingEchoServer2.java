/**
 * Welcome to https://waylau.com
 */
package com.waylau.java.demo.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Blocking Echo Server.
 * 同一时间 可接受多个客户端通信
 */
public class BlockingEchoServer2 {
	private static Logger log = LoggerFactory.getLogger(BlockingEchoServer2.class);
	public static int DEFAULT_PORT = 7;

	public static void main(String[] args) {
		int port;
		try {
			port = Integer.parseInt(args[0]);
		} catch (RuntimeException ex) {
			port = DEFAULT_PORT;
		}
		
		ServerSocket serverSocket = null;
		try {
			// 服务器监听
			serverSocket = new ServerSocket(port);
			log.info("服务端BlockingEchoServer2已启动，端口：{}" ,port);

            //服务器端一直监听这个端口，等待客户端的连接
			while(true) {
				// 接受客户端建立链接，生成Socket实例
				Socket clientSocket = serverSocket.accept();
				log.info("新client连接：{}",clientSocket.getRemoteSocketAddress().toString());
				new socketThread(clientSocket).start();//新建一个socketThread处理这个客户端的socket连接
			}

		} catch (IOException e) {
			System.out.println("BlockingEchoServer启动异常，端口：" + port);
			System.out.println(e.getMessage());
		}
	}

}
