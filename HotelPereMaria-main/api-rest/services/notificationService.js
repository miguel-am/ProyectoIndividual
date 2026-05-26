const admin = require('firebase-admin');
const serviceAccount = require("../serviceAccountKey.json"); 

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

async function sendPushNotification(userToken, title, body) {
    if (!userToken) {
        console.log("⚠️ No se envió la notificación: El usuario no tiene un Token FCM registrado.");
        return;
    }

    const message = {
        notification: { title, body },
        token: userToken
    };

    try {
        await admin.messaging().send(message);
        console.log('Notificación push enviada con éxito');
    } catch (error) {
        console.error('Error enviando push a Firebase:', error.message);
    }
}

module.exports = { sendPushNotification };