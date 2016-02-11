package edu.aifb.annotation.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.aifb.annotation.model.NamedEntity;
import edu.aifb.annotation.model.Sense;
import edu.aifb.annotation.services.EntityDetectionService;
import edu.aifb.annotation.util.RelatednessCache;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public abstract class GraphBasedTopicWeighter {

	private static float DEFAULT_MIN_WEIGHT = 0.1f;
	private EntityDetectionService service;
  
	public GraphBasedTopicWeighter(EntityDetectionService mongo) {
		super();
		this.service = mongo;
		
	}

	/**
	 * @return the mappings of the topic index and its weight
	 */
	public abstract HashMap<Integer, Double> getTopicWeights(Collection<Sense> senses,
			DirectedSparseGraph<Vertex, Edge> graph) throws Exception;

	/**
	 * Weights and sorts the given topics according to some criteria.
	 * 
	 * @param topics
	 *            the topics to be weighted and sorted.
	 * @param rc
	 *            a cache in which relatedness measures will be saved so they aren't repeatedly calculated. This may be
	 *            null.
	 * @return the weighted topics.
	 * @throws Exception
	 *             depends on the implementing class
	 */
	public ArrayList<Sense> getWeightedTopics(Collection<Sense> topics, RelatednessCache rc) throws Exception {
		long start = System.currentTimeMillis();
		DirectedSparseGraph<Vertex, Edge> graph = buildGraph(topics, rc);
		long end = System.currentTimeMillis();

		long newStart = System.currentTimeMillis();
		HashMap<Integer, Double> topicWeights = getTopicWeights(topics, graph);
		end = System.currentTimeMillis();

		ArrayList<Sense> weightedTopics = setTopicWeights(topicWeights, topics);

		end = System.currentTimeMillis();

		return weightedTopics;
	}

	public ArrayList<Sense> setTopicWeights(Map<Integer, Double> topicWeights, Collection<Sense> topics) {
		ArrayList<Sense> weightedTopics = new ArrayList<Sense>();

		double totalWeight = 0;
		double maxWeight = 0;

		for (Sense topic : topics) {
			double weight = topicWeights.containsKey(topic.getIndex()) ? topicWeights.get(topic.getIndex()) : 0;
			// set the weight of each topic after performing graph algorithm
			topic.setWeight(weight);
			totalWeight += weight;
			maxWeight = Math.max(maxWeight, weight);
		}

		for (Sense topic : topics) {
			if (topicWeights.containsKey(topic.getIndex())) {
				double weight = topic.getWeight();
				weight /= maxWeight;
				topic.setWeight(weight);

				// if (weight < wikipedia.getConfig().getMinWeight())
				// continue;
			} else {
				topic.setWeight(0.0);
			}

			if (topic.getWeight() != 0)
				weightedTopics.add(topic);
		}

		// Collections.sort(weightedTopics);

		return weightedTopics;
	}

	public DirectedSparseGraph<Vertex, Edge> buildGraph(Collection<Sense> topics, RelatednessCache rc)
			throws Exception {

		long start = System.currentTimeMillis();

		DirectedSparseGraph<Vertex, Edge> graph = new DirectedSparseGraph<Vertex, Edge>();

		Set<NamedEntityVertex> refVertices = new HashSet<NamedEntityVertex>();
		Map<Integer, SenseVertex> index2topicVertices = new HashMap<Integer, SenseVertex>();
		Map<String, Set<SenseVertex>> name2topicVertices = new HashMap<String, Set<SenseVertex>>();
		Set<Edge> refTopicEdges = new HashSet<Edge>();
		Set<Edge> topicTopicEdges = new HashSet<Edge>();

		Map<NamedEntity, Set<Sense>> ref2topics = new HashMap<NamedEntity, Set<Sense>>();
		Map<Sense, Set<NamedEntity>> topic2references = new HashMap<Sense, Set<NamedEntity>>();

		for (Sense topic : topics) {
			SenseVertex topicVertex = new SenseVertex(topic);
			topicVertex.setWeight(1);
			index2topicVertices.put(topic.getIndex(), topicVertex);

			Set<SenseVertex> topicVertices = name2topicVertices.get(topic.getTitle());
			if (topicVertices == null) {
				topicVertices = new HashSet<SenseVertex>();
				name2topicVertices.put(topic.getTitle(), topicVertices);
			}
			topicVertices.add(topicVertex);

			Set<NamedEntity> topicReferences = topic2references.get(topic);
			if (topicReferences == null) {
				topicReferences = new HashSet<NamedEntity>();
				topic2references.put(topic, topicReferences);
			}

			NamedEntity ref = topic.getEntity();
			topicReferences.add(ref);

			Set<Sense> referredTopics = ref2topics.get(ref);
			if (referredTopics == null) {
				referredTopics = new HashSet<Sense>();
				ref2topics.put(ref, referredTopics);
			}
			referredTopics.add(topic);
		}

		long end = System.currentTimeMillis();

		start = System.currentTimeMillis();

		for (NamedEntity reference : ref2topics.keySet()) {
			NamedEntityVertex refVertex = new NamedEntityVertex(reference);
			//TODO?
//			double vertexWeight = reference.getLabel().getLinkProbability();
			double vertexWeight = 1;
			refVertex.setWeight(vertexWeight);
			refVertices.add(refVertex);
			Set<Sense> referredTopics = ref2topics.get(reference);
			for (Sense topic : referredTopics) {
				Vertex topicVertex = index2topicVertices.get(topic.getIndex());
				Edge edge = new Edge(refVertex, topicVertex);
				//done
//				double edgeWeight = topic.getCommenness();
				double edgeWeight=0;
				try{
				edgeWeight = this.service.computeSim4Entities(topic.getEntity(), topic.getContext());
				}catch(NullPointerException e){
					System.out.println("service"+this.service);
					System.out.println(topic.getEntity());
					System.out.println(topic.getContext());
				}
				edge.setWeight(edgeWeight);
				refVertex.addEdge(edge);
				refTopicEdges.add(edge);
			}
		}

		end = System.currentTimeMillis();

		start = System.currentTimeMillis();

		// considering the pairs of topic vertices that are directly connected
		// in the data graph using pageLinksIn
		for (int index : index2topicVertices.keySet()) {
			SenseVertex target = index2topicVertices.get(index);
			
			Set<String> linksIn = this.getLinksIn(target.getSense(),topics);
		
			for (String name : linksIn) {
				Set<SenseVertex> sources = name2topicVertices.get(name);
			//Set<SenseVertex> sources =this.getSoucesSenses(index, index2topicVertices);
				if (sources == null)
					continue;
				for (SenseVertex source : sources) {
					//double relatedness = rc.getRelatedness(target.getSense(), source.getSense());
					double relatedness = this.service.computeSR(target.getSense(), source.getSense());
					if (relatedness == 0)
						continue;
					Set<NamedEntity> sourceReferenceSet = topic2references.get(target.getSense());
					Set<NamedEntity> targetReferenceSet = topic2references.get(source.getSense());
					Set<NamedEntity> intersection = new HashSet<NamedEntity>(sourceReferenceSet);
					intersection.retainAll(targetReferenceSet);
					if (intersection.size() != 0)
						continue;
					Edge edge = new Edge(target, source);
					edge.setWeight(relatedness);
					target.addEdge(edge);
					topicTopicEdges.add(edge);

					edge = new Edge(source, target);
					edge.setWeight(relatedness);
					source.addEdge(edge);
					topicTopicEdges.add(edge);
				}
			}
		}

		
	
		// considering the pairs of topic vertices that are directly connected
		// in the data graph using pageLinksOut
		// for (int index : index2topicVertices.keySet()) {
		// TopicVertex source = index2topicVertices.get(index);
		// Article[] linksOut = source.getTopic().getLinksOut();
		// for (Article article : linksOut) {
		// int pageId = article.getId();
		// Set<TopicVertex> targets = pageId2topicVertices.get(pageId);
		// if (targets == null)
		// continue;
		// double relatedness = rc.getRelatedness(source.getTopic(), article);
		// if (relatedness == 0)
		// continue;
		// for (TopicVertex target : targets) {
		// Set<TopicReference> sourceReferenceSet =
		// topic2references.get(source.getTopic());
		// Set<TopicReference> targetReferenceSet =
		// topic2references.get(target.getTopic());
		// Set<TopicReference> intersection = new
		// HashSet<TopicReference>(sourceReferenceSet);
		// intersection.retainAll(targetReferenceSet);
		// if (intersection.size() != 0)
		// continue;
		// Edge edge = new Edge(source, target);
		// edge.setWeight(relatedness);
		// source.addEdge(edge);
		// topicTopicEdges.add(edge);
		//
		// edge = new Edge(target, source);
		// edge.setWeight(relatedness);
		// target.addEdge(edge);
		// topicTopicEdges.add(edge);
		// }
		// }
		// }

		// considering all pairs of topic vertices in the disambiguation graph
		// for (int sourceIndex : index2topicVertices.keySet()) {
		// TopicVertex source = index2topicVertices.get(sourceIndex);
		// for (int targetIndex : index2topicVertices.keySet()) {
		// if (targetIndex == sourceIndex)
		// continue;
		// TopicVertex target = index2topicVertices.get(targetIndex);
		// double relatedness = rc.getRelatedness(source.getTopic(),
		// target.getTopic());
		// if (relatedness == 0)
		// continue;
		// Set<TopicReference> sourceReferenceSet =
		// topic2references.get(source.getTopic());
		// Set<TopicReference> targetReferenceSet =
		// topic2references.get(target.getTopic());
		// Set<TopicReference> intersection = new
		// HashSet<TopicReference>(sourceReferenceSet);
		// intersection.retainAll(targetReferenceSet);
		// if (intersection.size() != 0)
		// continue;
		// Edge edge = new Edge(source, target);
		// edge.setWeight(relatedness);
		// source.addEdge(edge);
		// topicTopicEdges.add(edge);
		// }
		// }

		end = System.currentTimeMillis();

		start = System.currentTimeMillis();

		for (Vertex vertex : refVertices) {
			boolean added = graph.addVertex(vertex);
			// if (added)
			// Logger.getLogger(GraphBasedTopicWeighter.class).info("Vertex " +
			// vertex + " is added!");
		}

		for (Vertex vertex : index2topicVertices.values()) {
			boolean added = graph.addVertex(vertex);
			// if (added)
			// Logger.getLogger(GraphBasedTopicWeighter.class).info("Vertex " +
			// vertex + " is added!");
		}

		for (Edge edge : refTopicEdges) {
			boolean added = graph.addEdge(edge, edge.getSource(), edge.getTarget());
			// if (added)
			// Logger.getLogger(GraphBasedTopicWeighter.class).info("Edge " +
			// edge + " is added!");
		}

		for (Edge edge : topicTopicEdges) {
			boolean added = graph.addEdge(edge, edge.getSource(), edge.getTarget());
			// if (added)
			// Logger.getLogger(GraphBasedTopicWeighter.class).info("Edge " +
			// edge + " is added!");
		}

		end = System.currentTimeMillis();

		return graph;
	}
	private  Set<String> getLinksIn(Sense sense, Collection<Sense> topics){
		Set<String> result=new HashSet();
		if(sense.getType().equals(NamedEntity.Type.NI)){
			List<String> links=this.service.getMongoDBLabel().getLinksIn(sense.getTitle());
			for(String s:links){
				result.add(s);
			}
			for(Sense s:topics){
				if(s.getType().equals(NamedEntity.Type.NO))
					result.add(s.getTitle());
			}
			
		}else{
			for(Sense s:topics){
				if(s.getIndex()!=sense.getIndex())
					result.add(s.getTitle());
			}
		}
		return result;
		
	}
	
	private Set<SenseVertex> getSoucesSenses(int topicid,Map<Integer, SenseVertex> index2topicVertex){
		Set<SenseVertex> result=new HashSet();
		for(Entry<Integer,SenseVertex> entry:index2topicVertex.entrySet()){
			if(entry.getKey()!=topicid){
				result.add(entry.getValue());
			}
		}
		return result;
	}

}
