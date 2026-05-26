const mongoose = require('mongoose');

const auditLogSchema = new mongoose.Schema({
    booking_id: { type: mongoose.Schema.Types.ObjectId, ref: 'Reservations', required: true },
    action: { type: String, required: true }, 
    actor_id: { 
            type: mongoose.Schema.Types.ObjectId, 
            required: true, 
            ref:'user'
        },    
    previous_state: { type: Object, default: null }, 
    new_state: { type: Object, default: null },      
    timestamp: { type: Date, default: Date.now }
});
auditLogSchema.index({ timestamp: 1 }, { expireAfterSeconds: 259200 });

auditLogSchema.pre('save', function() {
    if (!this.isNew) {
        throw new Error('El log de auditoría no puede ser modificado.');
    }
});

module.exports = mongoose.model('AuditLog', auditLogSchema);