const express = require('express')
const pool = require('../utilities').pool
const validation = require('../utilities').validation
let isStringProvided = validation.isStringProvided
let isValueProvided = validation.isValueProvided
const connections_functions = require('../utilities/exports').messaging
const router = express.Router()

/**
 * @apiDefine JSONError
 * @apiError (400: JSON Error) {String} message "malformed JSON in parameters"
 */ 

/**
 * @api {post} /request Request to add a connection
 * @apiName PostRequest
 * @apiGroup Request
 * 
 * @apiBody {Number} id1 the user's id
 * @apiBody {Number} id2 a valid id for the user to add
 * 
 * @apiSuccess (Success 201) {boolean} success true when the request is sent
 * @apiSuccess (Success 201) {Number} requestId the generated requestId
 * 
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 * @apiParamExample {json} Request-Body-Example:
 *  {
 *      "id1":1,
 *      "id2":3
 * 
 *  }
 * 
 * @apiUse JSONError
 */ 
 let duplicates = false
 router.post("/", (request, response, next) => {
    
    if (!isValueProvided(request.body.id1) || !isValueProvided(request.body.id2)) {
        response.status(400).send({
            message: "Missing required information"
        })
    }
    checkDuplicateConnection(request.body.id1, request.body.id2)
    checkDuplicateRequest(request.body.id1, request.body.id2)
    
    if(duplicates){
        
        response.status(400).send({
            response: "Duplicate request or connection already exists"
        })
    }
    else {
        next()
    }
}, (request, response, next) => {

    let insert = `INSERT INTO Requests(SenderID, ReceiverID)
                  VALUES ($1, $2)`
    let values = [request.body.id1, request.body.id2]
    pool.query(insert, values)
        .then(result => {    
            next()
        }).catch(err => {
            response.send({
                message: "SQL Error",
                response: "Submission Failed",
                success: false,
                error: err
            })

        })
        
}, (request, response, next) => {
    //Push notify the reciever
    let query = `SELECT token FROM Push_Token WHERE memberid = $1`
    let values = [request.body.id2]
    
    pool.query(query, values)
    .then(result => {
        result.rows.forEach(entry => 
            {
                let query = 'SELECT Firstname, Lastname, Email, Memberid, Nickname FROM members WHERE MemberID = $1'
                let values = [request.body.id1]
                pool.query(query, values)
                .then(result => {
                    let sender = result.rows[0]
                    connections_functions.sendRequestToIndividual(
                        entry.token,
                        sender,
                        request.body.id2
                    )
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

}, (request, response) => {
    //Send a push notification to the original sender to update their lists
    let query = `SELECT token FROM Push_Token WHERE memberid = $1`
    let values = [request.body.id1]
    pool.query(query, values)
    .then(result => {
        result.rows.forEach(entry => 
            {
                connections_functions.notifyListChange(entry.token, "outgoing")
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


//Helper method to prevent dupliacate adds
let checkDuplicateConnection = (id1, id2) => {
    let insert = "SELECT * FROM contacts WHERE (MemberID_A = $1 AND MemberID_B = $2) OR (MemberID_A = $2 AND MemberID_B = $1)"
    let values = [id1, id2]
    pool.query(insert, values)
        .then(result =>{
            
            if(result.rows.length == 0){
                
                dupllicates = false
            }
            else{
                duplicates = true
            }
            
        }).catch(err =>{
            duplicates = false
        })
    
}

//Helper method to prevent duplicate requests
let checkDuplicateRequest = (id1, id2) => {
    let insert = "SELECT * FROM Requests WHERE (SenderID = $1 AND ReceiverID = $2) OR (SenderID = $2 OR ReceiverID = $1)"
    let values = [id1, id2]
    pool.query(insert, values)
        .then(result =>{
            
            if(result.rows.length == 0){
                
                 duplicates = false
            }
            else{
                 duplicates = true
            }
            
        }).catch(err =>{
             duplicates = false
        })
    
} 

/**
 * @api {get} /request Get outgoing requests
 * @apiName GetOutRequest
 * @apiGroup Request
 * 
 * @apiHeader {Number} id1 the users's id
 * 
 * 
 * @apiSuccess (Success 201) {boolean} success true when the requests are returned
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

    let insert = `SELECT * FROM requests WHERE SenderID = $1`
    let values = [request.headers.id1]
    pool.query(insert, values)
        .then(result => {
            //Convert the ids into user info
            let newValues = []
            for(i = 0; i < result.rowCount ; i ++){

                let id1 = result.rows[i].senderid
                let id2 = result.rows[i].receiverid

                if(id1 != request.headers.id1){
                    newValues.push(id1)
                }
                else{
                    newValues.push(id2)
                }
            }
            
            let emailQuery = `SELECT FirstName, LastName, Email, MemberID, Nickname FROM members WHERE MemberID IN (` + newValues.join(',') +`)`
            
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
                error: err,
                rows: {}
            })

        })
})

/**
 * @api {get} /request/incoming Get incoming requests
 * @apiName GetIncRequest
 * @apiGroup Request
 * 
 * @apiHeader {Number} id the users's id
 * 
 * 
 * @apiSuccess (Success 201) {boolean} success true when the requests are returned
 * 
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 *@apiHeaderExample {json} Request-Example:
 *  {"id": 1}
 * 
 * @apiUse JSONError
 */ 
router.get("/incoming", (request, response, next) => {
    if (!isValueProvided(request.headers.id)) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else {
        next()
    }
}, (request, response) => {

    let insert = `SELECT * FROM requests WHERE ReceiverID = $1`
    let values = [request.headers.id]
    pool.query(insert, values)
        .then(result => {
            //Convert the ids into user info
            
            let newValues = []
            for(i = 0; i < result.rowCount ; i ++){

                let id1 = result.rows[i].senderid
                let id2 = result.rows[i].receiverid
                

                if(id1 != request.headers.id1){
                    newValues.push(id1)
                }
                else{
                    newValues.push(id2)
                }
            }
            
            let emailQuery = `SELECT FirstName, LastName, Email, MemberID, Nickname FROM members WHERE MemberID IN (` + newValues.join(',') +`)`
            
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
 * @api {delete} /request Delete incoming or outgoing
 * @apiName DeleteRequest
 * @apiGroup Request
 * 
 * @apiHeader {Number} id1 the users's id
 * @apiHeader {Number} id2 the id of the user the request was sent to
 * 
 * 
 * @apiSuccess (Success 201) {boolean} success true when the request is deleted
 * 
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
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
    //Delete item from db
    let insert = `DELETE FROM requests WHERE SenderID = $1 AND ReceiverID = $2`
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
}, (request, response, next) => {
    //Send a push notification to the original sender to update their lists

    let query = `SELECT token FROM Push_Token WHERE memberid = $1`
    let values = [request.headers.id1]
    pool.query(query, values)
    .then(result => {
        result.rows.forEach(entry => 
            {
                connections_functions.notifyListChange(entry.token, "outgoing")
            })
            next()
            
        }).catch(err => {
        
                response.send({
                    message: "SQL Error",
                    success: false,
                    error: err
                })
            }
        )
}, (request, response) => {
    //Send a push notification to the receiver to update their lists
   
    let query = `SELECT token FROM Push_Token WHERE memberid = $1`
    let values = [request.headers.id2]
    pool.query(query, values)
    .then(result => {
        
        result.rows.forEach(entry => 
            {
                connections_functions.notifyListChange(entry.token, "incoming")
            })
            response.send({
                success: true
            })
            
        }).catch(err => {
        
                response.send({
                    message: "SQL Error",
                    success: false,
                    error: err
                })
            }
        )
})
/**
 * @api {get} /request/search Get user info with nickname like *
 * @apiName GetUsers
 * @apiGroup Request
 * 
 * @apiHeader {Number} query the user's search query
 * 
 * 
 * @apiSuccess (Success 201) {boolean} success true when the users are returned
 * 
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 *@apiHeaderExample {json} Request-Example:
 *  {"query": "name"}
 * 
 * @apiUse JSONError
 */ 
router.get("/search", (request, response, next) => {
    if (!isStringProvided(request.headers.query)) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else {
        next()
    }
}, (request, response) => {

    let insert = `SELECT Firstname, Lastname, Email, Nickname, Memberid FROM members Where UPPER(Nickname) LIKE UPPER($1) OR UPPER(Firstname) LIKE UPPER($1) OR UPPER(Lastname) LIKE UPPER($1)`
    let values = ['%' + request.headers.query + '%']
    
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
                response: "No users with that information",
                error: err,
                rows: {}
            })

        })
})

module.exports = router