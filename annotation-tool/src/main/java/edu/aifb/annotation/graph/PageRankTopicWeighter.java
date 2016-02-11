package edu.aifb.annotation.graph;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.collections15.Transformer;

import edu.aifb.annotation.model.Sense;
import edu.aifb.annotation.mongodb.MongoDBLabel;
import edu.aifb.annotation.services.EntityDetectionService;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class PageRankTopicWeighter extends GraphBasedTopicWeighter {



	public PageRankTopicWeighter(EntityDetectionService mongo) {
		super(mongo);
		// TODO Auto-generated constructor stub
	}

	private float ALPHA = 0.15f;

	@Override
	public HashMap<Integer, Double> getTopicWeights(Collection<Sense> topics, DirectedSparseGraph<Vertex, Edge> graph)
			throws Exception {

		HashMap<Integer, Double> topicWeights = new HashMap<Integer, Double>();

		PageRankWithPriors<Vertex, Edge> ranker = new PageRankWithPriors<Vertex, Edge>(graph,
				new Transformer<Edge, Double>() {
					public Double transform(Edge edge) {
						double weight = edge.getWeight();
						Vertex source = edge.getSource();
						double totalWeight = source.getTotalEdgeWeight();
						return weight / totalWeight;
					}
				}, new Transformer<Vertex, Double>() {
					public Double transform(Vertex vertex) {
						return vertex.getWeight();
					}
				}, ALPHA);

		ranker.evaluate();

		for (Vertex vertex : graph.getVertices()) {
			if (vertex instanceof SenseVertex) {
				Sense topic = ((SenseVertex) vertex).getSense();
				double weight = ranker.getVertexScore(vertex);
				topicWeights.put(topic.getIndex(), weight);
			}

		}

		return topicWeights;
	}
}
