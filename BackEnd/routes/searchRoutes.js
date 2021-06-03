/** Express router providing search for tracks
 * @module routes/search
 * @requires express
 */

/**
 * express module
 * @const
 */
 const express = require('express')
 const router = express.Router()
 
 /**
  * Search controller to call when routing.
  * @type {object}
  * @const
  */
 const searchController = require('../controllers/searchController')
 
 /**
  * Route for searching
  * @name get/
  * @function
  * @memberof module:routes/search
  * @inner
  */
 router
   .route('/')
  .get(searchController.getSearchResults)
   
   
 /**
  * Route for getting search suggestions
  * @name get/suggest
  * @function
  * @memberof module:routes/search
  * @inner
  */
 router
 .route('/suggest/')
 .get(searchController.getSearchSuggestions)
 
 module.exports = router