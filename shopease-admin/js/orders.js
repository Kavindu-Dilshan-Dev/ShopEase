// ── Orders JS ──────────────────────────────────
const ORDERS_DATA = JSON.parse(sessionStorage.getItem('shopease_orders') || '[]').concat([
  { id:'ORD-1740','customer':'Amali Wickrama','amount':15499,'status':'PENDING',  'date':'2025-03-07','items':2,'email':'amali@gmail.com' },
  { id:'ORD-1739','customer':'Dasun Hettige','amount':3299, 'status':'CONFIRMED','date':'2025-03-07','items':3,'email':'dasun@gmail.com' },
  { id:'ORD-1738','customer':'Nadee Pathirana','amount':8750,'status':'SHIPPED', 'date':'2025-03-06','items':1,'email':'nadee@gmail.com' },
  { id:'ORD-1737','customer':'Chamara Seneviratne','amount':2199,'status':'DELIVERED','date':'2025-03-06','items':2,'email':'chamara@gmail.com'},
  { id:'ORD-1736','customer':'Ruwani Mendis','amount':6499,'status':'CANCELLED','date':'2025-03-05','items':1,'email':'ruwani@gmail.com'},
  { id:'ORD-1735','customer':'Aruna Dissanayake','amount':4100,'status':'CONFIRMED','date':'2025-03-05','items':4,'email':'aruna@gmail.com'},
]);

let filteredOrders = [...ORDERS_DATA];
let currentOrderPage = 1;
const ORDERS_PER_PAGE = 8;
let selectedOrderId = null;

function renderStatusPills() {
  const counts = {};
  ['PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'].forEach(s => {
    counts[s] = ORDERS_DATA.filter(o => o.status === s).length;
  });
  const pills = document.getElementById('statusPills');
  if (!pills) return;
  pills.innerHTML = `<div class="status-pill active" onclick="filterByStatus('',this)">All <span class="pill-count">${ORDERS_DATA.length}</span></div>` +
    Object.entries(counts).map(([s,c]) =>
      `<div class="status-pill" onclick="filterByStatus('${s}',this)">${s} <span class="pill-count">${c}</span></div>`
    ).join('');
}

function filterByStatus(status, pill) {
  document.querySelectorAll('.status-pill').forEach(p => p.classList.remove('active'));
  pill.classList.add('active');
  document.getElementById('statusFilter').value = status;
  filterOrders();
}

function filterOrders() {
  const q      = (document.getElementById('orderSearch')?.value || '').toLowerCase();
  const status = document.getElementById('statusFilter')?.value || '';
  filteredOrders = ORDERS_DATA.filter(o =>
    (!q || o.id.toLowerCase().includes(q) || o.customer.toLowerCase().includes(q)) &&
    (!status || o.status === status)
  );
  currentOrderPage = 1;
  renderOrdersTable();
}

function renderOrdersTable() {
  const tbody = document.getElementById('ordersTableBody');
  if (!tbody) return;
  const start = (currentOrderPage - 1) * ORDERS_PER_PAGE;
  const page  = filteredOrders.slice(start, start + ORDERS_PER_PAGE);

  tbody.innerHTML = page.map(o => `
    <tr>
      <td><span style="color:var(--accent);font-weight:600;cursor:pointer" onclick="openOrderDetail('${o.id}')">${o.id}</span></td>
      <td>${o.customer}</td>
      <td>${o.items} item(s)</td>
      <td style="color:var(--accent);font-weight:600">LKR ${o.amount.toLocaleString()}</td>
      <td><span class="status-badge status-${o.status}">${o.status}</span></td>
      <td style="color:var(--muted)">${o.date}</td>
      <td>
        <button class="pa-btn" style="width:auto;padding:6px 12px" onclick="openOrderDetail('${o.id}')">View</button>
        <button class="pa-btn" style="width:auto;padding:6px 12px;margin-left:4px" onclick="sendOrderNotif('${o.id}')">Notify</button>
      </td>
    </tr>`).join('');

  document.getElementById('orderCount').textContent = `Showing ${filteredOrders.length} orders`;
  renderPagination(Math.ceil(filteredOrders.length / ORDERS_PER_PAGE));
}

function renderPagination(totalPages) {
  const pag = document.getElementById('pagination');
  if (!pag) return;
  pag.innerHTML = Array.from({length: Math.min(totalPages, 5)}, (_,i) =>
    `<button class="page-btn ${i+1===currentOrderPage?'active':''}" onclick="goToPage(${i+1})">${i+1}</button>`
  ).join('');
}

function goToPage(p) { currentOrderPage = p; renderOrdersTable(); }

function openOrderDetail(id) {
  const order = ORDERS_DATA.find(o => o.id === id);
  if (!order) return;
  selectedOrderId = id;
  document.getElementById('modalOrderId').textContent = 'Order ' + order.id;
  document.getElementById('modalStatus').value = order.status;
  document.getElementById('modalBody').innerHTML = `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
      <div><p style="color:var(--muted);font-size:12px">CUSTOMER</p><p style="font-weight:600">${order.customer}</p></div>
      <div><p style="color:var(--muted);font-size:12px">EMAIL</p><p>${order.email}</p></div>
      <div><p style="color:var(--muted);font-size:12px">AMOUNT</p><p style="color:var(--accent);font-weight:700;font-size:18px">LKR ${order.amount.toLocaleString()}</p></div>
      <div><p style="color:var(--muted);font-size:12px">ITEMS</p><p>${order.items} item(s)</p></div>
      <div><p style="color:var(--muted);font-size:12px">DATE</p><p>${order.date}</p></div>
      <div><p style="color:var(--muted);font-size:12px">STATUS</p><span class="status-badge status-${order.status}">${order.status}</span></div>
    </div>`;
  document.getElementById('orderModal').style.display = 'flex';
}

function updateOrderStatus() {
  const newStatus = document.getElementById('modalStatus').value;
  const order = ORDERS_DATA.find(o => o.id === selectedOrderId);
  if (order) {
    order.status = newStatus;
    document.getElementById('orderModal').style.display = 'none';
    filterOrders();
    // Auto-send browser notification on status change
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(`📦 Order ${selectedOrderId} Updated`, { body: `Status changed to ${newStatus}` });
    }
    showToast(`Order updated to ${newStatus} & notification sent!`, 'success');
  }
}

function sendOrderNotif(id) {
  const order = ORDERS_DATA.find(o => o.id === id);
  if (!order) return;
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification(`📦 ${order.id} — ${order.status}`, {
      body: `Hi ${order.customer}, your order status is: ${order.status}`
    });
  }
  showToast(`Notification sent to ${order.customer}`, 'success');
}

renderStatusPills();
filterOrders();
