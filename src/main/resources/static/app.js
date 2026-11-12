const ROUTES = {
  '/': 'Home',
  '/new-cars': 'New Cars',
  '/used-cars': 'Second-Hand Cars',
  '/services': 'Services',
  '/contact': 'Contact Us',
  '/admin': 'Admin Dashboard'
};
const MAX_PHOTOS = 5;
const MAX_PHOTO_BYTES = 2 * 1024 * 1024;
const ALLOWED_PHOTO_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);
const VEHICLE_FORM_FIELDS = [
  'make', 'model', 'year', 'vin', 'bodyType', 'price', 'mileage', 'color', 'sellerName',
  'conditionType', 'variant', 'fuelType', 'transmission', 'engineSpec', 'powerOutput',
  'torqueOutput', 'efficiencyRange', 'drivetrain', 'seatCount', 'warranty'
];
let vehicles = [];
let adminVehicles = [];
let photoPreviewUrls = [];
let conditionFilter = 'ALL';
let currentRoute = '/';
let currentEditingVehicleId = null;
let currentEditingPhotoCount = 0;
let currentPhotoVehicleId = null;
let selectedPurchaseVehicleId = null;
let selectedDetailsVehicleId = null;
let csrfToken;
let listingRenderToken = 0;
let toastTimer;
const $ = (selector) => document.querySelector(selector);
const money = (value) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number(value) || 0);
const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
const conditionLabel = (value) => String(value).toUpperCase() === 'NEW' ? 'New' : 'Second-Hand';
const isUnsafeMethod = (method) => !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(String(method || 'GET').toUpperCase());

async function getCsrfToken() {
  if (csrfToken) return csrfToken;
  const response = await fetch('/api/csrf', { headers: { Accept: 'application/json' } });
  if (!response.ok) throw new Error('Secure request token could not be prepared. Refresh and try again.');
  const result = await response.json();
  csrfToken = { token: result.token, headerName: result.headerName || 'X-CSRF-TOKEN' };
  return csrfToken;
}

async function requestJson(url, options = {}) {
  const isMultipart = typeof FormData !== 'undefined' && options.body instanceof FormData;
  const method = String(options.method || 'GET').toUpperCase();
  const headers = {
    ...(options.body && !isMultipart ? { 'Content-Type': 'application/json' } : {}),
    ...(options.headers || {})
  };
  if (isUnsafeMethod(method)) {
    const csrf = await getCsrfToken();
    headers[csrf.headerName] = csrf.token;
  }
  const response = await fetch(url, { ...options, method, headers, credentials: 'same-origin' });
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.message || body.detail || body.error || message;
    } catch (_) { /* retain the useful HTTP status for non-JSON security responses */ }
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return response.json();
}

function setDatabaseError(message) {
  const alert = $('#databaseAlert');
  if (!alert) return;
  alert.hidden = false;
  alert.textContent = `MySQL connection required: ${message}. Check the configured MySQL service and database settings.`;
}

function setRoute(path, push = true) {
  const route = ROUTES[path] ? path : '/';
  currentRoute = route;
  document.querySelectorAll('[data-page]').forEach((page) => { page.hidden = page.dataset.page !== route; });
  document.querySelectorAll('.nav-link[data-route]').forEach((link) => link.classList.toggle('active', link.getAttribute('href') === route));
  $('#currentPage').textContent = ROUTES[route];
  document.title = `${ROUTES[route]} — AutoAdmin Motors`;
  $('#sidebar').classList.remove('open');
  if (push && location.pathname !== route) history.pushState({ route }, '', route);
  if (route === '/admin') loadAdminDashboard();
  else renderListings();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showToast(message) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('show'), 3500);
}

async function photoFor(vehicleId) {
  try {
    const photos = await requestJson(`/api/vehicles/${encodeURIComponent(vehicleId)}/photos`);
    return Array.isArray(photos) ? photos[0] : null;
  } catch (_) {
    return null;
  }
}

function listingTarget() {
  if (currentRoute === '/new-cars') return { condition: 'NEW', grid: '#newListingGrid', count: '#newListingCount', search: '#newInventorySearch' };
  if (currentRoute === '/used-cars') return { condition: 'USED', grid: '#usedListingGrid', count: '#usedListingCount', search: '#usedInventorySearch' };
  if (currentRoute === '/') return { condition: conditionFilter, grid: '#listingGrid', count: '#listingCount', search: '#inventorySearch' };
  return null;
}

