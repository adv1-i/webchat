let selectedMessageId = null;
let selectedMessageElement = null;
let forwardButton = null;

function selectMessage(messageElement, messageId) {
    if (selectedMessageElement) {
        selectedMessageElement.style.backgroundColor = '';
    }

    if (selectedMessageElement === messageElement) {
        selectedMessageElement = null;
        removeForwardButton();
    } else {
        messageElement.style.backgroundColor = '#e0e0e0';
        selectedMessageElement = messageElement;
        showForwardButton(messageId);
    }
}

function showForwardButton(messageId) {
    removeForwardButton();

    forwardButton = document.createElement('button');
    forwardButton.innerHTML = '<img src="/svg/forward.svg" alt="Forward">';
    forwardButton.onclick = openForwardPopup;
    forwardButton.className = 'forward-button';
    document.querySelector('.chat-header').appendChild(forwardButton);

    selectedMessageId = messageId;
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
    const popup = document.createElement('div');
    popup.className = 'forward-popup';
    popup.innerHTML = '<h3>Select a room to forward the message</h3>';

    fetch('/api/rooms')
        .then(response => response.json())
        .then(rooms => {
            const roomList = document.createElement('ul');
            rooms.forEach(room => {
                const li = document.createElement('li');
                li.textContent = room.name;
                li.onclick = () => forwardMessage(room.id);
                roomList.appendChild(li);
            });
            popup.appendChild(roomList);
        });

    document.body.appendChild(popup);
}

function resetMessageState() {
    isEditing = false;
    editingMessageId = null;
    selectedMessageId = null;
    originalContent = '';
    originalFiles = [];
    selectedFiles = [];
    updateSendButton();
    updateFileList();

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
            document.querySelector('.forward-popup').remove();
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