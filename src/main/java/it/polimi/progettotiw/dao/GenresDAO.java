package it.polimi.progettotiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.progettotiw.beans.Genres;
public class GenresDAO {
    private final Connection con; //session between a Java application and a database

    public GenresDAO(Connection connection) {
        this.con = connection;
    }

    public List<Genres> getGenres() throws SQLException {
        String query = "SELECT name FROM Genres";
        List<Genres> genres = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Genres g = new Genres();
                    g.setName(rs.getString("name"));
                    genres.add(g);
                }
            }
        }
        return genres;
    }
    public List<String> getGenresNames() throws SQLException {
        String query = "SELECT name FROM Genres";
        List<String> genres = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    genres.add(rs.getString("name"));
                }
            }
        }
        return genres;
    }



}