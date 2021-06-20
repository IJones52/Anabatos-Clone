//express is the framework we're going to use to handle requests
const express = require('express')

const async = require('async')

//access the connection to th Heroku Database
const pool = require('../utilities/exports').pool

const router = express.Router()

const validation = require('../utilities').validation
const chat_functions = require('../utilities/exports').messaging
const { response, request } = require('express')
let isStringProvided = validation.isStringProvided

/**
 * @api {post} /chats Request to add a chat and its corresponding members
 * @apiName PostChats
 * @apiGroup Chats
 * 
 * @apiHeader {String} authorization Valid JSON Web Token JWT
 * @apiParam {String} name the name for the chat
 * @apiParam {Array} members list of memberid's to add
 * 
 * @apiSuccess (Success 201) {boolean} success true when the name is inserted
 * @apiSuccess (Success 201) {Number} chatId the generated chatId
 * 
 * @apiError (400: Unknown user) {String} message "unknown email address"
 * 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 * @apiError (400: Unknown Chat ID) {String} message "invalid chat id"
 * 
 * @apiUse JSONError
 */
 router.post('/', (request, response, next) => {
    // if body is empty
    if (!isStringProvided(request.body.name)) {
        response.status(400).send({
            message: "Missing chat name"
        })
    } else if (request.body.members === undefined || request.body.members.length === 0) {
        response.status(400).send({
            message: "Missing members array"
        })
    } else {
        next()
    }
}, (request, response, next) => {
    let insert = `INSERT INTO Chats(Name)
                    VALUES ($1)
                    RETURNING ChatId`
    let values = [request.body.name]
    pool.query(insert, values).then(result => {
        response.chatid = result.rows[0].chatid
        next()
    }).catch(err => {
        response.status(400).send({
            message: "SQL Error",
            error: err
        })
    })
}, (request, response, next) => {

    request.body.members.push(request.decoded.memberid)

    let query = `SELECT * FROM Members WHERE MemberId IN (` + request.body.members.join(',') + `)`

    pool.query(query).then(result => {
        if (result.rowCount == 0) {
            response.status(404).send({
                message: "Members not found"
            })
        } else {
            next()
        }
    }).catch(error => {
        response.status(400).send({
            message: "SQL Error",
            error: error
        })
    })
}, (request, response) => {
    let members = request.body.members
    console.log(members);

    // Insert each member id into the chat
    async.forEachOf(members, (memberid, i, callback) => {
        console.log("memberid: " + memberid);
        let insert = `INSERT INTO ChatMembers(ChatId, MemberId) VALUES ($1, $2) RETURNING *`
        let values = [response.chatid, memberid]

        pool.query(insert, values).then(result => {
            callback()
        }).catch(err => {
            callback(err)
        })
    }, err => {
        if (err) {
            console.log(err);
            response.status(400).send({
                message: "SQL Error",
                error: err
            })
        } else {
            response.send({
                success: true,
                chatid: response.chatid,
                members: request.body.members,
                name: request.body.name
            })
        }
    })

});


/**
 * @api {get} /chats Request to get all existing chats
 * @apiName GetChats
 * @apiGroup Chats
 * 
 * @apiHeader {String} authorization Valid JSON Web Token JWT
 * 
 * @apiSuccess {Object[]} chatInfo List of chatIds and chatNames
 * 
 * @apiError (400: SQL Error) {String} message The reported SQL error details
 * 
 * @apiUse JSONError
 */
router.get("/", (request, response, next) => {
    console.log(request.decoded.memberid);
    if (isNaN(request.decoded.memberid)) {
        response.status(400).send({
            message: "The memberid retrieved from JWT is not a number"
        })
    } else {
        next()
    }
}, (request, response, next) => {
    let chatQuery = `Select c.chatid, c.name from Chats as C 
                    inner join Chatmembers as cm 
                    on c.chatid=cm.chatid where cm.memberid=` + request.decoded.memberid

    pool.query(chatQuery).then(result1 => {
        request.result = result1.rows
        next()
    }).catch(err => {
        console.log(err);
        response.status(400).send({
            message: "SQL Error",
            error: err
        })
    })
}, (request, response, next) => {
    let values = request.result
    console.log(values);

    async.forEachOf(values, (value, i, callback) => {
        // get chat members
        let memberQuery = `Select m.memberid, m.firstname, m.lastname, m.email, m.nickname 
                            from chatmembers as cm 
                            inner join members as m on cm.memberid=m.memberid 
                            where chatid=` + values[i].chatid + `and m.memberid<>` + request.decoded.memberid

        pool.query(memberQuery).then(result2 => {
            values[i].members = result2.rows;
            console.log(values[i]);
            callback()
        }).catch(err => {
            console.log(err);
            callback(err)
        })
    }, err => {
        if (err) {
            console.log(err);
            response.status(400).send({
                message: "SQL Error",
                error: err
            })
        } else {
            request.result = values
            next()
        }
    })
}, (request, response) => {
    let values = request.result
    console.log(values);

    async.forEachOf(values, (value, i, callback) => {
        // get chat members
        let msgQuery = `SELECT Messages.PrimaryKey AS messageId, Members.FirstName, Members.LastName, Members.Email, Messages.Message, 
                            to_char(Messages.Timestamp AT TIME ZONE 'PDT', 'YYYY-MM-DD HH24:MI:SS.US' ) AS Timestamp
                            FROM Messages
                            INNER JOIN Members ON Messages.MemberId=Members.MemberId
                            WHERE ChatId=$1
                            ORDER BY Timestamp DESC
                            LIMIT 1`
        let chatid = [value.chatid]
        pool.query(msgQuery, chatid).then(result3 => {
            value.message = result3.rows[0]
            console.log(value);
            callback()
        }).catch(err => {
            console.log(err);
            callback(err)
        })
    }, err => {
        if (err) {
            console.log(err);
            response.status(400).send({
                message: "SQL Error",
                error: err
            })
        } else {
            request.result = values
            response.send({
                success: true,
                rows: values
            })
        }
    })
});

