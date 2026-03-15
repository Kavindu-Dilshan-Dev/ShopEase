/* ═══════════════════════════════════════════
   AUTH GUARD + SHARED HELPERS
   Include this on every page AFTER firebase-config.js
   ═══════════════════════════════════════════ */

// ── Auth Guard ───────────────────────────────
// Redirect to login if not authenticated
if (sessionStorage.getItem('se_admin_logged_in') !== 'true') {
  window.location.href = '../index.html';
}

// Set admin name in sidebar
const adminNameEl = document.getElementById('adminName');
if (adminNameEl) {
  adminNameEl.textContent =
    sessionStorage.getItem('se_admin_user') || 'Admin';
}

// ── Logout ───────────────────────────────────
function logout() {
  sessionStorage.clear();
  window.location.href = '../index.html';
}

// ── Sidebar Toggle ───────────────────────────
function toggleSidebar() {
  document.getElementById('sidebar').classList.toggle('open');
}

// ── Close Modal on Backdrop Click ────────────
function closeModal(e) {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.style.display = 'none';
  }
}

/* ═══════════════════════════════════════════
   TOAST NOTIFICATIONS
   ═══════════════════════════════════════════ */
function showToast(msg, type = 'success') {
  const area = document.getElementById('toastArea');
  if (!area) return;

  const icons = {
    success: `<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0
      000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1
      1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
      clip-rule="evenodd"/>`,
    error: `<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0
      000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1
      1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414
      10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
      clip-rule="evenodd"/>`,
    info: `<path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0
      0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0
      001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>`,
  };

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <svg class="toast-icon" viewBox="0 0 20 20"
      fill="currentColor">${icons[type]}</svg>
    <span class="toast-msg">${msg}</span>
    <button class="toast-close"
      onclick="this.parentElement.remove()">✕</button>`;

  area.appendChild(toast);
  setTimeout(() => {
    if (toast.parentElement) toast.remove();
  }, 4500);
}

/* ═══════════════════════════════════════════
   FIRESTORE REST API HELPERS
   No SDK needed — uses Firebase REST API directly
   ═══════════════════════════════════════════ */

// ── Get all documents from a collection ──────
async function firestoreGet(collection) {
  const url  = `${getFirestoreBase()}/${collection}`;
  const res  = await fetch(url);
  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.error?.message || 'Firestore GET failed');
  }
  if (!data.documents) return [];

  return data.documents.map(doc => {
    const id     = doc.name.split('/').pop();
    const fields = parseFirestoreFields(doc.fields || {});
    return { id, ...fields };
  });
}

// ── Add a new document ────────────────────────
async function firestoreAdd(collection, data) {
  const url  = `${getFirestoreBase()}/${collection}`;
  const body = { fields: buildFirestoreFields(data) };

  const res  = await fetch(url, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify(body),
  });
  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.error?.message || 'Firestore ADD failed');
  }
  return result;
}

// ── Update an existing document ───────────────
async function firestoreUpdate(collection, docId, data) {
  const url  = `${getFirestoreBase()}/${collection}/${docId}`;
  const body = { fields: buildFirestoreFields(data) };

  const res  = await fetch(url + '?currentDocument.exists=true', {
    method:  'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify(body),
  });
  const result = await res.json();

  if (!res.ok) {
    throw new Error(result.error?.message || 'Firestore UPDATE failed');
  }
  return result;
}

// ── Delete a document ─────────────────────────
async function firestoreDelete(collection, docId) {
  const url = `${getFirestoreBase()}/${collection}/${docId}`;
  const res = await fetch(url, { method: 'DELETE' });

  if (!res.ok) {
    const data = await res.json();
    throw new Error(data.error?.message || 'Firestore DELETE failed');
  }
}

// ── Parse Firestore field values ──────────────
function parseFirestoreFields(fields) {
  const result = {};
  for (const [key, val] of Object.entries(fields)) {
    if      (val.stringValue  !== undefined) result[key] = val.stringValue;
    else if (val.integerValue !== undefined) result[key] = Number(val.integerValue);
    else if (val.doubleValue  !== undefined) result[key] = Number(val.doubleValue);
    else if (val.booleanValue !== undefined) result[key] = val.booleanValue;
    else if (val.timestampValue !== undefined) result[key] = val.timestampValue;
    else if (val.nullValue !== undefined)    result[key] = null;
    else result[key] = '';
  }
  return result;
}

// ── Build Firestore field values ──────────────
function buildFirestoreFields(data) {
  const fields = {};
  for (const [key, val] of Object.entries(data)) {
    if      (typeof val === 'boolean') fields[key] = { booleanValue: val };
    else if (typeof val === 'number')  fields[key] = { doubleValue:  val };
    else if (val === null)             fields[key] = { nullValue: null };
    else                               fields[key] = { stringValue: String(val) };
  }
  return fields;
}
