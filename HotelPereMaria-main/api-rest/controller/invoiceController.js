const PDFDocument = require('pdfkit');
const Reservation = require('../models/reservation');
const nodemailer = require('nodemailer');
const { userDatabaseModel } = require('../models/user');
const Room = require('../models/rooms');
const Communication = require('../models/communication'); 
const comController = require('./communicationController');
const path = require('path');

let transporter;

function getTransporter() {
    if (!transporter) {
        console.log("Configurando transporter con:", process.env.EMAIL_USER);
        transporter = nodemailer.createTransport({
            host: 'smtp.gmail.com',
            port: 587,
            secure: false,
            auth: {
                user: process.env.EMAIL_USER,
                pass: process.env.EMAIL_PASS
            },
            tls: { rejectUnauthorized: false }
        });
    }
    return transporter;
}

async function generateInvoicePDF(req, res) {
    try {
        const { id } = req.params;
        
        const reservation = await Reservation.findById(id)
            .populate({ path: 'userId', model: 'user' })
            .populate({ path: 'roomIds', model: 'Room' });

        if (!reservation) return res.status(404).json({ error: 'Reserva no encontrada' });

        // Inicializamos PDFKit con tamaño A4 estándar
        const doc = new PDFDocument({ margin: 50, size: 'A4' });
        let buffers = [];
        
        doc.on('data', b => buffers.push(b));
        

        //ENCABEZADO
        const logoPath = path.join(__dirname, '../uploads/logo.png'); 
        try {
            doc.image(logoPath, 50, 45, { width: 65 });
        } catch (e) {
            doc.fillColor('#1E3A8A').fontSize(18).text("HOTEL", 50, 50, { bold: true });
        }

        doc.fillColor('#334155').fontSize(11).text("HOTEL JAVOTOROS", 200, 50, { align: 'right', bold: true });
        doc.fontSize(9);
        doc.text("Calle Mayor, 12 - Benidorm", 200, 65, { align: 'right' });
        doc.text("CIF: B99112233 | Tlf: +34 965 000 000", 200, 80, { align: 'right' });
        doc.text("info@hotelperemaria.com", 200, 95, { align: 'right' });

        doc.moveDown(2);
        doc.strokeColor('#E2E8F0').lineWidth(1).moveTo(50, 120).lineTo(545, 120).stroke();

        //BLOQUES DE DATOS
        doc.moveDown(1.5);
        const startY = doc.y;

        doc.fillColor('#1E3A8A').fontSize(12).text("DATOS DE FACTURACIÓN", 50, startY);
        doc.fillColor('#000000').fontSize(10);
        doc.text(`Factura Nº: `, 50, startY + 20, { continued: true }).font('Helvetica-Bold').text(`${reservation.invoice_number || 'Provisional'}`).font('Helvetica');
        doc.text(`Fecha Emisión: ${new Date().toLocaleDateString('es-ES')}`, 50, startY + 35);
        
        const esVip = reservation.userId && (reservation.userId.vipStatus === true || reservation.userId.isVip === true);
        if (esVip) {
            doc.text(`Tarifa aplicada: `, 50, startY + 50, { continued: true }).fillColor('#1E3A8A').text("PROMO WEB VIP (Descuento 20%)", { bold: true }).fillColor('#000000');
        } else {
            doc.text(`Estado: `, 50, startY + 50, { continued: true }).fillColor('#10B981').text("PAGADO", { bold: true }).fillColor('#000000');
        }

        doc.fillColor('#1E3A8A').fontSize(12).text("CLIENTE / HUÉSPED", 320, startY);
        doc.fillColor('#000000').fontSize(10);
        doc.text(`${reservation.userId.firstName || ''} ${reservation.userId.lastName || ''}`, 320, startY + 20, { bold: true });
        doc.text(`Identificación: ${reservation.userId.dni || 'N/A'}`, 320, startY + 35);
        doc.text(`Email: ${reservation.userId.email || 'N/A'}`, 320, startY + 50);

        doc.strokeColor('#E2E8F0').lineWidth(1).moveTo(50, startY + 75).lineTo(545, startY + 75).stroke()

        //DETALLES DE LA ESTANCIA Y CÁLCULO DE NOCHES
        doc.moveDown(2.5);
        const estanciaY = doc.y;
        doc.fillColor('#1E3A8A').fontSize(12).text("DETALLES DE LA ESTANCIA", 50, estanciaY);
        doc.fillColor('#000000').fontSize(10);
        
        const dIn = new Date(reservation.In);
        const dOut = new Date(reservation.Out);
        const date1 = Date.UTC(dIn.getFullYear(), dIn.getMonth(), dIn.getDate());
        const date2 = Date.UTC(dOut.getFullYear(), dOut.getMonth(), dOut.getDate());
        const msPerDay = 1000 * 60 * 60 * 24;
        const nights = Math.max(1, Math.floor((date2 - date1) / msPerDay)); 

        doc.text(`Check-in: ${dIn.toLocaleDateString('es-ES')}`, 50, estanciaY + 20);
        doc.text(`Check-out: ${dOut.toLocaleDateString('es-ES')}`, 50, estanciaY + 35);
        doc.text(`Noches calculadas: ${nights}`, 50, estanciaY + 50);
        doc.text(`Huéspedes: ${reservation.numGuests || 1}`, 320, estanciaY + 20);

        //TABLA DE CONCEPTOS REGLADA
        let tableTop = estanciaY + 80;
        doc.rect(50, tableTop, 495, 22).fill('#F1F5F9');
        doc.fillColor('#1E3A8A').fontSize(10);
        doc.text("Concepto / Habitación asignada", 60, tableTop + 6, { bold: true });
        doc.text("Noches", 250, tableTop + 6, { bold: true });
        doc.text("Precio/Noche", 350, tableTop + 6, { align: 'right', bold: true });

        let currentY = tableTop + 22;
        doc.fillColor('#000000');

        let acumuladoHabitacionesVisibles = 0;

        if (reservation.roomIds && reservation.roomIds.length > 0) {
            reservation.roomIds.forEach((room, index) => {
                if (index % 2 === 0) {
                    doc.rect(50, currentY, 495, 20).fill('#F8FAFC');
                    doc.fillColor('#000000');
                }

                let precioBaseHabitacion = room.pricePerNight || 0; 
                
                if (esVip && precioBaseHabitacion === 85) {
                    precioBaseHabitacion = 68;
                }
                
                const subtotalCalculado = precioBaseHabitacion * nights;
                acumuladoHabitacionesVisibles += subtotalCalculado;

                const numHab = room.roomNumber || room.numRoom || (index + 1);

                doc.text(`Habitación número ${numHab}`, 60, currentY + 5);
                doc.text(`${nights}`, 250, currentY + 5);
                doc.text(`${precioBaseHabitacion} €`, 350, currentY + 5, { align: 'right' });
                currentY += 20;
            });
        }

        doc.strokeColor('#CBD5E1').lineWidth(1).moveTo(50, currentY).lineTo(545, currentY).stroke();

        //TOTALES FINALES
        currentY += 15;
        
        const total = reservation.totalPrice || acumuladoHabitacionesVisibles;
        const baseImponible = (total / 1.10).toFixed(2);
        const ivaSoportado = (total - baseImponible).toFixed(2);

        doc.fontSize(10).fillColor('#475569');
        doc.text("Base Imponible (10% Excl.):", 300, currentY, { align: 'right', width: 140 });
        doc.text(`${baseImponible} €`, 450, currentY, { align: 'right', width: 95 });

        currentY += 15;
        doc.text("I.V.A. Soportado (10%):", 300, currentY, { align: 'right', width: 140 });
        doc.text(`${ivaSoportado} €`, 450, currentY, { align: 'right', width: 95 });

        currentY += 20;
        doc.rect(320, currentY, 225, 28).fill('#1E3A8A');
        doc.fillColor('#FFFFFF').fontSize(12);
        doc.text("TOTAL FACTURA:", 330, currentY + 8, { bold: true });
        doc.text(`${total.toFixed(2)} €`, 440, currentY + 8, { align: 'right', bold: true, width: 100 });

        //PIE DE PÁGINA
        doc.fillColor('#94A3B8').fontSize(8);
        doc.text("Gracias por su confianza. Esperamos verle pronto de vuelta.", 50, 740, { align: 'center', width: 495 });

        doc.once('end', async () => {
            try {
                const pdfData = Buffer.concat(buffers);

                res.setHeader('Content-Type', 'application/pdf');
                res.setHeader('Content-Disposition', `attachment; filename=Factura-${id}.pdf`);
                res.send(pdfData);

                const mailOptions = {
                    from: `"Hotel Javotoros" <${process.env.EMAIL_USER}>`,
                    to: reservation.userId.email,
                    subject: `Factura de tu estancia - Hotel Javotoros`,
                    text: `Estimado/a ${reservation.userId.firstName || 'Cliente'},\n\nAdjuntamos la factura correspondiente a tu estancia.\n\nGracias por confiar en nosotros.`,
                    attachments: [{
                        filename: `Factura-${reservation.invoice_number || 'Factura'}.pdf`,
                        content: pdfData
                    }]
                };

                await getTransporter().sendMail(mailOptions);
                
                if (comController && typeof comController.createCom === 'function') {
                    const actorName = req.user ? req.user.name : "Sistema";
                    await comController.createCom(id, 'email', `Factura descargada y enviada por email automáticamente.`, actorName);
                }

            } catch (error) {
                console.error("Error al procesar el envío simultáneo:", error);
            }
        });
        
        doc.end();

    } catch (error) {
        console.error("Error general:", error);
        if (!res.headersSent) {
            res.status(500).json({ error: "Fallo interno en el servidor" });
        }
    }
}

