function subscribeToMessageStatus() {
    stompClient.subscribe('/user/queue/message-status', function (statusUpdate) {
        const update = JSON.parse(statusUpdate.body);
        updateMessageStatus(update);
    });
}

function updateMessageStatus(statusUpdate) {
    const messageElement = document.querySelector(`[data-message-id="${statusUpdate.messageId}"]`);
    if (messageElement && messageElement.getAttribute('data-sender') === currentUsername) {
        const statusIcon = messageElement.querySelector('.message-status');
        if (statusIcon) {
            statusIcon.className = `message-status ${statusUpdate.status.toLowerCase()}`;
            statusIcon.src = getStatusIcon(statusUpdate.status);
        }
    }
}

function getStatusIcon(status) {
    switch (status) {
        case 'SENT':
        case 'DELIVERED':
            return '/svg/message_sent.svg';
        case 'READ':
            return '/svg/message_read.svg';
        default:
            return '';
    }
}

// Обновленные функции для работы со статусами сообщений
function markMessagesAsDelivered(roomId) {
    return fetch(`/api/messages/room/${roomId}/delivered`, {
        method: 'POST',
        credentials: 'include'
    }).then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
    });
}

function markMessagesAsRead(roomId) {
    return fetch(`/api/messages/room/${roomId}/read`, {
        method: 'POST',
        credentials: 'include'
    }).then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
    });
}

function markMessageAsRead(messageId) {
    return fetch(`/api/messages/${messageId}/status?status=READ`, {
        method: 'POST',
        credentials: 'include'
    }).then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
    }).then(() => {
        console.log(`Marked message ${messageId} as read`);
    }).catch(error => {
        console.error(`Error marking message ${messageId} as read:`, error);
    });
}

window.onfocus = () => {
    if (currentRoomId) {
        markMessagesAsRead(currentRoomId)
            .then(() => console.log('Marked messages as read on window focus'))
            .catch(error => console.error('Error marking messages as read on window focus:', error));
    }
};