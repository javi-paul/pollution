package main.api.masterslavepattern.safeTakeOff;

public enum TakeOffAlgorithm {
	
	OPTIMAL(0, "Optimal"),
	SIMPLIFIED(1, "Simplified"),
	RANDOM(2, "Random");
	
	private final int algorithmID;
	private final String algorithmName;
	
	private TakeOffAlgorithm(int id, String name) {
		this.algorithmID = id;
		this.algorithmName = name;
	}
	
	public int getID() {
		return this.algorithmID;
	}
	
	public String getName() {
		return this.algorithmName;
	}
	
	public static TakeOffAlgorithm getAlgorithm(String name) {
		TakeOffAlgorithm[] algorithms = TakeOffAlgorithm.values();
		for (int i = 0; i < algorithms.length; i++) {
			if (algorithms[i].getName().equals(name)) {
				return algorithms[i];
			}
		}
		
		return null;
	}
	
	public static String[] getAvailableAlgorithms() {
		TakeOffAlgorithm[] algorithms = TakeOffAlgorithm.values();
		String[] res = new String[algorithms.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = algorithms[i].getName();
		}
		
		return res;
	}
	
}