/* =========================================================================
 * CHẾ ĐỘ TỰ ĐỘNG DEMO  –  mở trang với  ?demo=auto
 * Tự bấm lần lượt mọi chức năng, có khung thuyết minh từng bước.
 * Phím tắt:  Space = tạm dừng / tiếp tục · → = bước tiếp · ← = bước trước · Esc = thoát
 * Tham số thêm: &speed=1.5 (chậm hơn) hoặc &speed=0.7 (nhanh hơn)
 * ========================================================================= */
(function () {
  const params = new URLSearchParams(location.search);
  if (params.get('demo') !== 'auto') return;
  const SPEED = Math.max(0.2, parseFloat(params.get('speed')) || 1);

  /* ------------------------------------------------------------ Giao diện */
  const css = document.createElement('style');
  css.textContent = `
    body.demo-on{padding-bottom:150px}
    @media (min-width:1101px){
      body.demo-on{height:100vh;min-height:0;overflow:hidden;padding-bottom:var(--demo-h,150px)}
      body.demo-on .layout{min-height:0;padding-top:10px;padding-bottom:10px}
      body.demo-on .stage{min-height:0}
      body.demo-on #graph{min-height:0}
      body.demo-on .controls{overflow:auto}
    }
    body.demo-on .toast{bottom:170px}
    .demo-box{position:fixed;left:50%;bottom:14px;transform:translateX(-50%);z-index:50;
      width:min(1100px,calc(100vw - 32px));background:#0f172a;color:#f8fafc;border-radius:14px;
      box-shadow:0 10px 30px rgba(0,0,0,.35);padding:14px 20px 12px;font-size:19px;line-height:1.45}
    .demo-head{display:flex;align-items:center;gap:12px;margin-bottom:4px}
    .demo-step{background:#1d4ed8;color:#fff;font-weight:700;border-radius:999px;padding:2px 12px;font-size:15px;white-space:nowrap}
    .demo-title{font-weight:700;font-size:21px;flex:1}
    .demo-btns{display:flex;gap:6px}
    .demo-btns button{background:#1e293b;color:#e2e8f0;border:1px solid #334155;border-radius:8px;
      padding:4px 10px;font-size:15px;cursor:pointer}
    .demo-btns button:hover{background:#334155}
    .demo-text{color:#cbd5e1}
    .demo-text b{color:#fde68a}
    .demo-prog{height:4px;background:#1e293b;border-radius:4px;margin-top:10px;overflow:hidden}
    .demo-prog i{display:block;height:100%;width:0;background:#60a5fa;transition:width .4s}
    .demo-focus{outline:4px solid #f59e0b!important;outline-offset:3px;border-radius:8px;
      box-shadow:0 0 0 8px rgba(245,158,11,.25)!important;transition:outline-color .2s}
    .demo-paused .demo-step{background:#b45309}
    .demo-end{position:fixed;inset:0;z-index:60;background:rgba(15,23,42,.72);display:grid;place-items:center}
    .demo-end .card2{background:#fff;color:#111827;border-radius:16px;padding:28px 36px;max-width:640px;text-align:center}
    .demo-end h2{margin:0 0 8px;font-size:28px}
    .demo-end button{margin:14px 6px 0;font-size:18px;padding:8px 18px;border-radius:10px;border:0;cursor:pointer;
      background:#1d4ed8;color:#fff}
    .demo-end button.sec{background:#e5e7eb;color:#111827}`;
  document.head.append(css);
  document.body.classList.add('demo-on');

  const box = document.createElement('div');
  box.className = 'demo-box';
  box.innerHTML = `
    <div class="demo-head">
      <span class="demo-step" id="dStep">Bước 0</span>
      <span class="demo-title" id="dTitle">Đang khởi động demo…</span>
      <span class="demo-btns">
        <button id="dPrev" title="Bước trước (←)">⏮</button>
        <button id="dPause" title="Tạm dừng / tiếp tục (Space)">⏸ Dừng</button>
        <button id="dNext" title="Bước tiếp (→)">⏭</button>
        <button id="dExit" title="Thoát demo (Esc)">✕</button>
      </span>
    </div>
    <div class="demo-text" id="dText"></div>
    <div class="demo-prog"><i id="dProg"></i></div>`;
  document.body.append(box);

  /* ------------------------------------------------------------ Điều khiển */
  let paused = false, skip = false, jumpTo = null, stopped = false;
  const setPaused = p => {
    paused = p; box.classList.toggle('demo-paused', p);
    $('dPause').textContent = p ? '▶ Tiếp' : '⏸ Dừng';
  };
  $('dPause').onclick = () => setPaused(!paused);
  $('dNext').onclick = () => { skip = true; setPaused(false); };
  $('dPrev').onclick = () => { jumpTo = Math.max(0, cur - 1); skip = true; setPaused(false); };
  $('dExit').onclick = exitDemo;
  window.addEventListener('keydown', e => {
    if (e.target.matches('input,select,textarea')) return;
    if (e.code === 'Space') { e.preventDefault(); $('dPause').click(); }
    else if (e.key === 'ArrowRight') $('dNext').click();
    else if (e.key === 'ArrowLeft') $('dPrev').click();
    else if (e.key === 'Escape') exitDemo();
  });
  function exitDemo() {
    stopped = true; skip = true;
    box.remove(); document.body.classList.remove('demo-on'); document.body.style.removeProperty('--demo-h');
    document.querySelectorAll('.demo-focus').forEach(x => x.classList.remove('demo-focus'));
    history.replaceState(null, '', location.pathname);
    setTimeout(() => { buildGraph(); startSim(); }, 50);
  }

  class Skip extends Error {}
  // Chờ có tính tạm dừng / bỏ qua
  async function wait(ms) {
    let left = ms * SPEED;
    while (left > 0 || paused) {
      if (skip || stopped) throw new Skip();
      await new Promise(r => setTimeout(r, 100));
      if (!paused) left -= 100;
    }
    if (skip || stopped) throw new Skip();
  }
  async function focus(el, ms = 700) {
    document.querySelectorAll('.demo-focus').forEach(x => x.classList.remove('demo-focus'));
    if (!el) return;
    el.classList.add('demo-focus');
    el.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    await wait(ms);
  }
  const unfocus = () => document.querySelectorAll('.demo-focus').forEach(x => x.classList.remove('demo-focus'));
  async function tab(name) {
    const t = document.querySelector(`.tab[data-tab="${name}"]`);
    await focus(t, 600); t.click(); await wait(400);
  }
  async function pick(id, value) { await focus($(id), 500); $(id).value = value; await wait(500); }
  async function press(id, fn) { await focus($(id), 700); await fn(); unfocus(); }
  async function type(id, text) {
    const el = $(id); await focus(el, 300); el.value = '';
    for (const ch of text) { el.value += ch; await wait(55); }
    await wait(250);
  }
  const stage = () => document.querySelector('.stage');
  const results = () => document.querySelector('.results');
  let cur = 0;
  function caption(i, title, html) {
    $('dStep').textContent = `Bước ${i + 1}/${STEPS.length}`;
    $('dTitle').textContent = title;
    $('dText').innerHTML = html;
    $('dProg').style.width = ((i + 1) / STEPS.length * 100) + '%';
    fitBox();
  }
  // Chừa đúng chiều cao khung thuyết minh ở đáy màn hình để không che đồ thị
  function fitBox() {
    const h = Math.ceil(box.getBoundingClientRect().height) + 24;
    if (document.body.style.getPropertyValue('--demo-h') !== h + 'px') {
      document.body.style.setProperty('--demo-h', h + 'px');
      buildGraph(); startSim();
    }
  }
  window.addEventListener('resize', fitBox);
  async function resetData() {
    clearHighlight();
    await loadGraph(await api('POST', '/api/reset', {}));
  }
  async function algo(a) {
    const b = document.querySelector(`#algoSeg button[data-algo="${a}"]`);
    await focus(b, 600); b.click(); await wait(400);
  }
  const newUserId = () => (state.nodes.find(n => n.name === 'Sinh Viên Demo') || {}).id;

  /* ------------------------------------------------------------ Kịch bản */
  const STEPS = [
    ['Giới thiệu mạng xã hội', `Mạng xã hội của <b>12 sinh viên PTIT</b> được biểu diễn bằng <b>đồ thị vô hướng</b>
      (danh sách kề <code>HashMap&lt;String, Set&lt;String&gt;&gt;</code>). Mỗi nút là một người, mỗi cạnh là quan hệ bạn bè.
      Màu nút = ngành học, kích thước nút = số bạn.`,
      async () => {
        await resetData(); $('result').innerHTML = '<p class="muted">Demo tự động đang chạy…</p>';
        await focus($('stats'), 2500); await focus(stage(), 4000); unfocus(); await wait(2500);
      }],

    ['Chức năng 1 · Bạn bè chung', `Tìm bạn chung giữa <b>Nguyễn Minh Tiến (U01)</b> và <b>Đỗ Quang Huy (U07)</b>:
      lấy <b>giao</b> hai tập bạn bè. Độ tương đồng <b>Jaccard</b> = |A ∩ B| / |A ∪ B|.`,
      async () => {
        await tab('mutual'); await pick('mutualA', 'U01'); await pick('mutualB', 'U07');
        await press('btnMutual', doMutual); await focus($('result'), 1500); unfocus(); await wait(5000);
      }],

    ['Chức năng 2 · Gợi ý kết bạn bằng BFS', `<b>BFS – “bạn của bạn”</b>: duyệt theo từng lớp, lấy những người cách U01 đúng
      <b>2 bước</b>. Điểm xếp hạng = số bạn chung × 2 + 0.5 (cùng ngành) + 0.5 (cùng quê) + 0.3 × số sở thích chung.
      Nút <b style="color:#f472b6">hồng</b> là người được gợi ý.`,
      async () => {
        await tab('recommend'); await pick('recUser', 'U01'); await algo('bfs');
        await press('btnRecommend', doRecommend); await focus($('result'), 1500); unfocus(); await wait(6500);
      }],

    ['Chức năng 2 · Gợi ý kết bạn bằng DFS', `<b>DFS – khám phá sâu</b> (độ sâu tối đa 3): đi sâu theo từng nhánh nên tìm ra
      cả người ở xa hơn – ví dụ <b>Bùi Quốc Anh (U09)</b> cách U01 tới <b>3 bước</b>, điều mà BFS 2 tầng không thấy.`,
      async () => {
        await tab('recommend'); await pick('recUser', 'U01'); await algo('dfs');
        $('recDepth').value = 3; $('depthVal').textContent = '3'; await focus($('depthRow'), 900);
        await press('btnRecommend', doRecommend); await focus($('result'), 1500); unfocus(); await wait(6500);
      }],

    ['Chức năng 3 · Đường kết nối ngắn nhất (BFS)', `Tìm đường ngắn nhất từ <b>Tiến (U01)</b> đến <b>Hoàng Kim Yến (U10)</b>.
      BFS trên đồ thị không trọng số luôn cho đường ngắn nhất; lộ trình được <b>truy vết qua mảng cha</b>. Độ phức tạp O(V + E).`,
      async () => {
        await tab('path'); await pick('pathFrom', 'U01'); await pick('pathTo', 'U10');
        await press('btnPath', doPath); await focus($('result'), 1500); unfocus(); await wait(6000);
      }],

    ['Trường hợp không có đường đi', `Từ U01 đến <b>Đặng Văn Nam (U11)</b>: U11 và U12 tạo thành một
      <b>thành phần liên thông riêng</b>, nên BFS duyệt hết mà không gặp → chương trình báo <b>không có đường kết nối</b>.`,
      async () => {
        await tab('path'); await pick('pathFrom', 'U01'); await pick('pathTo', 'U11');
        await press('btnPath', doPath); await focus($('result'), 1500); unfocus(); await wait(5000);
      }],

    ['Quản lý · Kết bạn (thêm cạnh)', `Cho <b>Hoàng Kim Yến (U10)</b> kết bạn với <b>Đặng Văn Nam (U11)</b>: thêm cạnh hai chiều vào
      danh sách kề. Hai cộng đồng được nối lại → số <b>cộng đồng giảm từ 2 xuống 1</b>.`,
      async () => {
        await tab('manage'); await pick('friendA', 'U10'); await pick('friendB', 'U11');
        await press('btnAddFriend', () => mutate('POST', '/api/friendships', { a: 'U10', b: 'U11' }, 'Đã kết bạn U10 – U11'));
        await focus($('stats'), 2500); unfocus(); await wait(3000);
      }],

    ['Tìm lại đường đi sau khi kết bạn', `Bây giờ tìm lại đường từ <b>U01</b> đến <b>U11</b>: đã có đường kết nối đi qua cạnh vừa thêm.`,
      async () => {
        await tab('path'); await pick('pathFrom', 'U01'); await pick('pathTo', 'U11');
        await press('btnPath', doPath); await focus($('result'), 1500); unfocus(); await wait(5500);
      }],

    ['Quản lý · Thêm người dùng (thêm đỉnh)', `Thêm một sinh viên mới vào đồ thị, rồi cho kết bạn với <b>Tiến (U01)</b>.`,
      async () => {
        // Nếu quay lại bước này lần nữa: xóa bản cũ để không bị trùng
        if (newUserId()) await mutate('DELETE', '/api/users/' + encodeURIComponent(newUserId()), null, 'Chuẩn bị lại dữ liệu');
        await tab('manage');
        await type('newName', 'Sinh Viên Demo'); await type('newAge', '20');
        $('newGender').value = 'Nữ'; await type('newMajor', 'CNTT');
        await type('newHometown', 'Hà Nội'); await type('newHobbies', 'Lập trình, AI');
        await press('btnAddUser', () => $('btnAddUser').onclick());
        await wait(1500);
        const id = newUserId();
        if (id) {
          await pick('friendA', id); await pick('friendB', 'U01');
          await press('btnAddFriend', () => mutate('POST', '/api/friendships', { a: id, b: 'U01' }, 'Đã kết bạn với U01'));
        }
        await focus(stage(), 2000); unfocus(); await wait(2500);
      }],

    ['Gợi ý kết bạn cho người mới', `Người mới chỉ quen <b>Tiến</b> → BFS gợi ý ngay bạn bè của Tiến, ưu tiên người
      <b>cùng ngành CNTT, cùng quê Hà Nội, cùng sở thích</b> (điểm cao hơn).`,
      async () => {
        const id = newUserId(); if (!id) return;
        await tab('recommend'); await pick('recUser', id); await algo('bfs');
        await press('btnRecommend', doRecommend); await focus($('result'), 1500); unfocus(); await wait(6000);
      }],

    ['Quản lý · Hủy kết bạn & Xóa người dùng', `<b>Hủy kết bạn</b> U10 – U11 (xóa cạnh) → lại tách thành 2 cộng đồng.
      Sau đó <b>xóa người dùng</b> mới thêm (xóa đỉnh và mọi cạnh nối với đỉnh đó).`,
      async () => {
        await tab('manage'); await pick('friendA', 'U10'); await pick('friendB', 'U11');
        await press('btnRemoveFriend', () => mutate('DELETE', '/api/friendships', { a: 'U10', b: 'U11' }, 'Đã hủy kết bạn U10 – U11'));
        await focus($('stats'), 2000);
        const id = newUserId();
        if (id) {
          await pick('friendA', id);
          await press('btnDeleteUser', () => mutate('DELETE', '/api/users/' + encodeURIComponent(id), null, 'Đã xóa người dùng'));
        }
        await focus($('stats'), 2000); unfocus(); await wait(2500);
      }],

    ['Top người có ảnh hưởng', `Xếp hạng theo <b>bậc của đỉnh</b> (số bạn bè): <b>Trần Thu Hà</b> và <b>Ngô Quỳnh Chi</b>
      cùng có 4 bạn – là những “cầu nối” quan trọng nhất của mạng.`,
      async () => {
        clearHighlight();
        const top = state.nodes.slice().sort((a, b) => b.degree - a.degree).slice(0, 2);
        top.forEach(n => state.hlNodes.add(n.id)); applyHighlight();
        await focus($('influencers'), 6500); unfocus(); await wait(1500);
      }],
  ];

  function endScreen() {
    unfocus();
    caption(STEPS.length - 1, 'Hoàn tất demo', 'Đã trình bày xong toàn bộ chức năng. Dữ liệu đã được đưa về trạng thái ban đầu.');
    const end = document.createElement('div');
    end.className = 'demo-end';
    end.innerHTML = `<div class="card2"><h2>✔ Hoàn tất demo</h2>
      <p>Đã trình bày: Bạn bè chung · Gợi ý BFS · Gợi ý DFS · Đường ngắn nhất · Thêm/xóa người dùng ·
      Kết bạn/hủy kết bạn · Top ảnh hưởng.</p>
      <button id="dAgain">↻ Chạy lại demo</button><button class="sec" id="dFree">Tự thao tác</button></div>`;
    document.body.append(end);
    $('dAgain').onclick = () => location.reload();
    $('dFree').onclick = () => { end.remove(); exitDemo(); };
  }

  async function run() {
    await new Promise(r => setTimeout(r, 800));
    for (cur = 0; cur < STEPS.length && !stopped; ) {
      const [title, text, fn] = STEPS[cur];
      caption(cur, title, text);
      try { await fn(); cur++; }
      catch (e) {
        if (!(e instanceof Skip)) { console.error(e); cur++; continue; }
        skip = false;
        if (stopped) return;
        if (jumpTo !== null) { cur = jumpTo; jumpTo = null; } else cur++;
      }
    }
    if (stopped) return;
    try { await resetData(); } catch (e) { /* bỏ qua */ }
    endScreen();
  }

  // Đợi app.js nạp xong đồ thị lần đầu
  const ready = setInterval(() => {
    if (state.nodes.length) { clearInterval(ready); buildGraph(); startSim(); run(); }
  }, 200);
})();
