const Reservation = require('../models/reservation');
const mongoose = require('mongoose');
const Room = require('../models/rooms'); 
const {userDatabaseModel} = require("../models/user");
const AuditLog = require('../models/auditLog');
const Config = require('../models/Config');
const Communication = require('../models/communication');
const invoiceController = require('./invoiceController');
const comController = require('./communicationController');
const { sendPushNotification } = require('../services/notificationService');


function parseDate(value) {
    const d = new Date(value);
    return isNaN(d.getTime()) ? null : d;
}

function startOfHotelDay(date) {
    const d = new Date(date);
    d.setHours(12, 0, 0, 0); // 12:00 del día hotelero
    return d;
}

async function createReservation(req, res) {
  try {
    console.log(req.body)
    if (!req.user || !req.user.id) {
            return res.status(401).json({ error: 'Usuario no autenticado o token inválido' });
        }
    const actorId = req.user.id; 

    //Validaciones básicas
    const { userId, roomIds, In, Out, numGuests } = req.body;
    if (!userId || !roomIds || !Array.isArray(roomIds) || roomIds.length === 0) {
      return res.status(400).json({ error: 'Debes seleccionar al menos una habitación' });
    }

    const inDateRaw = parseDate(In);
    const outDateRaw = parseDate(Out);

    if (!inDateRaw || !outDateRaw) {
      return res.status(400).json({ error: 'Fechas inválidas' });
    }

    const inDate = startOfHotelDay(inDateRaw);
    const outDate = startOfHotelDay(outDateRaw);

    if (!inDate || !outDate || inDate >= outDate) {
      return res.status(400).json({ error: 'Fechas inválidas' });
    }

    const today = startOfHotelDay(new Date());

    if (inDate < today) {
      return res.status(400).json({ error: 'La fecha de entrada no puede ser anterior a hoy.' });
    }

    if (outDate <= today) {
      return res.status(400).json({ error: 'La fecha de salida debe ser posterior a hoy.' });
    }

    //Buscamos colisiones
    const overlap = await Reservation.findOne({
      status: { $nin: ['cancelada', 'terminada'] },
      roomIds: { $in: roomIds }, 
      $or: [
        {
          In: { $lt: outDate },
          Out: { $gt: inDate }
        }
      ]
    });

    if (overlap) {
      return res.status(409).json({
        error: 'Una o más habitaciones no están disponibles en estas fechas.'
      });
    }

    //---Cálculo del Precio---
    // Obtenemos los documentos de las habitaciones para traer sus precios
    const roomsFound = await Room.find({ _id: { $in: roomIds } });
    
    if (roomsFound.length !== roomIds.length) {
      return res.status(404).json({ error: 'Una o más habitaciones no existen.' });
    }

    // Calcular número de noches
    const diffInMs = outDate.getTime() - inDate.getTime();
    const nights = Math.ceil(diffInMs / (1000 * 60 * 60 * 24));

    // Sumar el precio por noche de todas las habitaciones seleccionadas
    const pricePerNightTotal = roomsFound.reduce((total, room) => total + (room.pricePerNight || 0), 0);
    let finalPrice = pricePerNightTotal * nights;

    const user = await userDatabaseModel.findById(userId);
    
    // 3. SI EL USUARIO ES VIP, APLICAMOS EL 20% DE DESCUENTO
    if (user && user.vipStatus === true) {
      finalPrice = finalPrice * 0.80; 
    }

    // 3. Crear la reserva (añadiendo totalPrice)
    const reservation = new Reservation({
      userId,
      roomIds, 
      In: inDate,
      Out: outDate,
      totalPrice: finalPrice,
      numGuests: numGuests 
    });

    const newRes = await reservation.save();
    try {
            await createAuditEntry(
                reservation._id, 
                'CREACION', 
                actorId, 
                null,
                reservation.toObject()
            );
        } catch (logError) {
            console.error('Error al crear entrada de auditoría:', logError);
        }

try {
        // Buscamos al usuario para tener su email y nombre
        const user = await userDatabaseModel.findById(newRes.userId);
        if (user && user.email) {
            // Llamamos al método "chapuza" (pero efectivo) del invoiceController
            await invoiceController.sendWelcomeEmailInternal(
                user.email, 
                user.firstName, 
                newRes._id
            );

            if (typeof comController.createCom === 'function') {
                await comController.createCom(
                    newRes._id, 
                    'email', 
                    'Email automático: Confirmación de reserva y bienvenida enviada.'
                );
            }
        }
    } catch (mailErr) {
        console.error("No se pudo enviar el correo, pero la reserva es válida:", mailErr);
    }    return res.status(201).json(reservation);

  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: 'Error interno' });
  }
}


  async function listReservations(req, res) {
    const reservations = await Reservation
      .find()
      .sort({ checkIn: 1 });
  
    res.json(reservations);
  }
  
  async function getReservation(req, res) {
    const { id } = req.params;
  
    const reservation = await Reservation.findById(id);
    if (!reservation) {
      return res.status(404).json({ error: 'Reserva no encontrada' });
    }
  
    res.json(reservation);
  }

  // reservationController.js

