package Turing;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

class EditingRoom
{
  private boolean isEditing;
  private String multicastAddress;
  private MulticastSocket multicastSocket;
  private int soTimeout = 0; //Tempo illimitato

  EditingRoom(boolean isEditing,
              String multicastAddress,
              int port)
  {
    this.isEditing = isEditing;
    this.multicastAddress = multicastAddress;
    try
    {
      this.multicastSocket = new MulticastSocket(port);
      this.multicastSocket.setSoTimeout(soTimeout);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }

  InetAddress getInetMulticastAddress()
  {
    try
    {
      return InetAddress.getByName(multicastAddress);
    }
    catch(UnknownHostException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  void joinGroup()
  {
    try
    {
      multicastSocket.joinGroup(getInetMulticastAddress());
    }catch(IOException e)
    {
      e.printStackTrace();
    }
  }

  void leaveGroup()
  {
    if(multicastSocket == null)
      return;

    try
    {
      multicastSocket.leaveGroup(InetAddress.getByName(multicastAddress));
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }

  boolean isEditing()
  {
    return isEditing;
  }

  void setEditing(boolean editing)
  {
    isEditing = editing;
  }

  MulticastSocket getMulticastSocket()
  {
    return multicastSocket;
  }

  void setMulticastAddress(String multicastAddress)
  {
    this.multicastAddress = multicastAddress;
  }

  String getMulticastAddress()
  {
    return multicastAddress;
  }
}
