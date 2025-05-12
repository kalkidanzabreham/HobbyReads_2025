const jwt = require("jsonwebtoken")

const verifyToken = (req, res, next) => {
  const token = req.headers["x-access-token"] || req.headers["authorization"]

  if (!token) {
    return res.status(403).send({
      message: "No token provided!",
    })
  }

  // Remove Bearer prefix if present
  const tokenValue = token.startsWith("Bearer ") ? token.slice(7) : token

  try {
    const decoded = jwt.verify(tokenValue, process.env.JWT_SECRET || "hobbyreads-secret-key")
    req.userId = decoded.id
    next()
  } catch (err) {
    return res.status(401).send({
      message: "Unauthorized!",
    })
  }
}

const isAdmin = async (req, res, next) => {
  try {
    const [rows] = await global.db.query("SELECT isAdmin FROM users WHERE id = ?", [req.userId])

    if (rows.length === 0) {
      return res.status(404).send({
        message: "User not found.",
      })
    }

    if (!rows[0].isAdmin) {
      return res.status(403).send({
        message: "Require Admin Role!",
      })
    }

    next()
  } catch (err) {
    return res.status(500).send({
      message: err.message || "Some error occurred while checking admin status.",
    })
  }
}

module.exports = {
  verifyToken,
  isAdmin,
}