async function getAllInvoices(req, res) {
    try {
        const facturas = await Reservation.find({ status: 'terminada' })
            .populate({
                path: 'userId',
                model: 'user',
                select: 'firstName lastName email dni vipStatus isVip'
            })
            .populate({
                path: 'roomIds', 
                model: 'Room',
                select: 'roomNumber numRoom pricePerNight' 
            });

        const respuestaFormateada = facturas.map(f => {
            const habitacionesString = f.roomIds && f.roomIds.length > 0 
                ? f.roomIds.map(r => r.roomNumber || r.numRoom).join(', ') 
                : "S/N";

            return {
                _id: f._id,
                invoice_number: f.invoice_number,
                status: f.status,
                totalPrice: f.totalPrice,
                Out: f.Out,
                userId: f.userId, 
                roomNumber: habitacionesString 
            };
        });

        res.json(respuestaFormateada);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Error al obtener listado" });
    }
}

async function resendInvoiceEmail(req, res) {
    try {
        const { id } = req.params;
        const reservation = await Reservation.findById(id).populate({ path: 'userId', model: 'user' });
        if (!reservation) return res.status(404).json({ error: "Reserva no encontrada" });

        req.params.id = id;
        return generateInvoicePDF(req, res);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
}

async function sendWelcomeEmailInternal(clienteEmail, clienteNombre, reservaId) {
    try {
        const mailOptions = {
            from: process.env.EMAIL_USER,
            to: clienteEmail,
            subject: '¡Reserva Confirmada!',
            html: `<p>Tu reserva con código ${reservaId} ha sido registrada.</p>`
        };
        await getTransporter().sendMail(mailOptions);
        return true;
    } catch (error) {
        return false;
    }
}

module.exports = {
    generateInvoicePDF,
    getAllInvoices,
    resendInvoiceEmail,
    sendWelcomeEmailInternal
};