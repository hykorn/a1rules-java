/*
 * Author: Darren Loh
 * History: 
 * 120712/DL -File creation.
 * 121109/DL -Added "eventcode", "taskactioncode", "taskcausecode" to tagName_KPI
 * 140423/DL -Clear list before adding into the list. Fixed static memory issue if there is multiple rules in a single run.
 */
package rules;


import genericrules.KPI;
import genericrules.KPIConf;

import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.*;
 
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
 
public class XMLParser{
	
	//private String filename = "kpi.xml";
	
	private static KPIConf kpi_list;
	
	private final String parentTagName_kpi = "kpi";
	
	private final String parentTagName_kpiRel = "kpi_rel";
	
	private final String parentTagName_cm = "cm";
	
	private final String parentTagName_general = "general";
	
	private static String[] tagName_kpi = {"taskcode", "eventcode", "taskactioncode", "taskcausecode", "fullname", "vendorName", "technology", "condition", "unit", "triggerMethod", "category"};
	
	private static String[] tagName_cm = {"name"};
	
	private static String[] tagName_general = {"testMode", "technology", "vendor", "windowName", "taskQueueName" };
       
	private static ArrayList<String[]> tagValueListGeneral = new ArrayList<String[]>();
	
	private static ArrayList<String[]> tagValueListKPI = new ArrayList<String[]>();
	
	private static ArrayList<String[]> tagValueListKPIRel = new ArrayList<String[]>();
	
	private static ArrayList<String[]> tagValueListCM = new ArrayList<String[]>();
	
	private boolean isDebugMode = false;
	
	//Troubleshooting purpose
//    public static void main (String[] args){
//    	
//    	XMLParser xmlparser = new XMLParser();
//    	
//    	xmlparser.getKPIDataFromXML("kpi_NSN2G.xml");
//    	
//    	printData(tagName_general, tagValueListGeneral);
//    	
//    	printData(tagName_kpi, tagValueListKPI);
//    	
//    	printData(tagName_kpi, tagValueListKPIRel);
//     
//    }

	
    /**
     * @return
     * 
     * DESC:
     * This is main caller to parse the XML
     */
    public KPIConf getKPIDataFromXML(String filename) {

    	InputStream is = this.getClass().getResourceAsStream(filename);  
  	
    	try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

            Document doc = docBuilder.parse (is);
     
            // normalize text representation
            doc.getDocumentElement().normalize ();
 
            //140423/DL - Clear list before adding new data to the list
			tagValueListKPI.clear();
            tagValueListKPIRel.clear();
            tagValueListCM.clear();
            tagValueListGeneral.clear();

           //Get data based on different tags. Tags must be declared top of the class
           getData(doc, parentTagName_kpi, tagName_kpi, tagValueListKPI);
           
           getData(doc, parentTagName_kpiRel, tagName_kpi, tagValueListKPIRel);
           
           getData(doc, parentTagName_cm, tagName_cm, tagValueListCM);
           
           getData(doc, parentTagName_general, tagName_general, tagValueListGeneral);
           
            
        }catch (SAXParseException err) {
        
        	if(isDebugMode) {
        	 System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
        	 
        	 System.out.println(" " + err.getMessage ());
        	}

        }catch (SAXException e) {
        	
        	if(isDebugMode) {
        		Exception x = e.getException ();
        		
        		((x == null) ? e : x).printStackTrace ();
        	}
        }catch (Throwable t) {
        	
        	if(isDebugMode) {
        		t.printStackTrace ();
        	}
        }
    	
    	//Populate tag values into KPIConf object 
    	kpi_list = new KPIConf();
    	
    	if(tagValueListGeneral.size() > 0) {
    		populateDataKPI_General(); 		
    		
    		//by default NullGeneral is true
    		kpi_list.setNullGeneral(false);
    	} 
    	
        if(tagValueListCM.size() > 0) {
        	populateDataKPI_CM();
        	
        	//by default NullCM is true
        	kpi_list.setNullCM(false);
        }
        
        if(tagValueListKPI.size() > 0) {
        	populateDataKPI_KPI(tagValueListKPI, false);
        	
        	//by default NullKPI is true
        	kpi_list.setNullKpi(false);
        }
        
        if(tagValueListKPIRel.size() > 0) {
        	populateDataKPI_KPI(tagValueListKPIRel, true);
        	
        	//by default NullRelKPI is true
        	kpi_list.setNullRelKpi(false);
        }
        
