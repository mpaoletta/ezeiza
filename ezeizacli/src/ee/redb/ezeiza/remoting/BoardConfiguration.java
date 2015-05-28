package ee.redb.ezeiza.remoting;

import java.util.List;

public class BoardConfiguration {

	private String configId;
	private long startTime;
	private List<CellPhase> phases;
	private List<String> ordering;
	private boolean flush;
	
	public String getConfigId() {
		return configId;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public List<CellPhase> getPhases() {
		return phases;
	}
	
	public List<String> getOrdering() {
		return ordering;
	}
	
	public boolean flushRequested() {
		return flush;
	}
	
}