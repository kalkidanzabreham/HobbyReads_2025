const express = require("express");
const router = express.Router();
const connections = require("../controllers/connection.controller");
const { verifyToken } = require("../middleware/auth.middleware");

router.get("/", verifyToken, connections.getAcceptedConnections);

// Get all pending connection requests
router.get("/pending", verifyToken, connections.getPendingConnections);

// Get suggested connections
router.get("/suggested", verifyToken, connections.getSuggestedConnections);

// Send a connection request to a user
router.post("/:userId", verifyToken, connections.sendConnectionRequest);

// Accept a connection request
router.put("/:id/accept", verifyToken, connections.acceptConnection);

// Reject a connection request
router.put("/:id/reject", verifyToken, connections.rejectConnection);

// Delete a connection
router.delete("/:id", verifyToken, connections.deleteConnection);

module.exports = router;
