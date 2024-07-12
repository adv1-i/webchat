var stompClient = null;
var currentRoomId = null;
var currentUsername = '';
var currentRecipients = [];


function getCurrentUser() {
    fetch('/api/users/current')
        .then(response => {
            if (response.status === 401) {
                window.location.href = '/login';
            }
            return response.json();
        })
        .then(data => {
            currentUsername = data.username;
        })
        .catch(error => console.error('Error fetching current user:', error));
}


function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        loadRooms();
        getCurrentUser();
        subscribeToUserStatus();
        sendUserStatus('ONLINE');
    });
}

function sendUserStatus(status) {
    if (stompClient && currentUsername) {
        stompClient.send("/app/user.status", {}, JSON.stringify({username: currentUsername, status: status}));
    }
}


function loadRooms() {
    fetch('/api/rooms')
        .then(response => {
            if (response.status === 401) {
                window.location.href = '/login';
            }
            return response.json();
        })
        .then(data => {
            const chatList = document.getElementById('chatList');
            chatList.innerHTML = '';
            data.forEach(room => {
                const li = document.createElement('li');
                li.className = 'chat-room_item';
                li.onclick = () => joinRoom(room.id);
                li.innerHTML = `
                            <div class="chat-room-title">
                                <div class="chat_room_name_role">
                                    <span class="chat_room_link">${room.name}</span>
                                </div>
                            </div>
                        `;
                chatList.appendChild(li);
            });
        });
}

function loadMessages(roomId) {
    const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    fetch(`/api/messages/room/${roomId}?userTimeZone=${encodeURIComponent(userTimeZone)}`)
        .then(response => response.json())
        .then(messages => {
            const messagesDiv = document.getElementById('messages');
            messagesDiv.innerHTML = '';
            messages.forEach(message => {
                showMessageOutput(message);
            });
        });
}

function updateAvailableUsersList(addedUsers) {
    const availableUsersList = document.getElementById('availableUsersList');
    addedUsers.forEach(userId => {
        const userItem = availableUsersList.querySelector(`input[value="${userId}"]`).closest('li');
        if (userItem) {
            userItem.remove();
        }
    });
}

function getAvailableUsers(roomId) {
    fetch(`/api/rooms/${roomId}/available-users`)
        .then(response => response.json())
        .then(users => {
            const availableUsersList = document.getElementById('availableUsersList');
            availableUsersList.innerHTML = '';

            users.forEach(user => {
                const userItem = document.createElement('li');
                userItem.className = 'popup_new_member_item';

                userItem.innerHTML = `
                    <div class="popup_divider_container">
                        <div class="popup_new_member_img">
                            <img src="/svg/chat_room_img_little.svg" alt="member_toAdd_pic">
                        </div>
                        <div class="popup_member_name">${user.username}</div>
                        <input type="checkbox" name="userCheckbox" value="${user.id}">
                    </div>
                `;

                availableUsersList.appendChild(userItem);
            });
        })
        .catch(error => console.error('Ошибка при получении доступных пользователей:', error));
}



function joinRoom(roomId) {
    if (currentRoomId) {
        stompClient.unsubscribe(currentRoomId);
    }
    currentRoomId = roomId;
    stompClient.subscribe(`/topic/${roomId}`, function (messageOutput) {
        showMessageOutput(JSON.parse(messageOutput.body));
    });
    document.getElementById('messages').innerHTML = '';
    fetch(`/api/rooms/${roomId}`)
        .then(response => response.json())
        .then(room => {
            currentRecipients = room.userIds.filter(userId => userId !== currentUsername);
            loadMessages(roomId);
            loadRoomUsers(roomId);
            updateUrl(roomId);
            updateRoomDetails(roomId);
            getAvailableUsers(roomId);
        });
}

function updateRoomDetails(roomId) {
    fetch(`/api/rooms/${roomId}/details`)
        .then(response => response.json())
        .then(data => {
            document.querySelector('.chat-name').textContent = data.name;
            document.querySelector('.chat-last-info').textContent = `${data.userCount} участника`;
            document.querySelector('.room_group_name').textContent = data.name;
            document.querySelector('.room_group_count').textContent = `${data.userCount} участника`;

            displayUsers(data.users);
            getAvailableUsers(roomId);
        })
        .catch(error => console.error('Ошибка при получении деталей комнаты:', error));
}


function updateUrl(roomId) {
    history.pushState(null, '', `/chatroom?sel=${roomId}`);
}

function loadRoomUsers(roomId) {
    fetch(`/api/rooms/${roomId}/users`)
        .then(response => response.json())
        .then(users => {
            updateUserList(users);
        });
}

function subscribeToUserStatus() {
    stompClient.subscribe('/topic/user.status', function (statusOutput) {
        updateUserStatus(JSON.parse(statusOutput.body));
    });
}

function updateUserStatus(statusUpdate) {
    const userElement = document.querySelector(`#userList li[data-username="${statusUpdate.username}"]`);
    if (userElement) {
        const statusElement = userElement.querySelector('.user-status');
        statusElement.className = `user-status ${statusUpdate.status.toLowerCase()}`;
    }
}


