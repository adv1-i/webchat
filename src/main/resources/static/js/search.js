const searchButton = document.getElementById("search-button", "popup_new_search-button");
const searchInput = document.querySelector('input[type="text"]');
// let searchIcon = searchButton.getElementById("search_btn");

function toggleSearchIcon() {
    searchIcon.src =
        searchInput.value.length > 0 ? "/svg/close-pic.svg" : "/svg/search.svg";
}

searchInput.addEventListener("input", toggleSearchIcon);

searchButton.addEventListener("click", function () {
    if (searchInput.value.length > 0) {
        searchInput.value = "";
        toggleSearchIcon();
    }
});