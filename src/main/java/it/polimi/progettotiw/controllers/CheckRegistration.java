package it.polimi.progettotiw.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.progettotiw.beans.User;
import it.polimi.progettotiw.dao.UserDAO;
import it.polimi.progettotiw.ConnectionHandler;

@WebServlet("/CheckRegistration")
public class CheckRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
	private static final int MAX_NAME_LENGTH     = 50;
	private static final int MAX_PASSWORD_LENGTH = 60;

	private Connection connection;
	private TemplateEngine templateEngine;
	private JakartaServletWebApplication application;

	@Override
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		application = JakartaServletWebApplication.buildApplication(servletContext);

		WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setSuffix(".html");

		templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);

		connection = ConnectionHandler.getConnection(servletContext);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String username = request.getParameter("newUsername");
		String name     = request.getParameter("newName");
		String surname  = request.getParameter("newSurname");
		String password = request.getParameter("newPassword");
		String repeated = request.getParameter("newRepeatedPassword");

		// Controllo parametri
		if (username == null || name == null || surname == null ||
				password == null || repeated == null ||
				username.isBlank() || name.isBlank() || surname.isBlank() ||
				password.isBlank() || repeated.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti o non validi");
			return;
		}

		// Validazioni formali
		if (!USERNAME_PATTERN.matcher(username).matches()) {
			sendErrorPage(request, response, "usernameInvalid",
					"Username non valido: usa 3–20 caratteri alfanumerici o underscore.");
			return;
		}
		if (name.length() > MAX_NAME_LENGTH || surname.length() > MAX_NAME_LENGTH) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nome o cognome troppo lunghi");
			return;
		}
		if (password.length() < 8 || password.length() > MAX_PASSWORD_LENGTH) {
			sendErrorPage(request, response, "passwordInvalid",
					"Password deve essere tra 8 e 60 caratteri.");
			return;
		}
		if (!password.equals(repeated)) {
			sendErrorPage(request, response, "passwordMismatch", "Le password non coincidono.");
			return;
		}

		UserDAO userDAO = new UserDAO(connection);

		try {
			// Recupera tutti gli utenti registrati
			ArrayList<User> registeredUsers = userDAO.findAllUsers();
			for (User regUser : registeredUsers) {
				if (regUser.getUsername().equals(username)) {
					sendErrorPage(request, response, "nicknameTaken",
							"Questo username è già in uso, scegline un altro.");
					return;
				}
			}

			// Creazione bean utente (password in chiaro)
			User u = new User();
			u.setUsername(username);
			u.setPassword(password);
			u.setName(name);
			u.setSurname(surname);
			userDAO.registerUser(u);
			ServletContext ctx = getServletContext();
			String baseDir = ctx.getInitParameter("UPLOAD_BASE");
			Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
			Path userPath = basePath.resolve(u.getUsername()).normalize();
			if (!userPath.startsWith(basePath)) {
				throw new IOException("Tentativo di path traversal");
			}
			//Files.createDirectories(userPath.resolve("pages"));


		} catch (SQLException e) {
			log("ERRORE SQL in registrazione utente", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno");
			return;
		} catch (IOException e) {
			log("ERRORE filesystem in registrazione utente", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore interno");
			return;
		}

		// Redirect al login
		String loginPath = getServletContext().getContextPath() + "/loginPage.html";
		response.sendRedirect(loginPath);
	}

	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			log("Errore chiusura connessione", e);
		}
	}

	/** Utility per mostrare errori utente via Thymeleaf */
	private void sendErrorPage(HttpServletRequest req, HttpServletResponse resp,
							   String varName, String message) throws IOException {
		WebContext ctx = new WebContext(application.buildExchange(req, resp));
		ctx.setVariable(varName, message);
		templateEngine.process("/loginPage.html", ctx, resp.getWriter());
	}
}