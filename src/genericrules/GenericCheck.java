/*
 * Author: Darren Loh
 * History: 
 * 120712/DL -File creation. 
 * 121016/DL -Remove event code from event properties as it is already auto generated
 * 130313/DL -Added TopN Support
 * 130503/DL -Added SiteName and ControllerName
 */
package genericrules;

import com.actix.analytics.objects.NSM_DATE;
import com.actix.analytics.objects.NSM_EVENT;
import com.actix.analytics.objects.NSM_EVENTTYPE;
import com.actix.analytics.objects.NSM_IS;
import com.actix.analytics.objects.NSM_ISINTOPN;
import com.actix.analytics.objects.NSM_RELATIONALEVENT;
import com.actix.analytics.objects.NSM_TOKENISEDSTRING;
import com.actix.analytics.objects.NSM_VALUE;
import com.actix.rules.flow.nodes.Check;
import com.actix.rules.flow.nodes.Loop;

import core.CheckPerformance;
	
public class GenericCheck extends CheckPerformance{

	private static boolean isDebugMode = false;
	
	/**
	 * @param triggerMethod
	 * @param condition
	 * @param curValue
	 * @param thres
	 * @param devMean
	 * @return
	 * 
	 * DESC:
	 * Create conditional Check based on triggerMethod and condition
	 */
	private static NSM_IS getCheckCondition(int triggerMethod, char condition, NSM_VALUE curValue, 
			                                NSM_VALUE thres, NSM_VALUE devMean){
		
		NSM_IS checkCondition = null;
		
		if ('g' == condition) {
			
			switch(triggerMethod) {
			
				case 1:
				case 122:
                case 132: checkCondition = IS(curValue,GREATERTHANOREQUALTO(thres)); break;
				
				case 2: checkCondition = IS(curValue,GREATERTHANOREQUALTO(devMean)); break;
				
				case 112: checkCondition = IS(curValue,GREATERTHANOREQUALTO(thres).AND(GREATERTHANOREQUALTO(devMean))); break;
				
				case 102: checkCondition = IS(curValue,GREATERTHANOREQUALTO(thres).OR(GREATERTHANOREQUALTO(devMean))); break;
				
			}
			
		} else if ('l' == condition) {
			
			switch(triggerMethod) {
			
				case 1 :
				case 122:
                case 132: checkCondition = IS(curValue,LESSTHANOREQUALTO(thres)); break;
			 
				case 2 : checkCondition = IS(curValue,LESSTHANOREQUALTO(devMean)); break;
			
				case 112: checkCondition = IS(curValue,LESSTHANOREQUALTO(thres).AND(LESSTHANOREQUALTO(devMean))); break;
				
				case 102: checkCondition = IS(curValue,LESSTHANOREQUALTO(thres).AND(LESSTHANOREQUALTO(devMean))); break;
			
			}
			
		} else if ('>' == condition) {
			
			switch(triggerMethod) {
			
			case 1 :
			case 122:
            case 132: checkCondition = IS(curValue,GREATERTHAN(thres)); break;
		
			case 2: checkCondition = IS(curValue,GREATERTHAN(devMean)); break;
		
			case 112: checkCondition = IS(curValue,GREATERTHAN(thres).AND(GREATERTHAN(devMean))); break;
			
			case 102: checkCondition = IS(curValue,GREATERTHAN(thres).OR(GREATERTHAN(devMean))); break;
		
			}
						
			
		}else if ('<' == condition) {
			
			switch(triggerMethod) {
			
			case 1 :
			case 122:
            case 132: checkCondition = IS(curValue,LESSTHAN(thres)); break;
		
			case 2 : checkCondition = IS(curValue,LESSTHAN(devMean)); break;
		
			case 112 : checkCondition = IS(curValue,LESSTHAN(thres).AND(LESSTHAN(devMean))); break;
			
			case 102 : checkCondition = IS(curValue,LESSTHAN(thres).OR(LESSTHAN(devMean))); break;
		
			}
			
			
		} else if ('=' == condition){
			
			switch(triggerMethod) {
			
			case 1 :
			case 122:
            case 132: checkCondition = IS(curValue,EQUALTO(thres)); break;
		
			case 2 : checkCondition = IS(curValue,EQUALTO(devMean)); break;
		
			case 112 : checkCondition = IS(curValue,EQUALTO(thres).AND(EQUALTO(devMean))); break;
			
			case 102 : checkCondition = IS(curValue,EQUALTO(thres).OR(EQUALTO(devMean))); break;
		
			}
				
		} else if ('!' == condition ){
			
			switch(triggerMethod) {
			
			case 1 :
			case 122:
            case 132: checkCondition = IS(curValue,NOTEQUALTO(thres)); break;
		
			case 2 : checkCondition = IS(curValue,NOTEQUALTO(devMean)); break;
		
			case 112 : checkCondition = IS(curValue,NOTEQUALTO(thres).AND(NOTEQUALTO(devMean))); break;
			
			case 102 : checkCondition = IS(curValue, GREATERTHAN(thres)); break;
		
			}
		}
				
		return checkCondition;
	}

