let systemThemeListener;

window.onload = function () {
    initTheme();
    initDropZone();
    loadSearch();
};

function storeSearch() {
    const searchQuery = document.getElementById("searchQuery").value;
    const searchNumber = document.getElementById("searchNumber").value;
    if (searchQuery === "") {
        localStorage.removeItem("searchQuery");
    } else {
        localStorage.setItem("searchQuery", searchQuery);
    }
    if (searchNumber === "") {
        localStorage.removeItem("searchNumber");
    } else {
        localStorage.setItem("searchNumber", searchNumber);
    }
}

function loadSearch() {
    const searchQuery = localStorage.getItem("searchQuery");
    const searchNumber = localStorage.getItem("searchNumber");
    if (searchQuery !== null) {
        document.getElementById("searchQuery").value = searchQuery;
    }
    if (searchNumber !== null) {
        document.getElementById("searchNumber").value = searchNumber;
    }
}

function initTheme() {
    const storedQuery = localStorage.getItem("searchQuery");

    if (storedQuery) {
        document.getElementById("searchQuery").value = storedQuery;
    }

    const selectedTheme = localStorage.getItem("selected-theme");

    if (selectedTheme) {
        document.getElementById("theme-dropdown").value = selectedTheme;
        changeTheme();
    } else {
        systemThemeListener = window.matchMedia("(prefers-color-scheme: dark)");
        systemThemeListener.addEventListener("change", systemThemeChange);
    }
}

function initDropZone() {
    const dropZone = document.querySelector(".drop-zone");
    const fileInput = document.querySelector("#file-input");

    dropZone.addEventListener("dragover", (event) => {
        event.preventDefault();
        dropZone.classList.add("drag-over");
    });

    dropZone.addEventListener("dragleave", () => {
        dropZone.classList.remove("drag-over");
    });

    dropZone.addEventListener("drop", (event) => {
        event.preventDefault();
        dropZone.classList.remove("drag-over");
        fileInput.files = event.dataTransfer.files;
        displayFileName(fileInput);
    });

    fileInput.addEventListener("change", () => {
        dropZone.classList.remove("drag-over");
        displayFileName(fileInput);
    });
}

function displayFileName(input) {
    const fileName = input.files[0].name;
    const fileNameSpan = document.getElementById("file-name");
    fileNameSpan.textContent = fileName;
}

function systemThemeChange(event) {
    const newColorScheme = event.matches ? "dark" : "light";
    document.body.classList.remove(
        `${newColorScheme === "light" ? "dark" : "light"}-theme`
    );
    document.body.classList.add(`${newColorScheme}-theme`);
    document.getElementById("theme-dropdown").value = "system";
    localStorage.setItem("selected-theme", "system");
}

function changeTheme() {
    const selectedTheme = document.getElementById("theme-dropdown").value;

    if (selectedTheme === "system") {
        if (!systemThemeListener) {
            systemThemeListener = window.matchMedia("(prefers-color-scheme: dark)");
            systemThemeListener.addEventListener("change", systemThemeChange);
        }
        const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
        if (prefersDark) {
            document.body.classList.add("dark-theme");
            document.body.classList.remove("light-theme");
        } else {
            document.body.classList.add("light-theme");
            document.body.classList.remove("dark-theme");
        }
    } else {
        if (systemThemeListener) {
            systemThemeListener.removeEventListener("change", systemThemeChange);
            systemThemeListener = null;
        }
        document.body.classList.add(`${selectedTheme}-theme`);
        document.body.classList.remove(
            `${selectedTheme === "light" ? "dark" : "light"}-theme`
        );
    }
    localStorage.setItem("selected-theme", selectedTheme);
}

function closeDialog() {
    const dialogBox = document.getElementById("dialog-box");
    dialogBox.style.display = "none";

    const fileInput = document.querySelector("#file-input");
    fileInput.value = "";
    fileInput.dispatchEvent(new Event("change"));

    // Clear the file name
    const fileNameSpan = document.getElementById("file-name");
    fileNameSpan.textContent = "";
}

