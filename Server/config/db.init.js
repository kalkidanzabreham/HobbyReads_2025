// Database initialization script

async function initDb() {
  try {
    const pool = global.db;
    console.log("Initializing database...");

    // Create users table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(255) NOT NULL UNIQUE,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        name VARCHAR(255),
        bio TEXT,
        profilePicture VARCHAR(255),
        isAdmin BOOLEAN DEFAULT false,
        createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      )
    `);

    // Create hobbies table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS hobbies (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL UNIQUE,
        createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      )
    `);

    // Create books table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS books (
        id INT AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        author VARCHAR(255) NOT NULL,
        description TEXT,
        coverImage VARCHAR(255),
        ownerId INT NOT NULL,
        status ENUM('Available for Trade', 'Not for Trade','Traded') DEFAULT 'Not for Trade',
        genre VARCHAR(255),
        bookCondition ENUM('Like New', 'Excellent', 'Very Good', 'Good', 'Fair', 'Poor') DEFAULT 'Good',
        createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (ownerId) REFERENCES users(id) ON DELETE CASCADE
      )
    `);

    // Create reviews table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS reviews (
        id INT AUTO_INCREMENT PRIMARY KEY,
        bookId INT NOT NULL,
        userId INT NOT NULL,
        rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
        comment TEXT,
        createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (bookId) REFERENCES books(id) ON DELETE CASCADE,
        FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
      )
    `);
    await pool.query(`
    CREATE TABLE IF NOT EXISTS user_hobbies (
      id INT AUTO_INCREMENT PRIMARY KEY,
      userId INT NOT NULL,
      hobbyId INT NOT NULL,
      createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (hobbyId) REFERENCES hobbies(id) ON DELETE CASCADE,
      UNIQUE KEY user_hobby_unique (userId, hobbyId)
    );
    `);
    await pool.query(`
    CREATE TABLE IF NOT EXISTS connections (
      id INT AUTO_INCREMENT PRIMARY KEY,
      userId INT NOT NULL,
      connectedUserId INT NOT NULL,
      status ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending',
      createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (connectedUserId) REFERENCES users(id) ON DELETE CASCADE,
      UNIQUE KEY connection_unique (userId, connectedUserId)
    );
    `);
    await pool.query(`
    CREATE TABLE IF NOT EXISTS trade_requests (
      id INT AUTO_INCREMENT PRIMARY KEY,
      requesterId INT NOT NULL,
      bookId INT NOT NULL,
      ownerId INT NOT NULL,
      status ENUM('pending', 'accepted', 'rejected', 'cancelled') NOT NULL DEFAULT 'pending',
      message TEXT,
      createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      FOREIGN KEY (requesterId) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (bookId) REFERENCES books(id) ON DELETE CASCADE,
      FOREIGN KEY (ownerId) REFERENCES users(id) ON DELETE CASCADE
    );
   `);

    console.log("Database initialized successfully");
  } catch (error) {
    console.error("Error initializing database:", error);
    throw error;
  }
}

module.exports = initDb;
