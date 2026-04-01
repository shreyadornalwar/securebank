(function(window) {
  var secureBankWS = {
    isConnected: false,
    eventSource: null,
    reconnectAttempts: 0,
    maxReconnectAttempts: 10,
    reconnectTimer: null,

    onConnect: null,
    onDisconnect: null,
    onUpdate: null,

    connect: function() {
      if (this.isConnected && this.eventSource) return;
      var self = this;
      var url = window.location.origin + '/api/events';

      this.eventSource = new EventSource(url);

      this.eventSource.onopen = function() {
        self.isConnected = true;
        self.reconnectAttempts = 0;
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
        self.eventSource.close();
        if (typeof self.onDisconnect === 'function') self.onDisconnect();
        self.scheduleReconnect();
      };
    },

    scheduleReconnect: function() {
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
        console.warn('[Realtime] Max reconnect attempts reached');
        return;
      }
      var delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
      this.reconnectAttempts++;
      var self = this;
      console.log('[Realtime] Reconnecting in ' + delay + 'ms (attempt ' + this.reconnectAttempts + ')');
      this.reconnectTimer = setTimeout(function() { self.connect(); }, delay);
    },

    disconnect: function() {
      clearTimeout(this.reconnectTimer);
      if (this.eventSource) {
        this.eventSource.close();
        this.eventSource = null;
      }
      this.isConnected = false;
      if (typeof this.onDisconnect === 'function') this.onDisconnect();
    }
  };

  window.secureBankWS = secureBankWS;
})(window);
