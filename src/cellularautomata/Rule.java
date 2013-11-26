package cellularautomata;

public class Rule {
	
	private long h,l;
	
	public Rule() {
		h = getRandomLong();
		l = getRandomLong();
	}
	
	public Rule(long h, long l) {
		this.h = h;
		this.l = l;
	}
	
	int get(int state) {
		if (state < 64) {
			return (int) ((l >>> state) & 1); 
		} else {
			return (int) ((h >>> state-64) & 1);
		}
	}
	
	/**
	 * Returns a child of this rule and mate
	 * @param mate
	 * @return child
	 */
	Rule mate(Rule mate) {
		long maskH = getRandomLong();
		long maskL = getRandomLong();
		
		long newH = h;
		long newL = l;
		
		// Clear this' bits where the mate's bit will be inserted
		newH &= ~maskH;
		newL &= ~maskL;
		
		// Insert mate's bits
		newH |= (mate.h & maskH);
		newL |= (mate.l & maskL);
		
		return new Rule(newH, newL);
	}
	
	/**
	 * Mutates this rule
	 */
	void mutate() {
		if (GA.random < GA.MUTATION_PROBABILITY) {
			GA.random = Math.random();
			int bitn = getRandom(128);
			if (bitn < 64) {
				l ^= (1 << bitn);
			} else {
				h ^= (1 << bitn-64);
			}
		}
	}
	
	private int getRandom(int max) {
		return (int)((double)(GA.random * max));
	}
	
	private long getRandomLong() {
		return (long)(double) (Math.random() * Integer.MAX_VALUE) | (long)(double) (Math.random() * Integer.MAX_VALUE) << 32;
	}
	
	public String toString() {
		return String.format("%x%x", h,l);
	}

}
