let selectedMessageId = null;
let selectedMessageElement = null;
let forwardButton = null;
let selectedRoomId = null;

function selectMessage(messageElement, messageId) {
    if (selectedMessageElement) {
        selectedMessageElement.style.backgroundColor = '';
    }

    if (selectedMessageElement === messageElement) {
        selectedMessageElement = null;
        removeActionButtons();
    } else {
        messageElement.style.backgroundColor = '#e0e0e0';
        selectedMessageElement = messageElement;
        showActionButtons(messageId);
    }
}

function removeForwardButton() {
    if (forwardButton) {
        forwardButton.remove();
        forwardButton = null;
    }
}

document.addEventListener('click', (event) => {
    if (!event.target.closest('.message')) {
        if (selectedMessageElement) {
            selectedMessageElement.style.backgroundColor = '';
            selectedMessageElement = null;
        }
        removeForwardButton();
    }
});

function openForwardPopup() {
    const popupOverlay = document.createElement('div');
    popupOverlay.className = 'popup-overlay';
    popupOverlay.style.display = 'flex';

    const popup = document.createElement('div');
    popup.className = 'popup';
    popup.style.opacity = '1';

    const title = document.createElement('h1');
    title.id = 'переслать-сообщение';
    title.className = 'popup_title_text';
    title.textContent = 'Переслать сообщение';

    const roomList = document.createElement('ul');
    roomList.className = 'popup_member_list';

    popup.appendChild(title);
    popup.appendChild(roomList);

    const lowerPart = document.createElement('div');
    lowerPart.className = 'popup_lower_part';
    const forwardMessageButton = document.createElement('button');
    forwardMessageButton.className = 'forward_message_button';
    forwardMessageButton.textContent = 'Переслать';
    forwardMessageButton.disabled = true;
    forwardMessageButton.onclick = () => {
        if (selectedRoomId) {
            forwardMessage(selectedRoomId);
            popupOverlay.remove();
        }
    };
    lowerPart.appendChild(forwardMessageButton);
    popup.appendChild(lowerPart);

    popupOverlay.appendChild(popup);
    document.body.appendChild(popupOverlay);

    popupOverlay.addEventListener('click', (e) => {
        if (e.target === popupOverlay) {
            popupOverlay.remove();
        }
    });

    fetch('/api/rooms')
        .then(response => response.json())
        .then(rooms => {
            rooms.forEach(room => {
                const li = document.createElement('li');
                li.className = 'popup_member_item';

                const nameSpan = document.createElement('span');
                nameSpan.className = 'popup_room_name';
                nameSpan.textContent = room.name;

                li.appendChild(nameSpan);
                li.onclick = () => {
                    roomList.querySelectorAll('li').forEach(item => item.style.backgroundColor = '');
                    li.style.backgroundColor = '#EFF4F8';
                    selectedRoomId = room.id;
                    enableForwardButton(forwardMessageButton);
                };

                roomList.appendChild(li);
            });
        });
}

function resetMessageState() {
    isEditing = false;
    editingMessageId = null;
    selectedMessageId = null;
    selectedRoomId = null;
    originalContent = '';
    originalFiles = [];
    selectedFiles = [];
    updateSendButton();
    updateFileList();

    if (selectedMessageElement) {
        selectedMessageElement.style.backgroundColor = '';
        selectedMessageElement = null;
    }

    removeForwardButton();

    const cancelButton = document.getElementById('cancelEditButton');
    if (cancelButton) {
        cancelButton.remove();
    }

    const fileListContainer = document.getElementById('fileListContainer');
    if (fileListContainer) {
        fileListContainer.style.display = 'none';
    }

    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
        messageInput.value = '';
        adjustTextareaHeight();
    }
}

function enableForwardButton(button) {
    button.disabled = false;
    button.style.opacity = '1';
    button.style.cursor = 'pointer';
}


function forwardMessage(targetRoomId) {
    fetch('/api/messages/forward', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `messageId=${selectedMessageId}&targetRoomId=${targetRoomId}`
    })
        .then(response => response.json())
        .then(forwardedMessage => {
            showNotification('Message forwarded successfully');
            resetMessageState();

            if (currentRoomId !== targetRoomId) {
                joinRoom(targetRoomId);
            }
        })
        .catch(error => {
            console.error('Error forwarding message:', error);
            showNotification('Error forwarding message', 'error');
        });
}