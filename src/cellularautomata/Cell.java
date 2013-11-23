package cellularautomata;

public class Cell {
	
	int value;
	private int nextValue;
	private Cell left;
	private Cell right;
	private int id;
	
	private Line state;
	
	private static int count = 0;
	
	public Cell(Line state) {
		super();
		this.state = state;
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
	
	public Cell getLeft() {
		return left;
	}
	
	public Cell getRight() {
		return right;
	}
	
	public void findNextValue() {
		int currentState = (left.value  << 2) |  (this.value  << 1) | (right.value);
		nextValue = ((state.rules >>> currentState) & 0b1);
	}
	
	public void goToNextState() {
		value = nextValue;
	}
	
	@Override
	public String toString() {
		return Integer.toString(id);
	}
	
}
