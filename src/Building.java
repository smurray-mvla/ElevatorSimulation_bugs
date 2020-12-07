import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Building {
	private final static Logger LOGGER = Logger.getLogger(Building.class.getName());
	private FileHandler fh;
	private File passDataFile;
	
	
	// Elevator State Variables
	private final static int STOP = 0;
	private final static int MVTOFLR = 1;
	private final static int OPENDR = 2;
	private final static int OFFLD = 3;
	private final static int BOARD = 4;
	private final static int CLOSEDR = 5;
	private final static int MV1FLR = 6;
	
	private final static int UP = 1;
	private final static int DOWN = -1;

	private final int NUM_FLOORS;
	private final int NUM_ELEVATORS;
	private final int FLOOR_QSIZE = 10;
	private Floor[] floors;
	private Elevator[] elevators;
	private GenericQueue<Passengers> passQ;
	private ArrayList<Passengers> passSuccess;
	private ArrayList<Passengers> gaveUp;
	private boolean queuesUpdated = false;
	private int numBoarded;
	private int boardingGrps;

	public Building(int numFloors, int numElevators, String logfile) {
		NUM_FLOORS = numFloors;
		NUM_ELEVATORS = numElevators;
		passQ = new GenericQueue<Passengers>(1000);
		passSuccess = new ArrayList<Passengers>();
		gaveUp = new ArrayList<Passengers>();
		passDataFile = new File(logfile.replaceAll(".log","PassData.csv"));

		System.setProperty("java.util.logging.SimpleFormatter.format",
				           "%4$s - %5$s%n");
		LOGGER.setLevel(Level.OFF);
		try {
			fh = new FileHandler(logfile);
			LOGGER.addHandler(fh);
			SimpleFormatter simple = new SimpleFormatter();
			fh.setFormatter(simple);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// create the floors
		floors = new Floor[NUM_FLOORS];
		for (int i = 0; i < NUM_FLOORS; i++) {
			floors[i]= new Floor(FLOOR_QSIZE);
		}
		elevators = new Elevator[NUM_ELEVATORS];
		numBoarded = 0;
		boardingGrps = 0;
	}
	
	public void configElevators(int capacity, int floorTicks, int doorTicks, int tickPassengers) {
		for (int i = 0; i < NUM_ELEVATORS; i++) {
			elevators[i] = new Elevator(NUM_FLOORS,capacity,floorTicks,doorTicks,tickPassengers);
		}	
	}

	public boolean addPassengersToQueue(int time, int numPass, int from, int to, boolean polite,int waitTime) {
		return(passQ.add(new Passengers(time, numPass,from,to,polite,waitTime)));
	}
	
	public void checkPassengerQueue(int time) {
		Passengers p;
		while (isWaiting(time)) {
			p = passQ.poll(); 
			addPassengersToFloor(p);
			boolean dir = (p.getToFloor() - p.getFromFloor()) >0;
			LOGGER.info("Time="+time+"   Called="+p.getNumber()+" Floor="+ (p.getFromFloor()+1) 
					+" Dir="+((dir)?"Up":"Down")+"   passID=" + p.getId());
		}
	}
	
	private boolean isWaiting(int time) {
		if (passQ.isEmpty()) 
			return false;
		return (time >= passQ.peek().getTime());
	}
	
	private boolean addPassengersToFloor(Passengers p) {
		boolean result;
		int direction = p.getToFloor() - p.getFromFloor();
		result = floors[p.getFromFloor()].addToQueueDir(p,direction);			
		queuesUpdated |= result;
		return(result);
	}
	
	public boolean getQueuesUpdated() {
		return queuesUpdated;
	}
	
	public void clearQueuesUpdated() {
		queuesUpdated = false;
	}
	
	public GenericQueue<Passengers> getPassQueue() {
		return passQ;
	}
	
	private boolean callPending() {
		return(upCallPending() || downCallPending());
	}
	
	private int numUpCallPending() {
		int callCnt = 0;
		for (int i = 0; i < NUM_FLOORS; i++) {
		   if (!floors[i].queueIsEmptyDir(UP)) {
			   callCnt++;
		   }
		}
		return callCnt;
	}

	private int numDownCallPending() {
		int callCnt = 0;
		for (int i = 0; i < NUM_FLOORS; i++) {
		   if (!floors[i].queueIsEmptyDir(DOWN)) {
			   callCnt++;
		   }
		}
		return callCnt;
	}

	private boolean upCallPending() {
		boolean aCall = false;
		for (int i = 0; i < NUM_FLOORS; i++) {
			aCall = !floors[i].queueIsEmptyDir(UP);
			if (aCall)
				break;
		}
		return aCall;
	}
	
	private boolean downCallPending() {
		boolean aCall = false;
		for (int i = 0; i < NUM_FLOORS; i++) {
			aCall = !floors[i].queueIsEmptyDir(DOWN);
			if (aCall)
				break;
		}
		return aCall;
	}
	
	private Passengers lowestUpCall() {
		for (int i = 0; i < NUM_FLOORS; i++ ) {
			if (!floors[i].queueIsEmptyDir(UP)) {
				return floors[i].peekQueueDir(UP);
			}
		}
		return null;
	}
	
	private Passengers highestDownCall() {
		for (int i = (NUM_FLOORS-1); i >= 0; i-- ) {
			if (!floors[i].queueIsEmptyDir(DOWN)) {
				return floors[i].peekQueueDir(DOWN);
			}
		}
		return null;
	}
	
	// this will ONLY be called from a stop state...
	private Passengers prioritizePassengerCalls(Elevator lift) {
		Passengers lowestUp = null;
		Passengers highestDown = null;
		int currFloor = lift.getCurrFloor();
		int upRequests = numUpCallPending();
		int downRequests = numDownCallPending();
		boolean currFloorUpReq = !(floors[currFloor].queueIsEmptyDir(UP));
		boolean currFloorDownReq = !(floors[currFloor].queueIsEmptyDir(DOWN));
		int distUp = NUM_FLOORS;
		int distDown = NUM_FLOORS;
		
		if (currFloorUpReq && !currFloorDownReq) {
			return floors[currFloor].peekQueueDir(UP);
		} else if (currFloorDownReq && !currFloorUpReq) {
			return floors[currFloor].peekQueueDir(DOWN);
		} else if (currFloorUpReq && currFloorDownReq) {
			return ((upRequests>=downRequests) ? floors[currFloor].peekQueueDir(UP)
					                           : floors[currFloor].peekQueueDir(DOWN));
		}
		
		if (upRequests >0) {
			lowestUp = lowestUpCall();
			distUp = Math.abs(lift.getCurrFloor() - lowestUp.getFromFloor());
		}
		if (downRequests >0) { 
			highestDown = highestDownCall();
			distDown = Math.abs(lift.getCurrFloor() - highestDown.getFromFloor());
		}	
		if ((upRequests > downRequests)) return lowestUp;
		if ((downRequests > upRequests)) return highestDown;
		if (distUp < distDown) return lowestUp;
		if (distDown < distUp) return highestDown;
		return lowestUp;
	}
	
	public String strFloorQueue(int floor, boolean up) {
		if (up) {
			return floors[floor].upQueueString();
		} else {
			return floors[floor].downQueueString();
		}
	}
	

	private boolean anyCallsAbove(int floor) {
		for (int i = floor+1; i < NUM_FLOORS; i++) {
			if (!floors[i].queueIsEmptyDir(UP) || !floors[i].queueIsEmptyDir(DOWN))
				return true;
		}
		return false;
	}
	
	private boolean anyCallsBelow(int floor) {
		for (int i = 0; i < floor; i++) {
			if (!floors[i].queueIsEmptyDir(UP) || !floors[i].queueIsEmptyDir(DOWN))
				return true;
		}
		return false;
	}

	private int currStateStop(int time, Elevator lift) {
		lift.elevatorStopped();
		if (lift.isEmpty() && !callPending()) {
			return STOP;
		} else {
			Passengers p = prioritizePassengerCalls(lift);
			if (lift.getCurrFloor() == p.getFromFloor()) {
				lift.setDirection((p.getToFloor()>lift.getCurrFloor()) ? 1 : -1);
				return OPENDR;
			} else {
				lift.setDirection((p.getFromFloor()>lift.getCurrFloor()) ? 1 : -1);
				lift.setMoveToFloorDir((p.getToFloor() > p.getFromFloor())?1:-1);
				lift.setMoveToFloor(p.getFromFloor());
				return MVTOFLR;
			}
		}
	}
	
	private int currStateMvToFlr(int time, Elevator lift) {
		lift.moveElevator();
		if (lift.getCurrFloor() != lift.getMoveToFloor()) {
			return MVTOFLR;	
		} else {
			lift.setDirection(lift.getMoveToFloorDir());
			return OPENDR;
		}
	}

	private int currStateMv1Flr(int time, Elevator lift) {
		lift.moveElevator();
		int currDirection = lift.getDirection();
		int currFloor = lift.getCurrFloor();
		if (lift.isMoving())
			return MV1FLR;
		if (lift.passToOffLoad() || floors[currFloor].callInDir(currDirection)) {
			return OPENDR;
		} else if (lift.isEmpty()) {
			if (!callPending()) {
				return STOP;
			} else {
				if (((currDirection > 0) &&  !anyCallsAbove(currFloor)) ||
					((currDirection < 0) && !anyCallsBelow(currFloor))) {
					lift.setDirection(-1*currDirection);
					return OPENDR;
				}
			}
		}
		return MV1FLR;
	}
	
	private int currStateOpenDr(int time, Elevator lift) {
		lift.openDoor();
		if (!lift.isDoorOpen()) {
			return OPENDR;
		} else if (lift.passToOffLoad()) {
			return OFFLD;
		} else {
			return BOARD;
		}
	}
	
	private int currStateOffLd(int time, Elevator lift) {

		int direction = lift.getDirection();
		int floor = lift.getCurrFloor();
		ArrayList<Passengers> passOffLoad = lift.offLoadPassengers();
		if (passOffLoad != null) {
			for (Passengers p : passOffLoad) {
				p.setTimeArrived(time);
				passSuccess.add(p);
				LOGGER.info("Time="+time+"   Arrived="+p.getNumber()+" Floor="+ (floor+1)
				+" passID=" + p.getId());				
			}
		}		
		if (!lift.offLoadDone()) 
			return OFFLD;
		else if (floors[floor].passToBoard(direction) != null) {
			return BOARD;
		} else if (lift.isEmpty() && (((direction>0) && !anyCallsAbove(floor)) ||
				   ((direction<0) && !anyCallsBelow(floor)))) {
			direction *= -1;
			if (floors[floor].passToBoard(direction) != null) {
				lift.setDirection(direction);
				return BOARD;
			}				
		}
		return CLOSEDR;
	}
	
	private void logGiveUp(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   GaveUp="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}

	private void logSkip(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Skip="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}
	
	private void logBoard(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Board="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}
	
	
			
	private int currStateBoard(int time, Elevator lift) {
		int dir = lift.getDirection();
		int currFloor = lift.getCurrFloor();
		Passengers p;
		int numPass = 0;
		boolean isCapacity = false;
		
		while (!isCapacity && (p=floors[currFloor].passToBoard(dir))!= null) {
			numPass = p.getNumber();
			if ((p.getTime()+p.getWaitTime()) < time) {
				logGiveUp(time,numPass,currFloor, dir, p.getId());
				gaveUp.add(p);
			} else if (lift.isCapacity(numPass)) {
				logSkip(time,numPass,currFloor, dir, p.getId());
				isCapacity = true;
			} else {
				numBoarded += numPass;
				p.setBoardTime(time);
				lift.addBoardedPassengers(p);
				logBoard(time,numPass,currFloor, dir, p.getId());				
			}
			if (!isCapacity) 
				floors[currFloor].pollQueueDir(dir);
			
		}
		boardingGrps = lift.calcBoardingGrps(numBoarded);
		lift.boardPassengers();
		if (lift.boardingDone(boardingGrps)) {
			queuesUpdated = true;
			return CLOSEDR;
		} else {
			return BOARD;
		}
		
	}
	
	private int currStateCloseDr(int time, Elevator lift) {
		numBoarded = 0;
		boardingGrps = 0;
		lift.clearNewPassengers();
		Passengers p = floors[lift.getCurrFloor()].passToBoard(lift.getDirection());
		lift.closeDoor();
		
		// somebody pressed the button or weren't able to board...
		if ((p != null) && (!p.isPolite())) {
			p.setPolite(true);
			return OPENDR;
		}

		if (lift.isDoorClosed()) {
			if (lift.isEmpty()) {
				if (!callPending()) {
					return STOP;
				} else if ((lift.getDirection() > 0) && anyCallsAbove(lift.getCurrFloor())) {
					return MV1FLR;
				} else if ((lift.getDirection() < 0) && anyCallsBelow(lift.getCurrFloor())) {
					return MV1FLR;
				} else {
					lift.setDirection(lift.getDirection() * -1);  // change direction
					return MV1FLR;
				}
			} else {
				return MV1FLR;
			}
		} 
		return CLOSEDR;
	}
	
	public int getElevatorState(Elevator lift) {
		return (lift.getCurrState());
	}
	
	public int getElevatorFloor(Elevator lift) {
		return (lift.getCurrFloor());
	}
	
	public int getElevatorDirection(Elevator lift) {
		return (lift.getDirection());
	}
	
	public int getElevatorPassengers(Elevator lift) {
		return (lift.getPassengers());
	}

	public boolean elevatorStateChanged(Elevator lift) {
		return (lift.stateChanged() || lift.floorChanged()); //);
	}
	
	public Elevator getElevator(int elevatorNum) {
		return elevators[elevatorNum];
	}
	
	public boolean passQueueIsEmpty() {
		return passQ.isEmpty();
	}
	
	public boolean elevatorIsStopped(int elevatorNum) {
		Elevator lift = getElevator(elevatorNum);
		return (lift.isStopped());
	}
	
	private String printState(int state) {
		String str = "";
		
		switch (state) {
			case STOP: 		str =  "STOP   "; break;
			case MVTOFLR: 	str =  "MVTOFLR"; break;
			case OPENDR:    str =  "OPENDR "; break;
			case CLOSEDR:	str =  "CLOSEDR"; break;
			case BOARD:		str =  "BOARD  "; break;
			case OFFLD:		str =  "OFFLD  "; break;
			case MV1FLR:	str =  "MV1FLR "; break;
		}
		return(str);
	}
	
	public void updateElevator(int time) {
		for (Elevator lift: elevators) {
			if (elevatorStateChanged(lift)) 
				LOGGER.info("Time="+time+"   Prev State: " + printState(lift.getPrevState()) + "   Curr State: "+printState(lift.getCurrState())
							+"   PrevFloor: "+(lift.getPrevFloor()+1) + "   CurrFloor: " + (lift.getCurrFloor()+1));
			switch (lift.getCurrState()) {
				case STOP: lift.updateCurrState(currStateStop(time,lift)); break;
				case MVTOFLR: lift.updateCurrState(currStateMvToFlr(time,lift)); break;
				case OPENDR: lift.updateCurrState(currStateOpenDr(time,lift)); break;
				case OFFLD: lift.updateCurrState(currStateOffLd(time,lift)); break;
				case BOARD: lift.updateCurrState(currStateBoard(time,lift)); break;
				case CLOSEDR: lift.updateCurrState(currStateCloseDr(time,lift)); break;
				case MV1FLR: lift.updateCurrState(currStateMv1Flr(time,lift)); break;
			}


		}
	}
	
	public void enableLogging() {
		LOGGER.setLevel(Level.INFO);
		LOGGER.info("CONFIG:   Capacity="+elevators[0].getCapacity()+"   Ticks-Floor="
				    +elevators[0].getTicksPerFloor()+"   Ticks-Door="+elevators[0].getTicksDoorOpenClose()
				    +"   Ticks-Passengers="+elevators[0].getPassPerTick());
	}
	
	public void closeLogs(int time) {
		if (LOGGER.getLevel() == Level.INFO) {
			LOGGER.info("Time="+time+"   Detected End of Simulation");
			fh.flush();
			fh.close();
		}
	}
	
	public void processPassengerData() {
		
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(passDataFile)));
			out.println("ID,Number,From,To,WaitToBoard,TotalTime");
			for (Passengers p : passSuccess) {
				String str = p.getId()+","+p.getNumber()+","+p.getFromFloor()+","+p.getToFloor()+","+
				             (p.getBoardTime() - p.getTime())+","+(p.getTimeArrived() - p.getTime());
				out.println(str);
			}
			for (Passengers p : gaveUp) {
				String str = p.getId()+","+p.getNumber()+","+p.getFromFloor()+","+p.getToFloor()+","+
				             p.getWaitTime()+",-1";
				out.println(str);
			}
			out.flush();
			out.close();
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