/**
 * @api {put} /chats/:chatId/:memberId Request add a connection to a chat via their memberid
 * @apiName PutChats
 * @apiGroup Chats
 * 
 * @apiDescription Adds the user associated with the memberid. 
 * 
 * @apiHeader {String} authorization Valid JSON Web Token JWT
 * 
 * @apiParam {Number} chatId the chat to add the user to
 * @apiParam {Number} memberId the member to add to the chat
 * 
 * @apiSuccess {boolean} success true when the name is inserted
 * 
 * @apiError (404: Chat Not Found) {String} message "chatID not found"
 * @apiError (404: Email Not Found) {String} message "email not found"
 * @apiError (400: Invalid Parameter) {String} message "Malformed parameter. chatId must be a number" 
 * @apiError (400: Invalid Parameter) {String} message "Malformed parameter. memberId must be a number"
 * @apiError (400: Duplicate Email) {String} message "user already joined"
 * @apiError (400: Missing Parameters) {String} message "Missing chatid in params"
 * @apiError (400: Missing Parameters) {String} message "Missing memberid in params"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 * @apiUse JSONError
 */
router.put("/:chatId/:memberId", (request, response, next) => {
    // validate on empty parameters
    if (!request.params.chatId) {
        response.status(400).send({
            message: "Missing chatid in params"
        })
    } else if (!request.params.memberId) {
        response.status(400).send({
            message: "Missing memberid in params"
        })
    } else if (isNaN(request.params.chatId)) {
        response.status(400).send({
            message: "Malformed parameter. chatId must be a number"
        })
    } else if (isNaN(request.params.memberId)) {
        response.status(400).send({
            message: "Malformed parameter. memberId must be a number"
        })
    } else {
        next()
    }
}, (request, response, next) => {
    // validate chat id exists
    let query = 'SELECT * FROM CHATS WHERE ChatId=$1'
    let values = [request.params.chatId]

    pool.query(query, values).then(result => {
        if (result.rowCount == 0) {
            response.status(404).send({
                message: "Chat ID not found"
            })
        } else {
            next()
        }
    }).catch(error => {
        response.status(400).send({
            message: "SQL Error",
            error: error
        })
    })
}, (request, response, next) => {
    // validate member exists
    let query = 'SELECT firstname, lastname, email, nickname FROM Members WHERE MemberId=$1'
    let values = [request.params.memberId]

    console.log(request.params.memberId);

    pool.query(query, values).then(result => {
        if (result.rowCount == 0) {
            response.status(404).send({
                message: "Member not found"
            })
        } else {
            // user found
            let user = result.rows[0]
            response.user = {
                memberid: request.params.memberId,
                firstname: user.firstname,
                lastname: user.lastname,
                email: user.email,
                nickname: user.nickname
            }
            console.log(response.user);
            next()
        }
    }).catch(error => {
        response.status(400).send({
            message: "SQL Error",
            error: error
        })
    })
}, (request, response, next) => {
    // validate email does not already exist in the chat
    let query = 'SELECT * FROM ChatMembers WHERE ChatId=$1 AND MemberId=$2'
    let values = [request.params.chatId, request.params.memberId]

    pool.query(query, values).then(result => {
        if (result.rowCount > 0) {
            response.status(400).send({
                message: "User already joined"
            })
        } else {
            next()
        }
    }).catch(error => {
        response.status(400).send({
            message: "SQL Error",
            error: error
        })
    })
}, (request, response, next) => {
    // Insert member id into the chat
    let insert = `INSERT INTO ChatMembers(ChatId, MemberId) VALUES ($1, $2)`
    let values = [request.params.chatId, request.params.memberId]

    pool.query(insert, values).then(result => {
        next()
    }).catch(err => {
        response.status(400).send({
            message: "SQL Error",
            error: err
        })
    })
}, (request, response) => {
    let query = `SELECT token FROM Push_Token
                        INNER JOIN ChatMembers ON
                        Push_Token.memberid=ChatMembers.memberid
                        WHERE ChatMembers.chatId=$1`
    let values = [request.params.chatId]
    pool.query(query, values).then(result => {
        result.rows.forEach(entry => {
            chat_functions.notifyUserAddedChat(entry.token, response.user)
        })
        response.send({
            success: true
        })
    }).catch(err => {
        response.status(400).send({
            message: "SQL Error on select from push token",
            error: err
        })
    })
});

