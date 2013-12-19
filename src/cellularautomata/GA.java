package cellularautomata;

import java.util.LinkedList;

public class GA {

	private static final boolean DEBUG = true;
	private static final boolean SURVIVOR_STATS = false;
	
	private Rule[] population;
	
	private final int    		POPULATION_SIZE = 100;
	private final double 		SURVIVAL_RATE   = 0.10;
	static final double 		MUTATION_PROBABILITY = 0.015;
	private static final int 	WIDTH = 49;
	private static final int 	STEPS_PER_FITNESS = WIDTH;
	private final int	 		FITNESS_TESTS_PER_INDV = 100;
	private final int 	 		GENERATIONS = 100;
//	private final int			INDETERMINATION_PENALTY = 1; // Blir ganget med 2^INDETERMINATION_PENALTY
	
	/* Derivative constants */
	private final int    SURVIVAL_COUNT  = (int) (POPULATION_SIZE * SURVIVAL_RATE);
	private final int    CHILDREN_PER_INDV = (int)(1.0 / SURVIVAL_RATE);
	
	
	/* Locally shared variables */
	private LinkedList<Rule> survivorRules; 
	private LinkedList<Integer> survivorFitness;
	
	static double random = 0.5;
	
	
	/* Memory */
	private Integer[] fitnesses = new Integer[0x100];
	
	public static void main(String[] args) {
		new GA();
	}
	
	public GA() {
        boolean testRule = false;
        if (testRule) {
    		printRuleTest(new Rule(0x24981a0d6f83e2c9L, 0x1d716561593987b7L), 20);
            return;
        }
		population = new Rule[POPULATION_SIZE];
		generateInitialPopulation();
		
		for (int i=0; i<GENERATIONS; i++) {
			makeNewGeneration();
			if (i%1 == 0) {
				System.out.printf("Generation #%d\n", i);
				printSurvivors();
			}
		}
		
		Rule winner = select().get(0);
		
		System.out.println("--- [WINNER PICKED] ---");
		System.out.printf("\t Winning rule: \t %s\n", winner);
		
		printSurvivors();
		
		System.out.println();
		
		printRuleTest(winner, 10);
	}
	
	private void printPopulationRules() {
		for (Rule rule : population) {
			System.out.println(rule);
		}
	}
	
	private void generateInitialPopulation() {
		for (int i=0; i<POPULATION_SIZE; i++) {
			random = Math.random();
			population[i] = new Rule();
		}
	}
	
	public void makeNewGeneration(){
		LinkedList<Rule> survivors = select();
		Rule indv, child;
		for (int i=0; i< SURVIVAL_COUNT; i++) {
			indv = survivors.get(i);
			for (int j=0; j<CHILDREN_PER_INDV; j++) {
				child = indv.mate(survivorRules.get((int)((double)random*SURVIVAL_COUNT)));
				random = Math.random();
				child.mutate();
				population[i+j] = child;
			}
		}
	}	

	
	private LinkedList<Rule> select() {
		survivorRules   = new LinkedList<Rule>(); 
		survivorFitness = new LinkedList<Integer>();
		int fitness;
		
		for (int i = 0; i < POPULATION_SIZE; i++) {
			fitness = fitness(i);
			int j;
			for (j = survivorFitness.size(); j>0 && fitness > survivorFitness.get(j-1); j--);
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
			System.out.printf("Rule: %s   \tfitness: %d\n", survivorRules.get(i), survivorFitness.get(i));
		}

	}
	
	
	private int fitness(int individual) {
		int fitness = 0;
		for (int i=0; i<FITNESS_TESTS_PER_INDV; i++) {
			if (singleFitness(individual)) {
				fitness++;
			}
		}
		debug("Total fitness for rule %x:\t%d\n", population[individual], fitness);
		fitnesses[individual] = fitness;
		return fitness;
	}
	
	
	private boolean singleFitness(int individual) {
		debug("Fitness for rule %s \n", population[individual]);
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

		debug("\tResult: \t#zeroes: %d,  #ones: %d\n", result[0], result[1]);

//		int fitness = Math.abs(result[0]- solution[0]) /*+ Math.abs(result[1] - solution[1])*/;
//		fitness <<= INDETERMINATION_PENALTY;
//		return fitness;
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
				s.next();
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
