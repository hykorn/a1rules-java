/*
 * Author: Darren Loh
 * History: 
 * 120712/DL -File creation. Code is quite messy as it needs to handle both relational or non-relational events. It will be cleaner if we separate them into 2 different classes in the future.
 * 121109/DL -Read from xml for eventcode, taskactionid , taskcause id 
 * 121214/DL -Split GenericRules code into common and actual caller
 */
package genericrules;

import java.util.ArrayList;

import tasks.GenericTask;

import com.actix.analytics.objects.NSM_EVENT;
import com.actix.analytics.objects.NSM_EVENTTYPE;
import com.actix.analytics.objects.NSM_PRIORITY;
import com.actix.analytics.objects.NSM_RELATIONALEVENT;
import com.actix.analytics.objects.NSM_STRING;
import com.actix.analytics.objects.NSM_TASK;
import com.actix.rules.flow.Publish;
import com.actix.rules.flow.nodes.Action;
import com.actix.rules.flow.nodes.Check;
import com.actix.rules.flow.nodes.FlowController;
import com.actix.rules.flow.nodes.FlowNode;
import com.actix.rules.flow.nodes.Loop;

import core.Branch;
import core.CheckPerformance;

@Publish
public class GenericRules extends CheckPerformance {

	private final static String EVENT_CODES = "Event Codes";
	
	//Debugging mode
	private static boolean isDebugMode = false;
	
	//this is not replacing actual Rule level 
	public static String elementLevel = "Sector";
	
	/**
	 * @param start
	 * @param elementName (Dummy events and tasks will be raised on this element) 
	 * 
	 * Desc:
	 * Generate 2 dummys events
	 * Generate 1 dummy task
	 */
	public static void raiseDummyEventTask(FlowController start, NSM_STRING elementName) {
		
		FlowController elementsToCheck = start.newCheck( ELEMENTNAME().IS(EQUALTO(elementName)) ).onTrue();
	
		NSM_EVENT myTestingEvent1 = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(elementLevel + "dummy_event 1"));
		