/**
 * @api {get} /chats/:chatId? Request to get the emails of user in a chat
 * @apiName GetChats
 * @apiGroup Chats
 * 
 * @apiHeader {String} authorization Valid JSON Web Token JWT
 * 
 * @apiParam {Number} chatId the chat to look up. 
 * 
 * @apiSuccess {Number} rowCount the number of messages returned
 * @apiSuccess {Object[]} members List of members in the chat
 * @apiSuccess {String} messages.email The email for the member in the chat
 * 
 * @apiError (404: ChatId Not Found) {String} message "Chat ID Not Found"
 * @apiError (400: Invalid Parameter) {String} message "Malformed parameter. chatId must be a number" 
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 * @apiUse JSONError
 */
router.get("/:chatId", (request, response, next) => {
    // validate on empty parameters
    if (!request.params.chatId) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else if (isNaN(request.params.chatId)) {
        response.status(400).send({
            message: "Malformed parameter. chatId must be a number"
        })
    } else {
        next()
    }
}, (request, response, next) => {
    // validate chat id exists
    let query = 'SELECT * FROM CHATS WHERE ChatId = $1'
    let values = [request.params.chatId]

    pool.query(query, values).then(result => {
        if (result.rowCount == 0) {
            response.status(404).send({
                message: "Chat ID not found"
            })
        } else {
            next()
        }
    }).catch(error => {
        response.status(400).send({
            message: "SQL Error",
            error: error
        })
    })
}, (request, response) => {
    // Retrieve the members
    let query = `SELECT Members.Email FROM ChatMembers 
                INNER JOIN Members ON ChatMembers.MemberId=Members.MemberId
                WHERE ChatId=$1`
    let values = [request.params.chatId]
    pool.query(query, values).then(result => {
        response.send({
            chatId: request.params.chatId,
            rowCount: result.rowCount,
            rows: result.rows
        })
    }).catch(err => {
        response.status(400).send({
            message: 'SQL Error',
            error: err
        })
    })
});

/**
 * @api {delete} /chats/:chatId/:memberId Request delete a user from a chat
 * @apiName DeleteUser
 * @apiGroup Chats
 * 
 * @apiDescription Does not delete the user associated with the required JWT but 
 * instead deletes the user based on the memberid parameter.  
 * 
 * @apiParam {Number} chatId the chat to delete the user from
 * @apiParam {Number} memberId the memberid of the member to be removed
 * 
 * @apiSuccess {boolean} success true when the name is deleted
 * 
 * @apiError (404: Chat Not Found) {String} message "chatID not found"
 * @apiError (404: Member Not Found) {String} message "member not found"
 * @apiError (400: Invalid Parameter) {String} message "Malformed parameter. chatId or memberId must be a number" 
 * @apiError (400: Duplicate Email) {String} message "user not in chat"
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 * @apiUse JSONError
 */
