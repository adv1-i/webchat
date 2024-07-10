const textarea = document.getElementById("messageInput");
const sendButton = document.getElementById("sendMessageButton");

function adjustTextareaHeight() {
    textarea.style.height = "auto";
    textarea.style.height = Math.min(textarea.scrollHeight, 100) + "px";
    updateSendButton();
}

textarea.addEventListener("input", adjustTextareaHeight);
textarea.addEventListener("keydown", function (event) {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
});

// switch between text and voice buttons

document.addEventListener("DOMContentLoaded", function () {
    const messageInput = document.getElementById("messageInput");
    messageInput.addEventListener("input", updateSendButton);
});