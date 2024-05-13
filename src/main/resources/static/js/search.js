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

function updateURLAndRedirect(sortBy, newSortOrder) {
    const url = new URL(window.location.href);
    const query = url.searchParams.get("query") || "";
    const page = parseInt(url.searchParams.get("page")) || 0;
    const currentSortBy = url.searchParams.get("sortBy");
    const currentSortOrder = url.searchParams.get("sortOrder");
    const currentSort = localStorage.getItem('sortByLastModified') || 'false';

    if (currentSortBy !== sortBy || currentSortOrder !== newSortOrder) {
        url.searchParams.set("query", query);
        url.searchParams.set("page", page);

        if (newSortOrder === "none") {
            url.searchParams.delete("sortBy");
            url.searchParams.delete("sortOrder");
            url.searchParams.delete("sortByLastModified");
            localStorage.removeItem("sortBy");
            localStorage.removeItem("sortOrder");
        } else {
            url.searchParams.set("sortBy", sortBy);
            url.searchParams.set("sortOrder", newSortOrder);
            url.searchParams.set("sortByLastModified", currentSort);
            localStorage.setItem("sortBy", sortBy);
            localStorage.setItem("sortOrder", newSortOrder);
        }

        window.location.href = url.toString();
    }
}