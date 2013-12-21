package cellularautomata;

public class Cell {
	
	int value;
	private int nextValue;
	Cell left;
	Cell right;
	private int id;
	
	private Line line;
	
	private static int count = 0;
	
	public Cell(Line state) {
		super();
		this.line = state;
		id = count++;
		value = Math.random() > 0.5 ? 1 : 0;
	}
	
	public void setLeft(Cell left) {
		this.left = left;
		left.setRight(this);
	}
	
	public void setRight(Cell right) {
		this.right = right;
	}
	
	public int getId() {
		return id;
	}
	
	public void findNextValue() {
		int currentState = (left.left.left.value  << 6) 
						 | (left.left.value       << 5) 
						 | (left.value            << 4) 
						 | (this.value            << 3) 
						 | (right.value           << 2) 
						 | (right.right.value     << 1) 
						 | (right.right.right.value);
		
		nextValue = line.rule.get(currentState);
	}
	
	public void goToNextState() {
		if (nextValue == 1) {
			line.hasOnes = true;
		} else if (nextValue == 0) {
			line.hasZeroes = true;
		}
		value = nextValue;
	}
	
	@Override
	public String toString() {
		return Integer.toString(id);
	}
	
}
