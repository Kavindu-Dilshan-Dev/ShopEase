// ── Notifications JS ───────────────────────────
// Firebase FCM Configuration - Replace with your project values
const FCM_CONFIG = {
  serverKey:  'YOUR_FIREBASE_SERVER_KEY',  // Firebase Console → Project Settings → Cloud Messaging
  projectId:  'YOUR_FIREBASE_PROJECT_ID',
  fcmApiUrl:  'https://fcm.googleapis.com/fcm/send',
  // Topic-based sending (no individual tokens needed for broadcast)
  topics: {
    all:      '/topics/all_users',
    android:  '/topics/android_users',
    premium:  '/topics/premium_users',
    inactive: '/topics/inactive_users',
  }
};

let notifHistory = JSON.parse(localStorage.getItem('notif_history') || '[]');
let currentType   = 'general';
let currentTarget = 'all';
let sentCount     = parseInt(localStorage.getItem('notif_sent_today') || '0');
let editingId     = null;

// ── Init ────────────────────────────────────────
function init() {
  updateCounters();
  renderHistory(notifHistory);
  setupCharCounters();
  requestBrowserNotificationPermission();
}

function requestBrowserNotificationPermission() {
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission();
  }
}

// ── Type / Target ───────────────────────────────
function setType(type, btn) {
  currentType = type;
  document.querySelectorAll('.type-tab').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  applyTypeDefaults(type);
}

function applyTypeDefaults(type) {
  const presets = {
    general: { title: '', body: '' },
    order:   { title: '📦 Order Update', body: 'Your order has been updated. Tap to view details.' },
    promo:   { title: '🎁 Special Offer Just for You!', body: 'Exclusive discount available for a limited time. Don\'t miss out!' },
    alert:   { title: '🚨 Important Notice', body: 'Please check your account for important updates.' },
  };
  if (presets[type].title) {
    document.getElementById('notifTitle').value = presets[type].title;
    document.getElementById('notifBody').value  = presets[type].body;
    updatePreview();
    updateCharCount('title'); updateCharCount('body');
  }
}

function setTarget(target, label) {
  currentTarget = target;
  document.querySelectorAll('.target-opt').forEach(l => l.classList.remove('active-opt'));
  label.classList.add('active-opt');
}

// ── Char Counters ───────────────────────────────
function setupCharCounters() {
  document.getElementById('notifTitle').addEventListener('input', () => { updatePreview(); updateCharCount('title'); });
  document.getElementById('notifBody').addEventListener('input',  () => { updatePreview(); updateCharCount('body'); });
}
function updateCharCount(field) {
  const el  = document.getElementById('notif' + field.charAt(0).toUpperCase() + field.slice(1));
  const cnt = document.getElementById(field + 'Count');
  if (el && cnt) cnt.textContent = el.value.length;
}

// ── Preview ─────────────────────────────────────
function updatePreview() {
  const title = document.getElementById('notifTitle').value || 'Notification Title';
  const body  = document.getElementById('notifBody').value  || 'Your message will appear here...';
  document.getElementById('prevTitle').textContent = title;
  document.getElementById('prevBody').textContent  = body;
}

// ── Schedule Toggle ─────────────────────────────
function toggleSchedule() {
  const checked = document.getElementById('scheduleToggle').checked;
  document.getElementById('schedulePicker').style.display = checked ? 'block' : 'none';
}

