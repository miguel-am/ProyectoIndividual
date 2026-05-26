const mongoose = require('mongoose');
const Room = require('../models/rooms'); 
const {userDatabaseModel} = require("../models/user");
const Config = require('../models/Config');
  
async function State(req, res){
    try {
        let config = await Config.findOne({ key: 'global_config' });
        if (!config) config = await Config.create({ key: 'global_config' });
        res.json({ enabled: config.logsEnabled });
    } catch (err) {
        res.status(500).json({ error: "Error al consultar configuración" });
    }
}  

async function ToggleState(req, res) {
    try {
        // Log para ver el tipo de dato y el contenido
        console.log("Tipo de Content-Type:", req.headers['content-type']);
        
        let datos = req.body;

        // Si llega como string (a veces pasa con HttpClient de C#), lo parseamos
        if (typeof datos === 'string') {
            try {
                datos = JSON.parse(datos);
            } catch (e) {
                console.error("Error parseando JSON manual:", e);
            }
        }

        // USAMOS 'datos' EN LUGAR DE 'req.body'
        const { enabled } = datos;

        console.log("Valor de enabled extraído:", enabled);

        if (typeof enabled === 'undefined') {
            return res.status(400).json({ error: "Campo 'enabled' faltante" });
        }

        const config = await Config.findOneAndUpdate(
            { key: 'global_config' },
            { logsEnabled: enabled },
            { 
                upsert: true, 
                new: true,
                returnDocument: 'after' // Para evitar el warning de Mongoose
            }
        );

        res.json({ success: true, enabled: config.logsEnabled });
    } catch (err) {
        console.error("Error en ToggleState:", err);
        res.status(500).json({ error: "Error en el servidor" });
    }
}
module.exports = {
    State,
    ToggleState,
};