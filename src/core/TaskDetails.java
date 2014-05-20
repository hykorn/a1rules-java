package core;

import com.actix.analytics.objects.*;
import com.actix.analytics.objects.NSM_EVENT;
import com.actix.analytics.objects.NSM_TASK;
import com.actix.analytics.objects.NSM_STRING;
import com.actix.analytics.objects.NSM_PROPERTY;
import com.actix.analytics.objects.NSM_RAISETASK;
import com.actix.rules.flow.nodes.Action;
import com.actix.rules.flow.nodes.Check;
import com.actix.rules.flow.nodes.FlowController;

public class TaskDetails extends ObjectConsumer
{	
	
	// Function to raise a technology specific task with a specified task type and task code
	// Note that the system is set up to trigger tasks based on Performance Events and associates any Configuration Events found
	// NB: Triggering based on Configuration Events is not supported in this release
	
	// Each performance check function requests a task to be raised and the current event to be associated
	// Each configuration check function requests that the current event be associated to the task
	// These requests are queued and the task is raised once all events have been generated at the end of the rule flow
	
	protected NSM_TASKBASE task;
	protected String taskCode;

	protected Action raiseTask = null;
	
	public String getTaskCode() {
		return taskCode;
	}

	public NSM_TASKBASE getTask() {
		return task;
	}

	public Action getRaiseTask() {
		return raiseTask;
	}
	
	public TaskDetails(String taskType, String taskCode)
	{
		// Function to define the task details and the task flow
		
		// Define the current task
		this.task = TASK(TASKTYPE(taskType, taskCode));
		this.taskCode = taskCode;
		
		// Define a new action to raise a task on the parent controller
		this.raiseTask = new Action(
					RAISETASK(task,TOKENISEDSTRING("$1 on $2", NAMEDSTRING(taskCode + "_Description"), ELEMENTNAME()),
						PROPERTY("Element", ELEMENTNAME()),
						PROPERTY("Technology", ELEMENTCATEGORY()),
						//PROPERTY("Level", "Controller"),					
						//PROPERTY("Region", PARENTNAME("Region")),
						//PROPERTY("Market", PARENTNAME("Market")),
						//PROPERTY("Cluster", PARENTNAME("Cluster")),
						PROPERTY("Task Code", taskCode)
					)
			);	
		
		//Set the task queue
		raiseTask.onComplete().newAction(SETTASKQUEUE(task, PARAMETERSTRING("Market")));
	}
	
	public TaskDetails(String taskType, String taskCode, String Parameter, NSM_VARIABLE oldValue, NSM_VARIABLE newValue)
	{
		// Function to define the task details and the task flow
		
		// Define the current task
		this.task = TASK(TASKTYPE(taskType, taskCode));
		this.taskCode = taskCode;
		
		// Define a new action to raise a task on the current sector
		this.raiseTask = new Action(
				RAISETASK(task,
					TOKENISEDSTRING("$1 on $2", TOKENISEDSTRING("$1 change found",Parameter), ELEMENTNAME()),
					PROPERTY("Element", ELEMENTNAME()),
					PROPERTY("Technology", ELEMENTCATEGORY()),
					PROPERTY("Level", "Sector"),
					PROPERTY("Previous Value", oldValue),
					PROPERTY("Current Value", newValue),
					PROPERTY("Region", PARAMETERSTRING("Region")),
					//PROPERTY("Market", PARAMETERSTRING("Market")),
					//PROPERTY("Cluster", PARAMETERSTRING("Cluster")),
					//PROPERTY("Channel", PARAMETERVALUE("BCCH")),
					//PROPERTY("Channel", PARAMETERVALUE("UARFCN")),
					//PROPERTY("Controller", PARENTNAME("Controller")),
					PROPERTY("Task Code", taskCode)
				)
			);	
		
		//Set the task queue
		raiseTask.onComplete().newAction(SETTASKQUEUE(task, PARAMETERSTRING("Market")));
		
	}
	
	public void RaiseTaskOn(FlowController flow)
	{
		// Function used by the performance check functions to request a task to be raised
		// Note that even though raise task is called on multiple occasions, the task is only generated once (unless different task codes are provided)
		
		//Attach the task flow to the current flow => Raise a task
		flow.addAction(raiseTask);
	}

