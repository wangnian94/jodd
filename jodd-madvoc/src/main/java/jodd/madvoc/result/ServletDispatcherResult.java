// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.madvoc.result;

import jodd.log.Logger;
import jodd.log.LoggerFactory;
import jodd.madvoc.ActionRequest;
import jodd.servlet.DispatcherUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.net.MalformedURLException;

/**
 * Servlet Dispatcher.
 * 
 * @see ServletRedirectResult
 */
public class ServletDispatcherResult extends AbstractTemplateViewResult {

	private static final Logger log = LoggerFactory.getLogger(ServletDispatcherResult.class);

	public static final String NAME = "dispatch";

	protected String[] extensions = new String[] {".jspf", ".jsp"};

	public ServletDispatcherResult() {
		super(NAME);
	}

	/**
	 * Renders the view by dispatching to the target JSP.
	 */
	protected void renderView(ActionRequest actionRequest, String target) throws Exception {
		target = processTarget(actionRequest, target);

		HttpServletRequest request = actionRequest.getHttpServletRequest();
		HttpServletResponse response = actionRequest.getHttpServletResponse();

		RequestDispatcher dispatcher = request.getRequestDispatcher(target);
		if (dispatcher == null) {
			response.sendError(SC_NOT_FOUND, "Result not found: " + target);	// should never happened
			return;
		}

		// If we're included, then include the view, otherwise do forward.
		// This allow the page to, for example, set content type.

		if (DispatcherUtil.isPageIncluded(request, response)) {
			dispatcher.include(request, response);
		} else {
			dispatcher.forward(request, response);
		}
	}

	/**
	 * Locates target using path with various extensions appended.
	 */
	protected String locateTarget(final ActionRequest actionRequest, final String path) {
		String target;

		for (String ext : extensions) {
			target = path + ext;

			if (targetExists(actionRequest, target)) {
				return target;
			}
		}

		return null;
	}

	/**
	 * Returns <code>true</code> if target exists.
	 */
	protected boolean targetExists(ActionRequest actionRequest, String target) {
		if (log.isDebugEnabled()) {
			log.debug("target check: " + target);
		}

		final ServletContext servletContext = actionRequest.getHttpServletRequest().getSession().getServletContext();

		try {
			return servletContext.getResource(target) != null;
		} catch (MalformedURLException ignore) {
			return false;
		}
	}

	/**
	 * Optionally processes target and returns modified target location of located JSP file.
	 * May be used by additional pre-processing of the target file. Called every time
	 * before final dispatching.
	 */
	protected String processTarget(ActionRequest actionRequest, String target) {
		return target;
	}

}