function selectedVehicles(condition, search) {
  const query = (search || '').trim().toLowerCase();
  return vehicles.filter((car) => {
    const carCondition = String(car.conditionType || 'USED').toUpperCase();
    const matchesCondition = condition === 'ALL' || carCondition === condition;
    const searchable = [car.year, car.make, car.model, car.variant, car.bodyType, car.fuelType, car.transmission, car.engineSpec].join(' ').toLowerCase();
    return matchesCondition && searchable.includes(query);
  });
}

async function renderListings() {
  const target = listingTarget();
  if (!target) return;
  const renderToken = ++listingRenderToken;
  const matches = selectedVehicles(target.condition, $(target.search).value || '');
  const label = target.condition === 'ALL' ? 'available cars' : target.condition === 'NEW' ? 'new cars' : 'second-hand cars';
  document.querySelectorAll('[data-condition-filter]').forEach((button) => {
    const active = button.dataset.conditionFilter === conditionFilter;
    button.classList.toggle('active', active);
    button.setAttribute('aria-pressed', String(active));
  });
  if (!matches.length) {
    if (renderToken !== listingRenderToken) return;
    const message = vehicles.length
      ? `No ${label} match this view yet. Try another search.`
      : 'The showroom is ready for its first New or Second-Hand listing.';
    $(target.count).textContent = `0 ${label} in the AutoAdmin Motors showroom`;
    $(target.grid).innerHTML = `<div class="empty-state listing-empty col-12"><strong>${escapeHtml(message)}</strong></div>`;
    return;
  }
  const cards = await Promise.all(matches.map(async (car) => {
    const photo = await photoFor(car.id);
    const image = photo
      ? `<img src="/api/vehicles/${encodeURIComponent(car.id)}/photos/${encodeURIComponent(photo.id)}" alt="${escapeHtml(`${car.year} ${car.make} ${car.model}`)}" loading="lazy">`
      : '<div class="listing-photo-placeholder" aria-hidden="true"><span>◈</span><small>SHOWROOM LISTING</small></div>';
    const isNew = String(car.conditionType || 'USED').toUpperCase() === 'NEW';
    const subtitle = isNew
      ? [car.variant, car.fuelType, car.transmission].filter(Boolean).join(' · ') || 'New vehicle'
      : `${Number(car.mileage || 0).toLocaleString('en-IN')} km · ${car.color || 'Colour on request'}`;
    const specChips = isNew
      ? [car.engineSpec, car.efficiencyRange].filter(Boolean).slice(0, 2).map((item) => `<span>${escapeHtml(item)}</span>`).join('')
      : `<span>${escapeHtml(car.bodyType || 'Car')}</span><span>${escapeHtml(car.color || 'Colour on request')}</span>`;
    return `<div class="col"><article class="public-listing-card card h-100"><div class="listing-photo">${image}<span class="condition-ribbon ${isNew ? 'condition-new' : 'condition-used'}">${isNew ? 'NEW' : 'SECOND-HAND'}</span></div><div class="listing-card-body"><span class="panel-kicker">${escapeHtml(car.year)} · ${escapeHtml(car.bodyType || 'CAR')}</span><h3>${escapeHtml(car.make)} ${escapeHtml(car.model)}</h3><p class="listing-variant">${escapeHtml(subtitle)}</p><div class="listing-spec-chips">${specChips || '<span>Details available</span>'}</div><div class="listing-card-footer"><strong>${money(car.price)}</strong><div class="listing-card-actions"><button class="btn btn-sm btn-outline-primary" type="button" data-view-vehicle="${escapeHtml(car.id)}">Details</button><button class="btn btn-sm btn-primary" type="button" data-buy-vehicle="${escapeHtml(car.id)}">Buy</button></div></div></div></article></div>`;
  }));
  if (renderToken !== listingRenderToken) return;
  $(target.count).textContent = `${matches.length} ${label} in the AutoAdmin Motors showroom`;
  $(target.grid).innerHTML = cards.join('');
}

async function loadListings() {
  try {
    const records = await requestJson('/api/vehicles');
    vehicles = Array.isArray(records) ? records : [];
    await renderListings();
  } catch (error) {
    setDatabaseError(error.message);
    const target = listingTarget();
    if (target) {
      $(target.count).textContent = 'Showroom listings could not be loaded.';
      $(target.grid).innerHTML = '<div class="empty-state listing-empty col-12">The MySQL connection is unavailable. No sample cars are being shown.</div>';
    }
  }
}

