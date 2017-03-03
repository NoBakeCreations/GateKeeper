package com.galacticcookiejar.nobakecreation.gatekeeper;

import com.galacticcookiejar.nobakecreation.minecraft.net.HandshakePacket;
import com.galacticcookiejar.nobakecreation.minecraft.net.MinecraftInputStream;
import com.galacticcookiejar.nobakecreation.minecraft.net.MinecraftOutputStream;

import java.io.IOException;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

  Map<String,Integer> proxyMapping;

  public Server() {
    this.proxyMapping = new HashMap<String, Integer>();
    this.proxyMapping.put("mctest1.galacticcookiejar.com", 5001);
    this.proxyMapping.put("mctest2.galacticcookiejar.com", 5002);
  }

  public void start() throws IOException {
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

        MinecraftInputStream client_mc_input = new MinecraftInputStream(client_connection.getInputStream());
        HandshakePacket client_handshake = (HandshakePacket)client_mc_input.getMinecraftPacket(
            HandshakePacket.State.HANDSHAKING,
            HandshakePacket.BoundTowards.SERVER);
        System.out.println("Connecting to server: " + client_handshake.getServerAddress() + ":" + client_handshake.getServerPort());

        if( this.proxyMapping.containsKey(client_handshake.getServerAddress()) ) {
          int server_port = this.proxyMapping.get(client_handshake.getServerAddress());
          Socket server_connection = new Socket("localhost", server_port);

          client_handshake.writeData(new MinecraftOutputStream(server_connection.getOutputStream()));

          ProxyThread t1 = new ProxyThread(client_connection, server_connection, client_connection.getInputStream(), server_connection.getOutputStream());
          ProxyThread t2 = new ProxyThread(client_connection, server_connection, server_connection.getInputStream(), client_connection.getOutputStream());

          t1.start();
          t2.start();

          connections.add(new Socket[] {server_connection, server_connection});
          proxy_threads.add(new Thread[] {t1, t2});

          System.out.println("Accepted a new connection!");
        } else {
          System.out.println("Unable to connect to server, not found in mapping.");
          client_connection.close();
        }
      } catch(Exception err) {
        System.out.println("Connection failed: " + err);
      }
    }

    try {
      server_handler.close();
    } catch(Exception err) {
    }
  }

  public static void main(String[] argv) throws IOException {
    Server s = new Server();
    s.start();
  }
}
