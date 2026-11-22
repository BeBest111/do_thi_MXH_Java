/* =========================================================================
 * SocialGraph PTIT – Frontend (thuần HTML/CSS/JS, không cần thư viện ngoài)
 * Giao tiếp với backend Java qua REST API (/api/...).
 * Nếu mở index.html bằng Live Server/file:// thì đổi API_BASE bên dưới.
 * ========================================================================= */
const API_BASE = location.protocol.startsWith('http') && location.port !== '5500' ? '' : 'http://localhost:8080';
const $ = id => document.getElementById(id);
const NS = 'http://www.w3.org/2000/svg';

const state = { nodes: [], edges: [], byId: {}, selected: null, algo: 'bfs',
                hlNodes: new Set(), hlEdges: new Set(), recNodes: new Set() };
const MAJOR_COLORS = {};
// Màu tươi, đậm vừa – nổi rõ trên nền trắng và khi in
const PALETTE = ['#60a5fa', '#a78bfa', '#22d3ee', '#fb923c', '#4ade80', '#f472b6', '#facc15', '#f87171'];

/* ---------------------------------------------------------------- API */
async function api(method, url, params) {
  let opts = { method };
  // GET/DELETE: tham số đi trên URL (backend đọc DELETE /api/friendships?a=&b= từ query)
  if (params && (method === 'GET' || method === 'DELETE')) {
    url += '?' + new URLSearchParams(params);
  } else if (params) {
    opts.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    opts.body = new URLSearchParams(params);
  }
  let res;
  try { res = await fetch(API_BASE + url, opts); }
  catch (e) { toast('Không kết nối được backend Java. Đã chạy server chưa?', true); throw e; }
  const data = await res.json();
  if (!res.ok) { toast(data.error || 'Lỗi không xác định', true); throw new Error(data.error); }
  return data;
}

function toast(msg, isErr) {
  const t = $('toast');
  t.textContent = msg; t.className = 'toast' + (isErr ? ' error' : '');
  clearTimeout(toast.h); toast.h = setTimeout(() => t.classList.add('hidden'), 3000);
}
const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const nm = u => `${esc(u.name)} <span class="muted">(${esc(u.id)})</span>`;

/* ---------------------------------------------------------------- Load graph */
async function loadGraph(data) {
  data = data || await api('GET', '/api/graph');
  const old = state.byId;
  const r0 = svg.getBoundingClientRect(), cw = r0.width || 800, ch = r0.height || 560;
  const R = Math.min(cw, ch) * 0.38, N0 = data.nodes.length || 1;
  state.nodes = data.nodes.map((n, i) => {
    const o = old[n.id];
    if (o) return Object.assign(o, n);
    // Xếp ban đầu đều trên vòng tròn quanh tâm khung đồ thị
    const a = 2 * Math.PI * i / N0;
    return Object.assign(n, { x: cw / 2 + Math.cos(a) * R, y: ch / 2 + Math.sin(a) * R, vx: 0, vy: 0 });
  });
  state.byId = Object.fromEntries(state.nodes.map(n => [n.id, n]));
  state.edges = data.edges.map(([a, b]) => ({ a, b, key: edgeKey(a, b) }));
  if (state.selected && !state.byId[state.selected]) state.selected = null;
  renderStats(data.stats);
  fillSelects();
  buildGraph();
  startSim();
  loadInfluencers();
}
const edgeKey = (a, b) => a < b ? a + '|' + b : b + '|' + a;

function renderStats(s) {
  $('stats').innerHTML = [['Người dùng', s.users], ['Kết nối', s.edges], ['Cộng đồng', s.components]]
    .map(([l, v]) => `<div class="stat"><b>${v}</b><span>${l}</span></div>`).join('');
}

