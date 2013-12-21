package cellularautomata;

import java.util.concurrent.TimeUnit;

public class GA {

	/* Debug constants */
	static final boolean DEBUG = false;
	static final boolean SURVIVOR_STATS = false;
	static final int     GENERATION_PRINT_SEQUENCE = 1;
	static final int     GENERATION_PRINT_AMOUNT   = 44;
	
	/* GA constants */
	private final int    		POPULATION_SIZE = 500;
	static final double 		MUTATION_PROBABILITY = 0.015;
	private static final int 	WIDTH = 49;
	private static final int 	STEPS_PER_FITNESS = WIDTH*2;
	private final int	 		FITNESS_TESTS_PER_INDV = 100;
	private final int 	 		GENERATIONS = 1000;
	
	/* Memory */
	private Rule[] population;
	private int generation;
	private static long t0;
	
	public static void main(String[] args) {
		t0 = System.currentTimeMillis();
		new GA();

		long duration  = System.currentTimeMillis()-t0;
		
		String timestamp = String.format("%02d:%02d:%02d", 
                           TimeUnit.MILLISECONDS.toHours(duration),
				           TimeUnit.MILLISECONDS.toMinutes(duration) -  
				           TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
				           TimeUnit.MILLISECONDS.toSeconds(duration) - 
				           TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));   
		
		System.out.printf("Total duration: "+ timestamp);
		
	}
	
	public GA() {
        boolean testRule = false;
        if (testRule) {
    		printRuleTest(new Rule(0x24981a0d6f83e2c9L, 0x1d716561593987b7L), 20);
            return;
        }
		population = new Rule[POPULATION_SIZE];
		generateInitialPopulation();
		
		for (generation=1; generation<=GENERATIONS; generation++) {
			makeNewGeneration();
		}
		
		Rule winner = fittestIndividual();
		
		System.out.println("--- [WINNER PICKED] ---");
		System.out.printf("\t Winning rule: \t %s\n", winner);
		
		printGeneration();
		
		System.out.println();
		
		printRuleTest(winner, 10);
	}
	
	private void generateInitialPopulation() {
		for (int i=0; i<POPULATION_SIZE; i++) {
			population[i] = new Rule();
		}
	}
	
	/**
	 * Selects the new generation by mating two individuals selected 
	 * probabilistically by their fitness.
	 * Point 3 of page 7 in paper by Mitchell.
	 */
	private void makeNewGeneration() {
		Rule[] nextPopulation = new Rule[POPULATION_SIZE];
		int totalFitness = calculateFitnesses();
		
		if (generation % GENERATION_PRINT_SEQUENCE == 0) {
			printGeneration();
		}
		
		// Create new generation
		for (int i=0; i<POPULATION_SIZE; i++) {
			
			int index1 = 0, index2 = 0;
			int map1 = (int) (Math.random() * totalFitness);
			int map2 = (int) (Math.random() * totalFitness);
			boolean done1 = false;
			boolean done2 = false;
			Rule child;
			
			// Find individuals to mate
			for (int j=0, k=0; j < POPULATION_SIZE; j++) {
				if (done1 && done2) break;
				
				k += population[j].fitness;
				if (!done1 && k >= map1) {
					index1 = j;
					done1 = true;
				}
				if (!done2 && k >= map2) {
					index2 = j;
					done2 = true;
				}
			}
			child = population[index1].mate(population[index2]);
			nextPopulation[i] = child;
		}
		population = nextPopulation;
	}
	
	/**
	 * Returns the fittest individual of this generation.
	 * @return fittestIndividual
	 */
	private Rule fittestIndividual() {
		Rule fittest = population[0];
		for (int i=1; i<POPULATION_SIZE; i++) {
			if (population[i].fitness > fittest.fitness) {
				fittest = population[i];
			}
		}
		return fittest;
	}
	
	private void printGeneration() {
		System.out.println("Generation: " + generation);
		int max = GENERATION_PRINT_AMOUNT < POPULATION_SIZE ? GENERATION_PRINT_AMOUNT : POPULATION_SIZE; 
		for (int i = 0; i<max; i++) {
			System.out.printf("Rule: %s   \tfitness: %d\n", population[i], population[i].fitness);
		}
	}
	
	/**
	 * Calculates the fitness of all individuals in this generation.
	 * Returns the total amount of fitness.
	 * @return totalFitness
	 */
	private int calculateFitnesses() {
		int fitness;
		int totalFitness = 0;
		for (int i = 0; i < POPULATION_SIZE; i++) {
			fitness = 1;
			for (int j=0; j<FITNESS_TESTS_PER_INDV; j++) {
				if (singleFitness(i)) {
					fitness++;
				}
			}
			population[i].fitness = fitness; 
			totalFitness += fitness;
			debug("Total fitness for rule %s:\t%d\n", population[i], fitness);
		}
		return totalFitness;
	}
	
	
	private boolean singleFitness(int individual) {
//		debug("Fitness for rule %s \n", population[individual]);
		Line s = new Line(WIDTH, population[individual]);
		int[] solution, result;
		solution = s.count();
//		debug("\tSolution: \t#zeroes: %d,  #ones: %d", solution[0], solution[1]);
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
//			if (DEBUG) {
//				s.print();
//				System.out.println();
//			}
			if (!s.isStable()) {
				s.next();
			} else {
				break;
			}
		}
		if (DEBUG) {
			s.print();
			System.out.println();
		}
		result = s.count();

//		debug("\tResult: \t#zeroes: %d,  #ones: %d\n", result[0], result[1]);

		return solution[0] == result[0] && solution[1] == result[1];
	}
	
	void printRuleTest(Rule rule, int n) {
		if (n<0) throw new IllegalArgumentException();
		Line s;
		population = new Rule[1];
		population[0] = rule;
		for (int i=0; i<n; i++) {
			s = new Line(WIDTH, rule);
			
			for (int j=0; j<STEPS_PER_FITNESS; j++) {
				s.print();
				System.out.println();
				if (!s.isStable()) {
					s.next();
				} else {
					break;
				}
			}
			s.print();
			System.out.println();
			System.out.println("Sucess: " + String.valueOf(singleFitness(0)));
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
