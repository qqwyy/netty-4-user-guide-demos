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
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Blocking Echo Client
 * @since 1.0.0 2019年9月28日
 * @author <a href="https://waylau.com">Way Lau</a> 
 */
public class BlockingEchoClient {

    private static Logger log = LoggerFactory.getLogger(BlockingEchoClient.class);

	public static void main(String[] args) {
        String hostName = "127.0.0.1";
        int portNumber = 7;
		if (args.length != 2) {
		    //do nothing
        }else{
             hostName = args[0];
             portNumber = Integer.parseInt(args[1]);
        }

        try{
            Socket echoSocket = new Socket(hostName, portNumber);
            log.info("连接{}:{}  连接状态：{}",hostName,portNumber,echoSocket.isConnected());

            PrintWriter out   = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("不明主机，主机名为： " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("不能从主机中获取I/O，主机名为：" + hostName);
            System.exit(1);
        } 
	}

}