function openDropdown(event) {
    event.stopPropagation();

    const dropdowns = document.querySelectorAll(".dropdown-content");
    dropdowns.forEach((dropdown) => {
        dropdown.style.display = "none";
    });

    const dropdownContent = event.target.nextElementSibling;
    dropdownContent.style.display = "block";

    window.onclick = function (e) {
        if (!e.target.matches(".options-button") && !dropdownContent.contains(e.target)) {
            dropdownContent.style.display = "none";
        }
    };

    const replaceButton = event.target.nextElementSibling.querySelector("#replaceButton");
    replaceButton.addEventListener("click", function (e) {
        e.stopPropagation();
        const dialogBox = document.getElementById("dialog-box");
        const filePathInput = dialogBox.querySelector('input[name="filePath"]');
        filePathInput.value = e.target.parentNode.querySelector('input[name="filePath"]').value;
        dialogBox.style.display = "block";
        closeDropdown();
    });

    const historyButton = event.target.nextElementSibling.querySelector("#historyButton");
    historyButton.addEventListener("click", function (e) {
        e.stopPropagation();
        const dialogBox = document.getElementById("dialog-box");
        const filePathInput = dialogBox.querySelector('input[name="filePath"]');
        filePathInput.value = e.target.parentNode.querySelector('input[name="filePath"]').value;
        showFileHistory(filePathInput.value)
    });

    const downloadButton = event.target.nextElementSibling.querySelector("#downloadButton");
    downloadButton.addEventListener("click", function (e) {
        e.stopPropagation();
        const dialogBox = document.getElementById("dialog-box");
        const filePathInput = dialogBox.querySelector('input[name="filePath"]');
        filePathInput.value = e.target.parentNode.querySelector('input[name="filePath"]').value;
        downloadFile(filePathInput.value)
    });
}

function closeDropdown() {
    const dropdownContent = document.querySelector(".dropdown-content");
    dropdownContent.style.display = "none";
}

document.addEventListener("DOMContentLoaded", function () {
    initSortLinks();
});

function initSortLinks() {
    const sortLinks = document.querySelectorAll(".sort");
    sortLinks.forEach(initSortLink);
}

function initSortLink(link) {
    hideArrows(link);
    setInitialArrowState(link);

    link.addEventListener("click", function (event) {
        event.preventDefault();
        handleSortLinkClick(this);
    });
}

function handleSortLinkClick(link) {
    const sortBy = link.dataset.sortby;
    const currentSortOrder = link.dataset.sortorder || "none";
    const newSortOrder = getNewSortOrder(currentSortOrder);

    updateSortLink(link, newSortOrder);
    updateOtherSortLinks(link);
    updateLocalStorage(sortBy, newSortOrder);
    updateURLAndRedirect(sortBy, newSortOrder);
    updateArrows(link, newSortOrder);
}

function getNewSortOrder(currentSortOrder) {
    return currentSortOrder === "none" ? "asc" : currentSortOrder === "asc" ? "desc" : "none";
}

function updateSortLink(link, newSortOrder) {
    link.dataset.sortorder = newSortOrder;
}

function updateOtherSortLinks(link) {
    const sortLinks = document.querySelectorAll(".sort");

    sortLinks.forEach(function (otherLink) {
        if (otherLink !== link) {
            otherLink.dataset.sortorder = "";
            hideArrows(otherLink);
        }
    });
}

function updateArrows(link, newSortOrder) {
    const arrows = link.querySelectorAll(".arrow");

    if (newSortOrder === "none") {
        arrows[0].style.display = "none";
        arrows[1].style.display = "none";
    } else {
        arrows[0].style.display = newSortOrder === "asc" ? "inline-block" : "none";
        arrows[1].style.display = newSortOrder === "desc" ? "inline-block" : "none";
    }
}

function updateLocalStorage(sortBy, newSortOrder) {
    if (newSortOrder !== "none") {
        localStorage.setItem(`sortOrder_${sortBy}`, newSortOrder);
    } else {
        localStorage.removeItem(`sortOrder_${sortBy}`);
    }
}