// ── Send Notification ───────────────────────────
async function sendNotification() {
  const title  = document.getElementById('notifTitle').value.trim();
  const body   = document.getElementById('notifBody').value.trim();
  const image  = document.getElementById('notifImage').value.trim();
  const action = document.getElementById('notifAction').value;
  const isScheduled = document.getElementById('scheduleToggle').checked;
  const schedTime   = document.getElementById('scheduleTime').value;

  if (!title) { showToast('Please enter a notification title', 'error'); return; }
  if (!body)  { showToast('Please enter a message body', 'error'); return; }
  if (isScheduled && !schedTime) { showToast('Please select a schedule time', 'error'); return; }

  const btn = document.getElementById('btnSend');
  btn.disabled = true;
  document.getElementById('sendBtnText').textContent = 'Sending…';

  const payload = {
    to: FCM_CONFIG.topics[currentTarget],
    notification: { title, body, ...(image && { image }) },
    data: { type: currentType, action, click_action: 'FLUTTER_NOTIFICATION_CLICK' },
    android: { priority: 'high', notification: { channel_id: `${currentType}_channel` } },
  };

  try {
    // ── Real FCM Send ─────────────────────────────
    // Uncomment below and add your server key to actually send via FCM:
    /*
    const response = await fetch(FCM_CONFIG.fcmApiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `key=${FCM_CONFIG.serverKey}`,
      },
      body: JSON.stringify(payload),
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.error || 'FCM error');
    */

    // ── Demo mode (simulated) ────────────────────
    await simulateDelay(1200);
    const mockResult = { success: 1, failure: 0, multicast_id: Date.now() };

    // Show browser notification as demo
    showBrowserNotification(title, body, image);

    // Save to history
    const histItem = {
      id:        'NOTIF-' + Date.now(),
      title, body, image, action,
      type:      currentType,
      target:    currentTarget,
      status:    isScheduled ? 'scheduled' : 'sent',
      schedTime: isScheduled ? schedTime : null,
      timestamp: new Date().toISOString(),
      sentTo:    getTargetCount(currentTarget),
      delivered: Math.floor(getTargetCount(currentTarget) * 0.94),
      opened:    Math.floor(getTargetCount(currentTarget) * 0.18),
    };

    notifHistory.unshift(histItem);
    localStorage.setItem('notif_history', JSON.stringify(notifHistory));

    if (!isScheduled) {
      sentCount++;
      localStorage.setItem('notif_sent_today', sentCount);
    }

    updateCounters();
    renderHistory(notifHistory);
    resetForm();

    const msg = isScheduled
      ? `⏰ Notification scheduled for ${new Date(schedTime).toLocaleString()}`
      : `✅ Sent to ${histItem.sentTo.toLocaleString()} users successfully`;
    showToast(msg, 'success');

  } catch (err) {
    showToast('Failed to send: ' + err.message, 'error');
  } finally {
    btn.disabled = false;
    document.getElementById('sendBtnText').textContent = 'Send Notification';
  }
}

// ── Browser Notification (demo) ─────────────────
function showBrowserNotification(title, body, image) {
  if ('Notification' in window && Notification.permission === 'granted') {
    const opts = { body, icon: image || '../favicon.ico', badge: '../favicon.ico', tag: 'shopease-notif' };
    new Notification('📱 ShopEase App: ' + title, opts);
  }
}

// ── Templates ───────────────────────────────────
const TEMPLATES = {
  sale:     { title: '🔥 Flash Sale — 24 Hours Only!',   body: 'HUGE discounts across all categories. Up to 60% off! Use code FLASH60 at checkout. Hurry, ends midnight!' },
  order:    { title: '✅ Order Confirmed!',               body: 'Great news! Your order has been confirmed and is being prepared. You\'ll receive a shipping update soon.' },
  shipped:  { title: '🚚 Your Order is On the Way!',     body: 'Your package is out for delivery! Track your order in the ShopEase app for real-time updates.' },
  welcome:  { title: '👋 We Miss You!',                  body: 'It\'s been a while! Come back and discover new arrivals + a special 15% welcome back discount just for you.' },
  review:   { title: '⭐ Enjoying ShopEase?',            body: 'If you love shopping with us, please take 30 seconds to leave a review. Your feedback means the world to us!' },
  promo:    { title: '🎁 Exclusive Offer Inside',        body: 'You\'ve been selected for an exclusive offer! Open the app to claim your special discount before it expires.' },
};

function applyTemplate(key) {
  const t = TEMPLATES[key];
  document.getElementById('notifTitle').value = t.title;
  document.getElementById('notifBody').value  = t.body;
  updatePreview();
  updateCharCount('title'); updateCharCount('body');
}

