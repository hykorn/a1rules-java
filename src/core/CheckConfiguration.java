package core;

import com.actix.analytics.objects.*;
import com.actix.rules.flow.nodes.Action;
import com.actix.rules.flow.nodes.Check;
import com.actix.rules.flow.nodes.FlowController;
import com.actix.rules.flow.nodes.Loop;

import core.TaskDetails;

public class CheckConfiguration extends ObjectConsumer {
	// Contains the functions required for checking Configuration Parameters
	//Typically ran on daily data at sector level
	
	public static Check ParameterChange(String Parameter, boolean isString, String EventCode, String level)
	{
		// Checks if the current value of the specified parameter has changed since the previous period
		// Define the current parameter
		NSM_PARAMETER CurrentParameter = PARAMETER(Parameter);		

		// Retrieve the old parameter string or value
		NSM_VARIABLE oldValue = isString ? CurrentParameter.PREVIOUSSTRING() : CurrentParameter.PREVIOUSVALUE();

		// Retrieve the new parameter string or value
		NSM_VARIABLE newValue = isString ? CurrentParameter.STRING() : CurrentParameter.VALUE();	
		
		// Define the Configuration Event

		NSM_EVENT Event = EVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

		// Check if the parameter has changed

		Check parameterCheck = new Check(CurrentParameter.HASCHANGED());
		//parameterCheck.onTrue().output(TOKENISEDSTRING("$1,$2,$3,$4",Parameter, oldValue,newValue ,ELEMENTNAME()));
		parameterCheck.onTrue().output(TOKENISEDSTRING("$1",Parameter));

		// If the parameter has changed, raise a new Configuration Event

		parameterCheck.onTrue().newAction(

			RAISEEVENT(

					Event,

					TOKENISEDSTRING("Configuration Change for " + Parameter + " parameter ($1 -> $2)", oldValue, newValue),

					PROPERTY("Detection Method", "Configuration Change Check"),

					PROPERTY("Parameter", Parameter),

					PROPERTY("Previous Value", oldValue),

					PROPERTY("Current Value", newValue),

					PROPERTY("Level", level)

			)	

		);
		
		TaskDetails task = new TaskDetails("Parameter Change", EventCode, "" + Parameter, oldValue, newValue);
		task.RaiseTaskOn(parameterCheck.onTrue());
		task.SetPriority(NSM_PRIORITY.LOW());
		task.AssociateConfigurationEvent(EventCode);
		
		return parameterCheck;

	}	

	public static Check ParameterChange(String Parameter, String EventCode, String level, NSM_VALUE value)
	{
		// Checks if the current value of the specified parameter has changed since the previous period
		// Define the current parameter
		NSM_PARAMETER CurrentParameter = PARAMETER(Parameter);		

		// Retrieve the old parameter string or value
		NSM_VARIABLE oldValue = CurrentParameter.PREVIOUSVALUE();

		// Retrieve the new parameter string or value
		NSM_VARIABLE newValue = CurrentParameter.VALUE();		

		// Define the Configuration Event

		NSM_EVENT Event = EVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

		// Check if the parameter has changed
		
		//Check parameterCheck = new Check(CurrentParameter.HASCHANGED());

		Check parameterCheck = new Check(CurrentParameter.HASCHANGED().AND(IS(newValue,NOTBETWEEN(ABSOLUTERANGE(CurrentParameter.PREVIOUSVALUE(), value)))));
		
		//parameterCheck.onTrue().newCheck(IS(newValue,NOTBETWEEN(ABSOLUTERANGE(CurrentParameter.PREVIOUSVALUE(), value))));

		// If the parameter has changed, raise a new Configuration Event

		parameterCheck.onTrue().output(TOKENISEDSTRING("$1,$2,$3",TOKENISEDSTRING("$1",oldValue),TOKENISEDSTRING("$1",newValue) ,TOKENISEDSTRING("$1",ELEMENTNAME())));
		//parameterCheck.onTrue().output(TOKENISEDSTRING("$1",Parameter));
		
		parameterCheck.onTrue().newAction(

			RAISEEVENT(

					Event,

					TOKENISEDSTRING("Configuration Change for " + Parameter + " parameter ($1 -> $2)", oldValue, newValue),

					PROPERTY("Detection Method", "Configuration Change Check"),

					PROPERTY("Parameter", Parameter),

					PROPERTY("Previous Value", oldValue),

					PROPERTY("Current Value", newValue),

					PROPERTY("Level", level)

			)	

		);

		TaskDetails task = new TaskDetails("Parameter Change", EventCode, "" + Parameter, oldValue, newValue);
		task.RaiseTaskOn(parameterCheck.onTrue());
		task.SetPriority(NSM_PRIORITY.LOW());
		task.AssociateConfigurationEvent(EventCode);

		return parameterCheck;

	}	