const SELECTS = ['mutualA', 'mutualB', 'recUser', 'pathFrom', 'pathTo', 'friendA', 'friendB'];
function fillSelects() {
  const prev = Object.fromEntries(SELECTS.map(id => [id, $(id).value]));
  const html = state.nodes.map(n => `<option value="${esc(n.id)}">${esc(n.name)} (${esc(n.id)})</option>`).join('');
  SELECTS.forEach(id => {
    const el = $(id); el.innerHTML = html;
    if (prev[id] && state.byId[prev[id]]) el.value = prev[id];
    else if (state.nodes.length) el.selectedIndex = Math.min(id.endsWith('B') || id === 'pathTo' ? 3 : 0, state.nodes.length - 1);
  });
  if (state.selected) setSelected(state.selected, true);
}

/* ---------------------------------------------------------------- Graph rendering */
const svg = $('graph');
let gEdges, gNodes, W = 800, H = 560, alpha = 0, raf = null, drag = null;

function colorOf(major) {
  if (!(major in MAJOR_COLORS)) MAJOR_COLORS[major] = PALETTE[Object.keys(MAJOR_COLORS).length % PALETTE.length];
  return MAJOR_COLORS[major];
}

function buildGraph() {
  const r = svg.getBoundingClientRect(); W = r.width || 800; H = r.height || 560;
  svg.setAttribute('viewBox', `0 0 ${W} ${H}`);
  svg.innerHTML = '';
  gEdges = document.createElementNS(NS, 'g'); gNodes = document.createElementNS(NS, 'g');
  svg.append(gEdges, gNodes);
  state.edges.forEach(e => {
    const l = document.createElementNS(NS, 'line'); l.setAttribute('class', 'edge'); e.el = l; gEdges.append(l);
  });
  state.nodes.forEach(n => {
    const g = document.createElementNS(NS, 'g'); g.setAttribute('class', 'node');
    const c = document.createElementNS(NS, 'circle');
    c.setAttribute('r', 18 + Math.min(n.degree, 6) * 2); c.setAttribute('fill', colorOf(n.major));
    const t = document.createElementNS(NS, 'text'); t.setAttribute('dy', 42 + Math.min(n.degree, 6) * 2);
    t.textContent = n.name.split(' ').slice(-1)[0];
    g.append(c, t); n.el = g; gNodes.append(g);
    g.addEventListener('mousedown', ev => { drag = n; n.fixed = true; alpha = Math.max(alpha, .3); ev.preventDefault(); });
    g.addEventListener('click', () => { if (!n.moved) setSelected(n.id); n.moved = false; });
    g.addEventListener('mouseenter', ev => showTip(n, ev));
    g.addEventListener('mousemove', ev => showTip(n, ev));
    g.addEventListener('mouseleave', () => $('tooltip').classList.add('hidden'));
  });
  applyHighlight(); tick();
}

function showTip(n, ev) {
  const tip = $('tooltip'), box = svg.parentElement.getBoundingClientRect();
  tip.innerHTML = `<b>${esc(n.name)}</b> · ${n.age} tuổi<br>${esc(n.major)} · ${esc(n.hometown)}<br>
    <span class="muted">${n.degree} bạn · ${n.hobbies.map(esc).join(', ') || '—'}</span>`;
  tip.style.left = (ev.clientX - box.left + 14) + 'px'; tip.style.top = (ev.clientY - box.top + 14) + 'px';
  tip.classList.remove('hidden');
}

window.addEventListener('mousemove', ev => {
  if (!drag) return;
  const r = svg.getBoundingClientRect();
  drag.x = (ev.clientX - r.left) * (W / r.width); drag.y = (ev.clientY - r.top) * (H / r.height);
  drag.moved = true; alpha = Math.max(alpha, .3); startSim();
});
window.addEventListener('mouseup', () => { if (drag) { drag.fixed = false; drag = null; } });