function showVehicleDetails(vehicleId) {
  const car = vehicles.find((item) => String(item.id) === String(vehicleId));
  if (!car) return;
  selectedDetailsVehicleId = car.id;
  const isNew = String(car.conditionType || 'USED').toUpperCase() === 'NEW';
  $('#vehicleDetailsCondition').textContent = `${conditionLabel(car.conditionType)} · ${car.year || 'Model year'} · ${car.bodyType || 'Vehicle'}`;
  $('#vehicleDetailsTitle').textContent = `${car.make} ${car.model}${car.variant ? ` · ${car.variant}` : ''}`;
  const specs = [
    ['Condition', conditionLabel(car.conditionType)], ['Model year', car.year], ['Variant / trim', car.variant],
    ['Body type', car.bodyType], ['Fuel / powertrain', car.fuelType], ['Transmission', car.transmission],
    ['Engine / motor', car.engineSpec], ['Power output', car.powerOutput], ['Torque', car.torqueOutput],
    ['Efficiency / range', car.efficiencyRange], ['Drivetrain', car.drivetrain],
    ['Seating capacity', car.seatCount ? `${car.seatCount} seats` : null], ['Manufacturer warranty', car.warranty],
    ['Mileage', isNew ? null : `${Number(car.mileage || 0).toLocaleString('en-IN')} km`],
    ['Colour', car.color], ['Price', money(car.price)]
  ].filter(([, value]) => value !== null && value !== undefined && String(value).trim() !== '');
  $('#vehicleDetailsBody').innerHTML = `<div class="specification-grid">${specs.map(([label, value]) => `<div class="specification-item"><span>${escapeHtml(label)}</span><strong>${escapeHtml(value)}</strong></div>`).join('')}</div><p class="specification-note">Specifications shown are the details saved with this listing.</p>`;
  bootstrap.Modal.getOrCreateInstance($('#vehicleDetailsModal')).show();
}

function openPurchase(vehicleId) {
  const car = vehicles.find((item) => String(item.id) === String(vehicleId));
  if (!car) return;
  selectedPurchaseVehicleId = car.id;
  $('#demoCarSummary').innerHTML = `<strong>${escapeHtml(car.year)} ${escapeHtml(car.make)} ${escapeHtml(car.model)}</strong><span>${money(car.price)} · ${escapeHtml(conditionLabel(car.conditionType))}</span>`;
  $('#purchaseFeedback').textContent = 'Choose Demo Card or Demo UPI, then select a result to simulate.';
  $('#purchaseFeedback').classList.remove('feedback-error', 'feedback-success');
  $('#demoPurchaseForm').reset();
  bootstrap.Modal.getOrCreateInstance($('#demoPurchaseModal')).show();
}

function clearPhotoPreviews() {
  photoPreviewUrls.forEach((url) => URL.revokeObjectURL(url));
  photoPreviewUrls = [];
}

function updatePhotoPreview() {
  clearPhotoPreviews();
  const input = $('#vehiclePhotos');
  const files = Array.from(input.files || []);
  const invalidType = files.find((file) => !ALLOWED_PHOTO_TYPES.has(file.type));
  const tooLarge = files.find((file) => file.size > MAX_PHOTO_BYTES);
  let error = '';
  if (files.length + currentEditingPhotoCount > MAX_PHOTOS) error = `Choose no more than ${MAX_PHOTOS - currentEditingPhotoCount} additional photos.`;
  else if (invalidType) error = 'Use JPG, PNG, or WebP image files only.';
  else if (tooLarge) error = 'Each photo must be 2 MB or smaller.';
  input.setCustomValidity(error);
  $('#photoFeedback').textContent = error || (files.length ? `${files.length} new photo${files.length === 1 ? '' : 's'} ready to upload.` : 'No new photos selected.');
  $('#photoFeedback').classList.toggle('feedback-error', Boolean(error));
  $('#photoPreviewGrid').innerHTML = files.map((file) => {
    const url = URL.createObjectURL(file);
    photoPreviewUrls.push(url);
    return `<div class="photo-preview"><img src="${url}" alt="Selected car photo preview"><span>${escapeHtml(file.name)}</span></div>`;
  }).join('');
}

