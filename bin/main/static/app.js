console.log('app.js loaded');

let stompClient = null;
let currentChatId = null;
let isWebSocketConnected = false;

const chatSessionKeys = {};

function loadSessionKeys() {
    const stored = localStorage.getItem('chatSessionKeys');
    if (stored) {
        try {
            Object.assign(chatSessionKeys, JSON.parse(stored));
            console.log('Loaded session keys for chats:', Object.keys(chatSessionKeys));
        } catch (e) {
            console.error('Failed to load session keys:', e);
        }
    }
}

function saveSessionKeys() {
    try {
        localStorage.setItem('chatSessionKeys', JSON.stringify(chatSessionKeys));
    } catch (e) {
        console.error('Failed to save session keys:', e);
    }
}

function initializeApp() {
    console.log('initializeApp called');
    loadSessionKeys();
    
    const loginContainer = document.getElementById('login-container');
    const loginBox = document.getElementById('login-box');
    const registerBox = document.getElementById('register-box');
    const loginForm = document.getElementById('loginForm');
    const mainAppContainer = document.getElementById('main-app-container');
    const chatWindowContainer = document.getElementById('chat-window-container');
    const messageElement = document.getElementById('message');

    const token = localStorage.getItem('jwtToken');
    console.log('Token:', token ? 'exists' : 'not found');
    if (token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const exp = payload.exp * 1000;
            if (Date.now() >= exp) {
                console.log('Token expired, clearing...');
                localStorage.removeItem('jwtToken');
                localStorage.removeItem('chatSessionKeys');
                return;
            }
            console.log('Auto-login as:', payload.sub);
            showMainApp(payload.sub);
        } catch (e) {
            console.error('Invalid token:', e);
            localStorage.removeItem('jwtToken');
        }
    }

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const login = document.getElementById('login').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ login, password }),
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwtToken', data.accessToken);
                setTimeout(() => showMainApp(login), 100);
            } else {
                messageElement.textContent = 'Login failed. Please check your credentials.';
            }
        } catch (error) {
            messageElement.textContent = 'An error occurred. Please try again later.';
            console.error('Login error:', error);
        }
    });

    document.getElementById('registerForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        const login = document.getElementById('register-login').value;
        const password = document.getElementById('register-password').value;

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ login, password }),
            });

            if (response.ok) {
                messageElement.style.color = 'green';
                messageElement.textContent = 'Registration successful! Please log in.';
                registerBox.classList.add('hidden');
                loginBox.classList.remove('hidden');
            } else {
                const errorText = await response.text();
                messageElement.style.color = 'red';
                messageElement.textContent = `Registration failed: ${errorText}`;
            }
        } catch (error) {
            messageElement.textContent = 'An error occurred during registration.';
            console.error('Register error:', error);
        }
    });

    document.getElementById('show-register-btn').addEventListener('click', () => {
        loginBox.classList.add('hidden');
        registerBox.classList.remove('hidden');
        messageElement.textContent = '';
    });

    document.getElementById('show-login-btn').addEventListener('click', () => {
        registerBox.classList.add('hidden');
        loginBox.classList.remove('hidden');
        messageElement.textContent = '';
    });

    document.getElementById('add-contact-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const contactUsernameInput = document.getElementById('contact-username-input');
        const username = contactUsernameInput.value.trim();
        const contactMessageEl = document.getElementById('contact-message');

        if (username) {
            const response = await addContact(username);
            if (response.ok) {
                contactMessageEl.style.color = 'green';
                contactMessageEl.textContent = `Contact request sent to ${username}.`;
                contactUsernameInput.value = '';
            } else {
                const errorText = await response.text();
                contactMessageEl.style.color = 'red';
                contactMessageEl.textContent = `Error: ${errorText}`;
            }
            setTimeout(() => { contactMessageEl.textContent = ''; }, 3000);
        }
    });

    document.getElementById('logout-button').addEventListener('click', () => {
        if (stompClient) {
            stompClient.disconnect();
            isWebSocketConnected = false;
        }
        localStorage.removeItem('jwtToken');
        window.location.reload();
    });

    document.getElementById('pending-requests-list').addEventListener('click', async (event) => {
        const target = event.target;
        const contactId = target.dataset.id;

        if (target.classList.contains('accept-btn')) {
            await acceptRequest(contactId);
        } else if (target.classList.contains('decline-btn')) {
            await declineOrDeleteContact(contactId);
        }
    });

    document.getElementById('create-chat-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const contactUsername = document.getElementById('contact-select').value;
        const encryptionAlgorithm = document.getElementById('algorithm-select').value;
        const cipherMode = document.getElementById('mode-select').value;
        const paddingMode = document.getElementById('padding-select').value;

        const dhKeys = DiffieHellman.generateKeys();
        const dhPublicKey = dhKeys.publicKey;
        console.log(`Generated DH keys for new chat. Public key: ${dhPublicKey.substring(0, 20)}...`);

        const response = await createChat(contactUsername, encryptionAlgorithm, cipherMode, paddingMode, dhPublicKey);
        if (response.ok) {
            const responseText = await response.text();
            const chatId = parseInt(responseText.match(/\d+/)[0]);
            chatSessionKeys[chatId] = { privateKey: dhKeys.privateKey, sharedSecret: null };
            saveSessionKeys();
            console.log(`Saved private key for chat ${chatId}`);
            await loadChats();
        } else {
            const errorText = await response.text();
            alert(`Failed to create chat: ${errorText}`);
        }
    });

    document.getElementById('send-message-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const messageInput = document.getElementById('message-input');
        const messageText = messageInput.value.trim();
        if (messageText && currentChatId) {
            const session = chatSessionKeys[currentChatId];
            if (!session || !session.sharedSecret) {
                alert("Error: Shared secret not computed for this chat. Cannot send message.");
                return;
            }
            const iv = "000102030405060708090a0b0c0d0e0f";
            const ciphertext = RC6.encrypt(messageText, session.sharedSecret, iv);
            console.log(`Encrypting "${messageText}" to "${ciphertext}"`);
            await sendMessage(currentChatId, ciphertext);
            messageInput.value = '';
        }
    });

    document.getElementById('back-to-main').addEventListener('click', () => {
        showMainApp(getCurrentUsername());
        chatWindowContainer.classList.add('hidden');
        currentChatId = null;
    });

    function showMainApp(username) {
        console.log('showMainApp called for:', username);
        loginContainer.classList.add('hidden');
        chatWindowContainer.classList.add('hidden');
        mainAppContainer.classList.remove('hidden');

        document.getElementById('welcome-user').textContent = `Welcome, ${username}!`;

        console.log('isWebSocketConnected:', isWebSocketConnected);
        if (!isWebSocketConnected) {
            connectWebSocket();
        }

        loadContacts();
        loadPendingRequests();
        loadChats();
    }

    async function loadContacts() {
        const token = localStorage.getItem('jwtToken');
        if (!token) return;

        try {
            const response = await fetch('/api/contacts/accepted', {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            const contacts = await response.json();
            const contactsList = document.getElementById('contacts-list');
            const contactSelect = document.getElementById('contact-select');

            contactsList.innerHTML = '';
            contactSelect.innerHTML = '';

            contacts.forEach(contact => {
                contactsList.innerHTML += `<li>${contact}</li>`;
                contactSelect.innerHTML += `<option value="${contact}">${contact}</option>`;
            });
        } catch (error) {
            console.error('Failed to load contacts:', error);
        }
    }

    async function loadPendingRequests() {
        const token = localStorage.getItem('jwtToken');
        if (!token) return;

        try {
            const response = await fetch('/api/contacts/pending', {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const requests = await response.json();
            const pendingList = document.getElementById('pending-requests-list');
            pendingList.innerHTML = '';

            requests.forEach(request => {
                const parts = request.split(', ');
                const idPart = parts[0].split(': ')[1];
                const fromPart = parts[1].split(': ')[1];
                const contactId = parseInt(idPart, 10);

                const listItem = document.createElement('li');
                listItem.innerHTML = `<span>From: <b>${fromPart}</b></span>
                                      <div>
                                        <button class="accept-btn" data-id="${contactId}">Accept</button>
                                        <button class="decline-btn" data-id="${contactId}">Decline</button>
                                      </div>`;
                pendingList.appendChild(listItem);
            });
        } catch (error) {
            console.error('Failed to load pending requests:', error);
        }
    }

    async function loadChats() {
        const token = localStorage.getItem('jwtToken');
        if (!token) return;

        try {
            const response = await fetch('/api/chats', {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const chats = await response.json();
            const chatsList = document.getElementById('chats-list');
            chatsList.innerHTML = '';

            chats.forEach(chat => {
                const currentUsername = getCurrentUsername();
                const otherParticipants = chat.participants.filter(p => p !== currentUsername);
                const listItem = document.createElement('li');
                listItem.innerHTML = `Chat #${chat.id} with ${otherParticipants.join(', ')} (<b>${chat.status}</b>)`;

                if (chat.status === 'PENDING') {
                    const joinButton = document.createElement('button');
                    joinButton.textContent = 'Join';
                    joinButton.onclick = async () => {
                        const dhKeys = DiffieHellman.generateKeys();
                        const dhPublicKey = dhKeys.publicKey;
                        chatSessionKeys[chat.id] = { privateKey: dhKeys.privateKey, sharedSecret: null };
                        saveSessionKeys();
                        console.log(`Generated and saved private key for joining chat ${chat.id}`);
                        await joinChat(chat.id, dhPublicKey);
                        await loadChats();
                    };
                    listItem.appendChild(joinButton);
                } else if (chat.status === 'ACTIVE') {
                    listItem.style.cursor = 'pointer';
                    listItem.onclick = () => {
                        openChatWindow(chat.id, otherParticipants.join(', '));
                    };
                }

                const deleteButton = document.createElement('button');
                deleteButton.textContent = 'Close';
                deleteButton.style.backgroundColor = '#dc3545';
                deleteButton.onclick = async (e) => {
                    e.stopPropagation();
                    await closeChat(chat.id);
                    await loadChats();
                };
                listItem.appendChild(deleteButton);

                chatsList.appendChild(listItem);
            });
        } catch (error) {
            console.error('Failed to load chats:', error);
        }
    }

    async function addContact(username) {
        const token = localStorage.getItem('jwtToken');
        return await fetch(`/api/contacts/request?toUsername=${encodeURIComponent(username)}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
    }

    async function acceptRequest(contactId) {
        const token = localStorage.getItem('jwtToken');
        const response = await fetch(`/api/contacts/accept/${contactId}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            console.log('Contact accepted, waiting for notification...');
        }
        return response;
    }

    async function declineOrDeleteContact(contactId) {
        const token = localStorage.getItem('jwtToken');
        await fetch(`/api/contacts/decline-or-delete/${contactId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
    }

    async function createChat(contactUsername, encryptionAlgorithm, cipherMode, paddingMode, dhPublicKey) {
        const token = localStorage.getItem('jwtToken');
        return await fetch('/api/chats/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ contactUsername, encryptionAlgorithm, cipherMode, paddingMode, dhPublicKey })
        });
    }

    async function joinChat(roomId, dhPublicKey) {
        const token = localStorage.getItem('jwtToken');
        const response = await fetch(`/api/chats/join/${roomId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ dhPublicKey })
        });

        if (!response.ok) {
            const errorText = await response.text() || "Unknown error";
            alert(`Failed to join chat: ${errorText}`);
        }
    }
    
    async function closeChat(roomId) {
        const token = localStorage.getItem('jwtToken');
        await fetch(`/api/chats/close/${roomId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });
    }

    function openChatWindow(chatId, otherParticipants) {
        mainAppContainer.classList.add('hidden');
        chatWindowContainer.classList.remove('hidden');
        document.getElementById('chat-with-user').textContent = `Chat with ${otherParticipants}`;
        document.getElementById('messages-area').innerHTML = '';
        currentChatId = chatId;

        connectWebSocket();
        loadChatHistory(chatId);
        computeSharedSecretForChat(chatId);
    }

    function connectWebSocket() {
        if (isWebSocketConnected) return;

        try {
            console.log('Connecting WebSocket...');
            const socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            stompClient.debug = null;

            const token = localStorage.getItem('jwtToken');
            const headers = { 'Authorization': `Bearer ${token}` };

            stompClient.connect(headers, function(frame) {
                isWebSocketConnected = true;
                console.log('WebSocket Connected!');
                console.log('Subscribing to notifications and messages...');
                
                const username = getCurrentUsername();
                console.log('Subscribing for user:', username);
                
                stompClient.subscribe('/user/queue/notifications', function(message) {
                    console.log('Received notification:', message.body);
                    const data = JSON.parse(message.body);
                    if (data.type === 'CONTACT_UPDATE') {
                        console.log('Contact update - reloading contacts');
                        loadContacts();
                        loadPendingRequests();
                    } else if (data.type === 'CHAT_UPDATE') {
                        console.log('Chat update - reloading chats');
                        loadChats();
                    }
                });
                
                stompClient.subscribe('/topic/notifications.' + username, function(message) {
                    console.log('Received topic notification:', message.body);
                    const data = JSON.parse(message.body);
                    if (data.type === 'CONTACT_UPDATE') {
                        console.log('Contact update - reloading contacts');
                        loadContacts();
                        loadPendingRequests();
                    } else if (data.type === 'CHAT_UPDATE') {
                        console.log('Chat update - reloading chats');
                        loadChats();
                    }
                });

                stompClient.subscribe('/user/queue/messages', function(message) {
                    console.log('Received message:', message.body);
                    const chatMessage = JSON.parse(message.body);
                    if (chatMessage.senderUsername !== getCurrentUsername() && chatMessage.chatRoomId === currentChatId) {
                        displayMessage(chatMessage, false);
                        saveMessageLocally(chatMessage.chatRoomId, chatMessage.senderUsername, chatMessage.ciphertext, false);
                    }
                });
                
                stompClient.subscribe('/topic/messages.' + username, function(message) {
                    console.log('Received topic message:', message.body);
                    const chatMessage = JSON.parse(message.body);
                    if (chatMessage.senderUsername !== getCurrentUsername() && chatMessage.chatRoomId === currentChatId) {
                        displayMessage(chatMessage, false);
                        saveMessageLocally(chatMessage.chatRoomId, chatMessage.senderUsername, chatMessage.ciphertext, false);
                    }
                });
            }, function(error) {
                console.error('WebSocket connection failed:', error);
                isWebSocketConnected = false;
                
                setTimeout(() => {
                    if (!isWebSocketConnected) {
                        console.log('Retrying WebSocket connection...');
                        connectWebSocket();
                    }
                }, 3000);
            });
        } catch (e) {
            console.error('Error:', e);
        }
    }

    function displayMessage(message, isOutgoing) {
        const messagesArea = document.getElementById('messages-area');
        const messageElement = document.createElement('div');
        
        let decryptedText = "DECRYPTION_FAILED";
        const session = chatSessionKeys[message.chatRoomId];
        if (session && session.sharedSecret) {
            const iv = "000102030405060708090a0b0c0d0e0f";
            decryptedText = RC6.decrypt(message.ciphertext, session.sharedSecret, iv);
            console.log(`Decrypting "${message.ciphertext}" to "${decryptedText}"`);
        } else {
            console.error(`Cannot decrypt message for chat ${message.chatRoomId}: shared secret not found.`);
        }

        messageElement.classList.add('message');
        if (isOutgoing) {
            messageElement.classList.add('outgoing');
        }

        messageElement.textContent = `${message.senderUsername}: ${decryptedText}`;
        messagesArea.appendChild(messageElement);
        messagesArea.scrollTop = messagesArea.scrollHeight;
    }

    async function sendMessage(chatId, ciphertext) {
        const token = localStorage.getItem('jwtToken');
        const message = {
            chatRoomId: chatId,
            senderUsername: getCurrentUsername(),
            ciphertext: ciphertext
        };

        await fetch('/api/messages/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(message)
        });
        displayMessage(message, true);
        saveMessageLocally(chatId, getCurrentUsername(), ciphertext, true);
    }

    function getCurrentUsername() {
        return document.getElementById('welcome-user').textContent.split(', ')[1].replace('!', '');
    }
    
    async function saveMessageLocally(chatRoomId, senderUsername, content, isOutgoing) {
        const token = localStorage.getItem('jwtToken');
        try {
            await fetch('/api/local-messages/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    chatRoomId: chatRoomId,
                    senderUsername: senderUsername,
                    content: content,
                    outgoing: isOutgoing
                })
            });
        } catch (error) {
            console.error('Failed to save message locally:', error);
        }
    }
    
    async function loadChatHistory(chatId) {
        const token = localStorage.getItem('jwtToken');
        try {
            const response = await fetch(`/api/local-messages/chat/${chatId}`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const messages = await response.json();
            const messagesArea = document.getElementById('messages-area');
            messagesArea.innerHTML = '';
            
            messages.forEach(msg => {
                const messageElement = document.createElement('div');
                messageElement.classList.add('message');
                if (msg.outgoing) {
                    messageElement.classList.add('outgoing');
                }
                messageElement.textContent = `${msg.senderUsername}: ${msg.content}`;
                messagesArea.appendChild(messageElement);
            });
            messagesArea.scrollTop = messagesArea.scrollHeight;
        } catch (error) {
            console.error('Failed to load chat history:', error);
        }
    }

    async function computeSharedSecretForChat(chatId) {
        if (chatSessionKeys[chatId] && chatSessionKeys[chatId].sharedSecret) {
            console.log(`Shared secret for chat ${chatId} already exists.`);
            return;
        }

        const mySession = chatSessionKeys[chatId];
        if (!mySession || !mySession.privateKey) {
            console.error(`Cannot compute shared secret for chat ${chatId}: my private key not found.`);
            return;
        }

        try {
            const token = localStorage.getItem('jwtToken');
            const response = await fetch(`/api/chats/${chatId}/other-public-key`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) throw new Error('Failed to get other user public key.');

            const theirPublicKey = await response.text();
            console.log(`Got other user's public key for chat ${chatId}: ${theirPublicKey.substring(0, 20)}...`);

            const sharedSecret = DiffieHellman.computeSharedSecret(mySession.privateKey, theirPublicKey);
            chatSessionKeys[chatId].sharedSecret = sharedSecret;
            saveSessionKeys();
            console.log(`Computed and saved shared secret for chat ${chatId}: ${sharedSecret.substring(0, 20)}...`);
        } catch (error) {
            console.error('Error computing shared secret:', error);
        }
    }
}

document.addEventListener('DOMContentLoaded', initializeApp);
