/**
 * Top 5 Coins Dashboard Component
 */
import { formatCurrency, formatCompactCurrency, formatPercentage, generateSparklineSVG } from '../utils/formatters.js';
import { StorageService } from '../services/storage.js';
import { cryptoApi } from '../api/coingecko.js';

export class DashboardComponent {
  constructor(container, onCoinSelect, onRefresh) {
    this.container = container;
    this.onCoinSelect = onCoinSelect;
    this.onRefresh = onRefresh;
    this.coinsData = [];
    this.secondaryCurrency = 'usd';
    this.primaryCurrency = 'inr';
    this.warningMessage = null;
  }

  setData(coins, primaryCurrency = 'inr', warning = null) {
    this.coinsData = coins || [];
    this.primaryCurrency = primaryCurrency;
    this.secondaryCurrency = primaryCurrency === 'inr' ? 'usd' : 'inr';
    this.warningMessage = warning;
    this.render();
  }

  render() {
    const settings = StorageService.getSettings();
    const showChart = settings.showMiniChart;
    const showPct = settings.showPercentage;

    let warningHtml = '';
    if (this.warningMessage) {
      warningHtml = `
        <div class="warning-banner">
          <span>⚠️ ${this.warningMessage}</span>
          <button class="icon-btn" id="btn-dismiss-warn" title="Dismiss">✕</button>
        </div>
      `;
    }

    let cardsHtml = '';
    if (this.coinsData.length === 0) {
      cardsHtml = `
        <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-muted);">
          <p style="font-size: 16px; margin-bottom: 8px;">Connecting to CoinGecko Live Feed...</p>
          <p style="font-size: 12px;">Fetching real-time cryptocurrency quotes</p>
        </div>
      `;
    } else {
      cardsHtml = this.coinsData.map((coin, index) => {
        const change24 = coin.price_change_percentage_24h || 0;
        const changeClass = change24 > 0 ? 'positive' : (change24 < 0 ? 'negative' : 'neutral');
        const changeArrow = change24 > 0 ? '▲' : (change24 < 0 ? '▼' : '•');

        const primaryPriceFormatted = formatCurrency(coin.current_price, this.primaryCurrency);
        // Estimate secondary price using exchange ratio or stored value
        const usdRate = coin.current_price_usd || (this.primaryCurrency === 'inr' ? (coin.current_price / 87.5) : coin.current_price * 87.5);
        const secondaryPriceFormatted = formatCurrency(usdRate, this.secondaryCurrency);

        const sparklinePoints = coin.sparkline_in_7d && coin.sparkline_in_7d.price ? coin.sparkline_in_7d.price.slice(-24) : [];
        const sparklineHtml = showChart ? `
          <div class="card-sparkline-box">
            ${generateSparklineSVG(sparklinePoints, 220, 36, change24 >= 0)}
          </div>
        ` : '';

        return `
          <div class="crypto-card" data-coin-id="${coin.id}" data-index="${index}" draggable="true">
            <div class="card-top">
              <div class="card-coin-info">
                <img src="${coin.image}" alt="${coin.name}" class="coin-logo" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'32\\' height=\\'32\\'><circle cx=\\'16\\' cy=\\'16\\' r=\\'15\\' fill=\\'%23334155\\'/></svg>'"/>
                <div class="coin-names">
                  <span class="coin-name">${coin.name}</span>
                  <span class="coin-symbol">${coin.symbol}</span>
                </div>
              </div>
              <div style="display: flex; align-items: center; gap: 6px;">
                <span class="card-rank-badge">#${coin.market_cap_rank || (index + 1)}</span>
                <button class="icon-btn btn-card-manage" data-coin-id="${coin.id}" title="Manage / Replace Coin">⋮</button>
              </div>
            </div>

            <div class="card-prices">
              <div class="primary-price">${primaryPriceFormatted}</div>
              <div class="secondary-price">${secondaryPriceFormatted}</div>
            </div>

            ${showPct ? `
              <div class="badge-change ${changeClass}">
                <span>${changeArrow}</span>
                <span>24H: ${formatPercentage(change24)}</span>
              </div>
            ` : ''}

            <div class="card-stats-grid">
              <div class="stat-item">
                <span class="stat-label">High:</span>
                <span class="stat-val">${formatCompactCurrency(coin.high_24h, this.primaryCurrency)}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Low:</span>
                <span class="stat-val">${formatCompactCurrency(coin.low_24h, this.primaryCurrency)}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Volume:</span>
                <span class="stat-val">${formatCompactCurrency(coin.total_volume, this.primaryCurrency)}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Market Cap:</span>
                <span class="stat-val">${formatCompactCurrency(coin.market_cap, this.primaryCurrency)}</span>
              </div>
            </div>

            ${sparklineHtml}

            <div class="card-footer">
              <span>Updated: ${new Date(coin.last_updated || Date.now()).toLocaleTimeString()}</span>
              <span style="color: var(--accent-cyan); font-weight: 600;">Details ➔</span>
            </div>
          </div>
        `;
      }).join('');
    }

    this.container.innerHTML = `
      ${warningHtml}
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
        <div>
          <h2 style="font-size: 18px; font-weight: 800; letter-spacing: -0.3px;">Top 5 Cryptocurrencies</h2>
          <p style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">Drag to reorder • Click any coin for comprehensive historical analytics</p>
        </div>
        <div style="display: flex; gap: 8px;">
          <button class="btn-secondary" id="btn-manage-top5">⚙️ Edit Selection (${this.coinsData.length}/5)</button>
        </div>
      </div>
      <div class="top5-grid" id="top5-cards-grid">
        ${cardsHtml}
      </div>
    `;

    this.bindEvents();
  }

