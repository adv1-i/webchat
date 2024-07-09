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
    fetch(`/api/messages/room/${roomId}`)
        .then(response => response.json())
        .then(messages => {
            const messagesDiv = document.getElementById('messages');
            messagesDiv.innerHTML = '';
            messages.forEach(message => {
                const messageDiv = document.createElement('div');
                messageDiv.className = 'message';
                if (message.sender === currentUsername) {
                    messageDiv.classList.add('sent');
                } else {
                    messageDiv.classList.add('received');
                }
                messageDiv.textContent = `${message.sender}: ${message.content}`;
                messagesDiv.appendChild(messageDiv);
            });
        });
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

            const userList = document.getElementById('popupMemberList');
            userList.innerHTML = '';
            data.users.forEach(user => {
                const li = document.createElement('li');
                li.textContent = user.username;
                userList.appendChild(li);
            });
        })
        .catch(error => console.error('Error fetching room details:', error));
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
    if (messageContent && stompClient && currentRoomId) {
        var chatMessage = {
            sender: currentUsername,
            content: messageContent,
            roomId: currentRoomId,
            recipients: currentRecipients
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        document.getElementById('messageInput').value = '';
        adjustTextareaHeight();
    }
}


function showMessageOutput(messageOutput) {
    var messageDiv = document.createElement('div');
    messageDiv.className = 'message';
    if (messageOutput.sender === currentUsername) {
        messageDiv.classList.add('sent');
    } else {
        messageDiv.classList.add('received');
    }
    messageDiv.textContent = `${messageOutput.sender}: ${messageOutput.content}`;
    document.getElementById('messages').appendChild(messageDiv);
}

function handleKeyPress(event) {
    if (event.key === "Enter") {
        sendMessage();
    }
}


window.onload = function () {
    connect();
};

window.onbeforeunload = function() {
    sendUserStatus('OFFLINE');
};