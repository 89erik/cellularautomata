package cellularautomata;

import java.util.LinkedList;

public class GA {

	private static final boolean DEBUG = false;
	private static final boolean SURVIVOR_STATS = false;
	
	private byte[] population;
	
	private final int    		POPULATION_SIZE = 100;
	private final double 		SURVIVAL_RATE   = 0.3;
	private final double 		MUTATION_PROBABILITY = 0.01;
	private static final int 	WIDTH = 10;
	private static final int 	STEPS_PER_FITNESS = 8;
	private final int	 		FITNESS_TESTS_PER_INDV = 1000;
	private final int 	 		GENERATIONS = 100000;
	private final int			INDETERMINATION_PENALTY = 1; // Blir ganget med 2^INDETERMINATION_PENALTY
	
	/* Derivative constants */
	private final int    SURVIVAL_COUNT  = (int) (POPULATION_SIZE * SURVIVAL_RATE);
	private final int    CHILDREN_PER_INDV = (int)(1.0 / SURVIVAL_RATE);
	
	
	/* Locally shared variables */
	private LinkedList<Byte> survivorRules; 
	private LinkedList<Integer> survivorFitness;
	
	private double random = 0.5;
	
	
	/* Memory */
	private Integer[] fitnesses = new Integer[0x100];
	
	public static void main(String[] args) {
		new GA();
//		printRuleTest(0xe9, 100);
	}
	
	public GA() {
		population = new byte[POPULATION_SIZE];
		generateInitialPopulation();
		
		for (int i=0; i<GENERATIONS; i++) {
			if (i%1000 == 0) {
				System.out.printf("Generation #%d\n", i);
			}
			makeNewGeneration();
		}
		
		byte winner = select().get(0);
		
		System.out.println("--- [WINNER PICKED] ---");
		System.out.printf("\t Winning rule: \t %x\n", winner);
		
		printSurvivors();
		
		System.out.println();
		
		printRuleTest(winner, 10);
		
		int best = Integer.MAX_VALUE;
		int bestI = 0xfffff;
		for (int i=0; i<fitnesses.length; i++) {
			if (fitnesses[i] != null && fitnesses[i] < best) {
				best = fitnesses[i];
				bestI = i;
			}
		}
		System.out.printf("Best from fitnesses list: Rule=%x, fitness=%d\n", bestI, best);
	}
	
	private void printPopulationRules() {
		for (byte rule : population) {
			System.out.println(rule);
		}
	}
	
	private void generateInitialPopulation() {
		for (int i=0; i<POPULATION_SIZE; i++) {
			random = Math.random();
			population[i] = (byte)( getRandom(0xff));
		}
	}
	
	public void makeNewGeneration(){
		LinkedList<Byte> survivors = select();
		byte indv;
		for (int i=0; i< SURVIVAL_COUNT; i++) {
			indv = survivors.get(i);
			for (int j=0; j<CHILDREN_PER_INDV; j++) {
				indv = mate(indv);
				population[i+j] = mutate(indv);
			}
		}
	}
	
	private byte mate(byte i1) {
		byte i2 = survivorRules.get((int)((double)random*SURVIVAL_COUNT));
		int mask = getRandom(0x100);
		i2 &= mask;
		return (byte) (i1 | i2);
	}
	
	private byte mutate(byte individual) {
		if (random < MUTATION_PROBABILITY) {
			random = Math.random();
			return (byte)(individual ^ (1 << getRandom(8)));
		}
		return individual;
	}
	
	private int getRandom(int max) {
		return (int)((double)(random * max));
	}
	
	private LinkedList<Byte> select() {
		survivorRules   = new LinkedList<Byte>(); 
		survivorFitness = new LinkedList<Integer>();
		int fitness;
		
		for (int i = 0; i < POPULATION_SIZE; i++) {
			fitness = fitness(i);
			int j;
			for (j = survivorFitness.size(); j>0 && fitness < survivorFitness.get(j-1); j--);
			if (j < SURVIVAL_COUNT) {
				survivorRules.add(j, population[i]);
				survivorFitness.add(j, fitness);
			}
		}
		
		if (SURVIVOR_STATS) {
			printSurvivors();
		}
		
		return survivorRules;
	}
	
	private void printSurvivors() {
		System.out.printf("Survivors:\n");
		for(int i = 0; i<survivorRules.size(); i++) {
			System.out.printf("Rule: %x, fitness: %d\n", survivorRules.get(i), survivorFitness.get(i));
		}

	}
	
	
	private int fitness(int individual) {
		int fitnessAccumulator = 0;
		int fitness;

		if (fitnesses[individual] == null) {
			for (int i=0; i<FITNESS_TESTS_PER_INDV; i++) {
				fitnessAccumulator += singleFitness(individual);
			}
			fitness = fitnessAccumulator;
			
			debug("Total fitness for rule %x:\t%d\n", population[individual], fitness);
			fitnesses[individual] = fitness;
		} else {
			fitness = fitnesses[individual];
		}
		return fitness;
	}
	
	
	private int singleFitness(int individual) {
		debug("Fitness for rule %x \n", population[individual]);
		Line s = new Line(WIDTH, population[individual]);
		int[] solution, result;
		solution = s.count();
		debug("\tSolution: \t#zeroes: %d,  #ones: %d", solution[0], solution[1]);
		if (solution[0] > solution[1]) {
			solution[0] = solution[0] + solution[1];
			solution[1] = 0;
			debug("\t(More zeroes)\n");
		} else {
			solution[1] = solution[0] + solution[1];
			solution[0] = 0;
			debug("\t(More ones)\n");
		}
		
	
		for (int i=0; i<STEPS_PER_FITNESS; i++) {
			if (DEBUG) {
				s.print();
				System.out.println();
			}
			s.next();
		}
		if (DEBUG) {
			s.print();
			System.out.println();
		}
		result = s.count();

		debug("\tResult: \t#zeroes: %d,  #ones: %d\n", result[0], result[1]);
		int fitness = Math.abs(result[0]- solution[0]) /*+ Math.abs(result[1] - solution[1])*/;
		fitness <<= INDETERMINATION_PENALTY;
		debug("\tFitness: %d\n", fitness);
		return fitness;
	}
	
	public static void printRuleTest(int rule, int n) {
		if (n<0) throw new IllegalArgumentException();
		
		Line s;
		for (int i=0; i<n; i++) {
			s = new Line(WIDTH, rule);
			
			for (int j=0; j<STEPS_PER_FITNESS; j++) {
				s.print();
				System.out.println();
				s.next();
			}
			s.print();
			System.out.println();
			System.out.println();
		}

	}
	
	public static void survivorStats(String msg, Object... args) {
		if (SURVIVOR_STATS) System.out.printf(msg, args);
	}
	
	public static void debug(String msg, Object... args) {
		if (DEBUG) System.out.printf(msg, args);
	}
}