/* Mô phỏng lực đơn giản (đẩy giữa các nút + lò xo theo cạnh + hút về tâm) */
function startSim() { alpha = Math.max(alpha, 1); if (!raf) raf = requestAnimationFrame(loop); }
function loop() {
  step(); tick();
  alpha *= 0.985;
  raf = alpha > 0.02 || drag ? requestAnimationFrame(loop) : null;
}
function step() {
  const N = state.nodes;
  for (let i = 0; i < N.length; i++) for (let j = i + 1; j < N.length; j++) {
    let dx = N[j].x - N[i].x, dy = N[j].y - N[i].y, d2 = dx * dx + dy * dy || 1, d = Math.sqrt(d2);
    const f = 40000 / d2 * alpha; dx /= d; dy /= d;
    N[i].vx -= dx * f; N[i].vy -= dy * f; N[j].vx += dx * f; N[j].vy += dy * f;
  }
  state.edges.forEach(e => {
    const a = state.byId[e.a], b = state.byId[e.b]; if (!a || !b) return;
    let dx = b.x - a.x, dy = b.y - a.y, d = Math.sqrt(dx * dx + dy * dy) || 1;
    const f = (d - 170) * 0.03 * alpha; dx /= d; dy /= d;
    a.vx += dx * f; a.vy += dy * f; b.vx -= dx * f; b.vy -= dy * f;
  });
  N.forEach(n => {
    n.vx += (W / 2 - n.x) * 0.006 * alpha; n.vy += (H / 2 - n.y) * 0.006 * alpha;
    if (!n.fixed) { n.x += n.vx; n.y += n.vy; }
    n.vx *= .6; n.vy *= .6;
    n.x = Math.max(40, Math.min(W - 40, n.x)); n.y = Math.max(35, Math.min(H - 50, n.y));
  });
}
function tick() {
  state.edges.forEach(e => {
    const a = state.byId[e.a], b = state.byId[e.b]; if (!a || !b || !e.el) return;
    e.el.setAttribute('x1', a.x); e.el.setAttribute('y1', a.y); e.el.setAttribute('x2', b.x); e.el.setAttribute('y2', b.y);
  });
  state.nodes.forEach(n => n.el && n.el.setAttribute('transform', `translate(${n.x},${n.y})`));
}

function applyHighlight() {
  const any = state.hlNodes.size || state.recNodes.size;
  state.nodes.forEach(n => {
    if (!n.el) return;
    const c = ['node'];
    if (n.id === state.selected) c.push('sel');
    if (state.hlNodes.has(n.id)) c.push('hl');
    if (state.recNodes.has(n.id)) c.push('rec');
    if (any && !state.hlNodes.has(n.id) && !state.recNodes.has(n.id) && n.id !== state.selected) c.push('dim');
    n.el.setAttribute('class', c.join(' '));
  });
  state.edges.forEach(e => {
    if (!e.el) return;
    const on = state.hlEdges.has(e.key);
    e.el.setAttribute('class', 'edge' + (on ? ' hl' : (any && state.hlEdges.size ? ' dim' : '')));
  });
}
function clearHighlight() {
  state.hlNodes.clear(); state.hlEdges.clear(); state.recNodes.clear(); applyHighlight();
}
function setSelected(id, silent) {
  state.selected = id;
  const tab = document.querySelector('.tab.active').dataset.tab;
  const target = { mutual: 'mutualA', recommend: 'recUser', path: 'pathFrom', manage: 'friendA' }[tab];
  if (!silent && state.byId[id]) $(target).value = id;
  applyHighlight();
}

/* ---------------------------------------------------------------- Features */
function userCard(u, extra) {
  return `<div class="card"><div class="name">${nm(u)}</div>
    <div class="meta">${esc(u.major)} · ${esc(u.hometown)} · ${u.degree} bạn</div>${extra || ''}</div>`;
}

async function doMutual() {
  const a = $('mutualA').value, b = $('mutualB').value;
  const d = await api('GET', '/api/mutual', { a, b });
  clearHighlight();
  d.mutual.forEach(u => { state.hlNodes.add(u.id); state.hlEdges.add(edgeKey(a, u.id)); state.hlEdges.add(edgeKey(b, u.id)); });
  state.hlNodes.add(a); state.hlNodes.add(b); applyHighlight();
  $('result').innerHTML = `<p><span class="badge g">${d.count} bạn chung</span>
    <span class="badge y">Jaccard ${(d.jaccard * 100).toFixed(1)}%</span></p>
    <p class="muted">${esc(state.byId[a].name)} ⟷ ${esc(state.byId[b].name)}</p>` +
    (d.mutual.length ? d.mutual.map(u => userCard(u)).join('') : '<p class="muted">Hai người không có bạn chung.</p>');
}