	public static Check NeighborParameterChange(String NeighborParameter, boolean isString, String EventCode, String level)

	{

		//Checks if the current value of the specified neighbour parameter has changed since the previous period

		

		// Define the current neighbor parameter		

		NSM_RELATIONALPARAMETER CurrentParameter = RELATIONALPARAMETER(NeighborParameter);

	

		// Retrieve the old neighbor parameter string or value

		NSM_VARIABLE oldValue = isString ? CurrentParameter.PREVIOUSSTRING() : CurrentParameter.PREVIOUSVALUE();

		

		// Retrieve the new parameter string or value

		NSM_VARIABLE newValue = isString ? CurrentParameter.STRING() : CurrentParameter.VALUE();

		

		// Define the neighbor relation Configuration Event

		NSM_RELATIONALEVENT Nbr_Event = RELATIONALEVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

		

		// Check if the neighbor count is greater then 0

		Check NeighborCheck = new Check(IS(COUNT().OVER(NEIGHBORS()),GREATERTHAN(0)));

		

		// If neighbors exist, create new neighbor loop

		Loop NeighborLoop = NeighborCheck.onTrue().newLoop(NEIGHBORS());

		

		// Check each neighbor to see if the neighbor parameter has changed

		Check NeighborParameterCheck = NeighborLoop.onEach().newCheck(CurrentParameter.HASCHANGED());

				

		// If the neighbor parameter has changed, raise a new relational configuration event

		NeighborParameterCheck.onTrue().newAction(

			RAISEEVENT(

					Nbr_Event,

					TOKENISEDSTRING("Configuration Change for Outbound Neighbor " + NeighborParameter + " parameter ($1 \u279c $2)", oldValue, newValue),

					PROPERTY("Neighbor Relationship", TOKENISEDSTRING("$1 \u279c $2", SOURCENAME(),ELEMENTNAME())),

					PROPERTY("Detection Method", "Neighbor Configuration Change Check"),

					PROPERTY("Neighbor Parameter", NeighborParameter),

					PROPERTY("Previous Value", oldValue),

					PROPERTY("Current Value", newValue),

					PROPERTY("Level", level)

			)	

		);

				

		return NeighborCheck;

	}

	public static Check NeighbourParameterDesign(String NeighborParameter, char ConditionOperator, String EventCode, TaskDetails task)

	{

		// Compares the current value of the specified neighbour parameter against the neighbour parameter’s predefined threshold value.

		

		// Define the current neighbor parameter value

		NSM_VALUE CurrentValue = RELATIONALPARAMETER(NeighborParameter).VALUE();

		

		// Retrieve the designed neighbor parameter value

		NSM_VALUE DesignedValue = NAMEDTHRESHOLD("Expected_" + NeighborParameter);

		

		// Generate the condition by passing the operator function parameter and the designed parameter value to the CondtionalMeaning function

		NSM_CONDITION currentCondition = ConditionalMeaning(ConditionOperator, DesignedValue);

		

		// Define the neighbor relation Configuration Event 

		NSM_RELATIONALEVENT Nbr_Event = RELATIONALEVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

		

		// Check if the neighbor count is greater then 0

		Check NeighborCheck = new Check(IS(COUNT().OVER(NEIGHBORS()),GREATERTHAN(0)));

		

		// Create a new Neighbor loop

		Loop NeighborLoop = NeighborCheck.onTrue().newLoop(NEIGHBORS());

		

		// Check each neighbor to see if the neighbor parameter meets the condition

		Check NeighborParameterCheck = NeighborLoop.onEach().newCheck(IS(CurrentValue, currentCondition));

		

		// If the neighbor parameter check fails the condition, raise a new action

		NeighborParameterCheck.onTrue().newAction(

			RAISEEVENT(

					Nbr_Event,

					TOKENISEDSTRING("Configuration for Outbound Neighbor" + NeighborParameter + " parameter not as designed. Current value ($1) is " + ConditionOperator + " designed value ($2)", CurrentValue, DesignedValue),

					PROPERTY("Neighbor Relationship", TOKENISEDSTRING("$1 ↔ $2", SOURCENAME(),ELEMENTNAME())),

					PROPERTY("Detection Method", "Neighbor Configuration Design Check"),

					PROPERTY("Neighbor Parameter", NeighborParameter),

					PROPERTY("Current Value", CurrentValue),

					PROPERTY("Designed Value", DesignedValue),

					PROPERTY("Level", "Sector")

			)

		);

		

		//Associate the event to the task

		task.AssociateRelationalEvent(Nbr_Event, NEIGHBORS());

				

		return NeighborCheck;

	}

