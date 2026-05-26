const Communication = require('../models/communication');
const { findById } = require('../models/reservation');
const { userDatabaseModel } = require('../models/user'); 


async function createCom(resId, tipo, contenido, autor = 'Sistema') {
    try {
        await Communication.create({
            reservationId: resId,
            type: tipo,
            content: contenido,
            author: autor,
            sent_at: new Date()
        });
    } catch (err) {
        console.error("Error al registrar comunicación:", err);
    }
}

async function getCommunications(req, res) {
    try {
        const comms = await Communication.find({ reservationId: req.params.id }).sort({ sent_at: -1 });
        res.json(comms);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
}

async function addManualNote(req, res) {
    try {
        const { content } = req.body;
        const actorId = req.user.id; // El ID que viene en el token

        // Buscamos al usuario que está realizando la acción (el admin/empleado)
        const staffUser = await userDatabaseModel.findById(actorId);
        
        // Definimos el nombre: si existe firstName lo usamos, si no, el rol o 'Sistema'
        const authorName = staffUser ? staffUser.firstName : req.user.rol || 'Empleado';

        const newNote = await Communication.create({
            reservationId: req.params.id,
            type: 'manual_note',
            content: content,
            author: authorName,
            sent_at: new Date()
        });

        res.status(201).json(newNote);
    } catch (err) {
        console.error("Error al añadir nota:", err);
        res.status(500).json({ error: err.message });
    }
}

// Método para la App móvil 
async function getClientTimeline(req, res) {
    try {
        // Buscamos solo comunicaciones tipo email o push (evitamos 'manual_note')
        const timeline = await Communication.find({ 
            reservationId: req.params.id,
            type: { $in: ['email', 'push'] } 
        }).sort({ sent_at: 1 }); // Orden cronológico (antiguo a reciente)
        
        res.json(timeline);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
}
  

module.exports = {
    createCom,
    getCommunications,
    addManualNote,
    getClientTimeline
}