        return kpi_list;
    }

    /**
     * Populate values from General tag into object
     */
    private static void populateDataKPI_General(){
    	              
           	String[] arrayTagValue = (String[]) tagValueListGeneral.get(0);
           	
           	kpi_list.setTestMode(arrayTagValue[0].toUpperCase().equals("YES"));
        	      	
           	kpi_list.setTechnology(arrayTagValue[1]);
        	
        	kpi_list.setVendor(arrayTagValue[2]);
        	
        	kpi_list.setWindowName(arrayTagValue[3]);
        	
        	kpi_list.setTaskQName(arrayTagValue[4]);
           	   	
    }
    
    /**
     * Populate values from CM tag into object
     */
    private static void populateDataKPI_CM(){
    	
    	String[] cm_list = new String[tagValueListCM.size()];
    	
    	 for(int childTagValueCount=0; childTagValueCount < tagValueListCM.size() ; childTagValueCount++){
              
          	String[] arrayTagValue = (String[]) tagValueListCM.get(childTagValueCount);
          	
          	cm_list[childTagValueCount] = arrayTagValue[0];
          	
          }
    	 
    	 kpi_list.setCm_List(cm_list);
           	
    }
    
    /**
     * Convert condition from xml into char 
     */
    private static char getCharCondition(String condition){
    	
    	char charCondition = ' ';
    	
		if(condition.equals("lt"))  charCondition = '<';
		
		else if(condition.equals("gt"))  charCondition = '>';
		
		else if(condition.equals("eq"))  charCondition = '=';
		
		else if(condition.equals("ge"))  charCondition = 'g';
		
		else if(condition.equals("le"))  charCondition = 'l';
		
		else if(condition.equals("nq"))  charCondition = '!';
		
		return charCondition;
    }
    
    /**
     * Populate values from KPI tag into object
     */
    private static void populateDataKPI_KPI(ArrayList<String[]> kpiList, boolean isRel){
    	 
    	KPI listofkpi[] = new KPI[kpiList.size()];
    	
    	 for(int childTagValueCount=0; childTagValueCount < kpiList.size() ; childTagValueCount++){
             
    		KPI newkpi = new KPI();
    		
         	String[] arrayTagValue = (String[]) kpiList.get(childTagValueCount);
         		
         	newkpi.setTaskCode(arrayTagValue[0]);
         	
         	newkpi.setEventCode(arrayTagValue[1]);
         	
         	newkpi.setTaskActionCode(arrayTagValue[2]);
         	
         	newkpi.setTaskCauseCode(arrayTagValue[3]);
         	
         	newkpi.setFullname(arrayTagValue[4]);
         	
         	newkpi.setVendorName(arrayTagValue[5]);
           	
         	newkpi.setTechnology(arrayTagValue[6]);
         	
         	newkpi.setCondition(getCharCondition(arrayTagValue[7]));
         	
         	newkpi.setUnit(arrayTagValue[8]);
         	
         	newkpi.setTriggerMethod(Integer.parseInt(arrayTagValue[9]));
         	
         	newkpi.setCategory(arrayTagValue[10]);
         	
         	listofkpi[childTagValueCount] = newkpi;
         	
         }
    	 
    	 if(isRel) {
    		 kpi_list.setKpiRel_list(listofkpi);
    	 } else {
    		 kpi_list.setKpi_list(listofkpi);
    	 }
    }
        
    /**
     * @param childTagName
     * @param childTagValueList
     * 
     * DESC:
     * This is debugging purpose method to check xml is parsed properly
     * Print data read from xml
     * TODO: to print data from the KPIConf object rather the ArrayList
     */
    @SuppressWarnings("unused")
	private static void printData(String[] childTagName, ArrayList<?> childTagValueList){
    	
        for(int childTagValueCount=0; childTagValueCount < childTagValueList.size() ; childTagValueCount++){
            
        	String[] arrayTagValue = (String[]) childTagValueList.get(childTagValueCount);
        	
        	for(int arrayTagValueCount=0; arrayTagValueCount < arrayTagValue.length ; arrayTagValueCount++){
        		
        		System.out.println(childTagName[arrayTagValueCount] + " = " + arrayTagValue[arrayTagValueCount]);
        	}

        }
    }
    
    /**
     * @param doc
     * @param tagName
     * @param childTagName
     * @param childTagValueList
     * @return
     * 
     * DESC:
     * This is core method to retrieve node values from individual xml tag
     */
    private static int getData(Document doc, String tagName, String[] childTagName, ArrayList<String[]> childTagValueList){
    	
        NodeList nodelist = doc.getElementsByTagName(tagName);
        
        int totalNodes = nodelist.getLength();
        
        for(int nodesCount=0; nodesCount < totalNodes ; nodesCount++){
	
            Node firstNode = nodelist.item(nodesCount);
            
            if( firstNode.getNodeType() == Node.ELEMENT_NODE ){

            	String[] arrayTagValue = new String[childTagName.length];
            	
                Element firstPersonElement = (Element)firstNode;

                for(int childTagNameCount=0; childTagNameCount < childTagName.length ; childTagNameCount++){
                	
	                NodeList list = firstPersonElement.getElementsByTagName(childTagName[childTagNameCount]);
	                
	                if( list.getLength() > 0) {  //if tag is missing
	                  Element element = (Element)list.item(0);
	               
	                  NodeList textFNList = element.getChildNodes();
	                              	
	                  if (textFNList.getLength() > 0) { //if tag value is empty
	                	  arrayTagValue[childTagNameCount] =  ((Node)textFNList.item(0)).getNodeValue().trim();  
	                  } else {
	                	  arrayTagValue[childTagNameCount] = "";
	                  }
	                } else {
	                	
	                	arrayTagValue[childTagNameCount] = "";
	                }
	                
                }

                childTagValueList.add(arrayTagValue);
                
            }//end of if clause

        }//end of for loop with s var
        
        return totalNodes;
    }
   
   
}
