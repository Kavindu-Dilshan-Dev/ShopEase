/* ═══════════════════════════════════════════
   PRODUCTS JS — Full Firebase CRUD
   Field names match Android Product.java exactly
   ═══════════════════════════════════════════ */

buildSidebar('products');

let allProducts   = [];
let editProductId = null;
let deleteId      = null;

// ── Initialise ───────────────────────────────
async function init() {
  // Populate category dropdowns from shared config
  CATEGORIES.forEach(c => {
    document.getElementById('catFilter').innerHTML +=
      `<option value="${c}">${c}</option>`;
    document.getElementById('pCategory').innerHTML +=
      `<option value="${c}">${c}</option>`;
  });
  await loadProducts();
}

// ── Load Products from Firebase ──────────────
async function loadProducts() {
  document.getElementById('loadingState').style.display  = 'block';
  document.getElementById('productsGrid').style.display  = 'none';
  document.getElementById('emptyState').style.display    = 'none';

  try {
    allProducts = await firestoreGet(COLLECTIONS.products);
    updateStats();
    renderProducts(allProducts);
  } catch (err) {
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('loadingState').innerHTML = `
      <div style="font-size:32px;margin-bottom:12px">❌</div>
      <p style="color:#f87171">Firebase error: ${err.message}</p>
      <p style="margin-top:8px;font-size:13px">
        Check <strong>js/firebase-config.js</strong>
        and update YOUR_PROJECT_ID
      </p>`;
    showToast('Firebase error: ' + err.message, 'error');
  }
}

// ── Update Stats Strip ───────────────────────
function updateStats() {
  document.getElementById('statTotal').textContent =
    allProducts.length;
  document.getElementById('statFeatured').textContent =
    allProducts.filter(p =>
      p.isFeatured === true || p.isFeatured === 'true').length;
  document.getElementById('statSale').textContent =
    allProducts.filter(p =>
      p.isOnSale === true || p.isOnSale === 'true').length;
  document.getElementById('statLow').textContent =
    allProducts.filter(p => Number(p.stockQuantity) < 10).length;
}

// ── Render Product Grid ──────────────────────
function renderProducts(list) {
  document.getElementById('loadingState').style.display = 'none';

  if (!list.length) {
    document.getElementById('emptyState').style.display   = 'block';
    document.getElementById('productsGrid').style.display = 'none';
    return;
  }

  document.getElementById('emptyState').style.display   = 'none';
  document.getElementById('productsGrid').style.display = 'grid';

  document.getElementById('productsGrid').innerHTML =
    list.map(p => `
      <div class="product-card">
        ${p.imageUrl
          ? `<img src="${p.imageUrl}" class="product-img"
               onerror="this.style.display='none';
               this.nextElementSibling.style.display='flex'"
               alt="${p.name}"/>
             <div class="product-img-placeholder"
               style="display:none">📦</div>`
          : `<div class="product-img-placeholder">📦</div>`}

        <div class="product-body">

          <!-- Badges -->
          <div style="display:flex;gap:6px;flex-wrap:wrap;
                      margin-bottom:8px">
            <span style="background:rgba(255,107,53,.15);
              color:var(--accent);font-size:11px;
              padding:2px 8px;border-radius:20px">
              ${p.category || 'Uncategorized'}
            </span>
            ${(p.isFeatured === true || p.isFeatured === 'true')
              ? `<span style="background:rgba(52,211,153,.15);
                   color:#34d399;font-size:11px;
                   padding:2px 8px;border-radius:20px">
                   ⭐ Featured</span>`
              : ''}
            ${(p.isOnSale === true || p.isOnSale === 'true')
              ? `<span style="background:rgba(248,113,113,.15);
                   color:#f87171;font-size:11px;
                   padding:2px 8px;border-radius:20px">
                   🏷 Sale</span>`
              : ''}
          </div>

          <p class="product-name">${p.name || '—'}</p>

          <p class="product-cat">
            Stock: ${p.stockQuantity || 0} units
            ${Number(p.stockQuantity) < 10
              ? `<span style="color:#f87171;font-size:11px">
                   ⚠️ Low</span>`
              : ''}
          </p>

          <p class="product-price">
            LKR ${Number(p.price || 0).toLocaleString()}
          </p>

          <p style="font-size:11px;color:var(--muted);margin-top:4px">
            ⭐ ${p.rating || 0} · ${p.reviewCount || 0} reviews
          </p>

          <div class="product-actions">
            <button class="pa-btn"
              onclick="openEditProduct('${p.id}')">
              ✏️ Edit
            </button>
            <button class="pa-btn danger"
              onclick="openDeleteProduct('${p.id}',
                '${(p.name || '').replace(/'/g,"\\'")}')">
              🗑 Delete
            </button>
          </div>

        </div>
      </div>`).join('');
}

// ── Filter Products ──────────────────────────
function filterProducts() {
  const q   = document.getElementById('productSearch').value.toLowerCase();
  const cat = document.getElementById('catFilter').value;
  renderProducts(allProducts.filter(p =>
    (!q   || (p.name || '').toLowerCase().includes(q)) &&
    (!cat || p.category === cat)
  ));
}

