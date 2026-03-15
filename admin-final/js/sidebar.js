/* ═══════════════════════════════════════════
   SIDEBAR BUILDER
   Call buildSidebar('pagename') in each page
   ═══════════════════════════════════════════ */

function buildSidebar(activePage) {
  const pages = [
    {
      id: 'dashboard',
      href: 'dashboard.html',
      label: 'Dashboard',
      icon: `<path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0
        01-1 1H3a1 1 0 01-1-1v-5zM8 7a1 1 0 011-1h2a1 1 0
        011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zM14 4a1 1 0
        011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0
        01-1-1V4z"/>`
    },
    {
      id: 'products',
      href: 'products.html',
      label: 'Products',
      icon: `<path fill-rule="evenodd" d="M10 2a4 4 0 00-4 4v1H5a1
        1 0 00-.994.89l-1 9A1 1 0 004 18h12a1 1 0
        00.994-1.11l-1-9A1 1 0 0015 7h-1V6a4 4 0 00-4-4zm2
        5V6a2 2 0 10-4 0v1h4z" clip-rule="evenodd"/>`
    },
    {
      id: 'orders',
      href: 'orders.html',
      label: 'Orders',
      badge: true,
      icon: `<path d="M3 1a1 1 0 000 2h1.22l.305 1.222a.997.997 0
        00.01.042l1.358 5.43-.893.892C3.74 11.846 4.632 14
        6.414 14H15a1 1 0 000-2H6.414l1-1H14a1 1 0
        00.894-.553l3-6A1 1 0 0017 3H6.28l-.31-1.243A1 1 0
        005 1H3z"/>`
    },
    {
      id: 'users',
      href: 'users.html',
      label: 'Users',
      icon: `<path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0
        11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97
        6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0
        015 5v1H1v-1a5 5 0 015-5z"/>`
    },
    {
      id: 'notifications',
      href: 'notifications.html',
      label: 'Notifications',
      icon: `<path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0
        004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0
        00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z"/>`
    },
    {
      id: 'analytics',
      href: 'analytics.html',
      label: 'Analytics',
      icon: `<path fill-rule="evenodd" d="M3 3a1 1 0 000 2v8a2 2 0
        002 2h2.586l-1.293 1.293a1 1 0 101.414 1.414L10
        15.414l2.293 2.293a1 1 0 001.414-1.414L12.414 15H15a2
        2 0 002-2V5a1 1 0 100-2H3zm11 4a1 1 0 10-2 0v4a1 1 0
        102 0V7zm-3 1a1 1 0 10-2 0v3a1 1 0 102 0V8zM8 9a1 1 0
        00-2 0v2a1 1 0 102 0V9z" clip-rule="evenodd"/>`
    }
  ];

  const navHTML = pages.map(p => `
    <a href="${p.href}"
       class="nav-item ${p.id === activePage ? 'active' : ''}">
      <svg viewBox="0 0 20 20" fill="currentColor">${p.icon}</svg>
      ${p.label}
      ${p.badge ? `<span class="nav-badge" id="pendingBadge">0</span>` : ''}
    </a>`).join('');

  const sidebarHTML = `
    <div class="sidebar-brand">
      <svg viewBox="0 0 40 40" fill="none" width="32">
        <rect width="40" height="40" rx="10" fill="#FF6B35"/>
        <path d="M10 14h20M10 20h14M10 26h18" stroke="#fff"
          stroke-width="2.5" stroke-linecap="round"/>
        <circle cx="28" cy="26" r="4" fill="#fff"/>
      </svg>
      <span>ShopEase</span>
    </div>
    <nav class="sidebar-nav">${navHTML}</nav>
    <div class="sidebar-footer">
      <div class="admin-avatar">
        <div class="avatar-ring">A</div>
        <div>
          <p class="admin-name" id="adminName">Admin</p>
          <p class="admin-role">Super Admin</p>
        </div>
      </div>
      <button class="btn-logout" onclick="logout()">
        <svg viewBox="0 0 20 20" fill="currentColor" width="18">
          <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0
            102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414
            1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414
            1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293
            1.293z" clip-rule="evenodd"/>
        </svg>
      </button>
    </div>`;

  document.getElementById('sidebar').innerHTML = sidebarHTML;
}
