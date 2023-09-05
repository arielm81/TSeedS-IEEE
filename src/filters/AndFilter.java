package filters;

import twitter4j.Status;

public class AndFilter extends FiltroAbs {
  FiltroAbs f1;
  FiltroAbs f2;
  
  public AndFilter(FiltroAbs f1, FiltroAbs f2) {
    this.f1 = f1;
    this.f2 = f2;
  }
  
  public boolean cumple(Status interaction)
  {
    if ((f1.cumple(interaction)) && (f2.cumple(interaction)))
      return true;
    return false;
  }
}
