(function () {
  "use strict";

  /* ========== STATE ========== */
  var state = {
    farmers: [],
    officers: [],
    farmerTickets: [],
    officerTickets: [],
    escalatedTickets: [],
    currentRole: null,
    currentFarmerId: null,
    currentOfficerId: null,
    selectedTicket: null,
    recoTicketId: null
  };

  function $(id) { return document.getElementById(id); }
  function parseNum(v) {
    if (v === null || v === undefined || v === "") return null;
    var n = Number(v);
    return Number.isFinite(n) ? n : null;
  }
  function esc(v) {
    if (v === null || v === undefined) return "";
    var d = document.createElement("div"); d.textContent = String(v); return d.innerHTML;
  }
  function safe(v, fb) { return (v === null || v === undefined || v === "") ? (fb || "\u2014") : String(v); }
  function trunc(v, max) { var t = safe(v, ""); if (!t || t === "\u2014") return "\u2014"; return t.length <= max ? t : t.slice(0, max - 1) + "\u2026"; }

  function fmtDate(v) {
    if (!v) return "\u2014";
    var d = new Date(v);
    if (isNaN(d.getTime())) return safe(v);
    return d.toLocaleString("en-IN", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit", hour12: true });
  }

  function statusCls(v) { return safe(v, "normal").toLowerCase().replace(/[\s_]+/g, "-"); }
  function badge(v) {
    var t = safe(v, "NORMAL");
    return '<span class="status-badge status-' + statusCls(t) + '">' + esc(t.replace(/_/g, " ")) + "</span>";
  }

  /* ========== TOAST ========== */
  function showToast(msg, type) {
    var rack = $("toastRack"); if (!rack) return;
    var t = document.createElement("div");
    t.className = "toast toast--" + (type || "success");
    t.textContent = msg;
    rack.appendChild(t);
    setTimeout(function () { t.classList.add("toast--exit"); }, 3500);
    setTimeout(function () { if (t.parentNode) t.parentNode.removeChild(t); }, 3800);
  }

  function openModal(id) {
    var modal = $(id);
    if (!modal) return;
    modal.classList.remove("hidden");
    modal.setAttribute("aria-hidden", "false");
  }

  function closeModal(id) {
    var modal = $(id);
    if (!modal) return;
    modal.classList.add("hidden");
    modal.setAttribute("aria-hidden", "true");
  }

  /* ========== API HELPER ========== */
  function api(url, opts) {
    var options = opts || {};
    var fetchOpts = { method: options.method || "GET", headers: {} };
    if (options.body) {
      fetchOpts.headers["Content-Type"] = "application/json";
      fetchOpts.body = JSON.stringify(options.body);
    }
    return fetch(url, fetchOpts).catch(function () {
      throw new Error("Unable to connect to the server.");
    }).then(function (res) {
      if (res.ok) {
        var ct = res.headers.get("content-type") || "";
        if (ct.indexOf("application/json") !== -1) return res.json();
        return res.text();
      }
      return res.text().then(function (txt) {
        var msg = "The request could not be completed.";
        try {
          var obj = JSON.parse(txt);
          var detail = String(obj.message || obj.error || "").toLowerCase();
          if (res.status === 400) msg = "Please check the information and try again.";
          else if (res.status === 403) msg = "Only the assigned officer can update this ticket.";
          else if (res.status === 404) msg = "Ticket not found.";
          else if (res.status >= 500) msg = "The server encountered an error. Please try again.";
          else if (detail.indexOf("assigned officer") !== -1) msg = "Only the assigned officer can update this ticket.";
          else if (detail.indexOf("not found") !== -1) msg = "Ticket not found.";
          else if (detail && res.status !== 400) msg = "The request could not be completed.";
        } catch (e) {
          if (res.status === 400) msg = "Please check the information and try again.";
          else if (res.status === 403) msg = "Only the assigned officer can update this ticket.";
          else if (res.status === 404) msg = "Ticket not found.";
          else if (res.status >= 500) msg = "The server encountered an error. Please try again.";
        }
        throw new Error(msg);
      });
    });
  }

  /* ========== SCREEN MANAGEMENT ========== */
  function showScreen(name) {
    $("landingScreen").classList.add("hidden");
    $("farmerPortal").classList.add("hidden");
    $("officerPortal").classList.add("hidden");
    if (name === "landing") { $("landingScreen").classList.remove("hidden"); }
    else if (name === "farmer") { $("farmerPortal").classList.remove("hidden"); }
    else if (name === "officer") { $("officerPortal").classList.remove("hidden"); }
  }

  /* ========== VIEW SWITCHING ========== */
  var viewTitles = {
    dashboard: "Dashboard", tickets: "My Tickets", raise: "Raise Advisory",
    profile: "Profile", escalated: "Escalated Tickets"
  };

  function switchView(role, view) {
    var prefix = role === "FARMER" ? "fv-" : "ov-";
    var topId = role === "FARMER" ? "farmerTopTitle" : "officerTopTitle";
    var portal = role === "FARMER" ? $("farmerPortal") : $("officerPortal");
    var panels = portal.querySelectorAll(".view-panel");
    for (var i = 0; i < panels.length; i++) { panels[i].classList.add("hidden"); }
    var target = $(prefix + view);
    if (target) target.classList.remove("hidden");
    $(topId).textContent = viewTitles[view] || view;

    var sidebar = role === "FARMER" ? $("farmerSidebar") : $("officerSidebar");
    var btns = sidebar.querySelectorAll(".nav-btn");
    for (var j = 0; j < btns.length; j++) {
      btns[j].classList.toggle("is-active", btns[j].getAttribute("data-view") === view);
    }

    if (role === "FARMER" && view === "tickets") loadFarmerTickets(true);
    if (role === "FARMER" && view === "raise") renderFarmerContext();
    if (role === "FARMER" && view === "profile") renderFarmerProfile();
    if (role === "OFFICER" && view === "tickets") loadOfficerTickets(true);
    if (role === "OFFICER" && view === "escalated") loadEscalatedTickets();
    if (role === "OFFICER" && view === "profile") renderOfficerProfile();

    closeMobileSidebar(role);
  }

  /* ========== MOBILE SIDEBAR ========== */
  function openMobileSidebar(role) {
    var sid = role === "FARMER" ? "farmerSidebar" : "officerSidebar";
    var ov = role === "FARMER" ? "farmerOverlay" : "officerOverlay";
    $(sid).classList.add("is-open");
    $(ov).classList.add("is-open");
  }
  function closeMobileSidebar(role) {
    var sid = role === "FARMER" ? "farmerSidebar" : "officerSidebar";
    var ov = role === "FARMER" ? "farmerOverlay" : "officerOverlay";
    $(sid).classList.remove("is-open");
    $(ov).classList.remove("is-open");
  }

  /* ========== LANDING: LOAD FARMERS ========== */
  function loadFarmers() {
    var sel = $("farmerSelect");
    sel.innerHTML = '<option value="">Loading farmers\u2026</option>';
    sel.disabled = true;
    api("/api/farmers").then(function (data) {
      state.farmers = data;
      sel.innerHTML = '<option value="">-- Select Farmer --</option>';
      data.forEach(function (f) {
        sel.innerHTML += '<option value="' + f.farmerId + '">' + esc(f.name) + "</option>";
      });
      sel.disabled = false;
      $("farmerHelp").textContent = "Select a farmer to continue";
    }).catch(function () {
      sel.innerHTML = '<option value="">Unable to load farmers</option>';
      $("farmerHelp").innerHTML = '<a href="#" onclick="location.reload()" style="color:var(--primary-dark);font-weight:700">Retry</a>';
    });
  }

  /* ========== LANDING: LOAD OFFICERS ========== */
  function loadOfficers() {
    var sel = $("officerSelect");
    sel.innerHTML = '<option value="">Loading officers\u2026</option>';
    sel.disabled = true;
    api("/api/officers").then(function (data) {
      state.officers = data;
      sel.innerHTML = '<option value="">-- Select Officer --</option>';
      data.forEach(function (o) {
        sel.innerHTML += '<option value="' + o.officerId + '">' + esc(o.name) + "</option>";
      });
      sel.disabled = false;
      $("officerHelp").textContent = "Select an officer to continue";
    }).catch(function () {
      sel.innerHTML = '<option value="">Unable to load officers</option>';
      $("officerHelp").innerHTML = '<a href="#" onclick="location.reload()" style="color:var(--primary-dark);font-weight:700">Retry</a>';
    });
  }

  /* ========== PREVIEW RENDERING ========== */
  function renderFarmerPreview() {
    var id = parseNum($("farmerSelect").value);
    var box = $("farmerPreview");
    $("farmerEnterBtn").disabled = !id;
    if (!id) {
      box.className = "preview-box";
      box.innerHTML = '<div class="preview-empty">Select a farmer to view region and contact details.</div>';
      return;
    }
    var f = state.farmers.find(function (x) { return x.farmerId === id; });
    if (!f) { box.innerHTML = '<div class="preview-empty">Farmer not found.</div>'; return; }
    box.className = "preview-box preview-box--active";
    box.innerHTML = '<div class="preview-grid">' +
      pi("Region", safe(f.region && f.region.name)) +
      pi("District", safe(f.region && f.region.district)) +
      pi("Phone", safe(f.phone)) +
      pi("Email", safe(f.email)) +
      "</div>";
  }

  function renderOfficerPreview() {
    var id = parseNum($("officerSelect").value);
    var box = $("officerPreview");
    $("officerEnterBtn").disabled = !id;
    if (!id) {
      box.className = "preview-box";
      box.innerHTML = '<div class="preview-empty">Select an officer to view specialization and jurisdiction.</div>';
      return;
    }
    var o = state.officers.find(function (x) { return x.officerId === id; });
    if (!o) { box.innerHTML = '<div class="preview-empty">Officer not found.</div>'; return; }
    box.className = "preview-box preview-box--officer";
    box.innerHTML = '<div class="preview-grid">' +
      pi("Specialization", safe(o.specialization)) +
      pi("Region", safe(o.region && o.region.name)) +
      pi("District", safe(o.region && o.region.district)) +
      pi("State", safe(o.region && o.region.state)) +
      "</div>";
  }

  function pi(label, value) {
    return '<div class="preview-item"><span>' + esc(label) + "</span><span>" + esc(value) + "</span></div>";
  }

  /* ========== ENTER PORTALS ========== */
  function enterFarmerPortal() {
    var id = parseNum($("farmerSelect").value);
    if (!id) { showToast("Please select a farmer.", "warning"); return; }
    state.currentRole = "FARMER";
    state.currentFarmerId = id;
    localStorage.setItem("currentRole", "FARMER");
    localStorage.setItem("currentFarmerId", String(id));
    showScreen("farmer");
    initFarmerDashboard();
  }

  function enterOfficerPortal() {
    var id = parseNum($("officerSelect").value);
    if (!id) { showToast("Please select an officer.", "warning"); return; }
    state.currentRole = "OFFICER";
    state.currentOfficerId = id;
    localStorage.setItem("currentRole", "OFFICER");
    localStorage.setItem("currentOfficerId", String(id));
    showScreen("officer");
    initOfficerDashboard();
  }

  /* ========== FARMER DASHBOARD ========== */
  function initFarmerDashboard() {
    var f = state.farmers.find(function (x) { return x.farmerId === state.currentFarmerId; });
    if (f) {
      $("fWelcomeName").textContent = "Welcome back, " + f.name;
      $("farmerTopMeta").textContent = safe(f.region && f.region.name) + " \u2022 " + safe(f.region && f.region.district);
    }
    switchView("FARMER", "dashboard");
    loadFarmerTickets(false);
  }

  function loadFarmerTickets(fullOnly) {
    api("/api/tickets/farmer/" + state.currentFarmerId).then(function (data) {
      state.farmerTickets = data;
      if (!fullOnly) renderFarmerStats();
      if (!fullOnly) renderFarmerRecent();
      renderFarmerAllTickets();
    }).catch(function (err) {
      showToast("Failed to load tickets: " + err.message, "error");
    });
  }

  function renderFarmerStats() {
    var t = state.farmerTickets;
    var total = t.length;
    var open = t.filter(function (x) { return x.status === "OPEN"; }).length;
    var prog = t.filter(function (x) { return x.status === "IN_PROGRESS"; }).length;
    var closed = t.filter(function (x) { return x.status === "CLOSED"; }).length;
    var esc = t.filter(function (x) { return x.escalationStatus === "ESCALATED"; }).length;
    $("fStats").innerHTML =
      sc("Total", total, "") + sc("Open", open, "open") + sc("In Progress", prog, "progress") +
      sc("Closed", closed, "closed") + sc("Escalated", esc, "escalated");
  }

  function sc(label, value, mod) {
    var cls = mod ? " stat-card--" + mod : "";
    return '<div class="stat-card' + cls + '"><div class="stat-card__label">' + label +
      '</div><div class="stat-card__value">' + value + "</div></div>";
  }

  function renderFarmerRecent() {
    var rows = state.farmerTickets.slice(0, 5);
    $("fRecentTable").innerHTML = buildTicketTable(rows, "farmer");
  }

  function renderFarmerAllTickets() {
    var el = $("fAllTickets");
    if (!el) return;
    if (state.farmerTickets.length === 0) {
      el.innerHTML = '<div class="empty-state"><div class="empty-state__text">No advisory tickets yet.<br>Raise your first crop advisory request to get expert assistance.</div></div>';
      return;
    }
    el.innerHTML = buildTicketTable(state.farmerTickets, "farmer");
  }

  /* ========== OFFICER DASHBOARD ========== */
  function initOfficerDashboard() {
    var o = state.officers.find(function (x) { return x.officerId === state.currentOfficerId; });
    if (o) {
      $("oWelcomeName").textContent = "Welcome, " + o.name;
      $("officerTopMeta").textContent = safe(o.specialization) + " \u2022 " + safe(o.region && o.region.name);
    }
    switchView("OFFICER", "dashboard");
    loadOfficerTickets(false);
  }

  function loadOfficerTickets(fullOnly) {
    api("/api/tickets/officer/" + state.currentOfficerId).then(function (data) {
      state.officerTickets = data;
      if (!fullOnly) renderOfficerStats();
      if (!fullOnly) renderOfficerRecent();
      renderOfficerAllTickets();
    }).catch(function (err) {
      showToast("Failed to load tickets: " + err.message, "error");
    });
  }

  function renderOfficerStats() {
    var t = state.officerTickets;
    var total = t.length;
    var open = t.filter(function (x) { return x.status === "OPEN"; }).length;
    var prog = t.filter(function (x) { return x.status === "IN_PROGRESS"; }).length;
    var closed = t.filter(function (x) { return x.status === "CLOSED"; }).length;
    var escalated = t.filter(function (x) { return x.escalationStatus === "ESCALATED"; }).length;
    $("oStats").innerHTML =
      sc("Assigned", total, "") + sc("Open", open, "open") + sc("In Progress", prog, "progress") +
      sc("Closed", closed, "closed") + sc("Escalated", escalated, "escalated");
  }

  function renderOfficerRecent() {
    var rows = state.officerTickets.slice(0, 5);
    $("oRecentTable").innerHTML = buildTicketTable(rows, "officer");
  }

  function renderOfficerAllTickets() {
    var el = $("oAllTickets");
    if (!el) return;
    if (state.officerTickets.length === 0) {
      el.innerHTML = '<div class="empty-state"><div class="empty-state__text">No tickets assigned yet.</div></div>';
      return;
    }
    el.innerHTML = buildTicketTable(state.officerTickets, "officer");
  }

  /* ========== ESCALATED TICKETS ========== */
  function loadEscalatedTickets() {
    api("/api/tickets/escalated").then(function (data) {
      state.escalatedTickets = data;
      renderEscalatedTable();
    }).catch(function (err) {
      showToast("Failed to load escalated tickets: " + err.message, "error");
    });
  }

  function renderEscalatedTable() {
    var el = $("oEscalatedTable");
    if (state.escalatedTickets.length === 0) {
      el.innerHTML = '<div class="empty-state"><div class="empty-state__text">No escalated tickets at this time.</div></div>';
      return;
    }
    var html = '<table class="data-table"><thead><tr>' +
      "<th>ID</th><th>Farmer</th><th>Crop</th><th>Officer</th><th>Status</th><th>Escalation</th><th>Created</th><th>Reopened</th><th>Action</th>" +
      "</tr></thead><tbody>";
    state.escalatedTickets.forEach(function (t) {
      html += "<tr>" +
        "<td><strong>#" + t.ticketId + "</strong></td>" +
        "<td>" + esc(t.farmer && t.farmer.name) + "</td>" +
        "<td>" + esc(t.cropName) + "</td>" +
        "<td>" + esc(t.officer && t.officer.name) + "</td>" +
        "<td>" + badge(t.status) + "</td>" +
        "<td>" + badge(t.escalationStatus) + "</td>" +
        "<td>" + fmtDate(t.createdAt) + "</td>" +
        "<td>" + fmtDate(t.reopenedAt) + "</td>" +
        '<td><button class="btn btn--view btn--sm" data-ticket-id="' + t.ticketId + '">View</button></td>' +
        "</tr>";
    });
    html += "</tbody></table>";
    el.innerHTML = html;
    attachViewHandlers(el);
  }

  /* ========== TABLE BUILDER ========== */
  function buildTicketTable(tickets, role) {
    if (!tickets.length) {
      return '<div class="empty-state"><div class="empty-state__text">No tickets found.</div></div>';
    }
    var html = '<table class="data-table"><thead><tr>' +
      "<th>ID</th><th>Crop</th><th>Symptoms</th>";
    if (role === "farmer") html += "<th>Officer</th>";
    if (role === "officer") html += "<th>Farmer</th>";
    html += "<th>Status</th><th>Escalation</th><th>Created</th><th>Action</th></tr></thead><tbody>";

    tickets.forEach(function (t) {
      html += "<tr>" +
        "<td><strong>#" + t.ticketId + "</strong></td>" +
        "<td>" + esc(t.cropName) + "</td>" +
        '<td><span class="table-sub">' + esc(trunc(t.symptoms, 50)) + "</span></td>";
      if (role === "farmer") html += "<td>" + esc(t.officer && t.officer.name) + "</td>";
      if (role === "officer") html += "<td>" + esc(t.farmer && t.farmer.name) + "</td>";
      html += "<td>" + badge(t.status) + "</td>" +
        "<td>" + badge(t.escalationStatus) + "</td>" +
        "<td>" + fmtDate(t.createdAt) + "</td>" +
        '<td><button class="btn btn--view btn--sm" data-ticket-id="' + t.ticketId + '">View</button></td>' +
        "</tr>";
    });
    html += "</tbody></table>";
    return html;
  }

  function attachViewHandlers(container) {
    var btns = container.querySelectorAll("[data-ticket-id]");
    for (var i = 0; i < btns.length; i++) {
      btns[i].addEventListener("click", function () {
        openTicketDetail(Number(this.getAttribute("data-ticket-id")));
      });
    }
  }

  /* ========== TICKET DETAIL MODAL ========== */
  function openTicketDetail(ticketId) {
    var allTickets = state.farmerTickets.concat(state.officerTickets, state.escalatedTickets);
    var ticket = null;
    for (var i = 0; i < allTickets.length; i++) {
      if (Number(allTickets[i].ticketId) === ticketId) { ticket = allTickets[i]; break; }
    }
    if (!ticket) {
      api("/api/tickets/" + ticketId).then(function (data) {
        showTicketModal(data);
      }).catch(function () { showToast("Ticket not found.", "error"); });
      return;
    }
    showTicketModal(ticket);
  }

  function showTicketModal(t) {
    state.selectedTicket = t;
    $("tmTitle").textContent = "Ticket #" + t.ticketId + " \u2014 " + safe(t.cropName);

    var photo = t.photoUrl
      ? '<a href="' + esc(t.photoUrl) + '" target="_blank" rel="noopener" style="color:var(--primary-dark)">View Photo \u2197</a>'
      : "\u2014";

    $("tmBody").innerHTML = '<div class="detail-grid">' +
      di("Ticket ID", "#" + t.ticketId) +
      di("Crop Name", safe(t.cropName)) +
      di("Farmer", safe(t.farmer && t.farmer.name)) +
      di("Officer", safe(t.officer && t.officer.name)) +
      di("Region", safe(t.region && t.region.name)) +
      di("District", safe(t.region && t.region.district)) +
      di("Status", badge(t.status), true) +
      di("Escalation", badge(t.escalationStatus), true) +
      di("Created At", fmtDate(t.createdAt)) +
      di("Assigned At", fmtDate(t.assignedAt)) +
      di("Reopened At", fmtDate(t.reopenedAt)) +
      di("Resolved At", fmtDate(t.resolvedAt)) +
      diFull("Symptoms", safe(t.symptoms)) +
      diFull("Photo", photo, true) +
      diFull("Recommendation", safe(t.recommendation), false, !!t.recommendation) +
      "</div>";

    var footer = '<button class="btn btn--secondary" id="tmDismiss">Close</button>';
    if (state.currentRole === "FARMER" && t.status === "CLOSED") {
      footer = '<button class="btn btn--warning" id="tmReopen">Reopen Ticket</button>' + footer;
    }
    if (state.currentRole === "OFFICER" && t.status !== "CLOSED") {
      footer = '<button class="btn btn--primary" id="tmAddReco">Add Recommendation</button>' +
        '<button class="btn btn--danger" id="tmCloseTicket">Close Ticket</button>' + footer;
    }
    $("tmFooter").innerHTML = footer;

    $("tmDismiss").addEventListener("click", closeTicketModal);
    if ($("tmReopen")) $("tmReopen").addEventListener("click", function () { reopenTicket(t.ticketId); });
    if ($("tmAddReco")) $("tmAddReco").addEventListener("click", function () { openRecoModal(t); });
    if ($("tmCloseTicket")) $("tmCloseTicket").addEventListener("click", function () { closeTicket(t.ticketId); });

    openModal("ticketModal");
  }

  function di(label, value, isHtml) {
    return '<div class="detail-item"><div class="detail-item__label">' + esc(label) +
      '</div><div class="detail-item__value">' + (isHtml ? value : esc(value)) + "</div></div>";
  }
  function diFull(label, value, isHtml, isReco) {
    return '<div class="detail-item detail-item--full' + (isReco ? " detail-item--reco" : "") +
      '"><div class="detail-item__label">' + esc(label) +
      '</div><div class="detail-item__value">' + (isHtml ? value : esc(value)) + "</div></div>";
  }

  function closeTicketModal() {
    closeModal("ticketModal");
    state.selectedTicket = null;
  }

  /* ========== RECOMMENDATION MODAL ========== */
  function openRecoModal(ticket) {
    state.recoTicketId = ticket.ticketId;
    $("recoInfo").textContent = "Ticket #" + ticket.ticketId + " \u2014 " + safe(ticket.cropName) + " (" + safe(ticket.farmer && ticket.farmer.name) + ")";
    $("recoText").value = ticket.recommendation || "";
    openModal("recoModal");
    $("recoText").focus();
  }

  function closeRecoModal() {
    closeModal("recoModal");
    state.recoTicketId = null;
  }

  function submitRecommendation() {
    var text = $("recoText").value.trim();
    if (!text) { showToast("Please enter a recommendation.", "warning"); return; }
    $("recoSubmit").disabled = true;
    api("/api/tickets/" + state.recoTicketId + "/recommendation/" + state.currentOfficerId, {
      method: "PUT",
      body: { recommendation: text }
    }).then(function () {
      showToast("Recommendation added successfully.", "success");
      closeRecoModal();
      closeTicketModal();
      loadOfficerTickets(false);
    }).catch(function (err) {
      showToast(err.message, "error");
    }).finally(function () { $("recoSubmit").disabled = false; });
  }

  /* ========== CLOSE TICKET ========== */
  function closeTicket(ticketId) {
    if (!confirm("Are you sure you want to close this ticket?")) return;
    api("/api/tickets/" + ticketId + "/close/" + state.currentOfficerId, { method: "PUT" })
      .then(function () {
        showToast("Ticket closed successfully.", "success");
        closeTicketModal();
        loadOfficerTickets(false);
      }).catch(function (err) { showToast(err.message, "error"); });
  }

  /* ========== REOPEN TICKET ========== */
  function reopenTicket(ticketId) {
    if (!confirm("Reopen this ticket?")) return;
    api("/api/tickets/" + ticketId + "/reopen/" + state.currentFarmerId, { method: "PUT" })
      .then(function () {
        showToast("Ticket reopened successfully.", "success");
        closeTicketModal();
        loadFarmerTickets(false);
      }).catch(function (err) { showToast(err.message, "error"); });
  }

  /* ========== RAISE TICKET ========== */
  function renderFarmerContext() {
    var f = state.farmers.find(function (x) { return x.farmerId === state.currentFarmerId; });
    if (f) {
      $("farmerCtx").innerHTML = "Submitting as <strong>" + esc(f.name) + "</strong> \u2022 " +
        esc(safe(f.region && f.region.name)) + ", " + esc(safe(f.region && f.region.district));
    }
  }

  function submitTicket(e) {
    e.preventDefault();
    var crop = $("cropInput").value.trim();
    var symptoms = $("symptomsInput").value.trim();
    var photo = $("photoInput").value.trim();
    if (!crop || !symptoms) { showToast("Crop name and symptoms are required.", "warning"); return; }

    $("raiseBtn").disabled = true;
    var body = { farmerId: state.currentFarmerId, cropName: crop, symptoms: symptoms };
    if (photo) body.photoUrl = photo;

    api("/api/tickets", { method: "POST", body: body })
      .then(function () {
        showToast("Advisory ticket created successfully.", "success");
        $("raiseForm").reset();
        loadFarmerTickets(false);
      }).catch(function (err) {
        showToast(err.message, "error");
      }).finally(function () { $("raiseBtn").disabled = false; });
  }

  /* ========== PROFILE ========== */
  function renderFarmerProfile() {
    var f = state.farmers.find(function (x) { return x.farmerId === state.currentFarmerId; });
    if (!f) return;
    $("fProfile").innerHTML =
      '<div class="profile-header"><div class="profile-avatar">' + esc(f.name.charAt(0)) +
      '</div><div><div class="profile-name">' + esc(f.name) + '</div><div class="profile-role">Registered Farmer</div></div></div>' +
      '<div class="profile-details">' +
      pd("Phone", f.phone) + pd("Email", f.email) + pd("Address", f.address) +
      pd("Region", f.region && f.region.name) + pd("District", f.region && f.region.district) +
      pd("State", f.region && f.region.state) +
      "</div>";
  }

  function renderOfficerProfile() {
    var o = state.officers.find(function (x) { return x.officerId === state.currentOfficerId; });
    if (!o) return;
    $("oProfile").innerHTML =
      '<div class="profile-header"><div class="profile-avatar profile-avatar--officer">' + esc(o.name.charAt(0)) +
      '</div><div><div class="profile-name">' + esc(o.name) + '</div><div class="profile-role">Agricultural Officer</div></div></div>' +
      '<div class="profile-details">' +
      pd("Phone", o.phone) + pd("Email", o.email) + pd("Specialization", o.specialization) +
      pd("Region", o.region && o.region.name) + pd("District", o.region && o.region.district) +
      pd("State", o.region && o.region.state) +
      "</div>";
  }

  function pd(label, value) {
    return '<div class="profile-detail"><div class="profile-detail__label">' + esc(label) +
      '</div><div class="profile-detail__value">' + esc(safe(value)) + "</div></div>";
  }

  /* ========== LOGOUT ========== */
  function logout() {
    state.currentRole = null;
    state.currentFarmerId = null;
    state.currentOfficerId = null;
    state.farmerTickets = [];
    state.officerTickets = [];
    state.escalatedTickets = [];
    localStorage.removeItem("currentRole");
    localStorage.removeItem("currentFarmerId");
    localStorage.removeItem("currentOfficerId");
    showScreen("landing");
    showToast("Logged out successfully.", "info");
  }

  /* ========== SESSION RESTORE ========== */
  function restoreSession() {
    var role = localStorage.getItem("currentRole");
    var fid = parseNum(localStorage.getItem("currentFarmerId"));
    var oid = parseNum(localStorage.getItem("currentOfficerId"));

    if (role === "FARMER" && fid) {
      state.currentRole = "FARMER";
      state.currentFarmerId = fid;
      api("/api/farmers").then(function (data) {
        state.farmers = data;
        var found = data.find(function (f) { return f.farmerId === fid; });
        if (!found) { logout(); return; }
        showScreen("farmer");
        initFarmerDashboard();
      }).catch(function () { logout(); });
      return true;
    }
    if (role === "OFFICER" && oid) {
      state.currentRole = "OFFICER";
      state.currentOfficerId = oid;
      api("/api/officers").then(function (data) {
        state.officers = data;
        var found = data.find(function (o) { return o.officerId === oid; });
        if (!found) { logout(); return; }
        showScreen("officer");
        initOfficerDashboard();
      }).catch(function () { logout(); });
      return true;
    }
    return false;
  }

  /* ========== INIT ========== */
  document.addEventListener("DOMContentLoaded", function () {
    /* Landing events */
    $("farmerSelect").addEventListener("change", renderFarmerPreview);
    $("officerSelect").addEventListener("change", renderOfficerPreview);
    $("farmerEnterBtn").addEventListener("click", enterFarmerPortal);
    $("officerEnterBtn").addEventListener("click", enterOfficerPortal);

    /* Nav buttons (event delegation) */
    document.addEventListener("click", function (e) {
      var btn = e.target.closest(".nav-btn, [data-view]");
      if (btn && btn.getAttribute("data-role") && btn.getAttribute("data-view")) {
        switchView(btn.getAttribute("data-role"), btn.getAttribute("data-view"));
      }
    });

    /* Mobile sidebar */
    $("farmerMenuBtn").addEventListener("click", function () { openMobileSidebar("FARMER"); });
    $("officerMenuBtn").addEventListener("click", function () { openMobileSidebar("OFFICER"); });
    $("farmerOverlay").addEventListener("click", function () { closeMobileSidebar("FARMER"); });
    $("officerOverlay").addEventListener("click", function () { closeMobileSidebar("OFFICER"); });

    /* Raise ticket form */
    $("raiseForm").addEventListener("submit", submitTicket);

    /* Ticket detail modal */
    $("tmClose").addEventListener("click", closeTicketModal);
    $("ticketModal").addEventListener("click", function (e) { if (e.target === $("ticketModal")) closeTicketModal(); });

    /* Recommendation modal */
    $("recoClose").addEventListener("click", closeRecoModal);
    $("recoCancel").addEventListener("click", closeRecoModal);
    $("recoSubmit").addEventListener("click", submitRecommendation);
    $("recoModal").addEventListener("click", function (e) { if (e.target === $("recoModal")) closeRecoModal(); });

    /* Escape key closes modals */
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape") {
        if (!$("recoModal").classList.contains("hidden")) closeRecoModal();
        else if (!$("ticketModal").classList.contains("hidden")) closeTicketModal();
      }
    });

    /* Logout */
    $("farmerLogout").addEventListener("click", logout);
    $("officerLogout").addEventListener("click", logout);

    /* Attach view handlers after table renders */
    var observer = new MutationObserver(function (mutations) {
      mutations.forEach(function (m) {
        m.addedNodes.forEach(function (node) {
          if (node.nodeType === 1 && node.querySelectorAll) {
            attachViewHandlers(node);
          }
        });
      });
    });
    observer.observe(document.body, { childList: true, subtree: true });

    /* Session restore or show landing */
    if (!restoreSession()) {
      showScreen("landing");
      loadFarmers();
      loadOfficers();
    } else {
      loadFarmers();
      loadOfficers();
    }
  });
})();
