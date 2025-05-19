package it.polimi.progettotiw.controllers;

import com.google.gson.Gson;
import it.polimi.progettotiw.ConnectionHandler;
import it.polimi.progettotiw.beans.Track;
import it.polimi.progettotiw.beans.User;
import it.polimi.progettotiw.dao.TrackDAO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/GetTrackData")
public class GetTrackData extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    @Override
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        connection = ConnectionHandler.getConnection(ctx);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 1) Controllo sessione: utente autenticato
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 2) Parametro track_id
        String tidParam = req.getParameter("track_id");
        if (tidParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro track_id mancante");
            return;
        }

        int trackId;
        try {
            trackId = Integer.parseInt(tidParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "track_id non valido");
            return;
        }

        // 3) Recupera Track dal DB
        try {
            TrackDAO trackDao = new TrackDAO(connection);

            Track track = trackDao.findById(trackId);
            if (track == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Brano non trovato");
                return;
            }

            // 4) Serializza Track (incluso nested album) in JSON
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            new Gson().toJson(track, resp.getWriter());

        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
