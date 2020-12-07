import java.util.ListIterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Floor {
	private GenericQueue<Passengers> down;
	private GenericQueue<Passengers> up;

	public Floor(int qSize) {
		down = new GenericQueue<Passengers>(qSize);
		up = new GenericQueue<Passengers>(qSize);
	}
	
	public boolean addToDownQueue(Passengers p) {
		return (down.add(p));
	}

	public boolean addToUpQueue(Passengers p) {
		return (up.add(p));
	}
	
	public boolean addToQueueDir(Passengers p, int direction) {
		return ((direction> 0)?addToUpQueue(p) : addToDownQueue(p));
	}
	
	public boolean upQueueIsEmpty() {
		return up.isEmpty();
	}

	public boolean downQueueIsEmpty() {
		return down.isEmpty();
	}
	
	public boolean queueIsEmptyDir(int direction) {
		return ((direction > 0) ? upQueueIsEmpty() : downQueueIsEmpty());
	}
	
	public Passengers peekUpQueue() {
		return up.peek();
	}

	public Passengers peekDownQueue() {
		return down.peek();
	}

	public Passengers peekQueueDir(int direction) {
		return ((direction > 0) ? peekUpQueue() : peekDownQueue());
	}

	public Passengers pollUpQueue() {
		Passengers p = up.poll();
		return p;
	}

	public Passengers pollDownQueue() {
		Passengers p = down.poll();
		return p;
	}

	public Passengers pollQueueDir(int direction) {
		return ((direction > 0) ? pollUpQueue() : pollDownQueue());
	}
	
	public String upQueueString() {
		String str = "";
		ListIterator<Passengers> list = up.getListIterator();
		if (list != null) {
			while (list.hasNext()) {
				str += list.next().getNumber();
				if (list.hasNext()) str += ",";
			}
		}
		return str;
	}

	public String downQueueString() {
		String str = "";
		ListIterator<Passengers> list = down.getListIterator();
		if (list != null) {
			while (list.hasNext()) {
				str += list.next().getNumber();
				if (list.hasNext()) str += ",";
			}
		}
		return str;
	}
	
	public boolean callInDir(int direction) {
		return ((direction>0) ? !up.isEmpty() : !down.isEmpty());
	}
	
	public Passengers passToBoard(int direction) {
		return ((direction>0)?up.peek():down.peek());
	}

}
