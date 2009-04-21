/**
 * Copyright (c) 2000-2009 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.scripting;

import java.util.Map;
import java.util.Set;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * <a href="ScriptingUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Alberto Montero
 */
public class ScriptingUtil {

	public static Map[] getPortletObjects(
		PortletConfig portletConfig, PortletContext portletContext,
		PortletRequest portletRequest, PortletResponse portletResponse) {

		return getScripting().getPortletObjects(
			portletConfig, portletContext, portletRequest, portletResponse);
	}

	public static Scripting getScripting() {
		return _scripting;
	}

	public static Set<String> getSupportedLanguages() {
		return getScripting().getSupportedLanguages();
	}

	public static Map<String, Object> eval(
			Map<String, Object> inputObjects, String lang,
			Set<String> outputObjectNames, String script)
		throws ScriptExecutionException, UnsupportedLanguageException {

		return getScripting().eval(
			inputObjects, lang, outputObjectNames, script);
	}

	public static Map<String, Object> eval(
			Map<String, Object> inputObjects,
			Map<String, Class> inputObjectTypes, String lang,
			Set<String> outputObjectNames, String script)
		throws ScriptExecutionException, UnsupportedLanguageException {

		return getScripting().eval(
			inputObjects, inputObjectTypes, lang, outputObjectNames, script);
	}

	public static void exec(
			Map<String, Object> inputObjects, String lang, String script)
		throws ScriptExecutionException, UnsupportedLanguageException {

		getScripting().exec(inputObjects, lang, script);
	}

	public static void exec(
			Map<String, Object> inputObjects,
			Map<String, Class> inputObjectTypes, String lang, String script)
		throws ScriptExecutionException, UnsupportedLanguageException {

		getScripting().exec(inputObjects, inputObjectTypes, lang, script);
	}

	public void setScripting(Scripting scripting) {
		_scripting = scripting;
	}

	private static Scripting _scripting;

}