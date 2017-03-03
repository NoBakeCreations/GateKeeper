package com.galacticcookiejar.nobakecreation.minecraft.net;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.DataInputStream;

public class MinecraftInputStream extends DataInputStream {
  private int lastLengthRead;

  public MinecraftInputStream(InputStream in) throws IOException {
    super(in);
    this.lastLengthRead = 0;
  }

  public int getLastLengthRead() {
    return this.lastLengthRead;
  }

  public int readVarInt() throws IOException {
    int numRead = 0,
        result = 0;
    byte read;

    do {
      read = this.readByte();
      int value = ( read & 0b01111111);
      result |= (value << (7 * numRead));

      numRead ++;
      if( numRead > 5 ) {
        throw new IOException("VarInt is too big");
      }
    } while( (read & 0b10000000) != 0 );

    this.lastLengthRead = numRead;
    return result;
  }

  public long readVarLong() throws IOException {
    int numRead = 0;
    long result = 0;
    byte read;
    do {
        read = this.readByte();
        int value = (read & 0b01111111);
        result |= (value << (7 * numRead));

        numRead++;
        if (numRead > 10) {
            throw new RuntimeException("VarLong is too big");
        }
    } while ((read & 0b10000000) != 0);

    this.lastLengthRead = numRead;
    return result;
  }

  public MinecraftPacket getMinecraftPacket(MinecraftPacket.State connectionState, MinecraftPacket.BoundTowards boundTo) throws IOException {
    int packet_length = this.readVarInt();
    byte[] packet = new byte[packet_length];
    this.readFully(packet);
    MinecraftInputStream packet_stream = new MinecraftInputStream(new ByteArrayInputStream(packet));
    int packet_id = packet_stream.readVarInt();

    if( boundTo == MinecraftPacket.BoundTowards.SERVER ) {
      switch(connectionState) {
        case HANDSHAKING:
            switch(packet_id) {
              case 0:
                  return new HandshakePacket(new MinecraftInputStream(new ByteArrayInputStream(packet)));
              default: break;
            }
            break;
        case LOGIN:
        case PLAY:
        case STATUS:
        default:
            break;
      }
    } else if( boundTo == MinecraftPacket.BoundTowards.CLIENT ) {

    }

    // Something about deconstructing the packet and building out the appropriate MinecraftPacket here.
    return null;
  }
}
