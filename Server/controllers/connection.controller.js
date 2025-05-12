// Get all accepted connections for current user
exports.getAcceptedConnections = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Get only accepted connections where user is either requester or recipient
    const [connections] = await pool.query(
      `SELECT c.*,
              u1.username as requesterUsername, u1.name as requesterName, u1.profilePicture as requesterProfilePicture,
              u2.username as recipientUsername, u2.name as recipientName, u2.profilePicture as recipientProfilePicture,
              u.id as otherUserId, u.username as username, u.name as name, u.bio as bio, u.profilePicture
       FROM connections c
       JOIN users u1 ON c.userId = u1.id
       JOIN users u2 ON c.connectedUserId = u2.id
       JOIN users u ON (c.userId = ? AND u.id = c.connectedUserId) OR (c.connectedUserId = ? AND u.id = c.userId)
       WHERE (c.userId = ? OR c.connectedUserId = ?) AND c.status = 'accepted'
       ORDER BY c.updatedAt DESC`,
      [userId, userId, userId, userId]
    );

    // Format the response to match the expected Connection model
    const formattedConnections = await Promise.all(
      connections.map(async (conn) => {
        // Determine if current user is the requester or recipient
        const isRequester = conn.userId === userId;

        // Get hobbies for the other user
        const [hobbies] = await pool.query(
          `SELECT h.name FROM user_hobbies uh
         JOIN hobbies h ON uh.hobbyId = h.id
         WHERE uh.userId = ?`,
          [conn.otherUserId]
        );

        // Calculate match percentage based on shared hobbies
        const [userHobbies] = await pool.query(
          `SELECT h.name FROM user_hobbies uh
         JOIN hobbies h ON uh.hobbyId = h.id
         WHERE uh.userId = ?`,
          [userId]
        );

        const otherUserHobbies = hobbies.map((h) => h.name);
        const currentUserHobbies = userHobbies.map((h) => h.name);

        // Simple match calculation - percentage of shared hobbies
        const sharedHobbies = otherUserHobbies.filter((h) =>
          currentUserHobbies.includes(h)
        );
        const matchPercentage = Math.round(
          (sharedHobbies.length /
            Math.max(
              1,
              Math.max(otherUserHobbies.length, currentUserHobbies.length)
            )) *
            100
        );

        return {
          id: conn.id,
          userId: isRequester ? conn.userId : conn.connectedUserId,
          connectedUserId: isRequester ? conn.connectedUserId : conn.userId,
          status: conn.status,
          name: conn.name,
          username: conn.username,
          bio: conn.bio || "",
          hobbies: otherUserHobbies,
          matchPercentage: matchPercentage,
          createdAt: conn.createdAt,
          updatedAt: conn.updatedAt
        };
      })
    );

    res.status(200).send(formattedConnections);
  } catch (err) {
    res.status(500).send({
      message:
        err.message ||
        "Some error occurred while retrieving accepted connections."
    });
  }
};

// Get all pending connections for current user
exports.getPendingConnections = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Get only pending connections where user is the recipient
    const [connections] = await pool.query(
      `SELECT c.*,
              u.id as requesterId, u.username, u.name, u.bio, u.profilePicture
       FROM connections c
       JOIN users u ON c.userId = u.id
       WHERE c.connectedUserId = ? AND c.status = 'pending'
       ORDER BY c.createdAt DESC`,
      [userId]
    );

    // Format the response to match the expected Connection model
    const formattedConnections = await Promise.all(
      connections.map(async (conn) => {
        // Get hobbies for the requester
        const [hobbies] = await pool.query(
          `SELECT h.name FROM user_hobbies uh
         JOIN hobbies h ON uh.hobbyId = h.id
         WHERE uh.userId = ?`,
          [conn.requesterId]
        );

        // Calculate match percentage based on shared hobbies
        const [userHobbies] = await pool.query(
          `SELECT h.name FROM user_hobbies uh
         JOIN hobbies h ON uh.hobbyId = h.id
         WHERE uh.userId = ?`,
          [userId]
        );

        const requesterHobbies = hobbies.map((h) => h.name);
        const currentUserHobbies = userHobbies.map((h) => h.name);

        // Simple match calculation - percentage of shared hobbies
        const sharedHobbies = requesterHobbies.filter((h) =>
          currentUserHobbies.includes(h)
        );
        const matchPercentage = Math.round(
          (sharedHobbies.length /
            Math.max(
              1,
              Math.max(requesterHobbies.length, currentUserHobbies.length)
            )) *
            100
        );

        return {
          id: conn.id,
          userId: conn.userId,
          connectedUserId: conn.connectedUserId,
          status: conn.status,
          name: conn.name,
          username: conn.username,
          bio: conn.bio || "",
          hobbies: requesterHobbies,
          matchPercentage: matchPercentage,
          createdAt: conn.createdAt,
          updatedAt: conn.updatedAt
        };
      })
    );

    res.status(200).send(formattedConnections);
  } catch (err) {
    res.status(500).send({
      message:
        err.message ||
        "Some error occurred while retrieving pending connections."
    });
  }
};

