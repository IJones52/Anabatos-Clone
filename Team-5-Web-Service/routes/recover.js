//express is the framework we're going to use to handle requests
const express = require('express')

//Access the connection to Heroku Database
const pool = require('../utilities').pool

const validation = require('../utilities').validation
let isValueProvided = validation.isValueProvided

const generateHash = require('../utilities').generateHash
const generateSalt = require('../utilities').generateSalt

const sendEmail = require('../utilities').sendEmail
const nodemailer = require("nodemailer");
const router = express.Router()


const randString = () => {
    const len = 8
    let randstr = ''
    for (let i =0; i<len; i++){
        const ch = Math.floor((Math.random() * 10) + 1)
        randstr += ch
    }
    return randstr
}

const sendMail = (email, str) => {
    var Transport = nodemailer.createTransport({
        service:"Gmail",
        auth:{
            user: "team5.tcss450@gmail.com",
            pass: "Placeholder1!"
        }
    })

    var mailOptions;
    let sender ="your_name";
    mailOptions = {
        from: sender,
        to: email,
        subject: "Lost Password",
        html: `Here's a temporary password to sort you out: ${str}. Don't forget to change it once you're logged in.`
    }

    Transport.sendMail(mailOptions, function(error, response){
        if (error){
            console.log(error)
        }
        else{
            console.log("Message Sent")
        }
    })

}

/**
 * @api {put} /recover Request to recover a password
 * @apiName PutRecovery
 * @apiGroup Recovery
 * 
 * @apiBody {String} email The email of the user who lost their passwrod
 * 
 * 
 * @apiSuccess {boolean} success true when the email has been sent
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 */ 
router.put("/", (request, response, next) => {
    if (!isValueProvided(request.body.email)) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else {
        next()
    }
}, (request, response) => {
    let salt = generateSalt(32)
    let randPass = randString()
    let salted_hash = generateHash(randPass, salt)
    //Delete the request
    let insert = `UPDATE Members SET Password = $2, Salt = $3  WHERE Email=$1`
    let values = [request.body.email, salted_hash, salt]
    
    pool.query(insert, values)
        .then(result => {
            sendMail(request.body.email, randPass)
            response.send({
                "success": true,
                "tempPass": randPass
            })
        }).catch(err => {
            response.send({
                message: "SQL Error",
                success: false,
                error: err
            })

        })

})



module.exports = router