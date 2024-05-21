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

function getCurrentParams() {
    const url = new URL(window.location.href);
    return {
        query: url.searchParams.get("query") || "",
        page: parseInt(url.searchParams.get("page")) || 0,
        currentSortBy: url.searchParams.get("sortBy"),
        currentSortOrder: url.searchParams.get("sortOrder"),
        currentSort: localStorage.getItem('sortByLastModified') || 'false'
    };
}

function updateURLAndLocalStorage(sortBy, newSortOrder, params) {
    const url = new URL(window.location.href);
    url.searchParams.set("query", params.query);
    url.searchParams.set("page", params.page);
    if (newSortOrder === "none") {
        url.searchParams.delete("sortBy");
        url.searchParams.delete("sortOrder");
        localStorage.removeItem("sortBy");
        localStorage.removeItem("sortOrder");
    } else {
        url.searchParams.set("sortBy", sortBy);
        url.searchParams.set("sortOrder", newSortOrder);
        localStorage.setItem("sortBy", sortBy);
        localStorage.setItem("sortOrder", newSortOrder);
    }
    url.searchParams.set("sortByLastModified", params.currentSort);
    return url;
}

function redirectTo(url) {
    window.location.href = url.toString();
}

function updateURLAndRedirect(sortBy, newSortOrder) {
    const params = getCurrentParams();
    if (params.currentSortBy !== sortBy || params.currentSortOrder !== newSortOrder) {
        const updatedUrl = updateURLAndLocalStorage(sortBy, newSortOrder, params);
        redirectTo(updatedUrl);
    }
}