async function listTodayReservations(req, res) {
    try {
        const start = new Date();
        start.setHours(0, 0, 0, 0);
        
        const end = new Date();
        end.setHours(23, 59, 59, 999);

        const reservations = await Reservation.find({
            status: { $in: ['confirmada', 'inHotel'] },
            
            $or: [
                { In: { $gte: start, $lte: end } },
                { Out: { $gte: start, $lte: end } }
            ]
        }).populate({
                path: 'userId',
                model: 'user', 
                select: 'firstName lastName email dni' 
            })
            .populate({ path: 'roomIds', 
              model: 'Room', select: 'numRoom' });

       const formattedResponse = reservations.map(r => {
            const resObj = r.toObject();
            return {
                _id: resObj._id,
                status: resObj.status,
                totalPrice: resObj.totalPrice,
                numRoom: resObj.roomIds && resObj.roomIds.length > 0 
                    ? resObj.roomIds.map(room => room.numRoom).join(', ') 
                    : "N/A",
                userId: resObj.userId 
            };
        });

        console.log(`Filtrando entre: ${start.toISOString()} y ${end.toISOString()}`);
        console.log(`Encontradas: ${reservations.length}`);

        res.json(formattedResponse);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
}

  async function getUserReservations(req, res) {
    try {
        
        const userId = req.user.id; 
        const myReservations = await Reservation.find({ userId: userId })
                                                .populate('roomIds');
        res.status(200).json(myReservations);
    } catch (err) {
        res.status(500).json({ error: "Error al obtener tus reservas" });
    }
}

//---*---
async function cancelReservation(req, res) {
    try {
        const { id } = req.params;
        // Si req.user no existe, es que el token no se validó correctamente
        if (!req.user || !req.user.id) {
            return res.status(401).json({ error: 'Usuario no autenticado o token inválido' });
        }
        const actorId = req.user.id; 

        const reservation = await Reservation.findById(id);
        if (!reservation) return res.status(404).json({ error: 'Reserva no encontrada' });

        const oldState = reservation.toObject(); 

        const updatedReservation = await Reservation.findByIdAndUpdate(
            id,
            { status: 'cancelada' },
            { new: true }
        );

        try {
            await createAuditEntry(
                reservation._id, 
                'CANCELACION', 
                actorId, 
                oldState,
                updatedReservation.toObject()
            );
        } catch (logError) {
            console.error('Error al crear entrada de auditoría:', logError);
        }
        await comController.createCom(id, 'phone', 'Llamada registrada: El cliente cancela por motivos personales', req.user.firstName)
        res.json(updatedReservation);

    } catch (error) {
        console.error('Error general en cancelReservation:', error);
        res.status(500).json({ error: 'Error interno del servidor' });
    }
}

//---*---
// reservationController.js

async function checkIn(req, res) {
    try {
        const { id } = req.params;
        const employeeId = req.user.id; // Obtenemos el ID del empleado del token

        // Buscamos la reserva
        const reservation = await Reservation.findById(id).populate({
            path:'userId',
            model: 'user'});

        if (!reservation) {
            return res.status(404).json({ error: 'Reserva no encontrada' });
        }

        // Guardamos el estado anterior para el Log de auditoría
        const oldState = reservation.toObject();

        reservation.status = 'inHotel';
        reservation.checkin_at = new Date();    
        reservation.checkin_by = employeeId;   

        const updatedReservation = await reservation.save();

        if (reservation.userId && reservation.userId.fcmToken) {
                try {
                await sendPushNotification(
                    reservation.userId.fcmToken,
                    "¡Bienvenido!",
                    "Tu habitación está lista. ¡Disfruta de tu estancia!"
                );
            } catch (pushErr) {
                console.error("Error en el envío satelital de Firebase:", pushErr.message);
            }
        } else {
            console.log("El usuario no tiene fcmToken en la base de datos.");
        }


        // Esto hará que funcionen tus logs de "quién hizo qué"
        if (typeof createAuditEntry === 'function') {
            await createAuditEntry(
                reservation._id,
                'CHECK-IN',
                employeeId,
                oldState,
                updatedReservation.toObject()
            );
        }
        
        await comController.createCom(id, 'push', 'Push enviado: ¡Bienvenido al hotel!')
        return res.status(200).json({ 
            message: 'Check-in realizado correctamente', 
            status: updatedReservation.status,
            checkin_at: updatedReservation.checkin_at
        });

    } catch (err) {
        console.error("ERROR CRÍTICO EN CHECKIN:", err);
        return res.status(500).json({ error: err.message });
    }
}

async function checkOut(req, res) {
    const { id } = req.params;
    const employeeId = req.user.id;

    try {
        const reservation = await Reservation.findById(id).populate({
            path:'userId',
            model: 'user'});
        if (!reservation) return res.status(404).json({ error: 'Reserva no encontrada' });

        const oldState = reservation.toObject();

        const timestamp = Date.now().toString().slice(-6);
        const generatedInvoiceNum = `FAC-${new Date().getFullYear()}-${timestamp}`;

        const updatedReservation = await Reservation.findByIdAndUpdate(
            id,
            { 
                status: 'terminada', 
                checkout_at: new Date(),
                checkout_by: employeeId,
                invoice_number: generatedInvoiceNum
            },
            { new: true, runValidators: false }
            
        );

        //REGISTRO DE AUDITORÍA
        await createAuditEntry(
            reservation._id,
            'CHECK-OUT',
            employeeId,
            oldState,
            updatedReservation.toObject()
        );

        const user = await userDatabaseModel.findById(reservation.userId);
        if (user) {
            // Ejemplo: 1 noche = 10 puntos
            const diffInMs = reservation.Out - reservation.In;
            const nights = Math.ceil(diffInMs / (1000 * 60 * 60 * 24));
            user.loyaltyPoints = (user.loyaltyPoints || 0) + (nights * 10);
            await user.save();
        }

        if (reservation.userId && reservation.userId.fcmToken) {
            try {
                await sendPushNotification(
                    reservation.userId.fcmToken,
                    "Gracias por tu visita",
                    "Nos encantaría saber tu opinión. ¡Haz clic aquí para valorar tu estancia!"
                );
            } catch (pushErr) {
                console.error("Error al enviar push en checkOut:", pushErr.message);
            }
        }

        await comController.createCom(id, 'push', 'Check-Out realizado. Notificación enviada al cliente.', req.user.name);
        res.json({
            message: "Check-out completado y factura generada",
            reservation: updatedReservation
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error en el proceso de salida' });
    }
}

  async function deleteReservation(req, res) {
    try {
      const { id } = req.params;
      const actorId = (req.user && req.user.id) ? req.user.id : null; 
  
      if (!id || !mongoose.isValidObjectId(id)) {
        return res.status(400).json({ error: 'ID inválido' });
      }

      if (!actorId) {
        return res.status(401).json({ error: "No se pudo identificar al autor de la eliminación" });
      }
  
      const reservation = await Reservation.findById(id);
  
      if (!reservation) {
        return res.status(404).json({ error: 'Reserva no encontrada' });
      }
  
      await Reservation.findByIdAndDelete(id);
      try {
            await createAuditEntry(
                reservation._id, 
                'ELIMINACION', 
                req.user.id, 
                reservation.toObject(),
                null
            );
        } catch (logError) {
            console.error('Error al crear entrada de auditoría:', logError);
        }
  
      return res.status(200).json({ message: 'Reserva eliminada correctamente' });
    } catch (err) {
      console.error('Error al eliminar reserva:', err);
      return res.status(500).json({ error: 'Error interno del servidor' });
    }
  }


  
//=============================================//
//================AUDITORIAS===================//
//=============================================// 
async function createAuditEntry(bookingId, action, actorId, oldState, newState) {
  try {

    const config = await Config.findOne({ key: 'global_config' });
        if (!config || !config.logsEnabled) return; // Si están desactivados, salimos
        
        const log = new AuditLog({
            booking_id: bookingId,
            action: action,
            actor_id: actorId,
            previous_state: oldState,
            new_state: newState,
            timestamp: new Date()
        });
        await log.save(); 
    } catch (err) {
        console.error("Error guardando el log de auditoría:", err);
    }
}

//---*---
async function getReservationAudit(req, res) {
    try {
        const { id } = req.params;
        const query = (id === 'all') ? {} : { booking_id: id };

        // Buscamos los logs y usamos 'populate' para traer los datos del usuario/empleado
        const auditLogs = await AuditLog.find(query)
            .populate('actor_id', 'firstName lastName')
            .populate({
                path: 'booking_id', 
                populate: [
                    { path: 'roomIds', select: 'roomNumber', model: 'Room' }, 
                    { path: 'userId', select: 'firstName lastName', model: 'user'} 
                ]
            }) 
            .sort({ timestamp: -1 });

       
        if (!auditLogs || auditLogs.length === 0) {
            return res.status(404).json({ message: 'No hay historial.' });
        }
        res.json(auditLogs);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error al obtener el historial.' });
    }
}

async function completeReservation(req, res) {
    try {
        const { id } = req.params;
        const reservation = await Reservation.findById(id);

        if (!reservation.invoice_number) {
            const count = await Reservation.countDocuments({ invoice_number: { $exists: true } });
            reservation.invoice_number = `FAC-${new Date().getFullYear()}-${(count + 1).toString().padStart(3, '0')}`;
            reservation.status = 'terminada';
            await reservation.save();
        }

        res.json({ message: "Factura generada", number: reservation.invoice_number });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
}





  module.exports = {
    createReservation,
    listReservations,
    getReservation,
    listTodayReservations,
    getUserReservations,
    cancelReservation,
    checkIn,
    checkOut,
    deleteReservation,
    parseDate,
    startOfHotelDay,
    createAuditEntry,
    getReservationAudit
  };
  
  
