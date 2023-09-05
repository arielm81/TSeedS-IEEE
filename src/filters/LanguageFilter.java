package filters;

import twitter4j.Status;

public class LanguageFilter extends FiltroAbs {
  String language;
  
  public LanguageFilter(String lang) {
    language = lang;
  }
  
  public boolean cumple(Status interaction)
  {
    if (interaction.getLang().equals(language))
      return true;
    return false;
  }
}
