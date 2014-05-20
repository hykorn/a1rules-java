package rules;

import genericrules.GenericRules;
import genericrules.KPIConf;

import com.actix.analytics.api.Frequency;
import com.actix.analytics.objects.NSM_STRING;
import com.actix.rules.flow.Publish;
import com.actix.rules.flow.Rule;
import com.actix.rules.flow.nodes.FlowController;

import core.Branch;

@Publish
public class GSM_Ericsson_5WorkDays extends Rule {

	private String filename = "Java.GSM.ERICSSON_5D.xml";
	
	//Debugging mode
	private boolean isDebugMode = false;
	
	@Override
	public Frequency runFrequency() {
		return Frequency.DAILY;
	}
	
	@Override
	public String elementLevel() {
		return "Sector";
	}
	
	@Publish
	protected void defineFlow(FlowController start) {
		
		KPIConf kpiList = null;
		
		//Get data from XML and store in KPIConf, KPI objects
		XMLParser xmlparser = new XMLParser();
		
		kpiList = xmlparser.getKPIDataFromXML(filename);
		
		NSM_STRING elementName = NAMEDSTRING("Single Element To Check");
		
		//NSM_STRING elementName = LITERALSTRING("none");
		
		//Creating dummy events, tasks for test mode 
		if (kpiList.isTestMode()) {
			
			GenericRules.raiseDummyEventTask(start, elementName);
			
		} else {
			
			//XML General Tag must not be null & either KPI or relational KPI must not be null
			if( !kpiList.isNullGeneral() && (!kpiList.isNullKpi() || !kpiList.isNullRelKpi()) ){
				
				//Flow to be filtered by vendor? technology? both? or none?
				FlowController elementToCheck = GenericRules.getFilteredFlow(kpiList, start, elementName);
				
				//Initiating NSM_Event objects
				GenericRules.populateAllPmEvent(kpiList);
				
				//Create conditional checks and add into branch
				Branch checkBranch = GenericRules.createBranch(kpiList); 
				
				//Append task creation at the end of the branch
				GenericRules.addTaskIntoBranch(kpiList, checkBranch);
				
				//Add branch into flow controller. Without this line, no rules will be triggered
				GenericRules.addBranchIntoFlow(kpiList, elementToCheck, checkBranch);
				
				
			} else {
				
				//Debugging purpose
				if (isDebugMode) {
					
					start.output(TOKENISEDSTRING("Incomplete XML. | XML-ReadGeneral = '$1' | XML-ReadKPI = '$2' | XML-ReadKPIRel = '$3' | XML-ReadCM = '$4'", 
							LITERALSTRING(kpiList.isNullGeneral() ? "FALSE" : "TRUE"),
							LITERALSTRING(kpiList.isNullKpi() ? "FALSE" : "TRUE"),
							LITERALSTRING(kpiList.isNullRelKpi() ? "FALSE" : "TRUE"),
							LITERALSTRING(kpiList.isNullCM() ? "FALSE" : "TRUE"))); 
				}			
			}
		}
		
	}
	
}