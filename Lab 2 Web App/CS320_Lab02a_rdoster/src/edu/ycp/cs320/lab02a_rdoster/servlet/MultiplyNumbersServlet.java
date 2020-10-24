package edu.ycp.cs320.lab02a_rdoster.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ycp.cs320.lab02a_rdoster.controller.NumbersController;
import edu.ycp.cs320.lab02a_rdoster.model.Numbers;

public class MultiplyNumbersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		System.out.println("MultiplyNumbers Servlet: doGet");	
		
		// call JSP to generate empty form
		req.getRequestDispatcher("/_view/MultiplyNumbers.jsp").forward(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		System.out.println("MultplyNumbers Servlet: doPost");
		
		// create Numbers model - model does not persist between requests
		// must recreate it each time a Post comes in 
		Numbers model = new Numbers();
				
		// create GuessingGame controller - controller does not persist between requests
		// must recreate it each time a Post comes in
		NumbersController controller = new NumbersController();
			
		// assign model reference to controller so that controller can access model
		controller.setModel(model);

		// holds the error message text, if there is any
		String errorMessage = null;

		// result of calculation goes here
		Double result = null;
		
		// decode POSTed form parameters and dispatch to controller
		try {
			Double first = getDoubleFromParameter(req.getParameter("first"));
			Double second = getDoubleFromParameter(req.getParameter("second"));
			
			model.setFirst(first);
			model.setSecond(second);
			
			// check for errors in the form data before using is in a calculation
			if (first == null || second == null) {
				errorMessage = "Please specify two numbers";
			}
			// otherwise, data is good, do the calculation
			// must create the controller each time, since it doesn't persist between POSTs
			// the view does not alter data, only controller methods should be used for that
			// thus, always call a controller method to operate on the data
			else {
				result = controller.setMult(first, second);
			}
		} catch (NumberFormatException e) {
			errorMessage = "Invalid double";
		}
		
		// add result objects as attributes
		// this adds the errorMessage text and the result to the response
		req.setAttribute("errorMessage", errorMessage);
		req.setAttribute("result", result);
		
		// set "game" attribute to the model reference
		// the JSP will reference the model elements through "game"
		req.setAttribute("game", model);
				
		// Forward to view to render the result HTML document
		req.getRequestDispatcher("/_view/MultiplyNumbers.jsp").forward(req, resp);
	}

	// gets double from the request with attribute named s
	private Double getDoubleFromParameter(String s) {
		if (s == null || s.equals("")) {
			return null;
		} else {
			return Double.parseDouble(s);
		}
	}
}
