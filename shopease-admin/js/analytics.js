// ── Analytics JS ───────────────────────────────
Chart.defaults.color = '#6b6b80';
Chart.defaults.borderColor = 'rgba(255,255,255,0.06)';

const chartOpts = (extra = {}) => ({
  responsive: true,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: '#13131a', borderColor: '#1e1e2e', borderWidth: 1,
      titleColor: '#e8e8f0', bodyColor: '#6b6b80',
    }
  },
  scales: {
    x: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#6b6b80' } },
    y: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: '#6b6b80' } }
  },
  ...extra
});

// Monthly Revenue
const mrCtx = document.getElementById('monthlyRevChart').getContext('2d');
const mrGrad = mrCtx.createLinearGradient(0,0,0,300);
mrGrad.addColorStop(0, 'rgba(255,107,53,0.3)');
mrGrad.addColorStop(1, 'rgba(255,107,53,0)');
new Chart(mrCtx, {
  type: 'bar',
  data: {
    labels: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
    datasets: [{
      label: 'Revenue',
      data: [210000,185000,248000,267000,294000,312000,289000,325000,298000,341000,356000,384000],
      backgroundColor: 'rgba(255,107,53,0.7)',
      borderColor: '#FF6B35', borderWidth: 1, borderRadius: 6,
    }]
  },
  options: { ...chartOpts(), plugins: { ...chartOpts().plugins, tooltip: { ...chartOpts().plugins.tooltip, callbacks: { label: ctx => ' LKR '+ctx.raw.toLocaleString() } } } }
});

// Order Status Split
new Chart(document.getElementById('statusChart').getContext('2d'), {
  type: 'doughnut',
  data: {
    labels: ['Confirmed','Shipped','Delivered','Pending','Cancelled'],
    datasets: [{ data: [312, 189, 584, 97, 65], backgroundColor: ['#34d399','#3b82f6','#a78bfa','#FFB347','#f87171'], borderWidth: 0, hoverOffset: 8 }]
  },
  options: { responsive: true, cutout: '65%', plugins: { legend: { display: true, position: 'bottom', labels: { color: '#6b6b80', padding: 12, usePointStyle: true } }, tooltip: { backgroundColor: '#13131a', borderColor: '#1e1e2e', borderWidth: 1 } } }
});

// Top Products
new Chart(document.getElementById('topProductsChart').getContext('2d'), {
  type: 'bar',
  data: {
    labels: ['Smart Watch','Running Shoes','Wireless Earbuds','Slim Jeans','Dark Choc'],
    datasets: [{ data: [128940,98750,84320,52100,41800], backgroundColor: ['#FF6B35','#3b82f6','#34d399','#a78bfa','#FFB347'], borderRadius: 6 }]
  },
  options: { ...chartOpts({ indexAxis: 'y' }), plugins: { ...chartOpts().plugins, tooltip: { ...chartOpts().plugins.tooltip, callbacks: { label: ctx => ' LKR '+ctx.raw.toLocaleString() } } } }
});

// User Registrations
new Chart(document.getElementById('userRegChart').getContext('2d'), {
  type: 'line',
  data: {
    labels: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
    datasets: [{
      data: [280,310,290,420,380,510,470,560,490,640,580,710],
      borderColor: '#a78bfa', backgroundColor: 'rgba(167,139,250,0.1)',
      borderWidth: 2.5, pointRadius: 4, fill: true, tension: 0.4
    }]
  },
  options: chartOpts()
});