router.delete("/:chatId/:memberId", (request, response, next) => {
    console.log("Delete user with id: " + request.params.memberId);

    // validate on empty parameters
    if (!request.params.chatId) {
        response.status(400).send({
            message: "Missing chatid in params"
        })
    } else if (!request.params.memberId) {
        response.status(400).send({
            message: "Missing memberid in params"
        })
    } else if (isNaN(request.params.chatId)) {
        response.status(400).send({
            message: "Malformed parameter. chatId must be a number"
        })
    } else if (isNaN(request.params.memberId)) {
        response.status(400).send({
            message: "Malformed parameter. memberId must be a number"
        })
    } else {
        next()
    }
}, (request, response, next) => {
    // validate member exists
    let query = 'SELECT firstname, lastname, email, nickname FROM Members WHERE MemberId=$1'
    let values = [request.params.memberId]

    console.log("member id: " + request.params.memberId);

    pool.query(query, values).then(result => {
        if (result.rowCount == 0) {
            response.status(404).send({
                message: "Member not found"
            })
        } else {
            // user found
            let user = result.rows[0]
            response.user = {
                memberid: request.params.memberId,
                firstname: user.firstname,
                lastname: user.lastname,
                email: user.email,
                nickname: user.nickname
            }
            console.log(response.user);
            next()
        }
    }).catch(error => {
        response.status(400).send({
            message: "SQL Error",
            error: error
        })
    })
}, (request, response, next) => {
    //validate email exists in the chat
    let query = 'SELECT * FROM ChatMembers WHERE ChatId=$1 AND MemberId=$2'
    let values = [request.params.chatId, request.params.memberId]

    console.log("chat id: " + request.params.chatId);

    pool.query(query, values)
        .then(result => {
            if (result.rowCount > 0) {
                next()
            } else {
                response.status(400).send({
                    message: "user not in chat"
                })
            }
        }).catch(error => {
            response.status(400).send({
                message: "SQL Error",
                error: error
            })
        })

}, (request, response, next) => {
    //Delete the memberId from the chat
    let insert = `DELETE FROM ChatMembers
                  WHERE ChatId=$1
                  AND MemberId=$2`
    let values = [request.params.chatId, request.params.memberId]
    pool.query(insert, values)
        .then(result => {
            next()
        }).catch(err => {
            response.status(400).send({
                message: "SQL Error",
                error: err
            })
        })
}, (request, response) => {
    let query = `SELECT token FROM Push_Token
                        INNER JOIN ChatMembers ON
                        Push_Token.memberid=ChatMembers.memberid
                        WHERE ChatMembers.chatId=$1`
    let values = [request.params.chatId]
    pool.query(query, values).then(result => {
        result.rows.forEach(entry => {
            chat_functions.notifyUserRemovedChat(entry.token, response.user)
        })
        response.send({
            success: true
        })
    }).catch(err => {
        response.status(400).send({
            message: "SQL Error on select from push token",
            error: err
        })
    })
});

/**
 * @api {delete} /chats/:chatId Request delete a chat
 * @apiName DeleteChats
 * @apiGroup Chats
 * 
 * @apiDescription Deletes the chat room requested.  
 * 
 * @apiParam {Number} chatId the chat to delete the user from
 * 
 * @apiSuccess {boolean} success true when the chat is deleted
 * 
 * @apiError (404: Chat Not Found) {String} message "chatID not found"
 * @apiError (404: Member Not Found) {String} message "member not found"
 * @apiError (400: Invalid Parameter) {String} message "Malformed parameter. chatId or memberId must be a number" 
 * @apiError (400: Duplicate Email) {String} message "user not in chat"
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 * @apiError (400: Malformed Parameters) {String} message "Malformed parameters"
 * 
 * @apiError (400: SQL Error) {String} message the reported SQL error details
 * 
 * @apiUse JSONError
 */
router.delete("/:chatId", (request, response, next) => {
    console.log("Chat id: " + request.params.chatId);
    //validate on empty parameters
    if (!request.params.chatId) {
        response.status(400).send({
            message: "Missing required information"
        })
    } else if (isNaN(request.params.chatId)) {
        response.status(400).send({
            message: "Malformed parameter. chatId must be a number"
        })
    } else {
        next()
    }
}, (request, response, next) => {
    // save chat members to notify them later
    let query = `SELECT token FROM Push_Token
                INNER JOIN ChatMembers ON
                Push_Token.memberid=ChatMembers.memberid
                WHERE ChatMembers.chatId=$1`
    let values = [request.params.chatId]
    pool.query(query, values).then(result => {
        request.members = result.rows
        next()
    }).catch(err => {
        response.status(400).send({
            message: "SQL Error on select from push token",
            error: err
        })
    })
}, (request, response, next) => {
    // remove chat members
    let insert = `DELETE FROM ChatMembers
                  WHERE ChatId=$1`
    let values = [request.params.chatId]
    pool.query(insert, values).then(result => {
        next()
    }).catch(err => {
        response.status(400).send({
            message: "SQL Error",
            error: err
        })
    })
}, (request, response, next) => {
    // remove messages
    let insert = `DELETE FROM Messages
                    WHERE ChatId=$1`
    let values = [request.params.chatId]
    pool.query(insert, values).then(result => {
        next()
    }).catch(err => {
        response.status(400).send({
            message: "SQL Error",
            error: err
        })
    })
}, (request, response, next) => {
    // remove the chat room
    let insert = `DELETE FROM Chats
                    WHERE ChatId=$1`
    let values = [request.params.chatId]
    pool.query(insert, values).then(result => {
        next()
    }).catch(err => {
        response.status(400).send({
            message: "SQL Error",
            error: err
        })
    })
}, (request, response) => {
    // notify each chat members
    request.members.forEach(entry => {
        chat_functions.notifyDeleteChat(entry.token, request.params.chatId)
    })
    response.send({
        success: true
    })
})

module.exports = router