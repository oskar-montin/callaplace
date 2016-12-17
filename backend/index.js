var mongoose = require('mongoose');
var express = require('express');
var bodyParser = require('body-parser');
var app = express();

var FCM = require('fcm-push');
var serverKey = require('./server-key');
var fcm = new FCM(serverKey);

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: true
}));

mongoose.Promise = Promise;
mongoose.connect('mongodb://angseus.ninja/callaplace');

var Device = mongoose.model('Device', {
  updatedAt: {type: Date, expires: 300},
  id: {type: String, index : true},
  token: String, loc: {type: [Number], index:'2d'}
});

app.post('/token', function (req, res, next) {
  Device.findOneAndUpdate({id: req.body.id}, {$set: {
    token: req.body.token
  }}, {upsert: true})
  .then(res.end, next);
})

app.post('/location', function (req, res, next) {
  Device.findOneAndUpdate({id: req.body.id}, {$set: {
    loc: [req.body.loc.lon, req.body.loc.lat],
    updatedAt: new Date()
  }}, {upsert: true})
  .then(res.end, next);
})

app.get('/call', function (req, res, next) {
  Promise.all([
    Device.findOne()
    .where('id', req.query.exclude),
    Device.findOne()
    //.ne('id', req.query.exclude)
    .near('loc', {
      center: [req.query.lon, req.query.lat],
      maxDistance: 30.0 / 6371000,
      spherical: true})])
  .then(pres => {
    call({caller: pres[0], callee: pres[1]}, res, next);
  }, next);
})

function call(req, res, next) {
  if (!req.callee) {
    return res.status(404).end();
  }

  // DEBUG: sleep, then send call
  else if (req.callee.id == req.caller.id) {
    setTimeout(() => {
      fcm.send({
        to: req.callee.token,
        data: {loc: {
          lon: req.caller.loc[0],
          lat: req.caller.loc[1]
        }}
      });
    }, 5000);
    return res.end();
  }

  fcm.send({
    to: req.callee.token,
    data: {loc: {
      lon: req.caller.loc[0],
      lat: req.caller.loc[1]
    }}
  }).then(res.json, next);
}

app.listen(3000);
