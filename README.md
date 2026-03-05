# 🎵 TIW Playlist Manager — JavaScript Version

A single-page web application for managing music playlists, built as a university project for the **Tecnologie Informatiche per il Web (TIW)** course at Politecnico di Milano (A.Y. 2024–25).

The application allows users to register, log in, upload tracks, organize them into albums, create playlists, and reorder songs via drag-and-drop — all without page reloads, using a pure JavaScript frontend communicating with a Java EE backend via REST/JSON APIs.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Configuration](#setup--configuration)
- [Build & Deploy](#build--deploy)
- [API Overview](#api-overview)

---

## ✨ Features

- **Authentication** — Registration and login with session management
- **Track Upload** — Upload audio tracks associated with albums and genres
- **Album Management** — Create albums and browse their content
- **Playlist Management** — Create playlists by selecting owned tracks
- **Add Tracks to Playlist** — Dynamically add tracks to existing playlists
- **Drag-and-drop Reorder** — Reorder tracks within a playlist and persist the new order
- **Single Page Application** — All views are rendered dynamically via JavaScript, no page reloads

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vanilla JavaScript (ES5+), HTML5, CSS3 |
| Backend | Java 24, Jakarta Servlet API 6.0 |
| Build Tool | Apache Maven |
| Application Server | Apache Tomcat 10+ |
| Database | MariaDB |
| JSON Serialization | Gson 2.13 |
| File Upload | Apache Commons FileUpload |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/it/polimi/progettotiw/
│   │   ├── beans/              # Data model (User, Track, Album, Playlist, Genres)
│   │   ├── controllers/        # Jakarta Servlets (REST-style endpoints)
│   │   ├── dao/                # Data Access Objects (JDBC queries)
│   │   ├── filter/             # Authentication filter (Checker.java)
│   │   └── ConnectionHandler.java
│   └── webapp/
│       ├── resources/          # Frontend JavaScript modules
│       │   ├── HomePageManager.js
│       │   ├── PlaylistTable.js
│       │   ├── PlaylistDetailView.js
│       │   ├── PlaylistCreator.js
│       │   ├── AlbumCreator.js
│       │   ├── TrackUploader.js
│       │   ├── PlayerView.js
│       │   ├── ReorderModal.js
│       │   └── Utils.js
│       ├── loginPage.html
│       ├── homePage.html
│       ├── errorPage.html
│       ├── style.css
│       ├── META-INF/context.xml
│       └── WEB-INF/web.xml
```

---

## ✅ Prerequisites

- **Java 24** (or compatible JDK)
- **Apache Maven 3.8+**
- **Apache Tomcat 10.1+**
- **MariaDB 10.6+**

---

## ⚙️ Setup & Configuration

### 1. Database

Create a MariaDB database and a dedicated user, then import your schema:

```sql
CREATE DATABASE tiw_playlist;
CREATE USER 'tiw_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON tiw_playlist.* TO 'tiw_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Application configuration

Edit `src/main/webapp/WEB-INF/web.xml` and update the following `<context-param>` values to match your environment:

```xml
<context-param>
    <param-name>dbUrl</param-name>
    <param-value>jdbc:mariadb://localhost:3306/tiw_playlist</param-value>
</context-param>
<context-param>
    <param-name>dbUser</param-name>
    <param-value>tiw_user</param-value>
</context-param>
<context-param>
    <param-name>dbPassword</param-name>
    <param-value>your_password</param-value>
</context-param>
<context-param>
    <param-name>UPLOAD_BASE</param-name>
    <param-value>/path/to/your/uploads/folder</param-value>
</context-param>
```

### 3. File upload directory (Tomcat)

Edit `src/main/webapp/META-INF/context.xml` to point to a local folder where uploaded audio files will be stored:

```xml
<Context>
    <Resources>
        <PreResources className="org.apache.catalina.webresources.DirResourceSet"
                      base="/path/to/your/uploads/folder"
                      webAppMount="/uploads" />
    </Resources>
</Context>
```

Make sure the folder exists and Tomcat has read/write permissions on it.

---

## 🚀 Build & Deploy

### Build the WAR

```bash
mvn clean package
```

The WAR file will be generated at:

```
target/Progetto-TIW-1.0-SNAPSHOT.war
```

### Deploy to Tomcat

Copy the WAR into Tomcat's `webapps/` directory:

```bash
cp target/Progetto-TIW-1.0-SNAPSHOT.war $CATALINA_HOME/webapps/TIWPlaylist.war
```

Then start Tomcat:

```bash
$CATALINA_HOME/bin/startup.sh
```

The app will be available at:

```
http://localhost:8080/TIWPlaylist/
```

---

## 🔌 API Overview

All endpoints return JSON and are protected by the `Checker` authentication filter (except login and registration).

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/CheckPassword` | Login |
| `POST` | `/CheckRegistration` | Register a new user |
| `GET` | `/GetUserPlaylistsData` | Get all playlists of the logged-in user |
| `GET` | `/GetPlaylistData` | Get detail of a specific playlist |
| `GET` | `/GetUserTracksData` | Get all tracks owned by the user |
| `GET` | `/GetTrackData` | Get metadata for a specific track |
| `GET` | `/GetAlbumData` | Get album info |
| `GET` | `/GetGenresData` | Get available genres |
| `POST` | `/SaveAlbum` | Create a new album |
| `POST` | `/UploadTrack` | Upload a new track |
| `POST` | `/SavePlaylist` | Create a new playlist |
| `POST` | `/AddTracksToPlaylist` | Add tracks to an existing playlist |
| `POST` | `/SavePlaylistOrder` | Persist new track order in a playlist |
| `GET` | `/Logout` | Log out and invalidate session |

---

## 👥 Authors

Developed by **Gruppo 13** — Politecnico di Milano, TIW 2024–25.
