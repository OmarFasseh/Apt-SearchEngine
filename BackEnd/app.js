const express = require('express')
const app = express()
app.use(express.json()) // to have body to requests specially for post methods

app.use('/public', express.static('./public'))

const searchRouter = require('./routes/searchRoutes')

// Mounting the Routers
app.use('/', searchRouter)
module.exports = app