package edu.aifb.annotation.mongodb;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoDBResource {
	private Mongo mg;
	private DB db;
	private List<DBCollection> directories;

	public MongoDBResource() throws UnknownHostException {
		this.mg = new Mongo("aifb-ls3-calc.aifb.uni-karlsruhe.de", 19000);
		this.db = this.mg.getDB("lexica");
		this.directories = new ArrayList();
		this.directories.add(this.db
				.getCollection("ResourceWordCoOccurrence-3_en"));

	}

	public double getPMI(String entity, String word) {
		BasicDBObject condition = new BasicDBObject();
		condition.append("resource", entity);
		condition.append("word", word);
		DBObject obj = this.directories.get(0).findOne(condition);
		if (obj == null)
			return 0;
		else
			return (double) obj.get("PMI(w,r)");

	}



	public static void main(String args[]) throws UnknownHostException {
		MongoDBResource mr = new MongoDBResource();

	}
}
