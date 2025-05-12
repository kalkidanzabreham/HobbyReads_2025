// Get all users (admin only)
const fs = require("fs");
const path = require("path");
const bcrypt = require("bcryptjs");

// Get current user profile
exports.getCurrentUser = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Get user data
    const [users] = await pool.query(
      `SELECT id, username, email, name, bio, profilePicture, createdAt, updatedAt
       FROM users
       WHERE id = ?`,
      [userId]
    );

    if (users.length === 0) {
      return res.status(404).send({
        message: "User not found."
      });
    }

    const user = users[0];

    // Get user hobbies
    const [hobbies] = await pool.query(
      `SELECT h.name
       FROM user_hobbies uh
       JOIN hobbies h ON uh.hobbyId = h.id
       WHERE uh.userId = ?`,
      [userId]
    );

    // Format response
    const userResponse = {
      id: user.id,
      username: user.username,
      email: user.email,
      name: user.name,
      bio: user.bio || "",
      profilePicture: user.profilePicture
        ? `${req.protocol}://${req.get("host")}/uploads/profiles/${
            user.profilePicture
          }`
        : null,
      hobbies: hobbies.map((h) => h.name),
      createdAt: user.createdAt,
      updatedAt: user.updatedAt
    };

    res.status(200).send(userResponse);
  } catch (err) {
    res.status(500).send({
      message:
        err.message || "Some error occurred while retrieving user profile."
    });
  }
};

// Update user profile
exports.updateProfile = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    const { name, bio } = req.body;
    let hobbies = [];

    // DEBUG: Log raw incoming hobbies
    // console.log("Incoming hobbies raw:", req.body.hobbies)

    // Parse hobbies safely
    if (req.body.hobbies) {
      try {
        const hobbiesData = JSON.parse(req.body.hobbies);
        if (Array.isArray(hobbiesData)) {
          hobbies = hobbiesData;
        } else {
          console.warn("Hobbies is not an array. Ignoring.");
        }
      } catch (e) {
        console.error("Error parsing hobbies JSON:", e);
      }
    }

    // console.log("Parsed hobbies array:", hobbies)

    if (!name) {
      return res.status(400).send({
        message: "Name is required!"
      });
    }

    // Start transaction
    const connection = await pool.getConnection();
    await connection.beginTransaction();

    try {
      let profilePicturePath = null;
      if (req.file) {
        const fileExtension = path.extname(req.file.originalname);
        const fileName = `${userId}_${Date.now()}${fileExtension}`;

        const uploadDir = path.join(
          __dirname,
          "..",
          "public",
          "uploads",
          "profiles"
        );
        if (!fs.existsSync(uploadDir)) {
          fs.mkdirSync(uploadDir, { recursive: true });
        }

        const filePath = path.join(uploadDir, fileName);
        fs.writeFileSync(filePath, req.file.buffer);

        profilePicturePath = fileName;

        // Delete old profile picture
        const [oldUser] = await connection.query(
          "SELECT profilePicture FROM users WHERE id = ?",
          [userId]
        );
        if (oldUser.length > 0 && oldUser[0].profilePicture) {
          const oldFilePath = path.join(uploadDir, oldUser[0].profilePicture);
          if (fs.existsSync(oldFilePath)) {
            fs.unlinkSync(oldFilePath);
          }
        }
      }

      // Update user profile
      const updateFields = [];
      const updateValues = [];

      updateFields.push("name = ?");
      updateValues.push(name);

      if (bio !== undefined) {
        updateFields.push("bio = ?");
        updateValues.push(bio);
      }

      if (profilePicturePath) {
        updateFields.push("profilePicture = ?");
        updateValues.push(profilePicturePath);
      }

      updateFields.push("updatedAt = NOW()");
      updateValues.push(userId);

      await connection.query(
        `UPDATE users SET ${updateFields.join(", ")} WHERE id = ?`,
        updateValues
      );

      // Handle hobbies update
      if (hobbies.length > 0) {
        await connection.query("DELETE FROM user_hobbies WHERE userId = ?", [
          userId
        ]);

        for (const hobbyName of hobbies) {
          let hobbyId;
          const [existingHobbies] = await connection.query(
            "SELECT id FROM hobbies WHERE name = ?",
            [hobbyName]
          );

          if (existingHobbies.length > 0) {
            hobbyId = existingHobbies[0].id;
          } else {
            const [result] = await connection.query(
              "INSERT INTO hobbies (name) VALUES (?)",
              [hobbyName]
            );
            hobbyId = result.insertId;
          }

          await connection.query(
            "INSERT INTO user_hobbies (userId, hobbyId) VALUES (?, ?)",
            [userId, hobbyId]
          );
        }
      }

      // Commit transaction
      await connection.commit();

      // Fetch updated user info
      const [users] = await pool.query(
        `SELECT id, username, email, name, bio, profilePicture, createdAt, updatedAt
         FROM users
         WHERE id = ?`,
        [userId]
      );

      // ✅ Fetch hobbies with id and name
      const [updatedHobbies] = await pool.query(
        `SELECT h.id, h.name
         FROM user_hobbies uh
         JOIN hobbies h ON uh.hobbyId = h.id
         WHERE uh.userId = ?`,
        [userId]
      );

      const userResponse = {
        id: users[0].id,
        username: users[0].username,
        email: users[0].email,
        name: users[0].name,
        bio: users[0].bio || "",
        profilePicture: users[0].profilePicture
          ? `${req.protocol}://${req.get("host")}/uploads/profiles/${
              users[0].profilePicture
            }`
          : null,
        hobbies: updatedHobbies.map((h) => ({ id: h.id, name: h.name })), // ✅ Send as [{id, name}, ...]
        createdAt: users[0].createdAt,
        updatedAt: users[0].updatedAt
      };

      res.status(200).send(userResponse);
      // console.log(userResponse)
    } catch (err) {
      await connection.rollback();
      console.error("Error inside transaction:", err);
      res
        .status(500)
        .send({ message: err.message || "Failed to update profile." });
    } finally {
      connection.release();
    }
  } catch (err) {
    console.error("Outer catch error:", err);
    res.status(500).send({
      message: err.message || "Some error occurred while updating user profile."
    });
  }
};

