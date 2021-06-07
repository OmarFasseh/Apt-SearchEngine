const express = require('express')
const app = express()
const cors = require('cors')
app.use(express.json()) // to have body to requests specially for post methods
app.use(cors())
app.use('/public', express.static('./public'))

const searchRouter = require('./routes/searchRoutes')

// Mounting the Routers
app.use('/', searchRouter)
module.exports = app