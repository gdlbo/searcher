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