// Change password
exports.changePassword = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const { currentPassword, newPassword } = req.body;
    const pool = global.db;

    // Validate request
    if (!currentPassword || !newPassword) {
      return res.status(400).send({
        message: "Current password and new password are required!"
      });
    }

    // Get user with password
    const [users] = await pool.query(
      "SELECT password FROM users WHERE id = ?",
      [userId]
    );

    if (users.length === 0) {
      return res.status(404).send({
        message: "User not found."
      });
    }

    // Verify current password
    const passwordIsValid = bcrypt.compareSync(
      currentPassword,
      users[0].password
    );

    if (!passwordIsValid) {
      return res.status(401).send({
        message: "Current password is incorrect!"
      });
    }

    // Hash new password
    const hashedPassword = bcrypt.hashSync(newPassword, 10);

    // Update password
    await pool.query(
      "UPDATE users SET password = ?, updatedAt = NOW() WHERE id = ?",
      [hashedPassword, userId]
    );

    res.status(200).send({
      message: "Password changed successfully!"
    });
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while changing password."
    });
  }
};

// Delete user account
exports.deleteAccount = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const { password } = req.body;
    const pool = global.db;

    // Validate request
    if (!password) {
      return res.status(400).send({
        message: "Password is required to delete account!"
      });
    }

    // Get user with password
    const [users] = await pool.query(
      "SELECT password, profilePicture FROM users WHERE id = ?",
      [userId]
    );

    if (users.length === 0) {
      return res.status(404).send({
        message: "User not found."
      });
    }

    // Verify password
    const passwordIsValid = bcrypt.compareSync(password, users[0].password);

    if (!passwordIsValid) {
      return res.status(401).send({
        message: "Password is incorrect!"
      });
    }

    // Start a transaction
    const connection = await pool.getConnection();
    await connection.beginTransaction();

    try {
      // Delete user hobbies
      await connection.query("DELETE FROM user_hobbies WHERE userId = ?", [
        userId
      ]);

      // Delete user connections
      await connection.query(
        "DELETE FROM connections WHERE userId = ? OR connectedUserId = ?",
        [userId, userId]
      );

      // Delete user
      await connection.query("DELETE FROM users WHERE id = ?", [userId]);

      // Delete profile picture if exists
      if (users[0].profilePicture) {
        const filePath = path.join(
          __dirname,
          "..",
          "public",
          "uploads",
          "profiles",
          users[0].profilePicture
        );
        if (fs.existsSync(filePath)) {
          fs.unlinkSync(filePath);
        }
      }

      // Commit transaction
      await connection.commit();

      res.status(200).send({
        message: "Account deleted successfully!"
      });
    } catch (err) {
      // Rollback transaction on error
      await connection.rollback();
      throw err;
    } finally {
      connection.release();
    }
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while deleting account."
    });
  }
};

