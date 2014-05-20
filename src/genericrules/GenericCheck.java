/*
 * Author: Darren Loh
 * History: 
 * 120712/DL -File creation. 
 * 121016/DL -Remove event code from event properties as it is already auto generated
 * 130313/DL -Added TopN Support
 * 130503/DL -Added SiteName and ControllerName
 */
package genericrules;

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
	private static NSM_IS getCheckCondition(int triggerMethod, char condition, NSM_VALUE curValue, NSM_VALUE thres, NSM_VALUE devMean){
		
		NSM_IS checkCondition = null;
		
		if ('g' == condition) {
			
			switch(triggerMethod) {
			
				case 1: checkCondition = IS(curValue,GREATERTHANOREQUALTO(thres)); break;
				
				case 2: checkCondition = IS(curValue,GREATERTHANOREQUALTO(devMean)); break;
				
				case 112: checkCondition = IS(curValue,GREATERTHANOREQUALTO(thres).AND(GREATERTHANOREQUALTO(devMean))); break;
				
				case 102: checkCondition = IS(curValue,GREATERTHANOREQUALTO(thres).OR(GREATERTHANOREQUALTO(devMean))); break;
			}
			
		} else if ('l' == condition) {
			
			switch(triggerMethod) {
			
				case 1 : checkCondition = IS(curValue,LESSTHANOREQUALTO(thres)); break;
			 
				case 2 : checkCondition = IS(curValue,LESSTHANOREQUALTO(devMean)); break;
			
				case 112: checkCondition = IS(curValue,LESSTHANOREQUALTO(thres).AND(LESSTHANOREQUALTO(devMean))); break;
				
				case 102: checkCondition = IS(curValue,LESSTHANOREQUALTO(thres).AND(LESSTHANOREQUALTO(devMean))); break;
			
			}
			
		} else if ('>' == condition) {
			
			switch(triggerMethod) {
			
			case 1 : checkCondition = IS(curValue,GREATERTHAN(thres)); break;
		
			case 2: checkCondition = IS(curValue,GREATERTHAN(devMean)); break;
		
			case 112: checkCondition = IS(curValue,GREATERTHAN(thres).AND(GREATERTHAN(devMean))); break;
			
			case 102: checkCondition = IS(curValue,GREATERTHAN(thres).OR(GREATERTHAN(devMean))); break;
		
			}
						
			
		}else if ('<' == condition) {
			
			switch(triggerMethod) {
			
			case 1 : checkCondition = IS(curValue,LESSTHAN(thres)); break;
		
			case 2 : checkCondition = IS(curValue,LESSTHAN(devMean)); break;
		
			case 112 : checkCondition = IS(curValue,LESSTHAN(thres).AND(LESSTHAN(devMean))); break;
			
			case 102 : checkCondition = IS(curValue,LESSTHAN(thres).OR(LESSTHAN(devMean))); break;
		
			}
			
			
		} else if ('=' == condition){
			
			switch(triggerMethod) {
			
			case 1 : checkCondition = IS(curValue,EQUALTO(thres)); break;
		
			case 2 : checkCondition = IS(curValue,EQUALTO(devMean)); break;
		
			case 112 : checkCondition = IS(curValue,EQUALTO(thres).AND(EQUALTO(devMean))); break;
			
			case 102 : checkCondition = IS(curValue,EQUALTO(thres).OR(EQUALTO(devMean))); break;
		
			}
				
		} else if ('!' == condition ){
			
			switch(triggerMethod) {
			
			case 1 : checkCondition = IS(curValue,NOTEQUALTO(thres)); break;
		
			case 2 : checkCondition = IS(curValue,NOTEQUALTO(devMean)); break;
		
			case 112 : checkCondition = IS(curValue,NOTEQUALTO(thres).AND(NOTEQUALTO(devMean))); break;
			
			case 102 : checkCondition = IS(curValue,NOTEQUALTO(thres).OR(NOTEQUALTO(devMean))); break;
		
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
	private static NSM_TOKENISEDSTRING getDesc(KPI kpi, int triggerMethod, NSM_VALUE curValue, NSM_VALUE thres, NSM_VALUE devMean, String level) {
		
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
			
			str =  "Threshold Or Deviation Mean";
			
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
		
		int trigMethod = kpi.getTriggerMethod();
		
		if (2 != trigMethod) { //if Trigger method is not "Deviation Mean only"
			
			thres =  NAMEDTHRESHOLD(kpi.getFullname() + "_PerfThreshold");
		
		}
		
		if (1 !=trigMethod) { //if Trigger method is not "Threshold only"
		
			NSM_VALUE deviationConstant = NAMEDTHRESHOLD("Deviation_Constant");	
		
			devMean = DEVIATIONFROMMEAN(kpi.getFullname(), NAMEDWINDOW(windowName), kpi.getCondition()=='>' ? deviationConstant : deviationConstant.MULTIPLIEDBY(-1)).VALUE();
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
		
			check.onTrue().output(TOKENISEDSTRING("Check = TRUE | KPI Value = '$1' | Trigger = '$2' | Condition = '$3' | Threshold = '$4' | Mean = '$5'", 
				curValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), thres, devMean));
		
			check.onFalse().output(TOKENISEDSTRING("Check = FALSE | KPI Value = '$1' | Trigger = '$2' | Condition = '$3' | Threshold = '$4' | Mean = '$5'", 
				curValue, LITERALVALUE(trigMethod), LITERALSTRING(Character.toString(kpi.getCondition())), thres, devMean));
		}

		check.onTrue().newAction(RAISEEVENT(
				event, 
				getDesc(kpi, trigMethod, curValue, thres, devMean, level),
				PROPERTY("KPI Value", TOKENISEDSTRING("$1" +  kpi.getUnit(), ROUND(curValue,2))),
				PROPERTY("KPI Name", kpi.getFullname()),
				PROPERTY("Category", kpi.getCategory()),
				// 130503/DL -Added SiteName and ControllerName
				PROPERTY("Site Name", PARENTNAME("Site")),
				PROPERTY("Controller Name", PARENTNAME("Controller")),
				PROPERTY("Level", level),
				PROPERTY("Element", ELEMENTNAME()),
				PROPERTY("Technology", ELEMENTCATEGORY()),
				PROPERTY("Check method", getCheckMethod(kpi.getTriggerMethod())),
				PROPERTY("Vendor", PARAMETERSTRING("Vendor"))
				));
		
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
				PROPERTY("Check method", getCheckMethod(kpi.getTriggerMethod())),
				PROPERTY("Vendor", PARAMETERSTRING("Vendor"))
				));
        
		return loop; 
	}
	
}
