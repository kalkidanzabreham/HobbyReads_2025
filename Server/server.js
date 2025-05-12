const express = require("express")
const cors = require("cors")
const path = require("path")
const bodyParser = require("body-parser")
const dotenv = require("dotenv")
const mysql = require("mysql2/promise")



const app = express()

// Middleware
app.use(cors())
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({ extended: true }))







// Simple route for testing
app.get("/", (req, res) => {
  res.json({ message: "Welcome to HobbyReads API" })
})

// Start server
const PORT = process.env.PORT || 8080
app.listen(PORT,"0.0.0.0", () => {
  console.log(`Server is running on port ${PORT}`)
})
