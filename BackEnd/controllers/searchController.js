/**
 * Controller module.
 * @module controllers/search
 * @requires express
 */
const mysql = require('mysql')
const stemmer = require('porter-stemmer').stemmer
const util = require('util')

const resultsPerPage = 20

//Connection to MYSQL DB
const con = mysql.createConnection({
  host: "localhost",
  port: '3306',
  user: "nodeuser",
  password: "nodeuser@1234",
  database: "noodle"
})

con.connect((err) => {
  if (err) throw err
  console.log("Connected to mysql db!")
})
const query = util.promisify(con.query).bind(con)

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

  //Get the search word, remove all non-alphanumeric characters, then stem
  var word = req.query.word
  word = word.replace(/[^\w]/gi, '')
  await (word = word.toLowerCase())
  const wordBeforeStemming = word
  word = stemmer(word)
  
  var page = 0
  if (req.query.page)
    page = req.query.page
  console.log('here')
  //Search for the word
  sqlSearch = "SELECT * FROM indexedurls where word = " + con.escape(word) + " ORDER BY tf desc limit " + page * resultsPerPage + ", " + resultsPerPage
  sqlCount = "SELECT COUNT(*) as numberOfURLs from indexedurls where word = " + con.escape(word)
  console.log('here1')
  
  let count,result;
  try{
    count = await query(sqlCount)
    result = await query(sqlSearch)
  }catch(e){
    console.log(e)
  }
  
  console.log('here2')

  //Add the word to search history
  sqlSearchHistory = "SELECT count FROM searchedWords where word = '" + wordBeforeStemming+ "'";
  const wordFreqQuery = await query(sqlSearchHistory)
  var wordCount = 0
  if (wordFreqQuery[0])
    wordCount = wordFreqQuery[0]["count"]

  if (!wordCount) wordCount = 0
  wordCount += 1
  console.log('here3')

  sqlSearchFreqAdd = "INSERT INTO searchedWords (word, count) VALUES('" + wordBeforeStemming + "'," + wordCount + ") on duplicate key update count=" + wordCount
  await query(sqlSearchFreqAdd)
  console.log('here4')

  res.status(200).json({
    status: 'success',
    data: {
      "count" : count[0]["numberOfURLs"],
      "URLsPerPage" : resultsPerPage,
      result
    }
  })

  
}


/**
 * Search suggestions
 */
 exports.getSearchSuggestions = async (req, res, next) => {
  if (!req.query.word) {
    res.status(400).json({
      status: 'failed'
    })
    return
  }

  //Get the search word, remove all non-alphanumeric characters, then stem
  var word = req.query.word
  word = word.replace(/[^\w]/gi, '')
  await (word = word.toLowerCase())
  
  //Search for the partial word
  sqlSearch = "SELECT word FROM searchedWords where word like '%" + word + "%' order by count desc LIMIT 10 "
  console.log(sqlSearch)
  const result = await query(sqlSearch)


  res.status(200).json({
    status: 'success',
    data: {
      result
    }
  })

  
}
