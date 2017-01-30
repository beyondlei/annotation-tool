package edu.aifb.annotation.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtil {
	
	public static void main(String args[]) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("res/json.txt"));	
		String text = br.readLine();
		
		JSONObject obj = new JSONObject();
		obj.getJSONObject(text);

		System.out.print(obj.toString());
		
	}

}