	public static Check ParameterDesignTolerance(String Parameter, int PercentageRange, String EventCode, TaskDetails task)
	{		
		// Compares the current value of the specified parameter against the parameter’s predefined threshold value, within a tolerance of +- a defined range.
		// Define the current parameter value
		NSM_VALUE CurrentValue = PARAMETERVALUE(Parameter);

		// Retrieve the designed parameter value
		NSM_VALUE DesignedValue = NAMEDTHRESHOLD("Expected_" + Parameter);

		// Define the Configuration Event
		NSM_EVENT Event = EVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

		// Check if the parameter is outside the design tolerance
		Check parameterCheck = new Check(IS(CurrentValue,NOTBETWEEN(PERCENTAGERANGE(DesignedValue, PercentageRange))));

		//If the parameter is outside the design tolerance, raise a new Configuration Event
		parameterCheck.onTrue().newAction(
			RAISEEVENT(
					Event,
					TOKENISEDSTRING("Configuration for " + Parameter + " parameter not as designed. Current value ($1) is outside the design tolerance ($2 ± " + PercentageRange + "%)", CurrentValue, DesignedValue),
					PROPERTY("Detection Method", "Configuration Design Tolerance Check"),
					PROPERTY("Parameter", Parameter),
					PROPERTY("Current Value", CurrentValue),
					PROPERTY("Design Tolerance", TOKENISEDSTRING("$1 ± " + PercentageRange + "%)", DesignedValue)),
					PROPERTY("Level", "Sector")
			)
		);

		// Associate the event to the task
		task.AssociateEvent(Event);
		return parameterCheck;
	}

	public static Check NeighbourParameterDesignTolerance(String NeighborParameter, int PercentageRange, String EventCode, TaskDetails task)

	{

		// Compares the current value of the specified neighbour parameter against the neighbour parameter’s threshold value, within a tolerance of +- a defined range

		

		// Define the current neighbor parameter value

		NSM_VALUE CurrentValue = RELATIONALPARAMETER(NeighborParameter).VALUE();

		

		// Define the current neighbor parameter value

		NSM_VALUE DesignedValue = NAMEDTHRESHOLD("Expected_" + NeighborParameter);

		

		// Define the neighbor relation Configuration Event

		NSM_RELATIONALEVENT NCA_Event = RELATIONALEVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

		

		// Check if the neighbor count is greater then 0

		Check NeighborCheck = new Check(IS(COUNT().OVER(NEIGHBORS()),GREATERTHAN(0)));

		

		// Create a new Neighbor loop

		Loop NeighborLoop = NeighborCheck.onTrue().newLoop(NEIGHBORS());

		

		// Check each neighbor to see if the neighbor parameter is outside the design tolerance

		Check NeighborParameterCheck = NeighborLoop.onEach().newCheck(IS(CurrentValue,NOTBETWEEN(PERCENTAGERANGE(DesignedValue, PercentageRange))));

		

		//If the neighbor parameter is outside the design tolerance, raise a new Configuration Event

		NeighborParameterCheck.onTrue().newAction(

			RAISEEVENT(

					NCA_Event,

					TOKENISEDSTRING("Configuration for Outbound Neighbor" + NeighborParameter + " parameter not as designed. Current value ($1) is outside the design tolerance ($2 ± " + PercentageRange + "%)", CurrentValue, DesignedValue),

					PROPERTY("Neighbor Relationship", TOKENISEDSTRING("$1 ↔ $2", SOURCENAME(),ELEMENTNAME())),

					PROPERTY("Detection Method", "Neighbor Configuration Design Tolerance Check"),

					PROPERTY("Neighbor Parameter", NeighborParameter),

					PROPERTY("Current Value", CurrentValue),

					PROPERTY("Design Tolerance", TOKENISEDSTRING("$1 ± " + PercentageRange + "%)", DesignedValue)),

					PROPERTY("Level", "Sector")

			)

		);

		

		if (task != null) {

			task.AssociateRelationalEvent(NCA_Event, NEIGHBORS());

		}

		

		return NeighborCheck;		

	}

	

