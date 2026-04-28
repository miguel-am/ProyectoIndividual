const Reservation = require('../models/reservation');
const mongoose = require('mongoose');
const Room = require('../models/rooms'); 
const {userDatabaseModel} = require("../models/user");
const AuditLog = require('../models/auditLog');
const config = require('../config');

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
    const { userId, roomIds, checkIn, checkOut, numGuests } = req.body;
    //Validaciones básicas
    if (!userId || !roomIds || !Array.isArray(roomIds) || roomIds.length === 0) {
      return res.status(400).json({ error: 'Debes seleccionar al menos una habitación' });
    }

    const inDateRaw = parseDate(checkIn);
    const outDateRaw = parseDate(checkOut);

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
      status: { $ne: 'cancelada' },
      roomIds: { $in: roomIds }, 
      $or: [
        {
          checkIn: { $lt: outDate },
          checkOut: { $gt: inDate }
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
      checkIn: inDate,
      checkOut: outDate,
      totalPrice: finalPrice,
      numGuests: numGuests 
    });

    await reservation.save();
    return res.status(201).json(reservation);

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
    const { id } = req.params;
    const actorId = req.user.id; 

    const reservation = await Reservation.findById(id);
    if (!reservation) return res.status(404).json({ error: 'Reserva no encontrada' });

    const oldState = reservation.toObject(); // Guardamos el estado previo 

    const updatedReservation = await Reservation.findByIdAndUpdate(
        id,
        { status: 'cancelada' },
        { new: true }
    );

    // Registro automático en el log 
    await createAuditEntry(
        id, 
        'CANCELACION', 
        actorId, 
        'user',
        oldState, 
        updatedReservation.toObject()
    );

    res.json(updatedReservation);
}

//---*---
async function checkIn(req, res) {
    const { id } = req.params;
    const employeeId = req.user.id; // El trabajador que está usando el WPF [cite: 47]

    try {
        const reservation = await Reservation.findById(id);
        if (!reservation) {
            return res.status(404).json({ error: 'Reserva no encontrada' });
        }

        // Validación: Solo si está confirmada y es la fecha correcta [cite: 43]
        if (reservation.status !== 'confirmada') {
            return res.status(400).json({ error: 'Solo se puede hacer check-in de reservas confirmadas' });
        }

        const oldState = reservation.toObject();

        // Actualizamos con los datos del empleado y nuevo estado [cite: 40, 53]
        const updatedReservation = await Reservation.findByIdAndUpdate(
            id,
            { 
                status: 'inHotel',
                checkin_at: new Date(),
                checkin_by: employeeId 
            },
            { new: true, runValidators: false }
        );

        //REGISTRO DE AUDITORÍA
        await createAuditEntry(
            id,
            'CHECK-IN',
            employeeId,
            'employee', 
            oldState,
            updatedReservation.toObject()
        );

        res.json(updatedReservation);
    } catch (err) {
        res.status(500).json({ error: 'Error al procesar el check-in' });
    }
}

async function checkOut(req, res) {
    const { id } = req.params;
    const employeeId = req.user.id;

    try {
        const reservation = await Reservation.findById(id);
        if (!reservation) return res.status(404).json({ error: 'Reserva no encontrada' });

        // Solo se puede hacer check-out si ya estaban en el hotel
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
            id,
            'CHECK-OUT',
            employeeId,
            'employee',
            oldState,
            updatedReservation.toObject()
        );

        const user = await userDatabaseModel.findById(reservation.userId);
        if (user) {
            // Ejemplo: 1 noche = 10 puntos
            const diffInMs = reservation.checkOut - reservation.checkIn;
            const nights = Math.ceil(diffInMs / (1000 * 60 * 60 * 24));
            user.loyaltyPoints = (user.loyaltyPoints || 0) + (nights * 10);
            await user.save();
        }

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
  
      if (!id || !mongoose.isValidObjectId(id)) {
        return res.status(400).json({ error: 'ID inválido' });
      }
  
      const reservation = await Reservation.findById(id);
  
      if (!reservation) {
        return res.status(404).json({ error: 'Reserva no encontrada' });
      }
  
      await Reservation.findByIdAndDelete(id);
  
      return res.status(200).json({ message: 'Reserva eliminada correctamente' });
    } catch (err) {
      console.error('Error al eliminar reserva:', err);
      return res.status(500).json({ error: 'Error interno del servidor' });
    }
  }

//---*---
async function createAuditEntry(bookingId, action, actorId, actorType, oldState, newState) {
  if (!config.LOGS_ENABLED) return;  
  try {
        const log = new AuditLog({
            booking_id: bookingId,
            action: action,
            actor_id: actorId,
            actor_type: actorType, 
            previous_state: oldState,
            new_state: newState,
            timestamp: new Date()
        });
        await log.save(); //guarda la entrada en la colección booking_audit_log
    } catch (err) {
        console.error("Error guardando el log de auditoría:", err);
    }
}

//---*---
async function getReservationAudit(req, res) {
    try {
        const { id } = req.params;

        //Buscamos los logs asociados a la reserva
        const auditLogs = await AuditLog.find({ booking_id: id })
            .sort({ timestamp: 1 }); //De más antiguo a más reciente 

        if (!auditLogs || auditLogs.length === 0) {
            return res.status(404).json({ message: 'No hay historial para esta reserva.' });
        }

        //El log es solo de lectura
        res.json(auditLogs);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error al obtener el historial.' });
    }
}
  

  module.exports = {
    createReservation,
    listReservations,
    getReservation,
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
  
  
