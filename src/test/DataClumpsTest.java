package test;

import visitor.DataClumpsDetector;

public class DataClumpsTest {

	public DataClumpsTest() {
		//what projects we chose for testing
		String projectPath1 = "C:\\Users\\lht\\Desktop\\test\\tinyuml-0.13_02-src";
		String projectPath2 = "C:\\Users\\lht\\Desktop\\test\\abbot-1.3.0-src";
		String projectPath3 = "C:\\Users\\lht\\Desktop\\test\\robocode-1.9.1.0-src";
		String projectPath4 = "C:\\Users\\lht\\Desktop\\test\\IHM 0.3\\src";
		
		//TODO
//		long start = System.currentTimeMillis();
		new DataClumpsDetector(projectPath1, 3, 5);//(3, 5) is to compare with inFusion
//		long end = System.currentTimeMillis();
		
//		System.out.println(end - start);
	}

}
