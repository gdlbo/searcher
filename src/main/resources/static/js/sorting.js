const ARROW_DISPLAY_NONE = "none";
const ARROW_DISPLAY_INLINE_BLOCK = "inline-block";

function initSortLinks() {
    document.querySelectorAll(".sort").forEach(initSortLink);
}

function initSortLink(link) {
    hideArrows(link);
    setInitialArrowState(link);

    link.addEventListener("click", (event) => {
        event.preventDefault();
        handleSortLinkClick(link);
    });
}

function setInitialArrowState(link) {
    const sortBy = link.dataset.sortby;
    const sortOrder = localStorage.getItem(`sortOrder_${sortBy}`);

    if (sortOrder) {
        link.dataset.sortorder = sortOrder;
        updateArrows(link, sortOrder);
    } else {
        hideArrows(link);
    }
}

function handleSortLinkClick(link) {
    if (link.dataset.sortable === "false") {
        return;
    }

    const sortBy = link.dataset.sortby;
    const currentSortOrder = link.dataset.sortorder || ARROW_DISPLAY_NONE;
    const newSortOrder = getNewSortOrder(currentSortOrder);

    updateSortLink(link, newSortOrder);
    updateOtherSortLinks(link);
    updateLocalStorage(sortBy, newSortOrder);
    updateURLAndRedirect(sortBy, newSortOrder);
    updateArrows(link, newSortOrder);
}

function getNewSortOrder(currentSortOrder) {
    if (currentSortOrder === ARROW_DISPLAY_NONE) return "asc";
    if (currentSortOrder === "asc") return "desc";
    return ARROW_DISPLAY_NONE;
}

function updateSortLink(link, newSortOrder) {
    link.dataset.sortorder = newSortOrder;
}

function updateOtherSortLinks(link) {
    document.querySelectorAll(".sort").forEach((otherLink) => {
        if (otherLink !== link) {
            otherLink.dataset.sortorder = ARROW_DISPLAY_NONE;
            hideArrows(otherLink);
        }
    });
}

function updateArrows(link, newSortOrder) {
    const arrows = link.querySelectorAll(".arrow");
    const ascArrowDisplay = newSortOrder === "asc" ? ARROW_DISPLAY_INLINE_BLOCK : ARROW_DISPLAY_NONE;
    const descArrowDisplay = newSortOrder === "desc" ? ARROW_DISPLAY_INLINE_BLOCK : ARROW_DISPLAY_NONE;

    arrows[0].style.display = ascArrowDisplay;
    arrows[1].style.display = descArrowDisplay;
}

function updateLocalStorage(sortBy, newSortOrder) {
    if (newSortOrder !== ARROW_DISPLAY_NONE) {
        localStorage.setItem(`sortOrder_${sortBy}`, newSortOrder);
    } else {
        localStorage.removeItem(`sortOrder_${sortBy}`);
    }
}

function hideArrows(link) {
    const arrows = link.querySelectorAll(".arrow");
    arrows.forEach((arrow) => {
        arrow.style.display = ARROW_DISPLAY_NONE;
    });
}

function initSortByLastModified() {
    let currentSortBy = localStorage.getItem('sortByLastModified') || 'false';
    let button = document.getElementById('toggleSortByLastModified');
    button.textContent = `Sort by Last Modified: ${currentSortBy === 'true' ? 'On' : 'Off'}`;
}

function initShowHidden() {
    let showHidden = localStorage.getItem('showHidden') || 'false';
    let button = document.getElementById('toggleShowHidden');
    button.textContent = `Show hidden files/dirs: ${showHidden === 'true' ? 'On' : 'Off'}`;
}

function toggleSortByLastModified() {
    let button = document.getElementById('toggleSortByLastModified');
    let currentSortBy = localStorage.getItem('sortByLastModified') || 'false';

    let newSortBy = currentSortBy === 'true' ? 'false' : 'true';
    localStorage.setItem('sortByLastModified', newSortBy);

    button.textContent = `Sort by Last Modified: ${newSortBy === 'true' ? 'On' : 'Off'}`;
    updateSortStatusAndRedirect(newSortBy);
}

function updateSortStatusAndRedirect(newSortBy) {
    let searchParams = new URLSearchParams(window.location.search);
    searchParams.set('sortByLastModified', newSortBy);
    window.location.search = searchParams.toString();
}

function toggleShowHidden() {
    let button = document.getElementById('toggleShowHidden');
    let currentSortBy = localStorage.getItem('showHidden') || 'false';

    let showHidden = currentSortBy === 'true' ? 'false' : 'true';
    localStorage.setItem('showHidden', showHidden);

    button.textContent = `Show hidden files/dirs: ${showHidden === 'true' ? 'On' : 'Off'}`;
    updateHiddenAndRedirect(showHidden);
}

function updateHiddenAndRedirect(showHidden) {
    let searchParams = new URLSearchParams(window.location.search);
    searchParams.set('showHidden', showHidden);
    window.location.search = searchParams.toString();
}