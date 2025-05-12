const fs = require("fs");
const path = require("path");

exports.createBook = async (req, res) => {
  try {
    const { title, author, description, status, genre, bookCondition } =
      req.body;
    const ownerId = req.userId;
    const pool = global.db;

    if (!title || !author) {
      return res
        .status(400)
        .send({ message: "Title and author are required!" });
    }

    let coverImageFilename = null;
    if (req.file) {
      const fileExtension = path.extname(req.file.originalname);
      const fileName = `${ownerId}_${Date.now()}${fileExtension}`;

      const uploadDir = path.join(
        __dirname,
        "..",
        "public",
        "uploads",
        "books"
      );
      if (!fs.existsSync(uploadDir)) {
        fs.mkdirSync(uploadDir, { recursive: true });
      }

      const filePath = path.join(uploadDir, fileName);
      fs.writeFileSync(filePath, req.file.buffer);

      coverImageFilename = fileName;
    }

    // Insert book
    const [result] = await pool.query(
      `INSERT INTO books (title, author, description, coverImage, ownerId, status, genre, bookCondition)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        title,
        author,
        description || null,
        coverImageFilename || null,
        ownerId,
        status || null,
        genre || null,
        bookCondition || null
      ]
    );

    const bookId = result.insertId;

    // Fetch created book
    const [books] = await pool.query(
      `SELECT b.*, u.username as ownerUsername, u.name as ownerName
       FROM books b
       JOIN users u ON b.ownerId = u.id
       WHERE b.id = ?`,
      [bookId]
    );

    if (books.length === 0) {
      return res
        .status(404)
        .send({ message: "Book not found after creation." });
    }

    const book = books[0];

    const bookResponse = {
      id: book.id,
      title: book.title,
      author: book.author,
      description: book.description || "",
      coverImage: book.coverImage
        ? `${req.protocol}://${req.get("host")}/uploads/books/${
            book.coverImage
          }`
        : null,
      ownerId: book.ownerId,
      ownerUsername: book.ownerUsername,
      ownerName: book.ownerName,
      status: book.status,
      genre: book.genre,
      bookCondition: book.bookCondition,
      createdAt: book.createdAt,
      updatedAt: book.updatedAt
    };

    res.status(201).send({
      message: "Book created successfully",
      book: bookResponse
    });
  } catch (err) {
    console.error("Error creating book:", err);
    res.status(500).send({
      message: err.message || "Some error occurred while creating the book."
    });
  }
};

// Get all books with optional filters
exports.getAllBooks = async (req, res) => {
  try {
    const { title, author, status, genre, ownerId } = req.query;
    const pool = global.db;

    let query = `
      SELECT b.*, u.username as ownerUsername, u.name as ownerName,
             COALESCE(AVG(r.rating), 0) as averageRating,
             COUNT(r.id) as reviewCount
      FROM books b
      JOIN users u ON b.ownerId = u.id
      LEFT JOIN reviews r ON b.id = r.bookId
    `;

    const queryParams = [];
    const conditions = [];

    if (title) {
      conditions.push("b.title LIKE ?");
      queryParams.push(`%${title}%`);
    }

    if (author) {
      conditions.push("b.author LIKE ?");
      queryParams.push(`%${author}%`);
    }

    if (status) {
      conditions.push("b.status = ?");
      queryParams.push(status);
    }

    if (genre) {
      conditions.push("b.genre = ?");
      queryParams.push(genre);
    }

    if (ownerId) {
      conditions.push("b.ownerId = ?");
      queryParams.push(ownerId);
    }

    if (conditions.length > 0) {
      query += " WHERE " + conditions.join(" AND ");
    }

    query += " GROUP BY b.id ORDER BY b.createdAt DESC";

    const [books] = await pool.query(query, queryParams);

    res.status(200).send(books);
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while retrieving books."
    });
  }
};

// Get book by ID
exports.getBookById = async (req, res) => {
  try {
    const bookId = req.params.id;
    const pool = global.db;

    // Get book with owner info and average rating
    const [books] = await pool.query(
      `SELECT b.*, u.username as ownerUsername, u.name as ownerName,
              COALESCE(AVG(r.rating), 0) as averageRating,
              COUNT(r.id) as reviewCount
       FROM books b
       JOIN users u ON b.ownerId = u.id
       LEFT JOIN reviews r ON b.id = r.bookId
       WHERE b.id = ?
       GROUP BY b.id`,
      [bookId]
    );

    if (books.length === 0) {
      return res.status(404).send({
        message: "Book not found."
      });
    }

    const book = books[0];

    // Get reviews for the book
    const [reviews] = await pool.query(
      `SELECT r.*, u.username, u.name, u.profilePicture
       FROM reviews r
       JOIN users u ON r.userId = u.id
       WHERE r.bookId = ?
       ORDER BY r.createdAt DESC`,
      [bookId]
    );

    book.reviews = reviews;

    res.status(200).send(book);
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while retrieving book."
    });
  }
};

// Update book
exports.updateBook = async (req, res) => {
  try {
    const bookId = req.params.id;
    const {
      title,
      author,
      description,
      coverImage,
      status,
      genre,
      bookCondition
    } = req.body;
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Check if user is the owner of the book
    const [books] = await pool.query("SELECT ownerId FROM books WHERE id = ?", [
      bookId
    ]);

    if (books.length === 0) {
      return res.status(404).send({
        message: "Book not found."
      });
    }

    if (books[0].ownerId !== userId) {
      return res.status(403).send({
        message: "You can only update your own books!"
      });
    }

    // Update book
    await pool.query(
      `UPDATE books
       SET title = ?, author = ?, description = ?, coverImage = ?,
           status = ?, genre = ?, condition = ?
       WHERE id = ?`,
      [
        title,
        author,
        description || null,
        coverImage || null,
        status || "Not for Trade",
        genre || null,
        bookCondition || "Good",
        bookId
      ]
    );

    res.status(200).send({
      message: "Book updated successfully"
    });
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while updating the book."
    });
  }
};

// Delete book
exports.deleteBook = async (req, res) => {
  try {
    const bookId = req.params.id;
    const userId = req.userId; // From auth middleware
    const pool = global.db;

    // Check if user is the owner of the book or an admin
    const [books] = await pool.query("SELECT ownerId FROM books WHERE id = ?", [
      bookId
    ]);

    if (books.length === 0) {
      return res.status(404).send({
        message: "Book not found."
      });
    }

    // Check if user is admin
    const [users] = await pool.query("SELECT isAdmin FROM users WHERE id = ?", [
      userId
    ]);

    const isAdmin = users.length > 0 && users[0].isAdmin;

    if (books[0].ownerId !== userId && !isAdmin) {
      return res.status(403).send({
        message: "You can only delete your own books!"
      });
    }

    // Delete book
    await pool.query("DELETE FROM books WHERE id = ?", [bookId]);

    res.status(200).send({
      message: "Book deleted successfully"
    });
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while deleting the book."
    });
  }
};

// GET /api/books/my
exports.getMyBooks = async (req, res) => {
  try {
    const userId = req.userId; // extracted from token by auth middleware

    const pool = global.db;
    const [books] = await pool.query(
      `SELECT b.*, u.username as ownerUsername, u.name as ownerName
       FROM books b
       JOIN users u ON b.ownerId = u.id
       WHERE b.ownerId = ?`,
      [userId]
    );

    res.status(200).send(books);
  } catch (err) {
    res.status(500).send({
      message: err.message || "Some error occurred while fetching your books."
    });
  }
};
