package it.polimi.progettotiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import it.polimi.progettotiw.beans.Playlist;
import it.polimi.progettotiw.beans.Track;
import it.polimi.progettotiw.beans.User;
import it.polimi.progettotiw.dao.PlaylistDAO;
import it.polimi.progettotiw.dao.AlbumDAO;
import it.polimi.progettotiw.dao.GenresDAO;
import it.polimi.progettotiw.beans.Album;
import it.polimi.progettotiw.dao.TrackDAO;
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
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.progettotiw.ConnectionHandler;

@WebServlet("/GoToHome")
public class GoToHome extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Controllo autenticazione
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/loginPage.html");
            return;
        }


        WebContext ctx = new WebContext(application.buildExchange(request, response));
        // Recupera e rimuovi il messaggio dalla sessione
        HttpSession session = request.getSession();
        String successMessage = (String) session.getAttribute("successMessage");
        if (successMessage != null) {
            ctx.setVariable("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }

// poi prosegui con il caricamento di userPlaylists, albums, genres...



        // DAO
        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
        AlbumDAO albumDAO       = new AlbumDAO(connection);
        GenresDAO genreDAO       = new GenresDAO(connection);
        TrackDAO trackDao = new TrackDAO(connection);



        List<Playlist> userPlaylists;
        List<Album> albums;
        List<String> genres;
        List<Track> userTracks;
        try {
            userPlaylists = playlistDAO.getPlaylistsOfUser(user.getUsername());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore caricamento playlist");
            return;
        }

        try {
            genres = genreDAO.getGenresNames();
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore caricamento generi home");
            return;
        }

        try {
            albums = albumDAO.getAlbumOfUser(user.getUsername());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore caricamento album home");
            return;
        }

        try {
           userTracks = trackDao.findByUserOrdered(user.getUsername());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore caricamento tracce");
            return;
        }



        // Render template
        String path = "/homePage.html";
        ctx = new WebContext(application.buildExchange(request, response));
        ctx.setVariable("userPlaylists", userPlaylists);
        ctx.setVariable("albums", albums);
        ctx.setVariable("genres", genres);
        ctx.setVariable("userTracks", userTracks);
        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
