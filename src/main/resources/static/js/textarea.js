const textarea = document.getElementById("messageInput");
const sendButton = document.getElementById("sendMessageButton");

function adjustTextareaHeight() {
    textarea.style.height = "auto";
    textarea.style.height = Math.min(textarea.scrollHeight, 100) + "px";
}

textarea.addEventListener("input", adjustTextareaHeight);

textarea.addEventListener("keydown", function (event) {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
    }
});

// switch between text and voice buttons

document.addEventListener("DOMContentLoaded", function () {
    const messageInput = document.getElementById("messageInput");
    const sendMessageButton = document.getElementById("sendMessageButton");
    let isTextMode = false;

    messageInput.addEventListener("input", function () {
        const currentValue = messageInput.value.trim();
        if (currentValue !== "" && !isTextMode) {
            isTextMode = true;
            sendMessageButton.classList.remove("zoom-appear");
            sendMessageButton.classList.add("zoom-hide");
            setTimeout(() => {
                sendMessageButton.classList.remove("zoom-hide");
                sendMessageButton.innerHTML = '<img src="/svg/send.svg" alt="send">';
                sendMessageButton.setAttribute("onclick", "sendMessage()");
                sendMessageButton.classList.add("zoom-appear");
            }, 60);
        } else if (currentValue === "" && isTextMode) {
            isTextMode = false;
            sendMessageButton.classList.remove("zoom-appear");
            sendMessageButton.classList.add("zoom-hide");
            setTimeout(() => {
                sendMessageButton.classList.remove("zoom-hide");
                sendMessageButton.innerHTML = '<img src="/svg/mic.svg" alt="mic">';
                sendMessageButton.setAttribute("onclick", "sendVoiceMessage()");
                sendMessageButton.classList.add("zoom-appear");
            }, 60);
        }
    });
});