function updateListingMode() {
  const condition = $('#conditionType').value;
  const isNew = condition === 'NEW';
  $('#newCarSpecs').hidden = !isNew;
  $('#vinField').hidden = isNew;
  $('#vin').required = !isNew;
  document.querySelectorAll('[data-new-required]').forEach((field) => { field.required = isNew; });
  $('#listingPriceLabel').textContent = isNew ? 'Ex-showroom / list price (₹)' : 'Asking price (₹)';
  $('#listingMileageLabel').textContent = isNew ? 'Odometer (km)' : 'Mileage (km)';
  if (!isNew) document.querySelectorAll('#newCarSpecs input, #newCarSpecs select').forEach((field) => { field.value = ''; });
}

function resetListingForm() {
  currentEditingVehicleId = null;
  currentEditingPhotoCount = 0;
  clearPhotoPreviews();
  $('#listingForm').reset();
  $('#conditionType').value = 'USED';
  $('#listingFeedback').textContent = '';
  $('#listingFormTitle').textContent = 'Add a car';
  $('#saveListingButton').innerHTML = 'Save car to MySQL <span>→</span>';
  $('#listingModeHint').textContent = 'Enter the vehicle details and attach up to five photos.';
  updateListingMode();
  updatePhotoPreview();
}

async function submitListing(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const input = $('#vehiclePhotos');
  const feedback = $('#listingFeedback');
  if (!form.reportValidity()) return;
  if (input.files.length + currentEditingPhotoCount > MAX_PHOTOS) {
    feedback.textContent = 'A car can have no more than five photos in total.';
    return;
  }
  if (Array.from(input.files).some((file) => !ALLOWED_PHOTO_TYPES.has(file.type) || file.size > MAX_PHOTO_BYTES)) {
    feedback.textContent = 'Photos must be JPG, PNG, or WebP, up to 2 MB each.';
    return;
  }
  const payload = new FormData(form);
  payload.delete('photos');
  Array.from(input.files).forEach((file) => payload.append('photos', file, file.name));
  const isEdit = currentEditingVehicleId !== null;
  const endpoint = isEdit ? `/api/admin/vehicles/${encodeURIComponent(currentEditingVehicleId)}` : '/api/admin/vehicles';
  try {
    const saved = await requestJson(endpoint, { method: 'POST', body: payload });
    const photoCount = input.files.length;
    feedback.textContent = `${conditionLabel(saved.conditionType)} car${photoCount ? ` and ${photoCount} photo${photoCount === 1 ? '' : 's'}` : ''} saved to MySQL.`;
    resetListingForm();
    await Promise.all([loadListings(), loadAdminInventory()]);
    showToast(isEdit ? 'Car details updated.' : 'Car added to the showroom.');
  } catch (error) {
    feedback.textContent = `Could not save the car: ${error.message}`;
    feedback.classList.add('feedback-error');
  }
}

async function submitContact(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const data = new FormData(form);
  const message = {
    name: data.get('name').trim(), email: data.get('email').trim(), phone: data.get('phone').trim(),
    subject: data.get('subject').trim(), message: data.get('message').trim()
  };
  try {
    await requestJson('/api/contact', { method: 'POST', body: JSON.stringify(message) });
    form.reset();
    $('#contactFeedback').textContent = 'Your message was saved. Thank you for contacting AutoAdmin Motors.';
  } catch (error) {
    $('#contactFeedback').textContent = `Could not save your message to MySQL: ${error.message}`;
    $('#contactFeedback').classList.add('feedback-error');
  }
}

async function submitDemoPurchase(event) {
  event.preventDefault();
  if (!selectedPurchaseVehicleId) return;
  const form = event.currentTarget;
  if (!form.reportValidity()) return;
  const data = new FormData(form);
  const payload = {
    buyerName: data.get('buyerName').trim(), buyerEmail: data.get('buyerEmail').trim(),
    buyerPhone: data.get('buyerPhone').trim(), paymentMethod: data.get('paymentMethod'),
    simulatedOutcome: data.get('simulatedOutcome')
  };
  const button = $('#purchaseSubmitButton');
  button.disabled = true;
  try {
    const result = await requestJson(`/api/purchases/${encodeURIComponent(selectedPurchaseVehicleId)}`, {
      method: 'POST', body: JSON.stringify(payload)
    });
    const feedback = $('#purchaseFeedback');
    feedback.textContent = `${result.message}${result.reference ? ` Demo reference: ${result.reference}.` : ''}`;
    feedback.classList.toggle('feedback-success', result.success);
    feedback.classList.toggle('feedback-error', !result.success);
    await Promise.all([loadListings(), currentRoute === '/admin' ? loadAdminDashboard() : Promise.resolve()]);
    showToast(result.success ? 'Demo purchase recorded.' : 'Demo decline recorded; the car is still available.');
  } catch (error) {
    $('#purchaseFeedback').textContent = error.message;
    $('#purchaseFeedback').classList.add('feedback-error');
  } finally {
    button.disabled = false;
  }
}

