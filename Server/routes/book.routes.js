const express = require("express");
const multer = require("multer");
const router = express.Router();
const bookController = require("../controllers/book.controller");
const { verifyToken } = require("../middleware/auth.middleware");
const storage = multer.memoryStorage();
const bookUpload = multer({
  storage: storage,
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB limit
  }
});

// Create a new book
router.post(
  "/",
  verifyToken,
  bookUpload.single("coverImage"),
  bookController.createBook
);

// Get all books with optional filters
router.get("/", bookController.getAllBooks);

router.get("/my", verifyToken, bookController.getMyBooks);

// Update book
router.put("/:id", verifyToken, bookController.updateBook);

// Delete book
router.delete("/:id", verifyToken, bookController.deleteBook);

// Get book by ID
router.get("/:id", bookController.getBookById);

module.exports = router;
