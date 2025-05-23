package it.polimi.progettotiw.beans;

import java.sql.Timestamp;
import java.time.LocalDateTime;
public class Playlist {
    private int playlist_id;
    private String title;
    private Timestamp time;
    private String creator;

    public int getPlaylist_id() {
        return playlist_id;
    }

    public void setPlaylist_id(int playlist_id) {
        this.playlist_id = playlist_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public LocalDateTime getLocalDateTime() {
        return time != null ? time.toLocalDateTime() : null;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}