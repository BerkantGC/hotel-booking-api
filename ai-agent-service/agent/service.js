import axios from 'axios';
import dotenv from 'dotenv';

dotenv.config();

const geminiApiKey = process.env.GEMINI_LLM_API_KEY;
const geminiUrl = `${process.env.GEMINI_LLM_API_URL}?key=${geminiApiKey}`;

// Desteklenen intent listesi
const allowedIntents = {
  search_hotel: { method: 'GET', url: process.env.HOTEL_SEARCH_URL },
  book_hotel: { method: 'POST', url: process.env.HOTEL_BOOK_URL }
};

// Gemini çağrısı
async function callGemini(prompt) {
  try {
    const res = await axios.post(geminiUrl, {
      contents: [{ parts: [{ text: prompt }] }]
    });
    return res.data.candidates[0].content.parts[0].text.trim();
  } catch (err) {
    console.error("Gemini error:", err.response?.data || err.message);
    throw new Error('Gemini LLM failed.');
  }
}

// Kullanıcı mesajını intent'e çevir
async function parseIntent(message) {
  const prompt = `
You are a hotel booking assistant. Your job is to classify user intent.

Possible intents:
1. "search_hotel" → To search hotels
2. "book_hotel" → To book a hotel

If not matching, return {"intent": "none"}

Always respond ONLY in raw JSON like:
{"intent": "search_hotel", "params": {"location": "Rome", "checkIn": "2025-07-15", "checkOut": "2025-07-18", "roomCount": 2}}

User: "${message}"
`;

  const raw = await callGemini(prompt);
  const cleaned = raw.replace(/```json|```/g, '');
  try {
    return JSON.parse(cleaned);
  } catch {
    throw new Error('LLM response is not valid JSON.');
  }
}

async function callIntentAPI(intent, params, headers) {
    const auth = headers['authorization'] || headers['Authorization'];
    if (!auth) {
        return { error: 'Authorization token is required.' };
    }  

    const def = allowedIntents[intent];
    if (!def) return { error: "Unsupported intent." };

    try {
        const config = {
            method: def.method,
            url: def.url,
            headers: {
            'Authorization': auth,
            }
        };

        // For GET requests, add params as query parameters
        if (def.method === 'GET') {
            config.url = `${def.url}?${new URLSearchParams(params).toString()}`;
        } else {
            // For POST requests, add params as request body
            config.data = params;
        }

        console.log(`Calling API for ${intent} with params:`, params);
        console.log(`Request config:`, config);

        const res = await axios(config);
        return res.data;
    } catch (err) {
        console.error(`Error calling API for ${intent}:`, err.response?.data || err.message);
        return { error: 'Internal API call failed.' };
    }
}


// Cevabı Gemini ile açıklat
function explainPrompt(apiResult) {
  return `
You are a hotel booking chatbot. Convert the API result below into a direct, conversational response. Do not include phrases like "here's a description" or "friendly description". Just provide the information directly.

API Result:
${JSON.stringify(apiResult)}

Respond naturally and directly with the hotel information.
`;
}

// Tam işlem
export async function handleMessage(message, headers) {
  const { intent, params } = await parseIntent(message);

  if (intent === 'none') return "Sorry, I couldn't understand that.";

  const apiResult = await callIntentAPI(intent, params, headers);
  const reply = await callGemini(explainPrompt(apiResult));
  return reply;
}