  bindEvents() {
    // Dismiss warning
    const dismissBtn = this.container.querySelector('#btn-dismiss-warn');
    if (dismissBtn) {
      dismissBtn.addEventListener('click', () => {
        this.warningMessage = null;
        this.render();
      });
    }

    // Manage top 5 button
    const manageBtn = this.container.querySelector('#btn-manage-top5');
    if (manageBtn) {
      manageBtn.addEventListener('click', () => {
        this.openCoinManagerModal();
      });
    }

    // Card click for details
    const cards = this.container.querySelectorAll('.crypto-card');
    cards.forEach(card => {
      card.addEventListener('click', (e) => {
        if (e.target.closest('.btn-card-manage')) {
          e.stopPropagation();
          const coinId = card.dataset.coinId;
          this.openCoinManagerModal(coinId);
          return;
        }
        const coinId = card.dataset.coinId;
        const coin = this.coinsData.find(c => c.id === coinId);
        if (coin && this.onCoinSelect) {
          this.onCoinSelect(coin);
        }
      });

      // Drag and drop reordering
      card.addEventListener('dragstart', (e) => {
        e.dataTransfer.setData('text/plain', card.dataset.index);
        card.style.opacity = '0.4';
      });
      card.addEventListener('dragend', () => {
        card.style.opacity = '1';
      });
      card.addEventListener('dragover', (e) => {
        e.preventDefault();
      });
      card.addEventListener('drop', (e) => {
        e.preventDefault();
        const fromIndex = parseInt(e.dataTransfer.getData('text/plain'), 10);
        const toIndex = parseInt(card.dataset.index, 10);
        if (!isNaN(fromIndex) && !isNaN(toIndex) && fromIndex !== toIndex) {
          this.reorderCoins(fromIndex, toIndex);
        }
      });
    });
  }

  reorderCoins(fromIndex, toIndex) {
    const coins = [...this.coinsData];
    const [moved] = coins.splice(fromIndex, 1);
    coins.splice(toIndex, 0, moved);
    this.coinsData = coins;
    StorageService.saveTopCoins(coins.map(c => c.id));
    this.render();
  }

