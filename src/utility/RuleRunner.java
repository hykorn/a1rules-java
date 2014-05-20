package utility;

import java.util.Calendar;

import com.actix.rules.flow.Rule;

public class RuleRunner 
{
	public static void main(String[] args) throws Exception 
	{
		
		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMdd");
		
		Calendar calendar = Calendar.getInstance();
			
		String contextDate = "20130213";
		
		calendar.setTime(dateFormat.parse(contextDate));
		
		//Rule rules = new H2G();
		
//		rules.run(calendar.getTime());

		//rules.run(calendar.getTime(),"MDH030180M");

	}
}
