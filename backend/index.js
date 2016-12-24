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
mongoose.connect(require('./db').url);

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

app.route('/call')
.get((req, res, next) => {
  Promise.all([
    Device.findOne()
    .where('id', req.query.exclude)
    .exec(),
    Device.find()
    //.ne('id', req.query.exclude)
    .near('loc', {
      center: [req.query.lon, req.query.lat],
      maxDistance: 30.0 / 6371000,
      spherical: true})
    .exec()])
  .then(pres => {
    call({caller: pres[0], callee: pres[1][0]}, res, next);
  }, next);

})
.delete((req, res, next) => {
  Device.findOne()
  .where('id', req.get('caller'))
  .then(caller => {
    console.log(req.get('caller'), caller.token)
    fcm.send({to: caller.token, notification: {
      title: 'Dissad'
    }})
    .then(res.end.bind(res), next);
  }, next);
});

function call(req, res, next) {
  if (!req.callee) {
    return res.status(404).end();
  }
  if (!req.caller) {
    //return res.status(404).end();
    // todo: do this?
    req.caller = {id: 'unknown'};
  }

  // DEBUG: sleep, then send call
  if (req.callee.id == req.caller.id) {
    var data = {caller: req.caller.id};
    setTimeout(() => fcm.send({to: req.caller.token, data}), 5000);
    return res.end();
  }
  var data = {caller: req.caller.id};
  if (req.caller.loc) {
    data.loc = {
      lon: req.caller.loc[0],
      lat: req.caller.loc[1]
    };
  }
  fcm.send({to: req.callee.token, data})
  .then(res.end.bind(res), next);
}

function end(req, res, next) {

}

app.listen(3000);
