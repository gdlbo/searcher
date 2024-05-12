let isAdmin = false;

window.onload = function () {
    initTheme();
    initDropZone();
    loadSearch();
};

document.addEventListener("DOMContentLoaded", function () {
    initSortLinks();
    isAdmin = document.body.getAttribute('data-is-admin') === 'true';
});

function openDropdown(event) {
    event.stopPropagation();
    closeAllDropdowns();
    const dropdownContent = event.target.nextElementSibling;
    dropdownContent.style.display = "block";

    setupGlobalClickListener(dropdownContent);
    setupButtonHandlers(dropdownContent);
}

function closeAllDropdowns() {
    const dropdowns = document.querySelectorAll(".dropdown-content");
    dropdowns.forEach(dropdown => dropdown.style.display = "none");
}

function setupGlobalClickListener(dropdownContent) {
    window.onclick = function (event) {
        if (!event.target.matches(".options-button") && !dropdownContent.contains(event.target)) {
            dropdownContent.style.display = "none";
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

function openSettingsDialog() {
    const settingsDialog = document.getElementById("settingsDialog");
    settingsDialog.style.display = "block";
}

function closeSettingsDialog() {
    const settingsDialog = document.getElementById("settingsDialog");
    settingsDialog.style.display = "none";
}