	/**
	 * @param kpi
	 * @param triggerMethod
	 * @param curValue
	 * @param thres
	 * @param devMean
	 * @param level
	 * @return
	 * 
	 * DESC:
	 * Construct problem description for Relational KPI based on triggerMethod
	 */
	private static NSM_TOKENISEDSTRING getDescRelational(KPI kpi, int triggerMethod, NSM_VALUE curValue, NSM_VALUE thres, 
			NSM_VALUE devMean, String level) {
		
		NSM_TOKENISEDSTRING str = null;
		
		if ( 1 == triggerMethod) { 
			
			str = TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " + level + " $1 -> $2: Current value ($3" +  kpi.getUnit() + ") "
					+ kpi.getCondition() + " Static Threshold ($4" + kpi.getUnit() +")", 
					SOURCESTRING(ELEMENTNAME()), ELEMENTNAME(), ROUND(curValue,2), thres);
			
		} else if ( 2 == triggerMethod) { 
			
			str = 	TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " +level + " $1 -> $2: Current value ($3" +  kpi.getUnit() + ") " 
					+ kpi.getCondition() + " Dynamic Trigger (Mean) $4" +  kpi.getUnit(), 
					SOURCESTRING(ELEMENTNAME()), ELEMENTNAME(), ROUND(curValue,2), ROUND(devMean,2));
			
		} else if ( 112 == triggerMethod) { 
			
			str = 	TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " +level + " $1 -> $2: Current value ($3" +  kpi.getUnit() + ") " 
					+ kpi.getCondition() + " Dynamic Trigger (Mean) $4" +  kpi.getUnit() + " AND " + kpi.getCondition() +
					" Static Threshold ($5" +  kpi.getUnit() +")", 
					SOURCESTRING(ELEMENTNAME()), ELEMENTNAME(), ROUND(curValue,2), ROUND(devMean,2), thres);
			
		} else if ( 102 == triggerMethod) { 
			
			str = 	TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " +level + " $1 -> $2: Current value ($3" +  kpi.getUnit() + ") " 
					+ kpi.getCondition() + " Dynamic Trigger (Mean) $4" +  kpi.getUnit() + " OR " + kpi.getCondition() + 
					" Static Threshold ($5" +  kpi.getUnit() +")", 
					SOURCESTRING(ELEMENTNAME()), ELEMENTNAME(), ROUND(curValue,2), ROUND(devMean,2), thres);
		} 
		
		return str;
	}
	
	/**
	 * @param kpi
	 * @param triggerMethod
	 * @param curValue
	 * @param thres
	 * @param devMean
	 * @param level
	 * @return
	 * 
	 * DESC:
	 * Construct problem description for non-relational KPI based on triggerMethod
	 */
	private static NSM_TOKENISEDSTRING getDesc(KPI kpi, int triggerMethod, NSM_VALUE curValue, NSM_VALUE thres, NSM_VALUE deltaThres, NSM_VALUE devMean, String level) {
		
		NSM_TOKENISEDSTRING str = null;
		
		if ( 1 == triggerMethod) {
			
			str = TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " + level + " $1: Current value ($2" +  kpi.getUnit() + ") "
					+ kpi.getCondition() + " Static Threshold ($3" + kpi.getUnit() +")", 
					ELEMENTNAME(), ROUND(curValue,2), thres);
			
		} else if( 2 == triggerMethod) { 
			
			str = 	TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " +level + " $1: Current value ($2" +  kpi.getUnit() + ") " 
					+ kpi.getCondition() + " Dynamic Trigger (Mean) $3" +  kpi.getUnit(), 
					ELEMENTNAME(), ROUND(curValue,2), ROUND(devMean,2));
			
		} else if( 112 == triggerMethod) { 
			
			str = 	TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " +level + " $1: Current value ($2" +  kpi.getUnit() + ") " 
					+ kpi.getCondition() + " Dynamic Trigger (Mean) $3" +  kpi.getUnit() + " AND " + kpi.getCondition() + 
					" Static Threshold ($4" +  kpi.getUnit() +")", 
					ELEMENTNAME(), ROUND(curValue,2), ROUND(devMean,2), thres);
			
		} else if( 102 == triggerMethod) { 
			
			str = 	TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " +level + " $1: Current value ($2" +  kpi.getUnit() + ") " 
					+ kpi.getCondition() + " Dynamic Trigger (Mean) $3" +  kpi.getUnit() + " OR " + kpi.getCondition() + 
					" Static Threshold ($4" +  kpi.getUnit() +")", 
					ELEMENTNAME(), ROUND(curValue,2), ROUND(devMean,2), thres);

		// TODO: HY Add
		} else if( 122 == triggerMethod) { 
			
			str = TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " + level + " $1: Delta ($2%) "
					+ kpi.getCondition() + " Delta Threshold ($3%)", 
					ELEMENTNAME(), ROUND(curValue,2), thres);
			
		// TODO: HY Add
		} else if( 132 == triggerMethod) { 
			
			str = 	TOKENISEDSTRING("Performance Trigger for " + kpi.getFullname() + " on " +level + " $1: Current value ($2" +  kpi.getUnit() + ") " 
					+ kpi.getCondition() + " Static Threshold $3" +  kpi.getUnit() + " AND " + kpi.getCondition() + 
					" Delta Threshold ($4%)", 
					ELEMENTNAME(), ROUND(curValue,2), thres, deltaThres);
			
		}
		
		return str;
	}
	
	/**
	 * @param triggerMethod
	 * @return
	 * 
	 * DESC:
	 * Return Check Method based on triggerMethod
	 */
	private static String getCheckMethod(int triggerMethod) {
		
		String str = "";
		
		if( 1 == triggerMethod) { 
			
			str = "Threshold only";
			
		} else if( 2 == triggerMethod) { 
			
			str =  "Deviation Mean only";
			
		} else if( 112 == triggerMethod) { 
			
			str =  "Threshold And Deviation Mean";
			
		} else if( 102 == triggerMethod) { 
			
			str =  "Threshold And Deviation Mean";
			
		} else if( 122 == triggerMethod) { 
			//TODO: HY Add
			str =  "Delta Threshold only";
			
		} else if( 132 == triggerMethod) { 
			//TODO: HY Add
			str =  "Threshold And Delta Threshold";
			
		}
		
		return str;
	}
	
	/**
	 * @param kpi
	 * @param windowName
	 * @param level
	 * @return
	 * 
	 * DESC:
	 * Create Check and raise Event if Check is true
	 */
	public static Check getCheck(KPI kpi, String windowName, String level)
	{	
		
		NSM_VALUE curValue = METRIC(kpi.getFullname()).VALUE();
		
		NSM_EVENT event = EVENT(NSM_EVENTTYPE.PERFORMANCEALERT(kpi.getEventCode()));
		
		NSM_VALUE thres = LITERALVALUE(0);
		
		NSM_VALUE devMean = LITERALVALUE(3.329);
		
		NSM_VALUE degradePct = LITERALVALUE(0);
		
		NSM_VALUE deltaThres = LITERALVALUE(0);
		
		//NSM_VALUE yestdyValue = LITERALVALUE(0);
		
		NSM_VALUE kpiAvgValue = LITERALVALUE(0);
		
		NSM_VALUE deltaAveKpi = LITERALVALUE(0);
		
		NSM_VALUE oldCurValue = curValue;
		
		int trigMethod = kpi.getTriggerMethod();
		
		//if Trigger method is not "Deviation Mean only" or "Delta only"
		if ((2 != trigMethod) || (122 != trigMethod)) {
			
			thres =  NAMEDTHRESHOLD(kpi.getFullname() + "_PerfThreshold");
		
		}
		
		//if Trigger method is not "Threshold only or "Delta only" or "Threshold & Delta"
		if ((1 !=trigMethod) || (122 != trigMethod) || (132 != trigMethod)) { 
		
			NSM_VALUE deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");	
		
			devMean = DEVIATIONFROMMEAN(kpi.getFullname(), NAMEDWINDOW(windowName), kpi.getCondition()=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
		}
		
		//TODO: HY Add
		if ((122 == trigMethod) || (132 == trigMethod))
		{
			//TODO: HY Temp test historical value
			//yestdyValue = LITERALVALUE(0.5);
			//NSM_DATE specificDate = LITERALDATE("20131125", "yyyymmdd");
			//yestdyValue =  HISTORICALVALUE(specificDate, METRIC(kpi.getFullname()).VALUE());
			//yestdyValue =  HISTORICALVALUE(PREVIOUSDAY(1), METRIC(kpi.getFullname()).VALUE());
			
			//TODO: HY Add
			kpiAvgValue = ROUND(MEAN(kpi.getFullname(), NAMEDWINDOW(windowName)).VALUE(),1);
			
			//NSM_VALUE deltaYesterday = ROUND(DELTA(kpi.getFullname(), PREVIOUSDAY(1)).VALUE(), 2);
			deltaAveKpi = curValue.MINUS(kpiAvgValue);
			
			degradePct = deltaAveKpi.DIVIDEDBY(kpiAvgValue).MULTIPLIEDBY(100);
			
			deltaThres = NAMEDTHRESHOLD(kpi.getFullname() + "_DeltaThreshold");
			
			if (122 == trigMethod)
			{
			  curValue = degradePct;
			  thres = deltaThres;
			}
						
			

		}
		
		//Use Top N to narrow down the number of tasks
		//Desc - Highest first
		//Asc - Lowest first
		
		boolean descendingTopN = false;
		
		if ( ('g' == kpi.getCondition()) || ('>' == kpi.getCondition())) {
			
			descendingTopN = true;
		}
		
		Check checkTopN = new Check(ISINTOPN(NAMEDTHRESHOLD("KPI_TOPN"), METRICVALUE(kpi.getFullname()), descendingTopN) );

		//End
		
		Check check = new Check(getCheckCondition(trigMethod, kpi.getCondition(), curValue, thres, devMean));

		if (isDebugMode) {
		
			if (122 == trigMethod)
			{
				check.onTrue().output(TOKENISEDSTRING("Check = TRUE | Ave KPI Value = '$1' | Trigger = '$2' | "
						+ "Condition = '$3' | DeltaThreshold = '$4' | DeltaPct = '$5' | KPI Value = '$6'", 
						kpiAvgValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), thres, curValue, oldCurValue));

				check.onFalse().output(TOKENISEDSTRING("Check = FALSE | Ave KPI Value = '$1' | Trigger = '$2' | "
						+ "Condition = '$3' | DeltaThreshold = '$4' | DeltaPct = '$5' | KPI Value = '$6'", 
						kpiAvgValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), thres, curValue, oldCurValue));
			} 
			else 
			{

				check.onTrue().output(TOKENISEDSTRING("Check = TRUE | KPI Value = '$1' | Trigger = '$2' | Condition = '$3' | Threshold = '$4' | Mean = '$5'", 
						curValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), thres, devMean));

				check.onFalse().output(TOKENISEDSTRING("Check = FALSE | KPI Value = '$1' | Trigger = '$2' | Condition = '$3' | Threshold = '$4' | Mean = '$5'", 
						curValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), thres, devMean));
			}
		}
		
		//TODO: HY Add - to check the delta threshold only if the threshold has been breached
		Check newCheck = new Check(getCheckCondition(trigMethod, kpi.getCondition(), degradePct, deltaThres, devMean));
		
		if (132 == trigMethod)
		{
			//check.onTrue().newCheck(getCheckCondition(trigMethod, kpi.getCondition(), degradePct, deltaThres, devMean));
			//Check newCheck = new Check(getCheckCondition(trigMethod, kpi.getCondition(), degradePct, deltaThres, devMean));
			check.onTrue().addCheck(newCheck);

			if (isDebugMode) {

				newCheck.onTrue().output(TOKENISEDSTRING("Check = TRUE | Ave KPI Value = '$1' | Trigger = '$2' | "
						+ "Condition = '$3' | DeltaThreshold = '$4' | DeltaPct = '$5'", 
						kpiAvgValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), deltaThres, degradePct));

				newCheck.onFalse().output(TOKENISEDSTRING("Check = FALSE | Ave KPI Value = '$1' | Trigger = '$2' | "
						+ "Condition = '$3' | DeltaThreshold = '$4' | DeltaPct = '$5'", 
						kpiAvgValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), deltaThres, degradePct));
			}

			newCheck.onTrue().newAction(RAISEEVENT(
					event, 
					getDesc(kpi, trigMethod, curValue, thres, deltaThres, devMean, level),
					PROPERTY("KPI Value", TOKENISEDSTRING("$1" +  kpi.getUnit(), ROUND(curValue,2))),
					PROPERTY("KPI Name", kpi.getFullname()),
					PROPERTY("Category", kpi.getCategory()),
					// 130503/DL -Added SiteName and ControllerName
					PROPERTY("Site Name", PARENTNAME("Site")),
					PROPERTY("Controller Name", PARENTNAME("Controller")),
					PROPERTY("Level", level),
					PROPERTY("Element", ELEMENTNAME()),
					PROPERTY("Technology", ELEMENTCATEGORY()),
					PROPERTY("Check method", getCheckMethod(kpi.getTriggerMethod()))
					//PROPERTY("Vendor", PARAMETERSTRING("Vendor"))
					));
		} else {
			
			check.onTrue().newAction(RAISEEVENT(
					event, 
					getDesc(kpi, trigMethod, curValue, thres, deltaThres, devMean, level),
					// TODO: HY mod use oldCurValue to cater for trigMethod 122
					PROPERTY("KPI Value", TOKENISEDSTRING("$1" +  kpi.getUnit(), ROUND(oldCurValue,2))),
					PROPERTY("KPI Name", kpi.getFullname()),
					PROPERTY("Category", kpi.getCategory()),
					// 130503/DL -Added SiteName and ControllerName
					PROPERTY("Site Name", PARENTNAME("Site")),
					PROPERTY("Controller Name", PARENTNAME("Controller")),
					PROPERTY("Level", level),
					PROPERTY("Element", ELEMENTNAME()),
					PROPERTY("Technology", ELEMENTCATEGORY()),
					PROPERTY("Check method", getCheckMethod(kpi.getTriggerMethod()))
					//PROPERTY("Vendor", PARAMETERSTRING("Vendor"))
					));
		}
		
		
		checkTopN.onTrue().addCheck(check);
		
		return checkTopN;
	}
	
	/**
	 * @param kpi
	 * @param windowName
	 * @param level
	 * @return
	 * 
	 * DESC:
	 * Create Loop and raise Event if Loop/check is true
	 */
	public static Loop getLoop(KPI kpi, String windowName, String level) {
		
		NSM_VALUE curValue = METRIC(kpi.getFullname()).VALUE();
		
		NSM_RELATIONALEVENT event = RELATIONALEVENT(NSM_EVENTTYPE.PERFORMANCEALERT(kpi.getEventCode()));
		
		NSM_VALUE thres = LITERALVALUE(0);
		
		NSM_VALUE devMean = LITERALVALUE(0);
		
		int trigMethod = kpi.getTriggerMethod();
		
		if ('D' != trigMethod) {
			
			thres =  NAMEDTHRESHOLD(kpi.getFullname() + "_PerfThreshold");
		
		}
		
		if ('T' != trigMethod) {
		
			NSM_VALUE deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");	
		
			devMean = DEVIATIONFROMMEAN(kpi.getFullname(), NAMEDWINDOW(windowName), kpi.getCondition()=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
		}	
	
		//Use Top N to narrow down the number of tasks
		boolean descendingTopN = false;
		
		if ( ('g' == kpi.getCondition()) || ('>' == kpi.getCondition())) {
			
			descendingTopN = true;
		}
		
		NSM_ISINTOPN isTopN = ISINTOPN(NAMEDTHRESHOLD("KPI_TOPN"), METRICVALUE(kpi.getFullname()), descendingTopN);
		
		NSM_IS isChkCond = getCheckCondition(trigMethod, kpi.getCondition(), curValue, thres, devMean);
		
		//End
						
		Loop loop = new Loop(NEIGHBORS());
		
		Check checkLoop = loop.onEach().newCheck(isTopN).onTrue().newCheck(isChkCond);
     
		if (isDebugMode) {
			
			checkLoop.onTrue().output(TOKENISEDSTRING("Loop = TRUE | KPI Value = '$1' | Trigger = '$2' | Condition = '$3' | Threshold = '$4' | Mean = '$5'", 
				curValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), thres, devMean));
		
			checkLoop.onFalse().output(TOKENISEDSTRING("Loop = FALSE | KPI Value = '$1' | Trigger = '$2' | Condition = '$3' | Threshold = '$4' | Mean = '$5'", 
				curValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), thres, devMean));
		}
		
		checkLoop.onTrue().newAction(RAISEEVENT(
				event, 
				getDescRelational(kpi, trigMethod, curValue, thres, devMean, level),
				PROPERTY("KPI Value", TOKENISEDSTRING("$1" +  kpi.getUnit(), ROUND(curValue,2))),
				PROPERTY("KPI Name", kpi.getFullname()),
				PROPERTY("Source target", SOURCESTRING(ELEMENTNAME())),
				PROPERTY("Category", kpi.getCategory()),
				//130503/DL -Added SiteName and ControllerName
				PROPERTY("Site Name", PARENTNAME("Site")),
				PROPERTY("Controller Name", PARENTNAME("Controller")),
				PROPERTY("Level", level),
				PROPERTY("Technology", ELEMENTCATEGORY()),
				PROPERTY("Check method", getCheckMethod(kpi.getTriggerMethod()))
				//PROPERTY("Vendor", PARAMETERSTRING("Vendor"))
				));
        
		return loop; 
	}
	
}
