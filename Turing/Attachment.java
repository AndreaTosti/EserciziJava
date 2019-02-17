package Turing;

import java.nio.ByteBuffer;
import java.util.LinkedList;

class Attachment
{
  private int remainingBytes;
  private ByteBuffer buffer;
  private Step step;
  private int totalSize;
  private String[] parameters;
  private LinkedList<Sezione> sections;

  Attachment(int remainingBytes, ByteBuffer buffer, Step step,
             int totalSize, String[] parameters,
             LinkedList<Sezione> sections)
  {
    this.remainingBytes = remainingBytes;
    this.buffer = buffer;
    this.step = step;
    this.totalSize = totalSize;
    this.parameters = parameters;
    this.sections = sections;
  }

  int getRemainingBytes()
  {
    return remainingBytes;
  }

  void setRemainingBytes(int remainingBytes)
  {
    this.remainingBytes = remainingBytes;
  }

  ByteBuffer getBuffer()
  {
    return buffer;
  }

  void setBuffer(ByteBuffer buffer)
  {
    this.buffer = buffer;
  }

  Step getStep()
  {
    return step;
  }

  void setStep(Step step)
  {
    this.step = step;
  }

  int getTotalSize()
  {
    return totalSize;
  }

  void setTotalSize(int totalSize)
  {
    this.totalSize = totalSize;
  }

  String[] getParameters()
  {
    return parameters;
  }

  void setParameters(String[] parameters)
  {
    this.parameters = parameters;
  }

  LinkedList<Sezione> getSections()
  {
    return sections;
  }

  void setSections(LinkedList<Sezione> sections)
  {
    this.sections = sections;
  }
}
