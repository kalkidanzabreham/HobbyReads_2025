const express = require("express");
const router = express.Router();
const multer = require("multer");
const storage = multer.memoryStorage();
const upload = multer({
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024 // 5MB limit
  }
});
const userController = require("../controllers/user.controller");
const { verifyToken, isAdmin } = require("../middleware/auth.middleware");

// Get all users (admin only)
router.get("/", verifyToken, isAdmin, userController.getAllUsers);

// Get user by ID
router.get("/:id", verifyToken, userController.getUserById);

// Delete user (admin only or own account)
router.delete("/:id", verifyToken, userController.deleteUserById);

router.delete("/delete-account", verifyToken, userController.deleteAccount);

// Get suggested users based on hobbies
router.get("/suggested/users", verifyToken, userController.getSuggestedUsers);

// Get current user profile
router.get("/me", verifyToken, userController.getCurrentUser);

// Update user profile
router.put(
  "/profile",
  upload.single("profilePicture"),
  verifyToken,
  userController.updateProfile
);

// Change password
router.put("/change-password", verifyToken, userController.changePassword);

// Delete account
router.delete("/delete-account", verifyToken, userController.deleteAccount);

module.exports = router;
