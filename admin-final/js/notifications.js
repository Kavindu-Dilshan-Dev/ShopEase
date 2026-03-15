/* ═══════════════════════════════════════════
   NOTIFICATIONS JS
   Saves to Firestore + optionally sends FCM push
   ═══════════════════════════════════════════ */

buildSidebar('notifications');

// ── State ────────────────────────────────────
let notifHistory  = JSON.parse(
  localStorage.getItem('se_notif_history') || '[]');
let currentType   = 'general';
let currentTarget = 'all';
let histFilter    = 'all';

// ── FCM Topic mapping ─────────────────────────
// These match the topics Android app subscribes to
const FCM_TOPICS = {
  all:     '/topics/all_users',
  android: '/topics/android_users',
  premium: '/topics/premium_users',
  promo:   '/topics/promo_users',
};

// ── Type & Target Setters ─────────────────────
function setType(type, btn) {
  currentType = type;
  document.querySelectorAll('.type-tab')
    .forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
}

function setTarget(target, label) {
  currentTarget = target;
  document.querySelectorAll('.target-opt')
    .forEach(l => l.classList.remove('active-opt'));
  label.classList.add('active-opt');
}

// ── Live Preview ──────────────────────────────
function updatePreview() {
  document.getElementById('prevTitle').textContent =
    document.getElementById('notifTitle').value.trim() ||
    'Notification Title';
  document.getElementById('prevBody').textContent =
    document.getElementById('notifBody').value.trim() ||
    'Your message will appear here…';
}

function updateCount(inputId, countId, max) {
  document.getElementById(countId).textContent =
    document.getElementById(inputId).value.length;
}

// ── Templates ─────────────────────────────────
const TEMPLATES = {
  sale: {
    title: '🔥 Flash Sale — 24 Hours Only!',
    body:  'Up to 60% off all categories! Use code FLASH60 at checkout. Hurry — ends midnight!'
  },
  order: {
    title: '✅ Order Confirmed!',
    body:  'Great news! Your order has been confirmed and is being prepared for delivery.'
  },
  shipped: {
    title: '🚚 Your Order is On the Way!',
    body:  'Your package is out for delivery! Track it in the ShopEase app.'
  },
  welcome: {
    title: '👋 We Miss You!',
    body:  "It's been a while! Come back and discover new arrivals + 15% welcome back discount."
  },
  review: {
    title: '⭐ Enjoying ShopEase?',
    body:  'Please take 30 seconds to leave us a review. Your feedback means a lot!'
  },
  promo: {
    title: '🎁 Exclusive Offer Inside',
    body:  "You've been selected for an exclusive offer! Open the app to claim your discount."
  },
};

function applyTemplate(key) {
  const t = TEMPLATES[key];
  document.getElementById('notifTitle').value = t.title;
  document.getElementById('notifBody').value  = t.body;
  updatePreview();
  document.getElementById('titleCount').textContent =
    t.title.length;
  document.getElementById('bodyCount').textContent =
    t.body.length;
}

// ── Send Notification ─────────────────────────
async function sendNotification() {
  const title     = document.getElementById('notifTitle').value.trim();
  const body      = document.getElementById('notifBody').value.trim();
  const action    = document.getElementById('notifAction').value;
  const serverKey = document.getElementById('fcmServerKey').value.trim();

  if (!title) { showToast('Please enter a title', 'error'); return; }
  if (!body)  { showToast('Please enter a message', 'error'); return; }

  const btn = document.getElementById('btnSend');
  btn.disabled = true;
  document.getElementById('sendBtnText').textContent = 'Sending…';

  let fcmSent        = false;
  let firestoreSaved = false;

  // ── Step 1: Send real FCM push (if server key given) ──
  if (serverKey) {
    try {
      const fcmBody = {
        to: FCM_TOPICS[currentTarget],
        notification: { title, body },
        data: {
          type:         currentType,
          action:       action,
          click_action: 'FLUTTER_NOTIFICATION_CLICK',
        },
        android: {
          priority:     'high',
          notification: {
            channel_id: currentType + '_channel'
          },
        },
      };

      const res    = await fetch(
        'https://fcm.googleapis.com/fcm/send', {
          method:  'POST',
          headers: {
            'Content-Type':  'application/json',
            'Authorization': `key=${serverKey}`,
          },
          body: JSON.stringify(fcmBody),
        });
      const result = await res.json();

      if (result.failure === 0 || result.message_id) {
        fcmSent = true;
      }
    } catch (err) {
      console.warn('FCM send error:', err.message);
    }
  }

  // Show browser notification as visual demo
  showBrowserNotification(title, body);

  // ── Step 2: Save to Firestore ──────────────
  // Saved to 'notifications' collection
  // → Android NotificationsActivity reads from this
  try {
    await firestoreAdd(COLLECTIONS.notifications, {
      title,
      message:   body,
      type:      currentType,
      action:    action,
      target:    currentTarget,
      isRead:    false,
      createdAt: Date.now(),
      sentBy:    'admin',
    });
    firestoreSaved = true;
  } catch (err) {
    console.warn('Firestore save error:', err.message);
  }

  // ── Step 3: Save to local history ──────────
  const item = {
    id:            'N-' + Date.now(),
    title, body, action,
    type:          currentType,
    target:        currentTarget,
    fcmSent,
    firestoreSaved,
    timestamp:     new Date().toISOString(),
  };
  notifHistory.unshift(item);
  localStorage.setItem('se_notif_history',
    JSON.stringify(notifHistory));
  renderHistory();

  // ── Step 4: Show result ─────────────────────
  const msg = fcmSent
    ? '✅ FCM push sent + saved to Firestore!'
    : firestoreSaved
      ? '📬 Saved to Firestore (add FCM key for device push)'
      : '⚠️ Failed — check Firebase config';

  showToast(msg, fcmSent || firestoreSaved ? 'success' : 'error');
  resetForm();

  btn.disabled = false;
  document.getElementById('sendBtnText').textContent =
    'Send Notification';
}

