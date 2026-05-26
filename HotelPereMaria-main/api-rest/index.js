const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });const dns = require('node:dns');
dns.setDefaultResultOrder('ipv4first');
const express = require ('express');
const mongoose = require('mongoose');
const reservationRoutes = require ('./routes/reservationRoutes');
const usersRoutes = require ('./routes/userRoutes');
const habitacionRoutes = require ('./routes/habitacionRoutes');
const reviewRoutes = require ('./routes/reviewRoutes');
const authRouter  = require('./routes/authRouter');
const invoiceRoutes = require('./routes/invoiceRoutes');
const comRoutes = require('./routes/comRoutes');


const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
//Ver imagenes por url
app.use('/uploads', express.static('uploads'));

app.use('/reservations',reservationRoutes);
app.use('/users',usersRoutes);
app.use('/rooms',habitacionRoutes);
app.use('/auth',authRouter)
app.use('/reviews',reviewRoutes)
app.use('/invoices', invoiceRoutes);
app.use('/communications', comRoutes);


const PORT = process.env.PORT || 3000;
const MONGO_URI = 'mongodb://127.0.0.1:27017/HotelPereMaria';

if (!MONGO_URI) {
  console.error("Falta MONGO_URI en el entorno");
  process.exit(1);
}

mongoose
  .connect(MONGO_URI)
  .then(() => console.log('Conectado a MongoDB Atlas'))
  .catch((err) => {
    console.error('Error MongoDB Atlas', err);
    process.exit(1);
  });

app.listen(PORT,'0.0.0.0', () => {
  console.log(`Servidor escuchando en puerto${PORT}`);
});