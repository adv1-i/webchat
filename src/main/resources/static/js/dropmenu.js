function infoModal() {
    const modal = document.getElementById("myModal");
    modal.style.display = "flex";
    const span = document.getElementsByClassName("close")[0];
    span.onclick = function () {
        modal.style.display = "none";
    };
}

function toggleDropdown() {
    document.getElementById("dropdownMenu").classList.toggle("visible");
}

window.onclick = function (event) {
    if (!event.target.matches(".modal-chat-info")) {
        var dropdowns = document.getElementsByClassName("dropdown-content");
        for (var i = 0; i < dropdowns.length; i++) {
            var openDropdown = dropdowns[i];
            if (openDropdown.classList.contains("visible")) {
                openDropdown.classList.remove("visible");
            }
        }
    }
};