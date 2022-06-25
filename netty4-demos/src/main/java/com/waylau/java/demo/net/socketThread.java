package com.waylau.java.demo.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class socketThread extends Thread {
    private Logger log = LoggerFactory.getLogger(BlockingEchoServer2.class);
    public Socket clientSocket;

    public socketThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        PrintWriter   out = null;
        BufferedReader in = null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // 发送信息给客户端
                    out.println(inputLine);
                    log.info("BlockingEchoServer -> {} :{}" ,clientSocket.getRemoteSocketAddress(),inputLine);
                }
            } catch (IOException e) {
                System.out.println("BlockingEchoServer异常!" + e.getMessage());
            }finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

}