exports.getAllUsers = async (req, res) => {
  try {
    const pool = global.db;

    const [users] = await pool.query(
      "SELECT id, username, email, name, bio, profilePicture, isAdmin, createdAt FROM users"
    );

    // Get hobbies for each user
    for (const user of users) {
      const [hobbies] = await pool.query(
        `SELECT h.id, h.name
         FROM hobbies h
         JOIN user_hobbies uh ON h.id = uh.hobbyId
         WHERE uh.userId = ?`,
        [user.id]
      );
      user.hobbies = hobbies;
    }

    res.status(200).send(users);
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while retrieving users."
    });
  }
};

// Get user by ID
exports.getUserById = async (req, res) => {
  try {
    const userId = req.params.id;
    const pool = global.db;

    const [users] = await pool.query(
      "SELECT id, username, email, name, bio, profilePicture, isAdmin, createdAt FROM users WHERE id = ?",
      [userId]
    );

    if (users.length === 0) {
      return res.status(404).send({
        message: "User not found."
      });
    }

    // Get user hobbies
    const [hobbies] = await pool.query(
      `SELECT h.id, h.name
       FROM hobbies h
       JOIN user_hobbies uh ON h.id = uh.hobbyId
       WHERE uh.userId = ?`,
      [userId]
    );

    const user = {
      ...users[0],
      hobbies: hobbies
    };

    res.status(200).send(user);
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while retrieving user."
    });
  }
};

// Update user profile
exports.updateUser = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const { name, bio, hobbies, profilePicture } = req.body; // Added profilePicture to request body
    const pool = global.db;

    // Update user info (name, bio)
    await pool.query("UPDATE users SET name = ?, bio = ? WHERE id = ?", [
      name,
      bio,
      userId
    ]);

    // Update profile picture if provided (this can be a URL or a direct path to an image)
    if (profilePicture) {
      await pool.query("UPDATE users SET profilePicture = ? WHERE id = ?", [
        profilePicture,
        userId
      ]);
    }

    // Update hobbies if provided
    if (hobbies && Array.isArray(hobbies)) {
      // Delete existing user-hobby associations
      await pool.query("DELETE FROM user_hobbies WHERE userId = ?", [userId]);

      // Add new hobbies
      for (const hobbyName of hobbies) {
        // Find or create hobby
        let hobbyId;
        const [existingHobbies] = await pool.query(
          "SELECT id FROM hobbies WHERE name = ?",
          [hobbyName]
        );

        if (existingHobbies.length > 0) {
          hobbyId = existingHobbies[0].id;
        } else {
          const [newHobby] = await pool.query(
            "INSERT INTO hobbies (name) VALUES (?)",
            [hobbyName]
          );
          hobbyId = newHobby.insertId;
        }

        // Associate hobby with user
        await pool.query(
          "INSERT INTO user_hobbies (userId, hobbyId) VALUES (?, ?)",
          [userId, hobbyId]
        );
      }
    }

    // Get updated hobbies in the desired format
    const [updatedHobbies] = await pool.query(
      `SELECT h.id, h.name FROM hobbies h
       JOIN user_hobbies uh ON h.id = uh.hobbyId
       WHERE uh.userId = ?`,
      [userId]
    );

    res.status(200).send({
      message: "User profile updated successfully.",
      hobbies: updatedHobbies, // Sends [{id: 1, name: "Fantasy"}, ...]
      profilePicture: profilePicture || null // Sends the updated profile picture URL if available
    });
    console.log({
      message: "User profile updated successfully.",
      hobbies: updatedHobbies, // Sends [{id: 1, name: "Fantasy"}, ...]
      profilePicture: profilePicture || null
    });
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while updating user profile."
    });
  }
};

