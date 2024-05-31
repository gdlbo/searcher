function loadSearch() {
    const params = new URLSearchParams(window.location.search);
    const searchInputs = document.querySelectorAll("[data-search-field]");
    searchInputs.forEach((input) => {
        const searchField = input.dataset.searchField;
        const value = params.get(searchField);
        if (value) input.value = value;
    });

    //add other parameters to page query
    const tabs = document.querySelectorAll("div[class=pagination] a");
    tabs.forEach((tab) => {
        const tabUrl = new URL(tab.href);
        const tabParams = new URLSearchParams(tabUrl.search);
        params.set("page", tabParams.get("page"));
        tab.href = `/search?${params}`;
    })

    const searchButton = document.getElementById("search-button");
    if (searchButton) {
        searchButton.addEventListener("click", () => {
            // Redirect the user to the search results page with the query parameters
            window.location.href = `/search?${buildQuery()}`;
        });
    }
}

function buildQuery() {
    const params = new URLSearchParams();
    const searchInputs = document.querySelectorAll("[data-search-field]");

    searchInputs.forEach(input => {
        const value = input.value.trim();
        if (value !== "") {
            params.append(input.dataset.searchField, value);
        }
    });

    return params;
}

function resetSearch(path) {
    const searchInputs = document.querySelectorAll('.search-input');
    searchInputs.forEach(input => {
        input.value = '';
    });

    window.location.href = path;
}

function checkForResults() {
    const tableRows = document.getElementById('file-rows');
    const noResultsDiv = document.getElementById('no-results');
    const rows = tableRows.querySelectorAll('tr');

    if (rows.length === 0) {
        noResultsDiv.style.display = 'block';
    } else {
        noResultsDiv.style.display = 'none';
    }
}

function initResetSearch() {
    const resetButton = document.getElementById('reset-search');

    if (resetButton) {
        resetButton.addEventListener('click', resetSearch);
    }

    checkForResults();
}