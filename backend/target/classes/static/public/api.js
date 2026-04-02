(function (window) {
  const API_BASE_URL = window.location.origin + '/api';

  const DEMO_USERS = [
    { email: 'admin@securebank.com', password: 'admin123', role: 'admin', name: 'Admin User', id: 1 },
    { email: 'staff@securebank.com', password: 'staff123', role: 'staff', name: 'Staff User', id: 2 },
    { email: 'customer@securebank.com', password: 'customer123', role: 'customer', name: 'Customer User', id: 3 }
  ];

  const api = {
    // Check if backend is available
    isBackendAvailable: false,

    // Check backend availability
    checkBackend: async function() {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 5000); // Increased timeout to 5 seconds
        const res = await fetch(`${API_BASE_URL}/accounts`, {
          method: 'GET',
          signal: controller.signal
        });
        clearTimeout(timeoutId);
        // Consider backend available if we get any response (200, 401, 403, etc.)
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
      try {
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
        localStorage.setItem('token', data.token);
        localStorage.setItem('currentUser', JSON.stringify(data.user));
        this.isBackendAvailable = true;
        return { success: true, user: data.user };
      } catch (error) {
        console.error('Backend unavailable, using demo mode');
        const user = DEMO_USERS.find(u =>
          u.email === credentials.email && u.password === credentials.password
        );
        if (user) {
          const userData = { id: user.id, email: user.email, role: user.role, name: user.name };
          localStorage.setItem('token', 'demo-token-' + user.id);
          localStorage.setItem('currentUser', JSON.stringify(userData));
          this.isBackendAvailable = false;
          return { success: true, user: userData };
        }
        return { success: false, message: 'Invalid email or password.' };
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
      if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
        var dashUrl = user.role === 'admin' ? 'admin.html' : user.role === 'staff' ? 'staff-dashboard.html' : 'customer-dashboard.html';
        window.location.href = dashUrl;
        return false;
      }
      return true;
    },

    getAccounts: async function () {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var res = await fetch(`${API_BASE_URL}/accounts`, {
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
          });
          if (res.ok) {
            var data = await res.json();
            // Store in localStorage as cache
            localStorage.setItem('bankAccounts', JSON.stringify(data));
            return data;
          }
        } catch (error) {
          console.warn('Failed to fetch accounts from backend');
        }
      }
      
      // Return cached data if backend unavailable
      return JSON.parse(localStorage.getItem('bankAccounts') || '[]');
    },

    getTransactions: async function () {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var res = await fetch(`${API_BASE_URL}/transactions`, {
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
          });
          if (res.ok) {
            var data = await res.json();
            var transactions = Array.isArray(data) ? data : (data.data || []);
            // Store in localStorage as cache
            localStorage.setItem('bankTransactions', JSON.stringify(transactions));
            return transactions;
          }
        } catch (error) {
          console.warn('Failed to fetch transactions from backend');
        }
      }
      
      // Return cached data if backend unavailable
      return JSON.parse(localStorage.getItem('bankTransactions') || '[]');
    },

    // Get customers list (for admin)
    getCustomers: async function () {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var res = await fetch(`${API_BASE_URL}/customers`, {
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
          });
          if (res.ok) {
            return await res.json();
          }
        } catch (error) {
          console.warn('Failed to fetch customers from backend');
        }
      }
      
      return [];
    },

    // Get staff list
    getStaff: async function () {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var res = await fetch(`${API_BASE_URL}/staff`, {
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
          });
          if (res.ok) {
            return await res.json();
          }
        } catch (error) {
          console.warn('Failed to fetch staff from backend');
        }
      }
      
      return [];
    },

    // Create account
    createAccount: async function (owner, type, balance) {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var res = await fetch(`${API_BASE_URL}/accounts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
            body: JSON.stringify({ owner: owner, type: type, balance: balance })
          });
          if (res.ok) {
            return { success: true };
          } else {
            var data = await res.json().catch(() => ({}));
            return { success: false, message: data.message || 'Failed to create account' };
          }
        } catch (error) {
          console.error('Failed to create account:', error);
          return { success: false, message: 'Backend unavailable' };
        }
      }
      
      return { success: false, message: 'Backend unavailable' };
    },

    // Update account balance
    updateAccountBalance: async function (accountId, balance) {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var intId = typeof accountId === 'string' && accountId.startsWith('ACC') 
            ? parseInt(accountId.substring(3)) 
            : accountId;
          var res = await fetch(`${API_BASE_URL}/accounts/${intId}/balance?balance=${balance}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
          });
          if (res.ok) {
            return { success: true };
          } else {
            var data = await res.json().catch(() => ({}));
            return { success: false, message: data.message || 'Failed to update balance' };
          }
        } catch (error) {
          console.error('Failed to update balance:', error);
          return { success: false, message: 'Backend unavailable' };
        }
      }
      
      return { success: false, message: 'Backend unavailable' };
    },

    // Delete account
    deleteAccount: async function (accountId) {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var intId = typeof accountId === 'string' && accountId.startsWith('ACC') 
            ? parseInt(accountId.substring(3)) 
            : accountId;
          var res = await fetch(`${API_BASE_URL}/accounts/${intId}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
          });
          if (res.ok) {
            return { success: true };
          } else {
            var data = await res.json().catch(() => ({}));
            return { success: false, message: data.message || 'Failed to delete account' };
          }
        } catch (error) {
          console.error('Failed to delete account:', error);
          return { success: false, message: 'Backend unavailable' };
        }
      }
      
      return { success: false, message: 'Backend unavailable' };
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

      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }

      try {
        if (path === '/transactions/deposit' || path === '/transactions/withdraw') {
          var body = typeof options.body === 'string' ? JSON.parse(options.body) : options.body;
          var accountId = toIntId(body.accountId || body.id);
          var amount = Number(body.amount || 0);

          var actualPath = path === '/transactions/deposit' ? '/deposit' : '/withdraw';
          var res = await fetch(`${API_BASE_URL}/transactions${actualPath}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
            body: JSON.stringify({ accountId: accountId, amount: amount })
          });

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

          var res = await fetch(`${API_BASE_URL}/transactions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
            body: JSON.stringify({ accountId: accountId, recipientId: recipientId, amount: amount })
          });

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
      } catch (error) {
        console.error('api.request error:', error);
        return { success: false, message: error.message || 'Request failed' };
      }
    },

    // Save data to backend (for any entity)
    saveToBackend: async function (endpoint, data) {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var res = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
            body: JSON.stringify(data)
          });
          return res.ok;
        } catch (error) {
          console.error('Failed to save to backend:', error);
          return false;
        }
      }
      return false;
    },

    // Update data in backend
    updateInBackend: async function (endpoint, data) {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var res = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
            body: JSON.stringify(data)
          });
          return res.ok;
        } catch (error) {
          console.error('Failed to update in backend:', error);
          return false;
        }
      }
      return false;
    },

    // Delete from backend
    deleteFromBackend: async function (endpoint) {
      if (!this.isBackendAvailable) {
        await this.checkBackend();
      }
      
      if (this.isBackendAvailable) {
        try {
          var res = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }
          });
          return res.ok;
        } catch (error) {
          console.error('Failed to delete from backend:', error);
          return false;
        }
      }
      return false;
    }
  };

  // Initialize backend availability check
  api.checkBackend();

  window.api = api;
})(window);