	public void AssociateEvent(NSM_EVENT event)
	{
		// Function used by the performance check functions to associate an event to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			//Associate the specified event
			raiseTask.onComplete().newAction(ADDEVENTTOTASK(task, event));
		}
	}

	public void AssociateConfigurationEvent(String code)
	{
		// Function used by the performance check functions to associate an event to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			NSM_EVENT event = EVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(code));
			
			//Associate the specified event
			raiseTask.onComplete().newAction(ADDEVENTTOTASK(task, event));
		}
	}

	public void AssociatePerformanceEvent(String code)
	{
		// Function used by the performance check functions to associate an event to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			NSM_EVENT event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(code));
			
			//Associate the specified event
			raiseTask.onComplete().newAction(ADDEVENTTOTASK(task, event));
		}
	}
	
	public void AssociateRelationalEvent(NSM_RELATIONALEVENT event, NSM_RELATIONSHIP relationship)
	{
		// Function used by the performance check functions to associate a relational event to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			//Associate the specified relational event
			raiseTask.onComplete().newAction(ADDEVENTTOTASK(task, event).OVER(relationship));
		}
	}

	public void AssociateRelationalEvent(NSM_RELATIONALEVENT event)
	{
		// Function used by the performance check functions to associate a relational event to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			//Associate the specified relational event
			raiseTask.onComplete().newAction(ADDEVENTTOTASK(task, event));
		}
	}

	public void AssociateRelationalEvent(NSM_EVENT event, NSM_RELATIONSHIP relationship)
	{
		// Function used by the performance check functions to associate a relational event to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			//Associate the specified relational event
			raiseTask.onComplete().newAction(ADDEVENTTOTASK(task, event).OVER(relationship));
		}
	}

	public void AssociateRelationalPerformanceEvent(String code, NSM_RELATIONSHIP relationship)
	{
		// Function used by the performance check functions to associate a relational event to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			NSM_EVENT event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(code));
			
			//Associate the specified relational event
			raiseTask.onComplete().newAction(ADDEVENTTOTASK(task, event).OVER(relationship));
		}
	}
	
	public void AssociatePerformanceHintEvent(String code) {

		AssociatePerformanceEvent(code);
		AssociateRelationalPerformanceEvent(code, NEIGHBORS());
	
	}
	
	public void SetPriority(Check check)
	{
		// Function used by the performance check functions to set the priority of a task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			//Associate the specified event
			raiseTask.onComplete().addCheck(check);
		}
		
	}

	public void SetCause(String Cause)
	{
		// Function used by the performance check functions to add a property to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			raiseTask.onComplete().newAction(SETTASKDIAGNOSIS(task, Cause));
		}
	}
	
	public void SetAction(String Action)
	{
		// Function used by the performance check functions to add a property to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			raiseTask.onComplete().newAction(SETTASKACTION(task, Action));
		}
	}
	
	public void SetPriority(NSM_PRIORITY Priority)
	{
		// Function used by the performance check functions to add a property to the task
		
		// Check that the task exists
		if (raiseTask != null)
		{
			raiseTask.onComplete().newAction(SETTASKPRIORITY(task,Priority));
		}
	}
	
	public void SetPriority(String priority)
	{
		// Function used by the performance check functions to set the priority of a task
		NSM_PRIORITY taskPriority = null;
		
		if (priority.compareToIgnoreCase("HIGH") == 0) {
			taskPriority = NSM_TASKPRIORITY.HIGH();
		} else if (priority.compareToIgnoreCase("MEDIUM") == 0) {
			taskPriority = NSM_TASKPRIORITY.MEDIUM();
		} else {
			taskPriority = NSM_TASKPRIORITY.LOW();			
		}

		
		// Check that the task exists
		if (raiseTask != null)
		{
			raiseTask.onComplete().newAction(SETTASKPRIORITY(task, CHECKEDPRIORITY(PARAMETERSTRING("VIP").IS(NOTBLANK()), NSM_TASKPRIORITY.HIGH(), taskPriority)));
		}
		
	}

	public void SetPriority(String kpi, double high, double medium)
	{
		raiseTask.onComplete().newAction(
				SETTASKPRIORITY(task,	
						SWITCHEDPRIORITY(
							METRICVALUE(kpi), 
							GREATERTHAN(high).THEN(NSM_TASKPRIORITY.HIGH()),
							LESSTHAN(medium).THEN(NSM_TASKPRIORITY.LOW()),
							DEFAULT(NSM_TASKPRIORITY.MEDIUM())
					)
				)
		);
	}
	
	public void SetPriority(String kpi, NSM_VALUE high, NSM_VALUE medium)
	{
		raiseTask.onComplete().newAction(
				SETTASKPRIORITY(task,	
						SWITCHEDPRIORITY(
							METRICVALUE(kpi), 
							GREATERTHAN(high).THEN(NSM_TASKPRIORITY.HIGH()),
							LESSTHAN(medium).THEN(NSM_TASKPRIORITY.LOW()),
							DEFAULT(NSM_TASKPRIORITY.MEDIUM())
					)
				)
		);
	}
	
	public Action getPriority(String kpi, double high, double medium)
	{
		return	new Action(SETTASKPRIORITY(task,
						SWITCHEDPRIORITY(
							METRICVALUE(kpi), 
							GREATERTHAN(high).THEN(NSM_TASKPRIORITY.HIGH()),
							LESSTHAN(medium).THEN(NSM_TASKPRIORITY.LOW()),
							DEFAULT(NSM_TASKPRIORITY.MEDIUM())
						)
					
				)
			);
	}

	public void SetPriorityLower(String kpi, NSM_VALUE high, NSM_VALUE medium)
	{
		raiseTask.onComplete().newAction(
				SETTASKPRIORITY(task,
					SWITCHEDPRIORITY(
							METRICVALUE(kpi), 
							LESSTHAN(high).THEN(NSM_TASKPRIORITY.HIGH()),
							GREATERTHAN(medium).THEN(NSM_TASKPRIORITY.LOW()),
							DEFAULT(NSM_TASKPRIORITY.MEDIUM())
					)
				)
		);
	}
	
	public void SetProperty(String key, NSM_VALUE val) {
		raiseTask.onComplete().newAction(SETTASKPROPERTY(task, PROPERTY(key, val)));
	}
	
	public void SetProperty(String key, String value) {
		NSM_VALUE nsmValue = METRICVALUE(value);
		
		//task.SETPROPERTY(PROPERTY(key, nsmValue));
		//task.SETPROPERTY(PROPERTY(key, CHECKEDVALUE(nsmValue.IS(NOTBLANK()), nsmValue, -1)));
		
		raiseTask.onComplete().newAction(SETTASKPROPERTY(task, PROPERTY(key, CHECKEDVALUE(nsmValue.IS(NOTBLANK()), nsmValue, -1))));
	}
}
