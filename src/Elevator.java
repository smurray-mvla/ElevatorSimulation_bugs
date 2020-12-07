import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * 
 */

/**
 * @author ScottM
 * This class will represent an elevator, and will contain
 * configuration information (capacity, speed, etc) as well
 * as state information - such as stopped, direction, and count
 * of passengers targetting each floor...
 */
public class Elevator {
	// Elevator State Variables
	private final static int STOP = 0;
	private final static int MVTOFLR = 1;
	private final static int OPENDR = 2;
	private final static int OFFLD = 3;
	private final static int BOARD = 4;
	private final static int CLOSEDR = 5;
	private final static int MV1FLR = 6;

	// Configuration parameters
	private int capacity = 15;
	private int ticksPerFloor = 5;
	private int ticksDoorOpenClose = 2;  
	private int passPerTick = 3;
	
	//State Variables
	private int currState;
	private int prevState;
	private int prevFloor = 0;
	private int currFloor = 0;
	private int direction = 1;
	private int moveToFloor = 0;
	private int moveToFloorDir = 0;
	private int timeInState = 0;
	private int offLoadGrps = 0;
	private int doorState = 0;
	
	private int passengers = 0;
	private boolean newPassengers = false;
	private ArrayList<Passengers>[] passByFloor;
	
	public Elevator(int numFloors, int capacity, int floorTicks,int doorTicks, int tickPassengers) {
		this.capacity = capacity;
		this.ticksPerFloor = floorTicks;
		this.ticksDoorOpenClose = doorTicks;
		this.passPerTick = tickPassengers;
		passByFloor = new ArrayList[numFloors];
		
		for (int i = 0; i < numFloors; i++) 
			passByFloor[i] = new ArrayList<Passengers>(10); 
		this.prevState = STOP;
		this.currState = STOP;
		this.timeInState = 0;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getTicksPerFloor() {
		return ticksPerFloor;
	}

	public int getTicksDoorOpenClose() {
		return ticksDoorOpenClose;
	}

	public int getPassPerTick() {
		return passPerTick;
	}

	public int getPassengers() {
		return passengers;
	}

	public int getCurrFloor() {
		return currFloor;
	}

	public void setCurrFloor(int currFloor) {
		this.currFloor = currFloor;
	}

	public int getPrevFloor() {
		return prevFloor;
	}

	public int getCurrState() {
		return currState;
	}

	public void setCurrState(int currState) {
		this.currState = currState;
	}

	public int getPrevState() {
		return prevState;
	}

	public void setPrevState(int prevState) {
		this.prevState = prevState;
	}
	
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public boolean isDoorOpen() {
		return doorState == ticksDoorOpenClose;
	}

	public boolean isDoorClosed() {
		return doorState == 0;
	}

	public int getMoveToFloor() {
		return moveToFloor;
	}

	public void setMoveToFloor(int floor) {
		this.moveToFloor = floor;
	}

	public int getMoveToFloorDir() {
		return moveToFloorDir;
	}

	public void setMoveToFloorDir(int direction) {
		this.moveToFloorDir = direction;
	}

	public void updateCurrState(int currState) {
		this.prevState = this.currState;
		this.currState = currState;
		if (this.prevState != this.currState) 
			timeInState = 0;
	}
	
	public boolean isMoving() {
		return (timeInState%ticksPerFloor)!=0;
	}

	public void moveElevator() {
		timeInState++;
		prevFloor = currFloor;
		if ((timeInState % ticksPerFloor) == 0) { 
			currFloor = currFloor + direction;
		}
	}
	
	public void elevatorStopped() {
		timeInState++;
	}
	
	public boolean isStopped() {
		return ((currState == STOP) && (timeInState>0));
	}
	
	public boolean passToOffLoad() {
		return (passByFloor[currFloor].size() != 0 ); 
	}

	public void setOffLoadGrps(int number) {
		passengers -= number;
		offLoadGrps = number/passPerTick;
		if ((number%passPerTick) != 0) 
			offLoadGrps++;
	}

	public boolean offLoadDone() {
		return (timeInState == offLoadGrps);
	}
	
	public ArrayList<Passengers> offLoadPassengers() {
		int offLoadCnt = 0;
		ArrayList<Passengers> passOffLoad = null;
		if (!(passByFloor[currFloor].size() == 0)) {
			passOffLoad = new ArrayList<Passengers>();
			for (Passengers p: passByFloor[currFloor]) {
				offLoadCnt += p.getNumber();
				passOffLoad.add(p);
	
			}
			passByFloor[currFloor] = new ArrayList<Passengers>();
			setOffLoadGrps(offLoadCnt);
		}
		timeInState++;
		return passOffLoad;
	}
	
	public void addBoardedPassengers(Passengers p) {
		passByFloor[p.getToFloor()].add(p);
		passengers += p.getNumber();
		newPassengers = true;
	}
	
	public void boardPassengers() {
		timeInState++;
	}
	
	public int calcBoardingGrps(int numBoarded) {
		return (((numBoarded % passPerTick) == 0 ) ? numBoarded/passPerTick : numBoarded/passPerTick+1);
	}
	
	public boolean boardingDone(int boardingGrps) {
		return (timeInState >= boardingGrps);
	}
			
	public boolean isCapacity(int num) {
		return ((passengers + num) > capacity);
	}
	
	public boolean stateChanged() {
		return (prevState != currState);
	}
	
	public boolean floorChanged() {
		return (prevFloor != currFloor);
	}
	
	public boolean areNewPassengers() {
		return newPassengers;
	}
	
	public void clearNewPassengers() {
		newPassengers = false;
	}
	
	public void openDoor() {
		prevFloor = currFloor;
		if (doorState < ticksDoorOpenClose)
			doorState ++;
	}
	
	public void closeDoor() {
		if (doorState > 0)
			doorState --;
	}

	public boolean isEmpty() {
		return passengers == 0;
	}
	
}
