/* ═══════════════════════════════════════════
   DASHBOARD JS
   ═══════════════════════════════════════════ */

// Build sidebar
buildSidebar('dashboard');

// Set date
document.getElementById('dateDisplay').textContent =
  new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    year:    'numeric',
    month:   'long',
    day:     'numeric'
  });

// ── Load Dashboard Data ──────────────────────
async function loadDashboard() {
  const statusEl = document.getElementById('firebaseStatus');

  try {
    // Load all collections in parallel
    const [products, orders, users] = await Promise.all([
      firestoreGet(COLLECTIONS.products),
      firestoreGet(COLLECTIONS.orders),
      firestoreGet(COLLECTIONS.users),
    ]);

    const pending = orders.filter(o => o.status === 'PENDING').length;

    // Update KPI values
    document.getElementById('kpiProducts').textContent = products.length;
    document.getElementById('kpiOrders').textContent   = orders.length;
    document.getElementById('kpiUsers').textContent    = users.length;
    document.getElementById('kpiPending').textContent  = pending;

    // Update pending badge in sidebar
    const badge = document.getElementById('pendingBadge');
    if (badge) badge.textContent = pending;

    // Firebase status feed
    statusEl.innerHTML = `
      <div class="feed-item">
        <span class="feed-dot" style="background:#34d399"></span>
        <span class="feed-text">
          🔥 Firebase connected —
          <strong>${products.length}</strong> products
        </span>
      </div>
      <div class="feed-item">
        <span class="feed-dot" style="background:#3b82f6"></span>
        <span class="feed-text">
          <strong>${orders.length}</strong> orders ·
          <strong>${pending}</strong> pending
        </span>
      </div>
      <div class="feed-item">
        <span class="feed-dot" style="background:#a78bfa"></span>
        <span class="feed-text">
          <strong>${users.length}</strong> registered users
        </span>
      </div>
      <div class="feed-item">
        <span class="feed-dot" style="background:#FF6B35"></span>
        <span class="feed-text">
          Package: <strong>com.kavindu.shopeaseapp</strong>
        </span>
      </div>`;

    // Recent orders table
    const tbody = document.getElementById('recentOrdersBody');
    if (!orders.length) {
      tbody.innerHTML = `
        <tr>
          <td colspan="4"
            style="text-align:center;color:var(--muted);padding:24px">
            No orders yet
          </td>
        </tr>`;
    } else {
      tbody.innerHTML = orders.slice(0, 6).map(o => `
        <tr>
          <td>
            <span style="color:var(--accent);font-weight:600">
              ${(o.id || '').substring(0, 16)}…
            </span>
          </td>
          <td style="color:var(--muted);font-size:12px">
            ${(o.userId || '—').substring(0, 12)}…
          </td>
          <td style="color:var(--accent);font-weight:600">
            LKR ${Number(o.totalAmount || 0).toLocaleString()}
          </td>
          <td>
            <span class="status-badge status-${o.status || 'PENDING'}">
              ${o.status || 'PENDING'}
            </span>
          </td>
        </tr>`).join('');
    }

  } catch (err) {
    statusEl.innerHTML = `
      <div class="feed-item">
        <span class="feed-dot" style="background:#f87171"></span>
        <span class="feed-text" style="color:#f87171">
          ❌ Firebase error: ${err.message}
        </span>
      </div>
      <div class="feed-item">
        <span class="feed-dot" style="background:#FFB347"></span>
        <span class="feed-text">
          → Open <strong>js/firebase-config.js</strong>
          and replace YOUR_PROJECT_ID
        </span>
      </div>`;
    showToast('Firebase connection failed — check firebase-config.js', 'error');
  }
}

loadDashboard();