async function doRecommend() {
  const user = $('recUser').value;
  const d = await api('GET', '/api/recommend', { user, algo: state.algo, depth: $('recDepth').value, limit: $('limitVal').textContent });
  clearHighlight(); state.hlNodes.add(user);
  d.results.forEach(r => { state.recNodes.add(r.user.id); r.mutual.forEach(m => {
    state.hlNodes.add(m.id); state.hlEdges.add(edgeKey(user, m.id)); state.hlEdges.add(edgeKey(m.id, r.user.id)); }); });
  applyHighlight();
  $('result').innerHTML = `<p><span class="badge p">${d.algo.toUpperCase()}</span>
    <span class="badge">${d.results.length} gợi ý cho ${esc(state.byId[user].name)}</span></p>` +
    (d.results.length ? d.results.map((r, i) => userCard(r.user,
      `<div style="margin-top:6px"><span class="badge g">${r.mutualCount} bạn chung</span>
       <span class="badge y">Điểm ${r.score.toFixed(1)}</span><span class="badge">Cách ${r.distance} bước</span></div>
       <div class="meta">${r.mutual.length ? 'Qua: ' + r.mutual.map(m => esc(m.name)).join(', ') : 'Kết nối gián tiếp'}</div>
       <div class="bar" style="width:${Math.max(12, 100 - i * 12)}%"></div>`)).join('')
      : '<p class="muted">Không có gợi ý nào (người này chưa có kết nối hoặc đã kết bạn với cả cộng đồng).</p>');
}

async function doPath() {
  const from = $('pathFrom').value, to = $('pathTo').value;
  const d = await api('GET', '/api/path', { from, to });
  clearHighlight();
  if (!d.connected) {
    $('result').innerHTML = `<p class="err">Không có đường kết nối giữa hai người (khác thành phần liên thông).</p>`; return;
  }
  d.path.forEach((u, i) => { state.hlNodes.add(u.id); if (i) state.hlEdges.add(edgeKey(d.path[i - 1].id, u.id)); });
  applyHighlight();
  $('result').innerHTML = `<p><span class="badge g">${d.distance} bước</span>
    <span class="badge y">${d.distance} độ phân cách</span></p>
    <div class="pathline">${d.path.map(u => `<span class="step">${esc(u.name)}</span>`).join('<span class="arrow">➜</span>')}</div>` +
    d.path.map(u => userCard(u)).join('');
}

async function loadInfluencers() {
  const list = await api('GET', '/api/influencers', { k: 5 });
  const max = Math.max(1, ...list.map(u => u.degree));
  $('influencers').innerHTML = list.map(u => `<li>${esc(u.name)} <small>· ${u.degree} bạn</small>
    <div class="bar" style="width:${u.degree / max * 100}%"></div></li>`).join('');
}

async function mutate(method, url, params, okMsg) {
  const d = await api(method, url, params);
  clearHighlight(); await loadGraph(d); toast(okMsg);
}

