const express = require('express')
const pool = require('../utilities').pool
const validation = require('../utilities').validation
let isStringProvided = validation.isStringProvided
let isValueProvided = validation.isValueProvided
const router = express.Router()
const connections_functions = require('../utilities/exports').messaging


/**
 * @apiDefine JSONError
 * @apiError (400: JSON Error) {String} message "malformed JSON in parameters"
 */ 

/**
 * @api {get} /connections Get all user connections
 * @apiName GetConnections
 * @apiGroup Connections
 * 
 * @apiHeader {Number} id1 the user's id
 * 
 * @apiSuccess (Success 201) {boolean} success true when the connections are retrived
 * @apiSuccess (Success 201) {List} A list of all connection's information
 * 
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 * @apiHeaderExample {json} Request-Example:
 *  {"id1": 1}
 * 
 * @apiUse JSONError
 */ 
router.get("/", (request, response, next) => {
    if (!isValueProvided(request.headers.id1)) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else {
        next()
    }
}, (request, response) => {
    //Grabe the list of connection ids
    let insert = `SELECT * FROM contacts WHERE MemberID_A = $1 OR MemberID_B = $1`
    let values = [request.headers.id1]
    pool.query(insert, values)
        .then(result => {
            //Convert the ids into user info
            let newValues = []
            for(i = 0; i < result.rowCount ; i ++){

                let id1 = result.rows[i].memberid_a 
                let id2 = result.rows[i].memberid_b

                if(id1 != request.headers.id1){
                    newValues.push(id1)
                }
                else{
                    newValues.push(id2)
                }
            }
            console.log(newValues)
            let emailQuery = `SELECT FirstName, LastName, Email, MemberID, Nickname FROM members WHERE MemberID IN (` + newValues.join(',') +`)`
            console.log(emailQuery)
            pool.query(emailQuery)
                .then(newResult => {
                    response.send({
                        success: true,
                        rows: newResult.rows,
                        rowCount: newResult.rowCount
                    })
                })
                .catch(err => {
                    
                    response.send({
                        message: "SQL Error",
                        rows: {},
                        error: err
                    })
        
                })

        }).catch(err => {
            
            response.send({
                message: "SQL Error",
                rows: {},
                error: err
            })

        })
})
/**
 * @api {post} /connections Accept a connection request
 * @apiName AcceptConnections
 * @apiGroup Connections
 * 
 * @apiBody {Number} id1 the user's id
 * @apiBody  {Number} id2 the user id of the accepeted connection
 * 
 * @apiSuccess (Success 201) {boolean} success true when the connections are retrived
 * 
 * 
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * @apiParamExample {json} Request-Body-Example:
 *  {
 *      "id1":1,
 *      "id2":3
 * 
 *  }
 * @apiUse JSONError
 */ 
router.post("/", (request, response, next) => {
    if (!isValueProvided(request.body.id1) || !isValueProvided(request.body.id2)) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else {
        next()
    }
}, (request, response, next) => {

    //Delete the request
    let insert = `DELETE FROM requests WHERE SenderID = $1 AND ReceiverID = $2`
    let values = [request.body.id1, request.body.id2]
    pool.query(insert, values)
        .then(result => {
            
        }).catch(err => {
            response.send({
                message: "SQL Error",
                success: false,
                error: err
            })

        })
    //Add the connection
    insert = 'INSERT INTO contacts(MemberID_A, MemberID_B) VALUES ($1, $2)'
    pool.query(insert, values)
    .then(result => {
        next()
    }).catch(err => {
        response.send({
            message: "SQL Error",
            success: false,
            error: err
        })

    })
}, (request, response, next) => {
    //Send a push notification to the original sender
    let query = `SELECT token FROM Push_Token WHERE memberid = $1`
    let values = [request.body.id1]
    pool.query(query, values)
    .then(result => {
        result.rows.forEach(entry => 
            {
                let query = 'SELECT Firstname, Lastname, Email, Memberid, Nickname FROM members WHERE MemberID = $1'
                let values = [request.body.id2]
                pool.query(query, values)
                .then(result => {
                    let receiver = result.rows[0]
                    connections_functions.notifyAcceptRequest(
                        entry.token,
                        request.body.id1,
                        receiver
                    )
                    connections_functions.notifyListChange(entry.token, "outgoing")
                    next()
                    
                }).catch(err => {
                response.send({ 
                        message: "SQL Error",
                        success: false,
                        error: err
                    })
                })
            })
            next()

    }).catch(err => {
        
        response.send({
            message: "SQL Error",
            success: false,
            error: err
        })
    })
}, (request, response, next) => {
    //Send a push notification to the original receiver
    let query = `SELECT token FROM Push_Token WHERE memberid = $1`
    let values = [request.body.id2]
    pool.query(query, values)
    .then(result => {
        result.rows.forEach(entry => 
            {
                connections_functions.notifyListChange(entry.token, "incoming")
            })
            
        }).catch(err => {
        
                response.send({
                    message: "SQL Error",
                    success: false,
                    error: err
                })
            }
        )

        response.send({
            success: true,
        })
    })


/**
 * @api {delete} /connections Remove a connection
 * @apiName DeleteConnections
 * @apiGroup Connections
 * 
 * @apiHeader {Number} id1 the user's id
 * @apiHeader {Number} id2 the id of the connection to delete
 * 
 * @apiSuccess (Success 201) {boolean} success true when the connection is removed
 * 
 * 
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * @apiHeaderExample {json} Request-Example:
 *  {"id1": 1, "id2": 3}
 * 
 * @apiUse JSONError
 */ 
router.delete("/", (request, response, next) => {
    if (!isValueProvided(request.headers.id1) || !isValueProvided(request.headers.id2)) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else {
        next()
    }
}, (request, response, next) => {

    let insert = `DELETE FROM contacts WHERE (MemberID_A = $1 AND MemberID_B = $2) OR (MemberID_A = $2 AND MemberID_B = $1)`
    let values = [request.headers.id1, request.headers.id2]
    pool.query(insert, values)
        .then(result => {
            next()
        }).catch(err => {
            response.send({
                message: "SQL Error",
                success: false,
                error: err
            })

        })
}, (request, response) => {
    let query = `SELECT token FROM Push_Token WHERE memberid = $1 OR memberid = $2`
    let values = [request.headers.id1, request.headers.id2]

  
    pool.query(query, values)
        .then(result => {
            result.rows.forEach(entry => 
                {
                    connections_functions.notifyListChange(entry.token, "connections")
                })
            
        }).catch(err => {
            response.send({
                message: "SQL Error",
                success: false,
                error: err
            })

        })
        response.send({
            success: true,
        })
    
}
)
module.exports = router


