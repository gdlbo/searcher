function storeSearch() {
    const searchNumber = document.getElementById("searchNumber").value;
    const searchInputs = document.querySelectorAll("[data-search-field]");
    searchInputs.forEach((input) => {
        const searchField = input.dataset.searchField;
        const value = input.value;

        if (value === "") {
            localStorage.removeItem(searchField);
        } else {
            localStorage.setItem(searchField, value);
        }
    });

    if (searchNumber === "") {
        localStorage.removeItem("searchNumber");
    } else {
        localStorage.setItem("searchNumber", searchNumber);
    }
}

function loadSearch() {
    // const searchInputs = document.querySelectorAll('.search-input');
    // searchInputs.forEach((input) => {
    //     input.addEventListener('keypress', (event) => {
    //         if (event.key === 'Enter') {
    //             // document.querySelector('form').submit();
    //             console.log('Enter pressed');
    //         }
    //     });
    // });
    //set search field values from query
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
    searchButton.addEventListener("click", () => {
        // Redirect the user to the search results page with the query parameters
        window.location.href = `/search?${buildQuery()}`;
    });
}

function buildQuery() {
    const params = new URLSearchParams();
    const searchInputs = document.querySelectorAll("[data-search-field]");
    searchInputs.forEach((input) => {
        let value = input.value.trim();
        if (value !== "") params.append(input.dataset.searchField, value);
    });
    return params;
}