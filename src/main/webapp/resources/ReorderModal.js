(function () {
    const URL_PLAYLIST_DATA = "GetPlaylistData";
    const URL_SAVE_ORDER = "SavePlaylistOrder";

    const overlay = document.getElementById("modalOverlay");
    const modal = document.getElementById("reorderModal");
    const list = document.getElementById("reorderList");
    const loadingEl = document.getElementById("loadingIndicator");
    const btnSave = document.getElementById("saveReorderBtn");
    const btnCancel = document.getElementById("cancelReorderBtn");

    const show = el => el.classList.remove("is-hidden");
    const hide = el => el.classList.add("is-hidden");

    let dragSrcEl = null;
    let dragTarget = null;
    let dragPosition = null;

    function handleDragStart(e) {
        dragSrcEl = e.currentTarget;
        e.dataTransfer.effectAllowed = "move";
        e.dataTransfer.setData("text/plain", ""); // FF fix
        dragSrcEl.classList.add("dragging");
    }

    function handleDragOver(e) {
        e.preventDefault();
        if (dragTarget) dragTarget.classList.remove("drag-over", "drag-above", "drag-below");

        const target = e.target.closest("li");
        if (!target || target === dragSrcEl) return;

        dragTarget = target;
        const rect = target.getBoundingClientRect();
        const midpoint = rect.top + rect.height / 2;
        dragPosition = e.clientY < midpoint ? "above" : "below";
        target.classList.add("drag-over",
            dragPosition === "above" ? "drag-above" : "drag-below");
    }

    function clearDragClasses() {
        document.querySelectorAll("#reorderList li")
            .forEach(li => li.classList.remove("dragging",
                "drag-over", "drag-above", "drag-below"));
        dragSrcEl = dragTarget = null;
    }

    function handleDragLeave(e){
        if (dragTarget){
            dragTarget.classList.remove("drag-over","drag-above","drag-below");
            dragTarget = null;          // ok azzerare SOLO 'dragTarget'
        }
    }

    function handleDrop(e) {
        e.stopPropagation();
        if (!dragSrcEl) return;

        const targetLi = e.target.closest("li");
        const ul = list;

        if (targetLi && targetLi !== dragSrcEl) {
            dragPosition === "above"
                ? ul.insertBefore(dragSrcEl, targetLi)
                : ul.insertBefore(dragSrcEl, targetLi.nextSibling);
        }
        if (!targetLi) ul.appendChild(dragSrcEl);

        clearDragClasses();
    }

    function openReorderModal(playlistId) {
        show(overlay);
        show(modal);
        list.innerHTML = "";
        show(loadingEl);
        hide(list);

        loadPlaylistTracks(playlistId);
    }

    window.openReorderModal = openReorderModal; // globale come prima

    function closeReorderModal() {
        hide(overlay);
        hide(modal);
        clearDragClasses();
    }

    overlay.addEventListener("click", closeReorderModal);
    btnCancel.addEventListener("click", closeReorderModal);
    document.addEventListener("keydown", e => {
        if (e.key === "Escape" && !overlay.classList.contains("is-hidden")) {
            closeReorderModal();
        }
    });

    function loadPlaylistTracks(playlistId) {
        makeCall("GET",
            `${URL_PLAYLIST_DATA}?playlist_id=${playlistId}`,
            null,
            req => {
                if (req.readyState !== XMLHttpRequest.DONE) return;

                hide(loadingEl);
                show(list);

                if (req.status !== 200) {
                    redirectToErrorPage(req);
                    closeReorderModal();
                    return;
                }
                const {tracks} = JSON.parse(req.responseText);
                if (tracks.length === 0) {
                    list.innerHTML =
                        `<li style="text-align:center;cursor:default;">No tracks in this playlist</li>`;
                } else {
                    tracks.forEach(t => {
                        const li = document.createElement("li");
                        li.textContent = t.title;
                        li.dataset.trackId = t.track_id;
                        li.draggable = true;

                        li.addEventListener("dragstart", handleDragStart);
                        li.addEventListener("dragover", handleDragOver);
                        li.addEventListener("dragleave", handleDragLeave);
                        li.addEventListener("dragend", clearDragClasses);
                        li.addEventListener("drop", handleDrop);

                        list.appendChild(li);
                    });
                }
                modal.dataset.playlistId = playlistId;
                modal.dataset.originalTrackCnt = tracks.length;
            });
    }

    btnSave.addEventListener("click", () => {
        const originalCnt = +modal.dataset.originalTrackCnt || 0;
        const items       = [...document.querySelectorAll("#reorderList li")];

        if (items.length === 0){
            alert("No tracks to save");
            return;
        }
        if (items.length !== originalCnt) {
            alert(`Error: track number changed (original: ${originalCnt}, actual: ${items.length})`);
            return;
        }

        btnSave.disabled = true;
        const oldText = btnSave.textContent;
        btnSave.textContent = "Saving…";

        const tempForm = document.createElement("form");

        const inputPlaylist = document.createElement("input");
        inputPlaylist.type  = "hidden";
        inputPlaylist.name  = "playlist_id";
        inputPlaylist.value = modal.dataset.playlistId;
        tempForm.appendChild(inputPlaylist);

        items.forEach(li => {
            const inp = document.createElement("input");
            inp.type  = "hidden";
            inp.name  = "trackIds[]";
            inp.value = li.dataset.trackId;
            tempForm.appendChild(inp);
        });

        makeCall("POST", URL_SAVE_ORDER, tempForm, req => {
            btnSave.disabled  = false;
            btnSave.textContent = oldText;

            if (req.readyState !== XMLHttpRequest.DONE) return;
            if (req.status === 200) {
                document.dispatchEvent(new CustomEvent("playlistOrderSaved", {
                    detail: { playlistId: +modal.dataset.playlistId }
                }));

                closeReorderModal();
                alert("Order saved successfully!");
            } else {
                redirectToErrorPage(req);
            }
        });
    });


})();
