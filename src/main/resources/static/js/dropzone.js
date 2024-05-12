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

function closeDropdown() {
    const dropdownContent = document.querySelector(".dropdown-content");
    dropdownContent.style.display = "none";
}