// Get suggested connections for current user
exports.getSuggestedConnections = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Get users who are not already connected with the current user
    const [users] = await pool.query(
      `SELECT u.id, u.username, u.name, u.bio, u.profilePicture
       FROM users u
       WHERE u.id != ? AND u.id NOT IN (
         SELECT IF(c.userId = ?, c.connectedUserId, c.userId)
         FROM connections c
         WHERE c.userId = ? OR c.connectedUserId = ?
       )
       LIMIT 20`,
      [userId, userId, userId, userId]
    );

    // Format the response to match the expected Connection model
    const formattedSuggestions = await Promise.all(
      users.map(async (user) => {
        // Get hobbies for the suggested user
        const [hobbies] = await pool.query(
          `SELECT h.name FROM user_hobbies uh
         JOIN hobbies h ON uh.hobbyId = h.id
         WHERE uh.userId = ?`,
          [user.id]
        );

        // Calculate match percentage based on shared hobbies
        const [userHobbies] = await pool.query(
          `SELECT h.name FROM user_hobbies uh
         JOIN hobbies h ON uh.hobbyId = h.id
         WHERE uh.userId = ?`,
          [userId]
        );

        const suggestedUserHobbies = hobbies.map((h) => h.name);
        const currentUserHobbies = userHobbies.map((h) => h.name);

        // Simple match calculation - percentage of shared hobbies
        const sharedHobbies = suggestedUserHobbies.filter((h) =>
          currentUserHobbies.includes(h)
        );
        const matchPercentage = Math.round(
          (sharedHobbies.length /
            Math.max(
              1,
              Math.max(suggestedUserHobbies.length, currentUserHobbies.length)
            )) *
            100
        );

        return {
          id: 0, // No connection ID yet
          userId: user.id,
          connectedUserId: 0, // No connection yet
          status: "suggested",
          name: user.name,
          username: user.username,
          bio: user.bio || "",
          hobbies: suggestedUserHobbies,
          matchPercentage: matchPercentage
        };
      })
    );

    // Sort by match percentage (highest first)
    formattedSuggestions.sort((a, b) => b.matchPercentage - a.matchPercentage);

    res.status(200).send(formattedSuggestions);
  } catch (err) {
    res.status(500).send({
      message:
        err.message ||
        "Some error occurred while retrieving suggested connections."
    });
  }
};

