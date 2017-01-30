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

import edu.aifb.annotation.model.NamedEntity;
import edu.aifb.annotation.model.Sense;

public class MongoDBLabel {
	private Mongo mg;
	private DB db;
	private List<DBCollection> directories;

	@SuppressWarnings("deprecation")
	public MongoDBLabel() throws UnknownHostException{
		this.mg = new Mongo("aifb-ls3-maia.aifb.kit.edu",19005);
		this.db = this.mg.getDB("ABIRS");
		this.directories=new ArrayList();
		//directories[0] labelindex;
		//directories[1] labelentityindex;
		this.directories.add(this.db.getCollection("LabelIndex_EN_LC"));
		this.directories.add(this.db.getCollection("LabelEntityIndex_EN_LC"));
		this.directories.add(this.db.getCollection("ResourceRelatednessIndex_EN"));
		this.directories.add(this.db.getCollection("EntityDetails_EN"));
		
		
	}
	
	public String getContextFromTitle(String title){
		String result="";
		BasicDBObject condition = new BasicDBObject();
		condition.append("title", title);
		DBCursor cursor = this.directories.get(3).find(condition);
		if (cursor.hasNext()) {
			DBObject obj = cursor.next();
			result = (String) obj.get("context");
		}
		return result;
	}
	
	public List<String> getLinksOut(String title){
		List<String>  result=new ArrayList();
		BasicDBObject condition = new BasicDBObject();
		condition.append("title", title);
		DBCursor cursor = this.directories.get(3).find(condition);
		if (cursor.hasNext()) {
			DBObject obj = cursor.next();
			result = (List<String>) obj.get("linksOut");
		}
		return result;
	}
	
	public List<String> getLinksIn(String title){
		List<String>  result=new ArrayList();
		BasicDBObject condition = new BasicDBObject();
		condition.append("title", title);
		DBCursor cursor = this.directories.get(3).find(condition);
		if (cursor.hasNext()) {
			DBObject obj = cursor.next();
			result = (List<String>) obj.get("linksIn");
		}
		return result;
	}
	
	public double getRelateness(String entityA, String entityB) {
		double result = 0;
		BasicDBObject condition = new BasicDBObject();
		BasicDBObject sortCondition = new BasicDBObject();
		sortCondition.append("score", -1);
		condition.append("s_title", entityA);
		condition.append("t_title", entityB);
		DBCursor cursor = this.directories.get(2).find(condition)
				.sort(sortCondition).limit(1);
		if (cursor.hasNext()) {
			DBObject obj = cursor.next();
			result = (double) obj.get("score");
		} else {
			condition.clear();
			condition.append("s_title", entityB);
			condition.append("t_title", entityA);
			cursor = this.directories.get(2).find(condition)
					.sort(sortCondition).limit(1);
			if (cursor.hasNext()) {
				DBObject obj = cursor.next();
				result = (double) obj.get("score");
			}
		}
		return result;

	}
	
	public boolean isLabel(String labelText){
		BasicDBObject condition = new BasicDBObject();
		condition.append("label", labelText);
		DBCursor cursor = this.directories.get(0).find(condition).limit(1);
		return cursor.hasNext();
	}
	
	public long getSenses(NamedEntity entity){
		long links=0;
		List<Sense> senses = new ArrayList();
		BasicDBObject condition = new BasicDBObject();
		condition.append("label", entity.getWord());
		BasicDBObject sortCondition = new BasicDBObject();
		sortCondition.append("associationStrength", -1);
		DBCursor cursor=this.directories.get(1).find(condition).limit(5).sort(sortCondition);
		if(cursor.hasNext()){
			DBObject obj=cursor.next();
			if((long)obj.get("slinkOccCount")>links)
				links=(long)obj.get("slinkOccCount");
			Sense s= new Sense();
			s.setTitle((String)obj.get("entity"));
			s.setsLinkCount((long)obj.get("slinkOccCount"));
			s.setType(NamedEntity.Type.NI);
			s.setEntity(entity);
			senses.add(s);
		}
		entity.setSenses(senses);
		return links;
		
	}
	public String getContext(String title){
		String result="dummy";
		return result;
	}

	public static void main(String args[]) throws UnknownHostException{
		MongoDBLabel ml=new MongoDBLabel();
//		System.out.print(ml.isLabel("Richard Stephens"));
//		System.out.println(ml.getSenses("Richard Stephens"));
		System.out.println(ml.getRelateness("Tyrone Power", "Irish people"));
	}
}


