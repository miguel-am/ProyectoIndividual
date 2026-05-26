const express = require('express');
const router = express.Router();
const invoiceController = require('../controller/invoiceController');
const { verifyToken } = require('../middleware/authMiddleware'); // Tu middleware de seguridad


//Obtener todas las facturas
router.get('/', invoiceController.getAllInvoices);

//Reenviar por email
router.post('/:id/resend-email', verifyToken,invoiceController.resendInvoiceEmail);
//Ruta para descargar la factura por ID de reserva
router.get('/:id/download', verifyToken, invoiceController.generateInvoicePDF);

module.exports = router;