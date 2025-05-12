exports.getPendingTradeRequests = async (req, res) => {
  try {
    const userId = req.userId;
    const pool = global.db;

    const [tradeRequests] = await pool.query(
      `SELECT 
      tr.id, tr.requesterId, tr.bookId, tr.ownerId, tr.status, tr.message, tr.createdAt, tr.updatedAt,
      b.title as bookTitle, b.author as bookAuthor, b.coverImage as bookCoverUrl,
      requester.username as requesterUsername, requester.name as requesterName,
      owner.username as ownerUsername, owner.name as ownerName
      FROM trade_requests tr
      JOIN books b ON tr.bookId = b.id
      JOIN users requester ON tr.requesterId = requester.id
      JOIN users owner ON tr.ownerId = owner.id
      WHERE (tr.requesterId = ? OR tr.ownerId = ?) AND tr.status = 'pending'
      ORDER BY tr.createdAt DESC`,
      [userId, userId]
    );

    const formattedRequests = tradeRequests.map((request) => {
      const isIncoming = request.ownerId === userId;

      return {
        id: request.id,
        requesterId: request.requesterId,
        bookId: request.bookId,
        ownerId: request.ownerId,
        status: request.status,
        message: request.message,
        createdAt: request.createdAt,
        updatedAt: request.updatedAt,
        book: {
          id: request.bookId,
          title: request.bookTitle,
          author: request.bookAuthor,
          coverImageUrl: request.bookCoverUrl
            ? `${req.protocol}://${req.get("host")}/uploads/books/${
                request.bookCoverUrl
              }`
            : null
        },
        requester: {
          id: request.requesterId,
          username: request.requesterUsername,
          name: request.requesterName
        },
        owner: {
          id: request.ownerId,
          username: request.ownerUsername,
          name: request.ownerName
        },
        type: isIncoming ? "INCOMING" : "OUTGOING"
      };
    });

    res.status(200).send(formattedRequests);
    // console.log(formattedRequests)
  } catch (err) {
    res.status(500).send({
      message:
        err.message || "Some error occurred while retrieving trade requests."
    });
  }
};

// Create trade request (same, no change)
exports.createTradeRequest = async (req, res) => {
  try {
    const userId = req.userId;
    const pool = global.db;

    const { bookId, message } = req.body;

    if (!bookId) {
      return res.status(400).send({ message: "Book ID is required!" });
    }

    const [books] = await pool.query(
      "SELECT id, ownerId, status FROM books WHERE id = ?",
      [bookId]
    );

    if (books.length === 0) {
      return res.status(404).send({ message: "Book not found!" });
    }

    const book = books[0];

    if (book.status !== "Available for Trade") {
      return res
        .status(400)
        .send({ message: "This book is not available for trade!" });
    }

    if (book.ownerId === userId) {
      return res
        .status(400)
        .send({ message: "You cannot request to trade your own book!" });
    }

    const [existingRequests] = await pool.query(
      "SELECT id FROM trade_requests WHERE bookId = ? AND requesterId = ? AND status = 'pending'",
      [bookId, userId]
    );

    if (existingRequests.length > 0) {
      return res
        .status(400)
        .send({ message: "You already have a pending request for this book!" });
    }

    const [result] = await pool.query(
      "INSERT INTO trade_requests (requesterId, bookId, ownerId, message) VALUES (?, ?, ?, ?)",
      [userId, bookId, book.ownerId, message || null]
    );

    const [tradeRequests] = await pool.query(
      `SELECT 
        tr.id, tr.requesterId, tr.bookId, tr.ownerId, tr.status, tr.message, tr.createdAt, tr.updatedAt,
        b.title as bookTitle, b.author as bookAuthor, b.coverImage as bookCoverUrl,
        requester.username as requesterUsername, requester.name as requesterName,
        owner.username as ownerUsername, owner.name as ownerName
      FROM trade_requests tr
      JOIN books b ON tr.bookId = b.id
      JOIN users requester ON tr.requesterId = requester.id
      JOIN users owner ON tr.ownerId = owner.id
      WHERE tr.id = ?`,
      [result.insertId]
    );

    if (tradeRequests.length === 0) {
      return res
        .status(500)
        .send({ message: "Failed to retrieve the created trade request." });
    }

    const request = tradeRequests[0];

    const formattedRequest = {
      id: request.id,
      requesterId: request.requesterId,
      bookId: request.bookId,
      ownerId: request.ownerId,
      status: request.status,
      message: request.message,
      createdAt: request.createdAt,
      updatedAt: request.updatedAt,
      book: {
        id: request.bookId,
        title: request.bookTitle,
        author: request.bookAuthor,
        coverImageUrl: request.bookCoverUrl
          ? `${req.protocol}://${req.get("host")}/uploads/books/${
              request.bookCoverUrl
            }`
          : null
      },
      requester: {
        id: request.requesterId,
        username: request.requesterUsername,
        name: request.requesterName
      },
      owner: {
        id: request.ownerId,
        username: request.ownerUsername,
        name: request.ownerName
      },
      type: "outgoing"
    };

    res.status(201).send(formattedRequest);
  } catch (err) {
    res.status(500).send({
      message:
        err.message || "Some error occurred while creating the trade request."
    });
  }
};

