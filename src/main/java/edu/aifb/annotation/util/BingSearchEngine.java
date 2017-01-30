package edu.aifb.annotation.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.aifb.annotation.model.QueryResult;


@SuppressWarnings("deprecation")
public class BingSearchEngine {
	private final static String key=":BEFx2ZgWjwTLNCLeP/U82fSgMiAzo2a4uyT02AgE1zE";
	//private final static String key=":brAUUsDeG7obhUxwaKX4EUjfioO1gMWi/Xf/egzpq+U";
	private final static double TOTAL_WEB_INDEXES=1000000000.0;
	
	public String bingSearch(String keyword) throws ClientProtocolException, IOException{
		String response=null;
		HttpClient httpclient = new DefaultHttpClient();

		try {
			String encodeKey=new String(Base64.encodeBase64(key.getBytes()));
			HttpGet httpget = new HttpGet("https://api.datamarket.azure.com/Bing/SearchWeb/v1/Composite?Query=%27"+URLEncoder.encode(keyword)+"%27&$format=json&$top=200");
			httpget.setHeader(
					"Authorization",
					"Basic "+encodeKey);
			httpget.setHeader("Accept-Language","en-us,en;q=0.5");
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = httpclient.execute(httpget, responseHandler);
			} finally {
				// When HttpClient instance is no longer needed,
				// shut down the connection manager to ensure
				// immediate deallocation of all system resources
				httpclient.getConnectionManager().shutdown();
			}
		
		return response;
		
	}
	
	
	public double getBingSR(String entity1,String entity2) throws ClientProtocolException, IOException, JSONException{
		double result=0;
		long mentionHits=getHits(entity1,"");
		long entityHits=getHits(entity2,"");
		result=Math.max(mentionHits,entityHits)-getHits(entity1,entity2);
		result=1-Math.log(result)/Math.log((TOTAL_WEB_INDEXES-Math.min(mentionHits,entityHits)));
		return result;
	}
	
	public long getHits(String word1,String word2) throws ClientProtocolException, IOException, JSONException{
		String keyword="\""+word1+" "+word2+"\"";
		if(word2.equals(""))
			keyword=word1;
		long result=0;
		String responseBody =bingSearch(keyword);
		//System.out.println(responseBody);
		JSONObject root= new JSONObject(responseBody);
		JSONObject d=(JSONObject)root.get("d");
		JSONArray resultscomp =(JSONArray)d.get("results");
		JSONObject obj=(JSONObject) resultscomp.get(0);
	    result=Long.valueOf(obj.getString("WebTotal"));
		return result;
		
	}
	
	public List<QueryResult> search(String keyword) throws ClientProtocolException, IOException, JSONException {
		List<QueryResult> results=new ArrayList<>();
//		HttpClient httpclient = new DefaultHttpClient();
//		try {
//			String encodeKey=new String(Base64.encodeBase64(key.getBytes()));
//			HttpGet httpget = new HttpGet("https://api.datamarket.azure.com/Bing/SearchWeb/v1/Composite?Query=%27"+URLEncoder.encode(keyword)+"%27&$format=json&$top=200");
//			httpget.setHeader(
//					"Authorization",
//					"Basic "+encodeKey);
//			httpget.setHeader("Accept-Language","en-us,en;q=0.5");
//			ResponseHandler<String> responseHandler = new BasicResponseHandler();		
//			String responseBody = httpclient.execute(httpget, responseHandler);
		    String responseBody =bingSearch(keyword);
			System.out.println(responseBody);
			JSONObject root= new JSONObject(responseBody);
			JSONObject d=(JSONObject)root.get("d");
			JSONArray resultscomp =(JSONArray)d.get("results");
			JSONObject obj=(JSONObject) resultscomp.get(0);
		    System.err.println(obj.getString("WebTotal"));
			JSONArray items=obj.getJSONArray("Web");
			for(int i=0;i<items.length();i++){
				JSONObject item=(JSONObject) items.get(i);
				String id=item.getString("ID");
				String title=item.getString("Title");
				String description=item.getString("Description");
				String displayUrl=item.getString("DisplayUrl");
				String url=item.getString("Url");
				QueryResult result=new QueryResult(id,title,description,displayUrl,url);
				results.add(result);
			}

//		} finally {
//			// When HttpClient instance is no longer needed,
//			// shut down the connection manager to ensure
//			// immediate deallocation of all system resources
//			httpclient.getConnectionManager().shutdown();
//		}
		return results;

	}
	public static void main(String args[]) throws ClientProtocolException, IOException, JSONException{
		BingSearchEngine se = new BingSearchEngine();
		System.out.println(se.getBingSR("Michael Jordan", "123A"));
//		int i=0;
//		for(QueryResult re:se.search("Brian Lucey")){
//			i++;
//			System.out.println(re.getTitle());
//			System.out.println(re.getDescription());
//			System.out.println(re.getUrl());
//			
//		}
//		System.out.println(i);
	}

}
