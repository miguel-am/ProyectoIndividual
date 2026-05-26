const mongoose = require('mongoose');

const reservationSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    roomIds: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Room', required: true }],
    In: { type: Date, required: true },
    Out: { type: Date, required: true },
    totalPrice: { type: Number, required: true },
    numGuests: { type: Number, required: true, min: 1 },
    status: {
      type: String,
      enum: ['confirmada', 'terminada', 'cancelada', 'inHotel'],
      default: 'confirmada'
    },
    checkin_at: { type: Date },
    checkin_by: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    checkout_at: { type: Date },
    checkout_by: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    invoice_number: { type: String, unique: true, sparse: true } // Se genera al salir
  });

const Reservation = mongoose.model('Reservations',reservationSchema);
module.exports = Reservation;