/* ---------------------------------------------------------------- Events */
$('tabs').addEventListener('click', e => {
  const t = e.target.closest('.tab'); if (!t) return;
  document.querySelectorAll('.tab').forEach(x => x.classList.toggle('active', x === t));
  document.querySelectorAll('.tabpane').forEach(p => p.classList.toggle('active', p.id === 'pane-' + t.dataset.tab));
});
$('algoSeg').addEventListener('click', e => {
  const b = e.target.closest('button'); if (!b) return;
  state.algo = b.dataset.algo;
  document.querySelectorAll('#algoSeg button').forEach(x => x.classList.toggle('active', x === b));
  $('depthRow').classList.toggle('hidden', state.algo !== 'dfs');
});
$('recDepth').oninput = e => $('depthVal').textContent = e.target.value;
$('recLimit').oninput = e => $('limitVal').textContent = e.target.value;
$('btnMutual').onclick = doMutual;
$('btnRecommend').onclick = doRecommend;
$('btnPath').onclick = doPath;
$('btnClear').onclick = () => { clearHighlight(); $('result').innerHTML = '<p class="muted">Đã bỏ làm nổi bật.</p>'; };
$('btnAddUser').onclick = async () => {
  const name = $('newName').value.trim(); if (!name) return toast('Hãy nhập họ tên', true);
  await mutate('POST', '/api/users', { name, age: $('newAge').value || 20, gender: $('newGender').value,
    major: $('newMajor').value || 'CNTT', hometown: $('newHometown').value || 'Hà Nội', hobbies: $('newHobbies').value },
    'Đã thêm người dùng ' + name);
  ['newName', 'newAge', 'newMajor', 'newHometown', 'newHobbies'].forEach(i => $(i).value = '');
};
$('btnAddFriend').onclick = () => mutate('POST', '/api/friendships', { a: $('friendA').value, b: $('friendB').value }, 'Đã kết bạn');
$('btnRemoveFriend').onclick = () => mutate('DELETE', '/api/friendships', { a: $('friendA').value, b: $('friendB').value }, 'Đã hủy kết bạn');
$('btnDeleteUser').onclick = () => {
  const id = $('friendA').value;
  if (confirm('Xóa ' + state.byId[id].name + ' và toàn bộ kết nối?')) mutate('DELETE', '/api/users/' + encodeURIComponent(id), null, 'Đã xóa người dùng');
};
$('btnReset').onclick = () => mutate('POST', '/api/reset', {}, 'Đã nạp lại dữ liệu mẫu');
window.addEventListener('resize', () => { buildGraph(); startSim(); });

/* Tham số ?demo=... tự chạy một chức năng khi mở trang (tiện chụp ảnh báo cáo):
 * mutual | bfs | dfs | path | manage */
async function runDemo() {
  const demo = new URLSearchParams(location.search).get('demo');
  if (!demo) return;
  const click = tab => document.querySelector(`.tab[data-tab="${tab}"]`).click();
  if (demo === 'mutual') { click('mutual'); $('mutualA').value = 'U01'; $('mutualB').value = 'U04'; await doMutual(); }
  if (demo === 'bfs' || demo === 'dfs') {
    click('recommend'); $('recUser').value = 'U01';
    document.querySelector(`#algoSeg button[data-algo="${demo}"]`).click(); await doRecommend();
  }
  if (demo === 'path') { click('path'); $('pathFrom').value = 'U01'; $('pathTo').value = 'U10'; await doPath(); }
  if (demo === 'manage') click('manage');
}

/* ?rects=1: sau khi đồ thị ổn định, ghi tọa độ các vùng giao diện vào <body data-rects>
 * (dùng cho script cắt ảnh phóng to trong báo cáo). */
function reportRects() {
  if (!new URLSearchParams(location.search).get('rects')) return;
  // Khung cao: cho bảng điều khiển / kết quả giãn theo nội dung để không bị cắt
  if (new URLSearchParams(location.search).get('rects') === 'tall') document.querySelector('.layout').style.alignItems = 'start';
  setTimeout(() => {
    const R = el => { const r = el.getBoundingClientRect(); return [r.left, r.top, r.right, r.bottom]; };
    const fit = (panel, kids) => {
      const p = R(panel), bottom = Math.max(...kids.filter(Boolean).map(k => R(k)[3])) + 18;
      return [p[0], p[1], p[2], Math.min(p[3], bottom)];
    };
    const rects = {
      controls: fit(document.querySelector('.controls'), [document.querySelector('.tabpane.active')]),
      stage: R(document.querySelector('.stage')),
      results: fit(document.querySelector('.results'), [$('result'), $('influencers')]),
    };
    rects.vw = [innerWidth, innerHeight];
    document.body.dataset.rects = JSON.stringify(rects);
  }, 6000);
}

loadGraph().then(runDemo).then(reportRects);