function updateURLAndRedirect(sortBy, newSortOrder) {
    const url = new URL(window.location.href);
    const query = url.searchParams.get("query") || "";
    const page = parseInt(url.searchParams.get("page")) || 0;

    url.searchParams.set("query", query);
    url.searchParams.set("page", page);

    if (newSortOrder === "none") {
        url.searchParams.delete("sortBy");
        url.searchParams.delete("sortOrder");
    } else {
        url.searchParams.set("sortBy", sortBy);
        url.searchParams.set("sortOrder", newSortOrder);
    }

    window.location.href = url.toString();
}

function hideArrows(link) {
    const arrows = link.querySelectorAll(".arrow");
    arrows[0].style.display = "none";
    arrows[1].style.display = "none";
}

function setInitialArrowState(link) {
    const sortBy = link.dataset.sortby;
    const sortOrder = localStorage.getItem(`sortOrder_${sortBy}`);

    if (sortOrder) {
        link.dataset.sortorder = sortOrder;
        const arrows = link.querySelectorAll(".arrow");
        arrows[0].style.display = sortOrder === "asc" ? "inline-block" : "none";
        arrows[1].style.display = sortOrder === "desc" ? "inline-block" : "none";
    }
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
                    deleteButton.onclick = () => deleteFileFromHistory(file.filePath);
                    deleteButton.classList.add('small-button');

                    const downloadButton = document.createElement('button');
                    downloadButton.textContent = 'Download';
                    downloadButton.onclick = () => downloadFile(file.filePath);
                    downloadButton.classList.add('small-button');

                    const buttonDiv = document.createElement('div');
                    buttonDiv.style.float = 'right';
                    buttonDiv.style.display = 'flex';
                    buttonDiv.appendChild(downloadButton);
                    buttonDiv.appendChild(deleteButton);

                    listItem.appendChild(dateSpan);
                    listItem.appendChild(buttonDiv);
                    fileHistoryList.appendChild(listItem);
                });
            }

            // Show the file history dialog
            const fileHistoryDialog = document.getElementById('fileHistoryDialog');
            fileHistoryDialog.style.display = 'block';
        });
}

function closeFileHistoryDialog() {
    const fileHistoryDialog = document.getElementById('fileHistoryDialog');
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

function downloadFile(filePath) {
    window.location.href = `/api/download?filePath=${encodeURIComponent(filePath)}`;
}

function openSettingsDialog() {
    const settingsDialog = document.getElementById("settingsDialog");
    settingsDialog.style.display = "block";
}

function closeSettingsDialog() {
    const settingsDialog = document.getElementById("settingsDialog");
    settingsDialog.style.display = "none";
}

function logout() {
    fetch('/auth/logout')
        .then(response => {
            if (response.ok) {
                window.location.href = '/auth/login';
            } else {
                console.error('Logout failed with status:', response.status);
            }
        })
        .catch(error => {
            console.error('Logout failed with error:', error);
        });
}

function grantAdmin() {
    const username = document.getElementById('username').value;
    if (!username) {
        alert('Please enter a username.');
        return;
    }

    fetch('/api/grantAdmin?username=' + encodeURIComponent(username), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    })
        .then(response => response.text())
        .then(text => {
            alert(text);
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error processing request');
        });
}

function resetDatabase() {
    fetch('/api/resetDatabase', {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            response.text().then(text => console.log(text));
        } else {
            console.error('Failed to reset database with status:', response.status);
        }
    }).catch(error => {
        console.error('Reset database failed with error:', error);
    });

    setTimeout(() => {
        window.location.reload();
    }, 2500);
}

function restartServer() {
    fetch('/api/restartServer', {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            response.text().then(text => console.log(text));
        } else {
            console.error('Failed to restart server with status:', response.status);
        }
    }).catch(error => {
        console.error('Restart server failed with error:', error);
    });

    setTimeout(() => {
        window.location.reload();
    }, 2500);
}

function changePassword(username, newPassword) {
    const formData = new URLSearchParams();
    formData.append('username', username);
    formData.append('newPassword', newPassword);

    fetch('/api/changePassword?username=' + encodeURIComponent(username) + '&newPassword=' + encodeURIComponent(newPassword), {
        method: 'GET',
        credentials: 'include'
    }).then(response => {
        if (response.ok) {
            response.text().then(text => console.log(text));
        } else {
            console.error('Failed to change password with status:', response.status);
        }
    }).catch(error => {
        console.error('Change password failed with error:', error);
    });
}
