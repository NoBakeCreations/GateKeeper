package com.galacticcookiejar.nobakecreation.minecraft.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class MinecraftPacket {
  /* Follows the protocol spec layed out at http://wiki.vg/Protocol */

  public static enum State {
    HANDSHAKING,
    LOGIN,
    PLAY,
    STATUS
  }

  public static enum BoundTowards {
    CLIENT,
    SERVER
  }

  private int packetId;
  private int length;
  private int lengthRaw;
  protected MinecraftInputStream in;
  private boolean compressed = false;
  private boolean readOnly = false;
  private byte[] data;
  private byte[] dataRaw;

  public MinecraftPacket(MinecraftInputStream in) throws IOException {
    this(in, false, false);
  }

  public MinecraftPacket(MinecraftInputStream in, boolean compressed) throws IOException {
    this(in, compressed, false);
  }

  public MinecraftPacket(MinecraftInputStream in, boolean compressed, boolean readOnly) throws IOException {
    this.readOnly = readOnly;
    this.compressed = compressed;

    this.lengthRaw = in.readVarInt();

    if( ! this.compressed ) {
      this.dataRaw = new byte[this.lengthRaw];
      in.readFully(this.dataRaw);

      this.data = this.dataRaw;
      this.length = this.lengthRaw;

    } else {
      this.length = in.readVarInt();
      this.dataRaw = new byte[this.lengthRaw - in.getLastLengthRead()];
      this.data = new byte[this.length];

      in.readFully(this.dataRaw);

      try {
        Inflater decompressor = new Inflater();
        decompressor.setInput(this.dataRaw, 0, this.lengthRaw);
        int decompressed_length = decompressor.inflate(this.data);
        decompressor.end();
        if( decompressed_length != this.length ) {
          throw new DataFormatException("Decompressed length does not match listed length");
        }
      } catch(DataFormatException err) {
        System.out.println("Failed to decompress packet: " + err);
      }

    }

    this.in = new MinecraftInputStream(new ByteArrayInputStream(this.data));
    this.packetId = this.in.readVarInt();
  }

  private MinecraftInputStream getDataFullInputStream() throws IOException {
    return new MinecraftInputStream(new ByteArrayInputStream(this.data, 0, this.length));
  }

  public MinecraftInputStream getDataInputStream() throws IOException {
    MinecraftInputStream temp_stream = this.getDataFullInputStream();
    temp_stream.readVarInt();
    return temp_stream;
  }

  public int getPacketId() { return this.packetId; }
  public int getLength() { return this.length; }

  public void setPacketId(int packetId) {
    if( ! this.readOnly ) {
      this.packetId = packetId;
    }
  }

  public void writeData(MinecraftOutputStream out) throws IOException {
    out.writeVarInt(this.lengthRaw);

    if( this.compressed ) {
      out.writeVarInt(this.length);
    }

    out.write(this.dataRaw);
  }
}
