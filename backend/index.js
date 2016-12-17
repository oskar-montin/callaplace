var mongoose = require('mongoose');
var express = require('express')
var bodyParser = require('body-parser')
var app = express();

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

app.post('/location', function (req, res, next) {
  Device.findOneAndUpdate({id: req.body.id}, {$set: {
    loc: [req.body.loc.lon, req.body.loc.lat],
    updatedAt: new Date()
  }}, {upsert: true})
  .then(res.end, next);
})

app.get('/call', function (req, res, next) {
  Device.findOne()
  //.ne('id', req.query.exclude)
  .near('loc', {
    center: [req.query.lon, req.query.lat],
    maxDistance: 30.0 / 6371000,
    spherical: true})
  .select('-_id id')
  .then(dev => dev ? res.send(dev.id) : res.status(404).end(), next);
})

app.listen(3000);
