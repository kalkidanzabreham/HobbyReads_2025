// Create a new review
exports.createReview = async (req, res) => {
    try {
      const { bookId, rating, comment } = req.body
      const userId = req.userId // From auth middleware
  
      // Validate request
      if (!bookId || !rating) {
        return res.status(400).send({
          message: "Book ID and rating are required!",
        })
      }
  
      if (rating < 1 || rating > 5) {
        return res.status(400).send({
          message: "Rating must be between 1 and 5!",
        })
      }
  
      const pool = global.db
  
      // Check if book exists
      const [books] = await pool.query("SELECT * FROM books WHERE id = ?", [bookId])
  
      if (books.length === 0) {
        return res.status(404).send({
          message: "Book not found.",
        })
      }
  
      // Check if user has already reviewed this book
      const [existingReviews] = await pool.query("SELECT * FROM reviews WHERE bookId = ? AND userId = ?", [
        bookId,
        userId,
      ])
  
      if (existingReviews.length > 0) {
        return res.status(400).send({
          message: "You have already reviewed this book. You can update your existing review instead.",
        })
      }
  
      // Create review
      const [result] = await pool.query("INSERT INTO reviews (bookId, userId, rating, comment) VALUES (?, ?, ?, ?)", [
        bookId,
        userId,
        rating,
        comment || null,
      ])
  
      const reviewId = result.insertId
  
      // Get the created review with user info
      const [reviews] = await pool.query(
        `SELECT r.*, u.username, u.name, u.profilePicture
         FROM reviews r
         JOIN users u ON r.userId = u.id
         WHERE r.id = ?`,
        [reviewId],
      )
  
      if (reviews.length === 0) {
        return res.status(404).send({
          message: "Review not found after creation.",
        })
      }
  
      res.status(201).send({
        message: "Review created successfully",
        review: reviews[0],
      })
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while creating the review.",
      })
    }
  }
  
  // Get reviews for a book
  exports.getBookReviews = async (req, res) => {
    try {
      const bookId = req.params.bookId
      const pool = global.db
  
      // Check if book exists
      const [books] = await pool.query("SELECT * FROM books WHERE id = ?", [bookId])
  
      if (books.length === 0) {
        return res.status(404).send({
          message: "Book not found.",
        })
      }
  
      // Get reviews with user info
      const [reviews] = await pool.query(
        `SELECT r.*, u.username, u.name, u.profilePicture
         FROM reviews r
         JOIN users u ON r.userId = u.id
         WHERE r.bookId = ?
         ORDER BY r.createdAt DESC`,
        [bookId],
      )
  
      res.status(200).send(reviews)
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while retrieving reviews.",
      })
    }
  }
  
  // Update review
  exports.updateReview = async (req, res) => {
    try {
      const reviewId = req.params.id
      const { rating, comment } = req.body
      const userId = req.userId // From auth middleware
  
      // Validate request
      if (!rating) {
        return res.status(400).send({
          message: "Rating is required!",
        })
      }
  
      if (rating < 1 || rating > 5) {
        return res.status(400).send({
          message: "Rating must be between 1 and 5!",
        })
      }
  
      const pool = global.db
  
      // Check if review exists and belongs to the user
      const [reviews] = await pool.query("SELECT * FROM reviews WHERE id = ?", [reviewId])
  
      if (reviews.length === 0) {
        return res.status(404).send({
          message: "Review not found.",
        })
      }
  
      if (reviews[0].userId !== userId) {
        return res.status(403).send({
          message: "You can only update your own reviews!",
        })
      }
  
      // Update review
      await pool.query("UPDATE reviews SET rating = ?, comment = ? WHERE id = ?", [rating, comment || null, reviewId])
  
      res.status(200).send({
        message: "Review updated successfully",
      })
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while updating the review.",
      })
    }
  }
  
  // Delete review
  exports.deleteReview = async (req, res) => {
    try {
      const reviewId = req.params.id
      const userId = req.userId // From auth middleware
      const pool = global.db
  
      // Check if review exists and belongs to the user
      const [reviews] = await pool.query("SELECT * FROM reviews WHERE id = ?", [reviewId])
  
      if (reviews.length === 0) {
        return res.status(404).send({
          message: "Review not found.",
        })
      }
  
      // Check if user is admin
      const [users] = await pool.query("SELECT isAdmin FROM users WHERE id = ?", [userId])
  
      const isAdmin = users.length > 0 && users[0].isAdmin
  
      if (reviews[0].userId !== userId && !isAdmin) {
        return res.status(403).send({
          message: "You can only delete your own reviews!",
        })
      }
  
      // Delete review
      await pool.query("DELETE FROM reviews WHERE id = ?", [reviewId])
  
      res.status(200).send({
        message: "Review deleted successfully",
      })
    } catch (err) {
      res.status(500).send({
        message: err.message || "Some error occurred while deleting the review.",
      })
    }
  }
  