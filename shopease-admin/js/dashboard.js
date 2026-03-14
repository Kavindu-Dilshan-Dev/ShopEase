// ── Dashboard JS ───────────────────────────────
document.getElementById('dateDisplay').textContent =
  new Date().toLocaleDateString('en-US', { weekday:'long', year:'numeric', month:'long', day:'numeric' });

// ── Sample Data ────────────────────────────────
const ORDERS = [
  { id:'ORD-1748','customer':'Kavinda Perera','amount':4850,'status':'CONFIRMED','date':'2025-03-11','items':3,'email':'kavinda@gmail.com' },
  { id:'ORD-1747','customer':'Nimal Silva',   'amount':12999,'status':'SHIPPED',  'date':'2025-03-11','items':1,'email':'nimal@gmail.com' },
  { id:'ORD-1746','customer':'Dilini Fernando','amount':1798,'status':'PENDING',  'date':'2025-03-10','items':2,'email':'dilini@gmail.com' },
  { id:'ORD-1745','customer':'Roshan Bandara','amount':5999,'status':'DELIVERED','date':'2025-03-10','items':1,'email':'roshan@gmail.com' },
  { id:'ORD-1744','customer':'Sanduni Jayawardena','amount':3497,'status':'CANCELLED','date':'2025-03-09','items':4,'email':'sanduni@gmail.com'},
  { id:'ORD-1743','customer':'Tharaka Wijesinghe','amount':7349,'status':'CONFIRMED','date':'2025-03-09','items':2,'email':'tharaka@gmail.com'},
  { id:'ORD-1742','customer':'Priya Gamage','amount':899,'status':'DELIVERED','date':'2025-03-08','items':1,'email':'priya@gmail.com'},
  { id:'ORD-1741','customer':'Kasun Rajapaksa','amount':24999,'status':'SHIPPED','date':'2025-03-08','items':3,'email':'kasun@gmail.com'},
];
sessionStorage.setItem('shopease_orders', JSON.stringify(ORDERS));

// ── KPIs ───────────────────────────────────────
function refreshStats() {
  const kpiEls = ['kpi-revenue','kpi-orders','kpi-users','kpi-products'];
  kpiEls.forEach(id => {
    const el = document.getElementById(id);
    if (el) { el.style.opacity = '0.4'; setTimeout(() => el.style.opacity = '1', 600); }
  });
}

// ── Recent Orders Table ────────────────────────
function renderRecentOrders() {
  const tbody = document.getElementById('recentOrdersBody');
  if (!tbody) return;
  tbody.innerHTML = ORDERS.slice(0,6).map(o => `
    <tr>
      <td><span style="font-weight:600;color:var(--accent)">${o.id}</span></td>
      <td>${o.customer}</td>
      <td style="color:var(--accent);font-weight:600">LKR ${o.amount.toLocaleString()}</td>
      <td><span class="status-badge status-${o.status}">${o.status}</span></td>
      <td style="color:var(--muted)">${o.date}</td>
    </tr>`).join('');
}
renderRecentOrders();

// ── Revenue Chart ──────────────────────────────
const chartData = {
  week:  { labels:['Mon','Tue','Wed','Thu','Fri','Sat','Sun'], data:[18400,22100,15800,28900,24300,38200,31000] },
  month: { labels:['W1','W2','W3','W4'], data:[84200,97300,112400,89600] },
  year:  { labels:['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'], data:[210000,185000,248000,267000,294000,312000,289000,325000,298000,341000,356000,384000] },
};

const revenueCtx = document.getElementById('revenueChart').getContext('2d');
const gradient = revenueCtx.createLinearGradient(0, 0, 0, 300);
gradient.addColorStop(0, 'rgba(255,107,53,0.3)');
gradient.addColorStop(1, 'rgba(255,107,53,0.0)');