function dateText(value) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? escapeHtml(value) : escapeHtml(new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(date));
}

async function loadAdminDashboard() {
  try {
    const session = await requestJson('/api/admin/session');
    $('#adminUsername').textContent = session.username;
    $('#adminNavLink').hidden = false;
    $('#adminLoginLink').hidden = true;
    $('#topAdminLink').hidden = true;
    const csrf = await getCsrfToken();
    $('#logoutCsrf').value = csrf.token;
    await Promise.all([loadAdminInventory(), loadAdminHistory()]);
  } catch (error) {
    if (currentRoute === '/admin') {
      $('#adminInventoryBody').innerHTML = `<tr><td colspan="6" class="empty-state">Admin data could not be loaded: ${escapeHtml(error.message)}. Sign in again.</td></tr>`;
    }
  }
}

async function loadAdminInventory() {
  try {
    adminVehicles = await requestJson('/api/admin/vehicles');
    const available = adminVehicles.filter((car) => car.status === 'AVAILABLE').length;
    $('#availableMetric').textContent = String(available);
    if (!adminVehicles.length) {
      $('#adminInventoryBody').innerHTML = '<tr><td colspan="6" class="empty-state">No inventory yet. Add the first car to the showroom.</td></tr>';
      return;
    }
    const photos = await Promise.all(adminVehicles.map(async (car) => {
      try { return await requestJson(`/api/admin/vehicles/${encodeURIComponent(car.id)}/photos`); } catch (_) { return []; }
    }));
    $('#adminInventoryBody').innerHTML = adminVehicles.map((car, index) => {
      const isAvailable = car.status === 'AVAILABLE';
      const label = `${car.year} ${car.make} ${car.model}`;
      return `<tr><td><div class="vehicle-label"><strong>${escapeHtml(label)}</strong><small>${escapeHtml(car.variant || car.bodyType || 'Vehicle')}</small></div></td><td><span class="condition-admin-pill">${escapeHtml(conditionLabel(car.conditionType))}</span><br><span class="status-pill ${isAvailable ? '' : 'status-sold'}">${escapeHtml(car.status)}</span></td><td>${money(car.price)}</td><td>${escapeHtml(car.vin || '—')}</td><td>${photos[index].length} / ${MAX_PHOTOS}</td><td><div class="admin-row-actions"><button class="table-action" type="button" data-edit-vehicle="${escapeHtml(car.id)}" ${isAvailable ? '' : 'disabled'}>Edit</button><button class="table-action" type="button" data-manage-photos="${escapeHtml(car.id)}" ${isAvailable ? '' : 'disabled'}>Photos</button>${isAvailable ? `<button class="table-action" type="button" data-record-sale="${escapeHtml(car.id)}">Record sale</button>` : ''}<button class="table-action table-action-danger" type="button" data-delete-vehicle="${escapeHtml(car.id)}">Delete</button></div></td></tr>`;
    }).join('');
  } catch (error) {
    $('#adminInventoryBody').innerHTML = `<tr><td colspan="6" class="empty-state">Could not load Admin inventory: ${escapeHtml(error.message)}</td></tr>`;
  }
}

