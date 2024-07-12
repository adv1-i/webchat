let selectedFiles = [];
const MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB
const MAX_FILES = 10;

document.getElementById('addFileToMessage').addEventListener('click', function() {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.multiple = true;
    fileInput.addEventListener('change', isEditing ? handleFileSelectionForEdit : handleFileSelection);
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
    const fileListContainer = document.getElementById('fileListContainer');
    const fileList = document.getElementById('fileList');
    const messageInputContainer = document.querySelector(".message-input")
    fileList.innerHTML = '';

    if (selectedFiles.length === 0) {
        fileListContainer.style.display = 'none';
        messageInputContainer.style.borderBottomLeftRadius = '10px';
        messageInputContainer.style.borderBottomRightRadius = '10px';
        return;
    }

    fileListContainer.style.display = 'block';

    messageInputContainer.style.borderBottomLeftRadius = '0';
    messageInputContainer.style.borderBottomRightRadius = '0';

    selectedFiles.forEach((file, index) => {
        const fileItem = document.createElement('div');
        fileItem.className = 'file-item';

        if (file.isExisting) {
            if (file.isImage) {
                const img = document.createElement('img');
                img.className = 'file-preview';
                img.src = `/api/files/${file.fileId}`;
                img.alt = file.name;
                fileItem.appendChild(img);
            } else {
                const icon = document.createElement('img');
                icon.className = 'file-icon';
                icon.src = '/svg/doc.svg';
                fileItem.appendChild(icon);
            }
        } else {
            if (file.type.startsWith('image/')) {
                const img = document.createElement('img');
                img.className = 'file-preview';
                img.src = URL.createObjectURL(file);
                fileItem.appendChild(img);
            } else {
                const icon = document.createElement('img');
                icon.className = 'file-icon';
                icon.src = '/svg/doc.svg';
                fileItem.appendChild(icon);
            }
        }

        const fileName = document.createElement('span');
        fileName.className = 'file-name';
        fileName.textContent = file.name;
        fileItem.appendChild(fileName);

        const removeButton = document.createElement('button');
        removeButton.className = 'remove-file';
        removeButton.textContent = 'X';
        removeButton.onclick = () => removeFile(index);
        fileItem.appendChild(removeButton);

        fileList.appendChild(fileItem);
    });
}

function removeFile(index) {
    if (selectedFiles[index].isExisting) {
        URL.revokeObjectURL(selectedFiles[index].fileId);
    }
    selectedFiles.splice(index, 1);
    updateFileList();
    updateSendButton();
}

function updateSendButton() {
    const messageInput = document.getElementById("messageInput");
    const sendMessageButton = document.getElementById("sendMessageButton");
    const hasContent = messageInput.value.trim() !== "" || selectedFiles.length > 0;

    if (isEditing) {
        sendMessageButton.innerHTML = '<img src="/svg/edited.svg" alt="Edit">';
        sendMessageButton.onclick = confirmEdit;
    } else if (hasContent) {
        sendMessageButton.innerHTML = '<img src="/svg/send.svg" alt="send">';
        sendMessageButton.onclick = sendMessage;
    } else {
        sendMessageButton.innerHTML = '<img src="/svg/mic.svg" alt="mic">';
        // sendMessageButton.onclick = sendVoiceMessage;
    }
}
