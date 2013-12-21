package cellularautomata;

import java.util.Random;

/**
 * A 128 bit rule.
 * @author Erik Lothe
 *
 */
public class Rule {
	
	private long h,l;
	public int fitness;
	private Random random;
	
	/**
	 * Generates a random rule
	 */
	public Rule() {
		random = new Random();
		h = random.nextLong();
		l = random.nextLong();
	}
	
	/**
	 * Instantiates a rule of given parameters
	 * @param highEnd
	 * @param lowEnd
	 */
	public Rule(long h, long l) {
		this.h = h;
		this.l = l;
	}
	
	/**
	 * Returns the effect of the given state according to this rule.
	 * @param state
	 * @return effect (1 or 0)
	 */
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
	@Deprecated
	Rule mateOld(Rule mate) {
		long maskH = random.nextLong();
		long maskL = random.nextLong();
		
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
	 * Returns a possibly mutated child of this rule and given mate.
	 * @param mate
	 * @return child
	 */
	Rule mate(Rule mate) {
		Rule child;
		int pivot = random.nextInt(128);
		
		long newH = h;
		long newL = l;
		long mask;

		if (pivot < 64) {
			mask = 0xffffffffffffffffL << pivot;
			newL &= ~mask;
			newL |= (mate.l & mask);
			newH = mate.h;
		} else {
			mask = 0xffffffffffffffffL << (pivot-64);
			newH &= ~mask;
			newH |= (mate.h & mask);
		}
		
		child = new Rule(newH, newL);
		child.mutate(pivot);
		return child;
	}
	
	/**
	 * Mutates this rule
	 */
	private void mutate(int pos) {
		if (Math.random() < GA.MUTATION_PROBABILITY) {
			if (pos < 64) {
				l ^= (1 << pos);
			} else {
				h ^= (1 << pos-64);
			}
		}
	}
	
	@Deprecated
	private long getRandomLong() {
		return (long)(double) (Math.random() * Integer.MAX_VALUE) | (long)(double) (Math.random() * Integer.MAX_VALUE) << 32;
	}
	
	public String toString() {
		return String.format("%x%x", h,l);
	}

}
