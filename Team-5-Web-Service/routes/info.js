const express = require('express')
const pool = require('../utilities').pool
const validation = require('../utilities').validation
let isStringProvided = validation.isStringProvided
let isValueProvided = validation.isValueProvided
const generateHash = require('../utilities').generateHash
const generateSalt = require('../utilities').generateSalt
const nodemailer = require("nodemailer");
const router = express.Router()

/**
 * @api {get} /info Get User id based on email
 * @apiName GetInfo
 * @apiGroup Info
 * 
 * @apiHeader {Number} query the user's search query
 * 
 * 
 * @apiSuccess (Success 201) {boolean} success true when the id is returned
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 *@apiHeaderExample {json} Request-Example:
 *  {"email": "test@test.com"}
 * 
 * @apiUse JSONError
 */ 
router.get("/", (request, response, next) => {
    if (!isValueProvided(request.headers.email)) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else {
        next()
    }
}, (request, response) => {

    let insert = `SELECT MemberID FROM members WHERE Email = $1`
    let values = [request.headers.email]
    pool.query(insert, values)
        .then(result => {
            response.send({
                success: true,
                rows: result.rows,
                rowCount: result.rowCount
            })
        }).catch(err => {
            response.send({
                message: "SQL Error",
                response: "No user with that email",
                rows: {},
                error: err
            })

        })
})
/**
 * @api {put} /info Change user password
 * @apiName PutInfo
 * @apiGroup Info
 * 
 * @apiBody {String} first a users first name
 * @apiBody {String} last a users last name
 * @apiBody {String} email a users email *unique
 * @apiBody {String} password a users password
 * @apiBody {String} [username] a username *unique, if none provided, email will be used
 * 

 * 
 * @apiBodyExample {json} Request-Body-Example:
 *  {
 *      "first":"Charles",
 *      "last":"Bryan",
 *      "email":"cfb3@fake.email",
 *      "password":"test12345"
 *  }
 * 
 * @apiSuccess (Success 201) {boolean} success true when the password is changed
 * 
 * 
 * @apiUse JSONError
 */ 
router.put('/', (request, response, next) => {
    if (isStringProvided(request.headers.authorization) && request.headers.authorization.startsWith('Basic ')) {
        next()
    } else {
        response.status(400).json({ message: 'Missing Authorization Header' })
    }
}, (request, response, next) => {
    // obtain auth credentials from HTTP Header
    const base64Credentials =  request.headers.authorization.split(' ')[1]
    
    const credentials = Buffer.from(base64Credentials, 'base64').toString('ascii')

    const [email, password] = credentials.split(':')

    if (isStringProvided(email) && isStringProvided(password)) {
        request.auth = { 
            "email" : email,
            "password" : password
        }
        next()
    } else {
        response.status(400).send({
            message: "Malformed Authorization Header"
        })
    }
}, (request, response) => {
    const theQuery = "SELECT Password, Salt, MemberId FROM Members WHERE Email=$1"
    const values = [request.auth.email]
    pool.query(theQuery, values)
        .then(result => { 
            if (result.rowCount == 0) {
                response.status(404).send({
                    response: 'User not found' 
                })
                return
            }

            //Retrieve the salt used to create the salted-hash provided from the DB
            let salt = result.rows[0].salt
            
            //Retrieve the salted-hash password provided from the DB
            let storedSaltedHash = result.rows[0].password 

            //Generate a hash based on the stored salt and the provided password
            let providedSaltedHash = generateHash(request.auth.password, salt)

            //Did our salted hash match their salted hash?
            if (storedSaltedHash === providedSaltedHash ) {
                //credentials match. Set the new Password
                let salt = generateSalt(32)
                let salted_hash = generateHash(request.body.newPassword, salt)


                const theQuery = "UPDATE Members SET Password = $2, Salt = $3  WHERE Email=$1"
                const values = [request.auth.email, salted_hash, salt]
                pool.query(theQuery, values)
                    .then(result => { 
                        //package and send the results
                        sendMail(request.auth.email, "")
                        response.json({
                            success: true,
                            response: 'Swap successful!',
                            
                        })
                    }).catch((err) => {
                        //log the error
                        console.log(err.stack)
                        response.status(400).send({
                            message: err.detail
                        })
                    })
                
            } else {
                //credentials dod not match
                response.status(400).send({
                    response: 'Credentials did not match' 
                })
            }
        })
        .catch((err) => {
            //log the error
            console.log(err.stack)
            response.status(400).send({
                message: err.detail
            })
        })
})

const sendMail = (email, str) => {
    var Transport = nodemailer.createTransport({
        service:"Gmail",
        auth:{
            user: "team5.tcss450@gmail.com",
            pass: "Placeholder1!"
        }
    })

    var mailOptions;
    let sender ="Anabatos Team";
    mailOptions = {
        from: sender,
        to: email,
        subject: "Password Change",
        html: `A password change was requested for your account recently. If you requested this then you may disregard this email. If you did not request this change go to the forgot password on the login screen to receive a temporary password.`
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

module.exports = router