	public static Check FindAddedElements(String EventCode, TaskDetails task)

	{

		// Checks for added elements below the specified level - currently set to sector level by default.

		

		// Define the Configuration Event

		NSM_RELATIONALEVENT Event = RELATIONALEVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

		

		// Check if there are any added children (radios)

		Check ElementCheck =  new Check(IS(COUNT().OVER(ADDEDCHILDREN()),GREATERTHAN(0)));

				

		// Loop over the added radios

		Loop ElementLoop = ElementCheck.onTrue().newLoop(ADDEDCHILDREN());

			

		// Raise a relational event on each sector where a new Radio has been added

		ElementLoop.onEach().newAction(

				RAISEEVENT(

						Event,

						TOKENISEDSTRING("Element Change for Sector $1: Carrier Added ($2)",SOURCENAME(),ELEMENTNAME()),

						PROPERTY("Detection Method", "Element Addition Check"),

						PROPERTY("Element Added",ELEMENTNAME()),

						PROPERTY("Level", "Sector")

				)

			);

		

		if (task != null) {

			//Associate the relational event to the task

			task.AssociateRelationalEvent(Event, CHILDREN());

		}

		

		return ElementCheck;

	}

	

	public static Check FindRemovedElements(String EventCode, TaskDetails task)

	{

		// Checks for removed elements below the specified level.

		

		// Define the Configuration Event

		NSM_RELATIONALEVENT ELM_Event = RELATIONALEVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

		

		// Check if there are any removed children (radios)

		Check ElementCheck =  new Check(IS(COUNT().OVER(REMOVEDCHILDREN()),GREATERTHAN(0)));

				

		//Loop over removed children

		Loop ElementLoop = ElementCheck.onTrue().newLoop(REMOVEDCHILDREN());

			

		// Raise a relational event on each sector where a Radio has been removed

		ElementLoop.onEach().newAction(

				RAISEEVENT(

						ELM_Event,

						TOKENISEDSTRING("Element Change for Sector $1: Carrier Removed ($2)",SOURCENAME(),ELEMENTNAME()),

						PROPERTY("Detection Method", "Element Removal Check"),

						PROPERTY("Element Removed",ELEMENTNAME()),

						PROPERTY("Level", "Sector")

				)

			);

		

		if (task != null) {

			//Associate the relational event to the task

			task.AssociateRelationalEvent(ELM_Event, REMOVEDCHILDREN());		

		}

		

		return ElementCheck;

	}

	public static Loop CheckNeighbourDistance(String rule, String eventType, String thres, String eventCode, TaskDetails task) {
		
		Loop nbrLoop = new Loop(NEIGHBORS());	
		NSM_VALUE distThreshold = NAMEDTHRESHOLD(thres);
		NSM_VALUE dist = DISTANCEBETWEEN("km");
		Check nbrDistanceCheck = null;
		
        nbrDistanceCheck = nbrLoop.onEach().newCheck(IS(dist, GREATERTHAN(distThreshold)));
        //nbrDistanceCheck.onTrue().output(TOKENISEDSTRING("$1 - $2, dist: $3 (exceeded $4)", SOURCENAME(), ELEMENTNAME(), LASTVALUE(), distThreshold));

        //Define the Performance Event
		NSM_RELATIONALEVENT nbrDistEvent = RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		
        Action raiseEvent = nbrDistanceCheck.onTrue().newAction(
				RAISEEVENT(
					nbrDistEvent,
					TOKENISEDSTRING(rule + " " + eventType + ": Distance between $1-$2: $3 km(threshold: $4km). Check if relation should be removed.", SOURCENAME(), ELEMENTNAME(), dist, distThreshold),
					PROPERTY("Distance", dist),
					PROPERTY("Threshold", distThreshold),
					PROPERTY("Detection Method", "Performance Deviation Check"),
					PROPERTY("Level", "Sector")
				)
			);
        
        if (task != null)
		{
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateRelationalEvent(nbrDistEvent, NEIGHBORS());
		}
        
		return nbrLoop; 
	}

