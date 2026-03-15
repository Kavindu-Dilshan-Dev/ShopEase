/* ═══════════════════════════════════════════
   USERS JS — Firebase Live
   ═══════════════════════════════════════════ */

buildSidebar('users');

let allUsers = [];

// ── Load Users from Firebase ─────────────────
async function loadUsers() {
  document.getElementById('usersBody').innerHTML = `
    <tr>
      <td colspan="5"
        style="text-align:center;color:var(--muted);padding:32px">
        🔥 Loading from Firebase…
      </td>
    </tr>`;

  try {
    allUsers = await firestoreGet(COLLECTIONS.users);
    filterUsers();
  } catch (err) {
    document.getElementById('usersBody').innerHTML = `
      <tr>
        <td colspan="5"
          style="text-align:center;color:#f87171;padding:32px">
          ❌ Firebase error: ${err.message}
        </td>
      </tr>`;
    showToast('Firebase error: ' + err.message, 'error');
  }
}

// ── Filter Users ─────────────────────────────
function filterUsers() {
  const q = document.getElementById('userSearch').value.toLowerCase();
  const filtered = q
    ? allUsers.filter(u =>
        (u.name  || '').toLowerCase().includes(q) ||
        (u.email || '').toLowerCase().includes(q))
    : allUsers;
  renderUsers(filtered);
}

// ── Render Users Table ────────────────────────
function renderUsers(list) {
  document.getElementById('userCount').textContent =
    `${list.length} users`;

  if (!list.length) {
    document.getElementById('usersBody').innerHTML = `
      <tr>
        <td colspan="5"
          style="text-align:center;color:var(--muted);padding:32px">
          No users found
        </td>
      </tr>`;
    return;
  }

  document.getElementById('usersBody').innerHTML =
    list.map(u => {
      const initial = (u.name || u.email || '?')
        .charAt(0).toUpperCase();
      const date = u.createdAt
        ? new Date(Number(u.createdAt)).toLocaleDateString('en-US', {
            day: 'numeric', month: 'short', year: 'numeric'
          })
        : '—';

      return `
        <tr>
          <td>
            <div class="user-info">
              ${u.profileImageUrl
                ? `<img src="${u.profileImageUrl}"
                     style="width:32px;height:32px;border-radius:50%;
                            object-fit:cover"
                     onerror="this.outerHTML=
                       '<div class=user-av>${initial}</div>'"/>`
                : `<div class="user-av">${initial}</div>`}
              <span class="user-name-text">
                ${u.name || '—'}
              </span>
            </div>
          </td>
          <td style="color:var(--muted)">${u.email   || '—'}</td>
          <td style="color:var(--muted)">${u.phone   || '—'}</td>
          <td style="color:var(--muted);font-size:12px;
                     max-width:180px;overflow:hidden;
                     text-overflow:ellipsis;white-space:nowrap">
            ${u.address || '—'}
          </td>
          <td style="color:var(--muted)">${date}</td>
        </tr>`;
    }).join('');
}

// ── Start ─────────────────────────────────────
loadUsers();
