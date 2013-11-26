package cellularautomata;

public class Line {

	Cell c0;
	Rule rule;
	
	public Line(int width, Rule rule) {
		if (width < 1) throw new IllegalArgumentException();
		
		this.rule = rule;

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
	
	private interface CellHandler {public void handle(Cell cell, Object returnValue) throws InterruptedException;}
	
	public boolean next() {
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
		return isStable();
	}
	
	public boolean isStable() {
		
		boolean[] stable = {true};
		iterate(new CellHandler() {
			private int lastVal = -1;
			@Override
			public void handle(Cell cell, Object returnValue) throws InterruptedException {
				if (lastVal == -1) {
					lastVal = cell.value;
				} 
				if (lastVal != cell.value) {
					boolean[] stable = (boolean[]) returnValue;
					stable[0] = false;
					throw new InterruptedException();
				}
				lastVal = cell.value;
			}
		}, stable);
		return stable[0];
	}
	
	public static void main(String[] args) {
		Line l = new Line(10, new Rule());
		l.print();
		System.out.println(l.isStable());
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
		try {
			Cell ci = c0;
			do {
				handler.handle(ci, returnValue);
				ci = ci.right;
			} while (ci != c0);
		} catch (InterruptedException e) {
			
		}
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
	
}
