package seed;

import java.util.Date;

public class SeedInfo {
  private String marginalInfluence;
  private Integer pos_in_seed;
  private Date time;
  
  public SeedInfo(String mi, Integer pos, Date t) {
    marginalInfluence = mi;
    pos_in_seed = pos;
    time = t;
  }
  
  public String getMarginalInfluence() {
    return marginalInfluence;
  }
  
  public void setMarginalInfluence(String marginalInfluence) {
    this.marginalInfluence = marginalInfluence;
  }
  
  public Integer getPosInSeed() {
    return pos_in_seed;
  }
  
  public void setPosInSeed(Integer pos_in_seed) {
    this.pos_in_seed = pos_in_seed;
  }
  
  public Date getTime() {
    return time;
  }
  
  public void setTime(Date time) {
    this.time = time;
  }
}
