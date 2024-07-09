var inactivityTime = function () {
    var time;
    window.onload = resetTimer;
    document.onmousemove = resetTimer;
    document.onkeypress = resetTimer;
    document.onscroll = resetTimer;
    document.onmousedown = resetTimer;
    document.ontouchstart = resetTimer;

    function logout() {
        sendUserStatus('OFFLINE');
    }

    function resetTimer() {
        clearTimeout(time);
        sendUserStatus('ONLINE');
        time = setTimeout(logout, 10000);
    }
};

window.onload = function () {
    connect();
    inactivityTime();
};

window.onbeforeunload = function() {
    sendUserStatus('OFFLINE');
};