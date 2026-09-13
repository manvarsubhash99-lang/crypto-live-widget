/**
 * Price Alert Monitor and Notification Dispatcher
 */
import { StorageService } from './storage.js';

class AlertEngine {
  constructor() {
    this.triggeredAlerts = new Set(); // Prevent spamming notifications in single session
    this.audioContext = null;
  }

  playChime() {
    try {
      if (!this.audioContext) {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        this.audioContext = new AudioCtx();
      }
      if (this.audioContext.state === 'suspended') {
        this.audioContext.resume();
      }

      const osc = this.audioContext.createOscillator();
      const gain = this.audioContext.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, this.audioContext.currentTime); // D5
      osc.frequency.exponentialRampToValueAtTime(880.0, this.audioContext.currentTime + 0.15); // A5

      gain.gain.setValueAtTime(0.3, this.audioContext.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + 0.4);

      osc.connect(gain);
      gain.connect(this.audioContext.destination);

      osc.start();
      osc.stop(this.audioContext.currentTime + 0.45);
    } catch (e) {
      console.warn('Audio chime playback failed:', e);
    }
  }

  checkPrices(coinsList, currentCurrency = 'inr') {
    const settings = StorageService.getSettings();
    if (!settings.alertsEnabled) return [];

    const alerts = StorageService.getAlerts().filter(a => a.active);
    const triggeredList = [];

    alerts.forEach(alert => {
      const coin = coinsList.find(c => c.id === alert.coinId);
      if (!coin) return;

      const currentPrice = coin.current_price;
      if (typeof currentPrice !== 'number') return;

      let triggered = false;
      if (alert.condition === 'above' && currentPrice >= alert.targetPrice) {
        triggered = true;
      } else if (alert.condition === 'below' && currentPrice <= alert.targetPrice) {
        triggered = true;
      }

      const triggerKey = `${alert.id}_${alert.condition}_${Math.floor(currentPrice)}`;
      if (triggered && !this.triggeredAlerts.has(triggerKey)) {
        this.triggeredAlerts.add(triggerKey);

        const currSymbol = alert.currency === 'inr' ? '₹' : (alert.currency === 'usd' ? '$' : '€');
        const formattedPrice = currentPrice.toLocaleString();
        const formattedTarget = alert.targetPrice.toLocaleString();
        const conditionText = alert.condition === 'above' ? 'risen above' : 'dropped below';

        const title = `🚨 Price Alert: ${alert.coinSymbol}`;
        const message = `${alert.coinName} has ${conditionText} ${currSymbol}${formattedTarget}! Current price: ${currSymbol}${formattedPrice}`;

        // Windows Notification via Electron Preload bridge
        if (window.cryptoLiveAPI && window.cryptoLiveAPI.showNotification) {
          window.cryptoLiveAPI.showNotification(title, message);
        } else if (Notification.permission === 'granted') {
          new Notification(title, { body: message });
        }

        // Sound
        if (settings.alertSounds) {
          this.playChime();
        }

        triggeredList.push({
          alert,
          currentPrice,
          message,
          time: new Date().toLocaleTimeString()
        });
      }
    });

    return triggeredList;
  }
}

export const alertEngine = new AlertEngine();
