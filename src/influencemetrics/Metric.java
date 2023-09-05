package influencemetrics;

public class Metric {
	private Double mi;
	private Double cl;
	private Double ri;
	private Double snp;
	
	public Metric(Double mi, Double cl, Double ri, Double snp){
		this.mi = mi;
		this.cl = cl;
		this.ri = ri;
		this.snp = snp;
	}

	public Double getMi() {
		return mi;
	}

	public void setMi(Double mi) {
		this.mi = mi;
	}

	public Double getCl() {
		return cl;
	}

	public void setCl(Double cl) {
		this.cl = cl;
	}

	public Double getRi() {
		return ri;
	}

	public void setRi(Double ri) {
		this.ri = ri;
	}

	public Double getSnp() {
		return snp;
	}

	public void setSnp(Double snp) {
		this.snp = snp;
	}
}
