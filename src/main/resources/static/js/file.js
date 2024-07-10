let selectedFiles = [];
const MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB
const MAX_FILES = 10;

document.getElementById('addFileToMessage').addEventListener('click', function() {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.multiple = true;
    fileInput.addEventListener('change', handleFileSelection);
    fileInput.click();
});

function handleFileSelection(event) {
    const newFiles = Array.from(event.target.files);

    newFiles.forEach(file => {
        if (file.size > MAX_FILE_SIZE) {
            alert(`File ${file.name} is too large. Maximum file size is 15 MB.`);
            return;
        }

        if (selectedFiles.length >= MAX_FILES) {
            alert('Maximum number of files (10) reached.');
            return;
        }

        selectedFiles.push(file);
    });

    updateFileList();
    updateSendButton();
}

function updateFileList() {
    const fileListContainer = document.getElementById('fileList');
    fileListContainer.innerHTML = '';
    selectedFiles.forEach((file, index) => {
        const fileItem = document.createElement('div');
        fileItem.textContent = file.name;
        const removeButton = document.createElement('button');
        removeButton.textContent = 'X';
        removeButton.onclick = () => removeFile(index);
        fileItem.appendChild(removeButton);
        fileListContainer.appendChild(fileItem);
    });
}

function removeFile(index) {
    selectedFiles.splice(index, 1);
    updateFileList();
    updateSendButton();
}

function updateSendButton() {
    const messageInput = document.getElementById("messageInput");
    const sendMessageButton = document.getElementById("sendMessageButton");
    const hasContent = messageInput.value.trim() !== "" || selectedFiles.length > 0;

    if (hasContent) {
        sendMessageButton.innerHTML = '<img src="/svg/send.svg" alt="send">';
        sendMessageButton.setAttribute("onclick", "sendMessage()");
    } else {
        sendMessageButton.innerHTML = '<img src="/svg/mic.svg" alt="mic">';
        sendMessageButton.setAttribute("onclick", "sendVoiceMessage()");
    }
}