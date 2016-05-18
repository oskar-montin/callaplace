var express    = require('express');
var bodyParser = require('body-parser');
var mongoose = require("mongoose");

mongoose.connect('mongodb://angseus.ninja:27017/callaplace');

var app = express();

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var PhoneLocation = require('./models/phonelocation.js');

app.route('/locations')
    .get(function(req, res, next){
        var $near = [req.query.lon, req.query.lat];
        var $maxDistance = (req.query.dist || 20) / 6371; // dist to rad
        PhoneLocation.find({ loc: { $near, $maxDistance }})
        .limit(Number(req.query.limit) || 10)
        .exec().then(res.json.bind(res), next);
    })
    .post(function(req, res, next){
        console.log(req.body);
        var id = req.body.user || mongoose.Types.ObjectId();
        var loc = [req.body.lon, req.body.lat];
        PhoneLocation.findByIdAndUpdate(id,
            { loc },{upsert: true, new: true })
        .exec().then(res.json.bind(res), next);
    });

app.listen(3000);