const API = '/api';
let token = localStorage.getItem('se_token') || null;
let currentUser = JSON.parse(localStorage.getItem('se_user') || 'null');
let bookingTeacherId = null;
let reviewSessionId = null;

function esc(s){const d=document.createElement('div');d.textContent=s??'';return d.innerHTML;}
function split(t){return t.split(',').map(s=>s.trim()).filter(Boolean);}

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = token;
  const res = await fetch(API + path, { ...options, headers });
  const data = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) throw new Error((data && data.error) || 'Something went wrong');
  return data;
}

function setSession(t, user) {
  token = t; currentUser = user;
  localStorage.setItem('se_token', t);
  localStorage.setItem('se_user', JSON.stringify(user));
}
function clearSession() {
  token = null; currentUser = null;
  localStorage.removeItem('se_token'); localStorage.removeItem('se_user');
}

function showApp() {
  document.getElementById('screen-auth').classList.remove('active');
  document.getElementById('tabbar').style.display = 'flex';
  document.getElementById('logoutBtn').style.display = 'inline-block';
  switchTab('members');
  loadUsers();
}
function showAuth() {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById('screen-auth').classList.add('active');
  document.getElementById('tabbar').style.display = 'none';
  document.getElementById('logoutBtn').style.display = 'none';
  document.getElementById('appbarTitle').textContent = 'Skill Exchange';
  document.getElementById('appbarSub').textContent = "Teach what you know, learn what you don't";
}

function switchTab(name) {
  document.querySelectorAll('.tab').forEach(t => t.classList.toggle('active', t.dataset.screen === name));
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById('screen-' + name).classList.add('active');
  const tab = [...document.querySelectorAll('.tab')].find(t => t.dataset.screen === name);
  document.getElementById('appbarTitle').textContent = tab.dataset.title;
  document.getElementById('appbarSub').textContent = tab.dataset.sub;
  if (name === 'sessions') loadSessions();
  if (name === 'profile') loadProfile();
}

// ---------- AUTH ----------
document.querySelectorAll('.auth-tab').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
    tab.classList.add('active');
    document.getElementById(tab.dataset.form + 'Form').classList.add('active');
    document.getElementById('authMsg').textContent = '';
  });
});

document.getElementById('loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  const msg = document.getElementById('authMsg');
  try {
    const data = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        email: document.getElementById('loginEmail').value,
        password: document.getElementById('loginPassword').value
      })
    });
    setSession(data.token, data.user);
    showApp();
  } catch (err) {
    msg.style.color = '#dc2626';
    msg.textContent = '❌ ' + err.message;
  }
});

document.getElementById('registerForm').addEventListener('submit', async e => {
  e.preventDefault();
  const msg = document.getElementById('authMsg');
  try {
    const data = await api('/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        name: document.getElementById('regName').value,
        email: document.getElementById('regEmail').value,
        password: document.getElementById('regPassword').value,
        location: document.getElementById('regLocation').value,
        skillsOffered: split(document.getElementById('regSkillsOffered').value),
        skillsWanted: split(document.getElementById('regSkillsWanted').value)
      })
    });
    setSession(data.token, data.user);
    showApp();
  } catch (err) {
    msg.style.color = '#dc2626';
    msg.textContent = '❌ ' + err.message;
  }
});

document.getElementById('logoutBtn').addEventListener('click', async () => {
  try { await api('/auth/logout', { method: 'POST' }); } catch (e) {}
  clearSession();
  showAuth();
});

// ---------- MEMBERS ----------
async function loadUsers() {
  const el = document.getElementById('userList');
  el.innerHTML = 'Loading...';
  try {
    const users = await api('/users');
    if (users.length === 0) { el.innerHTML = '<div class="empty"><div class="big">👥</div>No members yet.</div>'; return; }
    el.innerHTML = users.map(u => `
      <div class="user-item">
        <strong>${esc(u.name)}</strong> ${u.location ? '· ' + esc(u.location) : ''}<br>
        Teaches: ${(u.skillsOffered||[]).map(s=>`<span class="tag">${esc(s)}</span>`).join('')||'—'}<br>
        Wants: ${(u.skillsWanted||[]).map(s=>`<span class="tag want">${esc(s)}</span>`).join('')||'—'}
        ${currentUser && u.id !== currentUser.id ? `<div style="margin-top:8px"><button class="btn small" onclick="openBookModal(${u.id}, '${esc(u.name)}')">📅 Book Session</button></div>` : ''}
      </div>`).join('');
  } catch (err) { el.innerHTML = '<p>Could not load members.</p>'; }
}
document.getElementById('refreshBtn').addEventListener('click', loadUsers);

