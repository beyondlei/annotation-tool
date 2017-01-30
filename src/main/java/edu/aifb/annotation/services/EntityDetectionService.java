package edu.aifb.annotation.services;

import info.debatty.java.stringsimilarity.Levenshtein;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONException;

import edu.aifb.annotation.graph.GraphBasedTopicWeighter;
import edu.aifb.annotation.graph.PageRankTopicWeighter;
import edu.aifb.annotation.model.NamedEntity;
import edu.aifb.annotation.model.QueryResult;
import edu.aifb.annotation.model.Sense;
import edu.aifb.annotation.model.NamedEntity.Type;
import edu.aifb.annotation.mongodb.MongoDBLabel;
import edu.aifb.annotation.mongodb.MongoDBResource;
import edu.aifb.annotation.preprocessing.StanfordNLPProcessor;
import edu.aifb.annotation.util.BingSearchEngine;


public class EntityDetectionService {
	public static double MIN_SIM = 0.5;
	public static int QUERY_LEN = 5;
	public static long ENTITY_PRO = 50;
	private StanfordNLPProcessor preprocessor;
	private BingSearchEngine searchEngine;
	private Levenshtein levenshtein;
	private MongoDBLabel mongoDBLabel;
	private MongoDBResource mongoDBResource;
	private GraphBasedTopicWeighter topicWeighter;
	private Map<String,Double> relatenessCatche;

	
	
	public MongoDBLabel getMongoDBLabel() {
		return mongoDBLabel;
	}
	
	private void setNamedEntityTypeBySense(NamedEntity entity){
		Sense sense=null;
		for(Sense s:entity.getSenses()){
			if(sense==null){
				sense=s;
			}
			else{
				if(s.getWeight()>sense.getWeight())
					sense=s;
			}
				
		}
		entity.setStatus(sense.getType());
		for(NamedEntity e :entity.getRelateEntities()){
			e.setStatus(entity.getStatus());
		}
		
	}

	public EntityDetectionService(String configFile) throws Exception {
		this.searchEngine = new BingSearchEngine();
		this.preprocessor = new StanfordNLPProcessor(configFile);
		levenshtein = new Levenshtein();
		this.mongoDBLabel = new MongoDBLabel();
		this.mongoDBResource = new MongoDBResource();
		this.relatenessCatche=new HashMap();
		  this.topicWeighter=new PageRankTopicWeighter(this);
      
	}

	private void setEntityType(List<NamedEntity> entities) {
		for (NamedEntity entity : entities) {
			if (this.mongoDBLabel.isLabel(entity.getWord())) {
				double sLinks = this.mongoDBLabel.getSenses(entity);
				if (sLinks < ENTITY_PRO)
					entity.setStatus(NamedEntity.Type.UNKNOW);
				else
					// entity.setStatus(NamedEntity.Type.NO);
					entity.setStatus(NamedEntity.Type.NI);

			} else
				entity.setStatus(NamedEntity.Type.NO);
			if(entity.getStatus().equals(NamedEntity.Type.NO)||entity.getStatus().equals(NamedEntity.Type.UNKNOW)){
				Sense s=new Sense();
				s.setType(NamedEntity.Type.NO);
				s.setTitle(entity.getWord());
				s.setEntity(entity);
				entity.getSenses().add(s);
			}
			
		}

	}

	private Map<String, Double> getWordCountInDescript(List<String> desc) {
		Map<String, Double> result = new HashMap();
		for (String word : desc) {
			if (result.get(word) != null)
				result.put(word, result.get(word) + 1 / desc.size());
			else {
				result.put(word, 1.0 / desc.size());
			}
		}
		return result;
	}

	private void setQueryResult4Entities(List<NamedEntity> entities)
			throws ClientProtocolException, IOException, JSONException {
		for (NamedEntity entity : entities) {
			if (!entity.getStatus().equals(NamedEntity.Type.NI)) {
				String queryString = "+(" + entity.getWord() + ")";
				for (String keyword : entity.getEntityReferences()) {
					if (!keyword.equals(entity.getWord()))
						queryString = queryString + " " + keyword;
				}
				entity.setQueryResults(this.searchEngine.search(queryString
						.trim()));
			}
		}
	}

