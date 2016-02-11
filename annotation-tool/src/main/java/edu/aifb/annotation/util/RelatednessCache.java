package edu.aifb.annotation.util;

import edu.aifb.annotation.model.Sense;
import gnu.trove.map.hash.TLongDoubleHashMap;

/**
 * This class caches the results of calculating relatedness measures between senses; 
 * If all sense comparisons are performed via this class, then no calculations will be repeated.
 */
public class RelatednessCache {

	TLongDoubleHashMap cachedRelatedness ;
	SenseComparer comparer;
	
	private long comparisonsRequested = 0 ;
	private long comparisonsCalculated = 0 ;
	
	/**
	 * Initialises the relatedness cache, where relatedness will be measured using the given {@link  ArticleComparer}.
	 *  
	 * @param comparer the comparer to use. 
	 */
	public RelatednessCache(SenseComparer comparer) {
		cachedRelatedness = new TLongDoubleHashMap() ;
		this.comparer = comparer ;
	}
	
	
	/**
	 * Calculates (or retrieves) the semantic relatedness of two articles. 
	 * The result will be identical to that returned by {@link ArticleComparer#getRelatedness(Article, Article)}
	 * 
	 * @param art1 
	 * @param art2
	 * @return the semantic relatedness of art1 and art2
	 */
	public double getRelatedness(Sense art1, Sense art2) throws Exception {
		
		comparisonsRequested++ ;
	
		return 0 ;
	}
	
	public long getComparisonsCalculated() {
		return comparisonsCalculated ;
	}
	
	public long getComparisonsRequested() {
		return comparisonsRequested ;
	}
	
	public double getCachedProportion() {
		double p = (double)comparisonsCalculated/comparisonsRequested ;
		return 1-p ;
	}
}
