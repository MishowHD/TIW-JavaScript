package it.polimi.progettotiw.controllers;

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

import java.io.IOException;

@WebServlet("/ErrorHandler")
public class ErrorHandler extends HttpServlet {
    private static final long serialVersionUID = 1L;
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
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processError(request, response);
    }

    private void processError(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Estrai attributi standard di errore
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String requestUri = (String)  request.getAttribute("jakarta.servlet.error.request_uri");
        Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        // Costruisci titolo e messaggio
        String title = "Error" + (statusCode != null ? " " + statusCode : "");
        String message = "An unexpected error occurred.";
        String exceptionType = "N/A";
        String stackTrace = "";

        if (exception != null) {
            Throwable root = exception.getCause() != null ? exception.getCause() : exception;
            exceptionType = root.getClass().getName();
            message = root.getMessage() != null ? root.getMessage() : message;

            // riduco lo stack trace a prime 3 righe
            StackTraceElement[] trace = root.getStackTrace();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(3, trace.length); i++) {
                sb.append(trace[i].toString()).append("<br/>");
            }
            stackTrace = sb.toString();
        }

        // Imposta lo status della risposta
        response.setStatus(statusCode != null ? statusCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        // Prepara il contesto Thymeleaf
        WebContext ctx = new WebContext(
                application.buildExchange(request, response),
                request.getLocale());
        ctx.setVariable("errorTitle",    title);
        ctx.setVariable("errorMessage",  message);
        ctx.setVariable("statusCode",    statusCode != null ? statusCode : "N/A");
        ctx.setVariable("requestUri",    requestUri != null ? requestUri : "N/A");
        ctx.setVariable("exceptionType", exceptionType);
        ctx.setVariable("stackTrace",    stackTrace);
        ctx.setVariable("backUrl",       "/GoToHome");

        // Render della pagina
        templateEngine.process("errorPage", ctx, response.getWriter());
    }
}