	private List<String> getStringArray(String string) {
		List<String> result = new ArrayList();
		for (String s : string.split(" "))
			result.add(s);

		return result;
	}

	private List<String> getDescriptStrings(NamedEntity entity) {
		List<String> result = new ArrayList();
		int min = entity.getQueryResults().size();
		if (QUERY_LEN < min)
			min = QUERY_LEN;
		for (int i = 0; i < min; i++) {
			result.addAll(getStringArray(entity.getQueryResults().get(i)
					.getDescription()));
		}
		return result;
	}

	// private void computeSim4Entities(NamedEntity entity,String context) {
	// List<String> contextA=getDescriptStrings(entity);
	// List<String> contextB=getStringArray(context);
	// for (NamedEntity entity : entities) {
	// if (entity.getStatus().equals(NamedEntity.Type.UNKNOW)) {
	// List<String> descWords = getDescriptStrings(entity);
	// Map<String, Double> wordCountMap = getWordCountInDescript(descWords);
	// double sim = 0.0;
	// for (String entityReference : entity.getEntityReferences()) {
	// double tempSim = 0.0;
	// for (Entry<String, Double> wordMap : wordCountMap
	// .entrySet()) {
	// tempSim = tempSim
	// + wordMap.getValue()
	// * this.mongoDBResource.getPMI(entityReference,
	// entity.getWord());
	// System.out.println("tempSim:" + tempSim);
	// }
	// if (tempSim > sim)
	// sim = tempSim;
	// }
	// if (sim < MIN_SIM)
	// entity.setStatus(NamedEntity.Type.NO);
	// }
	//
	// }
	//
	// // for (NamedEntity entity : entities) {
	// // double tempSim = 0;
	// // if (entity.getQueryResults().size() > 0) {
	// // for (int i = 0; i < QUERY_LEN; i++) {
	// // QueryResult queryResult = entity.getQueryResults().get(i);
	// // for (Sense sense : entity.getLabel().getSenses()) {
	// // double sim = this.levenshtein.similarity(
	// // sense.getFirstParagraphMarkup(),
	// // queryResult.getDescription());
	// // if (sim > tempSim)
	// // tempSim = sim;
	// // }
	// // }
	// // if (tempSim > MIN_SIM) {
	// // entity.setStatus(NamedEntity.Type.NO);
	// // }
	// // }
	// // }
	// }

	public double computeSR(Sense a, Sense b) throws ClientProtocolException, IOException, JSONException{
		double result =0;
		if(a.getType().equals(Type.NI)&&b.getType().equals(Type.NI))
			result=computeSR4O2O(a,b);
		if(a.getType().equals(Type.NO)&&b.getType().equals(Type.NI))
			result=computeSR4N2O(a,b);
		if(a.getType().equals(Type.NI)&&b.getType().equals(Type.NO))
			result=computeSR4N2O(b,a);
		if(a.getType().equals(Type.NO)&&b.getType().equals(Type.NO))
			result=computeSR4N2N(a,b);
		
		return result;
	}
	
	
	private double getHits(Sense a,Sense b) throws ClientProtocolException, IOException, JSONException{
		Double result=this.relatenessCatche.get(a.getTitle()+b.getTitle());
		if(result==null){
			result=Double.valueOf(this.searchEngine.getBingSR(a.getTitle(), b.getTitle()));
			this.relatenessCatche.put(a.getTitle()+b.getTitle(), result.doubleValue());
		}
		return result.doubleValue();
	}
	
	public double computeSR4N2O(Sense a,Sense b) throws ClientProtocolException, IOException, JSONException{
		double result =0;
		double hits=this.getHits(a ,b);
		result=hits;
		if(a.getEntity().getSenses().size()>1)
		{
			double wiki=0;
			for(Sense s1:a.getEntity().getSenses()){
				for(Sense s2:b.getEntity().getSenses()){
					if(s1.getType().equals(NamedEntity.Type.NI)&&s2.getType().equals(NamedEntity.Type.NI))
						wiki=wiki+this.mongoDBLabel.getRelateness(s1.getTitle(), s2.getTitle());
				}
			}
			result=result-wiki;
		}
		return result;
		
	}
	
	
	 public double computeSR4N2N(Sense a,Sense b) throws ClientProtocolException, IOException, JSONException{
		double result =this.getHits(a, b);
		
		return result;
		
	}
	