// ── Image URL Preview ────────────────────────
function previewImage(url) {
  const wrap = document.getElementById('imgPreviewWrap');
  const img  = document.getElementById('imgPreview');
  if (url && url.startsWith('http')) {
    img.src = url;
    wrap.style.display = 'block';
    img.onerror = () => { wrap.style.display = 'none'; };
  } else {
    wrap.style.display = 'none';
  }
}

// ── Open Add Modal ────────────────────────────
function openAddProduct() {
  editProductId = null;
  document.getElementById('modalTitle').textContent     = '➕ Add New Product';
  document.getElementById('btnSaveProduct').textContent = '🔥 Save to Firebase';
  clearForm();
  document.getElementById('productModal').style.display = 'flex';
}

// ── Open Edit Modal ───────────────────────────
function openEditProduct(id) {
  const p = allProducts.find(pr => pr.id === id);
  if (!p) return;

  editProductId = id;
  document.getElementById('modalTitle').textContent     = '✏️ Edit Product';
  document.getElementById('btnSaveProduct').textContent = '🔥 Update in Firebase';

  document.getElementById('pName').value          = p.name          || '';
  document.getElementById('pCategory').value      = p.category      || '';
  document.getElementById('pPrice').value         = p.price         || '';
  document.getElementById('pDiscountPrice').value = p.discountPrice || '';
  document.getElementById('pStock').value         = p.stockQuantity || '';
  document.getElementById('pDesc').value          = p.description   || '';
  document.getElementById('pImage').value         = p.imageUrl      || '';
  document.getElementById('pRating').value        = p.rating        || '';
  document.getElementById('pReviewCount').value   = p.reviewCount   || '';
  document.getElementById('pFeatured').checked    =
    p.isFeatured === true || p.isFeatured === 'true';
  document.getElementById('pOnSale').checked      =
    p.isOnSale === true || p.isOnSale === 'true';

  previewImage(p.imageUrl);
  document.getElementById('productModal').style.display = 'flex';
}

// ── Save Product to Firebase ──────────────────
async function saveProduct() {
  const name     = document.getElementById('pName').value.trim();
  const category = document.getElementById('pCategory').value;
  const price    = parseFloat(document.getElementById('pPrice').value);
  const stock    = parseInt(document.getElementById('pStock').value);

  // Validation
  if (!name)        { showToast('Product name is required', 'error'); return; }
  if (!category)    { showToast('Please select a category', 'error'); return; }
  if (isNaN(price)) { showToast('Please enter a valid price', 'error'); return; }
  if (isNaN(stock)) { showToast('Please enter stock quantity', 'error'); return; }

  const btn = document.getElementById('btnSaveProduct');
  const origText = btn.textContent;
  btn.textContent = 'Saving…';
  btn.disabled    = true;

  // ✅ Field names match Android Product.java EXACTLY
  const productData = {
    name:          name,
    description:   document.getElementById('pDesc').value.trim(),
    category:      category,
    imageUrl:      document.getElementById('pImage').value.trim(),
    price:         price,
    discountPrice: parseFloat(
      document.getElementById('pDiscountPrice').value) || price,
    rating:        parseFloat(
      document.getElementById('pRating').value) || 0,
    reviewCount:   parseInt(
      document.getElementById('pReviewCount').value) || 0,
    stockQuantity: stock,
    isFeatured:    document.getElementById('pFeatured').checked,
    isOnSale:      document.getElementById('pOnSale').checked,
  };

  try {
    if (editProductId) {
      await firestoreUpdate(
        COLLECTIONS.products, editProductId, productData);
      showToast(`✅ "${name}" updated in Firebase!`, 'success');
    } else {
      await firestoreAdd(COLLECTIONS.products, productData);
      showToast(
        `✅ "${name}" added! Now visible in the Android app.`,
        'success');
    }

    document.getElementById('productModal').style.display = 'none';
    await loadProducts();

  } catch (err) {
    showToast('Firebase error: ' + err.message, 'error');
  } finally {
    btn.textContent = origText;
    btn.disabled    = false;
  }
}

// ── Delete ────────────────────────────────────
function openDeleteProduct(id, name) {
  deleteId = id;
  document.getElementById('deleteProductName').textContent = name;
  document.getElementById('deleteModal').style.display = 'flex';
}

async function confirmDelete() {
  if (!deleteId) return;
  const btn = document.getElementById('btnConfirmDelete');
  btn.textContent = 'Deleting…';
  btn.disabled    = true;

  try {
    await firestoreDelete(COLLECTIONS.products, deleteId);
    document.getElementById('deleteModal').style.display = 'none';
    showToast('🗑 Product deleted from Firebase', 'info');
    await loadProducts();
  } catch (err) {
    showToast('Delete failed: ' + err.message, 'error');
  } finally {
    btn.textContent = 'Delete Permanently';
    btn.disabled    = false;
    deleteId        = null;
  }
}

// ── Clear Form ────────────────────────────────
function clearForm() {
  ['pName','pCategory','pPrice','pDiscountPrice',
   'pStock','pDesc','pImage','pRating','pReviewCount']
    .forEach(id => document.getElementById(id).value = '');
  document.getElementById('pFeatured').checked         = false;
  document.getElementById('pOnSale').checked           = false;
  document.getElementById('imgPreviewWrap').style.display = 'none';
}

// ── Start ─────────────────────────────────────
init();
