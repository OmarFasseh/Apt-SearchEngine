/**
 * Controller module.
 * @module controllers/search
 * @requires express
 */
const resultsPerPage = 20

const mysql = require('mysql')
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
  }

  const word = (req.query.word)
  const page = (req.query.page)
  sql = "SELECT * FROM indexedurls where word = " + con.escape(word) + " ORDER BY count desc limit " + page * resultsPerPage + ", " + resultsPerPage


  results = null
  con.query(sql, (err, result) => {
    if (err)
      throw err
    console.log("Result: ")
    results = result
    Object.keys(result).forEach(function (key) {
      var row = result[key]
      console.log(row.url)
    })
  })
  
  res.status(200).json({
    status: 'success',
    data: {
      results
    }
  })
}