package izi_repository.domain.result;

import izi_repository.domain.result.tasksetting.TaskSetting;
import izi_repository.utils.OutputUtils;

/**
 * Domain object representing result of query.
 * 
 * @author Tomas Marek
 * 
 */
public class Result {

	private String ruleId;
	private String docId;
	private String docName;
	private String reportUri;
	private String database;
	private String table;
	private String text;
	private Rule rule;
	private TaskSetting taskSetting;

	private double[][] queryCompliance;

	/**
	 * @return the ruleId
	 */
	public String getRuleId() {
		return ruleId;
	}

	/**
	 * @param ruleId
	 *            the ruleId to set
	 */
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	/**
	 * @return the docId
	 */
	public String getDocId() {
		return docId;
	}

	/**
	 * @param docId
	 *            the docId to set
	 */
	public void setDocId(String docId) {
		this.docId = docId;
	}

	/**
	 * @return the docName
	 */
	public String getDocName() {
		return docName;
	}

	/**
	 * @param docName
	 *            the docName to set
	 */
	public void setDocName(String docName) {
		this.docName = docName;
	}

	/**
	 * @return the reportUri
	 */
	public String getReportUri() {
		return reportUri;
	}

	/**
	 * @param reportUri
	 *            the reportUri to set
	 */
	public void setReportUri(String reportUri) {
		this.reportUri = reportUri;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}

	/**
	 * @param table
	 *            the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}

	/**
	 * @param rule
	 *            the rule to set
	 */
	public void setRule(Rule rule) {
		this.rule = rule;
	}

	/**
	 * @return the queryComplience
	 */
	public double[][] getQueryCompliance() {
		return queryCompliance;
	}

	/**
	 * @param queryCompliance
	 *            the queryComplience to set
	 */
	public void setQueryCompliance(double[][] queryCompliance) {
		this.queryCompliance = queryCompliance;
	}

	/**
	 * @return the taskSetting
	 */
	public TaskSetting getTaskSetting() {
		return taskSetting;
	}

	/**
	 * @param taskSetting
	 *            the taskSetting to set
	 */
	public void setTaskSetting(TaskSetting taskSetting) {
		this.taskSetting = taskSetting;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append("<Hit docID=\"" + docId + "\" ruleID=\"" + ruleId + "\" docName=\"" + docName
				+ "\" reportURI=\"" + reportUri + "\" database=\"" + database + "\" table=\"" + table
				+ "\" queryCompliance=\"" + OutputUtils.getQueryComplianceForOutput(queryCompliance) + "\" >");
		if (text != null) {
			ret.append("<Text><![CDATA[" + text + "]]></Text>");
		}
		if (rule != null) {
			ret.append(rule.toString());
		}
		if (taskSetting != null) {
			ret.append(taskSetting.toString());
		}
		ret.append("</Hit>");
		return ret.toString();
	}

}