// ── History ─────────────────────────────────────
let historyFilter = 'all';

function renderHistory(items) {
  const list = document.getElementById('historyList');
  const filtered = historyFilter === 'all' ? items : items.filter(i => i.status === historyFilter);

  document.getElementById('statSent').textContent      = items.filter(i=>i.status==='sent').length;
  document.getElementById('statDelivered').textContent = items.filter(i=>i.status==='sent').reduce((s,i)=>s+(i.delivered||0),0).toLocaleString();
  const totalSent   = items.filter(i=>i.status==='sent').reduce((s,i)=>s+(i.sentTo||0),0);
  const totalOpened = items.filter(i=>i.status==='sent').reduce((s,i)=>s+(i.opened||0),0);
  document.getElementById('statOpened').textContent = totalSent > 0 ? Math.round(totalOpened/totalSent*100)+'%' : '0%';

  if (!filtered.length) {
    list.innerHTML = `<div class="hist-empty">
      <svg viewBox="0 0 24 24" fill="none" width="48"><path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" stroke="#6b6b80" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
      <p>No notifications here</p><span>Send your first notification above</span>
    </div>`;
    return;
  }

  list.innerHTML = filtered.map(item => `
    <div class="hist-item">
      <div class="hist-item-header">
        <span class="hist-type-badge hist-type-${item.type}">${item.type.toUpperCase()}</span>
        <span style="font-size:12px;color:var(--muted)">${item.status === 'scheduled' ? '⏰ '+new Date(item.schedTime).toLocaleString() : '✅ '+timeAgo(item.timestamp)}</span>
      </div>
      <p class="hist-title">${item.title}</p>
      <p class="hist-body">${item.body}</p>
      <div class="hist-footer">
        <span class="hist-target">👥 ${(item.sentTo||0).toLocaleString()} users · 📬 ${(item.delivered||0).toLocaleString()} delivered · 👁 ${(item.opened||0).toLocaleString()} opened</span>
        <button onclick="deleteHistItem('${item.id}')" style="background:none;border:none;color:var(--red);cursor:pointer;font-size:12px">✕</button>
      </div>
    </div>`).join('');
}

function filterHistory(filter, btn) {
  historyFilter = filter;
  document.querySelectorAll('.hf-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderHistory(notifHistory);
}

function deleteHistItem(id) {
  notifHistory = notifHistory.filter(i => i.id !== id);
  localStorage.setItem('notif_history', JSON.stringify(notifHistory));
  renderHistory(notifHistory);
}

function clearHistory() {
  if (confirm('Clear all notification history?')) {
    notifHistory = [];
    localStorage.removeItem('notif_history');
    renderHistory(notifHistory);
  }
}

// ── Helpers ─────────────────────────────────────
function getTargetCount(target) {
  return { all: 3841, android: 2634, premium: 892, inactive: 315 }[target] || 0;
}

function updateCounters() {
  document.getElementById('sentCount').textContent    = sentCount;
  document.getElementById('pendingCount').textContent = notifHistory.filter(i => i.status === 'scheduled').length;
}

function resetForm() {
  document.getElementById('notifTitle').value = '';
  document.getElementById('notifBody').value  = '';
  document.getElementById('notifImage').value = '';
  document.getElementById('notifAction').value = '';
  document.getElementById('scheduleToggle').checked = false;
  document.getElementById('schedulePicker').style.display = 'none';
  document.getElementById('prevTitle').textContent = 'Notification Title';
  document.getElementById('prevBody').textContent  = 'Your message will appear here...';
  document.getElementById('titleCount').textContent = '0';
  document.getElementById('bodyCount').textContent  = '0';
}

function simulateDelay(ms) { return new Promise(r => setTimeout(r, ms)); }

function timeAgo(iso) {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1)  return 'just now';
  if (mins < 60) return mins + 'm ago';
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)  return hrs + 'h ago';
  return Math.floor(hrs / 24) + 'd ago';
}

// ── Start ───────────────────────────────────────
init();
