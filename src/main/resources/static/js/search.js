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
    const searchInputs = document.querySelectorAll("[data-search-field]");
    const searchButton = document.getElementById("search-button");

    searchInputs.forEach((input) => {
        const searchField = input.dataset.searchField;
        const savedValue = localStorage.getItem(searchField);
        if (savedValue) {
            input.value = savedValue;
        }
    });
    searchButton.addEventListener("click", () => {
        const queryParams = new URLSearchParams();

        searchInputs.forEach((input) => {
            const searchField = input.dataset.searchField;
            const value = input.value;

            if (value) {
                queryParams.append(searchField, value);
                localStorage.setItem(searchField, value);
            } else {
                localStorage.removeItem(searchField);
            }
        });

        // Redirect the user to the search results page with the query parameters
        window.location.href = `/search?${queryParams.toString()}`;
    });


}