// ✅ Fixed version: Update trade request status
exports.updateTradeRequestStatus = async (req, res) => {
  try {
    const userId = req.userId;
    const tradeId = req.params.id;
    const { status } = req.body;

    if (!status || !["accepted", "rejected", "cancelled"].includes(status)) {
      return res.status(400).send({
        message: "Valid status (accepted, rejected, or cancelled) is required!"
      });
    }

    const pool = global.db;

    const [tradeRequests] = await pool.query(
      "SELECT * FROM trade_requests WHERE id = ?",
      [tradeId]
    );

    if (tradeRequests.length === 0) {
      return res.status(404).send({ message: "Trade request not found!" });
    }

    const tradeRequest = tradeRequests[0];

    if (
      tradeRequest.requesterId !== userId &&
      tradeRequest.ownerId !== userId
    ) {
      return res.status(403).send({
        message: "You are not authorized to update this trade request!"
      });
    }

    // Owner only accepts/rejects
    if (
      (status === "accepted" || status === "rejected") &&
      tradeRequest.ownerId !== userId
    ) {
      return res.status(403).send({
        message: "Only the book owner can accept or reject trade requests!"
      });
    }

    // Requester only cancels
    if (status === "cancelled" && tradeRequest.requesterId !== userId) {
      return res
        .status(403)
        .send({ message: "Only the requester can cancel trade requests!" });
    }

    // If accepted → mark book as traded + cancel all other pending requests
    if (status === "accepted") {
      await pool.query("UPDATE books SET status = 'Traded' WHERE id = ?", [
        tradeRequest.bookId
      ]);

      // ❗Cancel all other pending trade requests for this book
      await pool.query(
        "UPDATE trade_requests SET status = 'cancelled' WHERE bookId = ? AND status = 'pending' AND id != ?",
        [tradeRequest.bookId, tradeId]
      );
    }

    // Update this trade request
    await pool.query("UPDATE trade_requests SET status = ? WHERE id = ?", [
      status,
      tradeId
    ]);

    // Get updated record
    const [updatedRequests] = await pool.query(
      `SELECT 
        tr.id, tr.requesterId, tr.bookId, tr.ownerId, tr.status, tr.message, tr.createdAt, tr.updatedAt,
        b.title as bookTitle, b.author as bookAuthor, b.coverImage as bookCoverUrl,
        requester.username as requesterUsername, requester.name as requesterName,
        owner.username as ownerUsername, owner.name as ownerName
      FROM trade_requests tr
      JOIN books b ON tr.bookId = b.id
      JOIN users requester ON tr.requesterId = requester.id
      JOIN users owner ON tr.ownerId = owner.id
      WHERE tr.id = ?`,
      [tradeId]
    );

    if (updatedRequests.length === 0) {
      return res
        .status(500)
        .send({ message: "Failed to retrieve the updated trade request." });
    }

    const request = updatedRequests[0];
    const isIncoming = request.ownerId === userId;

    const formattedRequest = {
      id: request.id,
      requesterId: request.requesterId,
      bookId: request.bookId,
      ownerId: request.ownerId,
      status: request.status,
      message: request.message,
      createdAt: request.createdAt,
      updatedAt: request.updatedAt,
      book: {
        id: request.bookId,
        title: request.bookTitle,
        author: request.bookAuthor,
        coverImageUrl: request.bookCoverUrl
          ? `${req.protocol}://${req.get("host")}/uploads/books/${
              request.bookCoverUrl
            }`
          : null
      },
      requester: {
        id: request.requesterId,
        username: request.requesterUsername,
        name: request.requesterName
      },
      owner: {
        id: request.ownerId,
        username: request.ownerUsername,
        name: request.ownerName
      },
      type: isIncoming ? "incoming" : "outgoing"
    };

    res.status(200).send(formattedRequest);
  } catch (err) {
    res.status(500).send({
      message:
        err.message || "Some error occurred while updating the trade request."
    });
  }
};

exports.getAcceptedTradeRequests = async (req, res) => {
  try {
    const userId = req.params.id;
    const pool = global.db;
    console.log(userId);
    const [tradeRequests] = await pool.query(
      `SELECT 
      tr.id, tr.requesterId, tr.bookId, tr.ownerId, tr.status, tr.message, tr.createdAt, tr.updatedAt,
      b.title as bookTitle, b.author as bookAuthor, b.coverImage as bookCoverUrl,
      requester.username as requesterUsername, requester.name as requesterName,
      owner.username as ownerUsername, owner.name as ownerName
      FROM trade_requests tr
      JOIN books b ON tr.bookId = b.id
      JOIN users requester ON tr.requesterId = requester.id
      JOIN users owner ON tr.ownerId = owner.id
      WHERE (tr.requesterId = ? OR tr.ownerId = ?) AND tr.status = 'accepted'
      ORDER BY tr.createdAt DESC`,
      [userId, userId]
    );

    const formattedRequests = tradeRequests.map((request) => {
      const isIncoming = request.ownerId === userId;

      return {
        id: request.id,
        requesterId: request.requesterId,
        bookId: request.bookId,
        ownerId: request.ownerId,
        status: request.status,
        message: request.message,
        createdAt: request.createdAt,
        updatedAt: request.updatedAt,
        book: {
          id: request.bookId,
          title: request.bookTitle,
          author: request.bookAuthor,
          coverImageUrl: request.bookCoverUrl
            ? `${req.protocol}://${req.get("host")}/uploads/books/${
                request.bookCoverUrl
              }`
            : null
        },
        requester: {
          id: request.requesterId,
          username: request.requesterUsername,
          name: request.requesterName
        },
        owner: {
          id: request.ownerId,
          username: request.ownerUsername,
          name: request.ownerName
        },
        type: isIncoming ? "INCOMING" : "OUTGOING"
      };
    });

    res.status(200).send(formattedRequests);
    console.log(formattedRequests);
  } catch (err) {
    res.status(500).send({
      message:
        err.message ||
        "Some error occurred while retrieving accepted trade requests."
    });
  }
};
