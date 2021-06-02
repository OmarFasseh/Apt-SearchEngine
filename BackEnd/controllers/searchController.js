/**
 * Controller module.
 * @module controllers/search
 * @requires express
 */
const mysql = require('mysql')
const stemmer = require('porter-stemmer').stemmer


const resultsPerPage = 20
const con = mysql.createConnection({
  host: "localhost",
  user: "nodeuser",
  password: "nodeuser@1234",
  database: "noodle"
})



con.connect((err) => {
  if (err) throw err
  console.log("Connected to mysql db!")
})

/**
 * Search for results
 */
exports.getSearchResults = async (req, res, next) => {
  if (!req.query.word) {
    res.status(400).json({
      status: 'failed'
    })
    return
  }

  var word = stemmer(req.query.word)
  console.log(word)
  var page = 0
  if (req.query.page)
    page = req.query.page
 
  sql = "SELECT * FROM indexedurls where word = " + con.escape(word) + " ORDER BY count desc limit " + page * resultsPerPage + ", " + resultsPerPage


  con.query(sql, (err, result) => {
    if (err)
      throw err
    res.status(200).json({
      status: 'success',
      data: {
        result
      }
    })

  })
  
}