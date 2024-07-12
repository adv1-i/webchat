function deleteMessage(messageId) {
    if (confirm("Вы действительно хотите удалить сообщение?")) {
        const messageElement = document.querySelector(`[data-message-id="${messageId}"]`);
        messageElement.remove();
        fetch(`/api/messages/${messageId}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to delete message');
                }
            })
            .catch(error => {
                console.error('Error deleting message:', error);
            });

        currentContextMenu.remove();
        currentContextMenu = null;
    }
}
