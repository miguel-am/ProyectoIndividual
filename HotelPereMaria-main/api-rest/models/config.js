const mongoose = require('mongoose');

const configSchema = new mongoose.Schema({
    key: { type: String, unique: true, default: 'global_config' },
    logsEnabled: { type: Boolean, default: false },
    logRetentionDays: { type: Number, default: 3 }
});

module.exports = mongoose.model('Config', configSchema);