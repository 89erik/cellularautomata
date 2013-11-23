package cellularautomata;

public class Line {

	Cell c0;
	int rules;
	
	public Line(int width, int rules) {
		if (width < 1) throw new IllegalArgumentException();
		
		this.rules = rules;

		Cell left;
		Cell ci;
		
		left = ci = c0 = new Cell(this);
		
		for (int i=1; i<width; i++) {
			ci = new Cell(this);
			ci.setLeft(left);
			left = ci;
		}
		c0.setLeft(ci);
	}
	
	private interface CellHandler {public void handle(Cell cell, Object returnValue);}
	
	public void next() {
		iterate(new CellHandler() {
			@Override
			public void handle(Cell cell, Object returnValue) {
				cell.findNextValue();
			}
		}, null);
		iterate(new CellHandler() {
			@Override
			public void handle(Cell cell, Object returnValue) {
				cell.goToNextState();
			}
		}, null);
	}
	
	public int[] count() {
		int[] c = new int[2];
		iterate(new CellHandler() {
			@Override
			public void handle(Cell cell, Object returnValue) {
				int[] r = (int[]) returnValue;
				r[cell.value]++;
			}
		}, c);
		return c;
	}

	private Object iterate(CellHandler handler, Object returnValue) {
		Cell ci = c0;
		do {
			handler.handle(ci, returnValue);
			ci = ci.getRight();
		} while (ci != c0);
		return returnValue;
	}
	
	
	public void print() {
		iterate(new CellHandler() {
			@Override
			public void handle(Cell cell, Object returnValue) {
				System.out.print(cell.value);
			}
		}, null);
	}
	
	public static void main(String[] args) {
		//					    76543210
		Line s = new Line(20, 0b10101010);
		
		s.print();
		System.out.println();
		s.next();
		s.print();
	}
	
}
