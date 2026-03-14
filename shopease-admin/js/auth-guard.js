// ── Auth Guard ─────────────────────────────────
if (!sessionStorage.getItem('shopease_admin')) {
  window.location.href = '../index.html';
}
document.getElementById('adminName').textContent =
  sessionStorage.getItem('admin_user') || 'Admin';

function logout() {
  sessionStorage.clear();
  window.location.href = '../index.html';
}
function toggleSidebar() {
  document.getElementById('sidebar').classList.toggle('open');
}
function closeModal(e) {
  if (e.target.classList.contains('modal-overlay'))
    e.target.style.display = 'none';
}

// ── Toast Helper ───────────────────────────────
function showToast(msg, type = 'success') {
  const area = document.getElementById('toastArea');
  if (!area) return;
  const icons = {
    success: '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>',
    error:   '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>',
    info:    '<path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>',
  };
  const t = document.createElement('div');
  t.className = `toast ${type}`;
  t.innerHTML = `<svg class="toast-icon" viewBox="0 0 20 20" fill="currentColor">${icons[type]}</svg><span class="toast-msg">${msg}</span><button class="toast-close" onclick="this.parentElement.remove()">✕</button>`;
  area.appendChild(t);
  setTimeout(() => t.remove(), 4000);
}
