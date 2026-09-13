const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('cryptoLiveAPI', {
  // Window management
  minimize: () => ipcRenderer.send('window-minimize'),
  maximize: () => ipcRenderer.send('window-maximize'),
  close: () => ipcRenderer.send('window-close'),
  setAlwaysOnTop: (alwaysOnTop) => ipcRenderer.send('set-always-on-top', alwaysOnTop),
  setOpacity: (opacity) => ipcRenderer.send('set-opacity', opacity),
  setWidgetMode: (isWidget, dimensions) => ipcRenderer.send('set-widget-mode', { isWidget, dimensions }),
  toggleWidgetMode: () => ipcRenderer.send('toggle-widget-mode'),
  
  // Window geometry persistence
  saveWindowBounds: (bounds) => ipcRenderer.send('save-window-bounds', bounds),
  getWindowBounds: () => ipcRenderer.invoke('get-window-bounds'),

  // Startup & Tray
  setStartOnBoot: (enable) => ipcRenderer.send('set-start-on-boot', enable),
  getStartOnBoot: () => ipcRenderer.invoke('get-start-on-boot'),

  // Notifications
  showNotification: (title, body) => ipcRenderer.send('show-notification', { title, body }),
  playAlertSound: () => ipcRenderer.send('play-alert-sound'),

  // Event Listeners from Main Process / Tray
  onNavigate: (callback) => {
    const handler = (_event, destination) => callback(destination);
    ipcRenderer.on('navigate', handler);
    return () => ipcRenderer.removeListener('navigate', handler);
  },
  onRefreshRequested: (callback) => {
    const handler = () => callback();
    ipcRenderer.on('refresh-requested', handler);
    return () => ipcRenderer.removeListener('refresh-requested', handler);
  },
  onToggleWidget: (callback) => {
    const handler = (_event, state) => callback(state);
    ipcRenderer.on('toggle-widget', handler);
    return () => ipcRenderer.removeListener('toggle-widget', handler);
  }
});
