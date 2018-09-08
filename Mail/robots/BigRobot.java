package robots;

import automail.IMailDelivery;
import automail.Simulation;
import util.robotSetting;
import strategies.IMailPool;

public class BigRobot extends Robot{

	public BigRobot(IMailDelivery delivery, IMailPool mailPool) {
		super(delivery, mailPool);
		setConfig();
	}
	
	public void setConfig() {
		type = util.robotSetting.RobotType.Big;
		tube = new StorageTube(util.robotSetting.BIG_CAPACITY);
	}

}
