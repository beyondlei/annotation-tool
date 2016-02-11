package edu.aifb.annotation.graph;

import java.text.DecimalFormat;

import edu.aifb.annotation.model.NamedEntity;

public class NamedEntityVertex extends Vertex {

	private NamedEntity namedEntity;

	public NamedEntityVertex(NamedEntity namedEntity) {
		this.namedEntity = namedEntity;
	}

	public NamedEntity getNamedEntity() {
		return namedEntity;
	}

	public String toString() {
		return "[Label:" + namedEntity.getWord() + "] : " 
				+ new DecimalFormat("#.##").format(weight);
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof NamedEntityVertex))
			return false;
		NamedEntityVertex trv = (NamedEntityVertex) obj;
		return namedEntity.equals(trv.getNamedEntity());
	}

	public int hashCode() {
		return namedEntity.hashCode();
	}

}
