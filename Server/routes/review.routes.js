const express = require("express")
const router = express.Router()
const reviewController = require("../controllers/review.controller")
const { verifyToken } = require("../middleware/auth.middleware")

// Create a new review
router.post("/", verifyToken, reviewController.createReview)

// Get reviews for a book
router.get("/book/:bookId", reviewController.getBookReviews)

// Update review
router.put("/:id", verifyToken, reviewController.updateReview)

// Delete review
router.delete("/:id", verifyToken, reviewController.deleteReview)

module.exports = router
