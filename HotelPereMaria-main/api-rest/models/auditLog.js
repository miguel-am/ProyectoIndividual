const mongoose = require('mongoose');

const auditLogSchema = new mongoose.Schema({
    booking_id: { type: mongoose.Schema.Types.ObjectId, ref: 'Reservations', required: true },
    action: { type: String, required: true }, // Ej: 'CREACION', 'CANCELACION', 'CHECK-IN'
    actor_id: { type: mongoose.Schema.Types.ObjectId, required: true }, 
    actor_type: { type: String, enum: ['user', 'employee'], required: true }, 
    previous_state: { type: Object, default: null }, // JSON antes del cambio 
    new_state: { type: Object, default: null },      // JSON después del cambio 
    timestamp: { type: Date, default: Date.now }
});
auditLogSchema.index({ timestamp: 1 }, { expireAfterSeconds: 259200 });
// El log es inmutable: evitamos que se pueda editar o borrar desde la API 
auditLogSchema.pre('save', function(next) {
    if (!this.isNew) return next(new Error('El log de auditoría no puede ser modificado.'));
    next();
});

module.exports = mongoose.model('AuditLog', auditLogSchema);