		NSM_EVENT myTestingEvent2 = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(elementLevel + "dummy_event 2"));
		
		elementsToCheck.newAction(RAISEEVENT(myTestingEvent1, "Dummy event 1 for testing purpose." ,PROPERTY(elementLevel, elementName)));
		
		Action RaiseEvent2 = elementsToCheck.newAction(RAISEEVENT(myTestingEvent2, "Dummy event 2 for testing purpose." ,PROPERTY(elementLevel, elementName)));
		
		NSM_TASK myTesingTask = TASK(TASKTYPE("Performance Alert", "Test_Task"));
		
		Action RaiseTask = RaiseEvent2.onComplete().newAction(RAISETASK(myTesingTask,LITERALSTRING("Dummy task for testing purpose"),PROPERTY(elementLevel, elementName)) );
		
		RaiseTask.onComplete().newAction(ADDEVENTSTOTASK(myTesingTask, myTestingEvent1));
		
		RaiseTask.onComplete().newAction(ADDEVENTSTOTASK(myTesingTask, myTestingEvent2));
		
		RaiseTask.onComplete().newAction(SETTASKPRIORITY(myTesingTask, NSM_PRIORITY.LOW()));
	}
	
	/**
	 * @param start
	 * @param elementName
	 * @return Filtered flowcontroller
	 * 
	 * Desc:
	 * Filter flowcontroller based on technology and vendor
	 * Flexible enough to adapt to different configuration from XML General technology and vendor tag
	 * 
	 */
	public static FlowController getFilteredFlow(KPIConf kpiList, FlowController start, NSM_STRING elementName) {
		
		FlowController elementToCheck;
		
		//filter based on both technology and vendor
		if( (kpiList.getTechnology().length() > 0) && (kpiList.getVendor().length() > 0) ) {
			
			elementToCheck = start.newCheck(LOWERCASE(elementName).IS(EQUALTO("none")).
					AND(IS(UPPERCASE(ELEMENTCATEGORY()), CONTAINS(kpiList.getTechnology()))).
					AND(IS(UPPERCASE(PARAMETERSTRING("Vendor")), CONTAINS(kpiList.getVendor()))).
					OR(ELEMENTNAME().IS(EQUALTO(elementName)))).onTrue();
		}
		//filter based on technology
		else if( (kpiList.getTechnology().length() > 0) && (kpiList.getVendor().length() == 0) ) {
			
			elementToCheck = start.newCheck(LOWERCASE(elementName).IS(EQUALTO("none")).
					AND(IS(UPPERCASE(ELEMENTCATEGORY()), CONTAINS(kpiList.getTechnology()))).
					OR(ELEMENTNAME().IS(EQUALTO(elementName)))).onTrue();
			
		}
		//filter based on vendor
		else if( (kpiList.getTechnology().length() == 0) && (kpiList.getVendor().length() > 0) ) {
			
			elementToCheck = start.newCheck(LOWERCASE(elementName).IS(EQUALTO("none")).
					AND(IS(UPPERCASE(PARAMETERSTRING("Vendor")), CONTAINS(kpiList.getVendor()))).
					OR(ELEMENTNAME().IS(EQUALTO(elementName)))).onTrue();
		} 
		//no filtering as both technology and vendor tag are empty
		else {
			
			elementToCheck = start.newCheck(LOWERCASE(elementName).IS(EQUALTO("none")).
					AND(IS(ELEMENTCATEGORY(), EQUALTO("GSM"))).
					OR(ELEMENTNAME().IS(EQUALTO(elementName)))).onTrue();		
		}
		
		return elementToCheck;
	}
	
	/**
	 * Desc:
	 * Call PopulatePMevents if Kpi or KPIRel is not empty. Actual event inititation is not done here.
	 */
	public static void populateAllPmEvent(KPIConf kpiList) {
		
		if(kpiList.getKpiCount() > 0) {
			
			populatePMEvents(kpiList, false);
		}
		
		if(kpiList.getKpiRelCount() > 0) {
			
			populatePMEvents(kpiList, true);	
		}	
	
	}
	
	/**
	 * @param isRel (whether to initiate non-relational or relational event)
	 * 
	 * Desc:
	 * Initiate event for pmRelEventList and pmEventList based on number of KPIs
	 */
	public static void populatePMEvents(KPIConf kpiList, boolean isRel){
		
		int totalKpi = 0;
		
		if(isRel) {
			
			totalKpi = kpiList.getKpiRel_list().length;
			
			kpiList.setPmRelEventList(new NSM_RELATIONALEVENT[totalKpi]);
			
						
		} else {
			
			totalKpi = kpiList.getKpi_list().length;
			
			kpiList.setPmEventList(new NSM_EVENT[totalKpi]);
			
		}
		
		for(int kpiCount = 0; kpiCount < totalKpi; kpiCount++)
		{
			if(isRel) {
				
				kpiList.getPmRelEventList()[kpiCount] = RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(kpiList.getKpiRel_list()[kpiCount].getEventCode()));
				
			}
			else {
				
				kpiList.getPmEventList()[kpiCount] = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(kpiList.getKpi_list()[kpiCount].getEventCode()));
			}
		}
	}
	
	/**
	 * @return Branch
	 * 
	 * Desc:
	 * Creating branch of checks
	 * There are 3 possible checks to construct the branch
	 * 1. Both relational and non-relational
	 * 2. Only relational 
	 * 3. Only non-relational
	 * As branch is continuous, special handling must be done to handle case 1 above.
	 */
	public static Branch createBranch(KPIConf kpiList) {
		
		Branch checkBranch = null; 
		
		//Always check non-relational KPI first. Add non-relational kpi check into branch
		if(kpiList.getKpiCount() > 0) {
			
			//populatePMEvents(false);
			
			checkBranch = addCheckIntoBranch(kpiList, false);
		}
		
		//Check relational kpi next. 
		if(kpiList.getKpiRelCount() > 0) {
			
			//populatePMEvents(true);	
			
			//checkBranch is not null. Means there is non-relational kpi added to the main branch already.
			if (null != checkBranch) {
				
				//Create a new branch of relational kpi check
				Branch checkRelBranch = addCheckIntoBranch(kpiList, true);
				
				//Add relational check branch into existing main branch
				checkBranch.getEndCheck().onComplete().addLoop(checkRelBranch.getBeginLoop());
				
				//set the end check from relational check branch
				checkBranch.setEndLoop(checkRelBranch.getEndLoop());
				
			} 
			//checkBranch is null. Means there is no non-relational kpi added to the main branch. No special handling is required
			else {
				
				checkBranch = addCheckIntoBranch(kpiList, true);
				
				//checkBranch.setEndLoop(checkRelBranch.getEndLoop());
			}
		}
		
		return checkBranch;
	}
	
	/**
	 * @param newStart
	 * @param checkBranch
	 * 
	 * Desc:
	 * Add Branch into flowcontroller
	 */
	public static void addBranchIntoFlow(KPIConf kpiList, FlowController newStart, Branch checkBranch) {
		
		//Non-relational KPI exists, so it will be at the top of the branch
		if( kpiList.getKpiCount() > 0) {
			
			newStart.addCheck(checkBranch.getBeginCheck());
			
		} 
		//Non-relational KPI does not exist. Relational KPI will be at the top of the branch 
		else if(kpiList.getKpiRelCount() > 0) {
			
			newStart.addLoop(checkBranch.getBeginLoop());
			
		}
	}
	
	/**
	 * @param isRel
	 * @return
	 * 
	 * Desc:
	 * Called by createBranch() method. 
	 * Create check and return it as a branch
	 */
	private static Branch addCheckIntoBranch(KPIConf kpiList, boolean isRel) {
		
		ArrayList<FlowNode> checkList = new ArrayList<FlowNode>();
		
		int totalKpi = isRel ? kpiList.getKpiRel_list().length : kpiList.getKpi_list().length; 
		
		addCheckToList(kpiList, totalKpi, checkList, isRel);
		
		linkAllCheck(checkList, isRel); 
		
		Branch branch = isRel ? 
				new Branch((Loop)checkList.get(0),(Loop)checkList.get(checkList.size()-1)) : 
				new Branch((Check) checkList.get(0), (Check) checkList.get(checkList.size()-1));
		
		return branch;
	}
	
	/**
	 * @param totalKpi
	 * @param checkList
	 * @param isRel
	 * 
	 * Desc:
	 * Called by addCheckIntoBranch()
	 * Add Check into ArrayList, checkList
	 * Does not link the checks together.
	 */
	private static void addCheckToList(KPIConf kpiList, int totalKpi, ArrayList<FlowNode> checkList, boolean isRel) {
		
		for(int kpiCount = 0; kpiCount < totalKpi; kpiCount++) {
			
			if(isRel){
					
					checkList.add(GenericCheck.getLoop(kpiList.getKpiRel_list()[kpiCount], kpiList.getWindowName(), elementLevel));
				
			} else {
	
					checkList.add(GenericCheck.getCheck(kpiList.getKpi_list()[kpiCount], kpiList.getWindowName(), elementLevel));
			
			}
		}
		
	}
	
	/**
	 * @param checkList
	 * @param isRel
	 * 
	 * Desc:
	 * Called by addCheckIntoBranch()
	 * This is to link all the checks (in checkList) together as we only use the top check to add into flowcontroller. 
	 * Without calling this method, only the first check in the ArrayList will be executed during flowcontroller
	 */
	private static void linkAllCheck(ArrayList<FlowNode> checkList, boolean isRel) {
		
		for(int branchCount = 0; branchCount < (checkList.size() - 1); branchCount++)
		{
			//if relational KPI, use Loop 
			if(isRel) {
				
				Loop currentLoop = (Loop) checkList.get(branchCount);
	
				Loop nextLoop = (Loop) checkList.get(branchCount+1);
				
				currentLoop.onComplete().addLoop(nextLoop);			
				
				checkList.set(branchCount, currentLoop);
				
			} 
			// non-relational KPI, use Check
			else {
				
				Check currentCheck = (Check) checkList.get(branchCount);
					
				Check nextCheck = (Check) checkList.get(branchCount+1);
				
				currentCheck.onComplete().addCheck(nextCheck);	
					
				checkList.set(branchCount, currentCheck);
			}	
		}	
	}
	
	/**
	 * @param checkBranch
	 * 
	 * Desc:
	 * Add task checks at end of Branch (which contains Event Checks currently)
	 */
	public static void addTaskIntoBranch(KPIConf kpiList, Branch checkBranch) {

		if( (kpiList.getKpiRelCount() > 0) ) {
			
			checkBranch.getEndLoop().onComplete().addCheck(raiseTasksIfEventExist(kpiList));	
			
		} else { 
				
			checkBranch.getEndCheck().onComplete().addCheck(raiseTasksIfEventExist(kpiList));		
		}
	}
		
	/**
	 * @return
	 * 
	 * Desc:
	 * Called by addTaskIntoBranch()
	 * Create Check to check if event is existed in A1 and add into Check[]
	 * Loop Check[] and if check is existed, then raise Action for this event
	 */
	private static Check raiseTasksIfEventExist(KPIConf kpiList) {
		
		Check[] checkEvent = null;
		
		Check[] checkRelEvent = null;
		
		Check firstCheck = null;
		
		//Create Check to check if event is existed in A1 and add into Check[]
		if(kpiList.getKpiCount() > 0) {
			checkEvent = new Check[kpiList.getKpiCount()];
			
			addCheckIfEventExisted(kpiList, checkEvent, false);
		} 
		
		if(kpiList.getKpiRelCount() > 0) {
			checkRelEvent = new Check[kpiList.getKpiRelCount()];
			
			addCheckIfEventExisted(kpiList, checkRelEvent, true);
		}
		
		//If check (event existed), then raised Action for the event
		//To raise action, need to cater for 3 possibilities to raise actions
		//1. Need to raise Action for only Non-REL KPI
		//2. Need to raise Action for only REL KPI
		//3. Need to raise Action for both Non-REL KPI & REL KPI (must be linked together)
		
		//Need to raise Action for only Non-REL KPI
		if( (kpiList.getKpiCount() > 0) && (0 == kpiList.getKpiRelCount())) {
			
			addActionIfEventExist(kpiList, checkEvent, false);
			
			firstCheck = checkEvent[0];
			
		//Need to raise Action for only REL KPI
		} else if ( (0 == kpiList.getKpiCount()) && (kpiList.getKpiRelCount() > 0)) {
			
			addActionIfEventExist(kpiList, checkRelEvent, true);
			
			firstCheck = checkRelEvent[0];
			
		//Need to raise Action for both Non-REL KPI & REL KPI (must be linked together)	
		} else if ( (kpiList.getKpiCount() > 0) && (kpiList.getKpiRelCount() > 0)) {
			
			addActionIfEventExist(kpiList, checkEvent, false);
			
			checkEvent[checkEvent.length -1].onComplete().addCheck(checkRelEvent[0]);
			
			addActionIfEventExist(kpiList, checkRelEvent, true);
			
			firstCheck = checkEvent[0];
		}
		
		return firstCheck;
	}
	
	/**
	 * @param checkEvent
	 * @param isRel
	 * 
	 * DESC:
	 * Create Check to check whether event existed in A1
	 * if REL, use SUM to check 
	 * if Non-REL, use normal EXIST
	 */
	private static void addCheckIfEventExisted(KPIConf kpiList, Check[] checkEvent, boolean isRel) {
		
		for (int eventExistCount =  0; eventExistCount < checkEvent.length; eventExistCount++ )
		{
			
			if (isRel) {
				
				checkEvent[eventExistCount] = new Check(SUM(CHECKEDVALUE(((NSM_RELATIONALEVENT) kpiList.getPmRelEventList()[eventExistCount]).EXISTS(),1,0)).OVER(NEIGHBORS()).IS(GREATERTHAN(0)));

			
			} else {
				
				checkEvent[eventExistCount] = new Check((kpiList.getPmEventList()[eventExistCount]).EXISTS());
			}
			
			if (isDebugMode) {
				
				KPI kpi = isRel ? kpiList.getKpiRel_list()[eventExistCount] : kpiList.getKpi_list()[eventExistCount];
				
				checkEvent[eventExistCount].onTrue().output(TOKENISEDSTRING("IsEventExisted = TRUE | KPI = '$1'", LITERALSTRING(kpi.getFullname()))); 
				
				checkEvent[eventExistCount].onFalse().output(TOKENISEDSTRING("IsEventExisted = FALSE | KPI = '$1'", LITERALSTRING(kpi.getFullname())));
			}
			
		}
		
	}
	
	/**
	 * @param checkEvent
	 * @param isRel
	 * 
	 * DESC:
	 * Called by raiseTasksIfEventExist()
	 * Check if event exists in A1, if yes Raise Task
	 */
	private static void addActionIfEventExist(KPIConf kpiList, Check[] checkEvent, boolean isRel) {
		
		for (int checkEventCount = 0; checkEventCount < checkEvent.length; checkEventCount++)
		{
			if (isDebugMode) {
				
				KPI kpi = isRel ? kpiList.getKpiRel_list()[checkEventCount] : kpiList.getKpi_list()[checkEventCount];
				
				checkEvent[checkEventCount].onTrue().output(TOKENISEDSTRING("Raise Task = TRUE | KPI = '$1'", LITERALSTRING(kpi.getFullname()))); 
				
				checkEvent[checkEventCount].onFalse().output(TOKENISEDSTRING("Raise Task = FALSE | KPI = '$1'", LITERALSTRING(kpi.getFullname())));
			}
			
			//if event exists in A1, add Action to raise Task
			checkEvent[checkEventCount].onTrue().addAction(raiseGenericTask(kpiList, checkEventCount, isRel));
			
			//Link all the checks together
			if (checkEventCount != (checkEvent.length-1) ) {
				
					checkEvent[checkEventCount].onComplete().addCheck(checkEvent[checkEventCount+1]);
			}
		}
	}
	

	/**
	 * @param kpiNumber
	 * @param isRel
	 * @return Action
	 * 
	 * DESC:
	 * Raise Action Task
	 */
	private static Action raiseGenericTask(KPIConf kpiList, int kpiNumber, boolean isRel) {
		
		KPI kpi = isRel ? kpiList.getKpiRel_list()[kpiNumber] :  kpiList.getKpi_list()[kpiNumber];
		
		GenericTask task = new GenericTask(kpi, elementLevel, kpiList.getTaskQName(), kpi.getCategory());
		
		String taskcause = kpi.getTaskCauseCode();
		
		String taskaction = kpi.getTaskActionCode();
		
		task.SetCause(taskcause);
		
		task.SetAction(taskaction);
		
		setGenericTaskPriority(kpi, task);
		
//		setOwnTaskCodeEvent(kpi, task, false);
		
		associateOwnTaskCodeEvent(kpi, task, isRel);
		
		associateCmEvent(kpiList, kpi, task);
		
		//associate events from REL and Non-REL
	
		associateOtherTaskCodeEvent(kpiList, task, false);
		
		associateOtherTaskCodeEvent(kpiList, task, true);
		
		return task.getRaiseTask();
	}
	
	/**
	 * @param kpi
	 * @param task
	 * 
	 * DESC:
	 * Set Task priority based on condition 
	 */
	private static void setGenericTaskPriority(KPI kpi, GenericTask task)
	{
		if('<' == kpi.getCondition() || 'l' == kpi.getCondition()){
			
			task.SetPriorityLower(kpi.getFullname(), NAMEDTHRESHOLD(kpi.getFullname().concat("_Critical")), NAMEDTHRESHOLD(kpi.getFullname().concat("_Major")));
			
		} else {
			
			task.SetPriority(kpi.getFullname(), NAMEDTHRESHOLD(kpi.getFullname().concat("_Critical")), NAMEDTHRESHOLD(kpi.getFullname().concat("_Major")));
			
		 }
	}
	
	/**
	 * @param kpi
	 * @param task
	 * 
	 * Associate CM events
	 */
	private static void associateCmEvent(KPIConf kpiList, KPI kpi, GenericTask task)
	{
		if ( !kpiList.isNullCM()) {
			for (int cmCount = 0; cmCount < kpiList.getCm_List().length; cmCount++)
			{
				task.AssociateEvent(EVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(kpiList.getCm_List()[cmCount])));
			}
		} 
		
	}
	
	/**
	 * @param kpi
	 * @param task
	 * @param isRel
	 * 
	 * DESC:
	 * Associate current/own event to task
	 * No need to check whether event exists or not. It is already exist
	 */
	private static void associateOwnTaskCodeEvent(KPI kpi, GenericTask task, boolean isRel)
	{
		if (isRel) {
			
			task.AssociateRelationalEvent(RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(kpi.getEventCode())),NEIGHBORS());
			
		} else {
			
			task.AssociateEvent(EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(kpi.getEventCode())));
			
		}
	}
	
	/**
	 * @param task
	 * @param isRel
	 * 
	 * DESC:
	 * Associate events from other KPIs
	 * Need to check whether events from other KPIs exist or not. Will be done by appendEventCode() method
	 */
	private static void associateOtherTaskCodeEvent(KPIConf kpiList, GenericTask task, boolean isRel)
	{
		String shortcode = null;
		
		int checkKpiCount = isRel ? kpiList.getKpiRelCount() : kpiList.getKpiCount();
		
		for (int checkEventCount = 0; checkEventCount < checkKpiCount; checkEventCount++)
		{
			shortcode = "";
			
			if(isRel) {
				
			 shortcode = kpiList.getKpiRel_list()[checkEventCount].getEventCode();
			 
			} else {
				
			 shortcode = kpiList.getKpi_list()[checkEventCount].getEventCode();
			 
			}
			
			task.getRaiseTask().onComplete().addCheck(appendEventCodeIfEventExist(task,
					EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(shortcode)), 
					shortcode));
		}
	
	}
	
	/**
	 * @param task
	 * @param event
	 * @param shortcode
	 * @return
	 * 
	 * DESC:
	 * Called by associateOtherTaskCodeEvent()
	 * Check whether the rest of the events exist
	 * If exist, then append the event code to the EVENT CODE property of task
	 */
	private static Check appendEventCodeIfEventExist(GenericTask task, NSM_EVENT event, String shortcode) {
	
		Check eventExists = new Check(event.EXISTS());
		
		Check isEventAdded = eventExists.onTrue().newCheck(task.getTask().PROPERTYSTRING(EVENT_CODES).IS(CONTAINS(shortcode)));
		
		isEventAdded.onFalse().newAction(
				SETTASKPROPERTY(
						task.getTask(), 
						PROPERTY (
								EVENT_CODES,
								CHECKEDSTRING(
										STRINGLENGTH(task.getTask().PROPERTYSTRING(EVENT_CODES)).IS(GREATERTHAN(1)),
										CONCATENATE(task.getTask().PROPERTYSTRING(EVENT_CODES), shortcode).USING(","),
										shortcode
								)
						)
				)
		);
		return eventExists;
	}
}

