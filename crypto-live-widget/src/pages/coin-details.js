/**
 * Coin Details and Historical Chart Page
 */
import { formatCurrency, formatCompactCurrency, formatPercentage } from '../utils/formatters.js';
import { cryptoApi } from '../api/coingecko.js';

export class CoinDetailsPage {
  constructor(container, onBack) {
    this.container = container;
    this.onBack = onBack;
    this.coin = null;
    this.currency = 'inr';
    this.currentRange = '24h';
    this.chartData = [];
    this.isLoadingChart = false;
  }

  show(coin, currency = 'inr') {
    this.coin = coin;
    this.currency = currency;
    this.render();
    this.loadChart(this.currentRange);
  }

  async loadChart(range) {
    if (!this.coin) return;
    this.currentRange = range;
    this.isLoadingChart = true;
    this.renderChartState();

    try {
      const prices = await cryptoApi.getCoinMarketChart(this.coin.id, this.currency, range);
      this.chartData = prices || [];
    } catch (e) {
      console.warn('Failed to load chart data:', e);
      this.chartData = [];
    } finally {
      this.isLoadingChart = false;
      this.drawCanvasChart();
    }
  }

  renderChartState() {
    const canvas = this.container.querySelector('#coin-chart-canvas');
    const loadingOverlay = this.container.querySelector('#chart-loading-indicator');
    if (loadingOverlay) {
      loadingOverlay.style.display = this.isLoadingChart ? 'flex' : 'none';
    }
  }

  drawCanvasChart() {
    const canvas = this.container.querySelector('#coin-chart-canvas');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const width = canvas.width = canvas.parentElement.clientWidth || 600;
    const height = canvas.height = 240;

    ctx.clearRect(0, 0, width, height);

    if (this.chartData.length < 2) {
      ctx.fillStyle = '#64748B';
      ctx.font = '13px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText('Historical chart data unavailable for this range', width / 2, height / 2);
      return;
    }

    const prices = this.chartData.map(p => p[1]);
    const minPrice = Math.min(...prices);
    const maxPrice = Math.max(...prices);
    const range = maxPrice - minPrice || 1;

    const isPositive = prices[prices.length - 1] >= prices[0];
    const strokeColor = isPositive ? '#10B981' : '#EF4444';
    const fillTop = isPositive ? 'rgba(16, 185, 129, 0.28)' : 'rgba(239, 68, 68, 0.28)';
    const fillBottom = 'rgba(0, 0, 0, 0.0)';

    const padding = { top: 20, bottom: 25, left: 10, right: 10 };
    const chartW = width - padding.left - padding.right;
    const chartH = height - padding.top - padding.bottom;

    // Build curve points
    const step = chartW / (prices.length - 1);
    const points = prices.map((price, i) => {
      const x = padding.left + i * step;
      const y = padding.top + chartH - ((price - minPrice) / range) * chartH;
      return { x, y };
    });

    // Draw background gradient area
    const gradient = ctx.createLinearGradient(0, padding.top, 0, height - padding.bottom);
    gradient.addColorStop(0, fillTop);
    gradient.addColorStop(1, fillBottom);

    ctx.beginPath();
    ctx.moveTo(points[0].x, height - padding.bottom);
    points.forEach(p => ctx.lineTo(p.x, p.y));
    ctx.lineTo(points[points.length - 1].x, height - padding.bottom);
    ctx.closePath();
    ctx.fillStyle = gradient;
    ctx.fill();

    // Draw main line
    ctx.beginPath();
    ctx.moveTo(points[0].x, points[0].y);
    points.forEach(p => ctx.lineTo(p.x, p.y));
    ctx.strokeStyle = strokeColor;
    ctx.lineWidth = 2.5;
    ctx.stroke();

    // Draw min / max reference labels
    ctx.fillStyle = '#94A3B8';
    ctx.font = '11px monospace';
    ctx.textAlign = 'right';
    ctx.fillText(formatCompactCurrency(maxPrice, this.currency), width - 12, padding.top + 12);
    ctx.fillText(formatCompactCurrency(minPrice, this.currency), width - 12, height - padding.bottom - 4);
  }

