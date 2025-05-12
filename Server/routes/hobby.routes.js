const express = require("express")
const router = express.Router()
const hobbyController = require("../controllers/hobby.controller")
const { verifyToken, isAdmin } = require("../middleware/auth.middleware")

// Get all hobbies
router.get("/", hobbyController.getAllHobbies)

// Create a new hobby (admin only)
router.post("/", verifyToken, isAdmin, hobbyController.createHobby)

// Update hobby (admin only)
router.put("/:id", verifyToken, isAdmin, hobbyController.updateHobby)

// Delete hobby (admin only)
router.delete("/:id", verifyToken, isAdmin, hobbyController.deleteHobby)

module.exports = router