  openCoinManagerModal(replaceCoinId = null) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
      <div class="modal-content">
        <div class="modal-header">
          <h3 style="font-size: 16px; font-weight: 700;">
            ${replaceCoinId ? `Replace ${replaceCoinId.toUpperCase()}` : 'Manage Top 5 Coins Selection'}
          </h3>
          <button class="icon-btn" id="btn-close-modal">✕</button>
        </div>
        <div class="modal-body">
          <div class="input-group">
            <label class="input-label">Search Cryptocurrency</label>
            <input type="text" class="text-input" id="search-coin-input" placeholder="Search by name or symbol (e.g. ADA, XRP, Doge)..." autofocus/>
          </div>
          <div id="search-results-list" style="max-height: 260px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px;">
            <p style="font-size: 12px; color: var(--text-muted); text-align: center; padding: 20px;">Type at least 2 characters to search over 10,000+ coins</p>
          </div>
          <div style="border-top: 1px solid var(--border-glass); padding-top: 12px;">
            <div style="font-size: 12px; font-weight: 600; margin-bottom: 8px;">Current Active Top 5:</div>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;" id="active-pills-list">
              ${this.coinsData.map(c => `
                <span style="display: inline-flex; align-items: center; gap: 6px; padding: 4px 10px; background: rgba(30,41,59,0.8); border: 1px solid var(--border-glass); border-radius: 6px; font-size: 12px;">
                  <strong>${c.symbol.toUpperCase()}</strong> (${c.name})
                </span>
              `).join('')}
            </div>
          </div>
        </div>
      </div>
    `;

    document.body.appendChild(modal);

    const closeBtn = modal.querySelector('#btn-close-modal');
    closeBtn.addEventListener('click', () => modal.remove());
    modal.addEventListener('click', (e) => {
      if (e.target === modal) modal.remove();
    });

    const searchInput = modal.querySelector('#search-coin-input');
    const resultsContainer = modal.querySelector('#search-results-list');

    let debounceTimeout = null;
    searchInput.addEventListener('input', () => {
      clearTimeout(debounceTimeout);
      const query = searchInput.value.trim();
      if (query.length < 2) {
        resultsContainer.innerHTML = `<p style="font-size: 12px; color: var(--text-muted); text-align: center; padding: 20px;">Type at least 2 characters to search</p>`;
        return;
      }

      resultsContainer.innerHTML = `<p style="font-size: 12px; color: var(--text-secondary); text-align: center; padding: 20px;">Searching CoinGecko directory...</p>`;

      debounceTimeout = setTimeout(async () => {
        const results = await cryptoApi.searchCoins(query);
        if (results.length === 0) {
          resultsContainer.innerHTML = `<p style="font-size: 12px; color: var(--text-muted); text-align: center; padding: 20px;">No cryptocurrencies found for "${query}"</p>`;
          return;
        }

        resultsContainer.innerHTML = results.map(coin => `
          <div class="search-result-item" data-id="${coin.id}" style="display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; background: rgba(15,23,42,0.6); border-radius: 8px; cursor: pointer; border: 1px solid var(--border-glass);">
            <div style="display: flex; align-items: center; gap: 10px;">
              <img src="${coin.large || coin.thumb}" style="width: 24px; height: 24px; border-radius: 50%;" />
              <div>
                <span style="font-weight: 700; font-size: 13px;">${coin.name}</span>
                <span style="font-size: 11px; color: var(--text-muted); text-transform: uppercase; margin-left: 6px;">${coin.symbol}</span>
              </div>
            </div>
            <button class="btn-primary" style="padding: 4px 12px; font-size: 11px;">
              ${replaceCoinId ? 'Select' : 'Add / Replace'}
            </button>
          </div>
        `).join('');

        resultsContainer.querySelectorAll('.search-result-item').forEach(item => {
          item.addEventListener('click', () => {
            const selectedId = item.dataset.id;
            let currentIds = StorageService.getTopCoins();

            if (replaceCoinId) {
              const idx = currentIds.indexOf(replaceCoinId);
              if (idx !== -1) {
                currentIds[idx] = selectedId;
              } else {
                currentIds[0] = selectedId;
              }
            } else {
              if (!currentIds.includes(selectedId)) {
                currentIds.push(selectedId);
                if (currentIds.length > 5) currentIds.shift();
              }
            }

            StorageService.saveTopCoins(currentIds);
            modal.remove();
            if (this.onRefresh) this.onRefresh();
          });
        });
      }, 400);
    });
  }
}
