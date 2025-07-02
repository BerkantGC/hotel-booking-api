import express from "express";
import http from "http";
import { Server } from "socket.io";
import cors from "cors";
import dotenv from "dotenv";
import pg from "pg";
import jwt from "jsonwebtoken";

dotenv.config();

// PostgreSQL bağlantısı
const { Client } = pg;
const db = new Client({
  host: process.env.DB_HOST || "localhost",
  port: process.env.DB_PORT || 5432,
  database: process.env.DB_NAME || "hotel_booking",
  user: process.env.DB_USER || "postgres",
  password: process.env.DB_PASSWORD || "password",
});

// Veritabanı bağlantısını başlat
async function connectToDatabase() {
  try {
    await db.connect();
    console.log("PostgreSQL connected successfully");
  } catch (error) {
    console.error("PostgreSQL connection error:", error);
    process.exit(1);
  }
}

connectToDatabase();

// Gizli anahtar kontrolü
const INTERNAL_SECRET = process.env.INTERNAL_SECRET;
const JWT_SECRET = process.env.JWT_SECRET || "your-jwt-secret";

// Token'dan kullanıcı bilgilerini al
async function getUserFromToken(token) {
  try {
    // JWT token'ı doğrula
    const decoded = jwt.verify(token, JWT_SECRET);
    const userId = decoded.userId || decoded.id;

    // Veritabanından kullanıcı bilgilerini getir
    const query = "SELECT id FROM users WHERE id = $1";
    const result = await db.query(query, [userId]);

    if (result.rows.length === 0) {
      throw new Error("User not found");
    }

    return result.rows[0];
  } catch (error) {
    console.error("Token verification or user fetch error:", error);
    throw new Error("Invalid token or user not found");
  }
}

async function getNotificationsByUserId(userId) {
  try {
    console.log(`Fetching notifications for user ID: ${userId}`);
    const query = "SELECT * FROM notifications WHERE user_id = $1";
    const result = await db.query(query, [userId]);
    return result.rows;
  } catch (error) {
    console.error("Error fetching notifications:", error);
    throw new Error("Failed to fetch notifications");
  }
}

// Socket için kullanıcı doğrulama middleware
async function authenticateSocket(socket, next) {
  try {
    const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.replace("Bearer ", "");
    
    if (!token) {
      return next(new Error("Authentication token required"));
    }

    const user = await getUserFromToken(token);
    socket.user = user;
    next();
  } catch (error) {
    next(new Error("Authentication failed"));
  }
}

const app = express();
app.use(cors());
app.use(express.json());

const httpServer = http.createServer(app);

const io = new Server(httpServer, {
  cors: {
    origin: "*",  // Üretimde domain ile sınırla
    methods: ["GET", "POST"]
  }
});

// Socket authentication middleware'ini kullan
io.use(authenticateSocket);

app.post("/api/v1/notify", (req, res) => {
  const secret = req.headers["x-internal-secret"]
  console.log(" secret:", secret, " INTERNAL", INTERNAL_SECRET);
  if (secret !== INTERNAL_SECRET) {
    return res.status(403).json({ error: "Forbidden" });
  }

  const {notification}  = req.body;
  const userId = notification.user.id;

  console.log(`Received notification for user ${userId}:`, notification);

  const userRoom = `user_${userId}`;
  io.to(userRoom).emit("notification", { notification });
  console.log(`Notification sent to user ${userId}: ${notification}`);

  res.json({ status: "ok" });
});

// Token doğrulama endpoint'i
app.get("/api/v1/verify-token", async (req, res) => {
  try {
    const token = req.headers.authorization?.replace("Bearer ", "");
    
    if (!token) {
      return res.status(401).json({ error: "Token required" });
    }

    const user = await getUserFromToken(token);
    res.json({ 
      valid: true, 
      user: {
        id: user.id,
      }
    });
  } catch (error) {
    res.status(401).json({ valid: false, error: error.message });
  }
});

// Socket.IO bağlantısı
io.on("connection", async(socket) => {
  const user = socket.user;
  console.log(`Client connected: ${socket.id}, User: ${user.email} (ID: ${user.id})`);

  // Kullanıcıyı otomatik olarak kendi odasına ekle
  const userRoom = `user_${user.id}`;
  socket.join(userRoom);

  await getNotificationsByUserId(user.id)
  .then(notifications => {
    console.log(`Fetched ${notifications.length} notifications for user ${user.id}`);
    socket.emit("notifications", notifications);
    console.log(`Sent notifications to user ${user.id}`);
  })
  .catch(error => {
    console.error("Error fetching notifications:", error);
    socket.emit("error", { message: "Failed to fetch notifications" });
  });

  socket.on("leave", (userId) => {
    // Güvenlik: kullanıcı sadece kendi odasından ayrılabilir
    if (userId !== user.id) {
      socket.emit("error", { message: "You can only leave your own room" });
      return;
    }
    
    const userRoom = `user_${userId}`;
    socket.leave(userRoom);
    console.log(`Socket ${socket.id} left room for user ${userId}`);
  });

  socket.on("disconnect", () => {
    console.log(`Client disconnected: ${socket.id}, User: ${user.email}`);
  });
});

const PORT = process.env.SOCKET_SERVICE_PORT || process.env.PORT || 3000;
httpServer.listen(PORT, () => {
  console.log(`Socket.IO notification server running on port ${PORT}`);
});
