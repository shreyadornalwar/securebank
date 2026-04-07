(function () {
  var STORAGE_KEY = 'secureBankChatHistory';
  var TYPING_DELAY = 800;
  var BOT_NAME = 'SecureBot';
  var isOpen = false;

  // Detect current user role from the page
  function detectRole() {
    var path = window.location.pathname;
    if (path.includes('admin')) return 'admin';
    if (path.includes('staff')) return 'staff';
    return 'customer';
  }

  function getQuickRepliesForRole() {
    var role = detectRole();
    if (role === 'admin') {
      return [
        { label: 'Dashboard Stats', keyword: 'stats' },
        { label: 'Pending Loans', keyword: 'pending loans' },
        { label: 'Reports', keyword: 'reports' },
        { label: 'Help', keyword: 'help' }
      ];
    } else if (role === 'staff') {
      return [
        { label: 'Customer List', keyword: 'customers' },
        { label: 'Accounts', keyword: 'accounts' },
        { label: 'Pending Tasks', keyword: 'pending' },
        { label: 'Help', keyword: 'help' }
      ];
    }
    return [
      { label: 'Check Balance', keyword: 'balance' },
      { label: 'Transfer Money', keyword: 'transfer' },
      { label: 'Loan Options', keyword: 'loans' },
      { label: 'Help', keyword: 'help' }
    ];
  }

  var QUICK_REPLIES = getQuickRepliesForRole();

  function getRealData() {
    var accounts = JSON.parse(localStorage.getItem('bankAccounts') || '[]');
    var transactions = JSON.parse(localStorage.getItem('bankTransactions') || '[]');
    var cards = JSON.parse(localStorage.getItem('bankCards') || '[]');
    var beneficiaries = JSON.parse(localStorage.getItem('bankBeneficiaries') || '[]');
    var loans = JSON.parse(localStorage.getItem('bankLoans') || '[]');
    var customers = JSON.parse(localStorage.getItem('bankCustomers') || '[]');
    var staff = JSON.parse(localStorage.getItem('bankStaff') || '[]');
    return { accounts: accounts, transactions: transactions, cards: cards, beneficiaries: beneficiaries, loans: loans, customers: customers, staff: staff };
  }

  function formatAmount(n) {
    return '\u20B9' + (Number(n) || 0).toLocaleString('en-IN');
  }

  // Admin-specific responses
  var ADMIN_RESPONSES = [
    {
      keywords: ['stats', 'dashboard', 'statistics', 'overview', 'summary'],
      getResponse: function() {
        var data = getRealData();
        var totalCustomers = data.customers.length;
        var totalAccounts = data.accounts.length;
        var totalTransactions = data.transactions.length;
        var totalBalance = data.accounts.reduce(function(s, a) { return s + (Number(a.balance) || 0); }, 0);
        var pendingLoans = data.loans.filter(function(l) { return l.status === 'Pending'; }).length;
        return '**Dashboard Statistics:**\n\n' +
          '\u2022 Total Customers: **' + totalCustomers + '**\n' +
          '\u2022 Total Accounts: **' + totalAccounts + '**\n' +
          '\u2022 Total Balance: **' + formatAmount(totalBalance) + '**\n' +
          '\u2022 Total Transactions: **' + totalTransactions + '**\n' +
          '\u2022 Pending Loans: **' + pendingLoans + '**';
      },
      actions: []
    },
    {
      keywords: ['pending loans', 'loan approvals', 'approve loans'],
      getResponse: function() {
        var data = getRealData();
        var pending = data.loans.filter(function(l) { return l.status === 'Pending'; });
        if (pending.length === 0) return 'No pending loan applications. All loans have been processed.';
        var list = pending.map(function(l) {
          return '\u2022 **' + (l.id || 'Loan') + '** - ' + (l.customer || 'Customer') + ': ' + formatAmount(l.amount) + ' (' + l.type + ')';
        }).join('\n');
        return '**Pending Loan Applications (' + pending.length + '):**\n\n' + list + '\n\nReview and approve/reject these from the Loan Approvals section.';
      },
      actions: [{ label: 'Go to Loans', page: 'admin.html' }]
    },
    {
      keywords: ['reports', 'analytics', 'charts', 'graphs'],
      getResponse: function() {
        var data = getRealData();
        var currentMonth = new Date().toISOString().slice(0, 7);
        var monthlyDeposits = data.transactions.filter(function(t) { return t.type === 'DEPOSIT' && t.date && t.date.startsWith(currentMonth); }).reduce(function(s, t) { return s + t.amount; }, 0);
        var monthlyWithdrawals = data.transactions.filter(function(t) { return t.type === 'WITHDRAWAL' && t.date && t.date.startsWith(currentMonth); }).reduce(function(s, t) { return s + t.amount; }, 0);
        return '**Monthly Report Summary:**\n\n' +
          '\u2022 Deposits this month: **' + formatAmount(monthlyDeposits) + '**\n' +
          '\u2022 Withdrawals this month: **' + formatAmount(monthlyWithdrawals) + '**\n' +
          '\u2022 Net Flow: **' + formatAmount(monthlyDeposits - monthlyWithdrawals) + '**\n\n' +
          'View detailed charts and analytics in the Reports section.';
      },
      actions: [{ label: 'View Reports', page: 'admin.html' }]
    },
    {
      keywords: ['customers', 'customer list', 'all customers'],
      getResponse: function() {
        var data = getRealData();
        if (data.customers.length === 0) return 'No customers found in the system.';
        var active = data.customers.filter(function(c) { return c.status === 'ACTIVE'; }).length;
        var inactive = data.customers.length - active;
        return '**Customer Overview:**\n\n' +
          '\u2022 Total Customers: **' + data.customers.length + '**\n' +
          '\u2022 Active: **' + active + '**\n' +
          '\u2022 Inactive/Suspended: **' + inactive + '**\n\n' +
          'Manage customers from the Customer Management section.';
      },
      actions: [{ label: 'Manage Customers', page: 'admin.html' }]
    },
    {
      keywords: ['transactions', 'all transactions', 'transaction history'],
      getResponse: function() {
        var data = getRealData();
        if (data.transactions.length === 0) return 'No transactions found.';
        var deposits = data.transactions.filter(function(t) { return t.type === 'DEPOSIT'; }).length;
        var withdrawals = data.transactions.filter(function(t) { return t.type === 'WITHDRAWAL'; }).length;
        var recent = data.transactions.slice(0, 3);
        return '**Transaction Summary:**\n\n' +
          '\u2022 Total Transactions: **' + data.transactions.length + '**\n' +
          '\u2022 Deposits: **' + deposits + '**\n' +
          '\u2022 Withdrawals: **' + withdrawals + '**\n\n' +
          'Most recent: ' + recent.map(function(t) { return t.type + ' ' + formatAmount(t.amount); }).join(', ');
      },
      actions: [{ label: 'View All Transactions', page: 'admin.html' }]
    }
  ];

  var RESPONSES = [
    {
      keywords: ['balance', 'account', 'money', 'funds', 'how much'],
      getResponse: function() {
        var data = getRealData();
        var role = detectRole();
        
        // Admin view - show total bank balance
        if (role === 'admin') {
          var totalBalance = data.accounts.reduce(function(s, a) { return s + (Number(a.balance) || 0); }, 0);
          var totalDeposits = data.transactions.filter(function(t) { return t.type === 'DEPOSIT'; }).reduce(function(s, t) { return s + t.amount; }, 0);
          var totalWithdrawals = data.transactions.filter(function(t) { return t.type === 'WITHDRAWAL'; }).reduce(function(s, t) { return s + t.amount; }, 0);
          return '**Bank Overview:**\n\n' +
            '\u2022 Total Customer Balances: **' + formatAmount(totalBalance) + '**\n' +
            '\u2022 Total Deposits: **' + formatAmount(totalDeposits) + '**\n' +
            '\u2022 Total Withdrawals: **' + formatAmount(totalWithdrawals) + '**';
        }
        
        // Customer view
        if (data.accounts.length === 0) return 'No account data found. Please make a deposit or contact support to set up your account.';
        var lines = data.accounts.map(function(a) { return '\u20B9**' + (a.id || 'Account') + '**: ' + formatAmount(a.balance); }).join('\n');
        var total = data.accounts.reduce(function(s, a) { return s + (Number(a.balance) || 0); }, 0);
        return 'Your current account balances:\n\n' + lines + '\n\n**Total Balance**: ' + formatAmount(total);
      },
      actions: [{ label: 'View Accounts', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['transfer', 'send money', 'send', 'pay', 'payment', 'wire'],
      response: 'I can help you transfer funds! You can send money to saved beneficiaries or new recipients.\n\nTransfer options:\n\u2022 Between your accounts\n\u2022 To saved beneficiaries\n\u2022 To new recipients\n\nTransfers are processed instantly within SecureBank.',
      actions: [{ label: 'Go to Transfers', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['loan', 'loans', 'borrow', 'credit', 'emi', 'interest rate', 'home loan', 'personal loan'],
      getResponse: function() {
        var data = getRealData();
        if (data.loans.length > 0) {
          var active = data.loans.filter(function(l) { return l.status === 'Active'; });
          return 'You have ' + active.length + ' active loan(s).\n\nView your loans or apply for a new one from the dashboard.';
        }
        return 'We offer several loan options:\n\nHome Loan \u2014 from 8.5% p.a.\nPersonal Loan \u2014 from 10.5% p.a.\nCar Loan \u2014 from 9.0% p.a.\nEducation Loan \u2014 from 7.5% p.a.\nBusiness Loan \u2014 from 11.0% p.a.\n\nYou can calculate your EMI and apply online.';
      },
      actions: [{ label: 'Apply for Loan', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['help', 'support', 'assist', 'problem', 'issue', 'contact', 'customer service'],
      getResponse: function() {
        var role = detectRole();
        if (role === 'admin') {
          return 'I\'m here to help! As an admin, I can assist with:\n\n\u2022 **Dashboard stats** \u2014 System overview\n\u2022 **Pending loans** \u2014 Loan applications\n\u2022 **Reports** \u2014 Analytics & charts\n\u2022 **Customers** \u2014 Customer management\n\u2022 **Transactions** \u2014 Transaction summaries\n\nJust type what you need or tap a quick reply below.';
        }
        return 'I\'m here to help! Here\'s what I can assist with:\n\n\u2022 Balance \u2014 Check your account balances\n\u2022 Transfer \u2014 Send money to others\n\u2022 Loans \u2014 View and apply for loans\n\u2022 Cards \u2014 Manage your debit/credit cards\n\u2022 Transactions \u2014 View transaction history\n\u2022 Profile \u2014 Update your account settings\n\nJust type what you need or tap a quick reply below.';
      },
      actions: []
    },
    {
      keywords: ['card', 'cards', 'debit', 'credit card', 'virtual card'],
      getResponse: function() {
        var data = getRealData();
        if (data.cards.length === 0) return 'You don\'t have any cards yet. Apply for a debit or credit card from the dashboard.';
        return 'Your cards:\n\n' + data.cards.map(function(c) {
          var last4 = (c.number || '').slice(-4);
          return (c.type === 'Credit Card' ? 'Credit' : c.type === 'Debit Card' ? 'Debit' : c.type) + ' Card ending in ' + last4 + ' (' + c.status + ')';
        }).join('\n');
      },
      actions: [{ label: 'Manage Cards', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['transaction', 'transactions', 'history', 'statement', 'recent', 'activity'],
      getResponse: function() {
        var data = getRealData();
        if (data.transactions.length === 0) return 'No transactions found. Make a deposit or transfer to see your transaction history.';
        var recent = data.transactions.slice(0, 4);
        return 'Your recent transactions:\n\n' + recent.map(function(t) {
          var prefix = t.type === 'DEPOSIT' ? '+' : '-';
          return (t.description || t.type) + ' ' + prefix + formatAmount(t.amount);
        }).join('\n') + '\n\nFor the complete history, visit your transactions page.';
      },
      actions: [{ label: 'View Transactions', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['beneficiary', 'beneficiaries', 'recipient'],
      getResponse: function() {
        var data = getRealData();
        if (data.beneficiaries.length === 0) return 'No saved beneficiaries yet. Add beneficiaries from the dashboard to send money quickly.';
        return 'Your saved beneficiaries:\n\n' + data.beneficiaries.map(function(b) { return '\u2022 ' + (b.name || '--') + ' \u2014 ' + (b.account || '--'); }).join('\n');
      },
      actions: [{ label: 'Manage Beneficiaries', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['profile', 'account', 'settings', 'update', 'change password', 'personal'],
      response: 'You can update your profile information, change your password, and manage notification preferences from your profile settings.',
      actions: [{ label: 'Edit Profile', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['hello', 'hi', 'hey', 'good morning', 'good afternoon', 'good evening', 'greetings'],
      getResponse: function() {
        var role = detectRole();
        if (role === 'admin') {
          return 'Hello! \u{1F44B} Welcome to SecureBank **Admin Dashboard**. I\'m your admin assistant.\n\nHow can I help you today? You can ask me about dashboard stats, pending loans, reports, customers, or transactions.';
        }
        return 'Hello! \u{1F44B} Welcome to SecureBank. I\'m your virtual banking assistant.\n\nHow can I help you today? You can ask me about your balance, transfers, loans, cards, or anything else.';
      },
      actions: []
    },
    {
      keywords: ['thank', 'thanks', 'appreciate'],
      response: 'You\'re welcome! \u{1F60A} Is there anything else I can help you with?',
      actions: []
    },
    {
      keywords: ['bye', 'goodbye', 'close', 'exit'],
      response: 'Thank you for banking with SecureBank! Have a great day. \u{1F44B}\n\nFeel free to chat anytime you need assistance.',
      actions: []
    },
    {
      keywords: ['insights', 'spending', 'analysis', 'ai insights', 'tips'],
      response: 'Our AI-powered spending insights analyze your transactions to help you save. Check the insights dashboard for personalized recommendations based on your spending patterns.',
      actions: [{ label: 'View AI Insights', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['deposit', 'add money', 'fund'],
      response: 'You can deposit funds via:\n\n\u2022 Bank Transfer \u2014 From another bank\n\u2022 UPI \u2014 Instant deposits\n\u2022 Check Deposit \u2014 Scan and deposit\n\nUse the Deposit button on the dashboard to add funds.',
      actions: [{ label: 'Make a Deposit', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['withdraw', 'cash', 'atm'],
      response: 'Withdrawal options:\n\nATM \u2014 Free at all SecureBank ATMs\nBranch \u2014 Visit any branch with your ID\nOnline Transfer \u2014 Transfer to another account\n\nUse the Withdraw button on the dashboard.',
      actions: [{ label: 'Withdraw Funds', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['interest', 'earn', 'savings rate', 'apy'],
      response: 'Current interest rates:\n\nSavings Account: 4.2% APY\nFixed Deposit: 5.5-7.0% APY\nMoney Market: 4.8% APY\n\nHigher balances qualify for premium rates.',
      actions: []
    },
    {
      keywords: ['fee', 'charges', 'cost', 'pricing'],
      response: 'SecureBank fee structure:\n\nAccount Maintenance: Free\nOnline Transfers: Free\nATM (own bank): Free\nATM (other banks): \u20B925/transaction\nWire Transfer: \u20B9150\n\nPremium customers get additional fee waivers!',
      actions: []
    }
  ];

  var FALLBACK_RESPONSE = 'I\'m not sure I understood that. Could you try rephrasing? Here are some things I can help with:\n\n\u2022 Check your **balance**\n\u2022 Make a **transfer**\n\u2022 View **loan** options\n\u2022 Manage your **cards**\n\u2022 View **transactions**\n\u2022 Get **spending insights**';

  function getGreeting() {
    var hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  }

  function formatMessage(text) {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>')
      .replace(/• /g, '&bull; ');
  }

  function getTimeString() {
    var now = new Date();
    return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  function findResponse(input) {
    var lower = input.toLowerCase().trim();
    var role = detectRole();
    var bestMatch = null;
    var bestScore = 0;

    // Combine admin responses with regular responses based on role
    var allResponses = RESPONSES.concat(role === 'admin' ? ADMIN_RESPONSES : []);

    for (var i = 0; i < allResponses.length; i++) {
      var entry = allResponses[i];
      var score = 0;
      for (var j = 0; j < entry.keywords.length; j++) {
        if (lower.indexOf(entry.keywords[j]) !== -1) {
          score = Math.max(score, entry.keywords[j].length);
        }
      }
      if (score > bestScore) {
        bestScore = score;
        bestMatch = entry;
      }
    }

    if (!bestMatch) {
      // Return role-specific fallback
      if (role === 'admin') {
        return { 
          response: 'I\'m not sure I understood that. As an admin, you can ask me about:\n\n\u2022 **Dashboard stats** \u2014 System overview\n\u2022 **Pending loans** \u2014 Loan applications\n\u2022 **Reports** \u2014 Analytics\n\u2022 **Customers** \u2014 Customer information\n\u2022 **Transactions** \u2014 Transaction summaries', 
          actions: [{ label: 'Go to Dashboard', page: 'admin.html' }] 
        };
      }
      return { response: FALLBACK_RESPONSE, actions: [] };
    }
    var responseText = typeof bestMatch.getResponse === 'function' ? bestMatch.getResponse() : bestMatch.response;
    return { response: responseText, actions: bestMatch.actions || [] };
  }

  function loadHistory() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    } catch (e) {
      return [];
    }
  }

  function saveHistory(messages) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(messages.slice(-50)));
    } catch (e) {}
  }

  function createChatbotHTML() {
    var container = document.createElement('div');
    container.id = 'securebank-chatbot';
    container.innerHTML =
      '<div class="chatbot-toggle" id="chatbotToggle" title="Chat with ' + BOT_NAME + '">' +
        '<i class="fas fa-comment-dots"></i>' +
      '</div>' +
      '<div class="chatbot-window" id="chatbotWindow">' +
        '<div class="chatbot-header">' +
          '<div class="chatbot-header-left">' +
            '<div class="chatbot-avatar"><i class="fas fa-robot"></i></div>' +
            '<div class="chatbot-header-info">' +
              '<div class="chatbot-name">' + BOT_NAME + '</div>' +
              '<div class="chatbot-status"><span class="status-dot"></span> Online</div>' +
            '</div>' +
          '</div>' +
          '<div class="chatbot-header-actions">' +
            '<button class="chatbot-clear-btn" id="chatbotClearBtn" title="Clear chat"><i class="fas fa-trash-alt"></i></button>' +
            '<button class="chatbot-close-btn" id="chatbotCloseBtn"><i class="fas fa-times"></i></button>' +
          '</div>' +
        '</div>' +
        '<div class="chatbot-messages" id="chatbotMessages"></div>' +
        '<div class="chatbot-quick-replies" id="chatbotQuickReplies"></div>' +
        '<div class="chatbot-input-area">' +
          '<input type="text" class="chatbot-input" id="chatbotInput" placeholder="Type your message..." autocomplete="off">' +
          '<button class="chatbot-send-btn" id="chatbotSendBtn"><i class="fas fa-paper-plane"></i></button>' +
        '</div>' +
      '</div>';
    return container;
  }

  function renderQuickReplies(container) {
    var html = '';
    for (var i = 0; i < QUICK_REPLIES.length; i++) {
      html += '<button class="quick-reply-btn" data-keyword="' + QUICK_REPLIES[i].keyword + '">' + QUICK_REPLIES[i].label + '</button>';
    }
    container.innerHTML = html;

    var buttons = container.querySelectorAll('.quick-reply-btn');
    for (var j = 0; j < buttons.length; j++) {
      buttons[j].addEventListener('click', function () {
        var keyword = this.getAttribute('data-keyword');
        handleUserMessage(keyword);
      });
    }
  }

  function addMessageToUI(messagesContainer, sender, text, actions, time) {
    var msgDiv = document.createElement('div');
    msgDiv.className = 'chatbot-message ' + sender;

    var isBot = sender === 'bot';
    var avatar = isBot ? '<div class="msg-avatar"><i class="fas fa-robot"></i></div>' : '';
    var timeStr = time || getTimeString();

    var actionsHTML = '';
    if (actions && actions.length > 0) {
      actionsHTML = '<div class="msg-actions">';
      for (var i = 0; i < actions.length; i++) {
        actionsHTML += '<button class="msg-action-btn" data-page="' + actions[i].page + '"><i class="fas fa-external-link-alt"></i> ' + actions[i].label + '</button>';
      }
      actionsHTML += '</div>';
    }

    msgDiv.innerHTML =
      avatar +
      '<div class="msg-content">' +
        '<div class="msg-bubble">' + formatMessage(text) + actionsHTML + '</div>' +
        '<div class="msg-time">' + timeStr + '</div>' +
      '</div>';

    messagesContainer.appendChild(msgDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    var actionBtns = msgDiv.querySelectorAll('.msg-action-btn');
    for (var j = 0; j < actionBtns.length; j++) {
      actionBtns[j].addEventListener('click', function () {
        var page = this.getAttribute('data-page');
        if (page) window.location.href = page;
      });
    }
  }

  function showTypingIndicator(messagesContainer) {
    var typingDiv = document.createElement('div');
    typingDiv.className = 'chatbot-message bot';
    typingDiv.id = 'typingIndicator';
    typingDiv.innerHTML =
      '<div class="msg-avatar"><i class="fas fa-robot"></i></div>' +
      '<div class="msg-content">' +
        '<div class="msg-bubble typing-indicator">' +
          '<span></span><span></span><span></span>' +
        '</div>' +
      '</div>';
    messagesContainer.appendChild(typingDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }

  function removeTypingIndicator() {
    var el = document.getElementById('typingIndicator');
    if (el) el.remove();
  }

  function handleUserMessage(text) {
    var messagesContainer = document.getElementById('chatbotMessages');
    var input = document.getElementById('chatbotInput');

    addMessageToUI(messagesContainer, 'user', text, [], getTimeString());

    var history = loadHistory();
    history.push({ sender: 'user', text: text, time: new Date().toISOString() });
    saveHistory(history);

    input.value = '';

    showTypingIndicator(messagesContainer);

    setTimeout(function () {
      removeTypingIndicator();

      var match = findResponse(text);
      addMessageToUI(messagesContainer, 'bot', match.response, match.actions, getTimeString());

      var historyAfter = loadHistory();
      historyAfter.push({ sender: 'bot', text: match.response, actions: match.actions, time: new Date().toISOString() });
      saveHistory(historyAfter);
    }, TYPING_DELAY);
  }

  function initChatbot() {
    var existing = document.getElementById('securebank-chatbot');
    if (existing) return;

    var chatbotEl = createChatbotHTML();
    document.body.appendChild(chatbotEl);

    var toggle = document.getElementById('chatbotToggle');
    var window_ = document.getElementById('chatbotWindow');
    var closeBtn = document.getElementById('chatbotCloseBtn');
    var clearBtn = document.getElementById('chatbotClearBtn');
    var input = document.getElementById('chatbotInput');
    var sendBtn = document.getElementById('chatbotSendBtn');
    var messagesContainer = document.getElementById('chatbotMessages');
    var quickRepliesContainer = document.getElementById('chatbotQuickReplies');

    renderQuickReplies(quickRepliesContainer);

    function openChat() {
      isOpen = true;
      window_.classList.add('open');
      toggle.classList.add('active');
      setTimeout(function () { input.focus(); }, 300);
    }

    function closeChat() {
      isOpen = false;
      window_.classList.remove('open');
      toggle.classList.remove('active');
    }

    toggle.addEventListener('click', function () {
      if (isOpen) closeChat(); else openChat();
    });

    closeBtn.addEventListener('click', closeChat);

    clearBtn.addEventListener('click', function () {
      localStorage.removeItem(STORAGE_KEY);
      messagesContainer.innerHTML = '';
      addWelcomeMessage();
    });

    sendBtn.addEventListener('click', function () {
      var text = input.value.trim();
      if (text) handleUserMessage(text);
    });

    input.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        var text = input.value.trim();
        if (text) handleUserMessage(text);
      }
    });

    function addWelcomeMessage() {
      var history = loadHistory();
      if (history.length > 0) {
        for (var i = 0; i < history.length; i++) {
          var msg = history[i];
          var timeStr = '';
          if (msg.time) {
            try {
              timeStr = new Date(msg.time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            } catch (e) {
              timeStr = getTimeString();
            }
          }
          addMessageToUI(messagesContainer, msg.sender, msg.text, msg.actions || [], timeStr);
        }
      } else {
        var role = detectRole();
        var greeting = getGreeting();
        var welcomeText = '';
        
        if (role === 'admin') {
          welcomeText = greeting + '! \u{1F44B} I\'m ' + BOT_NAME + ', your **Admin Assistant**.\n\nI can help you with:\n\u2022 **Dashboard stats** \u2014 System overview\n\u2022 **Pending loans** \u2014 Loan applications\n\u2022 **Reports** \u2014 Analytics & charts\n\u2022 **Customers** \u2014 Customer management\n\u2022 **Transactions** \u2014 Transaction summaries\n\nHow can I assist you today?';
        } else if (role === 'staff') {
          welcomeText = greeting + '! \u{1F44B} I\'m ' + BOT_NAME + ', your **Staff Assistant**.\n\nI can help you with:\n\u2022 **Customers** \u2014 Customer information\n\u2022 **Accounts** \u2014 Account management\n\u2022 **Pending tasks** \u2014 Tasks requiring attention\n\u2022 **Reports** \u2014 View analytics\n\nHow can I assist you today?';
        } else {
          welcomeText = greeting + '! \u{1F44B} I\'m ' + BOT_NAME + ', your virtual banking assistant.\n\nI can help you with:\n\u2022 Checking your **balance**\n\u2022 Making **transfers**\n\u2022 **Loan** information\n\u2022 Managing **cards**\n\u2022 **Spending insights**\n\nHow can I assist you today?';
        }
        
        addMessageToUI(messagesContainer, 'bot', welcomeText, [], getTimeString());
        saveHistory([{ sender: 'bot', text: welcomeText, time: new Date().toISOString() }]);
      }
    }

    addWelcomeMessage();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initChatbot);
  } else {
    initChatbot();
  }
})();