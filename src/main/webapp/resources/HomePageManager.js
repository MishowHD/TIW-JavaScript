(function(){

    // endpoint REST
    const URL_PLAYLIST_LIST   = "GetUserPlaylistsData";
    const URL_ALBUM_LIST      = "GetAlbumData";
    const URL_TRACK_LIST      = "GetUserTracksData";
    const URL_CREATE_ALBUM    = "SaveAlbum";
    const URL_UPLOAD_TRACK    = "UploadTrack";
    const URL_SAVE_PLAYLIST   = "SavePlaylist";

    let playlistTable, albumCreator, trackUploader, playlistCreator;

    window.addEventListener("load", () => {
        // Verifica login
        if (!sessionStorage.getItem("username")) {
            window.location.href = "loginPage.html";
            return;
        }
        const manager = new HomePageManager();
        manager.start();
        manager.refresh();
    }, false);


    // --- PlaylistTable: mostra la tabella delle playlist ---
    function PlaylistTable(tableElem, tbodyElem, msgElem) {
        this.table = tableElem;
        this.tbody = tbodyElem;
        this.msg   = msgElem;

        this.reset = () => {
            this.table.style.visibility = "hidden";
            this.msg.textContent = "";
        };

        this.show = () => {
            makeCall("GET", URL_PLAYLIST_LIST, null, req => {
                if (req.readyState !== XMLHttpRequest.DONE) return;
                if (req.status === 200) {
                    const data = JSON.parse(req.responseText);
                    if (data.length === 0) {
                        this.msg.textContent = "No playlists available";
                        return;
                    }
                    this.update(data);
                }
                else if (req.status === 403) {
                    window.location.href = req.getResponseHeader("Location");
                    sessionStorage.removeItem("username");
                }
                else this.msg.textContent = req.responseText;
            });
        };

        this.update = (playlists) => {
            this.tbody.innerHTML = "";
            playlists.forEach(pl => {
                console.log("Received playlist time:", pl.time, typeof pl.time);
                const tr = document.createElement("tr");

                const tdTitle = document.createElement("td");
                const a = document.createElement("a");
                a.href = `GoToPlaylist?playlist_id=${pl.playlist_id}`;
                a.textContent = pl.title;
                tdTitle.appendChild(a);
                tr.appendChild(tdTitle);

                const tdDate = document.createElement("td");
                tdDate.textContent = new Date(pl.time)
                    .toLocaleString("it-IT", {
                        day: "2-digit", month: "2-digit", year: "numeric",
                        hour: "2-digit", minute: "2-digit"
                    });
                tr.appendChild(tdDate);

                this.tbody.appendChild(tr);
            });
            this.table.style.visibility = "visible";
        };
    }


    // --- AlbumCreator: intercetta il form /SaveAlbum ---
    function AlbumCreator(formElem, msgElem) {
        this.form = formElem;
        this.msg = msgElem;

        // Controllo dimensione immagine
        this.form.querySelector('input[name="image"]').addEventListener("change", e => {
            if (e.target.files[0]?.size > 5 * 1024 * 1024) {
                alert("La dimensione massima è 5MB");
                e.target.value = "";
            }
        });

        this.reset = () => this.form.reset();

        // FUNZIONE MODIFICATA (rimosso il popolamento della select)
        this.show = () => {}; // Non serve nessuna operazione aggiuntiva

        this.registerEvents = orchestrator => {
            this.form.addEventListener("submit", e => {
                e.preventDefault();
                // Passiamo direttamente il form a makeCall
                makeCall("POST", URL_CREATE_ALBUM, this.form, req => {
                    if (req.readyState !== XMLHttpRequest.DONE) return;
                    if (req.status === 200) {
                        orchestrator.refresh();
                        alert("Album creato con successo!");
                    } else if (req.status === 403) {
                        window.location.href = req.getResponseHeader("Location");
                        sessionStorage.removeItem("username");
                    } else {
                        alert(req.responseText || "Errore sconosciuto");
                    }
                });
            }, false);
        };
    }


    // --- TrackUploader: intercetta il form /UploadTrack ---
    function TrackUploader(formElem, msgElem) {
        this.form = formElem;
        this.msg  = msgElem;

        // client‐side: max 10MB
        this.form.querySelector('input[name="audioFile"]').addEventListener("change", e => {
            if (e.target.files[0].size > 10*1024*1024) {
                alert("La dimensione massima è 10MB");
                e.target.value = "";
            }
        });

        this.reset = () => this.form.reset();
        this.show =() => {};
        this.registerEvents = orchestrator => {
            this.form.addEventListener("submit", e => {
                e.preventDefault();
                makeCall("POST", URL_UPLOAD_TRACK, this.form, req => {
                    if (req.readyState !== XMLHttpRequest.DONE) return;
                    if (req.status === 200)      orchestrator.refresh();
                    else if (req.status === 403) {
                        window.location.href = req.getResponseHeader("Location");
                        sessionStorage.removeItem("username");
                    }
                    else alert(req.responseText);
                });
            }, false);

        };
    }


    // --- PlaylistCreator: intercetta il form /SavePlaylist con i checkbox ---
    function PlaylistCreator(formElem, msgElem) {
        this.form = formElem;
        this.msg  = msgElem;
        this.group = formElem.querySelector(".checkbox-group");

        this.reset = () => {
            this.form.querySelector('input[name="title"]').value = "";
            this.group.innerHTML = "";
        };

        this.show = () => {
            makeCall("GET", URL_TRACK_LIST, null, req => {
                if (req.readyState !== XMLHttpRequest.DONE) return;
                if (req.status === 200) {
                    const tracks = JSON.parse(req.responseText);
                    this.group.innerHTML = "";
                    if (tracks.length === 0) {
                        this.group.textContent = "No tracks available";
                    } else {
                        tracks.forEach(t => {
                            const div = document.createElement("div");
                            div.className = "checkbox-item";
                            const cb = document.createElement("input");
                            cb.type = "checkbox";
                            cb.name = "trackIds";
                            cb.value = t.trackId;
                            cb.id = "track_" + t.trackId;
                            div.appendChild(cb);
                            const lbl = document.createElement("label");
                            lbl.htmlFor = cb.id;
                            lbl.textContent = t.title;
                            div.appendChild(lbl);
                            this.group.appendChild(div);
                        });
                    }
                }
            });
        };

        this.registerEvents = orchestrator => {
            this.form.addEventListener("submit", e => {
                e.preventDefault();
                if (this.form.querySelectorAll('input[name="trackIds"]:checked').length === 0) {
                    alert("Seleziona almeno un brano");
                    return;
                }
                makeCall("POST", URL_SAVE_PLAYLIST, this.form, req => {
                    if (req.readyState !== XMLHttpRequest.DONE) return;
                    if (req.status === 200)      orchestrator.refresh();
                    else if (req.status === 403) {
                        window.location.href = req.getResponseHeader("Location");
                        sessionStorage.removeItem("username");
                    }
                    else alert(req.responseText);
                });
            }, false);

        };
    }


    // --- PageOrchestrator ---
    function HomePageManager() {
        const msg = document.getElementById("messageContainer");

        playlistTable   = new PlaylistTable(
            document.getElementById("playlistTable"),
            document.getElementById("playlistTableBody"),
            msg
        );
        albumCreator    = new AlbumCreator(
            document.getElementById("albumForm"),
            msg
        );
        trackUploader   = new TrackUploader(
            document.getElementById("trackForm"),
            msg
        );
        playlistCreator = new PlaylistCreator(
            document.getElementById("playlistForm"),
            msg
        );

        this.start = () => {
            albumCreator.registerEvents(this);
            trackUploader.registerEvents(this);
            playlistCreator.registerEvents(this);
            document.querySelector("a.logout").addEventListener("click", () => {
                sessionStorage.removeItem("username");
            }, false);
        };

        this.refresh = () => {
            msg.textContent = "";
            playlistTable.reset();
            albumCreator.reset();
            trackUploader.reset();
            playlistCreator.reset();
            playlistTable.show();
            albumCreator.show();
            trackUploader.show();
            playlistCreator.show();
        };
    }

})();