let revenueChart = new Chart(revenueCtx, {
  type: 'line',
  data: {
    labels: chartData.week.labels,
    datasets: [{
      label: 'Revenue (LKR)',
      data: chartData.week.data,
      borderColor: '#FF6B35',
      backgroundColor: gradient,
      borderWidth: 2.5,
      pointBackgroundColor: '#FF6B35',
      pointRadius: 4,
      pointHoverRadius: 6,
      fill: true,
      tension: 0.4,
    }]
  },
  options: {
    responsive: true,
    plugins: { legend: { display: false }, tooltip: {
      backgroundColor: '#13131a', borderColor: '#1e1e2e', borderWidth: 1,
      titleColor: '#e8e8f0', bodyColor: '#6b6b80',
      callbacks: { label: ctx => ' LKR ' + ctx.raw.toLocaleString() }
    }},
    scales: {
      x: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#6b6b80' } },
      y: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#6b6b80', callback: v => 'LKR '+(v/1000)+'k' } }
    }
  }
});

function switchChart(period, btn) {
  document.querySelectorAll('.ctab').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  const d = chartData[period];
  revenueChart.data.labels = d.labels;
  revenueChart.data.datasets[0].data = d.data;
  revenueChart.update('active');
}

// ── Category Donut Chart ───────────────────────
const catColors = ['#FF6B35','#3b82f6','#34d399','#a78bfa','#f87171','#FFB347'];
const catData   = { labels:['Electronics','Clothing','Food','Books','Sports','Beauty'], data:[38,22,16,10,9,5] };
const catCtx    = document.getElementById('categoryChart').getContext('2d');

new Chart(catCtx, {
  type: 'doughnut',
  data: { labels: catData.labels, datasets: [{ data: catData.data, backgroundColor: catColors, borderWidth: 0, hoverOffset: 8 }] },
  options: {
    responsive: true, cutout: '68%',
    plugins: { legend: { display: false }, tooltip: {
      backgroundColor: '#13131a', borderColor: '#1e1e2e', borderWidth: 1,
      callbacks: { label: ctx => ` ${ctx.label}: ${ctx.raw}%` }
    }}
  }
});

// Donut legend
const legend = document.getElementById('donutLegend');
if (legend) {
  legend.innerHTML = catData.labels.map((l, i) => `
    <div class="dl-item">
      <span class="dl-dot" style="background:${catColors[i]}"></span>
      <span class="dl-label">${l}</span>
      <span class="dl-val">${catData.data[i]}%</span>
    </div>`).join('');
}

// ── Live Activity Feed ─────────────────────────
const feedEvents = [
  { text: '<strong>Kavinda Perera</strong> placed a new order', color: '#34d399', time: '1m ago' },
  { text: '<strong>ORD-1747</strong> status changed to Shipped', color: '#3b82f6', time: '4m ago' },
  { text: '<strong>Dilini Fernando</strong> registered', color: '#a78bfa', time: '8m ago' },
  { text: 'Promo notification sent to <strong>3,841 users</strong>', color: '#FF6B35', time: '15m ago' },
  { text: '<strong>ORD-1745</strong> marked as Delivered', color: '#34d399', time: '22m ago' },
];

const feed = document.getElementById('liveFeed');
if (feed) {
  feedEvents.forEach(ev => {
    const item = document.createElement('div');
    item.className = 'feed-item';
    item.innerHTML = `<span class="feed-dot" style="background:${ev.color}"></span><span class="feed-text">${ev.text}</span><span class="feed-time">${ev.time}</span>`;
    feed.appendChild(item);
  });
  // Simulate live updates
  setInterval(() => {
    const events = [
      '<strong>New order</strong> placed — ORD-' + (1749 + Math.floor(Math.random()*10)),
      '<strong>' + ['Saman','Kumari','Pradeep','Malani'][Math.floor(Math.random()*4)] + '</strong> added item to cart',
      'Low stock alert for <strong>Wireless Earbuds</strong>',
    ];
    const item = document.createElement('div');
    item.className = 'feed-item';
    item.innerHTML = `<span class="feed-dot" style="background:#FF6B35"></span><span class="feed-text">${events[Math.floor(Math.random()*events.length)]}</span><span class="feed-time">now</span>`;
    feed.prepend(item);
    if (feed.children.length > 8) feed.lastChild.remove();
  }, 8000);
}