// Accept a connection request
exports.acceptConnection = async (req, res) => {
  try {
    const connectionId = req.params.id;
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Check if connection exists and user is the recipient
    const [connections] = await pool.query(
      "SELECT * FROM connections WHERE id = ?",
      [connectionId]
    );

    if (connections.length === 0) {
      return res.status(404).send({
        message: "Connection not found."
      });
    }

    if (connections[0].connectedUserId !== userId) {
      return res.status(403).send({
        message: "You can only accept connection requests sent to you!"
      });
    }

    if (connections[0].status !== "pending") {
      return res.status(400).send({
        message: "This connection request has already been processed."
      });
    }

    // Update connection status to accepted
    await pool.query(
      "UPDATE connections SET status = 'accepted', updatedAt = NOW() WHERE id = ?",
      [connectionId]
    );

    // Get the updated connection with user info
    const [updatedConnections] = await pool.query(
      `SELECT c.*,
              u1.username as requesterUsername, u1.name as requesterName, u1.bio as requesterBio,
              u1.profilePicture as requesterProfilePicture
       FROM connections c
       JOIN users u1 ON c.userId = u1.id
       WHERE c.id = ?`,
      [connectionId]
    );

    // Get hobbies for the requester
    const [hobbies] = await pool.query(
      `SELECT h.name FROM user_hobbies uh
       JOIN hobbies h ON uh.hobbyId = h.id
       WHERE uh.userId = ?`,
      [updatedConnections[0].userId]
    );

    // Calculate match percentage
    const [userHobbies] = await pool.query(
      `SELECT h.name FROM user_hobbies uh
       JOIN hobbies h ON uh.hobbyId = h.id
       WHERE uh.userId = ?`,
      [userId]
    );

    const requesterHobbies = hobbies.map((h) => h.name);
    const currentUserHobbies = userHobbies.map((h) => h.name);

    const sharedHobbies = requesterHobbies.filter((h) =>
      currentUserHobbies.includes(h)
    );
    const matchPercentage = Math.round(
      (sharedHobbies.length /
        Math.max(
          1,
          Math.max(requesterHobbies.length, currentUserHobbies.length)
        )) *
        100
    );

    // Format the response
    const formattedConnection = {
      id: updatedConnections[0].id,
      userId: updatedConnections[0].userId,
      connectedUserId: updatedConnections[0].connectedUserId,
      status: updatedConnections[0].status,
      name: updatedConnections[0].requesterName,
      username: updatedConnections[0].requesterUsername,
      bio: updatedConnections[0].requesterBio || "",
      hobbies: requesterHobbies,
      matchPercentage: matchPercentage,
      createdAt: updatedConnections[0].createdAt,
      updatedAt: updatedConnections[0].updatedAt
    };

    res.status(200).send(formattedConnection);
  } catch (err) {
    res.status(500).send({
      message:
        err.message || "Some error occurred while accepting the connection."
    });
  }
};

// Reject a connection request
exports.rejectConnection = async (req, res) => {
  try {
    const connectionId = req.params.id;
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Check if connection exists and user is the recipient
    const [connections] = await pool.query(
      "SELECT * FROM connections WHERE id = ?",
      [connectionId]
    );

    if (connections.length === 0) {
      return res.status(404).send({
        message: "Connection not found."
      });
    }

    if (connections[0].connectedUserId !== userId) {
      return res.status(403).send({
        message: "You can only reject connection requests sent to you!"
      });
    }

    if (connections[0].status !== "pending") {
      return res.status(400).send({
        message: "This connection request has already been processed."
      });
    }

    // Update connection status to rejected
    await pool.query(
      "UPDATE connections SET status = 'rejected', updatedAt = NOW() WHERE id = ?",
      [connectionId]
    );

    // Get the updated connection with user info
    const [updatedConnections] = await pool.query(
      `SELECT c.*,
              u1.username as requesterUsername, u1.name as requesterName, u1.bio as requesterBio,
              u1.profilePicture as requesterProfilePicture
       FROM connections c
       JOIN users u1 ON c.userId = u1.id
       WHERE c.id = ?`,
      [connectionId]
    );

    // Get hobbies for the requester
    const [hobbies] = await pool.query(
      `SELECT h.name FROM user_hobbies uh
       JOIN hobbies h ON uh.hobbyId = h.id
       WHERE uh.userId = ?`,
      [updatedConnections[0].userId]
    );

    // Format the response
    const formattedConnection = {
      id: updatedConnections[0].id,
      userId: updatedConnections[0].userId,
      connectedUserId: updatedConnections[0].connectedUserId,
      status: updatedConnections[0].status,
      name: updatedConnections[0].requesterName,
      username: updatedConnections[0].requesterUsername,
      bio: updatedConnections[0].requesterBio || "",
      hobbies: hobbies.map((h) => h.name),
      matchPercentage: 0, // Not relevant for rejected connections
      createdAt: updatedConnections[0].createdAt,
      updatedAt: updatedConnections[0].updatedAt
    };

    res.status(200).send(formattedConnection);
  } catch (err) {
    res.status(500).send({
      message:
        err.message || "Some error occurred while rejecting the connection."
    });
  }
};

