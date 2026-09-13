/**
 * Local Storage Service for user preferences, top 5 coin selection, and alerts.
 */

const STORAGE_KEYS = {
  TOP_COINS: 'cryptolive_top_5_coins',
  SETTINGS: 'cryptolive_settings',
  ALERTS: 'cryptolive_alerts',
  CACHED_RATES: 'cryptolive_last_rates'
};

const DEFAULT_COINS = ['bitcoin', 'ethereum', 'tether', 'binancecoin', 'solana'];

const DEFAULT_SETTINGS = {
  // General
  startOnBoot: false,
  startMinimized: false,
  alwaysOnTop: false,
  showTrayIcon: true,

  // Widget
  widgetMode: 'compact', // 'compact' | 'expanded'
  opacity: 0.95,
  widgetScale: 'medium', // 'small' | 'medium' | 'large'
  refreshIntervalSeconds: 20,
  showMiniChart: true,
  showPercentage: true,

  // Currency
  primaryCurrency: 'inr',
  secondaryCurrency: 'usd',

  // Appearance
  theme: 'dark', // 'dark' | 'light' | 'system'

  // Notifications
  alertsEnabled: true,
  alertSounds: true
};

export const StorageService = {
  getTopCoins() {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.TOP_COINS);
      if (!raw) return [...DEFAULT_COINS];
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed.slice(0, 5);
      }
    } catch (e) {
      console.error('Failed to load top coins:', e);
    }
    return [...DEFAULT_COINS];
  },

  saveTopCoins(coins) {
    try {
      const valid = coins.slice(0, 5);
      localStorage.setItem(STORAGE_KEYS.TOP_COINS, JSON.stringify(valid));
    } catch (e) {
      console.error('Failed to save top coins:', e);
    }
  },

  getSettings() {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.SETTINGS);
      if (!raw) return { ...DEFAULT_SETTINGS };
      return { ...DEFAULT_SETTINGS, ...JSON.parse(raw) };
    } catch (e) {
      return { ...DEFAULT_SETTINGS };
    }
  },

  saveSettings(settings) {
    try {
      localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(settings));
    } catch (e) {
      console.error('Failed to save settings:', e);
    }
  },

  getAlerts() {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.ALERTS);
      if (!raw) return [];
      return JSON.parse(raw);
    } catch (e) {
      return [];
    }
  },

  saveAlerts(alerts) {
    try {
      localStorage.setItem(STORAGE_KEYS.ALERTS, JSON.stringify(alerts));
    } catch (e) {
      console.error('Failed to save alerts:', e);
    }
  },

  addAlert(alert) {
    const alerts = this.getAlerts();
    const newAlert = {
      id: 'alert_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5),
      coinId: alert.coinId,
      coinSymbol: (alert.coinSymbol || '').toUpperCase(),
      coinName: alert.coinName,
      targetPrice: parseFloat(alert.targetPrice),
      currency: alert.currency || 'inr',
      condition: alert.condition || 'above', // 'above' (>) or 'below' (<)
      active: true,
      created: Date.now()
    };
    alerts.push(newAlert);
    this.saveAlerts(alerts);
    return newAlert;
  },

  deleteAlert(alertId) {
    const alerts = this.getAlerts().filter(a => a.id !== alertId);
    this.saveAlerts(alerts);
  },

  toggleAlert(alertId) {
    const alerts = this.getAlerts().map(a => {
      if (a.id === alertId) return { ...a, active: !a.active };
      return a;
    });
    this.saveAlerts(alerts);
  }
};
