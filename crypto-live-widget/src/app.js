/**
 * CryptoLive Widget - Main Application Entry Point
 */
import { cryptoApi } from './api/coingecko.js';
import { StorageService } from './services/storage.js';
import { alertEngine } from './services/alerts.js';
import { DashboardComponent } from './components/dashboard.js';
import { WidgetComponent } from './components/widget.js';
import { CoinDetailsPage } from './pages/coin-details.js';
import { AlertsView } from './pages/alerts-view.js';
import { SettingsPage } from './pages/settings.js';

class CryptoLiveApp {
  constructor() {
    this.currentView = 'dashboard';
    this.isWidgetMode = false;
    this.coinsData = [];
    this.isFetching = false;
    this.pollTimer = null;

    // DOM Elements
    this.container = document.getElementById('view-container');
    this.mainNavBar = document.getElementById('main-nav-bar');
    this.appTitlebar = document.getElementById('app-titlebar');
    this.currencySelect = document.getElementById('quick-currency-select');
    this.pinOnTopBtn = document.getElementById('btn-pin-ontop');
    this.lastSyncSpan = document.getElementById('last-sync-time');
    this.liveIndicator = document.getElementById('live-indicator');
    this.alertsBadge = document.getElementById('alerts-count-badge');
    this.widgetToggleLabel = document.getElementById('widget-toggle-label');

    // Sub-components
    this.dashboard = new DashboardComponent(
      this.container,
      (coin) => this.showCoinDetails(coin),
      () => this.fetchPrices()
    );

    this.widget = new WidgetComponent(
      this.container,
      (coin) => {
        this.exitWidgetMode();
        this.showCoinDetails(coin);
      },
      () => this.exitWidgetMode()
    );

    this.coinDetails = new CoinDetailsPage(
      this.container,
      () => this.navigateTo('dashboard')
    );

    this.alertsView = new AlertsView(this.container);
    this.settingsPage = new SettingsPage(
      this.container,
      (newSettings) => this.applySettings(newSettings)
    );
  }

  async init() {
    this.loadInitialSettings();
    this.bindGlobalEvents();
    this.bindElectronEvents();
    await this.fetchPrices();
    this.startPolling();
  }

  loadInitialSettings() {
    const s = StorageService.getSettings();
    document.documentElement.setAttribute('data-theme', s.theme || 'dark');
    if (this.currencySelect) {
      this.currencySelect.value = s.primaryCurrency || 'inr';
    }
    if (s.alwaysOnTop && this.pinOnTopBtn) {
      this.pinOnTopBtn.classList.add('active');
    }
    this.updateAlertsCount();
  }

  updateAlertsCount() {
    const alerts = StorageService.getAlerts().filter(a => a.active);
    if (this.alertsBadge) {
      this.alertsBadge.textContent = alerts.length;
    }
  }

