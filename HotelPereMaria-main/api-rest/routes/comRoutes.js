const express = require('express');
const router = express.Router();
const { verifyToken ,authorizeRoles } = require('../middleware/authMiddleware.js');    
const comController =  require('../controller/communicationController.js');


router.get('/:id', verifyToken, comController.getCommunications);
router.post('/:id', verifyToken, comController.addManualNote);
router.get('/timeline/:id', verifyToken, comController.getClientTimeline);

module.exports = router