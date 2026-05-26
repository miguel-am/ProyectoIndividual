const mongoose = require('mongoose');

const communicationSchema = new mongoose.Schema({
    reservationId: { 
        type: mongoose.Schema.Types.ObjectId, 
        ref: 'Reservation', 
        required: true 
    },
    type: { 
        type: String, 
        enum: ['email', 'push', 'phone', 'manual_note'], 
        required: true 
    },
    content: { 
        type: String, 
        required: true 
    },
    author: { 
        type: String, 
        default: 'Sistema' 
    },
    sent_at: { 
        type: Date, 
        default: Date.now 
    }
});

module.exports = mongoose.model('Communication', communicationSchema);