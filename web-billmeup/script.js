const GST_PERCENT = 5;
const SERVICE_PERCENT = 10;

const STORAGE_KEYS = {
  state: "billmeup_state_v2",
  history: "billmeup_history_v2",
  counter: "billmeup_counter_v2"
};

const RESTAURANT = {
  name: "Spice Garden Bistro",
  address: "24 MG Road, Indore, Madhya Pradesh 452001",
  phone: "+91 98765 43210",
  gstin: "23ABCDE1234F1Z5",
  fssai: "FSSAI LIC 11522011001234"
};

const FALLBACK_IMAGE = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/640px-No_image_available.svg.png";

const MENU_ITEMS = [
  { id: 101, name: "Margherita Pizza", category: "Main Course", price: 249, image: "web-billmeup/images/margherita-pizza.jpeg" },
  { id: 102, name: "Veg Burger", category: "Main Course", price: 149, image: "web-billmeup/images/veg-burger.jpeg" },
  { id: 103, name: "Paneer Tikka", category: "Starters", price: 219, image: "web-billmeup/images/paneer-tikka.jpeg" },
  { id: 104, name: "French Fries", category: "Starters", price: 119, image: "web-billmeup/images/french-fries.jpeg" },
  { id: 105, name: "Cold Coffee", category: "Beverages", price: 99, image: "web-billmeup/images/cold-coffee.jpeg" },
  { id: 106, name: "Masala Chai", category: "Beverages", price: 49, image: "web-billmeup/images/masala-chai.jpeg" },
  { id: 107, name: "Chocolate Brownie", category: "Desserts", price: 129, image: "web-billmeup/images/chocolate-cake.jpeg" },
  { id: 108, name: "Gulab Jamun", category: "Desserts", price: 79, image: "web-billmeup/images/gulab-jamun.jpeg" },
  { id: 109, name: "Mint Mojito", category: "Beverages", price: 129, image: "web-billmeup/images/mojito.jpeg" },
  { id: 110, name: "Veg Momos", category: "Starters", price: 139, image: "web-billmeup/images/veg-momo.jpeg" },
  { id: 111, name: "Chicken Biryani", category: "Main Course", price: 289, image: "web-billmeup/images/biryani.jpeg" },
  { id: 112, name: "Tandoori Roti", category: "Main Course", price: 25, image: "web-billmeup/images/tandoori-roti.jpeg" },
  { id: 113, name: "Masala Papad", category: "Starters", price: 69, image: "web-billmeup/images/masala-papad.jpeg" },
  { id: 114, name: "Mango Lassi", category: "Beverages", price: 119, image: "web-billmeup/images/mango-lassi.jpeg" },
  { id: 115, name: "Ice Cream Sundae", category: "Desserts", price: 149, image: "web-billmeup/images/ice-cream-sundae.jpeg" }
];



const cart = new Map();
let currentCategory = "All";
let salesHistory = [];
let toastTimer = null;
let lastGeneratedInvoice = null;

const categoryFilters = document.getElementById("categoryFilters");
const menuGrid = document.getElementById("menuGrid");
const cartBody = document.getElementById("cartBody");
const serviceChargeToggle = document.getElementById("serviceChargeToggle");
const cashInput = document.getElementById("cashInput");
const upiInput = document.getElementById("upiInput");
const cardInput = document.getElementById("cardInput");
const subtotalValue = document.getElementById("subtotalValue");
const gstValue = document.getElementById("gstValue");
const serviceValue = document.getElementById("serviceValue");
const grandTotalValue = document.getElementById("grandTotalValue");
const paidValue = document.getElementById("paidValue");
const dueValue = document.getElementById("dueValue");
const generateBillBtn = document.getElementById("generateBillBtn");
const clearCartBtn = document.getElementById("clearCartBtn");
const invoiceDialog = document.getElementById("invoiceDialog");
const invoiceText = document.getElementById("invoiceText");
const downloadShotBtn = document.getElementById("downloadShotBtn");
const printBtn = document.getElementById("printBtn");
const downloadPdfBtn = document.getElementById("downloadPdfBtn");
const closeDialogBtn = document.getElementById("closeDialogBtn");
const customerNameInput = document.getElementById("customerName");
const mobileNumberInput = document.getElementById("mobileNumber");
const orderTypeSelect = document.getElementById("orderType");
const tableNoInput = document.getElementById("tableNo");
const serverNameInput = document.getElementById("serverName");
const salesHistoryContainer = document.getElementById("salesHistory");
const clearHistoryBtn = document.getElementById("clearHistoryBtn");
const toast = document.getElementById("toast");

