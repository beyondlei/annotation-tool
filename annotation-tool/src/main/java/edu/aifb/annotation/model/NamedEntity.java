package edu.aifb.annotation.model;

import java.util.ArrayList;
import java.util.List;

public class NamedEntity implements Comparable<NamedEntity> {
	public enum Type {
		NI, NO, UNKNOW
	}

	private String word;
	private String type;
	private int start;
	private int end;
	private int sentenceNumber;
	private List<String> entityReferences;
	private List<QueryResult> queryResults;
	private List<NamedEntity> coreferencedEntities;
	private Type status;
	private List<Sense> senses;
	private String context;
	private String preContext;
	private String afterContext;
	private int tokenid;

	public int getTokenid() {
		return tokenid;
	}

	public void setTokenid(int tokenid) {
		this.tokenid = tokenid;
	}

	public NamedEntity(String word, String type, int start, int end, int sentNumber) {
		super();
		this.word = word;
		this.type = type;
		this.start = start;
		this.end = end;
		this.sentenceNumber = sentNumber;
		this.queryResults = new ArrayList<>();
		this.entityReferences = new ArrayList<>();
		this.coreferencedEntities = new ArrayList<>();
		this.senses = new ArrayList();
	}

	public NamedEntity(String word, String type, int start, int end) {
		super();
		this.word = word;
		this.type = type;
		this.start = start;
		this.end = end;
		this.queryResults = new ArrayList<>();
		this.entityReferences = new ArrayList<>();
		this.coreferencedEntities = new ArrayList<>();
		this.senses = new ArrayList();
		

	}

	public List<Sense> getSenses() {
		return senses;
	}

	public void setSenses(List<Sense> senses) {
		this.senses = senses;
	}

	public Type getStatus() {
		return status;
	}

	public void setStatus(Type status) {
		this.status = status;
	}

	public List<NamedEntity> getRelateEntities() {
		return coreferencedEntities;
	}

	public void setRelateEntities(List<NamedEntity> relateEntities) {
		this.coreferencedEntities = relateEntities;
	}

	public String getPreContext() {
		return preContext;
	}

	public void setPreContext(String preContext) {
		this.preContext = preContext;
	}

	public String getAfterContext() {
		return afterContext;
	}

	public void setAfterContext(String afterContext) {
		this.afterContext = afterContext;
	}

	public String getContext() {
		return this.context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public List<String> getEntityReferences() {
		return entityReferences;
	}

	public void setEntityReferences(List<String> entityReferences) {
		this.entityReferences = entityReferences;
	}

	public List<QueryResult> getQueryResults() {
		return queryResults;
	}

	public void setQueryResults(List<QueryResult> queryResults) {
		this.queryResults = queryResults;
	}

	public int getSentenceNumber() {
		return sentenceNumber;
	}

	public void setSentenceNumber(int sentenceNumber) {
		this.sentenceNumber = sentenceNumber;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof NamedEntity))
			return false;
		NamedEntity entity = (NamedEntity) obj;
		if (compareTo(entity) == 0)
			return true;
		else
			return false;
	}

	public int hashCode() {
		int hash = 1;
		hash = hash * 17 + start;
		hash = hash * 31 + end;
		return hash;
	}

	public int compareTo(NamedEntity o) {
		// starts first, then goes first
		int c = new Integer(start).compareTo(o.getStart());
		if (c != 0)
			return c;
		// starts at same time, so longest one goes first
		c = new Integer(o.getEnd()).compareTo(end);
		if (c != 0)
			return c;
		return 0;
	}

	public List<NamedEntity> getCoreferencedEntities() {
		return coreferencedEntities;
	}

	public void setCoreferencedEntities(List<NamedEntity> coreferencedEntities) {
		this.coreferencedEntities = coreferencedEntities;
	}

	
	
}
