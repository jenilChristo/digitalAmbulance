var express = require('express');
var router = express.Router();
var config = require('../config');

isAuthenticated = function(req,res,next){
    if(req.session.userType != null && req.session.userType != undefined ){
        next();
    }
    else{
        res.json("You are not authorized to view this page!");
    }
}

/* GET home page. */
router.get('/', function(req, res, next) {
    res.render('index',{uid:null,url:config.url,userType:null});
    res.end();
    });
    
router.post('/get-nearest-hospital',function(req,res){
    req.body  = JSON.parse(req.body);
    console.log(req.body)
    if(!req.body.latitude || !req.body.longitude){
        res.json({status:200,err:"Required fields missing"});
        res.end();
    }
        try{
            var rand = Math.random().toString(36).substring(7);
            var base64Data = req.body.accident_image.replace(/^data:image\/png;base64,/, "");
            require("fs").writeFile(process.cwd()+"/./public/images/accidents/"+rand+".png", base64Data, 'base64', function(err) {
              console.log(err);
            });
           var nearestHospitalQuery = {
                "hospital_location": {
                  $near: {
                    $geometry: {
                       type: "Point" ,
                       coordinates: [Number(req.body.longitude) , Number(req.body.latitude) ]
                    },
                    $maxDistance: 15000000,
                    $minDistance: 100
                  }
                }
             };
        config.mongoConn().then(function(db){
                db.collection("hospitals").find(nearestHospitalQuery).toArray().then(function(data){
                console.log(data);
                return data;
             }).then(function(fetchedData){
                 if(fetchedData.length > 0){
                    var emergencyDetails = {
                        "injury" : req.body.injury_type,
                        "location" : {
                            "latitude" : req.body.latitude,
                            "longitude" : req.body.longitude
                        },
                        "informer" : req.body.informer,
                        "accident_location":req.body.accident_location,
                        "picture" : "/images/accidents/"+rand+".png",
                        "hospital_informed" : fetchedData[0]._id.toString()
                        };
                    db.collection('emergencies').save(emergencyDetails).then(function(results){
                    io.sockets.emit('emergency',{hospital_id:fetchedData[0]._id,emergencyData:emergencyDetails});
                    res.json({"status":200,
                    "message":"Emergency Alert Sent to "+fetchedData[0].hospital_name+" ,you will get a call when they arrive",
                    "hospital_location":{"latitude":Number(fetchedData[0].hospital_location.coordinates[1]),"longitude":Number(fetchedData[0].hospital_location.coordinates[0]) },
                    "hospital_name":fetchedData[0].hospital_name,
                    "hospital_contact":fetchedData[0].hospital_contact,
                    "emergencies_served":"10",
                    "doctors_message":fetchedData[0].doctors_message
                    });
                    }).catch(function(err){
                        console.log(err);
                    }); 
                 }else{
                    res.json({status:208,error:"No nearby hospitals use Digital Ambulance"}); 
                 }

                }).catch(function(err){ 
                console.log(err);
              });     
            });      
    }
    catch(e){
          console.log(e);
          res.end(e);
    }
});
router.post('/check',function(req,res){
console.log(req.body);
res.end();
});
/* POST Login page. */
router.post('/authenticate', function(req, res, next) {
    if(req.body.email === "admin" && req.body.password === "qwertyu123"){
        req.session.userType = 'admin';
        req.session.userName = 'Admin';
        req.session.uid = 1;    
        res.redirect('/home');
    }
    else{
        try{
        config.mongoConn().then(function(db){
            db.collection("hospitals").find({"user_name":req.body.email,"password":req.body.password}).toArray().then(function(data){
                if(data.length > 0){ 
                    req.session.userType = 'hospital';
                    req.session.uid = data[0]._id;
                    req.session.userName = data[0].user_name;
                    res.redirect('/home')
                }
                else{
                    res.end("Oops ! Wrong Credentials");
                }
            }).catch(function(err){
                console.log(err);
            }); 
        }).catch(function(err){
            console.log(err);
        });            

        }
        catch(e){
            console.log(e)
        }

      }
});

router.get('/home',isAuthenticated,function(req,res){
    try{
            config.mongoConn().then(function(db){
                var query = (req.session.userType == 'hospital' && req.session.userType!=undefined ) ? {"hospital_informed":req.session.uid}:{};
                db.collection("emergencies").find(query).toArray().then(function(data){
                    res.render('home',{uid:req.session.uid,url:config.url,cases:data,userType:req.session.userType,userName:req.session.userName});
                    res.end();
                }).catch(function(err){
                    console.log(err);
                }); 
            }).catch(function(err){
                console.log(err);
            });           
    
    }
    catch(e){
          console.log(e);
          res.end(e);
    }
});
router.get('/all-hospitals',isAuthenticated,function(req,res){
    if(req.session.userType != 'hospital'){
        try{
            config.mongoConn().then(function(db){
                db.collection("hospitals").find({}).toArray().then(function(data){
                    console.log(data)
                    res.render('all-hospitals',{uid:req.session.uid,url:config.url,hospitals:data,userType:req.session.userType,userName:req.session.userName});
                    res.end();
                }).catch(function(err){
                    console.log(err);
                }); 
            }).catch(function(err){
                console.log(err);
            });           
    
    }
    catch(e){
          console.log(e);
          res.end(e);
    }
    }
    else{
        res.end("You are not authorized to view this page");
    }

});
router.get('/logout',function(req,res){
   req.session.destroy();
   res.redirect('/');
   res.send(401);
});
router.get('/new-hospital',isAuthenticated,function(req,res){
    if(req.session.userType !='hospital'){
        
        res.render('new-hospital',{uid:req.session.uid,url:config.url,userName:req.session.userName,userType:req.session.userType,code:req.query.status});
        res.end();
    }else{
        res.end("You are not authorized to view this page");
    }
 });
 router.post('/new-hospital',isAuthenticated,function(req,res){
    if(req.session.userType !='hospital'){
        try{

            var model ={
                "user_name" : req.body.user_name,
                "password" : req.body.password,
                "type" : "h",
                "message" : req.body.message,
                "hospital_location" : {
                    "type" : "Point",
                    "coordinates" : [ 
                        req.body.longitude, 
                        req.body.latitude
                    ]
                },
                "hospital_name" : req.body.hospital_name,
                "hospital_contact" : req.body.contact,
                "emergencies_served" : "212",
                "doctors_message" : req.body.doctor_message
            };
            config.mongoConn().then(function(db){
                db.collection("hospitals").save(model).then(function(data){
                    if(data.result.ok > 0){
                        res.redirect('/new-hospital?status=200');
                    }else{
                        res.redirect('/new-hospital?status=208');
                    }
                    
                }).catch(function(err){
                    console.log(err);
                }); 
            }).catch(function(err){
                console.log(err);
            });           
    
    }
    catch(e){
          console.log(e);
          res.end(e);
    }
    }else{
        res.end("You are not authorized to do this activity");
    }
 });
module.exports = router;
