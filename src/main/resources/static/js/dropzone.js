function initDropZone() {
    const dropZone = document.querySelector(".drop-zone");
    const fileInput = document.querySelector("#file-input");

    if (dropZone) {
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
    }

    if (fileInput) {
        fileInput.addEventListener("change", () => {
            dropZone.classList.remove("drag-over");
            displayFileName(fileInput);
        });
    }
}

function displayFileName(input) {
    if (input.files[0]) {
        const fileName = input.files[0].name;
        const fileNameSpan = document.getElementById("file-name");
        fileNameSpan.textContent = fileName;
    }
}

function closeDialogDropzone() {
    const dialogBox = document.getElementById("replaceDialog");
    dialogBox.style.display = "none";

    const fileInput = document.querySelector("#file-input");
    fileInput.value = "";
    fileInput.dispatchEvent(new Event("change"));

    // Clear the file name
    const fileNameSpan = document.getElementById("file-name");
    if (fileNameSpan) {
        fileNameSpan.textContent = "";
    }
}
function setupDragAndDrop() {
    const uploadDialog = document.getElementById('uploadDialog');
    const fileInput = document.getElementById('fileInput');

    if (uploadDialog) {
        addDragAndDropEvents(uploadDialog, fileInput);
        handleFileDrop(uploadDialog, fileInput);
    }
}

function addDragAndDropEvents(dialogElement, fileInput) {
    const dragOverlay = dialogElement.querySelector('.drag-overlay');

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dialogElement.addEventListener(eventName, preventDefaults, false);
        document.body.addEventListener(eventName, preventDefaults, false);
    });

    // Подсчет количества элементов dragenter и dragleave
    let dragCounter = 0;

    dialogElement.addEventListener('dragenter', (e) => {
        dragCounter++;
        showDragOverlay(dragOverlay);
    }, false);

    dialogElement.addEventListener('dragleave', (e) => {
        dragCounter--;
        if (dragCounter === 0) {
            hideDragOverlay(dragOverlay);
        }
    }, false);

    dialogElement.addEventListener('drop', (e) => {
        dragCounter = 0;
        hideDragOverlay(dragOverlay);
        handleFileDrop(e, fileInput);
    }, false);

    dialogElement.addEventListener('dragover', () => showDragOverlay(dragOverlay), false);
}

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}

function showDragOverlay(overlay) {
    overlay.style.display = 'flex';
}

function hideDragOverlay(overlay) {
    overlay.style.display = 'none';
}

function handleFileDrop(e, fileInput) {
    const dt = e.dataTransfer;
    const files = dt.files;

    if (files.length > 0) {
        fileInput.files = files;
    }
}