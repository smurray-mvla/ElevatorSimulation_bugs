import java.util.logging.Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ElevatorSimulation extends Application {
	private ElevatorSimController controller;
	private final int NUM_FLOORS;
	private final int NUM_ELEVATORS;
	private int currFloor = 0;
	private int passengers = 0;
	private int time = 0;
	private boolean up = true;
	private final static int STOP = 0;
	private final static int MVTOFLR = 1;
	private final static int OPENDR = 2;
	private final static int OFFLD = 3;
	private final static int BOARD = 4;
	private final static int CLOSEDR = 5;
	private final static int MV1FLR = 6;
	private boolean logEnabled = false;

	private Polygon t1, t2, t3, t4;
	private Circle c;
	private GridPane gp;
	private StackPane sp,ep;
	private TextField tfNumCycles;
	private Label elPass;
	private Label timeLabel;
	private Label[] upQueue = new Label[6];
	private Label[] downQueue = new Label[6];
	private VBox[] floorQueue = new VBox[6];
	private Timeline t;
	
	public ElevatorSimulation() {
		// TODO Auto-generated constructor stub
		controller = new ElevatorSimController(this);
		NUM_FLOORS = controller.getNumFloors();
		NUM_ELEVATORS = controller.getNumElevators();
		currFloor = 0;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane pane = new BorderPane();

		Font lblFont = Font.font("Arial",FontWeight.BOLD,16);
		t1 = new Polygon();
		t1.getPoints().addAll(5.0,20.0,25.0,20.0,15.0,20-10*Math.pow(3,0.5));
		t1.setStroke(Color.RED);
		t1.setStrokeWidth(2);
		t1.setFill(Color.RED);
		t2 = new Polygon();
		t2.getPoints().addAll(t1.getPoints());
		t2.setRotate(90);
		t2.setStroke(Color.CYAN);
		t2.setStrokeWidth(2);
		t2.setFill(Color.CYAN);
		t3 = new Polygon();
		t3.getPoints().addAll(t1.getPoints());
		t3.setRotate(180);
		t3.setStroke(Color.RED);
		t3.setStrokeWidth(2);
		t3.setFill(Color.RED);
		t4 = new Polygon();
		t4.getPoints().addAll(t1.getPoints());
		t4.setRotate(270);
		t4.setStroke(Color.CYAN);
		t4.setStrokeWidth(2);
		t4.setFill(Color.CYAN);
		c = new Circle(10,10,10);
		c.setStroke(Color.BLACK);
		c.setFill(Color.BLACK);
		
		sp = new StackPane();
		sp.getChildren().addAll(t1,t2,t3,t4,c);
		
		elPass = createNewLabel("0",lblFont,40);
		elPass.setPrefWidth(15);
		ep = new StackPane();
		ep.getChildren().add(elPass);
		gp = new GridPane();
		gp.setVgap(15);
		gp.setHgap(15);
		gp.setGridLinesVisible(false);
		
		timeLabel = createNewLabel("Time: 0",lblFont,100);
		Label fl1 = createNewLabel("1st",lblFont,40);
		Label fl2 = createNewLabel("2nd",lblFont,40);
		Label fl3 = createNewLabel("3rd",lblFont,40);
		Label fl4 = createNewLabel("4th",lblFont,40);
		Label fl5 = createNewLabel("5th",lblFont,40);
		Label fl6 = createNewLabel("6th",lblFont,40);

		gp.add(timeLabel,3,0);
		gp.add(fl1,1,6);
		gp.add(fl2,1,5);
		gp.add(fl3,1,4);
		gp.add(fl4,1,3);
		gp.add(fl5,1,2);
		gp.add(fl6,1,1);
		
		for (int i = 0; i < 6; i++) {
			upQueue[i] = createNewLabel("Up: ",lblFont,100);
			downQueue[i] = createNewLabel("Dn: ",lblFont,100);
			floorQueue[i] = new VBox(10); 
			floorQueue[i].getChildren().addAll(upQueue[i],downQueue[i]);
			gp.add(floorQueue[i],3,(6-i));
		}
		
		
		gp.add(sp,0,6);
		gp.add(ep,2,6);
		pane.setCenter(gp);
		
		HBox btnBox = new HBox(15);
		Button step = new Button("StepSim");
		Button stepN = new Button("Step N ");
		Button run = new Button("Run");
		Button log = new Button("Log");

		log.setOnAction(e->enableLogging(log));
		tfNumCycles = new TextField();
		tfNumCycles.setPrefWidth(100);
		step.setOnAction(e -> controller.stepSim());
		stepN.setOnAction(e -> stepNCycles());
		run.setOnAction(e -> {t.setCycleCount(Animation.INDEFINITE); t.play();});
		
        btnBox.getChildren().addAll(step,stepN,tfNumCycles,run,log);
        pane.setBottom(btnBox);  
 		handleStop();	

 		t = new Timeline(new KeyFrame(
				Duration.millis(50),
				ae -> controller.stepSim()));
		t.setCycleCount(Animation.INDEFINITE);

 		Scene scene = new Scene(pane,400,500);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Elevator Simulation");
		primaryStage.show();

	}
	
	private void enableLogging(Button btn) {
		System.out.println("Logging is ENABLED");
		btn.setStyle("-fx-background-color: Cyan");
		controller.enableLogging();
	}
	
	public void endSimulation() {
		t.pause();
	}
	
	private void stepNCycles() {
		if (!tfNumCycles.getText().isEmpty()) {
			t.setCycleCount(Integer.parseInt(tfNumCycles.getText()));
			t.play();
		}
	}
	
	
	private Label createNewLabel(String str, Font font, int width) {
		Label newLbl = new Label(str);
		newLbl.setFont(font);
		newLbl.setMinWidth(width);
		return newLbl;
	}

	public void updateFloorQueue(int floor, boolean up, String queue) {
		if (up) {
			upQueue[floor].setText("Up: "+queue);
		} else {
			downQueue[floor].setText("Dn: "+queue);
		}
	}

	public void handleStop() {
		c.setFill(Color.BLACK);
		c.setVisible(true);
		t1.setVisible(false);
		t2.setVisible(false);
		t3.setVisible(false);
		t4.setVisible(false);
	}
	
	public void handleBoard() {
		c.setVisible(false);
		t1.setVisible(false);
		t2.setVisible(false);
		t3.setVisible(false);
		t4.setVisible(true);
	}
	
	public void handleOffLoad() {
		c.setVisible(false);
		t1.setVisible(false);
		t2.setVisible(true);
		t3.setVisible(false);
		t4.setVisible(false);
	}

	public void handleOpenDoor() {
		c.setFill(Color.TRANSPARENT);
		c.setVisible(true);
		t1.setVisible(false);
		t2.setVisible(false);
		t3.setVisible(false);
		t4.setVisible(false);
		gp.getChildren().removeAll(sp);
		gp.getChildren().removeAll(ep);
		gp.add(sp,0,(6 - (currFloor)));
		gp.add(ep,2,(6 - (currFloor)));
	}

	public void handleCloseDoor() {
		c.setFill(Color.RED);
		c.setVisible(true);
		t1.setVisible(false);
		t2.setVisible(false);
		t3.setVisible(false);
		t4.setVisible(false);
	}

	public void handleMove() {
		c.setVisible(false);
		t1.setVisible(up);
		t2.setVisible(false);
		t3.setVisible(!up);
		t4.setVisible(false);
		gp.getChildren().removeAll(sp);
		gp.getChildren().removeAll(ep);
		gp.add(sp,0,(6 - (currFloor)));
		gp.add(ep,2,(6 - (currFloor)));
	}
	
	public void updateTime(int time) {
		this.time = time;
		timeLabel.setText("Time: "+Integer.toString(time));
	}
	
	public void updateElevatorState(int currState, int currFloor, int direction, int passengers) {
		this.currFloor = currFloor;
		up = (direction > 0);
		this.passengers = passengers;
		elPass.setText(Integer.toString(passengers));
		
		switch (currState) {
			case STOP : handleStop(); break;
			case MVTOFLR : handleMove(); break;
			case MV1FLR : handleMove(); break;
			case OPENDR : handleOpenDoor(); break;
			case CLOSEDR : handleCloseDoor(); break;
			case BOARD : handleBoard(); break;
			case OFFLD : handleOffLoad(); break;
		}
		
	}
	
	public void updateElevatorPassengers(int passengers) {
		this.passengers = passengers;
	}
	
	public static void main (String[] args) {
		Application.launch(args);
	}

}
