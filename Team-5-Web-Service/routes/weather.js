const express = require('express')
const request = require('request')

const router = express.Router()
const { isStringProvided } = require('../utilities').validation
const middleware = require('../middleware')

function zipRequestUrl(zipcode) {
  return `http://api.openweathermap.org/data/2.5/weather?zip=${zipcode}&appid=${process.env.WEATHER_MAP_API_KEY}`
}

function latLonUrl(lat, lon) {
  return `https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${process.env.WEATHER_MAP_API_KEY}`
}

function oneCallUrl(lat, lon) {
  return `https://api.openweathermap.org/data/2.5/onecall?lat=${lat}&lon=${lon}&appid=${process.env.WEATHER_MAP_API_KEY}`
}

/**
 * Get the weather for a location given coords and asynchronously
 * call a given function when the result is ready.
 *
 * @param lon - the longitude of the location
 * @param lat - the latitude of the location
 * @param giveValue - The function that will receive the result when it is ready
 */
function getOneCallWeatherFromCoords(lat, lon, name, giveValue) {
  request(oneCallUrl(lat, lon), (error, req, res) => {
    if (error) {
      // If there was an error with the request itself give a 500 code
      giveValue({ code: 500 })
    } else {
      const jsonResponse = JSON.parse(res)
      // If there is an error code
      if (jsonResponse.cod) {
        // Determine if the location couldn't be found or if something worse happened
        if (jsonResponse.cod === 404) {
          // No location
          giveValue({ code: 404 })
        } else {
          // Internal error (no api key/too many calls)
          giveValue({ code: 500 })
        }
      } else {
        // It's good just pass the data through
        jsonResponse['name'] = name
        giveValue(jsonResponse)
      }
    }
  })
}

/**
 * Get the weather for a location given zipcode and asynchronously
 * call a given function when the result is ready.
 *
 * @param zipcode - The zipcode of the location
 * @param giveValue - The function that will receive the result when it is ready
 */
function getWeatherFromZip(zipcode, giveValue) {
  // Get the coords
  request(zipRequestUrl(zipcode), (error, req, res) => {
    const jsonResponse = JSON.parse(res)
    if (error) {
      // Error with the request itself
      giveValue({ code: 500 })
    } else if (jsonResponse.cod === 200) {
      // Use the coords to use the single call api
      getOneCallWeatherFromCoords(jsonResponse.coord.lat, jsonResponse.coord.lon, jsonResponse.name, giveValue)
    } else if (jsonResponse.cod === 404) {
      // Couldn't find the location
      giveValue({ code: 404 })
    } else {
      // Internal server error accoring (no api key/too many calls)
      console.log(res)
      giveValue({ code: 500 })
    }
  })
}

/**
 * Get the weather for a location given zipcode and asynchronously
 * call a given function when the result is ready.
 *
 * @param zipcode - The zipcode of the location
 * @param giveValue - The function that will receive the result when it is ready
 */
function getWeatherFromCoords(lat, lon, giveValue) {
  // Get the coords
  request(latLonUrl(lat, lon), (error, req, res) => {
    const jsonResponse = JSON.parse(res)
    if (error) {
      // Error with the request itself
      giveValue({ code: 500 })
    } else if (jsonResponse.cod === 200) {
      // Use the coords to use the single call api
      getOneCallWeatherFromCoords(jsonResponse.coord.lat, jsonResponse.coord.lon, jsonResponse.name, giveValue)
    } else if (jsonResponse.cod === 404) {
      // Couldn't find the location
      giveValue({ code: 404 })
    } else {
      // Internal server error accoring (no api key/too many calls)
      giveValue({ code: 500 })
    }
  })
}

/**
 * @api {get} /weather Request to get the weather from a certain location
 * from either a zipcode or lon/lat coordinates
 * @apiName GetWeather
 * @apiGroup Weather
 *
 * @apiHeader {String} authorization - Valid JSON Web Token JWT
 * @apiParam {String} [zipcode] - The zipcode of the requested location
 * @apiParam {String} [lat] - The latitude of the location
 * @apiParam {String} [lon] - The longitude of the location
 *
 * @apiSuccess Returns the weather for the location when everything goes right
 *
 * @apiError (400: Missing Parameters) {String} message "Missing required information"
 *
 * @apiError (404: User Not Found) {String} message "Location not found"
 *
 * @apiError (500: User Not Found) {String} message "Error trying to get weather"
 */
router.get('/', middleware.checkToken, (req, res) => {
  if (isStringProvided(req.query.zip)) {
    console.log("Couldn't get lat/lon");
    // If we have a zip code then search with it
    // Sanitize the zip
    const zip = `${parseInt(req.query.zip, 10)}`
    getWeatherFromZip(zip, (weather) => {
      // Check for an error code in the weather and formulate the proper response
      switch (weather.code) {
        case 404:
          res.status(404).send({ message: 'Location not found' })
          break
        case 500:
          res.status(500).send({ message: 'Error trying to get the weather' })
          break
        case undefined:
        default:
          res.status(200).send(weather)
          break
      }
    })
  } else if (isStringProvided(req.query.lat) && isStringProvided(req.query.lon)) {
    // If we have lat/lon then use that to find the weather
    // Sanitize the lat and lon
   
    const lat = `${parseFloat(req.query.lat)}`
    const lon = `${parseFloat(req.query.lon)}`
    
    getWeatherFromCoords(lat, lon, (weather) => {
      // Check for an error code in the weather and formulate the proper response
      switch (weather.code) {
        case 404:
          res.status(404).send({ message: 'Location not found' })
          break
        case 500:
          res.status(500).send({ message: 'Error trying to get the weather' })
          break
        default:
          res.status(200).send(weather)
          break
      }
    })
  } else {
    // If we have neither then what are we doing here?
    res.status(400).send({ message: 'Missing required information' })
  }
})

module.exports = router
