package filters;

import twitter4j.Status;

public abstract class FiltroAbs
{
  public FiltroAbs() {}
  
  public abstract boolean cumple(Status paramStatus);
}
