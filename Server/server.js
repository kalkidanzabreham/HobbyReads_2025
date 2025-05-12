const express = require("express");
const cors = require("cors");
const path = require("path");
const bodyParser = require("body-parser");
const dotenv = require("dotenv");
const mysql = require("mysql2/promise");
const dbConfig = require("./config/db.config");

// Load environment variables
dotenv.config();

// Create MySQL connection pool using the config
const pool = mysql.createPool(dbConfig);

// Make the pool available globally
global.db = pool;

const app = express();

// Initialize database
const initDb = require("./config/db.init");
initDb().catch((err) => {
  console.error("Database initialization failed:", err);
  process.exit(1);
});

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// import routes
const authRoutes = require("./routes/auth.routes");
const userRoutes = require("./routes/user.routes");
const bookRoutes = require("./routes/book.routes");

// Use routes
app.use("/api/auth", authRoutes);
app.use("/api/users", userRoutes);
app.use("/api/books", bookRoutes);

// Simple route for testing
app.get("/", (req, res) => {
  res.json({ message: "Welcome to HobbyReads API" });
});

// Start server
const PORT = process.env.PORT || 8080;
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server is running on port ${PORT}`);
});