	public static Loop FindNeighboursAndCheckHandOver(String rule, String eventType, String kpi, boolean EventList, String EventCode, TaskDetails task)
	{
		/* Checks for added neighbours at sector level over the last 7days */
		NSM_NAMEDWINDOW window = NAMEDWINDOW("NeighborAddedHistory");
		Loop nbrAddedLoop = new Loop(ADDEDNEIGHBORS(window, true));
		
		// Check if any outbound neighbors have been added
		//Check NeighborCheck =  new Check(IS(COUNT().OVER(ADDEDNEIGHBORS(window, true)),GREATERTHAN(0)));

		// There are 2 options for implementation: 
		// (i) EventList == true: Generate 1 event listing all neighbours that have been added
		// (ii) EventList == false: Generate 1 event per neighbour relationship for all neighbours that have been added

		// Option (i)
		if (EventList) {
			// Check for Number of HO Attempt
			
			// Check for Number of Successful HO
			
			NSM_RELATIONALEVENT Event = RELATIONALEVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));			

			/* Create a concatenated string listing all of the added neighbours for the current sector */
			NSM_STRING NeighborList = CONCATENATE(ELEMENTNAME()).USING(", ").OVER(ADDEDNEIGHBORS());	
			NSM_VALUE metricValue = METRICVALUE(kpi);
			
			Check NeighborCheck = nbrAddedLoop.onEach().newCheck(IS(metricValue,EQUALTO(0)));
			// Raise a Neighbour list change event on the current sector
			NeighborCheck.onTrue().newAction(
					RAISEEVENT(
						Event,
						TOKENISEDSTRING(rule + " " + eventType + ": No handover between $1-$2", SOURCENAME(), ELEMENTNAME()),
						PROPERTY("Detection Method", "Neighbour Handover Check"),
						PROPERTY("Level", "Sector")
						)
				);

