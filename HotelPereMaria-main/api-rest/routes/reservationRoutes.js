const express = require('express');
const router = express.Router();
const reservationController = require('../controller/reservationController');
const logsController = require('../controller/logsController.js')
const { verifyToken ,authorizeRoles } = require('../middleware/authMiddleware.js');    
const Config = require('../models/Config');

//Add reservation (emp, admin)
router.post('/add', verifyToken,reservationController.createReservation);

//Delete reservation (emp,admin)
router.delete('/delete/:id', verifyToken,reservationController.deleteReservation);

//Cancelar reserva
router.patch ('/cancel/:id',verifyToken, reservationController.cancelReservation);

//List reservation (emp,admin)
router.get('/',reservationController.listReservations);

// Obtener reservas de un user
router.get('/my-reservations', verifyToken, authorizeRoles(['Usuario']), reservationController.getUserReservations);

// Obtener las reservas de hoy
router.get('/checkins-today', verifyToken,reservationController.listTodayReservations);

// Obtener reserva por ID
router.get('/:id', reservationController.getReservation);

// Check-In
router.patch('/:id/checkin', verifyToken, reservationController.checkIn);

//Check-Out
router.patch('/:id/checkout', verifyToken, reservationController.checkOut);

//Listar Logs
router.get('/:id/audit', verifyToken, reservationController.getReservationAudit);


// Endpoint para consultar el estado actual
router.get('/admin/config/logs', verifyToken,logsController.State);

// Cambiar estado
router.post('/admin/config/logs', verifyToken, logsController.ToggleState );
module.exports = router;