function formatMoney(value) {
  return Number(value).toFixed(2);
}

function formatDateTime(date) {
  const dd = String(date.getDate()).padStart(2, "0");
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const yyyy = date.getFullYear();
  const hh = String(date.getHours()).padStart(2, "0");
  const min = String(date.getMinutes()).padStart(2, "0");
  const ss = String(date.getSeconds()).padStart(2, "0");
  return `${dd}-${mm}-${yyyy} ${hh}:${min}:${ss}`;
}

function pad(text, width, align = "left") {
  const value = String(text);
  if (value.length >= width) return value.slice(0, width);
  const space = " ".repeat(width - value.length);
  return align === "right" ? `${space}${value}` : `${value}${space}`;
}

function parseAmount(value) {
  const num = Number(value);
  return Number.isFinite(num) && num > 0 ? num : 0;
}

function showToast(message, type = "success") {
  if (!toast) return;
  toast.textContent = message;
  toast.className = `toast show ${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.className = "toast";
  }, 2200);
}

function saveJSON(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}

function loadJSON(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch (e) {
    return fallback;
  }
}

function sanitizePaymentFields() {
  [cashInput, upiInput, cardInput].forEach((field) => {
    const value = Number(field.value);
    if (!Number.isFinite(value) || value < 0) {
      field.value = "0";
    }
  });
}

function validateMobile() {
  const mobile = mobileNumberInput.value.trim();
  if (!mobile) return true;
  const normalized = mobile.replace(/[\s-]/g, "");
  return /^\+?\d{10,13}$/.test(normalized);
}

function getInvoiceCounter() {
  return Number(localStorage.getItem(STORAGE_KEYS.counter) || "1000");
}

function nextBillNumber(now) {
  const next = getInvoiceCounter() + 1;
  localStorage.setItem(STORAGE_KEYS.counter, String(next));
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, "0");
  const d = String(now.getDate()).padStart(2, "0");
  return `BMB-${y}${m}${d}-${String(next).padStart(4, "0")}`;
}

function saveState() {
  const state = {
    currentCategory,
    customer: customerNameInput.value,
    mobile: mobileNumberInput.value,
    orderType: orderTypeSelect.value,
    tableNo: tableNoInput.value,
    serverName: serverNameInput.value,
    serviceCharge: serviceChargeToggle.checked,
    cash: cashInput.value,
    upi: upiInput.value,
    card: cardInput.value,
    cart: [...cart.values()].map((line) => ({ id: line.id, qty: line.qty }))
  };
  saveJSON(STORAGE_KEYS.state, state);
}

function restoreState() {
  const state = loadJSON(STORAGE_KEYS.state, null);
  if (!state) return;

  currentCategory = state.currentCategory || "All";
  customerNameInput.value = state.customer || "";
  mobileNumberInput.value = state.mobile || "";
  orderTypeSelect.value = state.orderType || "Dine In";
  tableNoInput.value = state.tableNo || "";
  serverNameInput.value = state.serverName || "";
  serviceChargeToggle.checked = Boolean(state.serviceCharge);
  cashInput.value = state.cash ?? "0";
  upiInput.value = state.upi ?? "0";
  cardInput.value = state.card ?? "0";

  cart.clear();
  (state.cart || []).forEach((saved) => {
    const found = MENU_ITEMS.find((item) => item.id === saved.id);
    if (found) {
      cart.set(found.id, { ...found, qty: Math.max(1, Number(saved.qty) || 1) });
    }
  });
}

function loadHistory() {
  salesHistory = loadJSON(STORAGE_KEYS.history, []);
}

function saveHistory() {
  saveJSON(STORAGE_KEYS.history, salesHistory);
}

function buildCategoryFilters() {
  categoryFilters.innerHTML = "";
  const categories = ["All", ...new Set(MENU_ITEMS.map((item) => item.category))];

  categories.forEach((category) => {
    const btn = document.createElement("button");
    btn.className = `chip${category === currentCategory ? " active" : ""}`;
    btn.textContent = category;
    btn.addEventListener("click", () => {
      currentCategory = category;
      renderCategoryFilters();
      renderMenu();
      saveState();
    });
    categoryFilters.appendChild(btn);
  });
}

function renderCategoryFilters() {
  [...categoryFilters.children].forEach((btn) => {
    btn.classList.toggle("active", btn.textContent === currentCategory);
  });
}

function renderMenu() {
  menuGrid.innerHTML = "";

  const filtered = currentCategory === "All"
    ? MENU_ITEMS
    : MENU_ITEMS.filter((item) => item.category === currentCategory);

  filtered.forEach((item) => {
    const card = document.createElement("article");
    card.className = "menu-card";
    card.innerHTML = `
      <img class="menu-image" src="${item.image}" alt="${item.name}" loading="lazy" />
      <div class="menu-top">
        <div>
          <p class="menu-name">${item.name}</p>
          <p class="menu-cat">${item.category}</p>
        </div>
        <span class="menu-price">Rs ${formatMoney(item.price)}</span>
      </div>
      <button class="btn" data-id="${item.id}">Add To Bill</button>
    `;

    const imageEl = card.querySelector("img");
    imageEl.addEventListener("error", () => {
      imageEl.src = FALLBACK_IMAGE;
    });

    card.querySelector("button").addEventListener("click", () => {
      addToCart(item.id);
    });

    menuGrid.appendChild(card);
  });
}

function addToCart(itemId) {
  const item = MENU_ITEMS.find((i) => i.id === itemId);
  if (!item) return;

  const existing = cart.get(itemId);
  if (existing) {
    existing.qty += 1;
  } else {
    cart.set(itemId, { ...item, qty: 1 });
  }

  renderCart();
  saveState();
}

function changeQty(itemId, qty) {
  const line = cart.get(itemId);
  if (!line) return;
  if (qty <= 0) {
    cart.delete(itemId);
  } else {
    line.qty = qty;
  }
  renderCart();
  saveState();
}

function removeItem(itemId) {
  cart.delete(itemId);
  renderCart();
  saveState();
}

function clearCart() {
  cart.clear();
  renderCart();
  saveState();
}

function renderCart() {
  cartBody.innerHTML = "";

  if (!cart.size) {
    const row = document.createElement("tr");
    row.innerHTML = `<td colspan="5" class="empty">No items in bill. Add from menu.</td>`;
    cartBody.appendChild(row);
    renderTotals();
    return;
  }

  [...cart.values()].forEach((line) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${line.name}</td>
      <td>
        <input type="number" min="1" value="${line.qty}" data-qty-id="${line.id}" style="width:70px; min-width:0;" />
      </td>
      <td>Rs ${formatMoney(line.price)}</td>
      <td>Rs ${formatMoney(line.price * line.qty)}</td>
      <td><button class="remove-btn" data-remove-id="${line.id}">X</button></td>
    `;

    tr.querySelector("[data-qty-id]").addEventListener("input", (e) => {
      const value = parseInt(e.target.value, 10);
      changeQty(line.id, Number.isFinite(value) ? value : 1);
    });

    tr.querySelector("[data-remove-id]").addEventListener("click", () => removeItem(line.id));

    cartBody.appendChild(tr);
  });

  renderTotals();
}

