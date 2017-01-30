package edu.aifb.annotation.graph;

import java.text.DecimalFormat;

import edu.aifb.annotation.model.Sense;

public class SenseVertex extends Vertex {

	private Sense sense;

	public SenseVertex(Sense sense) {
		this.sense = sense;
	}

	public Sense getSense() {
		return sense;
	}

	public String toString() {
		return "[Topic:" + sense.getTitle() + "] : "
				+ new DecimalFormat("#.##").format(weight);
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof SenseVertex))
			return false;
		SenseVertex tv = (SenseVertex) obj;
		return sense.equals(tv.getSense());
	}

	public int hashCode() {
		return sense.hashCode();
	}

}
