/* ═══════════════════════════════════════════
   ORDERS JS — Firebase Live
   ═══════════════════════════════════════════ */

buildSidebar('orders');

let allOrders       = [];
let selectedOrderId = null;

// ── Load Orders from Firebase ────────────────
async function loadOrders() {
  document.getElementById('ordersBody').innerHTML = `
    <tr>
      <td colspan="7"
        style="text-align:center;color:var(--muted);padding:32px">
        🔥 Loading from Firebase…
      </td>
    </tr>`;

  try {
    allOrders = await firestoreGet(COLLECTIONS.orders);
    renderStatusPills();
    filterOrders();
  } catch (err) {
    document.getElementById('ordersBody').innerHTML = `
      <tr>
        <td colspan="7"
          style="text-align:center;color:#f87171;padding:32px">
          ❌ Firebase error: ${err.message}
        </td>
      </tr>`;
    showToast('Firebase error: ' + err.message, 'error');
  }
}

// ── Status Pill Counts ───────────────────────
function renderStatusPills() {
  const statuses = [
    'PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'];
  const counts = {};
  statuses.forEach(s =>
    counts[s] = allOrders.filter(o => o.status === s).length);

  document.getElementById('statusPills').innerHTML =
    `<div class="status-pill active"
       onclick="setStatusPill('',this)">
       All <span class="pill-count">${allOrders.length}</span>
     </div>` +
    statuses.map(s => `
      <div class="status-pill"
        onclick="setStatusPill('${s}',this)">
        ${s}
        <span class="pill-count">${counts[s]}</span>
      </div>`).join('');

  // Update pending badge in sidebar
  const badge = document.getElementById('pendingBadge');
  if (badge) badge.textContent = counts['PENDING'] || 0;
}

function setStatusPill(status, pill) {
  document.querySelectorAll('.status-pill')
    .forEach(p => p.classList.remove('active'));
  pill.classList.add('active');
  document.getElementById('statusFilter').value = status;
  filterOrders();
}

// ── Filter Orders ────────────────────────────
function filterOrders() {
  const q = (document.getElementById('orderSearch').value || '')
    .toLowerCase();
  const status = document.getElementById('statusFilter').value;

  const filtered = allOrders.filter(o =>
    (!q || (o.id || '').toLowerCase().includes(q) ||
           (o.userId || '').toLowerCase().includes(q)) &&
    (!status || o.status === status)
  );
  renderOrders(filtered);
}

// ── Render Orders Table ──────────────────────
function renderOrders(list) {
  document.getElementById('orderCount').textContent =
    `Showing ${list.length} orders`;

  if (!list.length) {
    document.getElementById('ordersBody').innerHTML = `
      <tr>
        <td colspan="7"
          style="text-align:center;color:var(--muted);padding:32px">
          No orders found
        </td>
      </tr>`;
    return;
  }

  document.getElementById('ordersBody').innerHTML =
    list.map(o => {
      const date = o.createdAt
        ? new Date(Number(o.createdAt)).toLocaleDateString('en-US', {
            day: 'numeric', month: 'short', year: 'numeric'
          })
        : '—';
      return `
        <tr>
          <td>
            <span style="color:var(--accent);font-weight:600;
                         cursor:pointer"
              onclick="openOrderDetail('${o.id}')">
              ${(o.id || '').substring(0, 16)}…
            </span>
          </td>
          <td style="color:var(--muted);font-size:12px">
            ${(o.userId || '—').substring(0, 14)}…
          </td>
          <td style="color:var(--accent);font-weight:600">
            LKR ${Number(o.totalAmount || 0).toLocaleString()}
          </td>
          <td>
            ${Array.isArray(o.items) ? o.items.length : '—'}
            item(s)
          </td>
          <td>
            <span class="status-badge
              status-${o.status || 'PENDING'}">
              ${o.status || 'PENDING'}
            </span>
          </td>
          <td style="color:var(--muted)">${date}</td>
          <td>
            <button class="pa-btn"
              style="width:auto;padding:5px 12px"
              onclick="openOrderDetail('${o.id}')">
              Update
            </button>
          </td>
        </tr>`;
    }).join('');
}

// ── Open Order Detail Modal ──────────────────
function openOrderDetail(id) {
  const o = allOrders.find(x => x.id === id);
  if (!o) return;

  selectedOrderId = id;
  document.getElementById('modalOrderId').textContent =
    'Order #' + (id || '').substring(0, 20);
  document.getElementById('modalStatus').value =
    o.status || 'PENDING';

  document.getElementById('orderModalBody').innerHTML = `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
      <div>
        <p style="color:var(--muted);font-size:12px;
                  text-transform:uppercase;margin-bottom:4px">
          User ID
        </p>
        <p style="font-size:13px;word-break:break-all">
          ${o.userId || '—'}
        </p>
      </div>
      <div>
        <p style="color:var(--muted);font-size:12px;
                  text-transform:uppercase;margin-bottom:4px">
          Total Amount
        </p>
        <p style="color:var(--accent);font-weight:700;font-size:20px">
          LKR ${Number(o.totalAmount || 0).toLocaleString()}
        </p>
      </div>
      <div>
        <p style="color:var(--muted);font-size:12px;
                  text-transform:uppercase;margin-bottom:4px">
          Delivery Address
        </p>
        <p style="font-size:13px">
          ${o.deliveryAddress || '—'}
        </p>
      </div>
      <div>
        <p style="color:var(--muted);font-size:12px;
                  text-transform:uppercase;margin-bottom:4px">
          Payment ID
        </p>
        <p style="font-size:12px;word-break:break-all">
          ${o.paymentId || '—'}
        </p>
      </div>
    </div>`;

  document.getElementById('orderModal').style.display = 'flex';
}

// ── Update Order Status in Firebase ──────────
async function updateOrderStatus() {
  const newStatus = document.getElementById('modalStatus').value;
  const o = allOrders.find(x => x.id === selectedOrderId);
  if (!o) return;

  try {
    await firestoreUpdate(
      COLLECTIONS.orders, selectedOrderId, { status: newStatus });

    // Update local copy
    o.status = newStatus;

    document.getElementById('orderModal').style.display = 'none';
    renderStatusPills();
    filterOrders();
    showToast(`✅ Order status updated to ${newStatus}`, 'success');
  } catch (err) {
    showToast('Update failed: ' + err.message, 'error');
  }
}

// ── Start ─────────────────────────────────────
loadOrders();
