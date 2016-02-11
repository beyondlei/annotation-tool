package edu.aifb.annotation.model;

public class QueryResult {
	private String id;
	private String title;
	private String description;
	private String displayUrl;
	private String url;
	
	public QueryResult(){
		super();
	}
	
	public QueryResult(String id, String title, String description,
			String displayUrl, String url) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.displayUrl = displayUrl;
		this.url = url;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDisplayUrl() {
		return displayUrl;
	}
	public void setDisplayUrl(String displayUrl) {
		this.displayUrl = displayUrl;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	

}
