package utility;

import java.util.Calendar;

import rules.GSM_Ericsson_5WorkDays;
import rules.GSM_Ericsson_7Days;
import rules.WCDMA_Ericsson_5WorkDays;
import rules.WCDMA_Ericsson_7Days;

import com.actix.rules.flow.Rule;

public class RuleRunner 
{
	public static void main(String[] args) throws Exception 
	{
		
		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMdd");
		
		Calendar calendar = Calendar.getInstance();
			
		String contextDate = "20131125";
		
		calendar.setTime(dateFormat.parse(contextDate));
		
		Rule rules1 = new WCDMA_Ericsson_7Days();
		Rule rules2 = new GSM_Ericsson_7Days();
		Rule rules3 = new WCDMA_Ericsson_5WorkDays();
		Rule rules4 = new GSM_Ericsson_5WorkDays();
		
		rules1.run(calendar.getTime());
		rules2.run(calendar.getTime());
		rules3.run(calendar.getTime());
		rules4.run(calendar.getTime());

		//rules.run(calendar.getTime(),"MDH030180M");

	}
}
