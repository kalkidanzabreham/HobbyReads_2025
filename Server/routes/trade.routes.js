const express = require("express");
const router = express.Router();
const trades = require("../controllers/trade.controller");
const { verifyToken } = require("../middleware/auth.middleware");

router.post("/", verifyToken, trades.createTradeRequest);

router.put("/:id", verifyToken, trades.updateTradeRequestStatus);

router.get("/pending", verifyToken, trades.getPendingTradeRequests);
router.get("/user/:id", trades.getAcceptedTradeRequests);

module.exports = router;