// Send a connection request (renamed from createConnection for consistency)
exports.sendConnectionRequest = async (req, res) => {
  try {
    const userId = req.userId; // From auth middleware
    const connectedUserId = req.params.userId; // From URL parameter

    // Validate request
    if (!connectedUserId) {
      return res.status(400).send({
        message: "Connected user ID is required!"
      });
    }

    if (userId === Number.parseInt(connectedUserId)) {
      return res.status(400).send({
        message: "You cannot connect with yourself!"
      });
    }

    const pool = global.db;

    // Check if connected user exists
    const [users] = await pool.query("SELECT * FROM users WHERE id = ?", [
      connectedUserId
    ]);

    if (users.length === 0) {
      return res.status(404).send({
        message: "User not found."
      });
    }

    // Check if connection already exists
    const [existingConnections] = await pool.query(
      `SELECT * FROM connections
       WHERE (userId = ? AND connectedUserId = ?)
          OR (userId = ? AND connectedUserId = ?)`,
      [userId, connectedUserId, connectedUserId, userId]
    );

    if (existingConnections.length > 0) {
      return res.status(400).send({
        message: "Connection already exists!"
      });
    }

    // Create connection
    const [result] = await pool.query(
      "INSERT INTO connections (userId, connectedUserId, status) VALUES (?, ?, 'pending')",
      [userId, connectedUserId]
    );

    const connectionId = result.insertId;

    // Get the created connection with user info
    const [connections] = await pool.query(
      `SELECT c.*,
              u2.username, u2.name, u2.bio, u2.profilePicture
       FROM connections c
       JOIN users u2 ON c.connectedUserId = u2.id
       WHERE c.id = ?`,
      [connectionId]
    );

    if (connections.length === 0) {
      return res.status(404).send({
        message: "Connection not found after creation."
      });
    }

    // Get hobbies for the recipient
    const [hobbies] = await pool.query(
      `SELECT h.name FROM user_hobbies uh
       JOIN hobbies h ON uh.hobbyId = h.id
       WHERE uh.userId = ?`,
      [connectedUserId]
    );

    // Calculate match percentage
    const [userHobbies] = await pool.query(
      `SELECT h.name FROM user_hobbies uh
       JOIN hobbies h ON uh.hobbyId = h.id
       WHERE uh.userId = ?`,
      [userId]
    );

    const recipientHobbies = hobbies.map((h) => h.name);
    const currentUserHobbies = userHobbies.map((h) => h.name);

    const sharedHobbies = recipientHobbies.filter((h) =>
      currentUserHobbies.includes(h)
    );
    const matchPercentage = Math.round(
      (sharedHobbies.length /
        Math.max(
          1,
          Math.max(recipientHobbies.length, currentUserHobbies.length)
        )) *
        100
    );

    // Format the response
    const formattedConnection = {
      id: connections[0].id,
      userId: connections[0].userId,
      connectedUserId: connections[0].connectedUserId,
      status: connections[0].status,
      name: connections[0].name,
      username: connections[0].username,
      bio: connections[0].bio || "",
      hobbies: recipientHobbies,
      matchPercentage: matchPercentage,
      createdAt: connections[0].createdAt,
      updatedAt: connections[0].updatedAt
    };

    res.status(201).send(formattedConnection);
  } catch (err) {
    res.status(500).send({
      message:
        err.message || "Some error occurred while creating the connection."
    });
  }
};
exports.deleteConnection = async (req, res) => {
  try {
    const connectionId = req.params.id; // get from URL params
    const userId = req.userId; // get from auth middleware
    const pool = global.db;

    const [connections] = await pool.query(
      "SELECT * FROM connections WHERE id = ?",
      [connectionId]
    );

    if (connections.length === 0) {
      return res.status(404).send({
        message: "Connection not found."
      });
    }

    if (
      connections[0].userId !== userId &&
      connections[0].connectedUserId !== userId
    ) {
      return res.status(403).send({
        message: "You can only delete your own connections!"
      });
    }

    // Delete connection
    await pool.query("DELETE FROM connections WHERE id = ?", [connectionId]);

    res.status(200).send({
      message: "Connection deleted successfully"
    });
  } catch (err) {
    console.log(err);
    res.status(500).send({
      message:
        err.message || "Some error occurred while deleting the connection."
    });
  }
};