function computeTotals() {
  const subtotal = [...cart.values()].reduce((sum, line) => sum + line.price * line.qty, 0);
  const gst = subtotal * GST_PERCENT / 100;
  const service = serviceChargeToggle.checked ? subtotal * SERVICE_PERCENT / 100 : 0;
  const grandTotal = subtotal + gst + service;

  const cash = parseAmount(cashInput.value);
  const upi = parseAmount(upiInput.value);
  const card = parseAmount(cardInput.value);
  const paid = cash + upi + card;
  const due = paid - grandTotal;

  return { subtotal, gst, service, grandTotal, cash, upi, card, paid, due };
}

function renderTotals() {
  sanitizePaymentFields();
  const totals = computeTotals();
  subtotalValue.textContent = formatMoney(totals.subtotal);
  gstValue.textContent = formatMoney(totals.gst);
  serviceValue.textContent = formatMoney(totals.service);
  grandTotalValue.textContent = formatMoney(totals.grandTotal);
  paidValue.textContent = formatMoney(totals.paid);
  dueValue.textContent = formatMoney(totals.due);
}

function buildInvoiceData() {
  const customer = customerNameInput.value.trim() || "Walk-in Customer";
  const mobile = mobileNumberInput.value.trim() || "-";
  const orderType = orderTypeSelect.value;
  const tableNo = tableNoInput.value.trim() || (orderType === "Dine In" ? "N/A" : "Takeaway");
  const serverName = serverNameInput.value.trim() || "Staff";
  const now = new Date();
  const stamp = formatDateTime(now);
  const billNo = nextBillNumber(now);
  const totals = computeTotals();
  const cgst = totals.gst / 2;
  const sgst = totals.gst / 2;
  const itemCount = [...cart.values()].reduce((sum, line) => sum + line.qty, 0);

  const lines = [];
  lines.push("==========================================================");
  lines.push(`                 ${RESTAURANT.name.toUpperCase()}`);
  lines.push("                     TAX INVOICE");
  lines.push("==========================================================");
  lines.push(`${RESTAURANT.address}`);
  lines.push(`Phone: ${RESTAURANT.phone}    GSTIN: ${RESTAURANT.gstin}`);
  lines.push(`${RESTAURANT.fssai}`);
  lines.push("----------------------------------------------------------");
  lines.push(`Bill No     : ${billNo}`);
  lines.push(`Date & Time : ${stamp}`);
  lines.push(`Customer    : ${customer}`);
  lines.push(`Mobile      : ${mobile}`);
  lines.push(`Order Type  : ${orderType}`);
  lines.push(`Table No    : ${tableNo}`);
  lines.push(`Served By   : ${serverName}`);
  lines.push("----------------------------------------------------------");
  lines.push("Item                   Qty    Rate         Amount");
  lines.push("----------------------------------------------------------");

  [...cart.values()].forEach((line) => {
    const item = pad(line.name, 21, "left");
    const qty = pad(line.qty, 5, "right");
    const rate = pad(formatMoney(line.price), 9, "right");
    const amount = pad(formatMoney(line.price * line.qty), 12, "right");
    lines.push(`${item}${qty}  ${rate}  ${amount}`);
  });

  lines.push("----------------------------------------------------------");
  lines.push(`Items Count        : ${itemCount}`);
  lines.push(`Subtotal           : Rs ${formatMoney(totals.subtotal)}`);
  lines.push(`CGST (2.5%)        : Rs ${formatMoney(cgst)}`);
  lines.push(`SGST (2.5%)        : Rs ${formatMoney(sgst)}`);
  lines.push(`Service Charge     : Rs ${formatMoney(totals.service)}`);
  lines.push(`Grand Total        : Rs ${formatMoney(totals.grandTotal)}`);
  lines.push("----------------------------------------------------------");
  lines.push(`Cash               : Rs ${formatMoney(totals.cash)}`);
  lines.push(`UPI                : Rs ${formatMoney(totals.upi)}`);
  lines.push(`Card               : Rs ${formatMoney(totals.card)}`);
  lines.push(`Total Paid         : Rs ${formatMoney(totals.paid)}`);
  lines.push(totals.due < 0
    ? `Amount Due         : Rs ${formatMoney(Math.abs(totals.due))}`
    : `Change Return      : Rs ${formatMoney(totals.due)}`);
  lines.push("----------------------------------------------------------");
  lines.push("Payment Status     : " + (totals.due < 0 ? "PARTIALLY PAID" : "PAID"));
  lines.push("==========================================================");
  lines.push("Thank you for visiting Spice Garden Bistro.");
  lines.push("Please visit again.");

  const text = lines.join("\n");

  return {
    billNo,
    stamp,
    customer,
    mobile,
    orderType,
    tableNo,
    serverName,
    totals,
    text
  };
}

