
public class Passengers {
	private static int ID=0;
	private int time;
	private int id;
	private int number;
	private int fromFloor;
	private int toFloor;
	private boolean polite = true;
	private int waitTime;
	private int boardTime;
	private int timeArrived;
	
	public Passengers(int time, int number, int from, int to) {
		this.time = time;
		this.number = number;
		this.fromFloor = from-1;
		this.toFloor = to-1;
		this.polite = true;
		this.waitTime = 1000;
		id = ID;
		ID++;
	}
	
	public Passengers(int time, int number, int from, int to, boolean polite, int waitTime) {
		this.time = time;
		this.number = number;
		this.fromFloor = from-1;
		this.toFloor = to-1;
		this.polite = polite;
		this.waitTime = waitTime;
		id = ID;
		ID++;
	}
	
	public int getId() {
		return id;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public int getBoardTime() {
		return boardTime;
	}

	public void setBoardTime(int boardTime) {
		this.boardTime = boardTime;
	}

	public int getTimeArrived() {
		return timeArrived;
	}

	public void setTimeArrived(int timeArrived) {
		this.timeArrived = timeArrived;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getFromFloor() {
		return fromFloor;
	}

	public void setFromFloor(int fromFloor) {
		this.fromFloor = fromFloor;
	}

	public int getToFloor() {
		return toFloor;
	}

	public void setToFloor(int toFloor) {
		this.toFloor = toFloor;
	}

	public boolean isPolite() {
		return polite;
	}

	public void setPolite(boolean polite) {
		this.polite = polite;
	}
	
	@Override
	public String toString() {
		return("\n   Time="+this.time+"\n   ID="+this.id+"\n   Number="+this.number+"\n   From="+this.fromFloor+"\n   To="+this.toFloor +"\n");
	}

	
}