async function loadAdminHistory() {
  try {
    const [sales, attempts] = await Promise.all([
      requestJson('/api/admin/sales'), requestJson('/api/admin/payment-attempts')
    ]);
    $('#salesMetric').textContent = String(sales.length);
    $('#attemptsMetric').textContent = String(attempts.length);
    $('#salesHistoryBody').innerHTML = sales.length ? sales.map((sale) => `<tr><td>#${escapeHtml(sale.id)}</td><td>${escapeHtml(`${sale.year} ${sale.make} ${sale.model}`)}</td><td><div class="vehicle-label"><strong>${escapeHtml(sale.buyerName)}</strong><small>${escapeHtml(sale.buyerEmail)}</small></div></td><td>${money(sale.salePrice)}</td><td><span class="status-pill">${escapeHtml(sale.channel.replace('_', ' '))}</span></td><td>${dateText(sale.soldAt)}</td></tr>`).join('') : '<tr><td colspan="6" class="empty-state">No completed sales have been recorded yet.</td></tr>';
    $('#paymentHistoryBody').innerHTML = attempts.length ? attempts.map((attempt) => `<tr><td>${escapeHtml(attempt.reference)}</td><td>${escapeHtml(attempt.vehicleName)}</td><td><div class="vehicle-label"><strong>${escapeHtml(attempt.buyerName)}</strong><small>${escapeHtml(attempt.buyerEmail)}</small></div></td><td>${money(attempt.amount)}</td><td>${escapeHtml(attempt.method)}</td><td><span class="status-pill ${attempt.outcome === 'SUCCESS' ? '' : 'status-failed'}">${escapeHtml(attempt.outcome)}</span></td><td>${dateText(attempt.attemptedAt)}</td></tr>`).join('') : '<tr><td colspan="7" class="empty-state">No demo payment attempts have been recorded yet.</td></tr>';
  } catch (error) {
    $('#salesHistoryBody').innerHTML = `<tr><td colspan="6" class="empty-state">Could not load sale history: ${escapeHtml(error.message)}</td></tr>`;
    $('#paymentHistoryBody').innerHTML = `<tr><td colspan="7" class="empty-state">Could not load demo payment history: ${escapeHtml(error.message)}</td></tr>`;
  }
}

