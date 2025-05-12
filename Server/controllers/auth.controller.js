const bcrypt = require("bcryptjs")
const jwt = require("jsonwebtoken")

// Register a new user
exports.register = async (req, res) => {
  try {
    const { username, email, password, name, bio, hobbies } = req.body;

    // Validate request
    if (!username || !email || !password) {
      return res.status(400).send({
        message: "Username, email, and password are required!",
      });
    }

    const pool = global.db;

    // Check if username or email already exists
    const [existingUsers] = await pool.query("SELECT * FROM users WHERE username = ? OR email = ?", [username, email]);

    if (existingUsers.length > 0) {
      return res.status(400).send({
        message: "Username or email already in use!",
      });
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    // Check if this is the first user (admin)
    const [rows] = await pool.query('SELECT COUNT(*) as count FROM users');
    const isAdmin = rows[0].count === 0 ? true : false; // If no users, make this one admin

    // Insert new user with isAdmin status
    const [result] = await pool.query(
      "INSERT INTO users (username, email, password, name, bio, isAdmin) VALUES (?, ?, ?, ?, ?, ?)",
      [username, email, hashedPassword, name || null, bio || null, isAdmin]
    );

    const userId = result.insertId;

    // Associate hobbies if provided
    if (hobbies && hobbies.length > 0) {
      for (const hobbyName of hobbies) {
        // Find or create hobby
        let hobbyId;
        const [existingHobbies] = await pool.query("SELECT id FROM hobbies WHERE name = ?", [hobbyName]);

        if (existingHobbies.length > 0) {
          hobbyId = existingHobbies[0].id;
        } else {
          const [newHobby] = await pool.query("INSERT INTO hobbies (name) VALUES (?)", [hobbyName]);
          hobbyId = newHobby.insertId;
        }

        // Associate hobby with user
        await pool.query("INSERT INTO user_hobbies (userId, hobbyId) VALUES (?, ?)", [userId, hobbyId]);
      }
    }

    // Get user data with hobbies
    const [userData] = await pool.query(
      "SELECT id, username, email, name, bio, profilePicture, isAdmin, createdAt FROM users WHERE id = ?",
      [userId]
    );

    const [userHobbies] = await pool.query(
      `SELECT h.id, h.name
       FROM hobbies h
       JOIN user_hobbies uh ON h.id = uh.hobbyId
       WHERE uh.userId = ?`,
      [userId]
    );

    const user = {
      ...userData[0],
      hobbies: userHobbies,
    };

    // Generate JWT token with user id and isAdmin status
    const token = jwt.sign({ id: userId, isAdmin: user.isAdmin }, process.env.JWT_SECRET || "hobbyreads-secret-key", { expiresIn: "24h" });

    // Return user info and token
    res.status(201).send({
      message: "Registration successful",
      token: token,
      user: user,
    });
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred during registration.",
    });
  }
};



// Login user
exports.login = async (req, res) => {
  try {
    const { username, email, password } = req.body;

    // Validate request
    if (!username && !email) {
      return res.status(400).send({
        message: "Username or email is required!",
      });
    }

    if (!password) {
      return res.status(400).send({
        message: "Password is required!",
      });
    }

    const pool = global.db;

    // Find user by username or email
    const [users] = await pool.query("SELECT * FROM users WHERE username = ? OR email = ?", [
      username || "",
      email || "",
    ]);

    if (users.length === 0) {
      return res.status(404).send({
        message: "User not found.",
      });
    }

    const user = users[0];

    // Check password
    const validPassword = await bcrypt.compare(password, user.password);
    if (!validPassword) {
      return res.status(401).send({
        message: "Invalid password!",
      });
    }

    // Get user hobbies
    const [hobbies] = await pool.query(
      `SELECT h.id, h.name
       FROM hobbies h
       JOIN user_hobbies uh ON h.id = uh.hobbyId
       WHERE uh.userId = ?`,
      [user.id]
    );

    // Generate JWT token with user id and isAdmin status
    const token = jwt.sign({ id: user.id, isAdmin: user.isAdmin }, process.env.JWT_SECRET || "hobbyreads-secret-key", { expiresIn: "24h" });

    // Return user info (without password) and token
    const { password: _, ...userWithoutPassword } = user;

    res.status(200).send({
      message: "Login successful",
      token: token,
      user: {
        ...userWithoutPassword,
        hobbies: hobbies,
      },
    });
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred during login.",
    });
  }
};

// Get current user profile
exports.getUserProfile = async (req, res) => {
    try {
      const pool = global.db
  
      // Get user data
      const [users] = await pool.query(
        "SELECT id, username, email, name, bio, profilePicture, isAdmin, createdAt, updatedAt FROM users WHERE id = ?",
        [req.userId],
      )
  
      if (users.length === 0) {
        return res.status(404).send({
          message: "User not found.",
        })
      }
  
      // Get user hobbies (with id and name)
      const [hobbies] = await pool.query(
        `SELECT h.id, h.name FROM hobbies h
         JOIN user_hobbies uh ON h.id = uh.hobbyId
         WHERE uh.userId = ?`,
        [req.userId],
      )
  
      // Build the response user object
      const user = {
        id: users[0].id,
        username: users[0].username,
        email: users[0].email,
        name: users[0].name,
        bio: users[0].bio || "",
        profilePicture: users[0].profilePicture
          ? `${req.protocol}://${req.get("host")}/uploads/profiles/${users[0].profilePicture}`
          : null,
        isAdmin: users[0].isAdmin,
        createdAt: users[0].createdAt,
        updatedAt: users[0].updatedAt,
        hobbies: hobbies  // ✅ Now returns array of {id, name}
      }
  
      res.status(200).send(user)
      // console.log(user)
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while retrieving user profile.",
      })
    }
  }