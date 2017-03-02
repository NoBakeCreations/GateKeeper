package com.galacticcookiejar.nobakecreation.minecraft.net;

import java.io.IOException;

public class HandshakePacket extends MinecraftPacket {

  public int protocolVersion;
  public String serverAddress;
  public int serverPort;
  public State nextState;

  public HandshakePacket(MinecraftInputStream in) throws IOException {
    super(in);

    this.protocolVersion = this.in.readVarInt();
    int serverStringLength = this.in.readVarInt();
    this.serverAddress = this.in.readUTF();
    if( this.serverAddress.length() != serverStringLength ) {
      throw new IOException("Malformed string read, size does not match listed size");
    }
    this.serverPort = this.in.readUnsignedShort();
    int readNextState = this.in.readVarInt();
    if( readNextState == 1 ) {
      this.nextState = MinecraftPacket.State.STATUS;
    } else if( readNextState == 2 ) {
      this.nextState = MinecraftPacket.State.LOGIN;
    } else {
      throw new IOException("Next state specified is unrecognized");
    }
  }
}
