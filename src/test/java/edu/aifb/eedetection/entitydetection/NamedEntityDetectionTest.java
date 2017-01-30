package edu.aifb.eedetection.entitydetection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import edu.aifb.annotation.model.NamedEntity;
import edu.aifb.annotation.preprocessing.StanfordNLPProcessor;

public class NamedEntityDetectionTest {
	private StanfordNLPProcessor preprocessor;
	
	
	@Before
	public void init() throws IOException{
		this.preprocessor=new StanfordNLPProcessor("config/NLPConfig.properties");
	}
	
	@Test
	public void testNamedEntityDetection(){
		String testSentence="Brian Lucey, economics professor at Trinity College Dublin, said the government had taken too long to reach this point. He said he expected ultimate losses at Anglo and Allied Irish to be several billion euros' greater.";
		preprocessor.preprocessing(testSentence);
		List<NamedEntity> entities=preprocessor.getEntities();
		List<String> entityStrings = new ArrayList<>();
		for(NamedEntity entity:entities){
			entityStrings.add(entity.getWord());
		}
		Assert.assertTrue("Named Entity \"Brian Lucey\" is not detected", entityStrings.contains("Brian Lucey"));
		Assert.assertTrue("Named Entity \"Trinity College Dublin\" is not detected", entityStrings.contains("Trinity College Dublin"));
		Assert.assertTrue("Named Entity \"Allied Irish\" is not detected", entityStrings.contains("Allied Irish"));
		Assert.assertTrue("Named Entity \"Anglo\" is not detected", entityStrings.contains("Anglo"));
	
	}

}
