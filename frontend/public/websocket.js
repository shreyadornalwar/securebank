(function(window) {
  var secureBankWS = {
    isConnected: false,
    eventSource: null,
    reconnectAttempts: 0,
    maxReconnectAttempts: 3,
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
      var url = 'https://securebank-backend-rc4i.onrender.com/api/events';

      try {
        this.eventSource = new EventSource(url);

        this.eventSource.onopen = function() {
          self.isConnected = true;
          self.reconnectAttempts = 0;
          self.connectionFailed = false;
          console.log('[Realtime] Connected');
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

  window.secureBankWS = secureBankWS;
})(window);