function validateBeforeInvoice() {
  if (!cart.size) {
    showToast("Add at least one menu item before generating invoice.", "error");
    return false;
  }

  if (!validateMobile()) {
    showToast("Enter a valid mobile number (10-13 digits).", "error");
    return false;
  }

  return true;
}

function openInvoiceDialog(invoiceData) {
  invoiceText.textContent = invoiceData.text;
  invoiceDialog.showModal();
}

function renderHistory() {
  salesHistoryContainer.innerHTML = "";

  if (!salesHistory.length) {
    salesHistoryContainer.innerHTML = '<p class="empty">No invoices yet. Generate one bill to see history.</p>';
    return;
  }

  salesHistory.forEach((entry, index) => {
    const item = document.createElement("div");
    item.className = "history-item";
    item.innerHTML = `
      <div class="history-top">
        <span>${entry.billNo}</span>
        <strong>Rs ${formatMoney(entry.total)}</strong>
      </div>
      <div class="history-meta">${entry.stamp} | ${entry.customer} | ${entry.status}</div>
      <div class="history-actions">
        <button class="btn ghost" data-view-index="${index}">View</button>
      </div>
    `;
    salesHistoryContainer.appendChild(item);
  });

  salesHistoryContainer.querySelectorAll("[data-view-index]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const idx = Number(btn.getAttribute("data-view-index"));
      const entry = salesHistory[idx];
      if (!entry) return;
      lastGeneratedInvoice = { billNo: entry.billNo, text: entry.text };
      invoiceText.textContent = entry.text;
      invoiceDialog.showModal();
    });
  });
}