	public double computeSR4O2O(Sense a,Sense b){
		double result =0;
		result=this.mongoDBLabel.getRelateness(a.getTitle(), b.getTitle());
		return result;
		
	}
	
	
	public double computeSim4Entities(NamedEntity entity, String context) {
		List<String> contextA = getDescriptStrings(entity);
		List<String> contextB = getStringArray(context);
		Map<String, Double> wordCountMapA = getWordCountInDescript(contextA);
		Map<String, Double> wordCountMapB = getWordCountInDescript(contextB);
		double sim = 0.0;

		for (Entry<String, Double> wordMap : wordCountMapA.entrySet()) {
			if (wordCountMapB.get(wordMap.getKey()) != null)
				sim = sim + wordMap.getValue()
						* wordCountMapB.get(wordMap.getKey());

		}

		return sim;

	}

	public List<NamedEntity> getOutOfKBEntities(List<NamedEntity> entities) {
		List<NamedEntity> result = new ArrayList<NamedEntity>();
		for (NamedEntity entity : entities) {
			if (entity.getStatus().equals(NamedEntity.Type.NO))
				result.add(entity);
		}
		return result;
		// return entities;
	}

	public List<NamedEntity> detectEntities(String input) throws Exception {

		this.preprocessor.preprocessing(input);
		

		List<NamedEntity> results = preprocessor.getEntities();
		this.setEntityType(results);
		this.setQueryResult4Entities(results);
		//this.computeSim4Entities(results);
		List<Sense> senses=new ArrayList();
		for(NamedEntity entity:results){
			senses.addAll(entity.getSenses());
		}
		int i=0;
		for(Sense sense:senses){
			if(sense.getType().equals(NamedEntity.Type.NI))
				sense.setContext(this.mongoDBLabel.getContextFromTitle(sense.getTitle()));
			else{
				StringBuilder sb = new StringBuilder();
				for(QueryResult result:sense.getEntity().getQueryResults()){
					sb.append(result.getDescription());
				}
				sense.setContext(sb.toString());
				
			}
			
			sense.setIndex(i);
			i++;
		}
		System.out.println("Topic's count is"+i);
		this.topicWeighter.getWeightedTopics(senses, null);
		for(NamedEntity entity:results){
			this.setNamedEntityTypeBySense(entity);
		}
		return results;
		// return this.getOutOfKBEntities(results);

	}

	/**
	 * write results to a given xml in long version, which includes urls,
	 * relatedmentions, annotatedtext and position.
	 * 
	 * @param entities
	 *            results
	 * @param filename
	 *            given xml filename
	 * @param originText
	 *            origin text
	 * @throws IOException
	 *             possible IO error
	 */
	public void write2XMLLongVersion(List<NamedEntity> entities,
			String filename, String originText) throws IOException {

		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("result");
		Element annotatedText = root.addElement("annotatedtext");
		annotatedText.setText(getAnnotatedText(entities, originText));
		Element mentions = root.addElement("mentions");
		for (NamedEntity entity : entities) {
			Element mention = mentions.addElement("mention");
			Element mentiontext = mention.addElement("mentiontext");
			Element position = mention.addElement("position");
			position.setText(entity.getStart() + "," + entity.getEnd());
			mentiontext.setText(entity.getWord());
			Element relatedMentions = mention.addElement("relatedmentions");
			for (NamedEntity relatedEntity : entity.getRelateEntities()) {
				Element relatedMention = relatedMentions
						.addElement("relatedmention");
				Element relatedMentiontext = relatedMention
						.addElement("mentiontext");
				relatedMentiontext.setText(relatedEntity.getWord());
				Element relatedMentionPosition = relatedMention
						.addElement("position");
				relatedMentionPosition.setText(relatedEntity.getStart() + ","
						+ relatedEntity.getEnd());

			}
			Element urls = mention.addElement("urls");
			for (int i = 0; i < QUERY_LEN; i++) {
				Element url = urls.addElement("url");
				url.setText(entity.getQueryResults().get(i).getUrl());
			}

		}
		OutputFormat format = OutputFormat.createPrettyPrint();
		if (!filename.equals("")) {
			Writer fileWriter = new FileWriter(filename);
			XMLWriter xmlWriter = new XMLWriter(fileWriter, format);
			xmlWriter.write(document);
			xmlWriter.flush();
			xmlWriter.close();
		}
		printXMLString(document, format);

	}

