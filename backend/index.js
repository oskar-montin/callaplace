var mongoose = require('mongoose');
var express = require('express')
var app = express()

mongoose.connect('mongodb://angseus.ninja/test');

var db = mongoose.connection;
db.on('error', console.error.bind(console, 'db: connection error: '));
db.once('open', function() {
  console.log('db: connected!')
});

var Device = mongoose.model('Device', {
  id: String, loc: [Number]
});

app.post('/location', function (req, res) {
  var id = req.params.id;
  var loc = req.params.loc;
  Device.findOneAndUpdate({id}, {loc});
  res.end();
})

app.get('/call', function (req, res, next) {
  var center = req.params.loc;
  Device.findOne().where('loc').near({center}).select('id')
  .then(res.json, next);
})

app.listen(3000);
