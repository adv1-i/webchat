function addUsersToRoom() {
    const selectedUsers = [];
    const checkboxes = document.querySelectorAll('#availableUsersList input[type="checkbox"]:checked');

    checkboxes.forEach(checkbox => {
        selectedUsers.push(checkbox.value);
    });

    if (selectedUsers.length === 0) {
        alert('Пожалуйста, выберите пользователей для добавления.');
        return;
    }

    fetch(`/api/rooms/${currentRoomId}/users`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(selectedUsers)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Не удалось добавить пользователей');
            }
            return response.json();
        })
        .then(updatedRoom => {
            updateRoomDetails(currentRoomId);
            updateAvailableUsersList(selectedUsers);

            document.getElementById('newPopup').style.display = 'none';

            checkboxes.forEach(checkbox => {
                checkbox.checked = false;
            });

            alert('Пользователи успешно добавлены в комнату');
        })
        .catch(error => {
            console.error('Ошибка при добавлении пользователей:', error);
            alert('Произошла ошибка при добавлении пользователей. Пожалуйста, попробуйте снова.');
        });
}