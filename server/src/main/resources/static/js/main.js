'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var leaveForm = document.querySelector('#leaveForm');
var messageInput = document.querySelector('#message');
var receiverInput = document.querySelector('#receiver');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;
var sessionId = null;

var initSubscribeObj = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();
    let password = document.querySelector('#password').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({username: username, password: password}, onConnected, onError);
    }
    event.preventDefault();
}

function onConnected() {
    // Subscribe to the Start Public Topic
    initSubscribeObj = stompClient.subscribe('/message.new/'+username, onMessageReceived, {username: username});
    connectingElement.classList.add('hidden');
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    var receiverCode = receiverInput.value.trim();

    if(receiverCode && messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            receiver: receiverCode,
            textContent: messageContent,
            type: 'MESSAGE',
            tag: `f${(~~(Math.random()*1e8)).toString(16)}`
        };

        stompClient.send("/message.send", {username: username}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function sendLeave(event) {
    if(stompClient) {
        var chatMessage = {
            sender: username,
            receiver: '',
            textContent: '',
            type: 'LEAVE',
            tag: `f${(~~(Math.random()*1e8)).toString(16)}`
        };

        stompClient.send("/message.send", {username: username}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function sendSubmit(receiverCode, tag) {

    if(receiverCode && stompClient && tag) {
        var chatMessage = {
            sender: username,
            receiver: receiverCode,
            textContent: '',
            type: 'SUBMIT',
            tag: tag
        };

        stompClient.send("/message.send", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        sessionId = message.textContent;
        stompClient.unsubscribe(initSubscribeObj);
        stompClient.subscribe('/message.new/'+sessionId, onMessageReceived, {username: username});

        message.textContent = username + ' joined OK';

    } else if (message.type === 'MESSAGE'){
        // send submit about receipt
        sendSubmit(message.sender, message.tag)

        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    else if (message.type === 'SUBMIT'){
            messageElement.classList.add('chat-message');

            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode('LOG');
            avatarElement.appendChild(avatarText);
            avatarElement.style['background-color'] = getAvatarColor(message.sender);

            messageElement.appendChild(avatarElement);
            message.textContent = 'Member '+ message.sender + ' submit you message'
            var usernameElement = document.createElement('span');
            var usernameText = document.createTextNode(message.sender);
            usernameElement.appendChild(usernameText);
            messageElement.appendChild(usernameElement);
    }


    else if (message.type === 'ERROR'){
            messageElement.classList.add('chat-message');

            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode('ERROR');
            avatarElement.appendChild(avatarText);
            avatarElement.style['background-color'] = getAvatarColor(message.sender);

            messageElement.appendChild(avatarElement);

            var usernameElement = document.createElement('span');
            var usernameText = document.createTextNode(message.sender);
            usernameElement.appendChild(usernameText);
            messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.textContent);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)
leaveForm.addEventListener('submit', sendLeave, true)