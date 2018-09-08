package strategies;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import exceptions.FragileItemBrokenException;
import exceptions.TubeFullException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import java.util.*;
import mailItems.*;
import strategies.*;
import util.*;
import util.robotSetting.RobotType;
import robots.*;

/** 
 * @Author : Ziang Chen, The University of Melbourne
 * SWEN 30006 SEM2 - 2018 , PartA - Mailbot Blues
 * This class implements the strategy of selecting the mails from mail pool in order to achieve optimal efficiency for delivery
 * The strategy following 3 principles:
 * 1. Trying making sure the available highest priority first delivered 
 * 2. Avoid thirsty, earliest deliver time has high priority
 * 3. Optimize the mails selections for weak robot 
**/

public class MyMailPool implements IMailPool {
	
	/**
	* @DataStructures
	* Each robot has 2 states,  UNAVAILIABLE means returning and delivering  
	* States are linked to robot by key and value, Hashmap achieves O(1) data access
	* Two types of mails are stored in two different list
	* The lists provide the similar function as priority queue as they will be sorted following the principle
	*/
	public enum RobotState { WAITING, UNAVAILIABLE }
	Map<Robot, RobotState> robots = new HashMap<Robot,RobotState>(); 
	private List <MailItem> nonPriorityPool = new ArrayList <MailItem>();
	private List <MailItem> PriorityPool = new ArrayList <MailItem>();
	
	/**
	* @Principles
	* The comparator that implements principle 1 and 2
	* Applied when sorting Lists
	*/
	private class priorityComparator implements Comparator<MailItem>{
		@Override
		public int compare(MailItem o1, MailItem o2) {
			if(o1 instanceof PriorityMailItem && o2 instanceof  PriorityMailItem ) {
				PriorityMailItem po1 = (PriorityMailItem) o1;
				PriorityMailItem po2 = (PriorityMailItem) o2;
				if (po2.getPriorityLevel() != po1.getPriorityLevel()) {
					return po2.getPriorityLevel() - po1.getPriorityLevel();
				}
			}
			return o1.getArrivalTime() - o2.getArrivalTime();			
		}
	}
	
	/**
	* Add mails to the pool
	* Sort mails when new mails come in, which implements that high priority and earliest mails can be retrieved first 
	*/
	@Override
	public void addToPool(MailItem mailItem) {
		// TODO Auto-generated method stuba
		if(mailItem instanceof PriorityMailItem) {
			PriorityPool.add(mailItem);
			Collections.sort(PriorityPool, new priorityComparator());
			
		}else {
			nonPriorityPool.add(mailItem);
			Collections.sort(nonPriorityPool, new priorityComparator());
		}
	}
	
	/**
	* Repeatedly check the available robot by check value of given
	* If there are available robots 
	*/
	@Override
	public void step() {
		// TODO Auto-generated method stub
		for (Robot currRobot : robots.keySet() ) {
			if(robots.get(currRobot) == RobotState.WAITING) {
				fillStorageTube(currRobot);
			}
		}
	}
	
	/**
	* First select the items, which composed a list
	* Add item to the Tube 
	* @param robot
	*/
	private void fillStorageTube(Robot robot) {
		
		List <MailItem> mailToTube = popFromPool(robot.getRobotType() == RobotType.Standard);
		StorageTube tube = robot.getTube();
		for(MailItem mailitem : mailToTube) {
			try {
				try {
					tube.addItem(mailitem);
				} catch (FragileItemBrokenException e) {
					e.printStackTrace();
				}
			} catch (TubeFullException e) {
				e.printStackTrace();
			}
		}
		robot.dispatch();
	}
	
	/**
	* This function returns the result to fill the robot storage tube
	* If the robot is strong and the total number in the pool is no larger than 4, directly pop all of them
	* Otherwise, first select items from priority mails and check if the number of size is already 4
	* If it is, put the items into tube
	* If its not, select items from non-priority mails to fill the rest, re-compose the result with the previous result 
	* @param isStrong
	* @return opitmalResult
	*/
	public List <MailItem> popFromPool(boolean isStrong){
		List <MailItem> optimalResult = new ArrayList <MailItem> ();
	
		if(nonPriorityPool.size() + PriorityPool.size() <= 4 && isStrong) {
			optimalResult.addAll(nonPriorityPool);
			optimalResult.addAll(PriorityPool);
			nonPriorityPool.clear();
			PriorityPool.clear();
			return optimalResult;
		}
		// first select from priority mails
		List <MailItem> priorityMails = selectPriorityMail(isStrong);
		optimalResult.addAll(priorityMails);
		if(optimalResult.size() < 4) {
			optimalResult.clear();
			// then select from non-priority mails
			optimalResult.addAll(selectNonePriorityMail(isStrong, priorityMails));
		}
 		return optimalResult;
	}
	
	/**
	* As the pool is sorted, the items at front have more priority
	* The function selects the items from the front of priority mail pool 
	* @param isStrong
	* @return
	*/
	public List <MailItem> selectPriorityMail(boolean isStrong){
		List <MailItem> result = new ArrayList <MailItem> (); 
		
		for (MailItem mailitem : PriorityPool) {
			// There are still rooms if the result size is less than 4, 
			if(mailitem instanceof PriorityMailItem && result.size() < 4) {
				// check if the robot is the strong type, if not, check the weight after adding the item 
				if(isStrong) {
					result.add(mailitem);
				// If the total weight is larger than 2000 for a weak robot, pass this item.
				}else if (mailitem.getWeight() <= 2000){
					result.add(mailitem);
				}
			}
			if(result.size() == 4) {
				break;
			}
		}
		// remove the selected items to avoid duplicate delivery
		PriorityPool.removeAll(result);
		return result;
	}
	
	/**
	* Select from the none priority mail pool
	* Select is from front to end, to make sure the the items that arrives early can be delivered quickly
	* @param isStrong
	* @param priorityMails
	* @return
	*/
	public List <MailItem> selectNonePriorityMail(boolean isStrong, List <MailItem> priorityMails){
		List <MailItem> result = new ArrayList <MailItem> (); 
		result.addAll(priorityMails);
		
		for (MailItem mailitem : nonPriorityPool) {
			if (result.size() < 4) {
				if(isStrong) {
					result.add(mailitem);
				// If the total weight is larger than 2000 for a weak robot, pass this item.
				}else if (mailitem.getWeight() <= 2000) {
					result.add(mailitem);
				}
			}
			if(result.size() == 4) {
				break;
			}
		}
		nonPriorityPool.removeAll(result);
		return result;
	}
	/**
	* Select the given robot object and change its state as WAITING
	* Access time O(1)
	*/
	@Override
	public void registerWaiting(Robot robot) {
		robots.put(robot, RobotState.WAITING);
	}

	/**
	* Select the given robot object and change its state as UNAVAILIABLE
	* Access time O(1)
	*/
	@Override
	public void deregisterWaiting(Robot robot) {
		robots.put(robot, RobotState.UNAVAILIABLE);
	}
}