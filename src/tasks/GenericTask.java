/*
 * Author: Darren Loh
 * History: 
 * 120712/DL -File creation.
 */
package tasks;

import genericrules.KPI;

import com.actix.rules.flow.nodes.Action;

import core.TaskDetails;

public class GenericTask extends TaskDetails {

	//private static final String taskType = "Performance Alert";
	
	public GenericTask(KPI kpi, String elementLvl, String taskQ, String taskType)
	{
		super(taskType, kpi.getTaskCode());
		
		// Define a new action to raise a task on the current sector
		this.raiseTask = new Action(
				RAISETASK(task,
						TOKENISEDSTRING("$1 on $2", NAMEDSTRING(kpi.getTaskCode() + "_Description"), ELEMENTNAME()),
						PROPERTY("Element", ELEMENTNAME()),
						PROPERTY("Technology", ELEMENTCATEGORY()),
						PROPERTY("Category", kpi.getCategory()),
						PROPERTY("Task Code", kpi.getTaskCode()),
						PROPERTY("Level", elementLvl),
						PROPERTY("Problem", NAMEDSTRING(taskCode + "_Description")),
						PROPERTY("Vendor", kpi.getVendorName()),
						PROPERTY("Site Name", PARENTNAME("Site")),
						PROPERTY("Controller Name", PARENTNAME("Controller")),
						PROPERTY("KPI Name", kpi.getFullname())
						)
			);
		
		//Set the task queue
		if(taskQ.length() > 0) {
			raiseTask.onComplete().newAction(SETTASKQUEUE(task, taskQ));
		}
		
	}
	
}

