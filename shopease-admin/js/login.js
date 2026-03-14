// ── Login JS ────────────────────────────────
const ADMIN_CREDENTIALS = {
  username: 'admin',
  password: 'shopease@2025'
};

const form     = document.getElementById('loginForm');
const errorMsg = document.getElementById('errorMsg');
const btnLogin = document.querySelector('.btn-login');

form.addEventListener('submit', function (e) {
  e.preventDefault();
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value.trim();

  errorMsg.style.display = 'none';
  btnLogin.classList.add('loading');
  btnLogin.querySelector('span').textContent = 'Signing in…';

  // Simulate async auth check
  setTimeout(() => {
    if (username === ADMIN_CREDENTIALS.username &&
        password === ADMIN_CREDENTIALS.password) {
      sessionStorage.setItem('shopease_admin', 'true');
      sessionStorage.setItem('admin_user', username);
      window.location.href = 'pages/dashboard.html';
    } else {
      errorMsg.style.display = 'flex';
      btnLogin.classList.remove('loading');
      btnLogin.querySelector('span').textContent = 'Sign In';
      document.getElementById('password').value = '';
    }
  }, 800);
});

function togglePassword() {
  const input = document.getElementById('password');
  const icon  = document.getElementById('eyeIcon');
  if (input.type === 'password') {
    input.type = 'text';
    icon.innerHTML = '<path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clip-rule="evenodd"/><path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z"/>';
  } else {
    input.type = 'password';
    icon.innerHTML = '<path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/><path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/>';
  }
}

// Auto-redirect if already logged in
if (sessionStorage.getItem('shopease_admin')) {
  window.location.href = 'pages/dashboard.html';
}