  render() {
    if (!this.coin) return;
    const c = this.coin;

    const change1h = c.price_change_percentage_1h_in_currency || 0;
    const change24h = c.price_change_percentage_24h || 0;
    const change7d = c.price_change_percentage_7d_in_currency || 0;
    const change30d = c.price_change_percentage_30d_in_currency || 0;
    const change1y = c.price_change_percentage_1y_in_currency || 0;

    const formatChangeBadge = (val) => {
      const cls = val > 0 ? 'positive' : (val < 0 ? 'negative' : 'neutral');
      const arrow = val > 0 ? '▲' : (val < 0 ? '▼' : '•');
      return `<span class="badge-change ${cls}">${arrow} ${formatPercentage(val)}</span>`;
    };

    this.container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 16px;">
        <!-- Top Back and Header -->
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <button class="btn-secondary" id="btn-back-dashboard" style="display: inline-flex; align-items: center; gap: 6px;">
            ← Back to Top 5
          </button>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span class="card-rank-badge">Rank #${c.market_cap_rank || '—'}</span>
          </div>
        </div>

        <!-- Coin Identity Hero -->
        <div style="display: flex; justify-content: space-between; align-items: flex-end; background: var(--bg-card); padding: 20px; border-radius: var(--radius-md); border: 1px solid var(--border-glass);">
          <div style="display: flex; align-items: center; gap: 14px;">
            <img src="${c.image}" style="width: 48px; height: 48px; border-radius: 50%;" />
            <div>
              <h1 style="font-size: 22px; font-weight: 800;">${c.name} <span style="color: var(--text-muted); font-size: 15px; font-weight: 600;">(${c.symbol.toUpperCase()})</span></h1>
              <div style="display: flex; align-items: center; gap: 10px; margin-top: 4px;">
                <span style="font-size: 26px; font-weight: 800; font-family: var(--font-mono);">${formatCurrency(c.current_price, this.currency)}</span>
                ${formatChangeBadge(change24h)}
              </div>
            </div>
          </div>
          <div style="text-align: right;">
            <div style="font-size: 12px; color: var(--text-muted);">24H Range</div>
            <div style="font-size: 13px; font-weight: 700; font-family: var(--font-mono); color: var(--text-secondary); margin-top: 2px;">
              ${formatCompactCurrency(c.low_24h, this.currency)} — ${formatCompactCurrency(c.high_24h, this.currency)}
            </div>
          </div>
        </div>

        <!-- Interactive Chart Card -->
        <div style="background: var(--bg-card); padding: 16px; border-radius: var(--radius-md); border: 1px solid var(--border-glass); display: flex; flex-direction: column; gap: 12px;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 14px; font-weight: 700;">Price Performance Chart</span>
            <div class="time-range-bar">
              ${['1h', '24h', '7d', '30d', '1y', 'max'].map(r => `
                <button class="range-btn ${this.currentRange === r ? 'active' : ''}" data-range="${r}">${r.toUpperCase()}</button>
              `).join('')}
            </div>
          </div>
          <div style="position: relative; height: 240px; width: 100%;">
            <canvas id="coin-chart-canvas"></canvas>
            <div id="chart-loading-indicator" style="position: absolute; inset: 0; background: rgba(15,23,42,0.7); display: none; align-items: center; justify-content: center; font-size: 13px; color: var(--accent-green); font-weight: 600;">
              Fetching chart telemetry...
            </div>
          </div>
        </div>

        <!-- Performance Over Time (1H, 24H, 7D, 30D, 1Y) -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(110px, 1fr)); gap: 10px;">
          <div class="crypto-card" style="padding: 12px; align-items: center; text-align: center;">
            <span style="font-size: 11px; color: var(--text-muted); font-weight: 600;">1 HOUR</span>
            <div style="margin-top: 4px;">${formatChangeBadge(change1h)}</div>
          </div>
          <div class="crypto-card" style="padding: 12px; align-items: center; text-align: center;">
            <span style="font-size: 11px; color: var(--text-muted); font-weight: 600;">24 HOURS</span>
            <div style="margin-top: 4px;">${formatChangeBadge(change24h)}</div>
          </div>
          <div class="crypto-card" style="padding: 12px; align-items: center; text-align: center;">
            <span style="font-size: 11px; color: var(--text-muted); font-weight: 600;">7 DAYS</span>
            <div style="margin-top: 4px;">${formatChangeBadge(change7d)}</div>
          </div>
          <div class="crypto-card" style="padding: 12px; align-items: center; text-align: center;">
            <span style="font-size: 11px; color: var(--text-muted); font-weight: 600;">30 DAYS</span>
            <div style="margin-top: 4px;">${formatChangeBadge(change30d)}</div>
          </div>
          <div class="crypto-card" style="padding: 12px; align-items: center; text-align: center;">
            <span style="font-size: 11px; color: var(--text-muted); font-weight: 600;">1 YEAR</span>
            <div style="margin-top: 4px;">${formatChangeBadge(change1y)}</div>
          </div>
        </div>

