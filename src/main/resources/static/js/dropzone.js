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

    const historyButton = event.target.nextElementSibling.querySelector("#historyButton");
    if (historyButton) {
        historyButton.addEventListener("click", function (e) {
            e.stopPropagation();
            const dialogBox = document.getElementById("dialog-box");
            const filePathInput = dialogBox.querySelector('input[name="filePath"]');
            filePathInput.value = e.target.parentNode.querySelector('input[name="filePath"]').value;
            showFileHistory(filePathInput.value)
        });
    }

    const downloadButton = event.target.nextElementSibling.querySelector("#downloadButton");
    if (downloadButton) {
        downloadButton.addEventListener("click", function (e) {
            e.stopPropagation();
            const dialogBox = document.getElementById("dialog-box");
            const filePathInput = dialogBox.querySelector('input[name="filePath"]');
            filePathInput.value = e.target.parentNode.querySelector('input[name="filePath"]').value;
            downloadFile(filePathInput.value)
        });
    }

    const replaceButton = event.target.nextElementSibling.querySelector("#replaceButton");
    if (replaceButton) {
        replaceButton.addEventListener("click", function (e) {
            e.stopPropagation();
            const dialogBox = document.getElementById("dialog-box");
            dialogBox.style.display = "block";
            closeDropdown();
        });
    }

    const deleteButton = event.target.nextElementSibling.querySelector("#deleteButton");
    if (deleteButton) {
        deleteButton.addEventListener("click", function (e) {
            e.stopPropagation();
            const dialogBox = document.getElementById("dialog-box");
            const filePathInput = dialogBox.querySelector('input[name="filePath"]');
            filePathInput.value = e.target.parentNode.querySelector('input[name="filePath"]').value;
            removeFile(filePathInput.value)
        });
    }
}

function closeDropdown() {
    const dropdownContent = document.querySelector(".dropdown-content");
    dropdownContent.style.display = "none";
}