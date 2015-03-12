package vo;

import java.util.HashSet;

public class DataClumpsVO implements Comparable<DataClumpsVO> {
	
	private HashSet<String> dataClumps = new HashSet<String>();
	private int size = 0;
	
	private HashSet<DataInfoVO> source = new HashSet<DataInfoVO>();
	
	public DataClumpsVO(HashSet<String> dataClumps) {
		super();
		this.dataClumps = dataClumps;
		this.size = dataClumps.size();
	}

	public HashSet<String> getDataClumps() {
		return dataClumps;
	}

	public void setDataClumps(HashSet<String> dataClumps) {
		this.dataClumps = dataClumps;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public HashSet<DataInfoVO> getSource() {
		return source;
	}

	public void setSource(HashSet<DataInfoVO> source) {
		this.source = source;
	}
	
	//from largest to smallest
	@Override
	public int compareTo(DataClumpsVO other) {
		 if (this.getSize() < other.getSize()) {
			 return 1; 
		 }
	     else if (this.getSize() > other.getSize()) {
	    	 return -1;
	     }
	     else {
	    	 return 0;
	     }
	}
}
