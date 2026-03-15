/* ═══════════════════════════════════════════
   ANALYTICS JS — Live Firebase Charts
   ═══════════════════════════════════════════ */

buildSidebar('analytics');

// ── Chart Defaults ────────────────────────────
Chart.defaults.color       = '#6b6b80';
Chart.defaults.borderColor = 'rgba(255,255,255,0.06)';

const CHART_COLORS = [
  '#FF6B35','#3b82f6','#34d399',
  '#a78bfa','#f87171','#FFB347','#06b6d4'
];

const SCALE_OPTS = {
  x: {
    grid:  { color: 'rgba(255,255,255,0.04)' },
    ticks: { color: '#6b6b80' }
  },
  y: {
    grid:  { color: 'rgba(255,255,255,0.04)' },
    ticks: { color: '#6b6b80' }
  }
};

// Store chart instances so we can destroy before rebuild
let charts = {};

// ── Load and Build All Charts ─────────────────
async function loadAnalytics() {
  try {
    const [products, orders] = await Promise.all([
      firestoreGet(COLLECTIONS.products),
      firestoreGet(COLLECTIONS.orders),
    ]);

    buildCategoryChart(products);
    buildStatusChart(orders);
    buildPriceChart(products);
    buildStockChart(products);

  } catch (err) {
    showToast('Firebase error: ' + err.message, 'error');
  }
}

// ── Products by Category ──────────────────────
function buildCategoryChart(products) {
  const counts = {};
  products.forEach(p => {
    const c = p.category || 'Other';
    counts[c] = (counts[c] || 0) + 1;
  });

  if (charts.cat) charts.cat.destroy();
  charts.cat = new Chart(
    document.getElementById('catChart').getContext('2d'), {
      type: 'bar',
      data: {
        labels:   Object.keys(counts),
        datasets: [{
          data:            Object.values(counts),
          backgroundColor: CHART_COLORS,
          borderRadius:    6,
          borderWidth:     0,
        }]
      },
      options: {
        responsive: true,
        plugins:    { legend: { display: false } },
        scales:     SCALE_OPTS,
      }
    });
}

// ── Order Status Split ────────────────────────
function buildStatusChart(orders) {
  const statuses = [
    'PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'];
  const counts   = statuses.map(s =>
    orders.filter(o => o.status === s).length);
  const colors   = [
    '#FFB347','#34d399','#3b82f6','#a78bfa','#f87171'];

  if (charts.status) charts.status.destroy();
  charts.status = new Chart(
    document.getElementById('statusChart').getContext('2d'), {
      type: 'doughnut',
      data: {
        labels:   statuses,
        datasets: [{
          data:            counts,
          backgroundColor: colors,
          borderWidth:     0,
          hoverOffset:     8,
        }]
      },
      options: {
        responsive: true,
        cutout:     '65%',
        plugins:    {
          legend: {
            display:  true,
            position: 'bottom',
            labels:   {
              color:          '#6b6b80',
              padding:        10,
              usePointStyle:  true
            }
          }
        }
      }
    });
}

// ── Top Products by Price ─────────────────────
function buildPriceChart(products) {
  const sorted = [...products]
    .sort((a, b) => Number(b.price) - Number(a.price))
    .slice(0, 8);

  const labels = sorted.map(p =>
    p.name.length > 16
      ? p.name.substring(0, 16) + '…'
      : p.name);

  if (charts.price) charts.price.destroy();
  charts.price = new Chart(
    document.getElementById('priceChart').getContext('2d'), {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          data:            sorted.map(p => Number(p.price || 0)),
          backgroundColor: 'rgba(255,107,53,0.75)',
          borderColor:     '#FF6B35',
          borderWidth:     1,
          borderRadius:    6,
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        plugins: {
          legend:  { display: false },
          tooltip: {
            callbacks: {
              label: ctx =>
                ' LKR ' + ctx.raw.toLocaleString()
            }
          }
        },
        scales: {
          x: {
            grid:  { color: 'rgba(255,255,255,0.04)' },
            ticks: {
              color:    '#6b6b80',
              callback: v => 'LKR ' + (v / 1000) + 'k'
            }
          },
          y: {
            grid:  { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: '#6b6b80' }
          }
        }
      }
    });
}

// ── Stock Levels ──────────────────────────────
function buildStockChart(products) {
  const sorted = [...products]
    .sort((a, b) =>
      Number(a.stockQuantity) - Number(b.stockQuantity))
    .slice(0, 10);

  const labels = sorted.map(p =>
    p.name.length > 14
      ? p.name.substring(0, 14) + '…'
      : p.name);

  // Red if low stock (<10), green otherwise
  const colors = sorted.map(p =>
    Number(p.stockQuantity) < 10
      ? 'rgba(248,113,113,0.75)'
      : 'rgba(52,211,153,0.75)');

  if (charts.stock) charts.stock.destroy();
  charts.stock = new Chart(
    document.getElementById('stockChart').getContext('2d'), {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          data:            sorted.map(p =>
            Number(p.stockQuantity || 0)),
          backgroundColor: colors,
          borderRadius:    6,
          borderWidth:     0,
        }]
      },
      options: {
        responsive: true,
        plugins:    { legend: { display: false } },
        scales:     {
          x: {
            grid:  { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: '#6b6b80' }
          },
          y: {
            grid:  { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: '#6b6b80', stepSize: 10 }
          }
        }
      }
    });
}

// ── Start ─────────────────────────────────────
loadAnalytics();
