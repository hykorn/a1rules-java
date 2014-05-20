/*
 * Author: Darren Loh
 * History: 
 * 120712/DL -File creation. 
 * 121109/DL -Added taskActionCode, taskCauseCode
 */
package genericrules;

public class KPI {
	
	private String taskCode = "";
	
	private String eventCode = "";
	
	private String taskActionCode = "";
	
	private String taskCauseCode = "";
	
	private String fullname = "";
	
	private char condition = ' ';
	
	private String unit = "";
	
	private int triggerMethod ;
	
	private String vendorName = "";
	
	private String technology = "";
	
	private String category = "";
	
	//default values
	public KPI() {
		setTaskCode("NotSet");
		setEventCode("NotSet");
		setTaskActionCode("NotSet");
		setTaskCauseCode("NotSet");
		setFullname("NotSet");
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public char getCondition() {
		return condition;
	}

	public void setCondition(char condition) {
		this.condition = condition;
	}

	public int getTriggerMethod() {
		return triggerMethod;
	}

	public void setTriggerMethod(int triggerMethod) {
		this.triggerMethod = triggerMethod;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTaskCode() {
		return taskCode;
	}

	public void setTaskCode(String taskCode) {
		this.taskCode = taskCode;
	}

	public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public String getTaskActionCode() {
		return taskActionCode;
	}

	public void setTaskActionCode(String taskActionCode) {
		this.taskActionCode = taskActionCode;
	}

	public String getTaskCauseCode() {
		return taskCauseCode;
	}

	public void setTaskCauseCode(String taskCauseCode) {
		this.taskCauseCode = taskCauseCode;
	}

}
