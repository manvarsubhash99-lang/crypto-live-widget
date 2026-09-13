/**
 * Desktop Floating Widget Component (Compact and Expanded Modes)
 */
import { formatCurrency, formatCompactCurrency, formatPercentage, generateSparklineSVG } from '../utils/formatters.js';
import { StorageService } from '../services/storage.js';

export class WidgetComponent {
  constructor(container, onCoinSelect, onExitWidgetMode) {
    this.container = container;
    this.onCoinSelect = onCoinSelect;
    this.onExitWidgetMode = onExitWidgetMode;
    this.coinsData = [];
    this.currency = 'inr';
  }

  setData(coins, currency = 'inr') {
    this.coinsData = coins || [];
    this.currency = currency;
    this.render();
  }

  render() {
    const settings = StorageService.getSettings();
    const isExpanded = settings.widgetMode === 'expanded';

    const rowsHtml = this.coinsData.map(coin => {
      const change24 = coin.price_change_percentage_24h || 0;
      const changeClass = change24 > 0 ? 'positive' : (change24 < 0 ? 'negative' : 'neutral');
      const changeArrow = change24 > 0 ? '▲' : (change24 < 0 ? '▼' : '');
      const priceCompact = formatCompactCurrency(coin.current_price, this.currency);

      const sparklinePoints = coin.sparkline_in_7d && coin.sparkline_in_7d.price ? coin.sparkline_in_7d.price.slice(-16) : [];

      if (!isExpanded) {
        // Ultra-compact mode
        return `
          <div class="widget-item-row" data-coin-id="${coin.id}">
            <div class="widget-coin-col">
              <img src="${coin.image}" style="width: 20px; height: 20px; border-radius: 50%;" onerror="this.style.display='none'"/>
              <span style="font-weight: 800; font-size: 13px; letter-spacing: 0.5px;">${coin.symbol.toUpperCase()}</span>
            </div>
            <div class="widget-price-col">
              <span style="font-family: var(--font-mono); font-weight: 700; font-size: 13px;">${priceCompact}</span>
              <span class="badge-change ${changeClass}" style="padding: 1px 5px; font-size: 10px;">
                ${changeArrow} ${formatPercentage(change24)}
              </span>
            </div>
          </div>
        `;
      }

      // Expanded widget mode with mini sparkline & detailed price
      return `
        <div class="widget-item-row" data-coin-id="${coin.id}" style="padding: 10px;">
          <div style="display: flex; flex-direction: column; gap: 4px; flex: 1;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <img src="${coin.image}" style="width: 20px; height: 20px; border-radius: 50%;" />
              <span style="font-weight: 800; font-size: 13px;">${coin.symbol.toUpperCase()}</span>
              <span style="font-size: 11px; color: var(--text-muted);">${coin.name}</span>
            </div>
            <div style="font-family: var(--font-mono); font-weight: 800; font-size: 14px;">
              ${formatCurrency(coin.current_price, this.currency)}
            </div>
          </div>
          <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 4px;">
            <span class="badge-change ${changeClass}">
              ${changeArrow} ${formatPercentage(change24)}
            </span>
            ${settings.showMiniChart ? generateSparklineSVG(sparklinePoints, 80, 22, change24 >= 0) : ''}
          </div>
        </div>
      `;
    }).join('');

    const nowFormatted = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    this.container.innerHTML = `
      <div class="widget-compact-container">
        <!-- Draggable Mini Bar -->
        <div class="widget-titlebar-mini">
          <div style="display: flex; align-items: center; gap: 6px; font-size: 11px; font-weight: 700; color: var(--text-secondary);">
            <span style="display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: #10B981;"></span>
            CryptoLive
          </div>
          <div style="display: flex; align-items: center; gap: 4px; -webkit-app-region: no-drag;">
            <button class="icon-btn" id="btn-toggle-widget-expand" title="${isExpanded ? 'Switch to Compact' : 'Switch to Expanded'}" style="width: 22px; height: 22px; font-size: 10px;">
              ${isExpanded ? '🗗' : '🗖'}
            </button>
            <button class="icon-btn" id="btn-exit-widget-mode" title="Open Full Dashboard" style="width: 22px; height: 22px; font-size: 10px;">
              ⤢
            </button>
          </div>
        </div>

        <!-- Coins List -->
        <div style="display: flex; flex-direction: column; gap: 5px; flex: 1; overflow-y: auto;">
          ${rowsHtml}
        </div>

        <!-- Footer with Opacity and Timestamp -->
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 4px 6px; font-size: 10px; color: var(--text-muted); border-top: 1px solid rgba(255, 255, 255, 0.05);">
          <span>Updated: ${nowFormatted}</span>
          <div style="display: flex; align-items: center; gap: 6px; -webkit-app-region: no-drag;">
            <span>Opacity</span>
            <input type="range" id="widget-opacity-range" min="0.4" max="1" step="0.05" value="${settings.opacity || 0.95}" style="width: 50px; height: 3px; accent-color: var(--accent-green);" />
          </div>
        </div>
      </div>
    `;

    this.bindEvents();
  }

  bindEvents() {
    // Opacity slider
    const opacityInput = this.container.querySelector('#widget-opacity-range');
    if (opacityInput) {
      opacityInput.addEventListener('input', (e) => {
        const val = parseFloat(e.target.value);
        if (window.cryptoLiveAPI && window.cryptoLiveAPI.setOpacity) {
          window.cryptoLiveAPI.setOpacity(val);
        }
        const settings = StorageService.getSettings();
        settings.opacity = val;
        StorageService.saveSettings(settings);
      });
    }

    // Toggle expand
    const toggleExpandBtn = this.container.querySelector('#btn-toggle-widget-expand');
    if (toggleExpandBtn) {
      toggleExpandBtn.addEventListener('click', () => {
        const settings = StorageService.getSettings();
        settings.widgetMode = settings.widgetMode === 'expanded' ? 'compact' : 'expanded';
        StorageService.saveSettings(settings);
        this.render();
      });
    }

    // Exit widget mode back to dashboard
    const exitBtn = this.container.querySelector('#btn-exit-widget-mode');
    if (exitBtn) {
      exitBtn.addEventListener('click', () => {
        if (this.onExitWidgetMode) this.onExitWidgetMode();
      });
    }

    // Click coin row to view details
    this.container.querySelectorAll('.widget-item-row').forEach(row => {
      row.addEventListener('click', () => {
        const coinId = row.dataset.coinId;
        const coin = this.coinsData.find(c => c.id === coinId);
        if (coin && this.onCoinSelect) {
          this.onCoinSelect(coin);
        }
      });
    });
  }
}