  bindGlobalEvents() {
    // Nav tab clicks
    document.querySelectorAll('.nav-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        const targetView = tab.dataset.view;
        this.navigateTo(targetView);
      });
    });

    // Currency selector change
    if (this.currencySelect) {
      this.currencySelect.addEventListener('change', (e) => {
        const s = StorageService.getSettings();
        s.primaryCurrency = e.target.value;
        StorageService.saveSettings(s);
        this.fetchPrices();
      });
    }

    // Always on top toggle
    if (this.pinOnTopBtn) {
      this.pinOnTopBtn.addEventListener('click', () => {
        const s = StorageService.getSettings();
        s.alwaysOnTop = !s.alwaysOnTop;
        StorageService.saveSettings(s);
        this.pinOnTopBtn.classList.toggle('active', s.alwaysOnTop);
        if (window.cryptoLiveAPI && window.cryptoLiveAPI.setAlwaysOnTop) {
          window.cryptoLiveAPI.setAlwaysOnTop(s.alwaysOnTop);
        }
      });
    }

    // Refresh now button
    const refreshBtn = document.getElementById('btn-refresh-quotes');
    if (refreshBtn) {
      refreshBtn.addEventListener('click', () => {
        this.fetchPrices();
      });
    }

    // Widget mode toggle button
    const widgetBtn = document.getElementById('btn-toggle-widget');
    if (widgetBtn) {
      widgetBtn.addEventListener('click', () => {
        if (this.isWidgetMode) {
          this.exitWidgetMode();
        } else {
          this.enterWidgetMode();
        }
      });
    }

    // Window min/max/close
    document.getElementById('btn-win-min')?.addEventListener('click', () => {
      window.cryptoLiveAPI?.minimize();
    });
    document.getElementById('btn-win-max')?.addEventListener('click', () => {
      window.cryptoLiveAPI?.maximize();
    });
    document.getElementById('btn-win-close')?.addEventListener('click', () => {
      window.cryptoLiveAPI?.close();
    });
  }

  bindElectronEvents() {
    if (!window.cryptoLiveAPI) return;

    window.cryptoLiveAPI.onNavigate((destination) => {
      if (destination === 'widget') {
        this.enterWidgetMode();
      } else {
        if (this.isWidgetMode) this.exitWidgetMode();
        this.navigateTo(destination);
      }
    });

    window.cryptoLiveAPI.onRefreshRequested(() => {
      this.fetchPrices();
    });

    window.cryptoLiveAPI.onToggleWidget((state) => {
      this.isWidgetMode = state;
      this.updateWidgetUI();
    });
  }

  enterWidgetMode() {
    this.isWidgetMode = true;
    this.updateWidgetUI();

    if (window.cryptoLiveAPI?.setWidgetMode) {
      window.cryptoLiveAPI.setWidgetMode(true, { width: 340, height: 440 });
    }
  }

  exitWidgetMode() {
    this.isWidgetMode = false;
    this.updateWidgetUI();

    if (window.cryptoLiveAPI?.setWidgetMode) {
      window.cryptoLiveAPI.setWidgetMode(false, { width: 1060, height: 740 });
    }
    this.navigateTo('dashboard');
  }

  updateWidgetUI() {
    if (this.isWidgetMode) {
      this.mainNavBar.style.display = 'none';
      this.appTitlebar.style.display = 'none';
      if (this.widgetToggleLabel) this.widgetToggleLabel.textContent = 'Dashboard';
      const s = StorageService.getSettings();
      this.widget.setData(this.coinsData, s.primaryCurrency);
    } else {
      this.mainNavBar.style.display = 'flex';
      this.appTitlebar.style.display = 'flex';
      if (this.widgetToggleLabel) this.widgetToggleLabel.textContent = 'Widget Mode';
      this.renderCurrentView();
    }
  }

  navigateTo(viewName) {
    this.currentView = viewName;
    document.querySelectorAll('.nav-tab').forEach(tab => {
      tab.classList.toggle('active', tab.dataset.view === viewName);
    });
    this.renderCurrentView();
  }

  showCoinDetails(coin) {
    this.currentView = 'details';
    const s = StorageService.getSettings();
    this.coinDetails.show(coin, s.primaryCurrency);
  }

  renderCurrentView() {
    if (this.isWidgetMode) {
      const s = StorageService.getSettings();
      this.widget.setData(this.coinsData, s.primaryCurrency);
      return;
    }

    const s = StorageService.getSettings();
    switch (this.currentView) {
      case 'dashboard':
        this.dashboard.setData(this.coinsData, s.primaryCurrency, this.warningMessage);
        break;
      case 'alerts':
        this.alertsView.setCoins(this.coinsData);
        break;
      case 'settings':
        this.settingsPage.render();
        break;
      case 'details':
        // Kept handled by showCoinDetails
        break;
      default:
        this.dashboard.setData(this.coinsData, s.primaryCurrency, this.warningMessage);
    }
  }

  async fetchPrices() {
    if (this.isFetching) return;
    this.isFetching = true;

    if (this.liveIndicator) {
      this.liveIndicator.style.backgroundColor = '#F59E0B'; // amber sync
    }

    try {
      const coinIds = StorageService.getTopCoins();
      const s = StorageService.getSettings();
      const response = await cryptoApi.getCoinsMarkets(coinIds, s.primaryCurrency);

      if (response && response.data && response.data.length > 0) {
        this.coinsData = response.data;
        this.warningMessage = response.warning || null;

        // Check active price alerts
        alertEngine.checkPrices(this.coinsData, s.primaryCurrency);
        this.updateAlertsCount();

        const timeStr = new Date().toLocaleTimeString();
        if (this.lastSyncSpan) {
          this.lastSyncSpan.textContent = response.isCached ? `Cached: ${timeStr}` : `Updated: ${timeStr}`;
        }
      } else if (response && response.warning) {
        this.warningMessage = response.warning;
      }
    } catch (e) {
      console.error('Failed to fetch cryptocurrency rates:', e);
      this.warningMessage = 'Unable to update prices — showing last available data.';
    } finally {
      this.isFetching = false;
      if (this.liveIndicator) {
        this.liveIndicator.style.backgroundColor = '#10B981'; // live green
      }
      this.renderCurrentView();
    }
  }

  startPolling() {
    if (this.pollTimer) clearInterval(this.pollTimer);
    const s = StorageService.getSettings();
    const intervalMs = Math.max(15, s.refreshIntervalSeconds || 20) * 1000;
    this.pollTimer = setInterval(() => {
      this.fetchPrices();
    }, intervalMs);
  }

  applySettings(newSettings) {
    this.startPolling();
    this.fetchPrices();
  }
}

// Bootstrap application on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  const app = new CryptoLiveApp();
  app.init();
});
