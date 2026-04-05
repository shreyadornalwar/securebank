(function (window) {
  // ==================== CONFIGURATION ====================
  const BASE_URL = "https://securebank-backend-rc4i.onrender.com";
  const API_BASE_URL = BASE_URL + "/api";
  // =====================================================

  function timedFetch(url, options, timeout) {
    var controller = new AbortController();
    var timer = setTimeout(function() { controller.abort(); }, timeout || 10000);
    options = options || {};
    options.signal = controller.signal;
    return fetch(url, options).then(function(r) { clearTimeout(timer); return r; })
      .catch(function(e) { clearTimeout(timer); throw e; });
  }

  function getAuthHeaders() {
    var token = localStorage.getItem('token') || '';
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    };
  }

  const api = {
    isBackendAvailable: false,

    checkBackend: async function() {
      try {
        const res = await timedFetch(`${API_BASE_URL}/health`, { method: 'GET' }, 5000);
        this.isBackendAvailable = res.ok || res.status === 401 || res.status === 403;
        if (this.isBackendAvailable) {
          console.log('Backend connected successfully:', BASE_URL);
        }
        return this.isBackendAvailable;
      } catch (error) {
        console.warn('Backend check failed:', error.message);
        this.isBackendAvailable = false;
        return false;
      }
    },

    login: async function (credentials) {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 10000);

        const res = await fetch(`${API_BASE_URL}/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(credentials),
          signal: controller.signal
        });

        clearTimeout(timeoutId);

        if (!res.ok) {
          const data = await res.json().catch(() => null);
          return { success: false, message: data?.message || 'Login failed' };
        }

        const data = await res.json();
        if (data.user && data.user.role) data.user.role = data.user.role.toLowerCase();
        localStorage.setItem('token', data.token);
        localStorage.setItem('currentUser', JSON.stringify(data.user));
        this.isBackendAvailable = true;
        return { success: true, user: data.user };
      } catch (error) {
        console.error('Login error:', error.message);
        return { success: false, message: 'Connection error: ' + error.message };
      }
    },

    logout: function () {
      localStorage.removeItem('token');
      localStorage.removeItem('currentUser');
      window.location.href = 'login.html';
    },

    checkAuth: function () {
      return !!localStorage.getItem('token');
    },

    isAuthenticated: function () {
      return this.checkAuth();
    },

    getCurrentUser: function () {
      try {
        return JSON.parse(localStorage.getItem('currentUser') || 'null');
      } catch (err) {
        return null;
      }
    },

    requireAuth: function (allowedRoles) {
      if (!this.checkAuth()) {
        window.location.href = 'login.html';
        return false;
      }
      var user = this.getCurrentUser();
      if (!user) {
        window.location.href = 'login.html';
        return false;
      }
      if (user.role) user.role = user.role.toLowerCase();
      if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
        var dashUrl = user.role === 'admin' ? 'admin.html' : user.role === 'staff' ? 'staff-dashboard.html' : 'customer-dashboard.html';
        window.location.href = dashUrl;
        return false;
      }
      return true;
    },

    getAccounts: async function () {
      try {
        var res = await timedFetch(`${API_BASE_URL}/accounts`, {
          headers: getAuthHeaders()
        }, 10000);
        if (res.ok) {
          var data = await res.json();
          localStorage.setItem('bankAccounts', JSON.stringify(data));
          return data;
        }
      } catch (e) {
        console.warn('Get accounts error:', e.message);
      }
      return JSON.parse(localStorage.getItem('bankAccounts') || '[]');
    },

    getTransactions: async function () {
      try {
        var res = await timedFetch(`${API_BASE_URL}/transactions`, {
          headers: getAuthHeaders()
        }, 10000);
        if (res.ok) {
          var data = await res.json();
          var txns = Array.isArray(data) ? data : (data.data || []);
          localStorage.setItem('bankTransactions', JSON.stringify(txns));
          return txns;
        }
      } catch (e) {
        console.warn('Get transactions error:', e.message);
      }
      return JSON.parse(localStorage.getItem('bankTransactions') || '[]');
    },

    getCustomers: async function () {
      try {
        var res = await timedFetch(`${API_BASE_URL}/customers`, {
          headers: getAuthHeaders()
        }, 10000);
        if (res.ok) return await res.json();
      } catch (e) {
        console.warn('Get customers error:', e.message);
      }
      return [];
    },

    getStaff: async function () {
      try {
        var res = await timedFetch(`${API_BASE_URL}/staff`, {
          headers: getAuthHeaders()
        }, 10000);
        if (res.ok) return await res.json();
      } catch (e) {
        console.warn('Get staff error:', e.message);
      }
      return [];
    },

    createAccount: async function (owner, type, balance, email) {
      var body = { owner: owner, type: type, balance: balance };
      if (email) body.email = email;
      var res = await timedFetch(`${API_BASE_URL}/accounts`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(body)
      }, 10000);
      if (res.ok) return { success: true };
      var data = await res.json().catch(() => ({}));
      return { success: false, message: data.message || 'Failed to create account' };
    },

    updateAccountBalance: async function (accountId, balance) {
      var intId = typeof accountId === 'string' && accountId.startsWith('ACC')
        ? parseInt(accountId.substring(3))
        : accountId;
      var res = await timedFetch(`${API_BASE_URL}/accounts/${intId}/balance?balance=${balance}`, {
        method: 'PUT',
        headers: getAuthHeaders()
      }, 10000);
      if (res.ok) return { success: true };
      var data = await res.json().catch(() => ({}));
      return { success: false, message: data.message || 'Failed to update balance' };
    },

    deleteAccount: async function (accountId) {
      var intId = typeof accountId === 'string' && accountId.startsWith('ACC')
        ? parseInt(accountId.substring(3))
        : accountId;
      var res = await timedFetch(`${API_BASE_URL}/accounts/${intId}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
      }, 10000);
      if (res.ok) return { success: true };
      var data = await res.json().catch(() => ({}));
      return { success: false, message: data.message || 'Failed to delete account' };
    },

    request: async function (path, options) {
      options = options || {};
      options.method = options.method || 'GET';

      function toIntId(val) {
        if (typeof val === 'string' && val.startsWith('ACC')) {
          return parseInt(val.substring(3), 10);
        }
        return Number(val);
      }

      if (path === '/transactions/deposit' || path === '/transactions/withdraw') {
        var body = typeof options.body === 'string' ? JSON.parse(options.body) : options.body;
        var accountId = toIntId(body.accountId || body.id);
        var amount = Number(body.amount || 0);

        var actualPath = path === '/transactions/deposit' ? '/deposit' : '/withdraw';
        var res = await timedFetch(`${API_BASE_URL}/transactions${actualPath}`, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: JSON.stringify({ accountId: accountId, amount: amount })
        }, 15000);

        if (!res.ok) {
          var json = await res.json().catch(function() { return {}; });
          return { success: false, message: json.message || json.error || 'Failed' };
        }
        return { success: true };
      }

      if (path === '/transactions' && options.method === 'POST') {
        var body = typeof options.body === 'string' ? JSON.parse(options.body) : options.body;
        var accountId = toIntId(body.accountId || body.fromId || body.fromAccountId);
        var recipientId = toIntId(body.recipientId || body.toId);
        var amount = Number(body.amount);

        var res = await timedFetch(`${API_BASE_URL}/transactions`, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: JSON.stringify({ accountId: accountId, recipientId: recipientId, amount: amount })
        }, 15000);

        if (!res.ok) {
          var json = await res.json().catch(function() { return {}; });
          return { success: false, message: json.message || json.error || 'Transfer failed' };
        }
        return { success: true };
      }

      if (path === '/transactions' && options.method === 'GET') {
        return { success: true, data: await this.getTransactions() };
      }

      return { success: false, message: 'Unsupported API action' };
    },

    saveToBackend: async function (endpoint, data) {
      try {
        var res = await timedFetch(`${API_BASE_URL}${endpoint}`, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: JSON.stringify(data)
        }, 10000);
        return res.ok;
      } catch (e) { 
        console.warn('Save error:', e.message);
        return false; 
      }
    },

    updateInBackend: async function (endpoint, data) {
      try {
        var res = await timedFetch(`${API_BASE_URL}${endpoint}`, {
          method: 'PUT',
          headers: getAuthHeaders(),
          body: JSON.stringify(data)
        }, 10000);
        return res.ok;
      } catch (e) { 
        console.warn('Update error:', e.message);
        return false; 
      }
    },

    deleteFromBackend: async function (endpoint) {
      try {
        var res = await timedFetch(`${API_BASE_URL}${endpoint}`, {
          method: 'DELETE',
          headers: getAuthHeaders()
        }, 10000);
        return res.ok;
      } catch (e) { 
        console.warn('Delete error:', e.message);
        return false; 
      }
    }
  };

  var secureBankWS = {
    isConnected: false,
    eventSource: null,
    reconnectAttempts: 0,
    maxReconnectAttempts: 5,
    reconnectTimer: null,
    connectionFailed: false,

    onConnect: null,
    onDisconnect: null,
    onUpdate: null,

    connect: function() {
      if (this.connectionFailed) {
        console.log('[Realtime] Connection previously failed, not retrying');
        return;
      }
      if (this.isConnected && this.eventSource) return;
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        this.connectionFailed = true;
        console.warn('[Realtime] Backend unavailable, real-time updates disabled');
        return;
      }

      var self = this;
      var url = BASE_URL + '/api/events';

      try {
        this.eventSource = new EventSource(url);

        this.eventSource.onopen = function() {
          self.isConnected = true;
          self.reconnectAttempts = 0;
          self.connectionFailed = false;
          console.log('[Realtime] Connected to:', BASE_URL);
          if (typeof self.onConnect === 'function') self.onConnect();
        };

        this.eventSource.addEventListener('connected', function() {
          console.log('[Realtime] Handshake OK');
        });

        this.eventSource.addEventListener('account_created', function(e) {
          var data = JSON.parse(e.data);
          if (typeof self.onUpdate === 'function') self.onUpdate('account_created', data);
        });

        this.eventSource.addEventListener('transaction', function(e) {
          var data = JSON.parse(e.data);
          if (typeof self.onUpdate === 'function') self.onUpdate('transaction', data);
        });

        this.eventSource.onerror = function() {
          self.isConnected = false;
          if (self.eventSource) self.eventSource.close();
          self.eventSource = null;
          if (typeof self.onDisconnect === 'function') self.onDisconnect();
          self.scheduleReconnect();
        };
      } catch (e) {
        console.warn('[Realtime] Failed to create EventSource:', e.message);
        this.connectionFailed = true;
      }
    },

    scheduleReconnect: function() {
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        this.connectionFailed = true;
        console.warn('[Realtime] Max reconnect attempts reached, real-time disabled');
        return;
      }
      var delay = 3000;
      this.reconnectAttempts++;
      var self = this;
      console.log('[Realtime] Reconnecting in ' + delay + 'ms (attempt ' + this.reconnectAttempts + '/' + this.maxReconnectAttempts + ')');
      this.reconnectTimer = setTimeout(function() { self.connect(); }, delay);
    },

    disconnect: function() {
      clearTimeout(this.reconnectTimer);
      if (this.eventSource) {
        this.eventSource.close();
        this.eventSource = null;
      }
      this.isConnected = false;
      this.connectionFailed = false;
      this.reconnectAttempts = 0;
      if (typeof this.onDisconnect === 'function') this.onDisconnect();
    }
  };

  api.checkBackend();
  window.api = api;
  window.secureBankWS = secureBankWS;
})(window);