// Delete user (admin only or own account)
exports.deleteUser = async (req, res) => {
  try {
    const userId = req.params.id;
    const requesterId = req.userId; // From auth middleware
    const pool = global.db;

    // Check if user is admin or deleting own account
    const [users] = await pool.query("SELECT isAdmin FROM users WHERE id = ?", [
      requesterId
    ]);

    if (users.length === 0) {
      return res.status(404).send({
        message: "User not found."
      });
    }

    const isAdmin = users[0].isAdmin;

    if (requesterId !== Number.parseInt(userId) && !isAdmin) {
      return res.status(403).send({
        message: "You can only delete your own account!"
      });
    }

    // Delete user
    await pool.query("DELETE FROM users WHERE id = ?", [userId]);

    res.status(200).send({
      message: "User deleted successfully."
    });
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while deleting user."
    });
  }
};
exports.deleteUserById = async (req, res) => {
  try {
    const userIdToDelete = req.params.id; // The user ID from URL param
    const requesterId = req.userId; // Authenticated user ID from auth middleware
    const pool = global.db;

    // Check if requester exists and get isAdmin status
    const [users] = await pool.query("SELECT isAdmin FROM users WHERE id = ?", [
      requesterId
    ]);

    if (users.length === 0) {
      return res.status(404).send({ message: "Requester user not found." });
    }

    const isAdmin = users[0].isAdmin;

    // Only allow if requester is admin OR deleting their own account
    if (requesterId !== Number.parseInt(userIdToDelete) && !isAdmin) {
      return res.status(403).send({
        message: "You can only delete your own account or you must be admin!"
      });
    }

    // Check if user to delete exists
    const [targetUser] = await pool.query("SELECT id FROM users WHERE id = ?", [
      userIdToDelete
    ]);
    if (targetUser.length === 0) {
      return res.status(404).send({ message: "User to delete not found." });
    }

    // Delete user
    await pool.query("DELETE FROM users WHERE id = ?", [userIdToDelete]);

    res.status(200).send({ message: "User deleted successfully." });
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while deleting user."
    });
  }
};

// Get suggested users based on hobbies
exports.getSuggestedUsers = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Get current user's hobbies
    const [userHobbies] = await pool.query(
      "SELECT hobbyId FROM user_hobbies WHERE userId = ?",
      [userId]
    );

    if (userHobbies.length === 0) {
      return res.status(200).send([]);
    }

    const hobbyIds = userHobbies.map((h) => h.hobbyId);

    // Find users with similar hobbies
    const [suggestedUsers] = await pool.query(
      `SELECT DISTINCT u.id, u.username, u.name, u.bio, u.profilePicture,
              COUNT(DISTINCT uh.hobbyId) as matchingHobbies
       FROM users u
       JOIN user_hobbies uh ON u.id = uh.userId
       WHERE uh.hobbyId IN (?) AND u.id != ?
       GROUP BY u.id
       ORDER BY matchingHobbies DESC
       LIMIT 10`,
      [hobbyIds, userId]
    );

    // Get hobbies for each suggested user
    for (const user of suggestedUsers) {
      const [hobbies] = await pool.query(
        `SELECT h.id, h.name
         FROM hobbies h
         JOIN user_hobbies uh ON h.id = uh.hobbyId
         WHERE uh.userId = ?`,
        [user.id]
      );

      // Calculate match percentage
      const matchPercentage = Math.round(
        (user.matchingHobbies / hobbyIds.length) * 100
      );

      user.hobbies = hobbies;
      user.matchPercentage = matchPercentage;
      delete user.matchingHobbies;
    }

    res.status(200).send(suggestedUsers);
  } catch (err) {
    res.status(500).send({
      message:
        err.message || "Some error occurred while retrieving suggested users."
    });
  }
};
