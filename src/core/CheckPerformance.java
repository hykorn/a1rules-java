/*
 * History: 
 * 120607.darren.loh - Remove project specific code (strippedKPI, complicated conditional checking)
 */

package core;

import com.actix.analytics.objects.*;
import com.actix.rules.flow.nodes.Action;
import com.actix.rules.flow.nodes.Check;
import com.actix.rules.flow.nodes.Loop;

public abstract class CheckPerformance extends ObjectConsumer {
	
	// Contains the functions required for checking Performance Statistics

	public static Check CheckThreshold(String rule, String eventType, String eventCode, boolean performanceAlert, 
			String eventDesc, String KPI, String units, char ConditionOperator, NSM_VALUE thres, String windowName, 
		    TaskDetails task)
	{
		Check thresholdCheck = null;
		NSM_EVENT Event = null;
		NSM_VALUE currentThreshold = null;
		
		//Retrieve KPI Description from String Resources
		NSM_STRING currentDescription = LITERALSTRING(KPI);//NAMEDSTRING(KPI + "_Description");
		
		// Define KPI Value
		NSM_VALUE currentMetricValue = METRICVALUE(KPI);
		
		//@CHANGE 120607.darren.loh - START project specific replaceAll code is removed END
	
		//Retrieve Named Threshold value for KPI
		currentThreshold = thres;
		
		if(currentThreshold==null) {
			
			currentThreshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
		}
		
		// Generate the KPI condition by passing the operator parameter and the threshold to the CondtionalMeaning function
		NSM_CONDITION currentCondition = ConditionalMeaning(ConditionOperator, currentThreshold);
		
		if(performanceAlert)
			Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			Event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
		// Check if the KPI Value fails the KPI condition
		thresholdCheck = new Check(IS(currentMetricValue, currentCondition));
		
		//thresholdCheck.onTrue().output(currentMetricValue);
		
		//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = thresholdCheck.onTrue().newAction(
			RAISEEVENT(
					Event,
					TOKENISEDSTRING(rule + " " + eventType + ": " + eventDesc + ", " + KPI + " value ($2" + units + ") " + ConditionOperator + " Threshold ($3" + units + ")", currentDescription, ROUND(currentMetricValue, 2), currentThreshold),
					PROPERTY("Detection Method", "Performance Threshold Check"),
					PROPERTY("KPI", TOKENISEDSTRING("$1", currentDescription)),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units, ROUND(currentMetricValue,2))),
					PROPERTY("Threshold Value", TOKENISEDSTRING("$1" + units, currentThreshold)),
					PROPERTY("Type", eventType),
					PROPERTY("Level", "Sector")
			)
		);
			
		//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
		if(task != null) {
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(Event);
		}
		
		return thresholdCheck;
	}
	
	public static Check CheckThreshold(String rule, String eventType, String KPI, String eventCode, 
			boolean performanceAlert, char ConditionOperator, String units, String windowName, 
			NSM_VALUE thres, TaskDetails task)
	{
		Check thresholdCheck = null;
		NSM_EVENT Event = null;
		NSM_VALUE currentThreshold = null;
		
		//Retrieve KPI Description from String Resources
		NSM_STRING currentDescription = LITERALSTRING(KPI);//NAMEDSTRING(KPI + "_Description");
		
		// Define KPI Value
		NSM_VALUE currentMetricValue = METRICVALUE(KPI);
		
		 //@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
	
		//Retrieve Named Threshold value for KPI
		currentThreshold = thres;
		if(currentThreshold==null) {
			currentThreshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
		}
		
		// Generate the KPI condition by passing the operator parameter and the threshold to the CondtionalMeaning function
		NSM_CONDITION currentCondition = ConditionalMeaning(ConditionOperator, currentThreshold);
		
		if(performanceAlert)
			Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			Event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
		// Check if the KPI Value fails the KPI condition
		thresholdCheck = new Check(IS(currentMetricValue, currentCondition));
		
		//thresholdCheck.onTrue().output(currentMetricValue);
		
		//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = thresholdCheck.onTrue().newAction(
			RAISEEVENT(
					Event,
					TOKENISEDSTRING("$1 Performance Threshold violation: Current value ($2" + units + ") " + ConditionOperator + " $3" + units, currentDescription,currentMetricValue, currentThreshold),
					PROPERTY("Detection Method", "Performance Threshold Check"),
					PROPERTY("KPI", TOKENISEDSTRING("$1", currentDescription)),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units, currentMetricValue)),
					PROPERTY("Threshold Value", TOKENISEDSTRING("$1" + units, currentThreshold)),
					PROPERTY("Type", eventType),
					PROPERTY("Level", "Sector")
			)
		);
			
		//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
		if(task != null) {
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(Event);
		}
		
		return thresholdCheck;
	}
	
	public static Check CheckChildrenThresholds(String rule, String eventType, String KPI1, String KPI2, String eventCode, 
			boolean performanceAlert, char ConditionOperator, String units, String windowName, 
			NSM_VALUE thres1, NSM_VALUE thres2, TaskDetails task) {
		
		NSM_EVENT event = null;
		if(performanceAlert) {
			event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		} else {
			event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		}
		
		NSM_VALUE maxValue1 = MAXIMUM(KPI1, CHILDREN()).VALUE();
		NSM_VALUE maxValue2 = MAXIMUM(KPI2, CHILDREN()).VALUE();
		
		NSM_VALUE kpiThres1 = thres1;
		NSM_VALUE kpiThres2 = thres2;
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
		if(kpiThres1 == null) {
			kpiThres1 = NAMEDTHRESHOLD(KPI1 + "_PerfThreshold");
		}

		if(kpiThres2 == null) {
			kpiThres2 = NAMEDTHRESHOLD(KPI2 + "_PerfThreshold");
		}
		
		NSM_CONDITION condition1 = null;
		NSM_CONDITION condition2 = null;
		
		if(ConditionOperator == '>') {
			condition1 = GREATERTHAN(kpiThres1);
			condition2 = GREATERTHAN(kpiThres2);
		} else {
			condition1 = LESSTHAN(kpiThres1);
			condition2 = LESSTHAN(kpiThres2);
		}

		Check check = new Check(IS(maxValue1, condition1).OR(IS(maxValue2, condition2)));			
		NSM_VALUE oneGtwo = CHECKEDVALUE(IS(maxValue1, GREATERTHAN(maxValue2)), maxValue1);
		//check.onTrue().output(TOKENISEDSTRING("$1 maxChildValue1: $2, maxChildValue2: $3, maxChildVal: $4", ELEMENTNAME(), maxValue1,maxValue2,oneGtwo));
		
		Check compare = check.onTrue().newCheck(IS(maxValue1, GREATERTHAN(maxValue2)));
		//Check check2 = compare.onTrue().newCheck(IS(maxValue1,GREATERTHAN(0)));
		//Check check3 = compare.onFalse().newCheck(IS(maxValue2,GREATERTHAN(0)));
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
		Action raise1 = new Action(
				RAISEEVENT(
						event, 
						TOKENISEDSTRING(rule + " " + eventType + " of " + KPI1 + " on ($1" + units + ") " 
								+ ConditionOperator + " $2" + units + " on $3", maxValue1, kpiThres1, ELEMENTNAME()),
								PROPERTY("Value", maxValue1),
								PROPERTY("Type", eventType),
								PROPERTY("Level", "Site")));
		
		Action raise2 = new Action(
				RAISEEVENT(
						event, 
						TOKENISEDSTRING(rule + " " + eventType + " of " + KPI2 + " on ($1" + units + ") " 
								+ ConditionOperator + " $2" + units + " on $3", maxValue2, kpiThres2, ELEMENTNAME()),
								PROPERTY("Value", maxValue2),
								PROPERTY("Type", eventType),
								PROPERTY("Level", "Site")));
		
		compare.onTrue().addAction(raise1);
		
		compare.onFalse().addAction(raise2);

		return check;
	}
	
	public static Check CheckMaxThresholds(String rule, String eventType, String eventCode, boolean performanceAlert,
			String KPI1, String KPI2, char conditionOperator, String units, String windowName, 
			NSM_VALUE thres1, NSM_VALUE thres2, TaskDetails task) {
		
		Check thresholdCheck = null;
		NSM_EVENT Event = null;
		NSM_VALUE currentThreshold1 = null;
		NSM_VALUE currentThreshold2 = null;
		
		//Retrieve KPI Description from String Resources
		NSM_STRING currentDescription1 = LITERALSTRING(KPI1);//NAMEDSTRING(KPI + "_Description");
		NSM_STRING currentDescription2 = LITERALSTRING(KPI1);//NAMEDSTRING(KPI + "_Description");
		
		// Define KPI Value
		NSM_VALUE currentMetricValue1 = METRICVALUE(KPI1);
		NSM_VALUE currentMetricValue2 = METRICVALUE(KPI2);
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
	
		//Retrieve Named Threshold value for KPI
		currentThreshold1 = thres1;
		if(currentThreshold1==null) {
			currentThreshold1 = NAMEDTHRESHOLD(KPI1 + "_PerfThreshold");
		}
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
		
		//Retrieve Named Threshold value for KPI
		currentThreshold2 = thres2;
		if(currentThreshold2==null) {
			currentThreshold2 = NAMEDTHRESHOLD(KPI2 + "_PerfThreshold");
		}
	
		// Generate the KPI condition by passing the operator parameter and the threshold to the CondtionalMeaning function
		NSM_CONDITION currentCondition1 = ConditionalMeaning(conditionOperator, currentThreshold1);
		NSM_CONDITION currentCondition2 = ConditionalMeaning(conditionOperator, currentThreshold2);
		
		if(performanceAlert)
			Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			Event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
		// Check if the KPI Value fails the KPI condition
		thresholdCheck = new Check(IS(currentMetricValue1, currentCondition1).OR(IS(currentMetricValue2, currentCondition2)));
		
		Check oneGtwo = thresholdCheck.onTrue().newCheck(IS(currentMetricValue1, GREATERTHAN(currentMetricValue2)));
		
		//If the KPI value fails the check, then raise a Performance event
		Action raiseOneGTwoEvent = oneGtwo.onTrue().newAction(
			RAISEEVENT(
					Event,
					TOKENISEDSTRING(rule + " " + eventType + " Performance Threshold violation: " + KPI1 + " Current value ($1" + units + ") " + conditionOperator + " $2" + units, currentMetricValue1, currentThreshold1),
					PROPERTY("Detection Method", "Performance Threshold Check"),
					PROPERTY("KPI", TOKENISEDSTRING("$1 $2", currentDescription1, currentMetricValue1)),
					PROPERTY("Type", eventType),
					PROPERTY("Day", TOSTRING(CONTEXTDATE(),"DD-MM-YYYY")),
					PROPERTY("Level", "Sector")
			)
		);
		
		Action raiseTwoGOneEvent = oneGtwo.onFalse().newAction(
				RAISEEVENT(
					Event,
					TOKENISEDSTRING(rule + " " + eventType + " Performance Threshold violation: " + KPI2 + " Current value ($1" + units + ") " + conditionOperator + " $2" + units, currentMetricValue2, currentThreshold2),
					PROPERTY("Detection Method", "Performance Threshold Check"),
					PROPERTY("KPI", TOKENISEDSTRING("$1 $2", currentDescription2, currentMetricValue2)),
					PROPERTY("Type", eventType),
					PROPERTY("Day", TOSTRING(CONTEXTDATE(),"DD-MM-YYYY")),
					PROPERTY("Level", "Sector")
				)
			);
			
		//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
		if(task != null) {
			task.RaiseTaskOn(raiseOneGTwoEvent.onComplete());
			task.RaiseTaskOn(raiseTwoGOneEvent.onComplete());
			//Associate the event to the task
			task.AssociateEvent(Event);
		}
		
		return thresholdCheck;
	}
	
	public static Check CheckThresholds(String rule, String eventType, String eventCode, boolean performanceAlert,
			String KPI1, String KPI2, char conditionOperator, String units, String windowName, 
			NSM_VALUE thres1, NSM_VALUE thres2, TaskDetails task) {
		
		Check thresholdCheck = null;
		NSM_EVENT Event = null;
		NSM_VALUE currentThreshold1 = null;
		NSM_VALUE currentThreshold2 = null;
		
		//Retrieve KPI Description from String Resources
		NSM_STRING currentDescription1 = LITERALSTRING(KPI1);//NAMEDSTRING(KPI + "_Description");
		NSM_STRING currentDescription2 = LITERALSTRING(KPI1);//NAMEDSTRING(KPI + "_Description");
		
		NSM_VALUE currentMetricValue1 = METRICVALUE(KPI1);
		NSM_VALUE currentMetricValue2 = METRICVALUE(KPI2);
		if(windowName.length()>0) {
			currentMetricValue1 = MAXIMUM(KPI1,NAMEDWINDOW(windowName)).VALUE();
			currentMetricValue2 = MAXIMUM(KPI2,NAMEDWINDOW(windowName)).VALUE();
		} else {
			currentMetricValue1 = METRICVALUE(KPI1);
			currentMetricValue2 = METRICVALUE(KPI2);
		}
		// Define KPI Value
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
	
		//Retrieve Named Threshold value for KPI
		currentThreshold1 = thres1;
		if(currentThreshold1==null) {
			currentThreshold1 = NAMEDTHRESHOLD(KPI1 + "_PerfThreshold");
		}
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
		
		//Retrieve Named Threshold value for KPI
		currentThreshold2 = thres2;
		if(currentThreshold2==null) {
			currentThreshold2 = NAMEDTHRESHOLD(KPI2 + "_PerfThreshold");
		}
	
		// Generate the KPI condition by passing the operator parameter and the threshold to the CondtionalMeaning function
		NSM_CONDITION currentCondition1 = ConditionalMeaning(conditionOperator, currentThreshold1);
		NSM_CONDITION currentCondition2 = ConditionalMeaning(conditionOperator, currentThreshold2);
		
		if(performanceAlert)
			Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			Event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
		// Check if the KPI Value fails the KPI condition
		thresholdCheck = new Check(IS(currentMetricValue1, currentCondition1).AND(IS(currentMetricValue2, currentCondition2)));
		
		//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = thresholdCheck.onTrue().newAction(
			RAISEEVENT(
					Event,
					TOKENISEDSTRING(rule + " " + eventType + " Performance Threshold violation: " + KPI1 + " Current value ($1" + units + ") " + conditionOperator + " $2" + units + " AND " + KPI2 + " Current value ($3" + units + ") " + conditionOperator + " $4" + units, currentMetricValue1, currentThreshold1, currentMetricValue2, currentThreshold2),
					PROPERTY("Detection Method", "Performance Threshold Check"),
					PROPERTY("KPI 1", TOKENISEDSTRING("$1 $2", currentDescription1, currentMetricValue1)),
					PROPERTY("KPI 2", TOKENISEDSTRING("$1 $2", currentDescription2, currentMetricValue2)),
					PROPERTY("Type", eventType),
					PROPERTY("Level", "Sector")
			)
		);
			
		//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
		if(task != null) {
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(Event);
		}
		
		return thresholdCheck;
	}
	
	public static Check CheckMaxDailyThresholds(String KPI,char ConditionOperator, String units, String eventCode,TaskDetails task, String windowName 
	) {
		
		NSM_WINDOW win = NAMEDWINDOW(windowName);
		NSM_EVENT Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		NSM_VALUE maxValue = ROUND(MAXIMUM(KPI,win).VALUE(),2);
		NSM_VALUE currentMetricValue = ROUND(METRIC(KPI).VALUE(),2);
		NSM_CONDITION condition = null;

		if(ConditionOperator == '>') {
			condition = GREATERTHAN(maxValue);
		} else {
			condition = LESSTHAN(maxValue);
		}

		Check check = new Check(IS(currentMetricValue, condition));		
		//check.onTrue().output(TOKENISEDSTRING("$1, $2", currentMetricValue, maxValue));
		
		Action raise = check.onTrue().newAction(
						RAISEEVENT(
								Event,
								TOKENISEDSTRING("ACC Cause Radio Down for $1", ELEMENTNAME()),
								PROPERTY("Type", "Performance Cause Event"),
								PROPERTY("Detection Method", "Sector Performance Trigger Check"),
								PROPERTY("TCH Available", ROUND(METRIC("TCH_AVAILABLE").VALUE(),2)),
								PROPERTY("TCH Defined", ROUND(METRIC("TCH_DEFINED").VALUE(),2)),
								PROPERTY("Max Value", TOKENISEDSTRING("$1" + units,maxValue)),
								PROPERTY("Level", "Sector")
						)
					);

		return check;
	}
	
	public static Check CheckMaxChildrenThresholds(String rule, String eventType, String KPI1, String KPI2, String eventCode, 
			boolean performanceAlert, char ConditionOperator, String units, String windowName, 
			NSM_VALUE thres1, NSM_VALUE thres2, TaskDetails task) {
		
		NSM_EVENT event = null;
		if(performanceAlert) {
			event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		} else {
			event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		}
		
		NSM_VALUE maxValue1 = MAXIMUM(KPI1, CHILDREN(), DAY()).VALUE();
		NSM_VALUE maxValue2 = MAXIMUM(KPI2, CHILDREN(), DAY()).VALUE();
		
		NSM_VALUE kpiThres1 = thres1;
		NSM_VALUE kpiThres2 = thres2;
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
		
		if(kpiThres1 == null) {
			//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
			kpiThres1 = NAMEDTHRESHOLD(KPI1 + "_PerfThreshold");
		}

		if(kpiThres2 == null) {
			//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
			kpiThres2 = NAMEDTHRESHOLD(KPI2 + "_PerfThreshold");
		}
		
		NSM_CONDITION condition1 = null;
		NSM_CONDITION condition2 = null;
		
		if(ConditionOperator == '>') {
			condition1 = GREATERTHAN(kpiThres1);
			condition2 = GREATERTHAN(kpiThres2);
		} else {
			condition1 = LESSTHAN(kpiThres1);
			condition2 = LESSTHAN(kpiThres2);
		}

		Check check = new Check(IS(maxValue1, condition1).OR(IS(maxValue2, condition2)));			
		NSM_VALUE oneGtwo = CHECKEDVALUE(IS(maxValue1, GREATERTHAN(maxValue2)), maxValue1);
		//check.onTrue().output(TOKENISEDSTRING(eventType + " $1 maxValue1: $2, maxValue2: $3, maxVal: $4", ELEMENTNAME(), maxValue1,maxValue2,oneGtwo));
		
		Check compare = check.onTrue().newCheck(IS(maxValue1, GREATERTHAN(maxValue2)));
		//Check check2 = compare.onTrue().newCheck(IS(maxValue1,GREATERTHAN(0)));
		//Check check3 = compare.onFalse().newCheck(IS(maxValue2,GREATERTHAN(0)));
		
		Action raise1 = new Action(
				RAISEEVENT(
						event, 
						TOKENISEDSTRING("Max " + rule + " " + eventType + " of " + KPI1 + " ($1" + units + ") " 
								+ ConditionOperator + " $2" + units + " on $3", maxValue1, kpiThres1, ELEMENTNAME()),
								PROPERTY("Value", maxValue1),
								PROPERTY("Type", eventType),
								PROPERTY("Level", "Site")));
		
		Action raise2 = new Action(
				RAISEEVENT(
						event, 
						TOKENISEDSTRING("Max" + rule + " " + eventType + " of " + KPI2 + " ($1" + units + ") " 
								+ ConditionOperator + " $2" + units + " on $3", maxValue2, kpiThres2, ELEMENTNAME()),
								PROPERTY("Value", maxValue2),
								PROPERTY("Type", eventType),
								PROPERTY("Level", "Site")));
		
		compare.onTrue().addAction(raise1);
		compare.onFalse().addAction(raise2);
		
		//check3.onTrue().addAction(raise2);
		//check3.onFalse().addAction(raise1);
		
		return check;
	}
	
	public static Check CheckThresholdPlus(String rule, String eventType, String KPI, String eventCode, 
			boolean performanceAlert, boolean isFraction, boolean includeThreshold, double plus, 
			char conditionOperator, String units, String windowName, NSM_VALUE thres, TaskDetails task)
	{
		Check thresholdCheck = null;
		NSM_EVENT Event = null;
		NSM_VALUE currentThreshold = null;
		
		//Retrieve KPI Description from String Resources
		NSM_STRING currentDescription = LITERALSTRING(KPI);//NAMEDSTRING(KPI + "_Description");
		
		// Define KPI Value
		NSM_VALUE currentMetricValue = METRICVALUE(KPI);
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
	
		//Retrieve Named Threshold value for KPI
		currentThreshold = thres;
		if(currentThreshold==null) {
			currentThreshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
		}
		
		// Generate the KPI condition by passing the operator parameter and the threshold to the CondtionalMeaning function
		NSM_CONDITION currentCondition = ConditionalMeaning(conditionOperator, currentThreshold);
		
		if(performanceAlert)
			Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			Event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
		// Check if the KPI Value fails the KPI condition
		thresholdCheck = new Check(IS(currentMetricValue, currentCondition));
		
		//thresholdCheck.onTrue().output(currentMetricValue);
		
		//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = thresholdCheck.onTrue().newAction(
			RAISEEVENT(
					Event,
					TOKENISEDSTRING("$1 Performance Threshold violation: Current value ($2" + units + ") " + conditionOperator + " $3" + units, currentDescription,currentMetricValue, currentThreshold),
					PROPERTY("Detection Method", "Performance Threshold Check"),
					PROPERTY("KPI", TOKENISEDSTRING("$1", currentDescription)),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units, currentMetricValue)),
					PROPERTY("Threshold Value", TOKENISEDSTRING("$1" + units, currentThreshold)),
					PROPERTY("Type", eventType),
					PROPERTY("Level", "Sector")
			)
		);
			
		//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
		if(task != null) {
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(Event);
		}
		
		return thresholdCheck;
	}
	
	public static Check CheckThresholdHist(String KPI,char ConditionOperator, String units, String EventCode, TaskDetails task, NSM_VALUE Threshold, String windowName, boolean raise)
	{
		// Looks for days where Sector was above Threshold over window and counts the number of days
		//Rasies event if num days above threshold is >0
		
		NSM_WINDOW window = NAMEDWINDOW(windowName);
		
		// Define KPI Value
		NSM_VALUE currentMetricValue = ROUND(METRIC(KPI).VALUE(),2);
					
		// Define the historical average KPI Value
		NSM_VALUE MeanMetricValue = ROUND(MEAN(KPI, window).VALUE(),2);
		
		// Generate the KPI condition by passing the operator parameter and the threshold to the CondtionalMeaning function
		NSM_CONDITION currentCondition = ConditionalMeaning(ConditionOperator, Threshold);
		
		// Define the number of periods above threshold
		NSM_VALUE DaysAboveThreshold = OCCURRENCES(IS(currentMetricValue, currentCondition), window);
		
		//Define the Performance Event
		NSM_EVENT Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(EventCode));
		
		// Check if there's any periods over the threshold
		Check thresholdHistCheck = new Check(IS(DaysAboveThreshold, GREATERTHAN(0)));
		
		//If the KPI value fails the check, then raise a Performance event
		if (raise)		{
		Action raiseEvent = thresholdHistCheck.onTrue().newAction(
			RAISEEVENT(
					Event,
					TOKENISEDSTRING("ACC Cause Radio Down for $1", ELEMENTNAME()),
					PROPERTY("Type", "Performance Cause Event"),
					PROPERTY("Detection Method", "Sector Performance Trigger Check"),
					PROPERTY("TCH Available", currentMetricValue),
					PROPERTY("TCH Defined", ROUND(METRIC("TCH_DEFINED").VALUE(),2)),
					PROPERTY("Threshold Value", TOKENISEDSTRING("$1" + units,Threshold)),
					PROPERTY("Number of Days Above Threshold",DaysAboveThreshold),
					PROPERTY("Average Value", TOKENISEDSTRING("$1" + units,MeanMetricValue)),
					PROPERTY("Level", "Sector")
			)
		);			
		//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
		task.RaiseTaskOn(raiseEvent.onComplete());
		
		//Associate the event to the task
		task.AssociateEvent(Event);
		}					
		return thresholdHistCheck;
	}
	
	public static Loop CheckNeighbourDistanceAndThreshold(String rule, String eventType, String KPI, 
			String eventCode, boolean performanceAlert, char ConditionOperator, String units,
			NSM_VALUE thres, TaskDetails task)
	{
		Loop nbrLoop = new Loop(NEIGHBORS());
		//	Compares the current value of the specified KPI against the KPI�s pre-defined threshold value.
		
		//Retrieve Named Threshold value for KPI
		NSM_VALUE currentThreshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
	
		//Retrieve KPI Description from String Resources
		//NSM_STRING currentDescription = LITERALSTRING(KPI);//NAMEDSTRING(KPI + "_Description");
		
		// Define KPI Value
		NSM_VALUE currentMetricValue = RELATIONALMETRIC(KPI).VALUE();
		
		// Generate the KPI condition by passing the operator parameter and the threshold to the CondtionalMeaning function
		NSM_CONDITION currentCondition = ConditionalMeaning(ConditionOperator, currentThreshold);
		
		Check thresholdCheck = null;
		
		NSM_VALUE distThreshold = thres;
		NSM_VALUE dist = DISTANCEBETWEEN("km");
		
		//Define the Relational Performance Event
		NSM_RELATIONALEVENT relationalEvent = RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		// Check if the KPI Value fails the KPI condition
		thresholdCheck = nbrLoop.onEach().newCheck(IS(currentMetricValue, currentCondition).AND(IS(dist, GREATERTHAN(distThreshold))));
		//thresholdCheck = new Check(IS(currentMetricValue, currentCondition));
		//thresholdCheck.onTrue().output(TOKENISEDSTRING("$1 $2", currentMetricValue, currentThreshold));
		
		//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = thresholdCheck.onTrue().newAction(
			RAISEEVENT(
					relationalEvent,
					TOKENISEDSTRING(rule + " " + eventType + " of " + KPI + " from $1 to $2. " +
							"No Handover attempts to neighbor and distance between sectors $3 km(threshold: $4km), " +
							"remove relation.", SOURCENAME(), ELEMENTNAME(), dist, distThreshold),
					PROPERTY("Detection Method", "Sector Performance Trigger Check"),
					PROPERTY("KPI", KPI),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units, ROUND(currentMetricValue,2))),
					PROPERTY("KPI Threshold Value", TOKENISEDSTRING("$1" + units, ROUND(currentThreshold,2))),
					PROPERTY("Distance (km)", dist),
					PROPERTY("Distance Threshold (km)", distThreshold),
					PROPERTY("Level", "Sector")
			)
		);
		if(task != null) {
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateRelationalEvent(relationalEvent, NEIGHBORS());
		}
					
		return nbrLoop;
	}
	
	public static Check CheckAverage(String KPI,String KPIType, char ConditionOperator,String units, String EventCode, TaskDetails task, NSM_STRING KPIDescription, NSM_WINDOW Window, NSM_STRING WindowDescription)
	{
		// Compares the current value of the specified KPI against the historical average of the KPI over a predefined historical window
		
		// Define current KPI Value
		NSM_VALUE CurrentMetricValue = ROUND(METRIC(KPI).VALUE(),1);

		// Define the historical average KPI Value. If the KPI is fractional, use the CalculateFractionalAverage function
		NSM_VALUE MeanMetricValue = ROUND(MEAN(KPI, Window).VALUE(),1);
					
		// Define the number of valid periods used in the window calculations
		NSM_VALUE NumberofPeriods = AGGREGATE("COUNT", KPI, Window).VALUE();
		
		// Generate the KPI condition by passing the operator parameter and the threshold (i.e. historical average KPI value) to the CondtionalMeaning function
		NSM_CONDITION CurrentCondition = ConditionalMeaning(ConditionOperator,MeanMetricValue);	
		
		//Define the Performance Event
		NSM_EVENT Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(EventCode));
		
		// Check if the KPI Value fails the KPI condition
		Check AverageCheck = new Check(IS(CurrentMetricValue,CurrentCondition));
		
		//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = AverageCheck.onTrue().newAction(
			RAISEEVENT(
					Event,
					TOKENISEDSTRING(KPIType+ ": $1 Performance History Change: Current value ($2" + units + ") " + ConditionOperator + " $3 Average ($4" + units + ")", KPIDescription, CurrentMetricValue,WindowDescription, MeanMetricValue),
					PROPERTY("Detection Method", "Performance History Check"),
					PROPERTY("KPI", TOKENISEDSTRING("$1", KPIDescription)),
					PROPERTY("Window", TOKENISEDSTRING("$1", WindowDescription)),
					PROPERTY("Number of Aggregation Periods",NumberofPeriods),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units,CurrentMetricValue)),
					PROPERTY("Average Value", TOKENISEDSTRING("$1" + units,MeanMetricValue)),
					PROPERTY("Level", "Sector")
			)
		);
		
		if(task != null) {
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(Event);
		}
		return AverageCheck;
	}
	
	//public static Check CheckAveragePlus(String KPI,String KPIType, char ConditionOperator,String units, String EventCode, TaskDetails task, NSM_STRING KPIDescription, NSM_WINDOW Window, NSM_STRING WindowDescription)
	public static Check CheckAveragePlus(String rule, String eventType, String eventCode, 
			boolean performanceAlert, String eventDesc, String KPI,	String units, char conditionOperator, 
		    NSM_VALUE thres, String windowName, TaskDetails task)
	{
		// Compares the current value of the specified KPI against the historical average of the KPI over a predefined historical window
		NSM_WINDOW window = NAMEDWINDOW(windowName);
		
		// Define current KPI Value
		NSM_VALUE currentMetricValue = METRICVALUE(KPI);

		// Define the historical average KPI Value. If the KPI is fractional, use the CalculateFractionalAverage function
		NSM_VALUE meanMetricValue = MEAN(KPI, window).VALUE();
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
	
		// Calc Average plus%
		//Retrieve Named Threshold value for KPI
		NSM_VALUE currentThreshold = thres;
		if(currentThreshold==null) {
			currentThreshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
		}
		
		NSM_VALUE meanMetricValuePlusX = meanMetricValue.MULTIPLIEDBY(currentThreshold);
		NSM_VALUE plusPercent = currentThreshold.MINUS(1).MULTIPLIEDBY(100);
		
		// Define the number of valid periods used in the window calculations
		NSM_VALUE numOfPeriods = AGGREGATE("COUNT", KPI, window).VALUE();
		
		// Generate the KPI condition by passing the operator parameter and the threshold (i.e. historical average KPI value) to the CondtionalMeaning function
		NSM_CONDITION currentCondition = ConditionalMeaning(conditionOperator,meanMetricValuePlusX);	
		
		//Define the Performance Event
		NSM_EVENT event = null;
		if(performanceAlert)
			event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
		// Check if the KPI Value fails the KPI condition
		Check averageCheck = new Check(IS(currentMetricValue,currentCondition));
		
		//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = averageCheck.onTrue().newAction(
			RAISEEVENT(
					event,
					TOKENISEDSTRING(rule + " " + eventType + ": " + eventDesc + ", " + KPI + " value ($1" + units + ") " + conditionOperator + " Average + $2 ($3" + units + ")", currentMetricValue, plusPercent, meanMetricValuePlusX),
					PROPERTY("Detection Method", "Too Many Servers Check"),
					//PROPERTY("KPI", TOKENISEDSTRING("$1", KPIDescription)),
					//PROPERTY("Window", TOKENISEDSTRING("$1", WindowDescription)),
					PROPERTY("Number of Aggregation Periods",numOfPeriods),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units,currentMetricValue)),
					PROPERTY("Average Value", TOKENISEDSTRING("$1" + units,meanMetricValue)),
					PROPERTY("Average Plus", TOKENISEDSTRING("$1" + units,meanMetricValuePlusX)),
					PROPERTY("Level", "Sector")
			)
		);
		
		if(task != null) {
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(event);
		}
		
		return averageCheck;
	}

	public static Check CheckDeviation(String rule, String eventType, String KPI, String eventCode, 
			boolean performanceAlert, boolean isFraction, boolean includeThreshold, double devConst, 
			char condition, String units, String windowName, NSM_VALUE thres, TaskDetails task)
	{
		// Compares the current value of the specified KPI against the KPI deviation from the mean over a predefined historical window
		//Retrieve Named Window definition for KPI
		NSM_WINDOW CurrentWindow = NAMEDWINDOW(windowName);	
		
		//Retrieve KPI window description from String Resources
		//NSM_STRING CurrentWindowDescription = LITERALSTRING(KPI);//NAMEDSTRING(windowName + "_Description");
		
		// Define current KPI Value
		NSM_VALUE CurrentMetricValue = METRIC(KPI).VALUE();
			
		// Define the historical average KPI Value. If the KPI is fractional, use the CalculateFractionalAverage function
		//NSM_VALUE MetricValueAverage = isFraction ?  CalculateWindowAverage(KPI, CurrentWindow) : ROUND(MEAN(KPI, CurrentWindow).VALUE(),1);
			
		// Define the historical deviation KPI Value	
		//NSM_VALUE MetricValueDeviation = ROUND(DEVIATION(KPI, CurrentWindow).VALUE(),1);
	
		// Define the number of valid periods used in the window calculations
		NSM_VALUE NumberofPeriods = AGGREGATE("COUNT", KPI, CurrentWindow).VALUE();
		
		// Define the minimum value of the KPI over the historical window
		NSM_VALUE MinMetricValue = ROUND(MINIMUM(KPI, CurrentWindow).VALUE(),2);
		
		// Define the maximum value of the KPI over the historical window
		NSM_VALUE MaxMetricValue = ROUND(MAXIMUM(KPI, CurrentWindow).VALUE(),2);
		
		NSM_VALUE MetricDeviationFromMean;		
		
		NSM_VALUE deviationConstant;
		if(devConst<=0) {
			deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");			
			// Define the metric deviation from the mean (mean + (DeviationsFromMean * deviation))
			//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='<' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();	
		} else {
			//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? devConst : devConst*-1).VALUE();
			deviationConstant = LITERALVALUE(devConst);
		}
		
		MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
		
		// Define the difference between current value and deviation from mean
		NSM_VALUE Variance = condition=='>' ? CurrentMetricValue.MINUS(MetricDeviationFromMean):MetricDeviationFromMean.MINUS(CurrentMetricValue);
				
		NSM_EVENT event = null;
		if(performanceAlert)
			event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
			

		// Check if the KPI Value has breached threshold
		// Check if the KPI Value has deviated from the mean by more than the amount specified by MetricDeviationFromMean
		Check ThresholdCheck = null;
		Check DeviationCheck = null;
		
		// Define Threshold
		NSM_VALUE Threshold = thres;
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
		
		String sign = "+";

		//Retrieve KPI Description from String Resources
		//NSM_STRING CurrentMetricDescription = NAMEDSTRING(KPI + "_Description");
		//NSM_STRING CurrentMetricDescription = LITERALSTRING(strippedKPI);

		if(includeThreshold) {

			//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
			
			if(Threshold == null) {
				Threshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
			}
			
			if (condition == '>') {
				
				ThresholdCheck = new Check(IS(CurrentMetricValue,GREATERTHAN(Threshold)));
				
				DeviationCheck = ThresholdCheck.onTrue().newCheck(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean)));
				
				//DeviationCheck.onTrue().output(TOKENISEDSTRING("$1: $2, $3, $4, $5", ELEMENTNAME(), CurrentMetricValue,Threshold,ROUND(MetricDeviationFromMean,2), ROUND(MEAN(KPI, CurrentWindow).VALUE(),2)));

			} else {
				
				ThresholdCheck = new Check(IS(CurrentMetricValue,LESSTHAN(Threshold)));
				
				DeviationCheck = ThresholdCheck.onTrue().newCheck(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean)));
				
				//DeviationCheck.onTrue().output(TOKENISEDSTRING("$1: $2, $3, $4, $5", ELEMENTNAME(), CurrentMetricValue,Threshold,ROUND(MetricDeviationFromMean,2), ROUND(MEAN(KPI, CurrentWindow).VALUE(),2)));
				
				sign = "-";
			}
		} else {
			if (condition == '>') {

				DeviationCheck = new Check(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean)));
			
			} else {

				DeviationCheck = new Check(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean)));
				
				sign = "-";
			}

			Threshold = LITERALVALUE(0.0);
		}
		
		NSM_STRING description = null;
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name

		if (includeThreshold)
		{
			description = TOKENISEDSTRING(KPI + " ($1" 
				+ units + ") " + condition + " Dynamic Threshold ($2" + units + ") and  Fixed Threshold ($3" + units + ")",
				ROUND(CurrentMetricValue, 2),
				ROUND(MetricDeviationFromMean, 2),
				Threshold
			);
		}
		else
		{
			description = TOKENISEDSTRING(KPI + " ($1" 
					+ units + ") " + condition + " Dynamic Threshold ($3" + units + ")",
					ROUND(CurrentMetricValue,2),
					Threshold,
					ROUND(MetricDeviationFromMean,2)
				);
		}
		
		String kpiArea = "Unknown";
		if (rule == "ACC")
		{
			kpiArea = "Accessibility";
		}
		else if (rule == "RET")
		{
			kpiArea = "Retainability";
		}
		else if (rule == "MOB")
		{
			kpiArea = "Mobility";
		}

		//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = DeviationCheck.onTrue().newAction(
			RAISEEVENT(
					event,
					description,
					//PROPERTY("Detection Method", "Performance Deviation Check"),
					PROPERTY("KPI", LITERALSTRING(KPI)),
					//PROPERTY("Window", TOKENISEDSTRING("$1", CurrentWindowDescription)),
//					PROPERTY("Number of Aggregation Periods",NumberofPeriods),
					PROPERTY("Difference from Dynamic Threshold",TOKENISEDSTRING("$1"+ units,ROUND(Variance,2))),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units, ROUND(CurrentMetricValue,2))),
					PROPERTY("Dynamic Threshold", TOKENISEDSTRING("$1" + units, ROUND(MetricDeviationFromMean,2))),
					PROPERTY("3 week Average", TOKENISEDSTRING("$1" + units, ROUND(MEAN(KPI, CurrentWindow).VALUE(),2))),					
					PROPERTY("Threshold", includeThreshold ? TOKENISEDSTRING("$1" + units, Threshold) : LITERALSTRING("n/a")),
					//PROPERTY("Average Value", TOKENISEDSTRING("$1"+ units,MetricValueAverage)),
					PROPERTY("Min Value", TOKENISEDSTRING("$1"+ units,MinMetricValue)),
					PROPERTY("Max Value", TOKENISEDSTRING("$1"+ units,MaxMetricValue)),
					PROPERTY("Type", eventType),
					PROPERTY("Problem Area", kpiArea),
					PROPERTY("Level", "Sector")
			)
		);

		if (task != null)
		{
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(event);
		}
		
		return includeThreshold ? ThresholdCheck : DeviationCheck;
	}
	
	public static Check CheckDeviationPriority(String rule, String eventType, String KPI, String eventCode, 
			boolean performanceAlert, boolean isFraction, boolean includeThreshold, double devConst, 
			char condition, String units, String windowName, NSM_VALUE thres, TaskDetails task, NSM_VALUE medium, String level)
	{
		// Compares the current value of the specified KPI against the KPI deviation from the mean over a predefined historical window
		//Retrieve Named Window definition for KPI
		NSM_WINDOW CurrentWindow = NAMEDWINDOW(windowName);	
		
		//Retrieve KPI window description from String Resources
		//NSM_STRING CurrentWindowDescription = LITERALSTRING(KPI);//NAMEDSTRING(windowName + "_Description");
		
		// Define current KPI Value
		NSM_VALUE CurrentMetricValue = METRIC(KPI).VALUE();
			
		NSM_VALUE MetricDeviationFromMean;		
		
		NSM_VALUE deviationConstant;
		if(devConst<=0) {
			deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");			
			// Define the metric deviation from the mean (mean + (DeviationsFromMean * deviation))
			//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='<' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();	
		} else {
			//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? devConst : devConst*-1).VALUE();
			deviationConstant = LITERALVALUE(devConst);
		}
		
		MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
		
		// Define the difference between current value and deviation from mean
		NSM_VALUE Variance = condition=='>' ? CurrentMetricValue.MINUS(MetricDeviationFromMean):MetricDeviationFromMean.MINUS(CurrentMetricValue);
				
		NSM_EVENT event = null;
		if(performanceAlert)
			event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
			

		// Check if the KPI Value has breached threshold
		// Check if the KPI Value has deviated from the mean by more than the amount specified by MetricDeviationFromMean
		Check ThresholdCheck = null;
		Check DeviationCheck = null;
		
		// Define Threshold
		NSM_VALUE Threshold = thres;
		
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
		String sign = "+";

		//Retrieve KPI Description from String Resources
		//NSM_STRING CurrentMetricDescription = NAMEDSTRING(KPI + "_Description");
		//NSM_STRING CurrentMetricDescription = LITERALSTRING(strippedKPI);

		if(includeThreshold) {

			//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
			
			if(Threshold == null) {
				Threshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
			}
			
			//@CHANGE START 120607.darren.loh 
			//Simplify threshold checking based on its conditional operator. 
			NSM_CONDITION currentCondition = ConditionalMeaning(condition, Threshold);
			
			ThresholdCheck = new Check(IS(CurrentMetricValue, currentCondition));
			//@CHANGE END
			
		} else {
			if (condition == '>') {

				ThresholdCheck = new Check(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean)));
			
			} else {

				ThresholdCheck = new Check(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean)));
				
				sign = "-";
			}

			Threshold = LITERALVALUE(0.0);
		}
		
		
			//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = ThresholdCheck.onTrue().newAction(
			RAISEEVENT(
					event,
					TOKENISEDSTRING(rule + " " + eventType + " for " + KPI + " on " +level + " $1: Current value ($2" + units + ") " 
							+ condition + 
							//" Dynamic Trigger (Mean $4\u03C3 ($5" + units + ")) AND " + condition + 
							" Static Threshold ($5" + units +")", 
							ELEMENTNAME(), ROUND(CurrentMetricValue,2), deviationConstant, ROUND(MetricDeviationFromMean,2), Threshold),
					PROPERTY("KPI Value", TOKENISEDSTRING("$1" + units, ROUND(CurrentMetricValue,2))),
					PROPERTY("KPI Name", KPI),
					PROPERTY("Threshold", TOKENISEDSTRING("$1" + units, medium)),
					PROPERTY("Level", level),
					PROPERTY("Region", PARAMETERSTRING("Region")))
		);

		if (task != null)
		{
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(event);
		}
		
		return includeThreshold ? ThresholdCheck : DeviationCheck;
	}
	
	public static Loop CheckNeighborsDeviationPriority(String rule, String eventType, String KPI, String eventCode, 
			boolean performanceAlert, boolean isFraction, boolean includeThreshold, double devConst, 
			char condition, String units, String windowName, NSM_VALUE thres, TaskDetails task, NSM_VALUE medium, String level) {
		
		Loop Neighborloop = new Loop(NEIGHBORS());
//		Neighborloop.onEach().output(TOVALUE("Test"));
		
		Check NeighborDeviationCheck = null;
		NSM_VALUE Threshold = thres;
		NSM_VALUE CurrentMetricValue = RELATIONALMETRIC(KPI).VALUE();
		
//		NSM_VALUE isNeighbor_val = RELATIONALMETRIC("isNeighbour").VALUE();
//		Check checkIsNeighbor_val = Neighborloop.onEach().newCheck(IS(isNeighbor_val,EQUALTO(1))); 
//		checkIsNeighbor_val.onComplete().output(TOKENISEDSTRING("neighbors : $1, isNeighbor_val : $2",ELEMENTNAME(), isNeighbor_val));
//		Check nonzero = new Check(IS(thres,GREATERTHAN(0)));
//		Loop childLoop = nonzero.onComplete().newLoop(CHILDREN(level));
//		childLoop.onEach().output(TOKENISEDSTRING("$1",thres));
		
		/* Retrieve Named Window definition for KPI */
		NSM_WINDOW CurrentWindow = NAMEDWINDOW(windowName);	
		NSM_VALUE MetricDeviationFromMean;
		NSM_VALUE deviationConstant;
		if(devConst<=0) {
			deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");
		} else {
			deviationConstant = LITERALVALUE(devConst);
		}
		/* Define the metric deviation from the mean (mean + (DeviationsFromMean * deviation)) */
		MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
		
		String data = KPI.replaceAll("\"", "");
		String sign = "+";
		if(Threshold == null) {
			Threshold = NAMEDTHRESHOLD(data + "_PerfThreshold");
		}
		
		//@CHANGE START - 120607.darren.loh
		//Simplify conditional checking for threshold
		NSM_CONDITION currentCondition = ConditionalMeaning(condition, Threshold);
		
		NeighborDeviationCheck = Neighborloop.onEach().newCheck(IS(CurrentMetricValue,currentCondition));
		//@CHANGE END
		
		NSM_RELATIONALEVENT NeighborEvent = null;
        if(performanceAlert)
        	NeighborEvent = RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
        else
        	NeighborEvent = RELATIONALEVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
        
		//childDeviationCheck = childLoop.onEach().newCheck(IS(CurrentMetricValue,LESSTHAN(Threshold)).OR(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean))));
		//childDeviationCheck.onComplete().output(TOKENISEDSTRING("< : $1: $2, $3, $4, $5", ELEMENTNAME(), CurrentMetricValue,Threshold,ROUND(MetricDeviationFromMean,2), ROUND(MEAN(KPI, CurrentWindow).VALUE(),2)));
        
        NeighborDeviationCheck.onTrue().newAction(
				RAISEEVENT(
						NeighborEvent,
					TOKENISEDSTRING(rule + " " + eventType + " for " + KPI + " on " + level + " $1: Current value($2" + units + ") " 
							+ condition + 
							//" Dynamic Trigger (Mean $4\u03C3 ($5" + units + ")) AND " + condition + 
							" Static Threshold ($5" + units +")", 
							ELEMENTNAME(), ROUND(CurrentMetricValue,2), deviationConstant, ROUND(MetricDeviationFromMean,2), Threshold),
					PROPERTY("KPI Value", TOKENISEDSTRING("$1" + units, ROUND(CurrentMetricValue,2))),
					PROPERTY("KPI Name", KPI),
					PROPERTY("Threshold", TOKENISEDSTRING("$1" + units, medium)),
					PROPERTY("Level", level))
					//PROPERTY("Severity",SWITCHEDSTRING(CurrentMetricValue, WHEN(ConditionalMeaning(condition, high), "Critical"), WHEN(ConditionalMeaning(condition, medium), "Major"), WHEN(ConditionalMeaning(condition, low), "Minor")))	)
				);
        
        if (task != null)
		{
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			//task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateRelationalEvent(NeighborEvent, NEIGHBORS());
		}
        
		return Neighborloop; 
	}

	public static Check CheckDeviationCont(String rule, String eventType, String KPI, String eventCode, 
			boolean performanceAlert, boolean isFraction, boolean includeThreshold, double devConst, 
			char condition, String units, String windowName, NSM_VALUE thres, TaskDetails task, String desc, String level)
	{
		// Compares the current value of the specified KPI against the KPI deviation from the mean over a predefined historical window
		//Retrieve Named Window definition for KPI
		NSM_WINDOW CurrentWindow = NAMEDWINDOW(windowName);	
		
		//Retrieve KPI window description from String Resources
		//NSM_STRING CurrentWindowDescription = LITERALSTRING(KPI);//NAMEDSTRING(windowName + "_Description");
		
		// Define current KPI Value
		NSM_VALUE CurrentMetricValue = METRIC(KPI).VALUE();
			
		NSM_VALUE MetricDeviationFromMean;		
		
		NSM_VALUE deviationConstant;
		if(devConst<=0) {
			deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");			
			// Define the metric deviation from the mean (mean + (DeviationsFromMean * deviation))
			//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='<' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();	
		} else {
			//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? devConst : devConst*-1).VALUE();
			deviationConstant = LITERALVALUE(devConst);
		}
		
		MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
		
		// Define the difference between current value and deviation from mean
		NSM_VALUE Variance = condition=='>' ? CurrentMetricValue.MINUS(MetricDeviationFromMean):MetricDeviationFromMean.MINUS(CurrentMetricValue);
				
		NSM_EVENT event = null;
		if(performanceAlert)
			event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
			

		// Check if the KPI Value has breached threshold
		// Check if the KPI Value has deviated from the mean by more than the amount specified by MetricDeviationFromMean
		Check ThresholdCheck = null;
		Check DeviationCheck = null;
		
		// Define Threshold
		NSM_VALUE Threshold = thres;
		
		//@CHANGE 120607.darren.loh START Remove project specific removeAll code END
		
		String sign = "+";

		//Retrieve KPI Description from String Resources
		//NSM_STRING CurrentMetricDescription = NAMEDSTRING(KPI + "_Description");
		//NSM_STRING CurrentMetricDescription = LITERALSTRING(strippedKPI);

		if(includeThreshold) {

			//@CHANGE 120607.darren.loh START Remove project specific removeAll code END
		
			
			if(Threshold == null) {
				
				//@CHANGE 120607.darren.loh START update KPI name after removing project specific removeall code END
				Threshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
			}
			
			if (condition == '>') {
				
				ThresholdCheck = new Check(IS(CurrentMetricValue,GREATERTHAN(Threshold)));
				
				//DeviationCheck = ThresholdCheck.onTrue().newCheck(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean)));
				//DeviationCheck.onTrue().output(TOKENISEDSTRING("$1: $2, $3, $4, $5", ELEMENTNAME(), CurrentMetricValue,Threshold,ROUND(MetricDeviationFromMean,2), ROUND(MEAN(KPI, CurrentWindow).VALUE(),2)));

			} else {
				
				ThresholdCheck = new Check(IS(CurrentMetricValue,LESSTHAN(Threshold)));
				
				DeviationCheck = ThresholdCheck.onTrue().newCheck(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean)));
				//DeviationCheck.onTrue().output(TOKENISEDSTRING("$1: $2, $3, $4, $5", ELEMENTNAME(), CurrentMetricValue,Threshold,ROUND(MetricDeviationFromMean,2), ROUND(MEAN(KPI, CurrentWindow).VALUE(),2)));
				
				sign = "-";
			}
		} else {
			if (condition == '>') {

				//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
				
				if(Threshold == null) {
					//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
					Threshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
				}
				
				DeviationCheck = new Check(IS(CurrentMetricValue,GREATERTHAN(Threshold)));
				//DeviationCheck = new Check(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean)));
				
			
			} else {

				DeviationCheck = new Check(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean)));
				
				sign = "-";
			}

			Threshold = LITERALVALUE(0.0);
		}
			//If the KPI value fails the check, then raise a Performance event
		Action raiseEvent = DeviationCheck.onTrue().newAction(
			RAISEEVENT(
					event,
					TOKENISEDSTRING("$1",desc),
					PROPERTY(KPI + " Current Value", ROUND(METRIC(KPI).VALUE(),0)),
					PROPERTY(KPI + " Previous Value", ROUND(MEAN(KPI, CurrentWindow).VALUE(),0)),
					PROPERTY("Level", level),
					PROPERTY("Action", "Contributing Event to " + KPI)
					)
				);

		if (task != null)
		{
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(event);
		}
		
		return includeThreshold ? ThresholdCheck : DeviationCheck;
	}
	
		public static Check CheckParentDeviationCont(String rule, String eventType, String KPI, String eventCode, 
				boolean performanceAlert, boolean isFraction, boolean includeThreshold, double devConst, 
				char condition, String units, String windowName, NSM_VALUE thres, TaskDetails task, String desc, String level)
		{
			// Compares the current value of the specified KPI against the KPI deviation from the mean over a predefined historical window
			//Retrieve Named Window definition for KPI
			NSM_WINDOW CurrentWindow = NAMEDWINDOW(windowName);	
			
			//Retrieve KPI window description from String Resources
			//NSM_STRING CurrentWindowDescription = LITERALSTRING(KPI);//NAMEDSTRING(windowName + "_Description");
			
			// Define current KPI Value
			NSM_VALUE CurrentMetricValue = PARENTVALUE("VPU_CBSC",METRIC(KPI).VALUE());
				
			NSM_VALUE MetricDeviationFromMean;		
			
			NSM_VALUE deviationConstant;
			if(devConst<=0) {
				deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");			
				// Define the metric deviation from the mean (mean + (DeviationsFromMean * deviation))
				//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='<' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();	
			} else {
				//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? devConst : devConst*-1).VALUE();
				deviationConstant = LITERALVALUE(devConst);
			}
			
			MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
			
			// Define the difference between current value and deviation from mean
			NSM_VALUE Variance = condition=='>' ? CurrentMetricValue.MINUS(MetricDeviationFromMean):MetricDeviationFromMean.MINUS(CurrentMetricValue);
					
			NSM_EVENT event = null;
			if(performanceAlert)
				event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
			else
				event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
				

			// Check if the KPI Value has breached threshold
			// Check if the KPI Value has deviated from the mean by more than the amount specified by MetricDeviationFromMean
			Check ThresholdCheck = null;
			Check DeviationCheck = null;
			
			// Define Threshold
			NSM_VALUE Threshold = thres;
			
			//@CHANGE 120607.darren.loh - START Delete project specific replaceAll END
			
			//String condition = ">";
			String sign = "+";

			//Retrieve KPI Description from String Resources
			//NSM_STRING CurrentMetricDescription = NAMEDSTRING(KPI + "_Description");
			//NSM_STRING CurrentMetricDescription = LITERALSTRING(strippedKPI);

			if(includeThreshold) {

				//@CHANGE 120607.darren.loh - START Delete project specific replaceAll END
				
				if(Threshold == null) {
					//@CHANGE 120607.darren.loh - START proper KPI name after removing replaceAll code END
					Threshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
				}
				
				if (condition == '>') {
					
					ThresholdCheck = new Check(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean)));
					
					DeviationCheck = ThresholdCheck.onComplete().newCheck(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean)));
					//DeviationCheck.onComplete().output(TOKENISEDSTRING("$1: $2, $3, $4, $5", ELEMENTNAME(), CurrentMetricValue,Threshold,ROUND(MetricDeviationFromMean,2), ROUND(MEAN(KPI, CurrentWindow).VALUE(),2)));

				} else {
					
					ThresholdCheck = new Check(IS(CurrentMetricValue,LESSTHAN(Threshold)));
					
					DeviationCheck = ThresholdCheck.onTrue().newCheck(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean)));
					//DeviationCheck.onTrue().output(TOKENISEDSTRING("$1: $2, $3, $4, $5", ELEMENTNAME(), CurrentMetricValue,Threshold,ROUND(MetricDeviationFromMean,2), ROUND(MEAN(KPI, CurrentWindow).VALUE(),2)));
					
					sign = "-";
				}
			} else {
				if (condition == '>') {

					DeviationCheck = new Check(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean)));
					//DeviationCheck.onComplete().output(TOKENISEDSTRING("$1, $2, $3, $4 " + KPI,ELEMENTNAME(), PARENTNAME(), CurrentMetricValue, val));
				
				} else {

					DeviationCheck = new Check(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean)));
					
					sign = "-";
				}

				Threshold = LITERALVALUE(0.0);
			}
				//If the KPI value fails the check, then raise a Performance event
			Action raiseEvent = DeviationCheck.onTrue().newAction(
				PARENTACTION(RAISEEVENT(
						event,
						TOKENISEDSTRING("$1",desc),
						PROPERTY(KPI + " Current Value", ROUND(METRICVALUE(KPI),0)),
						PROPERTY(KPI + " Previous Value", ROUND(MEAN(KPI, CurrentWindow).VALUE(),0)),
						PROPERTY("Level", level),
						PROPERTY("Action", "Contributing Event to " + KPI)
						)
					));

			if (task != null)
			{
				//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
				task.RaiseTaskOn(raiseEvent.onComplete());
				
				//Associate the event to the task
				task.AssociateEvent(event);
			}
			
			return includeThreshold ? ThresholdCheck : DeviationCheck;
		}
	
	public static Loop CheckNeighbourDeviation(String rule, String eventType, String KPI, String eventCode, 
			boolean performanceAlert, boolean isFraction, boolean includeThreshold, double devConst, 
			char condition, String units, String windowName, NSM_VALUE thres, TaskDetails task) {
		
		//Loop nbrLoop = new Loop(SECTORSWITHINDISTANCE(beacon,"km"));
		Loop nbrLoop = new Loop(NEIGHBORS());
		//nbrLoop.onEach().output(TOKENISEDSTRING("$1, $2",SOURCENAME(),ELEMENTNAME()));
		Check nbrDeviationCheck = null;
		NSM_VALUE Threshold = null;
		NSM_VALUE CurrentMetricValue = METRIC(KPI).VALUE();
		
		/* Retrieve Named Window definition for KPI */
		NSM_WINDOW CurrentWindow = NAMEDWINDOW(windowName);	
		NSM_VALUE MetricDeviationFromMean;
		NSM_VALUE deviationConstant;
		if(devConst<=0) {
			deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");
		} else {
			deviationConstant = LITERALVALUE(devConst);
			//MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='<' ? devConst : devConst*-1).VALUE();		
		}
		/* Define the metric deviation from the mean (mean + (DeviationsFromMean * deviation)) */
		MetricDeviationFromMean = DEVIATIONFROMMEAN(KPI, CurrentWindow, condition=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
		
		//@CHANGE 120607.darren.loh - START remove project specific replaceAll code END
	
		//String condition = ">";
		String sign = "+";

		//@CHANGE 120607.darren.loh - START remove project specific replaceAll code END
		
		if (condition == '>') {	
			//@CHANGE 120607.darren.loh - START update KPI name as project specific replaceAll code is removed above END
			Threshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
			//nbrDeviationCheck = nbrLoop.onEach().newCheck(IS(CurrentMetricValue,GREATERTHAN(Threshold)).OR(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean))));
			nbrDeviationCheck = nbrLoop.onEach().newCheck(IS(CurrentMetricValue,GREATERTHAN(Threshold)).AND(IS(CurrentMetricValue,GREATERTHAN(MetricDeviationFromMean))));
		} else {
			//@CHANGE 120607.darren.loh - START update KPI name as project specific replaceAll code is removed above END
			Threshold = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
			//nbrDeviationCheck = nbrLoop.onEach().newCheck(IS(CurrentMetricValue,LESSTHAN(Threshold)).OR(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean))));
			nbrDeviationCheck = nbrLoop.onEach().newCheck(IS(CurrentMetricValue,LESSTHAN(Threshold)).AND(IS(CurrentMetricValue,LESSTHAN(MetricDeviationFromMean))));
			//condition = "<";
			sign = "-";
		}
		
		NSM_RELATIONALEVENT nbrDistEvent = null;
        if(performanceAlert)
        	nbrDistEvent = RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
        else
        	nbrDistEvent = RELATIONALEVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
        //@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
        nbrDeviationCheck.onTrue().newAction(
				RAISEEVENT(
					nbrDistEvent,
					/*TOKENISEDSTRING(eventType + " ($1:$2): $3 Performance Deviation from Mean: Current value ($4" 
							+ units + ") " + condition + " Mean " + sign + " " + "$5\u03C3 ($6" + units + "), Current value ($4) " + condition + " Threshold ($7)", 
							SOURCENAME(), ELEMENTNAME(), CurrentMetricDescription,CurrentMetricValue,deviationConstant,MetricDeviationFromMean,Threshold),*/
					TOKENISEDSTRING(rule + eventType + " " + KPI + " on Neighbor $1: Current value ($2" + units + ") " 
							+ condition + " Mean " + sign + " " + "$3\u03C3 ($4" + units + ") AND " + condition + " Threshold ($5" + units +")", 
							ELEMENTNAME(), ROUND(CurrentMetricValue,2), deviationConstant, ROUND(MetricDeviationFromMean,2), Threshold),
					PROPERTY("KPI", LITERALSTRING(KPI)),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units, ROUND(CurrentMetricValue,2))),
					PROPERTY("Deviation From Mean", TOKENISEDSTRING("$1" + units, ROUND(MetricDeviationFromMean,2))),
					PROPERTY("Threshold", TOKENISEDSTRING("$1" + units, Threshold)),
					PROPERTY("Detection Method", "Performance Deviation Check"),
					PROPERTY("Level", "Sector")
				));
        
        if (task != null)
		{
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			//task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateRelationalEvent(nbrDistEvent, NEIGHBORS());
		}
        
		return nbrLoop; 
	}
	
	public static Loop CheckNeighbourThreshold(String rule, String eventType, String KPI, String eventCode, 
			boolean performanceAlert, boolean isFraction, boolean includeThreshold, double devConst, 
			char conditionOperator, String units, String windowName, NSM_VALUE thres, TaskDetails task) {
		
		Loop nbrLoop = new Loop(NEIGHBORS());
		Check thresholdCheck = null;
		NSM_VALUE CurrentMetricValue = METRIC(KPI).VALUE();
		
		//@CHANGE 120607.darren.loh - START project specific replaceAll code is removed  END
		
		if(thres == null) {
			//@CHANGE 120607.darren.loh - START update KPI name as project specific replaceAll code is removed above END
			thres = NAMEDTHRESHOLD(KPI + "_PerfThreshold");
		}
		
		NSM_RELATIONALEVENT nbrDistEvent = null;
        if(performanceAlert)
        	nbrDistEvent = RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
        else
        	nbrDistEvent = RELATIONALEVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
		NSM_CONDITION condition = ConditionalMeaning(conditionOperator, thres);
		thresholdCheck = nbrLoop.onEach().newCheck(IS(CurrentMetricValue, condition));
        
		//@CHANGE 120607.darren.loh - Use actual KPI name instead of strippedKPI name
        Action raiseEvent = thresholdCheck.onTrue().newAction(
				RAISEEVENT(
					nbrDistEvent,
					TOKENISEDSTRING(rule + " " + eventType + " of " + KPI + " for $1-$2: Current value ($3" + units + ") " 
							+ conditionOperator + " Threshold ($4" + units +")", 
							SOURCENAME(), ELEMENTNAME(), ROUND(CurrentMetricValue,2), thres),
					PROPERTY("KPI", LITERALSTRING(KPI)),
					PROPERTY("Current Value", TOKENISEDSTRING("$1" + units, ROUND(CurrentMetricValue,2))),
					PROPERTY("Threshold", TOKENISEDSTRING("$1" + units, thres)),
					PROPERTY("Detection Method", "Performance Deviation Check"),
					PROPERTY("Level", "Sector")
				));
        
        if (task != null)
		{
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			//task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateRelationalEvent(nbrDistEvent, NEIGHBORS());
		}
        
		return nbrLoop; 
	}
	
	public static Check CheckDelta(String KPI, char ConditionOperator, char units,int NumberOfPeriods, int PercentageRange,String EventCode, TaskDetails task)
		{
			
			//	Compares the current value of the specified KPI against the value of the KPI for a specific previous period
			
			// Note that period is defined by the frequency of the rule - i.e. frequency = daily => period = 1 day
			
			//Negate the range value if the Condition operator is less than or less than equal to
			int range = (ConditionOperator == 'l'||ConditionOperator == '<') ? -PercentageRange : PercentageRange;
			
			// Define current KPI Value
			NSM_VALUE CurrentMetricValue = ROUND(METRIC(KPI).VALUE(),1);
			
			//Retrieve KPI Description from String Resources
//			NSM_STRING CurrentMetricDescription = NAMEDSTRING(KPI + "_Description");
			NSM_STRING CurrentMetricDescription = LITERALSTRING(KPI);

			// Define previous KPI Value - i.e. the value of the KPI for 'NumberOfPeriods' periods ago
			NSM_VALUE PreviousMetricValue = ROUND(HISTORICALVALUE(PREVIOUSPERIOD(NumberOfPeriods),METRIC(KPI).VALUE()),1);
			
			// Define the threshold to be a percentage delta from the previous value, i.e. previous KPI Value * (1 + range / 100)
			NSM_VALUE CurrentThreshold = PERCENTAGEDELTA(PreviousMetricValue, range);
			
			// Generate the KPI condition by passing the operator parameter and the threshold (i.e. the percentage delta) to the CondtionalMeaning function
			NSM_CONDITION CurrentCondition = ConditionalMeaning(ConditionOperator,CurrentThreshold);
			
			//Define the Performance Event
			NSM_EVENT PR_Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(EventCode));
			
			Check EventExists = new Check(PR_Event.EXISTS());

			// Check if the current KPI Value fails the KPI condition
			Check DeltaCheck = EventExists.onFalse().newCheck(IS(CurrentMetricValue,CurrentCondition));
		
			//If the current KPI value fails the check, then raise a Performance event
			Action raiseEvent = DeltaCheck.onTrue().newAction(
					RAISEEVENT(
							PR_Event,
							TOKENISEDSTRING("$1 Performance Delta: Current value ($2" + units + ") " + ConditionOperator + " previous value ($3" + units + ") by " + range + "%",CurrentMetricDescription, CurrentMetricValue, PreviousMetricValue),
							PROPERTY("Detection Method", "Performance Delta Check"),
							PROPERTY("KPI", TOKENISEDSTRING("$1", CurrentMetricDescription)),
							PROPERTY("Current Value", TOKENISEDSTRING("$1"+ units,CurrentMetricValue)),
							PROPERTY("Previous Date/Time",TOKENISEDSTRING("$1", PREVIOUSPERIOD(NumberOfPeriods))),
							PROPERTY("Level", "Sector")
					)
				);

			if (task != null) {
				//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
				task.RaiseTaskOn(raiseEvent.onComplete());
				
				//Associate the event to the task
				task.AssociateEvent(PR_Event);
			}
			
			return EventExists;
		}

	public static Check CheckRange(String rule, String eventType, String KPI, boolean isFraction, 
			char units,String PercentageRangeThreshold,String EventCode, TaskDetails task, String windowName, boolean performanceAlert)
	{		
		//Retrieve Named Window definition for KPI
		NSM_WINDOW CurrentWindow = NAMEDWINDOW(windowName);	
		
		//Retrieve KPI window description from String Resources
		//NSM_STRING CurrentWindowDescription = LITERALSTRING(KPI);//NAMEDSTRING(windowName + "_Description");

		NSM_VALUE PercentageRange = NAMEDTHRESHOLD(PercentageRangeThreshold);
		
		// Define current KPI Value
		NSM_VALUE CurrentMetricValue = ROUND(METRIC(KPI).VALUE(),0);
		
		//Retrieve KPI Description from String Resources
//		NSM_STRING CurrentMetricDescription = NAMEDSTRING(KPI + "_Description");
		NSM_STRING CurrentMetricDescription = LITERALSTRING(KPI);

		// Define the historical average KPI Value. If the KPI is fractional, use the CalculateFractionalAverage function
		NSM_VALUE MetricValueAverage = isFraction ?  CalculateWindowAverage(KPI, CurrentWindow) : ROUND(MEAN(KPI, CurrentWindow).VALUE(),1);
		
		// Generate the KPI condition by passing the operator parameter and the threshold (i.e. the percentage delta) to the CondtionalMeaning function
		NSM_CONDITION CurrentCondition = NOT(BETWEEN(PERCENTAGERANGE(MetricValueAverage, PercentageRange)));

		NSM_EVENT event = null;
		if(performanceAlert)
			event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(EventCode));
		else
			event = EVENT(NSM_EVENTTYPE.REALTIMEALERT(EventCode));
		
		Check EventExists = new Check(event.EXISTS());
		
		// Check if the current KPI Value fails the KPI condition
		Check DeltaCheck = EventExists.onFalse().newCheck(IS(CurrentMetricValue,CurrentCondition));
	
		NSM_VALUE lower = MetricValueAverage.MULTIPLIEDBY(LITERALVALUE(100).MINUS(PercentageRange).DIVIDEDBY(100));
		NSM_VALUE upper = MetricValueAverage.MULTIPLIEDBY(LITERALVALUE(100).PLUS(PercentageRange).DIVIDEDBY(100));
		
		//If the current KPI value fails the check, then raise a Performance event
		Action raiseEvent = DeltaCheck.onTrue().newAction(
				RAISEEVENT(
						event,
						//TOKENISEDSTRING(eventType + ": $1 Performance Delta: Current value ($2" + units + ") outside range",CurrentMetricDescription, CurrentMetricValue),
						//TOKENISEDSTRING(rule + " " + eventType + " of " + KPI + "for $1: Current value ($2), Not within ($3,$4)", 
						//		ELEMENTNAME(), ROUND(CurrentMetricValue,2), ROUND(lower,2), ROUND(upper,2)),
						TOKENISEDSTRING(rule + " " + eventType + " $1: Current " + KPI + " value ($2), changed by more than $3% from mean ($4), not within ($5,$6)",
								ELEMENTNAME(), CurrentMetricValue, PercentageRange, MetricValueAverage, ROUND(lower,2), ROUND(upper,2)),
						PROPERTY("Detection Method", "Performance Delta Check"),
						PROPERTY("KPI", TOKENISEDSTRING("$1", CurrentMetricDescription)),
						PROPERTY("Current Value", TOKENISEDSTRING("$1"+ units,CurrentMetricValue)),
						PROPERTY("Average Value", TOKENISEDSTRING("$1"+ units,MetricValueAverage)),
						PROPERTY("Range", TOKENISEDSTRING("($1,$2)"+ units, lower, upper)),
						PROPERTY("Level", "Sector")
				)
			);

		if (task != null) {
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(event);
		}
		
		return EventExists;
	}

	public static Loop CheckNeighbourRange(String rule, String eventType, String KPI, boolean isFraction, char units,
			String PercentageRangeThreshold,String eventCode, TaskDetails task, String windowName, boolean performanceAlert) {
		
		Loop nbrLoop = new Loop(NEIGHBORS());		
		//Retrieve Named Window definition for KPI
		NSM_WINDOW CurrentWindow = NAMEDWINDOW(windowName);	
		NSM_VALUE PercentageRange = NAMEDTHRESHOLD(PercentageRangeThreshold);
		NSM_VALUE CurrentMetricValue = ROUND(METRIC(KPI).VALUE(),0);
		//Retrieve KPI window description from String Resources
		//NSM_STRING CurrentWindowDescription = NAMEDSTRING(windowName + "_Description");
		
		//Retrieve KPI Description from String Resources
		//NSM_STRING CurrentMetricDescription = NAMEDSTRING(KPI + "_Description");
		//NSM_STRING CurrentMetricDescription = LITERALSTRING(KPI);

		// Define the historical average KPI Value. If the KPI is fractional, use the CalculateFractionalAverage function
		NSM_VALUE MetricValueAverage = ROUND(MEAN(KPI, CurrentWindow).VALUE(),0);
		
		// Generate the KPI condition by passing the operator parameter and the threshold (i.e. the percentage delta) to the CondtionalMeaning function
		NSM_CONDITION CurrentCondition = NOT(BETWEEN(PERCENTAGERANGE(MetricValueAverage, PercentageRange)));

		NSM_RELATIONALEVENT relDeltaEvent = null;
		if(performanceAlert)
			relDeltaEvent = RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(eventCode));
		else
			relDeltaEvent = RELATIONALEVENT(NSM_EVENTTYPE.REALTIMEALERT(eventCode));
		
		//Check EventExists = new Check(relDeltaEvent.EXISTS());
		
		// Check if the current KPI Value fails the KPI condition
		Check DeltaCheck = nbrLoop.onEach().newCheck(IS(CurrentMetricValue,CurrentCondition));
	
		NSM_VALUE lower = MetricValueAverage.MULTIPLIEDBY(LITERALVALUE(100).MINUS(PercentageRange).DIVIDEDBY(100));
		NSM_VALUE upper = MetricValueAverage.MULTIPLIEDBY(LITERALVALUE(100).PLUS(PercentageRange).DIVIDEDBY(100));
		
		//If the current KPI value fails the check, then raise a Performance event
		Action raiseEvent = DeltaCheck.onTrue().newAction(
				RAISEEVENT(
						relDeltaEvent,
						//TOKENISEDSTRING(eventType + ": $1 Performance Delta: Current value ($2" + units + ") outside range",CurrentMetricDescription, CurrentMetricValue),
						//TOKENISEDSTRING(rule + " " + eventType + " of " + KPI + " for $1-$2: Current value ($3), Not within ($4,$5)",
						//		SOURCENAME(), ELEMENTNAME(), ROUND(CurrentMetricValue,2), ROUND(lower,2), ROUND(upper,2)),
						TOKENISEDSTRING(rule + " " + eventType + " $1-$2: Current " + KPI + " value ($3), changed by more than $4% from mean ($5), not within ($6,$7)",
								SOURCENAME(), ELEMENTNAME(), CurrentMetricValue, PercentageRange, MetricValueAverage, ROUND(lower,2), ROUND(upper,2)),
						PROPERTY("Detection Method", "Performance Delta Check"),
						PROPERTY("KPI", KPI),
						PROPERTY("Current Value", TOKENISEDSTRING("$1"+ units,ROUND(CurrentMetricValue,2))),
						PROPERTY("Average Value", TOKENISEDSTRING("$1"+ units,MetricValueAverage)),
						PROPERTY("Range", TOKENISEDSTRING("($1,$2)"+ units, lower, upper)),
						PROPERTY("Level", "Sector")
				)
			);

		if (task != null) {
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			//task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateRelationalEvent(relDeltaEvent, NEIGHBORS());
		}
		
		return nbrLoop;
	}
	
	public static Check CheckDegradation(String KPI, boolean isFraction, char ConditionOperator,char units,int NumberOfPeriods,int PercentageRange,int EventCode, TaskDetails task)
		{
			// Check Degradation Compares the average value of the specified KPI over a predefined historical window against the average value of the KPI over a historical window, which occurred a number of periods previously
			
			// Note that period is defined by the frequency of the rule - i.e. frequency = daily => period = 1 day
			
			//Negate the range value if the Condition operator is less than or less than equal to
			int range = (ConditionOperator == 'l'||ConditionOperator == '<') ? -PercentageRange : PercentageRange;
			
			//Retrieve Named Window definition for KPI
			NSM_WINDOW ContextWindow = NAMEDWINDOW(KPI + "_History");
			
			//Retrieve KPI window description from String Resources
			NSM_STRING WindowDescription = NAMEDSTRING(KPI +"_History_Description");
		
			// Define the current KPI value to be the historical average for the KPI. If the KPI is fractional, use the CalculateFractionalAverage function
			NSM_VALUE CurrentMetricValue = isFraction ?  CalculateWindowAverage(KPI, ContextWindow) : ROUND(MEAN(KPI, ContextWindow).VALUE(),1);
			
			//Retrieve KPI Description from String Resources
			NSM_STRING CurrentMetricDescription = NAMEDSTRING(KPI + "_Description");

			// Define the previous KPI value to be the historical average for the KPI for the previous period. If the KPI is fractional, use the CalculateFractionalAverage function
			NSM_VALUE PreviousMetricValue = ROUND(HISTORICALVALUE(PREVIOUSPERIOD(NumberOfPeriods), CurrentMetricValue),1);
			
			// Define the current threshold to be the percentage delta, i.e. previous KPI Value * (1 + range / 100)
			NSM_VALUE CurrentThreshold = PERCENTAGEDELTA(PreviousMetricValue, range);
			
			// Generate the KPI condition by passing the operator parameter and the threshold (i.e. the percentage delta) to the CondtionalMeaning function
			NSM_CONDITION CurrentCondition = ConditionalMeaning(ConditionOperator,CurrentThreshold);
			
			// Define the number of periods used for calculations based on the current window
			NSM_VALUE NumberofCurrentPeriods = AGGREGATE("COUNT", KPI, ContextWindow).VALUE();
			
			// Define the number of periods used for calculations based on the previous window
			NSM_VALUE NumberofPreviousPeriods = ROUND(HISTORICALVALUE(PREVIOUSPERIOD(NumberOfPeriods), NumberofCurrentPeriods),1);
			
			//Define the Performance Event
			NSM_EVENT PDG_Event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT("PM_" + EventCode));
			
			// Check if the current KPI Value fails the KPI condition
			Check DegradationCheck = new Check(IS(CurrentMetricValue,CurrentCondition));
			
			//If the current KPI value fails the check, then raise a Performance event
			Action raiseEvent = DegradationCheck.onTrue().newAction(
					RAISEEVENT(
							PDG_Event,
							TOKENISEDSTRING("$1 Performance Degradation: $2 average ($3" + units + ") " + ConditionOperator + " previous $2 average ($4" + units + ") by " + range + "%",CurrentMetricDescription, WindowDescription, CurrentMetricValue, PreviousMetricValue),
							PROPERTY("Detection Method", "Performance Degradation Check"),
							PROPERTY("KPI", TOKENISEDSTRING("$1", CurrentMetricDescription)),
							PROPERTY("Window", TOKENISEDSTRING("$1", WindowDescription)),
							PROPERTY("Average Value over Current Window", TOKENISEDSTRING("$1"+ units,CurrentMetricValue)),
							PROPERTY("Average Value over Previous Window", TOKENISEDSTRING("$1"+ units,PreviousMetricValue)),
							PROPERTY("Degradation Threshold Value", TOKENISEDSTRING("$1"+ units,CurrentThreshold)),
							PROPERTY("Number of Current Aggregation Periods",NumberofCurrentPeriods),
							PROPERTY("Number of Previous Aggregation Periods",NumberofPreviousPeriods),
							PROPERTY("Previous Date/Time",TOKENISEDSTRING("$1", PREVIOUSPERIOD(NumberOfPeriods))),
							PROPERTY("Level", "Sector")
							)
				);
			
			//Call the Raise Task function to generate the Raise Task flow (Note that Raise task is only called once, once all events have been generated)
			task.RaiseTaskOn(raiseEvent.onComplete());
			
			//Associate the event to the task
			task.AssociateEvent(PDG_Event);			
			
			return DegradationCheck;
		}	
		
	public static Check NeighborEventExists(NSM_RELATIONALEVENT event) {

		return new Check(IS(SUM(CHECKEDVALUE(event.EXISTS(),1,0)).OVER(NEIGHBORS()),GREATERTHAN(0)));
	}
	
	//@CHANGE 120607.darren.loh - Use user friendly code 'g' for GREATERTHANOREQUALTO and 'l' for LESSTHANOREQUALTO
	protected static  NSM_CONDITION ConditionalMeaning(char ConditionOperator, NSM_VALUE Threshold)
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
			case 'g': 
				Condition = GREATERTHANOREQUALTO(Threshold);
				break;
			case 'l': 
				Condition = LESSTHANOREQUALTO(Threshold);
				break;
			case '!': 
				Condition = NOTEQUALTO(Threshold);
				break;
			default: 
				Condition = GREATERTHAN(Threshold);
			}
			
			// Return the current condition
			
			return Condition;
		}

	private static NSM_VALUE CalculateWindowAverage(String KPI, NSM_WINDOW ContextWindow)
		{	
			//Private function to calculate the Average of a fractional KPI over a window
			
			// This is a workaround to compensate for the fact that [Avg(A/B) over window] != [SUM(A)/SUM(B) * 100]
			
			//Calculate the sum of the Numerator over the window
			NSM_VALUE MetricNumeratorAverage = TOTAL(KPI + "_NUM", ContextWindow).VALUE();
			
			//Calculate the sum of the denominator over the window
			NSM_VALUE MetricDenominatorAverage = TOTAL(KPI + "_DENOM", ContextWindow).VALUE();
			
			//Calculate the window average to be [SUM(A)/SUM(B) * 100]
			NSM_VALUE MetricValueAverage = ROUND(RATIO(MetricNumeratorAverage, MetricDenominatorAverage).MULTIPLIEDBY(100),1);
						
			//Return the window average value
			return MetricValueAverage;
		}	
}
