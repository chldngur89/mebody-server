const state = {
  config: null,
  token: localStorage.getItem('mebody.server.accessToken') || '',
  me: null,
  selectedUser: null,
  currentTab: 'users',
};

const $ = (selector) => document.querySelector(selector);

function message(text, ok = true) {
  const el = [...document.querySelectorAll('[data-message]')]
    .find((candidate) => !candidate.closest('.hidden'));
  if (!el) return;
  el.textContent = text;
  el.className = `message show ${ok ? 'ok' : 'err'}`;
}

async function loadConfig() {
  const response = await fetch('/api/public/config');
  const payload = await response.json();
  state.config = payload.data;
}

async function supabaseLogin(email, password) {
  if (!state.config?.supabaseUrl || !state.config?.supabaseAnonKey) {
    throw new Error('Server .env에 SUPABASE_URL과 SUPABASE_ANON_KEY가 필요합니다.');
  }
  const response = await fetch(`${state.config.supabaseUrl}/auth/v1/token?grant_type=password`, {
    method: 'POST',
    headers: {
      apikey: state.config.supabaseAnonKey,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });
  const payload = await response.json();
  if (!response.ok) throw new Error(payload.error_description || payload.msg || payload.message || '로그인에 실패했습니다.');
  return payload;
}

async function api(path, options = {}) {
  if (!state.token) throw new Error('로그인이 필요합니다.');
  const headers = { Authorization: `Bearer ${state.token}`, ...(options.headers || {}) };
  if (options.body && !(options.body instanceof FormData)) headers['Content-Type'] = 'application/json';
  const response = await fetch(path, { ...options, headers });
  const contentType = response.headers.get('content-type') || '';
  const payload = contentType.includes('application/json') ? await response.json() : null;
  if (!response.ok) {
    const error = new Error(
      response.status === 401
        ? '로그인이 필요합니다.'
        : response.status === 403
          ? '관리자 권한이 필요합니다.'
          : payload?.message || `API 요청 실패 (${response.status})`
    );
    error.status = response.status;
    throw error;
  }
  return payload?.data;
}

function setView(view) {
  ['loginView', 'adminView', 'deniedView'].forEach((id) => $(`#${id}`)?.classList.add('hidden'));
  $(`#${view}`)?.classList.remove('hidden');
}

function setTab(tab) {
  state.currentTab = tab;
  document.querySelectorAll('[data-tab]').forEach((button) => button.classList.toggle('active', button.dataset.tab === tab));
  $('#usersSection').classList.toggle('hidden', tab !== 'users');
  $('#storageSection').classList.toggle('hidden', tab !== 'storage');
  if (tab === 'storage') loadImages();
}

async function bootstrap() {
  await loadConfig();
  if (!state.token) {
    setView('loginView');
    return;
  }
  try {
    state.me = await api('/api/admin/me');
    setView('adminView');
    $('#adminEmail').textContent = state.me.email || 'ADMIN';
    await Promise.all([loadSummary(), loadUsers()]);
  } catch (error) {
    localStorage.removeItem('mebody.server.accessToken');
    state.token = '';
    if (error.status === 403) {
      setView('deniedView');
      $('#deniedEmail').textContent = '';
      return;
    }
    setView('loginView');
    message(error.message || '로그인이 필요합니다.', false);
  }
}

async function loadSummary() {
  const summary = await api('/api/admin/dashboard/summary');
  const items = [
    ['전체 회원', summary.totalUsers, '누적 프로필'],
    ['활성 회원', summary.activeUsers, 'ACTIVE'],
    ['정지 회원', summary.suspendedUsers, 'SUSPENDED'],
    ['판매자', summary.sellerUsers, 'SELLER'],
    ['관리자', summary.adminUsers, 'ADMIN'],
    ['오늘 가입', summary.todaySignups, 'UTC 기준'],
  ];
  $('#summaryGrid').innerHTML = items.map(([label, value, hint]) => `
    <article class="panel summary-card"><span>${label}</span><strong>${value ?? 0}</strong><span>${hint}</span></article>
  `).join('');
}

async function loadUsers() {
  const params = new URLSearchParams();
  const search = $('#search').value.trim();
  const status = $('#statusFilter').value;
  if (search) params.set('search', search);
  if (status) params.set('status', status);
  if ($('#includeDeleted').checked) params.set('includeDeleted', 'true');
  params.set('size', '50');
  const page = await api(`/api/admin/users?${params.toString()}`);
  const users = page.content || [];
  $('#userRows').innerHTML = users.map((user) => `
    <tr data-user-id="${user.id}">
      <td><strong>${escapeHtml(user.email || '')}</strong><br><span style="color:#64748b">${escapeHtml(user.name || user.nickname || '-')}</span></td>
      <td><span class="pill ${user.role}">${user.role}</span></td>
      <td><span class="pill ${user.status}">${user.status}</span></td>
      <td><span class="pill">${user.grade}</span></td>
      <td>${escapeHtml(user.bodyBtiCode || '-')}</td>
      <td>${Number(user.missionAchievementRate || 0)}%</td>
      <td>${formatDate(user.createdAt)}</td>
    </tr>
  `).join('') || '<tr><td colspan="7" style="color:#64748b">회원이 없습니다.</td></tr>';
  document.querySelectorAll('[data-user-id]').forEach((row) => {
    const user = users.find((item) => item.id === row.dataset.userId);
    row.addEventListener('click', () => selectUser(user));
  });
}

function selectUser(user) {
  state.selectedUser = user;
  $('#detailEmpty').classList.add('hidden');
  $('#detailForm').classList.remove('hidden');
  $('#detailEmail').textContent = user.email || '';
  ['name', 'nickname', 'phone', 'role', 'status', 'grade', 'bodyBtiCode', 'bodyBtiTitle', 'bodyBtiDescription', 'missionAchievementRate'].forEach((key) => {
    const input = $(`#edit-${key}`);
    if (input) input.value = user[key] ?? '';
  });
}

async function saveUser() {
  if (!state.selectedUser) return;
  const payload = {
    name: $('#edit-name').value || null,
    nickname: $('#edit-nickname').value || null,
    phone: $('#edit-phone').value || null,
    role: $('#edit-role').value,
    status: $('#edit-status').value,
    grade: $('#edit-grade').value,
    bodyBtiCode: $('#edit-bodyBtiCode').value || null,
    bodyBtiTitle: $('#edit-bodyBtiTitle').value || null,
    bodyBtiDescription: $('#edit-bodyBtiDescription').value || null,
    missionAchievementRate: Number($('#edit-missionAchievementRate').value || 0),
  };
  const updated = await api(`/api/admin/users/${state.selectedUser.id}`, { method: 'PATCH', body: JSON.stringify(payload) });
  state.selectedUser = updated;
  message('회원 정보가 저장되었습니다.', true);
  await Promise.all([loadSummary(), loadUsers()]);
}

async function softDeleteUser() {
  if (!state.selectedUser) return;
  if (!confirm(`${state.selectedUser.email} 회원을 삭제 처리할까요?`)) return;
  await api(`/api/admin/users/${state.selectedUser.id}`, { method: 'DELETE' });
  state.selectedUser = null;
  $('#detailForm').classList.add('hidden');
  $('#detailEmpty').classList.remove('hidden');
  message('회원이 삭제 처리되었습니다.', true);
  await Promise.all([loadSummary(), loadUsers()]);
}

async function loadImages() {
  const prefix = $('#imagePrefix').value.trim() || 'characters';
  const images = await api(`/api/admin/storage/images?${new URLSearchParams({ prefix })}`);
  $('#storageGrid').innerHTML = (images || []).map((image) => `
    <article class="image-card">
      <img src="${image.publicUrl}" alt="${escapeHtml(image.path)}" />
      <div>${escapeHtml(image.path)}<br><button class="btn btn-ghost" type="button" data-delete-image="${escapeAttr(image.path)}" style="margin-top:10px;min-height:36px">삭제</button></div>
    </article>
  `).join('') || '<div style="padding:20px;color:#64748b">이미지가 없습니다.</div>';
  document.querySelectorAll('[data-delete-image]').forEach((button) => {
    button.addEventListener('click', async () => {
      if (!confirm(`${button.dataset.deleteImage} 이미지를 삭제할까요?`)) return;
      await api(`/api/admin/storage/images?${new URLSearchParams({ path: button.dataset.deleteImage })}`, { method: 'DELETE' });
      await loadImages();
    });
  });
}

async function uploadImage() {
  const file = $('#uploadFile').files?.[0];
  const path = $('#uploadPath').value.trim();
  if (!file || !path) {
    message('업로드 경로와 파일을 선택해주세요.', false);
    return;
  }
  const form = new FormData();
  form.append('path', path);
  form.append('file', file);
  await api('/api/admin/storage/images', { method: 'POST', body: form });
  message('이미지가 업로드되었습니다.', true);
  await loadImages();
}

function bind() {
  $('#loginForm')?.addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
      const auth = await supabaseLogin($('#loginEmail').value.trim(), $('#loginPassword').value);
      localStorage.setItem('mebody.server.accessToken', auth.access_token);
      state.token = auth.access_token;
      await bootstrap();
    } catch (error) {
      message(error.message || '로그인 실패', false);
    }
  });
  document.querySelectorAll('[data-logout]').forEach((button) => button.addEventListener('click', () => {
    localStorage.removeItem('mebody.server.accessToken');
    location.reload();
  }));
  document.querySelectorAll('[data-tab]').forEach((button) => button.addEventListener('click', () => setTab(button.dataset.tab)));
  $('#reloadUsers')?.addEventListener('click', () => Promise.all([loadSummary(), loadUsers()]));
  $('#search')?.addEventListener('keydown', (event) => { if (event.key === 'Enter') loadUsers(); });
  $('#statusFilter')?.addEventListener('change', loadUsers);
  $('#includeDeleted')?.addEventListener('change', loadUsers);
  $('#saveUser')?.addEventListener('click', () => saveUser().catch((error) => message(error.message, false)));
  $('#deleteUser')?.addEventListener('click', () => softDeleteUser().catch((error) => message(error.message, false)));
  $('#loadImages')?.addEventListener('click', () => loadImages().catch((error) => message(error.message, false)));
  $('#uploadImage')?.addEventListener('click', () => uploadImage().catch((error) => message(error.message, false)));
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString('ko-KR');
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char]));
}

function escapeAttr(value) {
  return escapeHtml(value).replace(/`/g, '&#96;');
}

bind();
bootstrap();
