package it.polimi.progettotiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;

import it.polimi.progettotiw.beans.User;
import it.polimi.progettotiw.dao.UserDAO;
import it.polimi.progettotiw.ConnectionHandler;

@WebServlet("/CheckPassword")
public class CheckPassword extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	private JakartaServletWebApplication application;
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		application = JakartaServletWebApplication.buildApplication(servletContext);
		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setSuffix(".html");
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		connection = ConnectionHandler.getConnection(servletContext);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// getting and sanitizing parameters
		String usr = request.getParameter("username");
		String pwd = request.getParameter("password");
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}

		UserDAO userDAO = new UserDAO(connection);
		User u;
		try {
			u = userDAO.checkCredentials(usr, pwd);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot check login");
			return;
		}

		String path;
		if (u == null) {// user not logged
			path = "/loginPage.html"; // path of loginPage page

			// Crea un nuovo contesto web con il nuovo API di Thymeleaf 3.1.x
			WebContext ctx = new WebContext(application.buildExchange(request, response));
			ctx.setVariable("errorMessage", "Incorrect user or password!");
			templateEngine.process(path, ctx, response.getWriter());
		} else {
			request.getSession().setAttribute("user", u);// save user in session
			path = getServletContext().getContextPath() + "/GoToHome";
			response.sendRedirect(path);
		}
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}