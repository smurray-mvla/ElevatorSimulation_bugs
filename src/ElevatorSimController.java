import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ElevatorSimController {
	private final static boolean UP = true;
	private final static boolean DOWN = false;
	private final int NUM_FLOORS;
	private final int NUM_ELEVATORS;
	private int numFloors;
	private int numElevators;
	private int capacity;
	private int floorTicks;
	private int doorTicks;
	private int tickPassengers;
	private String testfile;
	private String logfile;
	
	private ElevatorSimulation gui;
	private Building building;
	private int stepCnt = 0;
		
	public ElevatorSimController(ElevatorSimulation gui) {
		this.gui = gui;	
		configSimulation("ElevatorSimConfig.csv");
		NUM_FLOORS = numFloors;
		NUM_ELEVATORS = numElevators;
		logfile = testfile.replaceAll(".csv", ".log");
		building = new Building(NUM_FLOORS,NUM_ELEVATORS,logfile);
		building.configElevators(capacity,floorTicks,doorTicks,tickPassengers);
		initializePassengerData(testfile);
	}
	
	public int getNumFloors() {
		return NUM_FLOORS;
	}
	
	public int getNumElevators() {
		return NUM_ELEVATORS;
	}
	
	private void configSimulation(String filename) {
		try ( BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine())!= null) {
				String[] values = line.split(",");
				if (values[0].equals("numFloors")) {
					numFloors = Integer.parseInt(values[1]);
				} else if (values[0].equals("numElevators")) {
					numElevators = Integer.parseInt(values[1]);
				} else if (values[0].equals("passCSV")) {
					testfile = values[1];
				} else if (values[0].equals("capacity")) {
					capacity = Integer.parseInt(values[1]);
				} else if (values[0].equals("floorTicks")) {
					floorTicks = Integer.parseInt(values[1]);
				} else if (values[0].equals("doorTicks")) {
					doorTicks = Integer.parseInt(values[1]);
				} else if (values[0].equals("tickPassengers")) {
					tickPassengers = Integer.parseInt(values[1]);
				}
			}
			br.close();
		} catch (IOException e) { 
			System.err.println("Error in reading file: "+filename);
			e.printStackTrace();
		}
	}
	
	private void initializePassengerData(String filename) {
		int time=0, numPass=0,fromFloor=0, toFloor=0;
		boolean polite = true;
		int wait = 1000;
		boolean firstLine = false;
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			br.readLine();
			while ((line = br.readLine())!= null) {
				if (firstLine) {
					firstLine = false;
					continue;
				}
				String[] values = line.split(",");
				for (int i = 0; i < values.length; i++) {
					switch (i) {
						case 0 : time      = Integer.parseInt(values[i]); break;
						case 1 : numPass   = Integer.parseInt(values[i]); break;
						case 2 : fromFloor   = Integer.parseInt(values[i]); break;
						case 3 : toFloor  = Integer.parseInt(values[i]); break;
						case 5 : wait      = Integer.parseInt(values[i]); break;
						case 4 : polite = "TRUE".equalsIgnoreCase(values[i]); break;
					}
				}
				// only polite mode for now...
				building.addPassengersToQueue(time,numPass,fromFloor,toFloor,polite,wait);	
						}
		} catch (IOException e) { 
			System.err.println("Error in reading file: "+filename);
			e.printStackTrace();
		}
	}
	
	public void enableLogging() {
		building.enableLogging();
	}
	
	private void guiUpdateElevator() {
		gui.updateElevatorState(building.getElevatorState(building.getElevator(0)),
	            building.getElevatorFloor(building.getElevator(0)),
	            building.getElevatorDirection(building.getElevator(0)),
	            building.getElevatorPassengers(building.getElevator(0)));		
	}
	
	private void guiUpdateElevatorPassengers() {
		gui.updateElevatorPassengers(building.getElevatorPassengers(building.getElevator(0)));
	}
	
	private void guiUpdateFloorQueues() {
		for (int i = 0; i< NUM_FLOORS ; i++) {
			gui.updateFloorQueue(i,UP,building.strFloorQueue(i,UP))	;
			gui.updateFloorQueue(i,DOWN,building.strFloorQueue(i,DOWN))	;
		}
	}
	
 	public void stepSim() {
		stepCnt++;
		gui.updateTime(stepCnt);
		if (building.getQueuesUpdated() || building.elevatorStateChanged(building.getElevator(0))) {
			guiUpdateFloorQueues();
			guiUpdateElevator();
			//building.clearQueuesUpdated();
		}

		if (building.passQueueIsEmpty() && building.elevatorIsStopped(0)) {
			building.closeLogs(stepCnt);
			building.processPassengerData();
			gui.endSimulation();
		}
		
		building.checkPassengerQueue(stepCnt);
		building.updateElevator(stepCnt);

		if (building.getQueuesUpdated()) {
			guiUpdateFloorQueues();
			guiUpdateElevatorPassengers();
			building.clearQueuesUpdated();
		}
		
	}

}
