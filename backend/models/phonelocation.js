var mongoose = require('mongoose');

var PhoneLocation = new mongoose.Schema({
    loc: { type: [Number], index: '2d'} // [<lon>, <lat>]
});

module.exports = mongoose.model('PhoneLocation', PhoneLocation);