function saveInvoiceToHistory(invoiceData) {
  salesHistory.unshift({
    billNo: invoiceData.billNo,
    stamp: invoiceData.stamp,
    customer: invoiceData.customer,
    total: Number(invoiceData.totals.grandTotal),
    status: invoiceData.totals.due < 0 ? "PARTIALLY PAID" : "PAID",
    text: invoiceData.text
  });

  if (salesHistory.length > 40) {
    salesHistory = salesHistory.slice(0, 40);
  }

  saveHistory();
  renderHistory();
}

function printCurrentInvoice() {
  const text = invoiceText.textContent || "";
  const win = window.open("", "_blank", "width=900,height=700");
  if (!win) return;

  win.document.write(`
    <html>
      <head><title>BillMeUp Invoice</title></head>
      <body style="font-family: monospace; padding: 16px;">
        <pre>${text.replace(/</g, "&lt;")}</pre>
      </body>
    </html>
  `);
  win.document.close();
  win.focus();
  win.print();
}

function downloadCurrentInvoicePdf() {
  if (!lastGeneratedInvoice || !lastGeneratedInvoice.text) {
    showToast("Generate or open an invoice first.", "error");
    return;
  }

  if (!window.jspdf || !window.jspdf.jsPDF) {
    showToast("PDF library not loaded. Check internet and refresh.", "error");
    return;
  }

  const { jsPDF } = window.jspdf;
  const doc = new jsPDF({ unit: "pt", format: "a4" });
  const lines = doc.splitTextToSize(lastGeneratedInvoice.text, 540);
  doc.setFont("courier", "normal");
  doc.setFontSize(10);
  doc.text(lines, 28, 36);
  doc.save(`${lastGeneratedInvoice.billNo}.pdf`);
  showToast("Invoice PDF downloaded.", "success");
}

function downloadInvoiceScreenshot() {
  if (!lastGeneratedInvoice || !lastGeneratedInvoice.text) {
    showToast("Generate or open an invoice first.", "error");
    return;
  }
  if (typeof html2canvas === "undefined") {
    showToast("Screenshot library not loaded. Refresh and try again.", "error");
    return;
  }

  html2canvas(invoiceDialog, {
    backgroundColor: "#ffffff",
    scale: 2
  }).then((canvas) => {
    const link = document.createElement("a");
    link.download = `${lastGeneratedInvoice.billNo}-invoice.png`;
    link.href = canvas.toDataURL("image/png");
    link.click();
    showToast("Invoice screenshot saved.", "success");
  }).catch(() => {
    showToast("Could not capture screenshot.", "error");
  });
}

generateBillBtn.addEventListener("click", () => {
  sanitizePaymentFields();
  renderTotals();

  if (!validateBeforeInvoice()) {
    return;
  }

  const invoiceData = buildInvoiceData();
  lastGeneratedInvoice = { billNo: invoiceData.billNo, text: invoiceData.text };

  saveInvoiceToHistory(invoiceData);
  openInvoiceDialog(invoiceData);
  saveState();
  showToast(`Invoice ${invoiceData.billNo} generated.`, "success");
});

printBtn.addEventListener("click", printCurrentInvoice);
downloadPdfBtn.addEventListener("click", downloadCurrentInvoicePdf);
downloadShotBtn.addEventListener("click", downloadInvoiceScreenshot);
closeDialogBtn.addEventListener("click", () => invoiceDialog.close());
clearCartBtn.addEventListener("click", () => {
  clearCart();
  showToast("Current bill cleared.", "success");
});

clearHistoryBtn.addEventListener("click", () => {
  salesHistory = [];
  saveHistory();
  renderHistory();
  showToast("Sales history cleared.", "success");
});

[serviceChargeToggle, cashInput, upiInput, cardInput].forEach((el) => {
  el.addEventListener("input", () => {
    renderTotals();
    saveState();
  });
  el.addEventListener("change", () => {
    renderTotals();
    saveState();
  });
});

[customerNameInput, mobileNumberInput, orderTypeSelect, tableNoInput, serverNameInput].forEach((el) => {
  el.addEventListener("input", saveState);
  el.addEventListener("change", saveState);
});

buildCategoryFilters();
loadHistory();
restoreState();
renderCategoryFilters();
renderMenu();
renderCart();
renderHistory();
renderTotals();
