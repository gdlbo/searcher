let isAdmin = false;

window.onload = function () {
    initTheme();
    initDropZone();
    loadSearch();
};

document.addEventListener("DOMContentLoaded", function () {
    isAdmin = document.body.getAttribute('data-is-admin') === 'true';

    initThemeButtons()
    loadCheckboxStates();
    addCheckboxEventListeners();
    initResetSearch();
});

function loadCheckboxStates() {
    const checkboxes = document.querySelectorAll('input[type="checkbox"]');

    checkboxes.forEach(checkbox => {
        const column = checkbox.dataset.column;
        const storedValue = localStorage.getItem(column);
        const isChecked = storedValue === null ? true : storedValue === 'true';
        checkbox.checked = isChecked;
        updateColumnVisibility(column, isChecked);
    });
}

function updateColumnVisibility(column, isChecked) {
    const thElements = document.querySelectorAll(`th[data-column="${column}"]`);
    const tdElements = document.querySelectorAll(`td[data-column="${column}"]`);

    thElements.forEach(th => {
        th.style.display = isChecked ? '' : 'none';
    });

    tdElements.forEach(td => {
        td.style.display = isChecked ? '' : 'none';
    });
}

function saveCheckboxState(column, isChecked) {
    localStorage.setItem(column, isChecked);
}

function addCheckboxEventListeners() {
    const checkboxes = document.querySelectorAll('input[type="checkbox"]');

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            const column = this.dataset.column;
            const isChecked = this.checked;
            saveCheckboxState(column, isChecked);
            updateColumnVisibility(column, isChecked);
        });
    });
}

function openOptionsButton() {
    const optionsDialog = document.getElementById('optionsDialog');
    optionsDialog.style.display = 'block';

    const optionsDialogContent = optionsDialog.firstElementChild

    const location = optionsDialogContent.getAttribute('data-location')
    const id = optionsDialogContent.getAttribute('data-id')

    const buttonsList = optionsDialogContent.querySelector(".button-list")

    const createButton = (text, eventClick) => {
        const button = document.createElement("button");
        button.textContent = text;
        button.setAttribute("data-location", location);
        button.addEventListener("click", eventClick);
        buttonsList.appendChild(button);
    }

    createButton("Скачать", () => {
        downloadFile(location);
        closeDialog('optionsDialog');
    });
    createButton("Копировать путь", () => {
        copyPath(location);
        closeDialog('optionsDialog');
    });
    if (isAdmin) {
        createButton("Заменить", () => {
            document.getElementById("replaceDialog").style.display = "block";
            closeDialog('optionsDialog');
        });
        createButton("Удалить", () => {
            removeFile(id);
            closeDialog('optionsDialog');
        });
        createButton("Редактировать", () => {
            updateFile(id);
            closeDialog('optionsDialog');
        });
    }
    createButton("История", () => {
        showFileHistory(location);
        closeDialog('optionsDialog');
    });
    createButton("Закрыть", () => closeDialog('optionsDialog'));
}

function copyPath(filePath) {
    navigator.clipboard.writeText(filePath).then(function () {
        console.log('Async: Copying to clipboard was successful!');
    }, function (err) {
        console.error('Async: Could not copy text: ', err);
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
                    listItem.style.flexDirection = 'column';
                    listItem.style.alignItems = 'center';
                    listItem.style.border = '1px solid #ccc';
                    listItem.style.padding = '20px';
                    listItem.style.marginBottom = '20px';
                    listItem.style.width = '200px';

                    const dateSpan = document.createElement('span');
                    dateSpan.textContent = file.lastModified;
                    dateSpan.style.marginBottom = '10px';

                    const buttonDiv = document.createElement('div');
                    buttonDiv.style.display = 'flex';
                    buttonDiv.style.justifyContent = 'space-between';
                    buttonDiv.style.width = '100%';

                    const deleteButton = document.createElement('button');
                    deleteButton.textContent = 'Удалить';
                    deleteButton.onclick = () => deleteFileFromHistory(file.location);
                    deleteButton.classList.add('small-button');
                    deleteButton.style.flex = '1';
                    deleteButton.style.marginRight = '10px';

                    const downloadButton = document.createElement('button');
                    downloadButton.textContent = 'Скачать';
                    downloadButton.onclick = () => downloadFile(file.location);
                    downloadButton.classList.add('small-button');
                    downloadButton.style.flex = '1';

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
            fileHistoryDialog.querySelector('.dialog-content').scrollTop = 0;
            document.body.style.overflow = '';
            fileHistoryDialog.style.display = 'block';
        });
}

function uploadDropdown() {
    const uploadDocumentType = document.getElementById("uploadDocumentType");
    const dropdown = document.getElementById("type-doc-upload-dropdown");

    if (dropdown.value === "none") {
        uploadDocumentType.value = ""
    } else {
        uploadDocumentType.value = dropdown.value || "";
    }
}

function updateDropdown() {
    const updateDocumentType = document.getElementById("updateDocumentType");
    const dropdown = document.getElementById("type-doc-update-dropdown");

    if (dropdown.value === "none") {
        updateDocumentType.value = ""
    } else {
        updateDocumentType.value = dropdown.value || "";
    }
}

function formatDateTimeString(dateTimeString) {
    const [datePart, timePart] = dateTimeString.split(' ');
    return `${datePart}T${timePart.slice(0, 5)}`;
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

function closeDialog(elementId) {
    const dialogBox = document.getElementById(elementId);
    dialogBox.querySelector('.dialog-content').scrollTop = 0;
    document.body.style.overflow = '';
    dialogBox.style.display = 'none';

    if (elementId === 'optionsDialog') {
        const buttonsList = dialogBox.querySelector('.button-list');
        buttonsList.innerHTML = '';
    }
}

function openDialog(elementId) {
    const dialogBox = document.getElementById(elementId);
    document.body.style.overflow = 'hidden';
    dialogBox.style.display = 'block';
}