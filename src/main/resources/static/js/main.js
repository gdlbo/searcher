let isAdmin = false;
let isDebug = false;

window.onload = function () {
    initTheme();
    initDropZone();
    loadSearch();
};
document.addEventListener("DOMContentLoaded", function () {
    const savedSortBy = localStorage.getItem("sortBy");
    const savedSortOrder = localStorage.getItem("sortOrder");
    const savedShowHidden = localStorage.getItem("showHidden");
    const url = new URL(window.location.href);
    const currentSortBy = url.searchParams.get("sortBy");
    const currentSortOrder = url.searchParams.get("sortOrder");
    const currentShowHidden = url.searchParams.get("showHidden");

    if (savedSortBy !== currentSortBy || savedSortOrder !== currentSortOrder || savedShowHidden !== currentShowHidden) {
        if (savedSortBy && savedSortOrder) {
            updateURLAndRedirect(savedSortBy, savedSortOrder);
        } else {
            updateURLWithShowHidden(savedShowHidden);
        }
    }

    isAdmin = document.body.getAttribute('data-is-admin') === 'true';
    isDebug = document.body.getAttribute('data-is-debug') === 'true';

    initSortLinks();
    initSortByLastModified();
    initShowHidden();
    initDebug();
    initThemeButtons()
});

function openDropdown(event) {
    event.stopPropagation();
    closeAllDropdowns();
    const dropdownContent = event.target.nextElementSibling;
    dropdownContent.classList.add("show");

    setupGlobalClickListener(dropdownContent);
    setupButtonHandlers(dropdownContent);

    const rect = dropdownContent.getBoundingClientRect();
    if (rect.bottom > window.innerHeight) {
        dropdownContent.style.top = 'auto';
        dropdownContent.style.bottom = '100%';
    } else if (rect.top < 0) {
        dropdownContent.style.top = '100%';
        dropdownContent.style.bottom = 'auto';
    } else {
        dropdownContent.style.top = '100%';
        dropdownContent.style.bottom = 'auto';
    }
}

function closeAllDropdowns() {
    const dropdowns = document.querySelectorAll(".dropdown-content");
    dropdowns.forEach(dropdown => {
        dropdown.classList.remove("show");
    });
}

function setupGlobalClickListener(dropdownContent) {
    window.onclick = function (event) {
        if (!event.target.matches(".options-button") && !dropdownContent.contains(event.target)) {
            dropdownContent.classList.remove("show");
        }
    };
}

function setupButtonHandlers(dropdownContent) {
    dropdownContent.addEventListener('click', function (event) {
        event.stopPropagation();
        const buttonType = event.target.closest('button')?.id;
        if (!buttonType) return;

        const dialogBox = document.getElementById("dialog-box");
        const filePath = event.target.closest('.dropdown-content').querySelector('input[name="filePath"]').value;

        switch (buttonType) {
            case "historyButton":
                showFileHistory(filePath);
                break;
            case "downloadButton":
                downloadFile(filePath);
                break;
            case "replaceButton":
                dialogBox.style.display = "block";
                closeAllDropdowns();
                break;
            case "deleteButton":
                removeFile(filePath);
                break;
        }
    });
}

function showFileHistory(filePath) {
    const encodedFilePath = encodeURIComponent(filePath);
    fetch('/api/history?fileName=' + encodedFilePath)
        .then(response => response.json())
        .then(files => {
            const fileHistoryList = document.getElementById('fileHistoryList');
            fileHistoryList.innerHTML = '';

            if (files.length === 0) {
                fileHistoryList.textContent = 'История изменений отсутствует';
            } else {
                files.forEach(file => {
                    const listItem = document.createElement('div');
                    listItem.style.display = 'flex';
                    listItem.style.justifyContent = 'space-between';
                    listItem.style.alignItems = 'center';
                    listItem.style.marginBottom = '30px';

                    const dateSpan = document.createElement('span');
                    dateSpan.textContent = new Date(parseInt(file.lastModified)).toLocaleString();

                    const deleteButton = document.createElement('button');
                    deleteButton.textContent = 'Delete';
                    deleteButton.onclick = () => deleteFileFromHistory(file.location);
                    deleteButton.classList.add('small-button');

                    const downloadButton = document.createElement('button');
                    downloadButton.textContent = 'Download';
                    downloadButton.onclick = () => downloadFile(file.location);
                    downloadButton.classList.add('small-button');

                    const buttonDiv = document.createElement('div');
                    buttonDiv.style.float = 'right';
                    buttonDiv.style.display = 'flex';
                    buttonDiv.appendChild(downloadButton);

                    if (isAdmin) {
                        buttonDiv.appendChild(deleteButton);
                    }

                    listItem.appendChild(dateSpan);
                    listItem.appendChild(buttonDiv);
                    fileHistoryList.appendChild(listItem);
                });
            }

            // Show the file history dialog
            const fileHistoryDialog = document.getElementById('fileHistoryDialog');
            document.body.style.overflow = 'hidden';
            fileHistoryDialog.style.display = 'block';
        });
}

function closeFileHistoryDialog() {
    const fileHistoryDialog = document.getElementById('fileHistoryDialog');
    document.body.style.overflow = '';
    fileHistoryDialog.style.display = 'none';
}

function deleteFileFromHistory(filePath) {
    // Send a GET request to the server to delete the file from history
    const encodedFilePath = encodeURIComponent(filePath);
    fetch('/api/remove?filePath=' + encodedFilePath)
        .then(response => {
            if (!response.ok) {
                console.error('Failed to delete file from history: ' + encodedFilePath);
            }

            // Refresh the file history dialog
            const currentFilePath = document.querySelector('#historyButton').getAttribute('onclick').match(/'([^']+)'/)[1];
            showFileHistory(currentFilePath);
        });
}

function openSettingsDialog() {
    const settingsDialog = document.getElementById("settingsDialog");
    document.body.style.overflow = 'hidden';
    settingsDialog.style.display = "block";
}

function closeSettingsDialog() {
    const settingsDialog = document.getElementById("settingsDialog");
    settingsDialog.querySelector('.dialog-content').scrollTop = 0;
    document.body.style.overflow = '';
    settingsDialog.style.display = "none";
}

function toggleDebug() {
    let button = document.getElementById('toggleDebug');
    let debug = !isDebug;

    button.textContent = `Debug: ${debug ? 'On' : 'Off'}`;
    applyDebugStatus(debug)
}

function initDebug() {
    let button = document.getElementById('toggleDebug');
    button.textContent = `Debug: ${isDebug === true ? 'On' : 'Off'}`;
}