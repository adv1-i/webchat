const chatUserName = document.querySelector('.chat-name');
const chatLastInfo = document.querySelector('.chat-last-info');
const popupOverlay = document.querySelector('.popup-overlay');
const popup = document.querySelector('.popup');
const closePopup = document.querySelector('.close-popup');
const memberList = document.querySelector('.popup_member_list');
const showAllToggle = document.querySelector('.show-all-toggle');
const tabAllMembers = document.querySelector('.popup_tab_all_members');
const tabModerators = document.querySelector('.popup_tab_moderators');
const addMemberContainer = document.querySelector('.add_member_to_room_container');

let allUsers = [];
let moderators = [];

function openPopup() {
    popupOverlay.style.display = 'flex';
    setTimeout(() => {
        popup.classList.add('active');
    }, 10);
    loadRoomDetails();
}

function closePopupFunction() {
    popup.classList.remove('active');
    setTimeout(() => {
        popupOverlay.style.display = 'none';
    }, 300);
}

function loadRoomDetails() {
    const roomId = currentRoomId;
    fetch(`/api/rooms/${roomId}/details`)
        .then(response => response.json())
        .then(data => {
            allUsers = data.users;
            moderators = allUsers.filter(user => data.moderatorIds.includes(user.id.toString()));
            displayUsers(allUsers);
        })
        .catch(error => console.error('Error loading room details:', error));
}

function displayUsers(users) {
    memberList.innerHTML = '';
    users.forEach(user => {
        const li = document.createElement('li');
        li.className = 'popup_member_item';
        li.innerHTML = `
            <span class="popup_member_name">${user.username}</span>
            <span class="popup_member_role">${moderators.some(mod => mod.id === user.id) ? 'Администратор' : 'Участник'}</span>
            ${user.username !== currentUsername ? `
                <button class="delete-user-btn">
                    <img src="/svg/delete_from_room.svg" alt="Delete user" />
                </button>
            ` : ''}
        `;
        if (user.username !== currentUsername) {
            const deleteBtn = li.querySelector('.delete-user-btn');
            deleteBtn.addEventListener('click', () => confirmDeleteUser(user));
        }
        memberList.appendChild(li);
    });
}

function confirmDeleteUser(user) {
    if (confirm(`Вы точно хотите удалить ${user.username} из комнаты чата?`)) {
        deleteUserFromRoom(user.id);
    }
}

function deleteUserFromRoom(userId) {
    const roomId = currentRoomId;
    fetch(`/api/rooms/${roomId}/users/${userId}`, {
        method: 'DELETE',
    })
        .then((response) => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Failed to delete user from room');
            }
        })
        .then((data) => {
            loadRoomDetails();
            updateRoomDetails(roomId);
        })
        .catch((error) => {
            console.error('Error:', error);
        });

}


function switchTab(tab) {
    tabAllMembers.classList.remove('active');
    tabModerators.classList.remove('active');
    tab.classList.add('active');

    if (tab === tabAllMembers) {
        addMemberContainer.classList.remove('hidden');
        displayUsers(allUsers);
    } else {
        addMemberContainer.classList.add('hidden');
        displayUsers(moderators);
    }
}

chatUserName.addEventListener('click', openPopup);
chatLastInfo.addEventListener('click', openPopup);
closePopup.addEventListener('click', closePopupFunction);
popupOverlay.addEventListener('click', (e) => {
    if (e.target === popupOverlay) {
        closePopupFunction();
    }
});

tabAllMembers.addEventListener('click', () => switchTab(tabAllMembers));
tabModerators.addEventListener('click', () => switchTab(tabModerators));


const addMember = document.querySelector('.add_member_to_room_container');
addMember.style.cursor = 'pointer';

addMember.addEventListener('click', function() {
    document.querySelector('.popup-overlay').style.display = 'none';
    document.getElementById('newPopup').style.display = 'flex';
});

document.getElementById('backBtn').addEventListener('click', function() {
    document.getElementById('newPopup').style.display = 'none';
    document.querySelector('.popup-overlay').style.display = 'flex';
});