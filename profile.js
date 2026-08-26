const REPO_OWNER = 'Semin696';
const REPO_NAME = 'moonlightlauncher';
const FOLDER = 'realise';
const EXE_EXT = /\.(exe|zip|jar|msi)$/i;
const API_URL = `https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/contents/${FOLDER}`;
const TOKEN_KEY = 'mlv_token';

let user = null;
let downloadsLoaded = false;

const $ = id => document.getElementById(id);

function esc(str) {
  const div = document.createElement('div');
  div.textContent = String(str);
  return div.innerHTML;
}

async function api(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(path, { method, headers, body: body ? JSON.stringify(body) : undefined });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || 'Ошибка запроса');
  return data;
}

function isOwner() {
  return user && user.role === 'owner';
}

function subLabel() {
  return isOwner() ? 'Владельческая подписка · активна' : 'Подписка · активна';
}

function renderAuthArea() {
  const area = $('authArea');
  if (!user) {
    area.innerHTML = `<a class="btn ghost small" href="index.html">Войти</a>`;
    return;
  }
  area.innerHTML = `
    <span class="badge name">${esc(user.username)}</span>
    <button class="btn ghost small" id="logoutBtn">Выйти</button>
  `;
  $('logoutBtn').addEventListener('click', async () => {
    const token = localStorage.getItem(TOKEN_KEY);
    localStorage.removeItem(TOKEN_KEY);
    if (token) await api('/api/logout', { method: 'POST', body: { token } }).catch(() => {});
    location.reload();
  });
}

function formatSize(bytes) {
  if (bytes == null) return '';
  const mb = bytes / 1024 / 1024;
  return mb >= 1 ? `${mb.toFixed(1)} МБ` : `${(bytes / 1024).toFixed(0)} КБ`;
}

function loadDownloads() {
  if (downloadsLoaded) return;
  downloadsLoaded = true;
  fetch(`${API_URL}?t=${Date.now()}`)
    .then(r => {
      if (!r.ok) throw new Error(r.status);
      return r.json();
    })
    .then(files => {
      if (!Array.isArray(files)) throw new Error('bad response');
      const builds = files.filter(f => f.type === 'file' && EXE_EXT.test(f.name));
      if (builds.length === 0) {
        $('downloadList').classList.add('hidden');
        $('errorState').classList.remove('hidden');
        return;
      }
      $('downloadList').innerHTML = builds.map(file => `
        <div class="download-item">
          <div class="file-info">
            <div class="file-name">${esc(file.name)}</div>
            <div class="file-meta">Размер: ${formatSize(file.size)}</div>
          </div>
          <a class="btn primary" href="${esc(file.download_url)}" download>Скачать</a>
        </div>
      `).join('');
    })
    .catch(() => {
      $('downloadList').classList.add('hidden');
      $('errorState').classList.remove('hidden');
    });
}

function renderProfile() {
  $('profileAvatar').textContent = (user.username[0] || '?').toUpperCase();
  $('profileName').textContent = user.username;
  let roleText = 'Пользователь';
  if (user.role === 'owner') roleText = 'Владелец';
  else if (user.role === 'tester') roleText = 'Тестер';
  else if (user.subscribed) roleText = 'С подпиской';
  $('profileRole').textContent = roleText;
  $('profileSub').innerHTML = user.subscribed
    ? `<span class="ok">${subLabel()}</span>`
    : '<span class="err">Нет</span>';
  $('profileDate').textContent = user.createdAt
    ? new Date(user.createdAt).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })
    : '—';
  $('launcherSubs').innerHTML = user.subscribed
    ? `<span class="badge sub">${subLabel()}</span>`
    : '<span class="badge nosub">Нет</span>';
  $('subOwnerBlock').classList.toggle('hidden', !isOwner());
}

function switchTab(tab) {
  document.querySelectorAll('[data-tab]').forEach(t => t.classList.toggle('active', t.dataset.tab === tab));
  $('tabInfo').classList.toggle('hidden', tab !== 'info');
  $('tabLauncher').classList.toggle('hidden', tab !== 'launcher');
  $('tabSub').classList.toggle('hidden', tab !== 'sub');
  if (tab === 'launcher') renderLauncherTab();
}

function renderLauncherTab() {
  const gate = $('launcherGate');
  const dl = $('launcherDownloads');
  if (!user.subscribed) {
    gate.classList.remove('hidden');
    dl.classList.add('hidden');
    return;
  }
  gate.classList.add('hidden');
  dl.classList.remove('hidden');
  loadDownloads();
}

document.querySelectorAll('[data-tab]').forEach(btn => {
  btn.addEventListener('click', () => {
    history.replaceState(null, '', `#${btn.dataset.tab}`);
    switchTab(btn.dataset.tab);
  });
});

$('toSubTabBtn').addEventListener('click', () => switchTab('sub'));

$('subActivateBtn').addEventListener('click', async () => {
  const code = $('subCodeInput').value.trim();
  if (!code) return;
  try {
    const data = await api('/api/activate', { method: 'POST', body: { code } });
    $('subStatus').innerHTML = `<span class="ok">${esc(data.message)}</span>`;
    $('subCodeInput').value = '';
    const me = await api('/api/me');
    user = me.user;
    renderAuthArea();
    renderProfile();
    switchTab('launcher');
  } catch (err) {
    $('subStatus').innerHTML = `<span class="err">${esc(err.message)}</span>`;
  }
});

$('tabGenKeyBtn').addEventListener('click', async () => {
  try {
    const data = await api('/api/codes', { method: 'POST', body: { type: $('keyType').value } });
    navigator.clipboard?.writeText(data.code).catch(() => {});
    $('tabGenKeyStatus').innerHTML =
      `<span class="ok">Ключ создан: <span class="code-value">${esc(data.code)}</span> — скопирован в буфер</span>`;
  } catch (err) {
    $('tabGenKeyStatus').innerHTML = `<span class="err">${esc(err.message)}</span>`;
  }
});

async function init() {
  const hash = location.hash.slice(1);
  if (!localStorage.getItem(TOKEN_KEY)) {
    $('pageLoading').classList.add('hidden');
    $('guestGate').classList.remove('hidden');
    return;
  }
  try {
    const me = await api('/api/me');
    user = me.user;
  } catch {
    localStorage.removeItem(TOKEN_KEY);
  }
  $('pageLoading').classList.add('hidden');
  if (!user) {
    $('guestGate').classList.remove('hidden');
    return;
  }
  $('profileContent').classList.remove('hidden');
  renderAuthArea();
  renderProfile();
  switchTab(['info', 'launcher', 'sub'].includes(hash) ? hash : 'info');
}

init();