        <!-- Comprehensive Market & Supply Statistics -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
          <!-- Market Capitalization Stats -->
          <div style="background: var(--bg-card); padding: 16px; border-radius: var(--radius-md); border: 1px solid var(--border-glass); display: flex; flex-direction: column; gap: 10px;">
            <h3 style="font-size: 13px; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px;">Market Valuation</h3>
            <div style="display: flex; justify-content: space-between; font-size: 13px; padding-bottom: 6px; border-bottom: 1px solid var(--border-glass);">
              <span style="color: var(--text-muted);">Market Cap</span>
              <span style="font-weight: 700; font-family: var(--font-mono);">${formatCurrency(c.market_cap, this.currency)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13px; padding-bottom: 6px; border-bottom: 1px solid var(--border-glass);">
              <span style="color: var(--text-muted);">Fully Diluted Valuation (FDV)</span>
              <span style="font-weight: 700; font-family: var(--font-mono);">${formatCurrency(c.fully_diluted_valuation, this.currency)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13px; padding-bottom: 6px; border-bottom: 1px solid var(--border-glass);">
              <span style="color: var(--text-muted);">24H Trading Volume</span>
              <span style="font-weight: 700; font-family: var(--font-mono);">${formatCurrency(c.total_volume, this.currency)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13px;">
              <span style="color: var(--text-muted);">Volume / Market Cap</span>
              <span style="font-weight: 700; font-family: var(--font-mono);">
                ${c.market_cap ? (c.total_volume / c.market_cap).toFixed(4) : '—'}
              </span>
            </div>
          </div>

          <!-- Token Supply & All-Time Stats -->
          <div style="background: var(--bg-card); padding: 16px; border-radius: var(--radius-md); border: 1px solid var(--border-glass); display: flex; flex-direction: column; gap: 10px;">
            <h3 style="font-size: 13px; font-weight: 700; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.5px;">Supply & Records</h3>
            <div style="display: flex; justify-content: space-between; font-size: 13px; padding-bottom: 6px; border-bottom: 1px solid var(--border-glass);">
              <span style="color: var(--text-muted);">Circulating Supply</span>
              <span style="font-weight: 700; font-family: var(--font-mono);">${c.circulating_supply ? Number(c.circulating_supply).toLocaleString() : '—'}</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13px; padding-bottom: 6px; border-bottom: 1px solid var(--border-glass);">
              <span style="color: var(--text-muted);">Total Supply</span>
              <span style="font-weight: 700; font-family: var(--font-mono);">${c.total_supply ? Number(c.total_supply).toLocaleString() : '—'}</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13px; padding-bottom: 6px; border-bottom: 1px solid var(--border-glass);">
              <span style="color: var(--text-muted);">Max Supply</span>
              <span style="font-weight: 700; font-family: var(--font-mono);">${c.max_supply ? Number(c.max_supply).toLocaleString() : '∞'}</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13px; padding-bottom: 6px; border-bottom: 1px solid var(--border-glass);">
              <span style="color: var(--text-muted);">All-Time High (ATH)</span>
              <span style="font-weight: 700; font-family: var(--font-mono); color: var(--accent-green);">${formatCurrency(c.ath, this.currency)} <small style="color: var(--text-muted);">(${new Date(c.ath_date || Date.now()).toLocaleDateString()})</small></span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13px;">
              <span style="color: var(--text-muted);">All-Time Low (ATL)</span>
              <span style="font-weight: 700; font-family: var(--font-mono); color: var(--accent-red);">${formatCurrency(c.atl, this.currency)} <small style="color: var(--text-muted);">(${new Date(c.atl_date || Date.now()).toLocaleDateString()})</small></span>
            </div>
          </div>
        </div>
      </div>
    `;

    this.bindEvents();
  }

  bindEvents() {
    const backBtn = this.container.querySelector('#btn-back-dashboard');
    if (backBtn) {
      backBtn.addEventListener('click', () => {
        if (this.onBack) this.onBack();
      });
    }

    const rangeBtns = this.container.querySelectorAll('.range-btn');
    rangeBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        rangeBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const range = btn.dataset.range;
        this.loadChart(range);
      });
    });

    window.addEventListener('resize', () => {
      this.drawCanvasChart();
    });
  }
}
