package com.galacticcookiejar.nobakecreation.gatekeeper;

import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

  public Server() {

  }

  public void start() {
    List<Socket[]> connections = new ArrayList<Socket[]>();
    List<Thread[]> proxy_threads = new ArrayList<Thread[]>();

    ServerSocket server_handler = null;
    Socket client_connection = null;

    try {
      server_handler = new ServerSocket(25565, 10);
    } catch(Exception err) {
      System.out.println("Failed to establish server: " + err);
    }

    while( ! server_handler.isClosed() ) {
      try {
        client_connection = server_handler.accept();
        Socket server_connection = new Socket("localhost", 5001);

        ProxyThread t1 = new ProxyThread(client_connection, server_connection, client_connection.getInputStream(), server_connection.getOutputStream());
        ProxyThread t2 = new ProxyThread(client_connection, server_connection, server_connection.getInputStream(), client_connection.getOutputStream());
        t1.start();
        t2.start();

        connections.add(new Socket[] {server_connection, server_connection});
        proxy_threads.add(new Thread[] {t1, t2});

        System.out.println("Accepted a new connection!");
      } catch(Exception err) {
        System.out.println("Connection failed: " + err);
      }
    }

    try {
      server_handler.close();
    } catch(Exception err) {
    }
  }

  public static void main(String[] argv) {
    Server s = new Server();
    s.start();
  }
}