	private void printXMLString(Document document, OutputFormat format)
			throws IOException {
		StringWriter writer = new StringWriter();
		XMLWriter xmlWriter = new XMLWriter(writer, format);
		xmlWriter.write(document);
		xmlWriter.flush();
		xmlWriter.close();
		System.out.println(writer.toString());
	}

	/**
	 * Write results to a given XML filename in short version
	 * 
	 * @param entities
	 *            results
	 * @param filename
	 *            given xml file name
	 * @throws IOException
	 *             possible IO error
	 */
	public void write2XMLShortVersion(List<NamedEntity> entities,
			String filename) throws IOException {

		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("item");
		for (NamedEntity entity : entities) {
			Element mention = root.addElement("mention");
			Element mentiontext = mention.addElement("mentiontext");
			Element mentiontype = mention.addElement("mentiontype");
			mentiontext.setText(entity.getWord());
			mentiontype.setText(entity.getStatus().toString());
			for (NamedEntity relateEntity : entity.getRelateEntities()) {
				Element relateMention = root.addElement("mention");
				Element relateMentiontext = relateMention
						.addElement("mentiontext");
				relateMentiontext.setText(relateEntity.getWord());
				Element relateMentionType = relateMention
						.addElement("mentiontype");
				relateMentionType.setText(entity.getStatus().toString());
			}

		}
		OutputFormat format = OutputFormat.createPrettyPrint();
		Writer fileWriter = new FileWriter(filename);
		XMLWriter xmlWriter = new XMLWriter(fileWriter, format);
		xmlWriter.write(document);
		xmlWriter.flush();
		xmlWriter.close();

	}

	public String getAnnotatedText(List<NamedEntity> entities, String text) {
		Set<NamedEntity> entitiesSet = new TreeSet();
		for (NamedEntity entity : entities) {
			entitiesSet.add(entity);
			for (NamedEntity relateEntity : entity.getRelateEntities())
				entitiesSet.add(relateEntity);
		}
		int start = 0;
		StringBuilder sb = new StringBuilder();
		for (NamedEntity entity : entitiesSet) {
			sb.append(text.substring(start, entity.getStart()));
			sb.append("[" + entity.getWord() + "]");
			start = entity.getEnd();
		}
		sb.append(text.substring(start));
		return sb.toString();

	}

	public static void main(String args[]) throws Exception {
		EntityDetectionService service = new EntityDetectionService(
				"config/NLPConfig.properties");
		File input = new File("res/input");
		String output = "res/output/";
		for (File file : input.listFiles()) {

			if (!file.isDirectory() && file.getName().contains("txt")) {

				InputStream is = new FileInputStream(file.getAbsolutePath());
				// InputStream is = new FileInputStream(
				// "/Users/yunpeng/Desktop/material/APW_ENG_20101001.0006.txt");
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "utf-8"));

				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
					sb.append(line + "\n");

				}

				List<NamedEntity> entities = service.detectEntities(sb
						.toString());
				String filename = output + "/" + file.getName().replace("txt", "xml");
				service.write2XMLShortVersion(entities, filename);
				//
				// }
				//
				// }
				// System.out.println(service.getAnnotatedText(entities,
				// sb.toString()));
				// service.write2XMLLongVersion(entities, "", sb.toString());
				// for (NamedEntity entity : entities) {
				// System.out.println(entity.getWord() + ":" +
				// entity.getType());
				// for (String coreReference : entity.getEntityReferences()) {
				// System.out.println("=========" + coreReference);
				// }
				//
				// for (NamedEntity coreReference : entity.getRelateEntities())
				// {
				// System.out.println("-------=======" +
				// coreReference.getWord());
			}
		}

	}

}
