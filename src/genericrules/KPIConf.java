/*
 * Author: Darren Loh
 * History: 
 * 120712/DL -File creation. 
 */
package genericrules;

import com.actix.analytics.objects.NSM_EVENT;
import com.actix.analytics.objects.NSM_RELATIONALEVENT;

public class KPIConf {
	
	private String vendor = "";
	
	private String technology = "";
	
	private String taskQName = "";
	
	private KPI[] kpi_list;
	
	private KPI[] kpiRel_list;
	
	private String[] cm_list;
	
	private String windowName = "";
	
	private boolean testMode = false;

	private boolean isNullGeneral = true;
	
	private boolean isNullCM = true;
	
	private boolean isNullKpi = true;
	
	private boolean isNullRelKpi = true;
	
	private NSM_EVENT[] pmEventList = null; 
	
	private NSM_RELATIONALEVENT[] pmRelEventList = null;
	
	public boolean isTestMode() {
		return testMode;
	}
	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public String getTaskQName() {
		return taskQName;
	}
	public void setTaskQName(String taskQName) {
		this.taskQName = taskQName;
	}

	public KPI[] getKpi_list() {
		return kpi_list;
	}

	public void setKpi_list(KPI kpi_list[]) {
		this.kpi_list = kpi_list;
	}

	public String[] getCm_List() {
		return cm_list;
	}

	public void setCm_List(String[] cm_list) {
		this.cm_list = cm_list;
	}

	public KPI[] getKpiRel_list() {
		return kpiRel_list;
	}

	public void setKpiRel_list(KPI[] kpiRel_list) {
		this.kpiRel_list = kpiRel_list;
	}
	public String getWindowName() {
		return windowName;
	}
	public void setWindowName(String windowName) {
		this.windowName = windowName;
	}
	public boolean isNullGeneral() {
		return isNullGeneral;
	}
	public void setNullGeneral(boolean isNullGeneral) {
		this.isNullGeneral = isNullGeneral;
	}
	public boolean isNullCM() {
		return isNullCM;
	}
	public void setNullCM(boolean isNullCM) {
		this.isNullCM = isNullCM;
	}
	public boolean isNullKpi() {
		return isNullKpi;
	}
	public void setNullKpi(boolean isNullKpi) {
		this.isNullKpi = isNullKpi;
	}
	public boolean isNullRelKpi() {
		return isNullRelKpi;
	}
	public void setNullRelKpi(boolean isNullRelKpi) {
		this.isNullRelKpi = isNullRelKpi;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getTechnology() {
		return technology;
	}
	public void setTechnology(String technology) {
		this.technology = technology;
	}
	public NSM_EVENT[] getPmEventList() {
		return pmEventList;
	}
	public void setPmEventList(NSM_EVENT[] pmEventList) {
		this.pmEventList = pmEventList;
	}
	public NSM_RELATIONALEVENT[] getPmRelEventList() {
		return pmRelEventList;
	}
	public void setPmRelEventList(NSM_RELATIONALEVENT[] pmRelEventList) {
		this.pmRelEventList = pmRelEventList;
	}
	public int getKpiCount() {
		
		if( null != kpi_list) {
				
				return kpi_list.length;
				
		} else {
			
			return 0;
		}

	}
	
	public int getKpiRelCount() {
		
		if( null != kpiRel_list) {
			
			return kpiRel_list.length;
			
		} else {
			
			return 0;
		}
			
	}
}
