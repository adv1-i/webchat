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
    const roomId = currentRoomId; // Предполагается, что currentRoomId доступен глобально
    fetch(`/api/rooms/${roomId}/details`)
        .then(response => response.json())
        .then(data => {
            allUsers = data.users;
            moderators = allUsers.filter(user => data.moderatorIds.includes(user.id.toString()));
            displayUsers(allUsers); // По умолчанию показываем всех пользователей
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
        `;
        memberList.appendChild(li);
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