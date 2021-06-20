const Pushy = require('pushy')

// Plug in secret api key
const pushyAPI = new Pushy(process.env.PUSHY_API_KEY)

// use to send msg to a specific client by the token
function sendMessageToIndividual(token, message) {
    // build the msg for Pushy to send
    var data = {
        "type": "msg",
        "message": message,
        "chatid": message.chatid
    }

    // Send push notif via the Send Notif API
    // https://pushy.me/docs/api/send-notifications 
    pushyAPI.sendPushNotification(data, token, {}, (err, id) => {
        // Log errors to console
        if (err) {
            return console.log('Fatal error', err);
        }

        // log success
        console.log('Push sent successfully! (ID: ' + id + ')');
    })
}

function sendRequestToIndividual(token, sender, receiver) {
    var data = {
        "type": "request",
        "sender": sender,
        "receiver": receiver
    }

    pushyAPI.sendPushNotification(data, token, {}, (err, id) => {
        // Log errors to console
        if (err) {
            return console.log('Fatal error', err);
        }

        // log success
        console.log('Push sent successfully! (ID: ' + id + ')');
    })
}

function notifyAcceptRequest(token, sender, receiver) {
    var data = {
        "type": "connection",
        "sender": sender,
        "receiver": receiver
    }

    pushyAPI.sendPushNotification(data, token, {}, (err, id) => {
        // Log errors to console
        if (err) {
            return console.log('Fatal error', err);
        }

        // log success
        console.log('Push sent successfully! (ID: ' + id + ')');
    })
}

function notifyListChange(token, list) {

    var data = {
        "type": "update",
        "list": list
    }

    pushyAPI.sendPushNotification(data, token, {}, (err, id) => {
        // Log errors to console
        if (err) {
            return console.log('Fatal error', err);
        }

        // log success
        console.log('Push sent successfully! (ID: ' + id + ')');
    })
}

function notifyUserAddedChat(token, user) {
    var data = {
        "type": "userAdded",
        "user": user
    }

    pushyAPI.sendPushNotification(data, token, {}, (err, id) => {
        // Log errors to console
        if (err) {
            return console.log('Fatal error', err);
        }

        // log success
        console.log('Push sent successfully! (ID: ' + id + ')');
    })
}

function notifyUserRemovedChat(token, user) {
    var data = {
        "type": "userRemoved",
        "user": user
    }

    pushyAPI.sendPushNotification(data, token, {}, (err, id) => {
        // Log errors to console
        if (err) {
            return console.log('Fatal error', err);
        }

        // log success
        console.log('Push sent successfully! (ID: ' + id + ')');
    })
}

function notifyDeleteChat(token, chatid) {
    var data = {
        "type": "deleteChat",
        "chatid": chatid
    }

    pushyAPI.sendPushNotification(data, token, {}, (err, id) => {
        // Log errors to console
        if (err) {
            return console.log('Fatal error', err);
        }

        // log success
        console.log('Push sent successfully! (ID: ' + id + ')');
    })
}

function notifyCreateChat(token, chat) {
    var data = {
        "type": "createChat",
        "chat": chat
    }

    pushyAPI.sendPushNotification(data, token, {}, (err, id) => {
        // Log errors to console
        if (err) {
            return console.log('Fatal error', err);
        }

        // log success
        console.log('Push sent successfully! (ID: ' + id + ')');
    })
}

module.exports = {
    sendMessageToIndividual, sendRequestToIndividual, notifyAcceptRequest, notifyListChange, notifyCreateChat, notifyDeleteChat, notifyUserAddedChat, notifyUserRemovedChat
}