function updateUserList(users) {
    let userList = document.getElementById('userList');

    if (!userList) {
        userList = document.createElement('ul');
        userList.id = 'userList';
        document.body.appendChild(userList);
    }

    userList.innerHTML = '';

    users.forEach(user => {
        const li = document.createElement('li');
        li.setAttribute('data-username', user.username);
        const statusSpan = document.createElement('span');
        statusSpan.className = `user-status ${user.status.toLowerCase()}`;
        li.appendChild(statusSpan);
        li.appendChild(document.createTextNode(user.username));
        userList.appendChild(li);
    });
}

function sendMessage() {
    var messageContent = document.getElementById('messageInput').value.trim();
    if (stompClient && currentRoomId) {
        if (selectedFiles.length > 0) {
            const formData = new FormData();
            formData.append('content', messageContent);
            formData.append('sender', currentUsername);
            formData.append('roomId', currentRoomId);
            formData.append('recipients', JSON.stringify(currentRecipients));

            selectedFiles.forEach(file => {
                formData.append('files', file);
            });

            fetch('/api/messages/send-with-files', {
                method: 'POST',
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(message => {
                    // Удаляем локальное отображение сообщения
                    // showMessageOutput(message);
                    clearMessageInput();
                })
                .catch(error => {
                    console.error('Error sending message:', error);
                    alert('Failed to send message. Please try again.');
                });
        } else if (messageContent) {
            const message = {
                content: messageContent,
                sender: currentUsername,
                roomId: currentRoomId,
                recipients: currentRecipients,
                timestamp: new Date(),
                formattedTime: formatTime(new Date())
            };

            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
            // Удаляем локальное отображение сообщения
            // showMessageOutput(message);
            clearMessageInput();
        }
    }
}

// Добавим функцию для форматирования времени
function formatTime(date) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function clearMessageInput() {
    document.getElementById('messageInput').value = '';
    selectedFiles = [];
    updateFileList();
    adjustTextareaHeight();
    updateSendButton();
}


function showMessageOutput(messageOutput) {
    if (!messageOutput || !messageOutput.sender) {
        console.error('Invalid message format:', messageOutput);
        return;
    }

    let messageDiv = document.querySelector(`[data-message-id="${messageOutput.id}"]`);

    if (messageDiv) {
        updateMessageInDOM(messageOutput);
    } else {
        messageDiv = document.createElement('div');
        messageDiv.className = 'message';
        messageDiv.setAttribute('data-message-id', messageOutput.id);
        messageDiv.setAttribute('data-sender', messageOutput.sender);

        if (messageOutput.sender === currentUsername) {
            messageDiv.classList.add('sent');
        } else {
            messageDiv.classList.add('received');
        }

        const messageContentDiv = document.createElement('div');
        messageContentDiv.className = 'message-content';

        let messageText = `${messageOutput.sender}: ${messageOutput.content}`;
        const textDiv = document.createElement('div');
        textDiv.className = 'message-text';
        textDiv.textContent = messageText;

        if (messageOutput.isEdited) {
            const editedIndicator = document.createElement('span');
            editedIndicator.className = 'edited-indicator';
            editedIndicator.textContent = ' (ред.)';
            textDiv.appendChild(editedIndicator);
        }

        messageContentDiv.appendChild(textDiv);


    if (messageOutput.fileIds && messageOutput.fileNames && messageOutput.fileIds.length > 0) {
        var imagesDiv = document.createElement('div');
        imagesDiv.className = 'message-images';

        messageOutput.fileIds.forEach((fileId, index) => {
            const fileName = messageOutput.fileNames[index].toLowerCase();
            if (fileName.endsWith('.jpg') || fileName.endsWith('.jpeg') || fileName.endsWith('.png') || fileName.endsWith('.gif')) {
                var img = document.createElement('img');
                img.src = `/api/files/${fileId}`;
                img.alt = fileName;
                imagesDiv.appendChild(img);
            }
        });

        if (imagesDiv.children.length > 0) {
            messageContentDiv.appendChild(imagesDiv);
        }

        messageOutput.fileIds.forEach((fileId, index) => {
            const fileName = messageOutput.fileNames[index].toLowerCase();
            if (!fileName.endsWith('.jpg') && !fileName.endsWith('.jpeg') && !fileName.endsWith('.png') && !fileName.endsWith('.gif')) {
                var fileDiv = document.createElement('a');
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
    }

        const timeSpan = document.createElement('span');
        timeSpan.className = 'message-time';
        timeSpan.textContent = messageOutput.formattedTime || formatTime(new Date(messageOutput.timestamp));
        messageContentDiv.appendChild(timeSpan);

        messageDiv.appendChild(messageContentDiv);
        messageDiv.oncontextmenu = (event) => showContextMenu(event, messageOutput.id);

        document.getElementById('messages').appendChild(messageDiv);
    }
}

window.onload = function () {
    connect();
};

window.onbeforeunload = function() {
    sendUserStatus('OFFLINE');
};