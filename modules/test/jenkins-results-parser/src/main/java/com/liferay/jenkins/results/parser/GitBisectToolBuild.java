/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.jenkins.results.parser;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import org.dom4j.DocumentException;
import org.dom4j.Element;

import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class GitBisectToolBuild extends TopLevelBuild {

	@Override
	public Element getJenkinsReportElement() {
		if (_workspaceGitRepository == null) {
			throw new IllegalStateException(
				"Please set the workspace git repository");
		}

		if (_downstreamBuildDataList == null) {
			throw new IllegalStateException(
				"Please set the downstream build data list");
		}

		return Dom4JUtil.getNewElement(
			"html", null, getJenkinsReportHeadElement(),
			getJenkinsReportBodyElement());
	}

	public void setDownstreamBuildDataList(
		List<BuildData> downstreamBuildDataList) {

		_downstreamBuildDataList = downstreamBuildDataList;
	}

	public void setWorkspaceGitRepository(
		WorkspaceGitRepository workspaceGitRepository) {

		_workspaceGitRepository = workspaceGitRepository;
	}

	protected GitBisectToolBuild(String url) {
		this(url, null);
	}

	protected GitBisectToolBuild(String url, TopLevelBuild topLevelBuild) {
		super(url, topLevelBuild);
	}

	protected Element getJenkinsReportBodyElement() {
		String buildURL = getBuildURL();

		Element headingElement = Dom4JUtil.getNewElement(
			"h1", null, "Jenkins report for ",
			Dom4JUtil.getNewAnchorElement(buildURL, buildURL));

		Element subheadingElement = null;

		JSONObject jobJSONObject = getBuildJSONObject();

		String description = jobJSONObject.optString("description");

		if (!description.isEmpty()) {
			subheadingElement = Dom4JUtil.getNewElement("h2");

			try {
				Dom4JUtil.addRawXMLToElement(subheadingElement, description);
			}
			catch (DocumentException de) {
				throw new RuntimeException(
					"Unable to parse description HTML " + description, de);
			}
		}

		return Dom4JUtil.getNewElement(
			"body", null, headingElement, subheadingElement,
			getJenkinsReportSummaryElement(), getJenkinsReportTimelineElement(),
			getJenkinsReportTableElement(),
			Dom4JUtil.getNewElement(
				"p", null,
				Dom4JUtil.getNewElement(
					"em", null, "Indicates HEAD Commit (*)")));
	}

	protected Element getJenkinsReportHeadElement() {
		return Dom4JUtil.getNewElement(
			"head", null, getJenkinsReportHeadJQueryElement(),
			getJenkinsReportHeadScriptElement(),
			getJenkinsReportHeadStyleElement());
	}

	protected Element getJenkinsReportHeadJQueryElement() {
		Element jqueryElement = Dom4JUtil.getNewElement("script");

		jqueryElement.addAttribute("src", _JQUERY_URL);
		jqueryElement.addAttribute("type", "text/javascript");
		jqueryElement.addText("");

		return jqueryElement;
	}

	protected Element getJenkinsReportHeadScriptElement() {
		Element scriptElement = Dom4JUtil.getNewElement("script");

		scriptElement.addAttribute("type", "text/javascript");

		StringBuilder sb = new StringBuilder();

		sb.append("$(document).ready(function() {\n");
		sb.append("$('[data-toggle=\"toggle\"]').change(function(){\n");
		sb.append("$(this).parents().next('.hidden-row').toggle();\n");
		sb.append("var label = $(this).parent('td').find('label');\n");
		sb.append("var text = label.text();\n");
		sb.append("if (text == '+') { text = '-' }\n");
		sb.append("else { text = '+' }\n");
		sb.append("label.text(text);\n");
		sb.append("});\n");
		sb.append("});\n");

		scriptElement.addText(sb.toString());

		return scriptElement;
	}

	protected Element getJenkinsReportHeadStyleElement() {
		StringBuilder sb = new StringBuilder();

		sb.append("body {\n");
		sb.append("font-family: sans-serif;\n");
		sb.append("}\n");

		sb.append("canvas {\n");
		sb.append("display: block;\n");
		sb.append("height: 300px;\n");
		sb.append("width: 1900px;\n");
		sb.append("}\n");

		sb.append("table {\n");
		sb.append("width: 1200px;\n");
		sb.append("}\n");

		sb.append("table > caption, td, th {\n");
		sb.append("padding: 3px;\n");
		sb.append("text-align: left;\n");
		sb.append("}\n");

		sb.append("th {\n");
		sb.append("background-color: #CCCCCC;\n");
		sb.append("font-weight: bold;\n");
		sb.append("}\n");

		sb.append("td {\n");
		sb.append("background-color: #EEEEEE;\n");
		sb.append("max-width: 250px;\n");
		sb.append("overflow: hidden;\n");
		sb.append("text-overflow: ellipsis;\n");
		sb.append("white-space: nowrap;\n");
		sb.append("}\n");

		sb.append("td:nth-child(1) {\n");
		sb.append("text-align: center;\n");
		sb.append("width: 20px;\n");
		sb.append("}\n");

		sb.append("td:nth-child(3) {\n");
		sb.append("width: 250px;\n");
		sb.append("}\n");

		sb.append(".hidden-row {\n");
		sb.append("display: none;\n");
		sb.append("}\n");

		sb.append(".result-row tr td {\n");
		sb.append("background-color: #DDDDDD;\n");
		sb.append("}\n");

		sb.append(".result-row tr td label {\n");
		sb.append("cursor: pointer;\n");
		sb.append("display: block;\n");
		sb.append("}\n");

		sb.append("[data-toggle=\"toggle\"] {\n");
		sb.append("display: none;\n");
		sb.append("}\n");

		return Dom4JUtil.getNewElement("style", null, sb.toString());
	}

	protected Element getJenkinsReportTableBodyElement() {
		Element tableBodyElement = Dom4JUtil.getNewElement("tbody");

		List<Commit> historicalCommits =
			_workspaceGitRepository.getHistoricalCommits();

		Element tableRowContainerElement = null;

		Commit previousCommit = null;
		Integer previousCommitCount = 0;

		for (Commit commit : historicalCommits) {
			boolean first = false;

			if (historicalCommits.indexOf(commit) == 0) {
				first = true;
			}

			PortalBuildData portalBuildData = _getPortalBuildDataBySHA(
				commit.getSHA(), _downstreamBuildDataList);

			if (first || (portalBuildData != null)) {
				tableRowContainerElement = Dom4JUtil.getNewElement(
					"tbody", tableBodyElement);

				tableRowContainerElement.addAttribute("class", "result-row");
			}
			else if (tableRowContainerElement == null) {
				tableRowContainerElement = Dom4JUtil.getNewElement(
					"tbody", tableBodyElement);

				tableRowContainerElement.addAttribute("class", "hidden-row");
			}

			Element tableRowElement = Dom4JUtil.getNewElement(
				"tr", tableRowContainerElement);

			if (first || (portalBuildData != null)) {
				Element labelElement = Dom4JUtil.getNewElement(
					"label", null, "+");

				labelElement.addAttribute("for", commit.getSHA());

				Element inputElement = Dom4JUtil.getNewElement("input", null);

				inputElement.addAttribute("data-toggle", "toggle");
				inputElement.addAttribute("id", commit.getSHA());
				inputElement.addAttribute("name", commit.getSHA());
				inputElement.addAttribute("type", "checkbox");

				Dom4JUtil.getNewElement(
					"td", tableRowElement, labelElement, inputElement);
			}
			else {
				Dom4JUtil.getNewElement("td", tableRowElement, "");
			}

			String abbreviatedSHA = commit.getAbbreviatedSHA();

			if (first) {
				abbreviatedSHA = "*" + abbreviatedSHA;
			}

			String gitHubCommitURL = _workspaceGitRepository.getGitHubURL();

			gitHubCommitURL = gitHubCommitURL.replaceAll("/tree/.+", "");

			gitHubCommitURL += "/commit/" + commit.getSHA();

			Dom4JUtil.getNewElement(
				"td", tableRowElement,
				Dom4JUtil.getNewAnchorElement(gitHubCommitURL, abbreviatedSHA));

			Dom4JUtil.getNewElement(
				"td", tableRowElement,
				StringEscapeUtils.escapeXml(commit.getMessage()));

			if (previousCommit == null) {
				previousCommit = commit;

				previousCommitCount++;

				Dom4JUtil.getNewElement("td", tableRowElement, "");
			}
			else if (portalBuildData == null) {
				previousCommitCount++;

				Dom4JUtil.getNewElement("td", tableRowElement, "");
			}
			else {
				String gitHubCommitDiffURL =
					_workspaceGitRepository.getGitHubURL();

				gitHubCommitDiffURL = gitHubCommitDiffURL.replaceAll(
					"/tree/.+", "");

				gitHubCommitDiffURL = JenkinsResultsParserUtil.combine(
					gitHubCommitDiffURL, "/compare/", commit.getSHA(), "...",
					previousCommit.getSHA());

				String gitHubCommitDiffString =
					JenkinsResultsParserUtil.combine(
						commit.getAbbreviatedSHA(), "...",
						previousCommit.getAbbreviatedSHA());

				String commitCountString = JenkinsResultsParserUtil.combine(
					"(", String.valueOf(previousCommitCount), " commits)");

				if (previousCommitCount == 1) {
					commitCountString = JenkinsResultsParserUtil.combine(
						"(", String.valueOf(previousCommitCount), " commit)");
				}

				Dom4JUtil.getNewElement(
					"td", tableRowElement,
					Dom4JUtil.getNewAnchorElement(
						gitHubCommitDiffURL, gitHubCommitDiffString),
					Dom4JUtil.getNewElement("span", null, commitCountString));

				previousCommit = commit;

				previousCommitCount = 1;
			}

			if (portalBuildData == null) {
				Dom4JUtil.getNewElement("td", tableRowElement, "");
				Dom4JUtil.getNewElement("td", tableRowElement, "");
				Dom4JUtil.getNewElement("td", tableRowElement, "");
				Dom4JUtil.getNewElement("td", tableRowElement, "");

				if (first) {
					tableRowContainerElement = null;
				}

				continue;
			}

			Dom4JUtil.getNewElement(
				"td", tableRowElement,
				Dom4JUtil.getNewAnchorElement(
					portalBuildData.getBuildURL(), "build"));

			Dom4JUtil.getNewElement(
				"td", tableRowElement,
				portalBuildData.getBuildDurationString());

			Dom4JUtil.getNewElement(
				"td", tableRowElement, portalBuildData.getBuildStatus());

			Dom4JUtil.getNewElement(
				"td", tableRowElement, portalBuildData.getBuildResult());

			tableRowContainerElement = null;
		}

		return tableBodyElement;
	}

	@Override
	protected Element getJenkinsReportTableColumnHeadersElement() {
		Element toggleElement = Dom4JUtil.getNewElement("th", null, "");

		Element commitElement = Dom4JUtil.getNewElement(
			"th", null, "Commit SHA");

		Element shaElement = Dom4JUtil.getNewElement(
			"th", null, "Commit Message");

		Element commitDiffElement = Dom4JUtil.getNewElement(
			"th", null, "Commit Diffs");

		Element buildElement = Dom4JUtil.getNewElement(
			"th", null, "Build Link");

		Element buildTimeElement = Dom4JUtil.getNewElement(
			"th", null, "Build Time");

		Element statusElement = Dom4JUtil.getNewElement(
			"th", null, "Build Status");

		Element resultElement = Dom4JUtil.getNewElement(
			"th", null, "Build Result");

		Element tableColumnHeaderElement = Dom4JUtil.getNewElement("tr");

		Dom4JUtil.addToElement(
			tableColumnHeaderElement, toggleElement, commitElement, shaElement,
			commitDiffElement, buildElement, buildTimeElement, statusElement,
			resultElement);

		return tableColumnHeaderElement;
	}

	protected Element getJenkinsReportTableElement() {
		Element topLevelTableElement = Dom4JUtil.getNewElement("table");

		String gitHubCommitsURL = _workspaceGitRepository.getGitHubURL();

		gitHubCommitsURL = gitHubCommitsURL.replace("/tree/", "/commits/");

		Element captionElement = Dom4JUtil.getNewElement(
			"caption", topLevelTableElement);

		Dom4JUtil.getNewElement(
			"h2", captionElement, "Commit history of ",
			Dom4JUtil.getNewAnchorElement(gitHubCommitsURL, gitHubCommitsURL));

		Dom4JUtil.addToElement(
			topLevelTableElement, getJenkinsReportTableColumnHeadersElement(),
			getJenkinsReportTableBodyElement());

		return topLevelTableElement;
	}

	private PortalBuildData _getPortalBuildDataBySHA(
		String sha, List<BuildData> downstreamBuildDataList) {

		for (BuildData buildData : downstreamBuildDataList) {
			PortalBuildData portalBuildData = (PortalBuildData)buildData;

			if (sha.equals(portalBuildData.getPortalBranchSHA())) {
				return portalBuildData;
			}
		}

		return null;
	}

	private static final String _JQUERY_URL =
		"https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.3.1.min.js";

	private List<BuildData> _downstreamBuildDataList;
	private WorkspaceGitRepository _workspaceGitRepository;

}