package edu.aifb.annotation.preprocessing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.aifb.annotation.model.NamedEntity;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.Dictionaries.MentionType;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordNLPProcessor {
	// public Map<String, List<String>> entityCoreferences;
	private StanfordCoreNLP pipeline;
	private List<NamedEntity> entities;
	private final static int WINDOWSSIZE = 10;
	private final static String[] ENTITY_TYPES = { "PERSON", "MISC", "ORGANIZATION", "LOCATION" };

	public StanfordNLPProcessor(String configFile) throws IOException {
		super();
		FileInputStream inputFile = new FileInputStream(configFile);
		Properties props = new Properties();
		props.load(inputFile);
		pipeline = new StanfordCoreNLP(props);
		this.entities = new ArrayList<>();

	}

	public List<NamedEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<NamedEntity> entities) {
		this.entities = entities;
	}

	private boolean isEntity(String type) {
		for (String typ : ENTITY_TYPES) {
			if (typ.equals(type))
				return true;
		}
		return false;
	}

	public void preprocessing(String text) {
		this.entities.clear();
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<NamedEntity> tempList = new ArrayList<NamedEntity>();

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		int i = 1;
		int tokenid = 0;
		Map<Integer, String> tokenMap = new HashMap();
		for (CoreMap sentence : sentences) {

			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
			// for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			for (int j = 0; j < tokens.size(); j++) {
				CoreLabel token = tokens.get(j);
				String word = token.get(TextAnnotation.class);
				String ne = token.get(NamedEntityTagAnnotation.class);
				tokenMap.put(tokenid, word);

				if (isEntity(ne)) {

					NamedEntity entity = new NamedEntity(word, ne, token.beginPosition(), token.endPosition());
					entity.setSentenceNumber(i);
					tempList.add(entity);
					entity.setTokenid(tokenid);

				}
				tokenid++;

			}
		}
		
		NamedEntity preEntity = null;
		i++;
		int start = 0;
		int preTokenid = 0;
		for (NamedEntity entity : tempList) {
			// System.out.println(entity.getWord()+":"+entity.getStart()+":"+entity.getEnd()+":"+entity.getType());
			if (preEntity == null) {
				start = entity.getStart();
				preTokenid = entity.getTokenid();
			}
			boolean typeIsEqual = false;
			boolean isConnect = false;
			if (preEntity != null) {
				typeIsEqual = entity.getType().equals(preEntity.getType());
				isConnect = (entity.getStart() - preEntity.getEnd() <= 1);
			}
			if (preEntity != null && (!typeIsEqual || (typeIsEqual && !isConnect))) {
				NamedEntity tempEntity = new NamedEntity(text.substring(start, preEntity.getEnd()), preEntity.getType(),
						start, preEntity.getEnd());
				tempEntity.setSentenceNumber(preEntity.getSentenceNumber());
				tempEntity.setTokenid(preTokenid);
				this.setContext(tempEntity, tokenMap);
				this.entities.add(tempEntity);
				preTokenid = entity.getTokenid();
				start = entity.getStart();
			}

			preEntity = entity;
		}

		if (preEntity != null) {
			NamedEntity entity = new NamedEntity(text.substring(start, preEntity.getEnd()), preEntity.getType(), start,
					preEntity.getEnd(), preEntity.getSentenceNumber());
			this.entities.add(entity);
			entity.setTokenid(preTokenid);
			this.setContext(entity, tokenMap);
		}
		tempList.clear();

		Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
		for (CorefChain c : graph.values()) {
			// System.out.println(c.getRepresentativeMention());
			NamedEntity entity = isInEntities(c.getRepresentativeMention());
			if (entity != null) {
				List<String> coreferences = new ArrayList<String>();
				List<NamedEntity> relatedEntities = new ArrayList<>();
				for (CorefMention men : c.getMentionsInTextualOrder()) {

					// System.out.println(men.mentionType + "::::::"
					// + men.mentionSpan);
					if (men.mentionType != MentionType.PRONOMINAL) {
						coreferences.add(men.mentionSpan);
						if (!men.mentionSpan.equals(entity.getWord())) {
							NamedEntity ent = isInEntities(men);
							if (ent != null) {
								relatedEntities.add(ent);
								this.entities.remove(ent);
							}
						}

					}

				}
				entity.setRelateEntities(relatedEntities);
				entity.setEntityReferences(coreferences);
			}
		}
		this.filterEntities();
	}

	private void setContext(NamedEntity entity, Map<Integer, String> map) {
		int start = entity.getTokenid() - WINDOWSSIZE;
		int end = entity.getWord().split(" ").length + start + WINDOWSSIZE;
		// System.out.print(map.size());
		if (start < 0)
			start = 0;
		if (end > map.size())
			end = map.size();

		StringBuilder sb = new StringBuilder();
		for (int i = start; i < entity.getTokenid(); i++) {
			sb.append(map.get(i));
			sb.append(" ");
		}
		for (int i = entity.getWord().split(" ").length + start; i < end; i++) {
			sb.append(map.get(i));
			sb.append(" ");
		}

		entity.setContext(sb.toString().trim());

	}

	private boolean isInEntities(String entity) {
		for (NamedEntity ent : entities) {
			if (ent.getWord().equals(entity))
				return true;
		}
		return false;
	}

	private void filterEntities() {
		for (int i = 0; i < this.entities.size(); i++) {
			NamedEntity entity = this.entities.get(i);
			for (int j = 0; j < i; j++) {
				NamedEntity pre = this.entities.get(j);
				if (pre.getWord().contains(entity.getWord())) {
					pre.getRelateEntities().add(entity);
					pre.getRelateEntities().addAll(entity.getRelateEntities());
					this.entities.remove(i);
					i--;
					break;
				}
			}
		}
	}

	private NamedEntity isInEntities(CorefMention corefMention) {
		for (NamedEntity ent : entities) {
			if (ent.getWord().equals(corefMention.mentionSpan) && ent.getSentenceNumber() == corefMention.sentNum) {
				// System.out.println(corefMention.mentionSpan+":"+ent.getSentenceNumber()+":"+corefMention.sentNum);
				// System.out.println(corefMention.position.elems());
				// System.out.println(ent.getWord()+"-"+ent.getEnd()+":"+corefMention.endIndex+"--"+ent.getStart()+":"+corefMention.startIndex);
				return ent;
			}
		}
		return null;
	}

	public static void main(String args[]) throws IOException {
//		InputStream is = new FileInputStream("res/input/APW_ENG_20101001.0016.txt");
//		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
//
//		StringBuilder sb = new StringBuilder();
//		String line;
//		while ((line = reader.readLine()) != null) {
//			// System.out.println(line);
//			if (!line.equals("")) {
//				System.out.println(line);
//				sb.append(line + "\n");
//			}
//
//		}
		StanfordNLPProcessor pro = new StanfordNLPProcessor("config/NLPConfig.properties");
		pro.preprocessing(
				"Brian Lucey, economics professor at Trinity College Dublin, said the government had taken too long to reach this point. He said he expected ultimate losses at Anglo and Allied Irish to be several billion euros' greater.");
//		pro.preprocessing(sb.toString());
		for (NamedEntity entity : pro.getEntities()) {
			System.out.println(entity.getWord() + ":" + entity.getType() + ":" + entity.getStart());
			// System.out.println(entity.getContext());
			for (String coreReference : entity.getEntityReferences()) {
				System.out.println("=========" + coreReference);
			}

			for (NamedEntity coreReference : entity.getRelateEntities()) {
				System.out.println("-------=======" + coreReference.getWord());
			}
		}

	}

}
