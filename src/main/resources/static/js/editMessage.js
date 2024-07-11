let isEditing = false;
let editingMessageId = null;
let currentContextMenu = null;

function showContextMenu(event, messageId) {
    event.preventDefault();

    // Close existing context menu if any
    if (currentContextMenu) {
        currentContextMenu.remove();
    }

    const contextMenu = document.createElement('div');
    contextMenu.className = 'context-menu';
    contextMenu.innerHTML = `
        <ul>
            <li onclick="editMessage('${messageId}')">Редактировать</li>
        </ul>
    `;
    document.body.appendChild(contextMenu);
    contextMenu.style.top = `${event.clientY}px`;
    contextMenu.style.left = `${event.clientX}px`;

    currentContextMenu = contextMenu;

    document.addEventListener('click', function removeContextMenu(e) {
        if (!contextMenu.contains(e.target)) {
            contextMenu.remove();
            currentContextMenu = null;
            document.removeEventListener('click', removeContextMenu);
        }
    });
}

function editMessage(messageId) {
    const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
    const messageContent = messageElement.querySelector('.message-text').textContent;
    const messageInput = document.getElementById('messageInput');

    // Remove " (ред.)" from the content if it exists
    const cleanContent = messageContent.replace(`${messageElement.dataset.sender}: `, '').replace(' (ред.)', '');
    messageInput.value = cleanContent;
    adjustTextareaHeight();

    isEditing = true;
    editingMessageId = messageId;

    updateSendButton();

    // Add cancel button
    const cancelButton = document.createElement('button');
    cancelButton.id = 'cancelEditButton';
    cancelButton.innerHTML = 'Отменить';
    cancelButton.onclick = cancelEdit;
    document.querySelector('.input_button_container').appendChild(cancelButton);

    const fileListContainer = document.getElementById('fileListContainer');
    const files = messageElement.querySelectorAll('.message-images img, .message-file');
    if (files.length > 0) {
        fileListContainer.style.display = 'block';
        const fileList = document.getElementById('fileList');
        fileList.innerHTML = '';
        selectedFiles = [];
        files.forEach((file, index) => {
            const fileItem = document.createElement('div');
            fileItem.className = 'file-item';

            const isImage = file.tagName === 'IMG';
            const fileName = isImage ? file.alt : file.querySelector('span').textContent;
            const fileId = isImage ? file.src.split('/').pop() : file.href.split('/').pop();

            fileItem.innerHTML = `
                <span>${fileName}</span>
                <button onclick="removeFile(${index})">X</button>
            `;
            fileList.appendChild(fileItem);

            selectedFiles.push({
                name: fileName,
                isExisting: true,
                fileId: fileId,
                isImage: isImage
            });
        });
    }
    updateFileList();
}

function confirmEdit() {
    if (!isEditing || !editingMessageId) return;

    const messageInput = document.getElementById('messageInput');
    const updatedContent = messageInput.value.trim();

    // Prevent empty messages
    if (updatedContent === '') {
        alert('Сообщение не может быть пустым');
        return;
    }

    const formData = new FormData();
    formData.append('content', updatedContent);
    formData.append('messageType', selectedFiles.length > 0 ? 'FILE' : 'TEXT');

    // Append existing files
    const existingFiles = selectedFiles.filter(file => file.isExisting);
    existingFiles.forEach(file => {
        formData.append('existingFiles', file.fileId);
    });

    // Append new files
    const newFiles = selectedFiles.filter(file => !file.isExisting);
    newFiles.forEach(file => {
        formData.append('newFiles', file);
    });

    fetch(`/api/messages/${editingMessageId}`, {
        method: 'PUT',
        body: formData
    })
        .then(response => response.json())
        .then(editedMessage => {
            updateMessageInDOM(editedMessage);
            clearMessageInput();
            resetEditState();
            showNotification('Сообщение успешно отредактировано');
        })
        .catch(error => {
            console.error('Error editing message:', error);
            showNotification('Ошибка при редактировании сообщения', 'error');
        });
}

function updateMessageInDOM(editedMessage) {
    const messageElement = document.querySelector(`[data-message-id="${editedMessage.id}"]`);
    if (messageElement) {
        const messageContentDiv = messageElement.querySelector('.message-content');
        messageContentDiv.innerHTML = ''; // Clear content

        let messageText = `${editedMessage.sender}: ${editedMessage.content}`;
        const textDiv = document.createElement('div');
        textDiv.className = 'message-text';
        textDiv.textContent = messageText;
        messageContentDiv.appendChild(textDiv);

        // Обновляем файлы, если они есть
        if (editedMessage.fileIds && editedMessage.fileNames && editedMessage.fileIds.length > 0) {
            const imagesDiv = document.createElement('div');
            imagesDiv.className = 'message-images';

            editedMessage.fileIds.forEach((fileId, index) => {
                const fileName = editedMessage.fileNames[index].toLowerCase();
                if (fileName.match(/\.(jpg|jpeg|png|gif)$/i)) {
                    const img = document.createElement('img');
                    img.src = `/api/files/${fileId}`;
                    img.alt = fileName;
                    imagesDiv.appendChild(img);
                } else {
                    const fileDiv = document.createElement('a');
                    fileDiv.className = 'message-file';
                    fileDiv.href = `/api/files/${fileId}`;
                    fileDiv.download = fileName;
                    fileDiv.innerHTML = `
                        <img src="/svg/doc.svg" alt="File">
                        <span>${fileName}</span>
                    `;
                    messageContentDiv.appendChild(fileDiv);
                }
            });

            if (imagesDiv.children.length > 0) {
                messageContentDiv.appendChild(imagesDiv);
            }
        }

        // Добавляем индикатор редактирования
        const editedIndicator = document.createElement('span');
        editedIndicator.className = 'edited-indicator';
        editedIndicator.textContent = ' (ред.)';
        textDiv.appendChild(editedIndicator);
    }
}

function handleFileSelectionForEdit(event) {
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

function cancelEdit() {
    resetEditState();
    clearMessageInput();
}

function resetEditState() {
    isEditing = false;
    editingMessageId = null;
    updateSendButton();

    // Remove cancel button
    const cancelButton = document.getElementById('cancelEditButton');
    if (cancelButton) {
        cancelButton.remove();
    }
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);
    setTimeout(() => notification.remove(), 3000);
}