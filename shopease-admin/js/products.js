// ── Products JS ────────────────────────────────
let PRODUCTS = [
  { id:1, name:'Wireless Earbuds Pro', category:'Electronics', price:4999, stock:45, desc:'Noise cancelling wireless earbuds', image:'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400', emoji:'🎧' },
  { id:2, name:'Smart Watch Series 5', category:'Electronics', price:12999, stock:18, desc:'Track fitness and notifications', image:'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400', emoji:'⌚' },
  { id:3, name:'Premium Cotton T-Shirt', category:'Clothing', price:899, stock:120, desc:'Comfortable everyday cotton tee', image:'', emoji:'👕' },
  { id:4, name:'Slim Fit Jeans', category:'Clothing', price:2499, stock:60, desc:'Classic slim fit denim', image:'', emoji:'👖' },
  { id:5, name:'Organic Green Tea', category:'Food', price:649, stock:200, desc:'Pure Ceylon organic green tea', image:'', emoji:'🍵' },
  { id:6, name:'Dark Chocolate Box', category:'Food', price:1299, stock:85, desc:'Assorted premium dark chocolates', image:'', emoji:'🍫' },
  { id:7, name:'Clean Code Book', category:'Books', price:3200, stock:30, desc:'A handbook of Agile craftsmanship', image:'', emoji:'📚' },
  { id:8, name:'Yoga Mat Premium', category:'Sports', price:1899, stock:40, desc:'Non-slip 6mm thick yoga mat', image:'', emoji:'🧘' },
  { id:9, name:'Running Shoes', category:'Sports', price:5999, stock:25, desc:'Lightweight breathable running shoes', image:'', emoji:'👟' },
  { id:10, name:'Bluetooth Speaker', category:'Electronics', price:3499, stock:55, desc:'Portable 360° surround sound', image:'', emoji:'🔊' },
];
let editProdId = null;

function renderProducts(list) {
  const grid = document.getElementById('productsGrid');
  if (!grid) return;
  grid.innerHTML = list.map(p => `
    <div class="product-card">
      ${p.image
        ? `<img src="${p.image}" class="product-img" onerror="this.style.display='none';this.nextElementSibling.style.display='flex'" alt="${p.name}"/><div class="product-img-placeholder" style="display:none">${p.emoji}</div>`
        : `<div class="product-img-placeholder">${p.emoji}</div>`}
      <div class="product-body">
        <p class="product-name">${p.name}</p>
        <p class="product-cat">${p.category} · Stock: ${p.stock}</p>
        <p class="product-price">LKR ${p.price.toLocaleString()}</p>
        <div class="product-actions">
          <button class="pa-btn" onclick="openEditProduct(${p.id})">✏️ Edit</button>
          <button class="pa-btn danger" onclick="deleteProduct(${p.id})">🗑 Delete</button>
        </div>
      </div>
    </div>`).join('');
}

function filterProducts() {
  const q = document.getElementById('productSearch').value.toLowerCase();
  renderProducts(q ? PRODUCTS.filter(p => p.name.toLowerCase().includes(q) || p.category.toLowerCase().includes(q)) : PRODUCTS);
}

function openAddProduct() {
  editProdId = null;
  document.getElementById('productModalTitle').textContent = 'Add Product';
  ['pName','pPrice','pStock','pDesc','pImage'].forEach(id => document.getElementById(id).value = '');
  document.getElementById('productModal').style.display = 'flex';
}

function openEditProduct(id) {
  const p = PRODUCTS.find(pr => pr.id === id);
  if (!p) return;
  editProdId = id;
  document.getElementById('productModalTitle').textContent = 'Edit Product';
  document.getElementById('pName').value     = p.name;
  document.getElementById('pCategory').value = p.category;
  document.getElementById('pPrice').value    = p.price;
  document.getElementById('pStock').value    = p.stock;
  document.getElementById('pDesc').value     = p.desc;
  document.getElementById('pImage').value    = p.image;
  document.getElementById('productModal').style.display = 'flex';
}

function saveProduct() {
  const name  = document.getElementById('pName').value.trim();
  const price = parseFloat(document.getElementById('pPrice').value);
  const stock = parseInt(document.getElementById('pStock').value);
  if (!name || isNaN(price)) { showToast('Name and price are required', 'error'); return; }

  if (editProdId) {
    const p = PRODUCTS.find(pr => pr.id === editProdId);
    if (p) { p.name = name; p.category = document.getElementById('pCategory').value; p.price = price; p.stock = stock; p.desc = document.getElementById('pDesc').value; p.image = document.getElementById('pImage').value; }
    showToast('Product updated!', 'success');
  } else {
    PRODUCTS.push({ id: Date.now(), name, category: document.getElementById('pCategory').value, price, stock, desc: document.getElementById('pDesc').value, image: document.getElementById('pImage').value, emoji:'📦' });
    showToast('Product added!', 'success');
  }
  document.getElementById('productModal').style.display = 'none';
  renderProducts(PRODUCTS);
}

function deleteProduct(id) {
  if (!confirm('Delete this product?')) return;
  PRODUCTS = PRODUCTS.filter(p => p.id !== id);
  renderProducts(PRODUCTS);
  showToast('Product deleted', 'info');
}

renderProducts(PRODUCTS);
