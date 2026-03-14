// ── Users JS ───────────────────────────────────
const USERS_DATA = [
  { id:1, name:'Kavinda Perera',       email:'kavinda@gmail.com',  phone:'+94771234567', orders:12, spent:48200, joined:'2024-01-15', status:'active' },
  { id:2, name:'Nimal Silva',          email:'nimal@gmail.com',    phone:'+94712345678', orders:8,  spent:31500, joined:'2024-02-20', status:'active' },
  { id:3, name:'Dilini Fernando',      email:'dilini@gmail.com',   phone:'+94723456789', orders:5,  spent:15800, joined:'2024-03-10', status:'inactive' },
  { id:4, name:'Roshan Bandara',       email:'roshan@gmail.com',   phone:'+94734567890', orders:20, spent:89400, joined:'2023-11-05', status:'active' },
  { id:5, name:'Sanduni Jayawardena',  email:'sanduni@gmail.com',  phone:'+94745678901', orders:3,  spent:8700,  joined:'2024-06-18', status:'inactive' },
  { id:6, name:'Tharaka Wijesinghe',   email:'tharaka@gmail.com',  phone:'+94756789012', orders:15, spent:62300, joined:'2023-12-22', status:'active' },
  { id:7, name:'Priya Gamage',         email:'priya@gmail.com',    phone:'+94767890123', orders:7,  spent:22100, joined:'2024-04-03', status:'active' },
  { id:8, name:'Kasun Rajapaksa',      email:'kasun@gmail.com',    phone:'+94778901234', orders:25, spent:112000,joined:'2023-09-14', status:'active' },
  { id:9, name:'Amali Wickrama',       email:'amali@gmail.com',    phone:'+94789012345', orders:4,  spent:12500, joined:'2024-07-28', status:'active' },
  { id:10, name:'Dasun Hettige',       email:'dasun@gmail.com',    phone:'+94790123456', orders:11, spent:43700, joined:'2024-01-30', status:'inactive' },
];

function getInitial(name) { return name.charAt(0).toUpperCase(); }

function renderUsers(list) {
  const tbody = document.getElementById('usersTableBody');
  if (!tbody) return;
  tbody.innerHTML = list.map(u => `
    <tr>
      <td>
        <div class="user-info">
          <div class="user-av">${getInitial(u.name)}</div>
          <span class="user-name-text">${u.name}</span>
        </div>
      </td>
      <td style="color:var(--muted)">${u.email}</td>
      <td style="color:var(--muted)">${u.phone}</td>
      <td style="text-align:center">${u.orders}</td>
      <td style="color:var(--accent);font-weight:600">LKR ${u.spent.toLocaleString()}</td>
      <td style="color:var(--muted)">${u.joined}</td>
      <td><span class="status-badge ${u.status==='active'?'status-CONFIRMED':'status-CANCELLED'}">${u.status}</span></td>
      <td>
        <button class="pa-btn" style="width:auto;padding:5px 10px" onclick="sendUserNotif(${u.id})">📱 Notify</button>
      </td>
    </tr>`).join('');
  document.getElementById('userCount').textContent = `Showing ${list.length} users`;
}

function filterUsers() {
  const q      = (document.getElementById('userSearch')?.value || '').toLowerCase();
  const status = document.getElementById('userStatusFilter')?.value || '';
  const filtered = USERS_DATA.filter(u =>
    (!q || u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)) &&
    (!status || u.status === status)
  );
  renderUsers(filtered);
}

function sendUserNotif(id) {
  const user = USERS_DATA.find(u => u.id === id);
  if (!user) return;
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification('👤 User Notification Sent', { body: `Notification sent to ${user.name} (${user.email})` });
  }
  showToast(`Notification sent to ${user.name}`, 'success');
}

renderUsers(USERS_DATA);
