package robots;

import automail.IMailDelivery;
import automail.Simulation;
import exceptions.FragileItemBrokenException;
import util.robotSetting.RobotType;
import strategies.IMailPool;

public class CarefulRobot extends Robot {
	
	public int step = 0;
	public CarefulRobot(IMailDelivery delivery, IMailPool mailPool) {
		super(delivery, mailPool);
		setConfig();
	}
	
	public void setConfig() {
		type = util.robotSetting.RobotType.Careful;
		tube = new StorageTube(util.robotSetting.CAREFUL_CAPACITY);
	}
	
	protected void moveTowards(int destination) throws FragileItemBrokenException {
		step = step + 1;
		if (step == 2) {
			if(current_floor < destination){
				current_floor = current_floor + 1;
			}
			else{
	          current_floor = current_floor - 1;
			}
          }
		  step = 0;
	   }
}
