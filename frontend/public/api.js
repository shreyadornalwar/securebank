(function (window) {
  const API_BASE_URL = 'https://securebank-ngv1.onrender.com/api';

  function timedFetch(url, options, timeout) {
    var controller = new AbortController();
    var timer = setTimeout(function() { controller.abort(); }, timeout || 5000);
    options = options || {};
    options.signal = controller.signal;
    return fetch(url, options).then(function(r) { clearTimeout(timer); return r; })
      .catch(function(e) { clearTimeout(timer); throw e; });
  }

  const api = {
    isBackendAvailable: false,

    checkBackend: async function() {
      try {
        const res = await timedFetch(`${API_BASE_URL}/accounts`, { method: 'GET' }, 5000);
        this.isBackendAvailable = res.ok || res.status === 401 || res.status === 403;
        if (this.isBackendAvailable) {
          console.log('Backend connected successfully');
        }
        return this.isBackendAvailable;
      } catch (error) {
        console.warn('Backend check failed:', error.message);
        this.isBackendAvailable = false;
        return false;
      }
    },

    login: async function (credentials) {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000);

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
      // Normalize role to lowercase
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
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
        }, 5000);
        if (res.ok) {
          var data = await res.json();
          localStorage.setItem('bankAccounts', JSON.stringify(data));
          return data;
        }
      } catch (e) {}
      return JSON.parse(localStorage.getItem('bankAccounts') || '[]');
    },

    getTransactions: async function () {
      try {
        var res = await timedFetch(`${API_BASE_URL}/transactions`, {
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
        }, 5000);
        if (res.ok) {
          var data = await res.json();
          var txns = Array.isArray(data) ? data : (data.data || []);
          localStorage.setItem('bankTransactions', JSON.stringify(txns));
          return txns;
        }
      } catch (e) {}
      return JSON.parse(localStorage.getItem('bankTransactions') || '[]');
    },

    getCustomers: async function () {
      try {
        var res = await timedFetch(`${API_BASE_URL}/customers`, {
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
        }, 5000);
        if (res.ok) return await res.json();
      } catch (e) {}
      return [];
    },

    getStaff: async function () {
      try {
        var res = await timedFetch(`${API_BASE_URL}/staff`, {
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
        }, 5000);
        if (res.ok) return await res.json();
      } catch (e) {}
      return [];
    },

    createAccount: async function (owner, type, balance, email) {
      var body = { owner: owner, type: type, balance: balance };
      if (email) body.email = email;
      var res = await timedFetch(`${API_BASE_URL}/accounts`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
        body: JSON.stringify(body)
      }, 5000);
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
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
      }, 5000);
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
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
      }, 5000);
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
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
          body: JSON.stringify({ accountId: accountId, amount: amount })
        }, 10000);

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
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
          body: JSON.stringify({ accountId: accountId, recipientId: recipientId, amount: amount })
        }, 10000);

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
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
          body: JSON.stringify(data)
        }, 5000);
        return res.ok;
      } catch (e) { return false; }
    },

    updateInBackend: async function (endpoint, data) {
      try {
        var res = await timedFetch(`${API_BASE_URL}${endpoint}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
          body: JSON.stringify(data)
        }, 5000);
        return res.ok;
      } catch (e) { return false; }
    },

    deleteFromBackend: async function (endpoint) {
      try {
        var res = await timedFetch(`${API_BASE_URL}${endpoint}`, {
          method: 'DELETE',
          headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
        }, 5000);
        return res.ok;
      } catch (e) { return false; }
    }
  };

  api.checkBackend();
  window.api = api;
})(window);
