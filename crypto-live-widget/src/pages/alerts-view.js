/**
 * Price Alerts View Component
 */
import { StorageService } from '../services/storage.js';
import { formatCurrency } from '../utils/formatters.js';

export class AlertsView {
  constructor(container, activeCoins = []) {
    this.container = container;
    this.activeCoins = activeCoins;
  }

  setCoins(coins) {
    this.activeCoins = coins;
    this.render();
  }

  render() {
    const alerts = StorageService.getAlerts();
    const settings = StorageService.getSettings();

    const alertsHtml = alerts.length === 0 ? `
      <div style="text-align: center; padding: 40px; color: var(--text-muted); background: var(--bg-card); border-radius: var(--radius-md); border: 1px solid var(--border-glass);">
        <p style="font-size: 16px; margin-bottom: 6px;">No Active Price Alerts</p>
        <p style="font-size: 12px;">Create an alert below to receive Windows notifications when prices cross your targets.</p>
      </div>
    ` : alerts.map(a => {
      const conditionSymbol = a.condition === 'above' ? '>' : '<';
      const condColor = a.condition === 'above' ? 'var(--accent-green)' : 'var(--accent-red)';
      return `
        <div class="crypto-card" style="flex-direction: row; justify-content: space-between; align-items: center; padding: 14px;">
          <div style="display: flex; align-items: center; gap: 12px;">
            <span style="font-size: 18px;">🔔</span>
            <div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="font-weight: 800; font-size: 14px;">${a.coinSymbol}</span>
                <span style="font-size: 12px; color: var(--text-muted);">(${a.coinName})</span>
              </div>
              <div style="font-family: var(--font-mono); font-size: 14px; font-weight: 700; margin-top: 2px;">
                When price is <span style="color: ${condColor}; font-weight: 800;">${conditionSymbol}</span> ${formatCurrency(a.targetPrice, a.currency)}
              </div>
            </div>
          </div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <button class="icon-btn btn-toggle-alert" data-id="${a.id}" style="color: ${a.active ? 'var(--accent-green)' : 'var(--text-muted)'};" title="${a.active ? 'Disable' : 'Enable'}">
              ${a.active ? '● Active' : '○ Paused'}
            </button>
            <button class="icon-btn btn-delete-alert" data-id="${a.id}" title="Delete Alert" style="color: var(--accent-red);">
              🗑️
            </button>
          </div>
        </div>
      `;
    }).join('');

    this.container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <h2 style="font-size: 18px; font-weight: 800;">Cryptocurrency Price Alerts</h2>
            <p style="font-size: 12px; color: var(--text-muted);">Desktop native notifications trigger automatically when market thresholds are met</p>
          </div>
        </div>

        <!-- Add New Alert Card -->
        <div style="background: var(--bg-card); padding: 16px; border-radius: var(--radius-md); border: 1px solid var(--border-glass); display: flex; flex-direction: column; gap: 12px;">
          <span style="font-size: 14px; font-weight: 700;">Create New Price Alert</span>
          <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)) auto; gap: 10px; align-items: flex-end;">
            <div class="input-group">
              <label class="input-label">Cryptocurrency</label>
              <select class="select-input" id="alert-coin-select">
                ${this.activeCoins.map(c => `
                  <option value="${c.id}" data-symbol="${c.symbol}" data-name="${c.name}">${c.name} (${c.symbol.toUpperCase()})</option>
                `).join('')}
              </select>
            </div>

            <div class="input-group">
              <label class="input-label">Condition</label>
              <select class="select-input" id="alert-condition-select">
                <option value="above">Rises Above (&gt;)</option>
                <option value="below">Drops Below (&lt;)</option>
              </select>
            </div>

            <div class="input-group">
              <label class="input-label">Target Price</label>
              <input type="number" step="any" class="text-input" id="alert-price-input" placeholder="e.g. 10000000" />
            </div>

            <div class="input-group">
              <label class="input-label">Currency</label>
              <select class="select-input" id="alert-currency-select">
                <option value="inr" ${settings.primaryCurrency === 'inr' ? 'selected' : ''}>INR (₹)</option>
                <option value="usd" ${settings.primaryCurrency === 'usd' ? 'selected' : ''}>USD ($)</option>
                <option value="eur">EUR (€)</option>
                <option value="gbp">GBP (£)</option>
              </select>
            </div>

            <button class="btn-primary" id="btn-save-alert" style="height: 38px;">
              + Add Alert
            </button>
          </div>
        </div>

        <!-- Active Alerts List -->
        <div style="display: flex; flex-direction: column; gap: 8px;">
          ${alertsHtml}
        </div>
      </div>
    `;

    this.bindEvents();
  }

  bindEvents() {
    const saveBtn = this.container.querySelector('#btn-save-alert');
    if (saveBtn) {
      saveBtn.addEventListener('click', () => {
        const coinSelect = this.container.querySelector('#alert-coin-select');
        const condSelect = this.container.querySelector('#alert-condition-select');
        const priceInput = this.container.querySelector('#alert-price-input');
        const currSelect = this.container.querySelector('#alert-currency-select');

        const coinId = coinSelect.value;
        const selectedOption = coinSelect.options[coinSelect.selectedIndex];
        const coinName = selectedOption.dataset.name || coinId;
        const coinSymbol = selectedOption.dataset.symbol || coinId;
        const condition = condSelect.value;
        const targetPrice = parseFloat(priceInput.value);
        const currency = currSelect.value;

        if (isNaN(targetPrice) || targetPrice <= 0) {
          alert('Please enter a valid target price.');
          return;
        }

        StorageService.addAlert({
          coinId,
          coinName,
          coinSymbol,
          condition,
          targetPrice,
          currency
        });

        priceInput.value = '';
        this.render();
      });
    }

    this.container.querySelectorAll('.btn-delete-alert').forEach(btn => {
      btn.addEventListener('click', () => {
        StorageService.deleteAlert(btn.dataset.id);
        this.render();
      });
    });

    this.container.querySelectorAll('.btn-toggle-alert').forEach(btn => {
      btn.addEventListener('click', () => {
        StorageService.toggleAlert(btn.dataset.id);
        this.render();
      });
    });
  }
}
