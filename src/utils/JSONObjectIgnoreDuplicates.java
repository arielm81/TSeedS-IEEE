package utils;

import twitter4j.JSONObject;

public class JSONObjectIgnoreDuplicates extends JSONObject
{
  public JSONObjectIgnoreDuplicates(String json) throws twitter4j.JSONException {
    super(json);
  }
  
  public JSONObject putOnce(String key, Object value) throws twitter4j.JSONException
  {
    if ((key != null) && (value != null)) { Object storedValue;
      if ((storedValue = opt(key)) != null) {
        if (!storedValue.equals(value)) {
          throw new twitter4j.JSONException("Duplicate key \"" + key + "\"");
        }
        return this;
      }
      put(key, value);
    }
    return this;
  }
}
