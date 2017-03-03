package com.galacticcookiejar.nobakecreation.gatekeeper;

import com.galacticcookiejar.nobakecreation.minecraft.net.HandshakePacket;
import com.galacticcookiejar.nobakecreation.minecraft.net.MinecraftInputStream;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Thread;
import java.net.Socket;

import com.galacticcookiejar.nobakecreation.minecraft.net.MinecraftPacket;

public class ProxyThread extends Thread {
  private InputStream incoming;
  private OutputStream outgoing;
  private Socket client, server;

  public ProxyThread(Socket client, Socket server, InputStream incoming, OutputStream outgoing) {
    super("GateKeeperProxyTransceiver");
    this.incoming = incoming;
    this.outgoing = outgoing;
    this.client = client;
    this.server = server;
  }

  public void run() {

    try {
      while( this.client.isConnected() && this.server.isConnected() && !this.isInterrupted() ) {
        int buffer_size = incoming.available();
        if( buffer_size > 0 ) {
          byte[] buffer = new byte[buffer_size];
          int read_size = this.incoming.read(buffer);
          if( read_size > buffer_size ) {
            throw new IOException("Read too many bytes");
          }
          this.outgoing.write(buffer, 0, read_size);
        } else {
          Thread.sleep(200);
        }
      }
    } catch(IOException err) {
      System.out.println("Something failed in thread IO: " + err);
    } catch(Exception err) {
      System.out.println("Something overly generic failed in this thread: " + err);
    }

    try {
      this.incoming.close();
    } catch(IOException err) {
      System.out.println("Failed to close incoming input stream: " + err);
    }

    try {
      this.outgoing.close();
    } catch(IOException err) {
      System.out.println("Failed to close incoming input stream: " + err);
    }
  }

}