// ---------- MATCHES ----------
document.getElementById('matchBtn').addEventListener('click', async () => {
  const el = document.getElementById('matchResults');
  el.innerHTML = 'Searching...';
  try {
    const matches = await api(`/users/${currentUser.id}/matches`);
    if (matches.length === 0) { el.innerHTML = '<div class="empty"><div class="big">🔍</div>No matches found yet.</div>'; return; }
    el.innerHTML = matches.map(m => `
      <div class="match-item ${m.mutualMatch ? 'mutual' : ''}">
        <strong>${m.mutualMatch ? '🤝 ' : ''}${esc(m.user.name)}</strong><br>
        ${m.theyCanTeachYou.length ? 'They teach you: ' + m.theyCanTeachYou.map(s=>`<span class="tag">${esc(s)}</span>`).join('') + '<br>' : ''}
        ${m.youCanTeachThem.length ? 'You teach them: ' + m.youCanTeachThem.map(s=>`<span class="tag want">${esc(s)}</span>`).join('') : ''}
        <div style="margin-top:8px"><button class="btn small" onclick="openBookModal(${m.user.id}, '${esc(m.user.name)}')">📅 Book Session</button></div>
      </div>`).join('');
  } catch (err) { el.innerHTML = `<p>❌ ${esc(err.message)}</p>`; }
});

// ---------- BOOK SESSION MODAL ----------
function openBookModal(teacherId, name) {
  bookingTeacherId = teacherId;
  document.getElementById('bookWith').textContent = `With ${name}`;
  document.getElementById('bookSkill').value = '';
  document.getElementById('bookTime').value = '';
  document.getElementById('bookNotes').value = '';
  document.getElementById('bookModal').classList.add('active');
}
document.getElementById('bookCancel').addEventListener('click', () => document.getElementById('bookModal').classList.remove('active'));
document.getElementById('bookConfirm').addEventListener('click', async () => {
  const skill = document.getElementById('bookSkill').value.trim();
  const time = document.getElementById('bookTime').value;
  if (!skill || !time) return;
  try {
    await api('/sessions', {
      method: 'POST',
      body: JSON.stringify({ teacherId: bookingTeacherId, skill, scheduledAt: time, notes: document.getElementById('bookNotes').value.trim() })
    });
    document.getElementById('bookModal').classList.remove('active');
    switchTab('sessions');
  } catch (err) { alert('❌ ' + err.message); }
});

// ---------- SESSIONS ----------
async function loadSessions() {
  const el = document.getElementById('sessionList');
  el.innerHTML = 'Loading...';
  try {
    const sessions = await api(`/sessions/user/${currentUser.id}`);
    if (sessions.length === 0) { el.innerHTML = '<div class="empty"><div class="big">📅</div>No sessions booked yet.</div>'; return; }
    el.innerHTML = sessions.map(s => {
      const isTeacher = s.teacher.id === currentUser.id;
      const other = isTeacher ? s.learner : s.teacher;
      const role = isTeacher ? 'Teaching' : 'Learning';
      const when = new Date(s.scheduledAt).toLocaleString();
      let actions = '';
      if (s.status === 'PENDING' && isTeacher) {
        actions += `<button class="btn small success" onclick="updateStatus(${s.id},'CONFIRMED')">Confirm</button>
                    <button class="btn small danger" onclick="updateStatus(${s.id},'CANCELLED')">Decline</button>`;
      } else if (s.status === 'CONFIRMED') {
        actions += `<button class="btn small success" onclick="updateStatus(${s.id},'COMPLETED')">Mark Completed</button>
                    <button class="btn small danger" onclick="updateStatus(${s.id},'CANCELLED')">Cancel</button>`;
      } else if (s.status === 'PENDING' && !isTeacher) {
        actions += `<button class="btn small danger" onclick="updateStatus(${s.id},'CANCELLED')">Cancel Request</button>`;
      } else if (s.status === 'COMPLETED') {
        actions += `<button class="btn small" onclick="openReviewModal(${s.id}, '${esc(other.name)}')">⭐ Leave Review</button>`;
      }
      return `
        <div class="session-item">
          <strong>${role}: ${esc(s.skill)}</strong> <span class="tag status ${s.status}">${s.status}</span><br>
          With ${esc(other.name)} · ${when}
          ${s.notes ? `<br>🔗 ${esc(s.notes)}` : ''}
          <div class="session-actions">${actions}</div>
        </div>`;
    }).join('');
  } catch (err) { el.innerHTML = `<p>❌ ${esc(err.message)}</p>`; }
}

