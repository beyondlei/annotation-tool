package edu.aifb.annotation.model;

public class Sense {

	// since the same phrase may occur more than once, but will always be
	// disambiguated the same way, one topic will be generated for each phrase
	// and identified by the index
	private int index;
    private String context;
	private String title;
	private NamedEntity entity;
	private NamedEntity.Type type;
	private long sLinkCount;
	
	private double weight;

	
	
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public NamedEntity getEntity() {
		return entity;
	}

	public void setEntity(NamedEntity entity) {
		this.entity = entity;
	}

	public long getsLinkCount() {
		return sLinkCount;
	}

	public void setsLinkCount(long sLinkCount) {
		this.sLinkCount = sLinkCount;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public double getWeight() {
		return weight;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public NamedEntity.Type getType() {
		return type;
	}

	public void setType(NamedEntity.Type type) {
		this.type = type;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Sense))
			return false;
		Sense tr = (Sense) obj;
		if (!(title.equals(tr.title)))
			return false;
		else
			return entity.equals(tr.entity);
	}

	public int hashCode() {
		int hash = 1;
		hash = hash * 17 + entity.hashCode();
		hash = hash * 13 + title.hashCode();
		return hash;
	}

	public String[] getLinksIn() {
		// TODO Auto-generated method stub
		return null;
	}

}
