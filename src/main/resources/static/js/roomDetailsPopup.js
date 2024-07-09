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

function openPopup() {
    popupOverlay.style.display = 'flex';
    setTimeout(() => {
        popup.classList.add('active');
    }, 10);
}

function closePopupFunction() {
    popup.classList.remove('active');
    setTimeout(() => {
        popupOverlay.style.display = 'none';
    }, 300);
}

chatUserName.addEventListener('click', openPopup);
chatLastInfo.addEventListener('click', openPopup);
closePopup.addEventListener('click', closePopupFunction);
popupOverlay.addEventListener('click', (e) => {
    if (e.target === popupOverlay) {
        closePopupFunction();
    }
});



function switchTab(tab) {
    tabAllMembers.classList.remove('active');
    tabModerators.classList.remove('active');
    tab.classList.add('active');

    if (tab === tabAllMembers) {
        addMemberContainer.classList.remove('hidden');
        memberList.querySelectorAll('.popup_member_item').forEach(item => item.style.display = '');
    } else {
        addMemberContainer.classList.add('hidden');
        memberList.querySelectorAll('.popup_member_item').forEach(item => {
            if (item.querySelector('.popup_member_role').textContent !== 'Администратор') {
                item.style.display = 'none';
            } else {
                item.style.display = '';
            }
        });
    }
}

tabAllMembers.addEventListener('click', () => switchTab(tabAllMembers));
tabModerators.addEventListener('click', () => switchTab(tabModerators));