function startEdit(vehicleId) {
  const car = adminVehicles.find((item) => String(item.id) === String(vehicleId));
  if (!car) return;
  currentEditingVehicleId = car.id;
  $('#listingForm').reset();
  for (const field of VEHICLE_FORM_FIELDS) {
    const element = $('#listingForm').elements.namedItem(field);
    if (element) element.value = car[field] ?? '';
  }
  updateListingMode();
  for (const field of ['variant', 'fuelType', 'transmission', 'engineSpec', 'powerOutput', 'torqueOutput', 'efficiencyRange', 'drivetrain', 'seatCount', 'warranty']) {
    const element = $('#listingForm').elements.namedItem(field);
    if (element) element.value = car[field] ?? '';
  }
  currentEditingPhotoCount = 0;
  requestJson(`/api/admin/vehicles/${encodeURIComponent(car.id)}/photos`).then((list) => {
    currentEditingPhotoCount = list.length;
    $('#photoFeedback').textContent = `${list.length} existing photo${list.length === 1 ? '' : 's'}; up to ${MAX_PHOTOS - list.length} more can be added.`;
    updatePhotoPreview();
  }).catch(() => {});
  $('#listingFormTitle').textContent = `Edit ${car.make} ${car.model}`;
  $('#saveListingButton').innerHTML = 'Save vehicle changes <span>→</span>';
  $('#listingModeHint').textContent = 'Update vehicle details; use Photos to remove or manage existing images.';
  $('#listingFeedback').textContent = '';
  $('#listingForm').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function deleteVehicle(vehicleId) {
  const car = adminVehicles.find((item) => String(item.id) === String(vehicleId));
  if (!car || !window.confirm(`Delete ${car.year} ${car.make} ${car.model}? This is blocked when sale, payment, or service history exists.`)) return;
  try {
    await requestJson(`/api/admin/vehicles/${encodeURIComponent(vehicleId)}`, { method: 'DELETE' });
    await Promise.all([loadAdminInventory(), loadListings()]);
    showToast('Car removed from inventory.');
  } catch (error) {
    showToast(error.message);
  }
}

async function openPhotoManager(vehicleId) {
  const car = adminVehicles.find((item) => String(item.id) === String(vehicleId));
  if (!car) return;
  currentPhotoVehicleId = car.id;
  $('#adminPhotosTitle').textContent = `${car.make} ${car.model} · photos`;
  $('#adminPhotoFeedback').textContent = '';
  $('#adminPhotoUpload').value = '';
  await renderAdminPhotos();
  bootstrap.Modal.getOrCreateInstance($('#adminPhotosModal')).show();
}

async function renderAdminPhotos() {
  const photos = await requestJson(`/api/admin/vehicles/${encodeURIComponent(currentPhotoVehicleId)}/photos`);
  $('#adminPhotoList').innerHTML = photos.length ? photos.map((photo) => `<article class="managed-photo"><img src="/api/vehicles/${encodeURIComponent(currentPhotoVehicleId)}/photos/${encodeURIComponent(photo.id)}" alt="${escapeHtml(photo.fileName)}"><div><span>${escapeHtml(photo.fileName)}</span><button class="table-action table-action-danger" type="button" data-delete-photo="${escapeHtml(photo.id)}">Remove</button></div></article>`).join('') : '<p class="empty-state">No photos have been added to this car.</p>';
}

function openOfflineSale(vehicleId) {
  const car = adminVehicles.find((item) => String(item.id) === String(vehicleId));
  if (!car) return;
  $('#offlineSaleForm').dataset.vehicleId = car.id;
  $('#offlineSaleForm').reset();
  $('#offlineSaleFeedback').textContent = '';
  $('#offlineCarSummary').innerHTML = `<strong>${escapeHtml(car.year)} ${escapeHtml(car.make)} ${escapeHtml(car.model)}</strong><span>Current list price: ${money(car.price)}</span>`;
  bootstrap.Modal.getOrCreateInstance($('#offlineSaleModal')).show();
}

async function submitOfflineSale(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const data = new FormData(form);
  const payload = {
    buyerName: data.get('buyerName').trim(), buyerEmail: data.get('buyerEmail').trim(),
    buyerPhone: data.get('buyerPhone').trim(), salePrice: data.get('salePrice') ? Number(data.get('salePrice')) : null
  };
  try {
    const result = await requestJson(`/api/admin/sales/offline/${encodeURIComponent(form.dataset.vehicleId)}`, {
      method: 'POST', body: JSON.stringify(payload)
    });
    $('#offlineSaleFeedback').textContent = result.message;
    await Promise.all([loadAdminInventory(), loadAdminHistory(), loadListings()]);
    showToast('Offline sale recorded in MySQL.');
    setTimeout(() => bootstrap.Modal.getOrCreateInstance($('#offlineSaleModal')).hide(), 1200);
  } catch (error) {
    $('#offlineSaleFeedback').textContent = error.message;
    $('#offlineSaleFeedback').classList.add('feedback-error');
  }
}

async function submitAdminPhotos(event) {
  event.preventDefault();
  const input = $('#adminPhotoUpload');
  const files = Array.from(input.files || []);
  if (!files.length) {
    $('#adminPhotoFeedback').textContent = 'Choose one or more JPG, PNG, or WebP images first.';
    return;
  }
  if (files.length > MAX_PHOTOS || files.some((file) => !ALLOWED_PHOTO_TYPES.has(file.type) || file.size > MAX_PHOTO_BYTES)) {
    $('#adminPhotoFeedback').textContent = 'Use up to five JPG, PNG, or WebP photos, each no larger than 2 MB.';
    $('#adminPhotoFeedback').classList.add('feedback-error');
    return;
  }
  const payload = new FormData();
  files.forEach((file) => payload.append('photos', file, file.name));
  try {
    await requestJson(`/api/admin/vehicles/${encodeURIComponent(currentPhotoVehicleId)}/photos`, { method: 'POST', body: payload });
    input.value = '';
    await Promise.all([renderAdminPhotos(), loadAdminInventory(), loadListings()]);
    $('#adminPhotoFeedback').textContent = 'Selected car photos saved in MySQL.';
  } catch (error) {
    $('#adminPhotoFeedback').textContent = error.message;
    $('#adminPhotoFeedback').classList.add('feedback-error');
  }
}

async function removeAdminPhoto(photoId) {
  if (!window.confirm('Remove this photo from the car listing?')) return;
  try {
    await requestJson(`/api/admin/vehicles/${encodeURIComponent(currentPhotoVehicleId)}/photos/${encodeURIComponent(photoId)}`, { method: 'DELETE' });
    await Promise.all([renderAdminPhotos(), loadAdminInventory(), loadListings()]);
  } catch (error) {
    $('#adminPhotoFeedback').textContent = error.message;
    $('#adminPhotoFeedback').classList.add('feedback-error');
  }
}

function setAdminTab(tab) {
  document.querySelectorAll('[data-admin-tab]').forEach((button) => {
    const active = button.dataset.adminTab === tab;
    button.classList.toggle('active', active);
    button.setAttribute('aria-selected', String(active));
  });
  document.querySelectorAll('[data-admin-panel]').forEach((panel) => { panel.hidden = panel.dataset.adminPanel !== tab; });
  if (tab === 'sales' || tab === 'payments') loadAdminHistory();
  if (tab === 'inventory') loadAdminInventory();
}

async function checkAdminSession() {
  try {
    const session = await requestJson('/api/admin/session');
    $('#adminNavLink').hidden = false;
    $('#adminLoginLink').hidden = true;
    $('#topAdminLink').hidden = true;
    if (currentRoute !== '/admin') $('#adminUsername').textContent = session.username;
  } catch (_) {
    $('#adminNavLink').hidden = true;
    $('#adminLoginLink').hidden = false;
    $('#topAdminLink').hidden = false;
  }
}

function bindEvents() {
  document.addEventListener('click', (event) => {
    const routeLink = event.target.closest('a[data-route]');
    if (routeLink) {
      event.preventDefault();
      setRoute(routeLink.getAttribute('href'));
      return;
    }
    const browseLink = event.target.closest('a[href="#availableCars"]');
    if (browseLink) {
      event.preventDefault();
      document.querySelector('#availableCars').scrollIntoView({ behavior: 'smooth' });
      return;
    }
    const detailsButton = event.target.closest('[data-view-vehicle]');
    if (detailsButton) showVehicleDetails(detailsButton.dataset.viewVehicle);
    const buyButton = event.target.closest('[data-buy-vehicle]');
    if (buyButton) openPurchase(buyButton.dataset.buyVehicle);
    const conditionButton = event.target.closest('[data-condition-filter]');
    if (conditionButton) {
      conditionFilter = conditionButton.dataset.conditionFilter;
      renderListings();
    }
    const tabButton = event.target.closest('[data-admin-tab]');
    if (tabButton) setAdminTab(tabButton.dataset.adminTab);
    const editButton = event.target.closest('[data-edit-vehicle]');
    if (editButton) startEdit(editButton.dataset.editVehicle);
    const deleteButton = event.target.closest('[data-delete-vehicle]');
    if (deleteButton) deleteVehicle(deleteButton.dataset.deleteVehicle);
    const photoButton = event.target.closest('[data-manage-photos]');
    if (photoButton) openPhotoManager(photoButton.dataset.managePhotos).catch((error) => showToast(error.message));
    const deletePhotoButton = event.target.closest('[data-delete-photo]');
    if (deletePhotoButton) removeAdminPhoto(deletePhotoButton.dataset.deletePhoto);
    const offlineSaleButton = event.target.closest('[data-record-sale]');
    if (offlineSaleButton) openOfflineSale(offlineSaleButton.dataset.recordSale);
  });

  window.addEventListener('popstate', () => setRoute(location.pathname, false));
  $('#menuToggle').addEventListener('click', () => $('#sidebar').classList.toggle('open'));
  $('#inventorySearch').addEventListener('input', renderListings);
  $('#newInventorySearch').addEventListener('input', renderListings);
  $('#usedInventorySearch').addEventListener('input', renderListings);
  $('#conditionType').addEventListener('change', updateListingMode);
  $('#vehiclePhotos').addEventListener('change', updatePhotoPreview);
  $('#listingForm').addEventListener('submit', submitListing);
  $('#contactForm').addEventListener('submit', submitContact);
  $('#demoPurchaseForm').addEventListener('submit', submitDemoPurchase);
  $('#offlineSaleForm').addEventListener('submit', submitOfflineSale);
  $('#adminPhotoForm').addEventListener('submit', submitAdminPhotos);
  $('#addVehicleButton').addEventListener('click', () => { resetListingForm(); $('#listingForm').scrollIntoView({ behavior: 'smooth', block: 'start' }); });
  $('#cancelListingButton').addEventListener('click', resetListingForm);
  $('#refreshSalesButton').addEventListener('click', loadAdminHistory);
  $('#refreshPaymentsButton').addEventListener('click', loadAdminHistory);
  $('#buyDetailsButton').addEventListener('click', () => {
    if (!selectedDetailsVehicleId) return;
    bootstrap.Modal.getOrCreateInstance($('#vehicleDetailsModal')).hide();
    openPurchase(selectedDetailsVehicleId);
  });

  const heroImage = $('#heroImage');
  const fallbackHeroImage = () => {
    if (heroImage.dataset.localSrc && heroImage.dataset.localFallback !== 'true') {
      heroImage.dataset.localFallback = 'true';
      heroImage.src = heroImage.dataset.localSrc;
      return;
    }
    $('#heroPhoto').classList.add('is-fallback');
  };
  heroImage.addEventListener('error', fallbackHeroImage);
  if (heroImage.complete && heroImage.naturalWidth === 0) fallbackHeroImage();
}

bindEvents();
updateListingMode();
setRoute(location.pathname, false);
loadListings();
checkAdminSession();
