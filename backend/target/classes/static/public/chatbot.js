(function () {
  var STORAGE_KEY = 'secureBankChatHistory';
  var TYPING_DELAY = 800;
  var BOT_NAME = 'SecureBot';
  var isOpen = false;

  var QUICK_REPLIES = [
    { label: 'Check Balance', keyword: 'balance' },
    { label: 'Transfer Money', keyword: 'transfer' },
    { label: 'Loan Options', keyword: 'loans' },
    { label: 'Help', keyword: 'help' }
  ];

  function getRealData() {
    var accounts = JSON.parse(localStorage.getItem('bankAccounts') || '[]');
    var transactions = JSON.parse(localStorage.getItem('bankTransactions') || '[]');
    var cards = JSON.parse(localStorage.getItem('bankCards') || '[]');
    var beneficiaries = JSON.parse(localStorage.getItem('bankBeneficiaries') || '[]');
    var loans = JSON.parse(localStorage.getItem('bankLoans') || '[]');
    return { accounts: accounts, transactions: transactions, cards: cards, beneficiaries: beneficiaries, loans: loans };
  }

  function formatAmount(n) {
    return '\u20B9' + (Number(n) || 0).toLocaleString('en-IN');
  }

  var RESPONSES = [
    {
      keywords: ['balance', 'account', 'money', 'funds', 'how much'],
      getResponse: function() {
        var data = getRealData();
        if (data.accounts.length === 0) return 'No account data found. Please make a deposit or contact support to set up your account.';
        var lines = data.accounts.map(function(a) { return '\u20B9**' + (a.id || 'Account') + '**: ' + formatAmount(a.balance); }).join('\n');
        var total = data.accounts.reduce(function(s, a) { return s + (Number(a.balance) || 0); }, 0);
        return 'Your current account balances:\n\n' + lines + '\n\n**Total Balance**: ' + formatAmount(total);
      },
      actions: [{ label: 'View Accounts', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['transfer', 'send money', 'send', 'pay', 'payment', 'wire'],
      response: 'I can help you transfer funds! You can send money to saved beneficiaries or new recipients.\n\nTransfer options:\n• Between your accounts\n• To saved beneficiaries\n• To new recipients\n\nTransfers are processed instantly within SecureBank.',
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
        return 'We offer several loan options:\n\nHome Loan — from 8.5% p.a.\nPersonal Loan — from 10.5% p.a.\nCar Loan — from 9.0% p.a.\nEducation Loan — from 7.5% p.a.\nBusiness Loan — from 11.0% p.a.\n\nYou can calculate your EMI and apply online.';
      },
      actions: [{ label: 'Apply for Loan', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['help', 'support', 'assist', 'problem', 'issue', 'contact', 'customer service'],
      response: 'I\'m here to help! Here\'s what I can assist with:\n\n• Balance — Check your account balances\n• Transfer — Send money to others\n• Loans — View and apply for loans\n• Cards — Manage your debit/credit cards\n• Transactions — View transaction history\n• Profile — Update your account settings\n\nJust type what you need or tap a quick reply below.',
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
        return 'Your saved beneficiaries:\n\n' + data.beneficiaries.map(function(b) { return '\u2022 ' + (b.name || '--') + ' — ' + (b.account || '--'); }).join('\n');
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
      response: 'Hello! 👋 Welcome to SecureBank. I\'m your virtual banking assistant.\n\nHow can I help you today? You can ask me about your balance, transfers, loans, cards, or anything else.',
      actions: []
    },
    {
      keywords: ['thank', 'thanks', 'appreciate'],
      response: 'You\'re welcome! 😊 Is there anything else I can help you with?',
      actions: []
    },
    {
      keywords: ['bye', 'goodbye', 'close', 'exit'],
      response: 'Thank you for banking with SecureBank! Have a great day. 👋\n\nFeel free to chat anytime you need assistance.',
      actions: []
    },
    {
      keywords: ['insights', 'spending', 'analysis', 'ai insights', 'tips'],
      response: 'Our AI-powered spending insights analyze your transactions to help you save. Check the insights dashboard for personalized recommendations based on your spending patterns.',
      actions: [{ label: 'View AI Insights', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['deposit', 'add money', 'fund'],
      response: 'You can deposit funds via:\n\n• Bank Transfer — From another bank\n• UPI — Instant deposits\n• Check Deposit — Scan and deposit\n\nUse the Deposit button on the dashboard to add funds.',
      actions: [{ label: 'Make a Deposit', page: 'customer-dashboard.html' }]
    },
    {
      keywords: ['withdraw', 'cash', 'atm'],
      response: 'Withdrawal options:\n\nATM — Free at all SecureBank ATMs\nBranch — Visit any branch with your ID\nOnline Transfer — Transfer to another account\n\nUse the Withdraw button on the dashboard.',
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

  var FALLBACK_RESPONSE = 'I\'m not sure I understood that. Could you try rephrasing? Here are some things I can help with:\n\n• Check your **balance**\n• Make a **transfer**\n• View **loan** options\n• Manage your **cards**\n• View **transactions**\n• Get **spending insights**';

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
    var bestMatch = null;
    var bestScore = 0;

    for (var i = 0; i < RESPONSES.length; i++) {
      var entry = RESPONSES[i];
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

    if (!bestMatch) return { response: FALLBACK_RESPONSE, actions: [] };
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
        '<span class="chatbot-toggle-badge" id="chatbotBadge">1</span>' +
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
    var badge = document.getElementById('chatbotBadge');

    renderQuickReplies(quickRepliesContainer);

    function openChat() {
      isOpen = true;
      window_.classList.add('open');
      toggle.classList.add('active');
      badge.style.display = 'none';
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
        var greeting = getGreeting();
        var welcomeText = greeting + '! 👋 I\'m ' + BOT_NAME + ', your virtual banking assistant.\n\nI can help you with:\n• Checking your **balance**\n• Making **transfers**\n• **Loan** information\n• Managing **cards**\n• **Spending insights**\n\nHow can I assist you today?';
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
