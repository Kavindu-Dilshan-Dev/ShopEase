/* ═══════════════════════════════════════════
   FIREBASE CONFIG — ShopEase Admin
   Replace YOUR_* values with your actual
   Firebase project values from:
   Firebase Console → Project Settings → Your apps
   ═══════════════════════════════════════════ */

// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyDHkNgOjMKVNPRVO5yl6t73nZOx00yLc8k",
  authDomain: "shopease-4f4b8.firebaseapp.com",
  projectId: "shopease-4f4b8",
  storageBucket: "shopease-4f4b8.firebasestorage.app",
  messagingSenderId: "1020611543958",
  appId: "1:1020611543958:web:b2177eb9d3f1cca4b953b7",
  measurementId: "G-681X7YC15N"
};

// ── Firestore Collection Names ────────────────
// These MUST match your Android app collections exactly
const COLLECTIONS = {
  products:      'products',
  orders:        'orders',
  users:         'users',
  notifications: 'notifications',
  promotions:    'promotions'
};

// ── Product Categories ────────────────────────
// These MUST match your Android app SearchFragment categories
const CATEGORIES = [
  'Electronics',
  'Clothing',
  'Food',
  'Books',
  'Sports',
  'Beauty'
];

// ── Firestore REST API Base URL ───────────────
function getFirestoreBase() {
  return `https://firestore.googleapis.com/v1/projects/shopease-4f4b8/databases/(default)/documents`;
}
