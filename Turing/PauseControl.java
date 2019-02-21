package Turing;

class PauseControl
{
  private boolean needToPause;

  synchronized void pausePoint() throws InterruptedException
  {
    while(needToPause)
      wait();
  }

  synchronized void pause()
  {
    needToPause = true;
  }

  synchronized void unPause()
  {
    needToPause = false;
    this.notifyAll();
  }
}