			if (task != null) {
				//Associate the event to the task
				task.AssociateRelationalEvent(Event, NEIGHBORS());
			}
		}	
		
		return nbrAddedLoop;
	}
	
	public static Check FindAddedNeighbours(boolean EventList, String EventCode, TaskDetails task)
	{
		// Checks for added neighbours at sector level.
		// Check if any neighbors have been added
		Check NeighborCheck =  new Check(IS(COUNT().OVER(ADDEDNEIGHBORS()),GREATERTHAN(0)));

		// There are 2 options for implementation: 
		// (i) EventList == true: Generate 1 event listing all neighbours that have been added
		// (ii) EventList == false: Generate 1 event per neighbour relationship for all neighbours that have been added

		// Option (i)
		if (EventList == true)
		{
			// Define the neighbor event
			NSM_EVENT Event = EVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));			

			// Create a concatenated string listing all of the added neighbours for the current sector
			NSM_STRING NeighborList = CONCATENATE(ELEMENTNAME()).USING(", ").OVER(ADDEDNEIGHBORS());	

			// Raise a Neighbour list change event on the current sector
			NeighborCheck.onTrue().newAction(
					RAISEEVENT(
							Event,
							TOKENISEDSTRING("Neighbor List Change for Sector $1: $2 Outbound Neighbor(s) Added", ELEMENTNAME(), COUNT().OVER(ADDEDNEIGHBORS())),
							PROPERTY("Outbound Neighbors Added",NeighborList),
							PROPERTY("Detection Method", "Neighbour Addition Check"),
							PROPERTY("Level", "Sector")
							)
			);

			if (task != null) {
				//Associate the event to the task
				task.AssociateEvent(Event);
			}
		}		

		// Option (ii)
		else
		{
			// Define the neighbor relation event
			NSM_RELATIONALEVENT Event = RELATIONALEVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));			

			// Create loop to loop over the added neighbor
			Loop NeighborLoop = NeighborCheck.onTrue().newLoop(ADDEDNEIGHBORS());

			// Raise an neighbour change event for each neighbor relationship added
			NeighborLoop.onEach().newAction(
					RAISEEVENT(
							Event,
							TOKENISEDSTRING("Neighbor List Change for Sector $1: Outbound Neighbor Added ($2)",SOURCENAME(),ELEMENTNAME()),
							PROPERTY("Detection Method", "Neighbour Addition Check"),
							PROPERTY("Neighbor Relationship", TOKENISEDSTRING("$1 ↔ $2", SOURCENAME(),ELEMENTNAME())),
							PROPERTY("Level", "Sector")
				)
			);		

			if (task != null) {
				//Associate the relational event to the task
				task.AssociateRelationalEvent(Event, ADDEDNEIGHBORS());
			}
		}		
		return NeighborCheck;
	}

	

	public static Check FindRemovedNeighbours(boolean EventList, String EventCode, TaskDetails task)

	{

		// Checks for removed neighbours at sector level

				

		// Check if any neighbors have been removed	

		Check NeighborCheck =  new Check(IS(COUNT().OVER(REMOVEDNEIGHBORS()),GREATERTHAN(0)));

		

		// There are 2 options for implementation: 

		// (i) EventList == true: Generate 1 event listing all neighbours that have been removed

		// (ii) EventList == false: Generate 1 event per neighbour relationship for all neighbours that have been removed

		

		// Option (i)

		if (EventList == true)

		{

			// Define the neighbor event

			NSM_EVENT Event = EVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

			

			// Create a concatenated string listing all of the removed neighbours for the current sector

			NSM_STRING NeighborList = CONCATENATE(ELEMENTNAME()).USING(", ").OVER(REMOVEDNEIGHBORS());

			

			// Raise a Neighbour list change event on the current sector

			NeighborCheck.onTrue().newAction(

					RAISEEVENT(

							Event,

							TOKENISEDSTRING("Neighbor List Change for Sector $1: $2 Outbound Neighbor(s) Removed",ELEMENTNAME(),COUNT().OVER(REMOVEDNEIGHBORS())),

							PROPERTY("Outbound Neighbors Removed",NeighborList),

							PROPERTY("Detection Method", "Neighbour Removal Check"),

							PROPERTY("Level", "Sector")

							)

			);

			

			if (task != null) {

				//Associate the event to the task

				task.AssociateEvent(Event);

			}

		}

		

		// Option (ii)

		else

		{

			// Define the neighbor relation event

			NSM_RELATIONALEVENT Event = RELATIONALEVENT(NSM_EVENTTYPE.CONFIGURATIONALERT(EventCode));

			

			// Create loop to loop over the removed neighbors

			Loop NeighborLoop = NeighborCheck.onTrue().newLoop(REMOVEDNEIGHBORS());

			

			// Raise an neighbour list change event for each neighbor relationship removed

			NeighborLoop.onEach().newAction(

					RAISEEVENT(

							Event,

							TOKENISEDSTRING("Neighbor List Change for Sector $1: Outbound Neighbor Removed ($2)",SOURCENAME(),ELEMENTNAME()),

							PROPERTY("Detection Method", "Neighbour Removal Check"),

							PROPERTY("Neighbor Relationship", TOKENISEDSTRING("$1 ↔ $2", SOURCENAME(),ELEMENTNAME())),

							PROPERTY("Level", "Sector")

				)

			);	

			

			if (task != null) {

				//Associate the relational event to the task

				task.AssociateRelationalEvent(Event, REMOVEDNEIGHBORS());

			}

		}

				

		return NeighborCheck;

	}

	

	private static  NSM_CONDITION ConditionalMeaning(char ConditionOperator, NSM_VALUE Threshold)

	{

		// Private function to calculate the NSM_CONDITION required based on the Condition Operator and the current threshold

		

		NSM_CONDITION Condition;

		

		//Define the NSM_CONDITION required for each of the different cases of condition operator

		

		switch (ConditionOperator)

		{

		case '>': 

			Condition = GREATERTHAN(Threshold);

			break;

		case '<': 

			Condition = LESSTHAN(Threshold);

			break;

		case '=': 

			Condition = EQUALTO(Threshold);

			break;

		case '\u2265': 

			Condition = GREATERTHANOREQUALTO(Threshold);

			break;

		case '\u2264': 

			Condition = LESSTHANOREQUALTO(Threshold);

			break;

		case '\u2260': 

			Condition = NOTEQUALTO(Threshold);

			break;

		default: 

			Condition = GREATERTHAN(Threshold);

		}

		

		// Return the current condition

		

		return Condition;

	}

		

}


