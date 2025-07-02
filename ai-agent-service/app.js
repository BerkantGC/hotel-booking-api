import express from 'express';
import dotenv from 'dotenv';
import { handleMessage } from './agent/service.js';

dotenv.config();

const app = express();
app.use(express.json());

app.post('/ai-agent/chat', async (req, res) => {
  const { message } = req.body;
  if (!message) return res.status(400).json({ error: 'Message is required' });

  try {
    const reply = await handleMessage(message, req.headers);
    res.json({ reply });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 3003;
app.listen(PORT, () => console.log(`AI Agent running on http://localhost:${PORT}`));
