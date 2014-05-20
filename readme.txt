1. Fixed (GenericRules.java > raiseGenericTask) - the issue of creating a long list of junks “Default Action” in the Task Action drop down list.

-It now links all Tasks to Action "Investigate Further" by default.

-Do NOT execute 06_AU_TaskActions_xxx.sql if using this version.

2. Fixed GenericCheck.java (line 67). From AND condition to OR condition. 