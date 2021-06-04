const express = require('express')
const app = require('./app')
const port = 8000;



const server = app.listen(port, () => {
  console.log(`App is running on port ${port}`)
})

/*
app.get('/', (req, res) => {
  res.send('Hello World!')
});

app.listen(port, () => {
  console.log(`Example app listening on port ${port}!`)
});
*/