async function updateStatus(sessionId, status) {
  try {
    await api(`/sessions/${sessionId}/status`, { method: 'PUT', body: JSON.stringify({ status }) });
    loadSessions();
  } catch (err) { alert('❌ ' + err.message); }
}

// ---------- REVIEWS ----------
function openReviewModal(sessionId, name) {
  reviewSessionId = sessionId;
  document.getElementById('reviewFor').textContent = `For your session with ${name}`;
  document.getElementById('reviewRating').value = 5;
  document.getElementById('reviewComment').value = '';
  document.getElementById('reviewModal').classList.add('active');
}
document.getElementById('reviewCancel').addEventListener('click', () => document.getElementById('reviewModal').classList.remove('active'));
document.getElementById('reviewConfirm').addEventListener('click', async () => {
  try {
    await api('/reviews', {
      method: 'POST',
      body: JSON.stringify({
        sessionId: reviewSessionId,
        rating: Number(document.getElementById('reviewRating').value),
        comment: document.getElementById('reviewComment').value
      })
    });
    document.getElementById('reviewModal').classList.remove('active');
    loadSessions();
  } catch (err) { alert('❌ ' + err.message); }
});

// ---------- PROFILE ----------
async function loadProfile() {
  const el = document.getElementById('profileCard');
  el.innerHTML = 'Loading...';
  try {
    const [user, rating, reviews] = await Promise.all([
      api(`/users/${currentUser.id}`),
      api(`/users/${currentUser.id}/rating`),
      api(`/reviews/user/${currentUser.id}`)
    ]);
    el.innerHTML = `
      <h2>${esc(user.name)}</h2>
      <p style="color:var(--muted);font-size:13px;margin-top:-8px">${esc(user.email)} ${user.location ? '· ' + esc(user.location) : ''}</p>
      <p><span class="stars">${'★'.repeat(Math.round(rating.average))}${'☆'.repeat(5-Math.round(rating.average))}</span>
         ${rating.average} (${rating.count} review${rating.count===1?'':'s'})</p>
      <p><strong>Teaches:</strong> ${(user.skillsOffered||[]).map(s=>`<span class="tag">${esc(s)}</span>`).join('')||'—'}</p>
      <p><strong>Wants to learn:</strong> ${(user.skillsWanted||[]).map(s=>`<span class="tag want">${esc(s)}</span>`).join('')||'—'}</p>
      <h2 style="margin-top:16px">Reviews Received</h2>
      ${reviews.length ? reviews.map(r => `
        <div class="user-item">
          <span class="stars">${'★'.repeat(r.rating)}${'☆'.repeat(5-r.rating)}</span> from ${esc(r.reviewer.name)}<br>
          ${r.comment ? esc(r.comment) : '<em>No comment</em>'}
        </div>`).join('') : '<p style="color:var(--muted);font-size:13px">No reviews yet.</p>'}
    `;
  } catch (err) { el.innerHTML = `<p>❌ ${esc(err.message)}</p>`; }
}

// ---------- TAB NAV ----------
document.querySelectorAll('.tab').forEach(tab => {
  tab.addEventListener('click', () => switchTab(tab.dataset.screen));
});

// ---------- INIT ----------
if (token && currentUser) {
  showApp();
} else {
  showAuth();
}
