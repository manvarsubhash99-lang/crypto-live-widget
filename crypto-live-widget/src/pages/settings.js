/**
 * Settings Page Component
 */
import { StorageService } from '../services/storage.js';

export class SettingsPage {
  constructor(container, onSettingsChanged) {
    this.container = container;
    this.onSettingsChanged = onSettingsChanged;
  }

  render() {
    const s = StorageService.getSettings();

    this.container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 20px; max-width: 800px; margin: 0 auto;">
        <div>
          <h2 style="font-size: 20px; font-weight: 800;">Application Settings</h2>
          <p style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">Customize desktop behavior, widget aesthetics, currency displays, and alerts.</p>
        </div>

        <!-- General Section -->
        <div style="background: var(--bg-card); padding: 18px; border-radius: var(--radius-md); border: 1px solid var(--border-glass); display: flex; flex-direction: column; gap: 14px;">
          <h3 style="font-size: 14px; font-weight: 700; color: var(--accent-cyan); text-transform: uppercase; letter-spacing: 0.5px;">General</h3>
          
          <label style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Launch on Windows startup</div>
              <div style="font-size: 11px; color: var(--text-muted);">Automatically launch CryptoLive Widget when Windows boots</div>
            </div>
            <input type="checkbox" id="setting-startOnBoot" ${s.startOnBoot ? 'checked' : ''} style="accent-color: var(--accent-green); width: 18px; height: 18px; cursor: pointer;"/>
          </label>

          <label style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Start Minimized</div>
              <div style="font-size: 11px; color: var(--text-muted);">Launch directly into the background or system tray without opening window</div>
            </div>
            <input type="checkbox" id="setting-startMinimized" ${s.startMinimized ? 'checked' : ''} style="accent-color: var(--accent-green); width: 18px; height: 18px; cursor: pointer;"/>
          </label>

          <label style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Always on Top</div>
              <div style="font-size: 11px; color: var(--text-muted);">Keep widget hovering above all other active desktop windows</div>
            </div>
            <input type="checkbox" id="setting-alwaysOnTop" ${s.alwaysOnTop ? 'checked' : ''} style="accent-color: var(--accent-green); width: 18px; height: 18px; cursor: pointer;"/>
          </label>

          <label style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Show System Tray Icon</div>
              <div style="font-size: 11px; color: var(--text-muted);">Keep an active icon in the Windows taskbar system notification tray</div>
            </div>
            <input type="checkbox" id="setting-showTrayIcon" ${s.showTrayIcon ? 'checked' : ''} style="accent-color: var(--accent-green); width: 18px; height: 18px; cursor: pointer;"/>
          </label>
        </div>

        <!-- Widget Section -->
        <div style="background: var(--bg-card); padding: 18px; border-radius: var(--radius-md); border: 1px solid var(--border-glass); display: flex; flex-direction: column; gap: 14px;">
          <h3 style="font-size: 14px; font-weight: 700; color: var(--accent-green); text-transform: uppercase; letter-spacing: 0.5px;">Desktop Widget Mode</h3>

          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Widget Mode Style</div>
              <div style="font-size: 11px; color: var(--text-muted);">Choose between clean ultra-compact view or expanded chart view</div>
            </div>
            <select class="select-input" id="setting-widgetMode" style="width: 140px;">
              <option value="compact" ${s.widgetMode === 'compact' ? 'selected' : ''}>Compact Mode</option>
              <option value="expanded" ${s.widgetMode === 'expanded' ? 'selected' : ''}>Expanded Mode</option>
            </select>
          </div>

          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Opacity (${Math.round((s.opacity || 0.95) * 100)}%)</div>
              <div style="font-size: 11px; color: var(--text-muted);">Adjust window transparency level for desktop immersion</div>
            </div>
            <input type="range" id="setting-opacity" min="0.3" max="1" step="0.05" value="${s.opacity || 0.95}" style="accent-color: var(--accent-green); width: 140px;"/>
          </div>

          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Widget Size</div>
              <div style="font-size: 11px; color: var(--text-muted);">Adjust base scale of widget typography and padding</div>
            </div>
            <select class="select-input" id="setting-widgetScale" style="width: 140px;">
              <option value="small" ${s.widgetScale === 'small' ? 'selected' : ''}>Small (Compact)</option>
              <option value="medium" ${s.widgetScale === 'medium' ? 'selected' : ''}>Medium (Standard)</option>
              <option value="large" ${s.widgetScale === 'large' ? 'selected' : ''}>Large</option>
            </select>
          </div>

          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Price Refresh Interval</div>
              <div style="font-size: 11px; color: var(--text-muted);">Automatic quote sync period (Rate-limit safe 15s–60s)</div>
            </div>
            <select class="select-input" id="setting-refreshIntervalSeconds" style="width: 140px;">
              <option value="15" ${s.refreshIntervalSeconds === 15 ? 'selected' : ''}>Every 15 seconds</option>
              <option value="20" ${s.refreshIntervalSeconds === 20 ? 'selected' : ''}>Every 20 seconds (Recommended)</option>
              <option value="30" ${s.refreshIntervalSeconds === 30 ? 'selected' : ''}>Every 30 seconds</option>
              <option value="60" ${s.refreshIntervalSeconds === 60 ? 'selected' : ''}>Every 60 seconds</option>
            </select>
          </div>

          <label style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Show 24H Mini Chart / Sparkline</div>
              <div style="font-size: 11px; color: var(--text-muted);">Display trending vector sparklines inside cards</div>
            </div>
            <input type="checkbox" id="setting-showMiniChart" ${s.showMiniChart ? 'checked' : ''} style="accent-color: var(--accent-green); width: 18px; height: 18px; cursor: pointer;"/>
          </label>

          <label style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Show 24H Percentage Change</div>
              <div style="font-size: 11px; color: var(--text-muted);">Display color-coded positive/negative percentage badges</div>
            </div>
            <input type="checkbox" id="setting-showPercentage" ${s.showPercentage ? 'checked' : ''} style="accent-color: var(--accent-green); width: 18px; height: 18px; cursor: pointer;"/>
          </label>
        </div>

        <!-- Currency & Theme -->
        <div style="background: var(--bg-card); padding: 18px; border-radius: var(--radius-md); border: 1px solid var(--border-glass); display: flex; flex-direction: column; gap: 14px;">
          <h3 style="font-size: 14px; font-weight: 700; color: var(--accent-amber); text-transform: uppercase; letter-spacing: 0.5px;">Currency & Appearance</h3>

          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Primary Display Currency</div>
              <div style="font-size: 11px; color: var(--text-muted);">Default currency symbol and conversion rate</div>
            </div>
            <select class="select-input" id="setting-primaryCurrency" style="width: 140px;">
              <option value="inr" ${s.primaryCurrency === 'inr' ? 'selected' : ''}>INR (₹ - Rupee)</option>
              <option value="usd" ${s.primaryCurrency === 'usd' ? 'selected' : ''}>USD ($ - Dollar)</option>
              <option value="eur" ${s.primaryCurrency === 'eur' ? 'selected' : ''}>EUR (€ - Euro)</option>
              <option value="gbp" ${s.primaryCurrency === 'gbp' ? 'selected' : ''}>GBP (£ - Pound)</option>
            </select>
          </div>

          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Theme Appearance</div>
              <div style="font-size: 11px; color: var(--text-muted);">Fintech dark glassmorphism or high-contrast theme</div>
            </div>
            <select class="select-input" id="setting-theme" style="width: 140px;">
              <option value="dark" ${s.theme === 'dark' ? 'selected' : ''}>Dark Mode (Fintech Glass)</option>
              <option value="light" ${s.theme === 'light' ? 'selected' : ''}>Light Mode</option>
              <option value="system" ${s.theme === 'system' ? 'selected' : ''}>Match Windows System</option>
            </select>
          </div>
        </div>

        <!-- Notifications -->
        <div style="background: var(--bg-card); padding: 18px; border-radius: var(--radius-md); border: 1px solid var(--border-glass); display: flex; flex-direction: column; gap: 14px;">
          <h3 style="font-size: 14px; font-weight: 700; color: #EC4899; text-transform: uppercase; letter-spacing: 0.5px;">Alert Notifications</h3>

          <label style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Enable Price Alerts</div>
              <div style="font-size: 11px; color: var(--text-muted);">Send native Windows desktop notification toasts when targets are met</div>
            </div>
            <input type="checkbox" id="setting-alertsEnabled" ${s.alertsEnabled ? 'checked' : ''} style="accent-color: var(--accent-green); width: 18px; height: 18px; cursor: pointer;"/>
          </label>

          <label style="display: flex; justify-content: space-between; align-items: center; cursor: pointer;">
            <div>
              <div style="font-size: 13px; font-weight: 600;">Notification Chime Sounds</div>
              <div style="font-size: 11px; color: var(--text-muted);">Play audio chime when a price alert triggers</div>
            </div>
            <input type="checkbox" id="setting-alertSounds" ${s.alertSounds ? 'checked' : ''} style="accent-color: var(--accent-green); width: 18px; height: 18px; cursor: pointer;"/>
          </label>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-bottom: 20px;">
          <button class="btn-primary" id="btn-save-all-settings" style="padding: 10px 24px;">
            Save Preferences
          </button>
        </div>
      </div>
    `;

    this.bindEvents();
  }

  bindEvents() {
    const saveBtn = this.container.querySelector('#btn-save-all-settings');
    const opacitySlider = this.container.querySelector('#setting-opacity');

    opacitySlider.addEventListener('input', (e) => {
      const val = parseFloat(e.target.value);
      if (window.cryptoLiveAPI && window.cryptoLiveAPI.setOpacity) {
        window.cryptoLiveAPI.setOpacity(val);
      }
    });

    saveBtn.addEventListener('click', () => {
      const updated = {
        startOnBoot: this.container.querySelector('#setting-startOnBoot').checked,
        startMinimized: this.container.querySelector('#setting-startMinimized').checked,
        alwaysOnTop: this.container.querySelector('#setting-alwaysOnTop').checked,
        showTrayIcon: this.container.querySelector('#setting-showTrayIcon').checked,
        widgetMode: this.container.querySelector('#setting-widgetMode').value,
        opacity: parseFloat(this.container.querySelector('#setting-opacity').value),
        widgetScale: this.container.querySelector('#setting-widgetScale').value,
        refreshIntervalSeconds: parseInt(this.container.querySelector('#setting-refreshIntervalSeconds').value, 10),
        showMiniChart: this.container.querySelector('#setting-showMiniChart').checked,
        showPercentage: this.container.querySelector('#setting-showPercentage').checked,
        primaryCurrency: this.container.querySelector('#setting-primaryCurrency').value,
        theme: this.container.querySelector('#setting-theme').value,
        alertsEnabled: this.container.querySelector('#setting-alertsEnabled').checked,
        alertSounds: this.container.querySelector('#setting-alertSounds').checked
      };

      StorageService.saveSettings(updated);

      // Notify Electron main process of native setting changes
      if (window.cryptoLiveAPI) {
        if (window.cryptoLiveAPI.setAlwaysOnTop) {
          window.cryptoLiveAPI.setAlwaysOnTop(updated.alwaysOnTop);
        }
        if (window.cryptoLiveAPI.setOpacity) {
          window.cryptoLiveAPI.setOpacity(updated.opacity);
        }
        if (window.cryptoLiveAPI.setStartOnBoot) {
          window.cryptoLiveAPI.setStartOnBoot(updated.startOnBoot);
        }
      }

      // Apply theme
      document.documentElement.setAttribute('data-theme', updated.theme);

      alert('Settings saved successfully!');
      if (this.onSettingsChanged) this.onSettingsChanged(updated);
    });
  }
}