// ── Browser Notification (visual demo) ───────
function showBrowserNotification(title, body) {
  if (!('Notification' in window)) return;
  if (Notification.permission === 'granted') {
    new Notification('📱 ShopEase App: ' + title, { body });
  } else if (Notification.permission !== 'denied') {
    Notification.requestPermission().then(p => {
      if (p === 'granted')
        new Notification('📱 ShopEase App: ' + title, { body });
    });
  }
}

// ── Render History ────────────────────────────
function renderHistory() {
  const list = document.getElementById('historyList');

  // Update stats
  document.getElementById('statTotal').textContent =
    notifHistory.length;
  document.getElementById('statFcm').textContent =
    notifHistory.filter(i => i.fcmSent).length;
  document.getElementById('statFirestore').textContent =
    notifHistory.filter(i => i.firestoreSaved).length;

  // Filter
  const data =
    histFilter === 'fcm'       ? notifHistory.filter(i => i.fcmSent)
    : histFilter === 'firestore' ? notifHistory.filter(i => i.firestoreSaved)
    : notifHistory;

  if (!data.length) {
    list.innerHTML = `
      <div class="hist-empty">
        <svg viewBox="0 0 24 24" fill="none" width="40">
          <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118
            14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4
            0v.341C7.67 6.165 6 8.388 6 11v3.159c0
            .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0
            11-6 0v-1m6 0H9" stroke="#6b6b80"
            stroke-width="1.5" stroke-linecap="round"
            stroke-linejoin="round"/>
        </svg>
        <p>No notifications here</p>
        <span>Send your first notification above</span>
      </div>`;
    return;
  }

  list.innerHTML = data.map(item => `
    <div class="hist-item">
      <div class="hist-item-header">
        <span class="hist-type-badge hist-type-${item.type}">
          ${item.type.toUpperCase()}
        </span>
        <div style="display:flex;gap:6px;align-items:center">
          ${item.fcmSent
            ? `<span style="font-size:11px;color:#34d399">
                 📱 FCM</span>`
            : ''}
          ${item.firestoreSaved
            ? `<span style="font-size:11px;color:#3b82f6">
                 🔥 Firestore</span>`
            : ''}
          <span style="font-size:11px;color:var(--muted)">
            ${timeAgo(item.timestamp)}
          </span>
        </div>
      </div>
      <p class="hist-title">${item.title}</p>
      <p class="hist-body">${item.body}</p>
      <div class="hist-footer">
        <span>👥 ${item.target}</span>
        <button onclick="removeHistItem('${item.id}')"
          style="background:none;border:none;
                 color:var(--red);cursor:pointer;font-size:12px">
          ✕ Remove
        </button>
      </div>
    </div>`).join('');
}

// ── History Helpers ───────────────────────────
function filterHistory(f, btn) {
  histFilter = f;
  document.querySelectorAll('.hf-btn')
    .forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderHistory();
}

function removeHistItem(id) {
  notifHistory = notifHistory.filter(i => i.id !== id);
  localStorage.setItem('se_notif_history',
    JSON.stringify(notifHistory));
  renderHistory();
}

function clearHistory() {
  if (!confirm('Clear all notification history?')) return;
  notifHistory = [];
  localStorage.removeItem('se_notif_history');
  renderHistory();
}

// ── Reset Form ────────────────────────────────
function resetForm() {
  document.getElementById('notifTitle').value  = '';
  document.getElementById('notifBody').value   = '';
  document.getElementById('notifAction').value = '';
  document.getElementById('prevTitle').textContent =
    'Notification Title';
  document.getElementById('prevBody').textContent =
    'Your message will appear here…';
  document.getElementById('titleCount').textContent = '0';
  document.getElementById('bodyCount').textContent  = '0';
}

// ── Time Ago ──────────────────────────────────
function timeAgo(iso) {
  const diff = Date.now() - new Date(iso).getTime();
  const m    = Math.floor(diff / 60000);
  if (m < 1)  return 'just now';
  if (m < 60) return m + 'm ago';
  const h = Math.floor(m / 60);
  if (h < 24) return h + 'h ago';
  return Math.floor(h / 24) + 'd ago';
}

// ── Start ─────────────────────────────────────
renderHistory();
