let isAdmin = false;

window.onload = function () {
    initTheme();
    initDropZone();
    loadSearch();
};

document.addEventListener("DOMContentLoaded", function () {
    isAdmin = document.body.getAttribute('data-is-admin') === 'true';

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

function updateFile(id) {
    fetch('/api/searchFile?id=' + id)
        .then(response => response.json())
        .then(data => {
            if (data.user != null) {
                openUpdateDialog(data.id, data.decNumber, data.deviceName, data.documentType, data.usedDevices, data.project, data.inventoryNumber, data.location, data.lastModified, data.creationTime, data.user.username)
            } else {
                console.error('Error:', data);
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
    closeAllDropdowns();
}

function setupButtonHandlers(dropdownContent) {
    dropdownContent.addEventListener('click', function (event) {
        event.stopPropagation();
        const button = event.target.closest('button');
        const buttonType = button?.id;
        if (!buttonType) return;

        const content = event.target.closest('.dropdown-content');
        const location = content.getAttribute('data-location')
        const id = content.getAttribute('data-id')

        switch (buttonType) {
            case "historyButton":
                showFileHistory(location);
                break;
            case "downloadButton":
                downloadFile(location);
                break;
            case "replaceButton":
                document.getElementById("replaceDialog").style.display = "block";
                closeAllDropdowns();
                break;
            case "deleteButton":
                removeFile(id);
                break;
            case "updateButton":
                updateFile(id);
                break;
            case "copyButton":
                copyPath(location);
                break;
        }
    });
}

function copyPath(filePath) {
    navigator.clipboard.writeText(filePath).then(function () {
        console.log('Async: Copying to clipboard was successful!');
    }, function (err) {
        console.error('Async: Could not copy text: ', err);
    });
    closeAllDropdowns();
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
                    dateSpan.textContent = file.lastModified;

                    const deleteButton = document.createElement('button');
                    deleteButton.textContent = 'Удалить';
                    deleteButton.onclick = () => deleteFileFromHistory(file.location);
                    deleteButton.classList.add('small-button');

                    const downloadButton = document.createElement('button');
                    downloadButton.textContent = 'Скачать';
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

function openUploadDialog() {
    const uploadDialog = document.getElementById('uploadDialog');
    uploadDialog.style.display = 'block';
}

function closeUploadDialog() {
    const uploadDialog = document.getElementById('uploadDialog');
    uploadDialog.style.display = 'none';
}

function formatDateTimeString(dateTimeString) {
    let datePart = dateTimeString.split(' ')[0];
    let timePart = dateTimeString.split(' ')[1];
    return `${datePart}T${timePart.substring(0, 5)}`;
}

function openUpdateDialog(fileId, decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, location, lastModified, creationTime, userName) {
    document.getElementById('fileId').value = fileId;
    document.getElementById('updateDecNumber').value = decNumber;
    document.getElementById('updateDeviceName').value = deviceName;
    document.getElementById('updateDocumentType').value = documentType;
    document.getElementById('updateUsedDevices').value = usedDevices;
    document.getElementById('updateProject').value = project;
    document.getElementById('updateInventoryNumber').value = inventoryNumber;
    document.getElementById('updateLocation').value = location;
    document.getElementById('updateLastModified').value = formatDateTimeString(lastModified);
    document.getElementById('updateCreationTime').value = formatDateTimeString(creationTime);
    document.getElementById('updateUserName').value = userName;
    document.getElementById('updateDialog').style.display = 'block';
}

function closeUpdateDialog() {
    document.getElementById('updateDialog').style.display = 'none';
}