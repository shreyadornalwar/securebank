(function (window) {
  var CATEGORY_KEYWORDS = {
    'Food & Dining': ['food', 'restaurant', 'cafe', 'dining', 'swiggy', 'zomato', 'pizza', 'burger', 'meal', 'lunch', 'dinner', 'breakfast', 'snack', 'coffee', 'tea', 'grocery', 'supermarket', 'market', 'vegetables', 'fruits'],
    'Shopping': ['shopping', 'amazon', 'flipkart', 'myntra', 'clothes', 'fashion', 'electronics', 'gadget', 'mobile', 'laptop', 'shoes', 'apparel', 'mall', 'store', 'retail'],
    'Transport': ['uber', 'ola', 'taxi', 'cab', 'fuel', 'petrol', 'diesel', 'gas', 'metro', 'bus', 'train', 'flight', 'travel', 'parking', 'toll'],
    'Utilities': ['electricity', 'water', 'gas bill', 'internet', 'wifi', 'broadband', 'phone bill', 'recharge', 'utility', 'maintenance', 'bill payment'],
    'Entertainment': ['movie', 'netflix', 'spotify', 'prime', 'hotstar', 'game', 'concert', 'show', 'entertainment', 'subscription', 'gaming'],
    'Health': ['hospital', 'doctor', 'pharmacy', 'medicine', 'health', 'medical', 'clinic', 'lab', 'test', 'insurance', 'fitness', 'gym'],
    'Education': ['course', 'book', 'tuition', 'school', 'college', 'university', 'udemy', 'coursera', 'education', 'training', 'certification'],
    'Transfer': ['transfer', 'sent', 'received', 'remittance', 'upi', 'pay ', 'payment'],
    'Income': ['salary', 'deposit', 'credit', 'interest', 'dividend', 'refund', 'cashback', 'bonus', 'income']
  };

  var CATEGORY_ICONS = {
    'Food & Dining': 'fa-utensils',
    'Shopping': 'fa-shopping-bag',
    'Transport': 'fa-car',
    'Utilities': 'fa-bolt',
    'Entertainment': 'fa-film',
    'Health': 'fa-heartbeat',
    'Education': 'fa-graduation-cap',
    'Transfer': 'fa-exchange-alt',
    'Income': 'fa-arrow-down',
    'Other': 'fa-ellipsis-h'
  };

  var CATEGORY_COLORS = {
    'Food & Dining': '#EF4444',
    'Shopping': '#8B5CF6',
    'Transport': '#F59E0B',
    'Utilities': '#3B82F6',
    'Entertainment': '#EC4899',
    'Health': '#10B981',
    'Education': '#6366F1',
    'Transfer': '#64748B',
    'Income': '#22C55E',
    'Other': '#9CA3AF'
  };

  function categorize(description) {
    var desc = (description || '').toLowerCase();
    for (var cat in CATEGORY_KEYWORDS) {
      var keywords = CATEGORY_KEYWORDS[cat];
      for (var i = 0; i < keywords.length; i++) {
        if (desc.indexOf(keywords[i]) !== -1) return cat;
      }
    }
    return 'Other';
  }

  function getMonthKey(dateStr) {
    var d = new Date(dateStr);
    if (isNaN(d.getTime())) return null;
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
  }

  function getMonthName(monthKey) {
    var parts = monthKey.split('-');
    var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months[parseInt(parts[1], 10) - 1] + ' ' + parts[0];
  }

  function analyze(transactions) {
    if (!transactions || !Array.isArray(transactions) || transactions.length === 0) {
      return { empty: true, insights: [], categoryBreakdown: [], monthlyTrend: [], currentMonthTotal: 0, previousMonthTotal: 0, currentMonth: null, previousMonth: null, transactions: [] };
    }

    var categorized = [];
    transactions.forEach(function(t) {
      if ((t.type === 'WITHDRAWAL' || t.type === 'TRANSFER' || t.type === 'withdrawal' || t.type === 'transfer') && t.date) {
        var monthKey = getMonthKey(t.date);
        if (monthKey) {
          categorized.push({
            date: t.date,
            description: t.description || '',
            amount: Number(t.amount) || 0,
            type: t.type.toUpperCase(),
            category: categorize(t.description),
            monthKey: monthKey
          });
        }
      }
    });

    var monthSpending = {};
    var categoryMonthSpending = {};

    categorized.forEach(function(t) {
      if (!monthSpending[t.monthKey]) monthSpending[t.monthKey] = 0;
      monthSpending[t.monthKey] += t.amount;

      if (!categoryMonthSpending[t.category]) categoryMonthSpending[t.category] = {};
      if (!categoryMonthSpending[t.category][t.monthKey]) categoryMonthSpending[t.category][t.monthKey] = 0;
      categoryMonthSpending[t.category][t.monthKey] += t.amount;
    });

    var months = Object.keys(monthSpending).sort();
    if (months.length === 0) {
      return { empty: true, insights: [], categoryBreakdown: [], monthlyTrend: [], currentMonthTotal: 0, previousMonthTotal: 0, currentMonth: null, previousMonth: null, transactions: transactions };
    }

    var currentMonth = months[months.length - 1];
    var previousMonth = months.length >= 2 ? months[months.length - 2] : null;

    var insights = [];

    // 1. Month-over-month total spending comparison
    if (previousMonth) {
      var currentTotal = monthSpending[currentMonth] || 0;
      var previousTotal = monthSpending[previousMonth] || 0;
      if (previousTotal > 0) {
        var pctChange = ((currentTotal - previousTotal) / previousTotal * 100);
        var absPct = Math.abs(Math.round(pctChange));
        if (pctChange > 5) {
          insights.push({
            type: 'alert',
            icon: 'fa-chart-line',
            title: 'Spending Increased',
            message: 'You spent ' + absPct + '% more this month (\u20B9' + currentTotal.toLocaleString('en-IN') + ' vs \u20B9' + previousTotal.toLocaleString('en-IN') + ').',
            color: '#EF4444',
            priority: 1
          });
        } else if (pctChange < -5) {
          insights.push({
            type: 'positive',
            icon: 'fa-chart-line',
            title: 'Spending Decreased',
            message: 'You spent ' + absPct + '% less this month (\u20B9' + currentTotal.toLocaleString('en-IN') + ' vs \u20B9' + previousTotal.toLocaleString('en-IN') + '). Great job!',
            color: '#22C55E',
            priority: 1
          });
        } else {
          insights.push({
            type: 'info',
            icon: 'fa-chart-line',
            title: 'Spending Stable',
            message: 'Your spending is consistent at \u20B9' + currentTotal.toLocaleString('en-IN') + ' this month.',
            color: '#3B82F6',
            priority: 3
          });
        }
      }
    }

    // 2. Category-specific insights
    var categories = Object.keys(categoryMonthSpending);
    categories.forEach(function(cat) {
      if (cat === 'Other' || cat === 'Transfer' || cat === 'Income') return;
      var catMonths = categoryMonthSpending[cat];
      var catCurrentMonth = catMonths[currentMonth] || 0;
      if (previousMonth) {
        var catPreviousMonth = catMonths[previousMonth] || 0;
        if (catPreviousMonth > 0 && catCurrentMonth > 0) {
          var catPctChange = ((catCurrentMonth - catPreviousMonth) / catPreviousMonth * 100);
          var catAbsPct = Math.abs(Math.round(catPctChange));
          if (catPctChange > 20) {
            insights.push({
              type: 'alert',
              icon: CATEGORY_ICONS[cat] || 'fa-tag',
              title: cat + ' Spike',
              message: 'You spent ' + catAbsPct + '% more on ' + cat.toLowerCase() + ' this month (\u20B9' + catCurrentMonth.toLocaleString('en-IN') + ').',
              color: CATEGORY_COLORS[cat] || '#6B7280',
              priority: 2
            });
          } else if (catPctChange < -20) {
            insights.push({
              type: 'positive',
              icon: CATEGORY_ICONS[cat] || 'fa-tag',
              title: cat + ' Savings',
              message: 'You saved ' + catAbsPct + '% on ' + cat.toLowerCase() + ' this month (\u20B9' + catCurrentMonth.toLocaleString('en-IN') + ' vs \u20B9' + catPreviousMonth.toLocaleString('en-IN') + ').',
              color: CATEGORY_COLORS[cat] || '#6B7280',
              priority: 2
            });
          }
        }
      }
    });

    // 3. Top spending category
    var catTotals = {};
    categorized.forEach(function(t) {
      if (t.monthKey === currentMonth && t.category !== 'Other' && t.category !== 'Income') {
        if (!catTotals[t.category]) catTotals[t.category] = 0;
        catTotals[t.category] += t.amount;
      }
    });
    var sortedCats = Object.keys(catTotals).sort(function(a, b) { return catTotals[b] - catTotals[a]; });
    if (sortedCats.length > 0) {
      var topCat = sortedCats[0];
      var topAmount = catTotals[topCat];
      var totalSpending = monthSpending[currentMonth] || 1;
      var topPct = Math.round(topAmount / totalSpending * 100);
      insights.push({
        type: 'info',
        icon: CATEGORY_ICONS[topCat] || 'fa-trophy',
        title: 'Top Spending Category',
        message: topCat + ' is your largest expense at \u20B9' + topAmount.toLocaleString('en-IN') + ' (' + topPct + '% of total spending).',
        color: CATEGORY_COLORS[topCat] || '#6B7280',
        priority: 3
      });
    }

    // 4. Average daily spending
    if (currentMonth) {
      var now = new Date();
      var currentMonthKey = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
      var dayOfMonth = currentMonth === currentMonthKey ? now.getDate() : 30;
      var monthTotal = monthSpending[currentMonth] || 0;
      var avgDaily = Math.round(monthTotal / dayOfMonth);
      var projectedMonthEnd = avgDaily * 30;
      insights.push({
        type: 'info',
        icon: 'fa-calendar-day',
        title: 'Daily Average',
        message: 'You\'re spending an average of \u20B9' + avgDaily.toLocaleString('en-IN') + '/day. Projected monthly: \u20B9' + projectedMonthEnd.toLocaleString('en-IN') + '.',
        color: '#6366F1',
        priority: 4
      });
    }

    // 5. Transaction frequency
    var currentMonthTxs = categorized.filter(function(t) { return t.monthKey === currentMonth; });
    if (currentMonthTxs.length > 0) {
      var freq = currentMonthTxs.length;
      insights.push({
        type: freq > 30 ? 'alert' : 'info',
        icon: 'fa-receipt',
        title: 'Transaction Frequency',
        message: 'You made ' + freq + ' spending transaction' + (freq > 1 ? 's' : '') + ' this month' + (freq > 30 ? '. Consider consolidating purchases.' : '.'),
        color: freq > 30 ? '#F59E0B' : '#3B82F6',
        priority: 4
      });
    }

    // 6. Savings rate
    if (currentMonth) {
      var income = 0;
      transactions.forEach(function(t) {
        var tType = (t.type || '').toUpperCase();
        var tMonthKey = getMonthKey(t.date);
        if (tType === 'DEPOSIT' && tMonthKey === currentMonth) {
          income += Number(t.amount) || 0;
        }
      });
      if (income > 0) {
        var spent = monthSpending[currentMonth] || 0;
        var saved = income - spent;
        var savingsRate = Math.round(saved / income * 100);
        insights.push({
          type: savingsRate >= 20 ? 'positive' : savingsRate >= 10 ? 'info' : 'alert',
          icon: 'fa-piggy-bank',
          title: 'Savings Rate',
          message: 'You saved ' + savingsRate + '% of your income this month (\u20B9' + saved.toLocaleString('en-IN') + ').' + (savingsRate < 20 ? ' Aim for 20% or more.' : ''),
          color: savingsRate >= 20 ? '#22C55E' : savingsRate >= 10 ? '#3B82F6' : '#EF4444',
          priority: 1
        });
      }
    }

    // Build category breakdown for current month
    var categoryBreakdown = {};
    currentMonthTxs.forEach(function(t) {
      if (!categoryBreakdown[t.category]) categoryBreakdown[t.category] = 0;
      categoryBreakdown[t.category] += t.amount;
    });

    var breakdownArray = Object.keys(categoryBreakdown).map(function(cat) {
      return { category: cat, amount: categoryBreakdown[cat], icon: CATEGORY_ICONS[cat] || 'fa-tag', color: CATEGORY_COLORS[cat] || '#6B7280' };
    }).sort(function(a, b) { return b.amount - a.amount; });

    // Monthly trend data
    var monthlyTrend = months.map(function(m) {
      return { month: getMonthName(m), amount: monthSpending[m] || 0 };
    });

    insights.sort(function(a, b) { return a.priority - b.priority; });

    return {
      empty: false,
      insights: insights,
      categoryBreakdown: breakdownArray,
      monthlyTrend: monthlyTrend,
      currentMonthTotal: monthSpending[currentMonth] || 0,
      previousMonthTotal: previousMonth ? (monthSpending[previousMonth] || 0) : 0,
      currentMonth: getMonthName(currentMonth),
      previousMonth: previousMonth ? getMonthName(previousMonth) : null,
      transactions: transactions
    };
  }

  function renderInsightsDashboard(containerId, transactions) {
    var result = analyze(transactions);
    var container = document.getElementById(containerId);
    if (!container) return;

    if (result.empty) {
      container.innerHTML =
        '<div class="card">' +
        '<div class="card-header"><h3><i class="fas fa-robot" style="margin-right:8px;color:var(--primary)"></i> AI Spending Insights</h3></div>' +
        '<div style="padding: 60px 24px; text-align: center;">' +
        '<div style="width: 80px; height: 80px; border-radius: 50%; background: rgba(99,102,241,0.1); display: flex; align-items: center; justify-content: center; margin: 0 auto 20px;">' +
        '<i class="fas fa-chart-pie" style="font-size: 32px; color: var(--primary);"></i></div>' +
        '<h3 style="font-size: 18px; font-weight: 700; margin-bottom: 8px; color: var(--text-dark);">No Transaction Data Yet</h3>' +
        '<p style="font-size: 14px; color: var(--text-gray); max-width: 360px; margin: 0 auto 24px; line-height: 1.6;">' +
        'Make some deposits, withdrawals, or transfers to see personalized AI-powered spending insights here.</p>' +
        '<div style="display: flex; gap: 12px; justify-content: center; flex-wrap: wrap;">' +
        '<button class="btn btn-primary" onclick="showDepositModal()"><i class="fas fa-arrow-down"></i> Make a Deposit</button>' +
        '<button class="btn btn-outline" onclick="showTransferModal()"><i class="fas fa-exchange-alt"></i> Transfer</button>' +
        '</div></div></div>';
      return;
    }

    var html = '';

    // Summary stats
    html += '<div class="stats-grid" style="grid-template-columns: repeat(3, 1fr); margin-bottom: 24px;">';
    html += '<div class="stat-card blue">';
    html += '<div class="stat-icon"><i class="fas fa-rupee-sign"></i></div>';
    html += '<div class="stat-value">\u20B9' + result.currentMonthTotal.toLocaleString('en-IN') + '</div>';
    html += '<div class="stat-label">Spending (' + result.currentMonth + ')</div>';
    html += '</div>';

    var changePct = result.previousMonthTotal > 0 ? Math.round((result.currentMonthTotal - result.previousMonthTotal) / result.previousMonthTotal * 100) : 0;
    var changeColor = changePct > 0 ? 'orange' : 'green';
    var changeIcon = changePct > 0 ? 'fa-arrow-up' : 'fa-arrow-down';
    html += '<div class="stat-card ' + changeColor + '">';
    html += '<div class="stat-icon"><i class="fas ' + changeIcon + '"></i></div>';
    html += '<div class="stat-value">' + (changePct > 0 ? '+' : '') + changePct + '%</div>';
    html += '<div class="stat-label">vs ' + (result.previousMonth || 'Last Month') + '</div>';
    html += '</div>';

    // Calculate savings from real deposit data
    var currentMonthKey = null;
    if (result.monthlyTrend.length > 0) {
      var lastTrendMonth = result.monthlyTrend[result.monthlyTrend.length - 1].month;
      var monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      var parts = lastTrendMonth.split(' ');
      var monthIdx = monthNames.indexOf(parts[0]);
      var year = parts[1];
      if (monthIdx >= 0) {
        currentMonthKey = year + '-' + String(monthIdx + 1).padStart(2, '0');
      }
    }

    var totalDeposits = 0;
    if (currentMonthKey) {
      result.transactions.forEach(function(t) {
        var tType = (t.type || '').toUpperCase();
        var tMonthKey = getMonthKey(t.date);
        if (tType === 'DEPOSIT' && tMonthKey === currentMonthKey) {
          totalDeposits += Number(t.amount) || 0;
        }
      });
    }
    var savingsAmount = totalDeposits - result.currentMonthTotal;

    html += '<div class="stat-card ' + (savingsAmount >= 0 ? 'green' : 'orange') + '">';
    html += '<div class="stat-icon"><i class="fas fa-piggy-bank"></i></div>';
    html += '<div class="stat-value">\u20B9' + Math.abs(savingsAmount).toLocaleString('en-IN') + '</div>';
    html += '<div class="stat-label">' + (savingsAmount >= 0 ? 'Saved' : 'Overspent') + '</div>';
    html += '</div>';
    html += '</div>';

    // AI Insights cards
    if (result.insights.length > 0) {
      html += '<div class="card">';
      html += '<div class="card-header"><h3><i class="fas fa-robot" style="margin-right:8px;color:var(--primary)"></i> AI Spending Insights</h3></div>';
      html += '<div style="padding: 20px;">';
      html += '<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 16px;">';

      result.insights.forEach(function(insight) {
        var bgOpacity = insight.type === 'alert' ? '0.08' : insight.type === 'positive' ? '0.08' : '0.06';
        html += '<div class="insight-card" style="background: rgba(' + hexToRgb(insight.color) + ',' + bgOpacity + '); border: 1px solid ' + insight.color + '22; border-radius: 12px; padding: 20px; position: relative; overflow: hidden;">';
        html += '<div style="position: absolute; top: 0; left: 0; width: 4px; height: 100%; background: ' + insight.color + ';"></div>';
        html += '<div style="display: flex; align-items: flex-start; gap: 12px;">';
        html += '<div style="width: 40px; height: 40px; border-radius: 10px; background: ' + insight.color + '18; display: flex; align-items: center; justify-content: center; flex-shrink: 0;"><i class="fas ' + insight.icon + '" style="color: ' + insight.color + '; font-size: 16px;"></i></div>';
        html += '<div><div style="font-weight: 700; font-size: 14px; margin-bottom: 4px; color: ' + insight.color + ';">' + insight.title + '</div>';
        html += '<div style="font-size: 13px; color: #4B5563; line-height: 1.5;">' + insight.message + '</div></div>';
        html += '</div>';
        if (insight.type === 'alert') {
          html += '<div style="position: absolute; top: 12px; right: 12px;"><span class="badge badge-warning" style="font-size: 10px;">Action Needed</span></div>';
        } else if (insight.type === 'positive') {
          html += '<div style="position: absolute; top: 12px; right: 12px;"><span class="badge badge-success" style="font-size: 10px;">On Track</span></div>';
        }
        html += '</div>';
      });

      html += '</div></div></div>';
    }

    // Category Breakdown
    if (result.categoryBreakdown.length > 0) {
      var totalCatSpending = result.categoryBreakdown.reduce(function(s, c) { return s + c.amount; }, 0);
      html += '<div class="card" style="margin-top: 24px;">';
      html += '<div class="card-header"><h3><i class="fas fa-chart-pie" style="margin-right:8px;color:var(--primary)"></i> Category Breakdown</h3></div>';
      html += '<div style="padding: 20px;">';
      html += '<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 12px;">';

      result.categoryBreakdown.forEach(function(cat) {
        var pct = totalCatSpending > 0 ? Math.round(cat.amount / totalCatSpending * 100) : 0;
        html += '<div style="display: flex; align-items: center; gap: 12px; padding: 12px; background: #f9fafb; border-radius: 10px;">';
        html += '<div style="width: 36px; height: 36px; border-radius: 8px; background: ' + cat.color + '18; display: flex; align-items: center; justify-content: center; flex-shrink: 0;"><i class="fas ' + cat.icon + '" style="color: ' + cat.color + '; font-size: 14px;"></i></div>';
        html += '<div style="flex: 1;"><div style="display: flex; justify-content: space-between; margin-bottom: 4px;"><span style="font-size: 13px; font-weight: 600;">' + cat.category + '</span><span style="font-size: 13px; font-weight: 700;">\u20B9' + cat.amount.toLocaleString('en-IN') + '</span></div>';
        html += '<div style="background: #E5E7EB; border-radius: 4px; height: 6px; overflow: hidden;"><div style="width: ' + pct + '%; height: 100%; background: ' + cat.color + '; border-radius: 4px; transition: width 0.5s;"></div></div>';
        html += '<div style="font-size: 11px; color: #9CA3AF; margin-top: 2px;">' + pct + '% of total</div></div>';
        html += '</div>';
      });

      html += '</div></div></div>';
    }

    // Monthly Trend
    if (result.monthlyTrend.length > 1) {
      var maxAmount = Math.max.apply(null, result.monthlyTrend.map(function(m) { return m.amount; }));
      html += '<div class="card" style="margin-top: 24px;">';
      html += '<div class="card-header"><h3><i class="fas fa-chart-bar" style="margin-right:8px;color:var(--primary)"></i> Monthly Spending Trend</h3></div>';
      html += '<div style="padding: 20px;">';
      html += '<div style="display: flex; align-items: flex-end; gap: 8px; height: 200px; padding: 0 10px;">';

      result.monthlyTrend.forEach(function(m) {
        var barHeight = maxAmount > 0 ? Math.round(m.amount / maxAmount * 160) : 0;
        var isLast = m.month === result.monthlyTrend[result.monthlyTrend.length - 1].month;
        html += '<div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 8px;">';
        html += '<div style="font-size: 11px; font-weight: 600; color: #6B7280;">\u20B9' + (m.amount / 1000).toFixed(0) + 'k</div>';
        html += '<div style="width: 100%; max-width: 50px; height: ' + barHeight + 'px; background: ' + (isLast ? 'var(--primary)' : '#CBD5E1') + '; border-radius: 6px 6px 0 0; transition: height 0.5s; min-height: 4px;"></div>';
        html += '<div style="font-size: 11px; font-weight: ' + (isLast ? '700' : '500') + '; color: ' + (isLast ? 'var(--primary)' : '#9CA3AF') + ';">' + m.month.split(' ')[0] + '</div>';
        html += '</div>';
      });

      html += '</div></div></div>';
    }

    container.innerHTML = html;
  }

  function hexToRgb(hex) {
    hex = hex.replace('#', '');
    var r = parseInt(hex.substring(0, 2), 16);
    var g = parseInt(hex.substring(2, 4), 16);
    var b = parseInt(hex.substring(4, 6), 16);
    return r + ',' + g + ',' + b;
  }

  window.SpendingInsights = {
    analyze: analyze,
    categorize: categorize,
    renderInsightsDashboard: renderInsightsDashboard
  };
})(window);
