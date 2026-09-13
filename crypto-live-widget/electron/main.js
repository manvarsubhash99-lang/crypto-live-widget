const { app, BrowserWindow, ipcMain, Notification, screen } = require('electron');
const path = require('path');
const fs = require('fs');
const { createTray, destroyTray } = require('./tray');

let mainWindow = null;
let isWidgetMode = false;
let currentOpacity = 0.96;
let currentAlwaysOnTop = false;

// Persistent window state file path
const stateFilePath = path.join(app.getPath('userData'), 'window-state.json');

function loadWindowState() {
  try {
    if (fs.existsSync(stateFilePath)) {
      return JSON.parse(fs.readFileSync(stateFilePath, 'utf8'));
    }
  } catch (err) {
    console.error('Failed to load window state:', err);
  }
  return {
    dashboard: { width: 1060, height: 740, x: undefined, y: undefined },
    widget: { width: 360, height: 480, x: undefined, y: undefined },
    isWidget: false,
    alwaysOnTop: false,
    opacity: 0.96
  };
}

function saveWindowState(state) {
  try {
    const current = loadWindowState();
    const updated = { ...current, ...state };
    fs.writeFileSync(stateFilePath, JSON.stringify(updated, null, 2), 'utf8');
  } catch (err) {
    console.error('Failed to save window state:', err);
  }
}

function createWindow() {
  const savedState = loadWindowState();
  isWidgetMode = savedState.isWidget || false;
  currentAlwaysOnTop = savedState.alwaysOnTop || false;
  currentOpacity = savedState.opacity || 0.96;

  const targetBounds = isWidgetMode ? savedState.widget : savedState.dashboard;

  const primaryDisplay = screen.getPrimaryDisplay();
  const { width: screenWidth, height: screenHeight } = primaryDisplay.workAreaSize;

  const defaultX = targetBounds.x !== undefined ? targetBounds.x : Math.max(0, Math.floor((screenWidth - targetBounds.width) / 2));
  const defaultY = targetBounds.y !== undefined ? targetBounds.y : Math.max(0, Math.floor((screenHeight - targetBounds.height) / 2));

  mainWindow = new BrowserWindow({
    width: targetBounds.width,
    height: targetBounds.height,
    x: defaultX,
    y: defaultY,
    minWidth: isWidgetMode ? 280 : 800,
    minHeight: isWidgetMode ? 200 : 560,
    frame: false,
    transparent: true,
    backgroundColor: '#00000000',
    alwaysOnTop: currentAlwaysOnTop,
    opacity: currentOpacity,
    hasShadow: true,
    resizable: true,
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true,
      sandbox: false
    }
  });

  mainWindow.loadFile(path.join(__dirname, '../src/index.html'));

  mainWindow.once('ready-to-show', () => {
    mainWindow.show();
    if (currentAlwaysOnTop) {
      mainWindow.setAlwaysOnTop(true, 'screen-saver');
    }
  });

  // Track position and resize
  const recordBounds = () => {
    if (!mainWindow || mainWindow.isDestroyed()) return;
    const bounds = mainWindow.getBounds();
    const state = loadWindowState();
    if (isWidgetMode) {
      state.widget = bounds;
    } else {
      state.dashboard = bounds;
    }
    state.isWidget = isWidgetMode;
    state.alwaysOnTop = currentAlwaysOnTop;
    state.opacity = currentOpacity;
    saveWindowState(state);
  };

  mainWindow.on('resize', recordBounds);
  mainWindow.on('move', recordBounds);

  mainWindow.on('close', (event) => {
    if (!app.isQuitting) {
      event.preventDefault();
      mainWindow.hide();
    }
    return false;
  });

  // Create tray
  createTray(mainWindow, () => {
    mainWindow.webContents.send('refresh-requested');
  });
}

// IPC Handlers
ipcMain.on('window-minimize', () => {
  if (mainWindow) mainWindow.minimize();
});

ipcMain.on('window-maximize', () => {
  if (mainWindow) {
    if (mainWindow.isMaximized()) {
      mainWindow.unmaximize();
    } else {
      mainWindow.maximize();
    }
  }
});

ipcMain.on('window-close', () => {
  if (mainWindow) {
    mainWindow.hide();
  }
});

ipcMain.on('set-always-on-top', (_event, alwaysOnTop) => {
  currentAlwaysOnTop = Boolean(alwaysOnTop);
  if (mainWindow) {
    mainWindow.setAlwaysOnTop(currentAlwaysOnTop, currentAlwaysOnTop ? 'screen-saver' : 'normal');
  }
  saveWindowState({ alwaysOnTop: currentAlwaysOnTop });
});

ipcMain.on('set-opacity', (_event, opacity) => {
  currentOpacity = Math.max(0.3, Math.min(1.0, parseFloat(opacity) || 1.0));
  if (mainWindow) {
    mainWindow.setOpacity(currentOpacity);
  }
  saveWindowState({ opacity: currentOpacity });
});

ipcMain.on('set-widget-mode', (_event, { isWidget, dimensions }) => {
  if (!mainWindow) return;
  isWidgetMode = isWidget;
  const state = loadWindowState();

  if (isWidgetMode) {
    // Switch to compact widget bounds
    const w = (dimensions && dimensions.width) || state.widget.width || 360;
    const h = (dimensions && dimensions.height) || state.widget.height || 480;
    mainWindow.setMinimumSize(280, 200);
    mainWindow.setSize(w, h, true);
    mainWindow.setAlwaysOnTop(true, 'screen-saver');
  } else {
    // Switch to full dashboard bounds
    const w = state.dashboard.width || 1060;
    const h = state.dashboard.height || 740;
    mainWindow.setMinimumSize(800, 560);
    mainWindow.setSize(w, h, true);
    mainWindow.setAlwaysOnTop(currentAlwaysOnTop, currentAlwaysOnTop ? 'screen-saver' : 'normal');
  }

  mainWindow.webContents.send('toggle-widget', isWidgetMode);
  state.isWidget = isWidgetMode;
  saveWindowState(state);
});

ipcMain.on('toggle-widget-mode', () => {
  if (!mainWindow) return;
  isWidgetMode = !isWidgetMode;
  ipcMain.emit('set-widget-mode', null, { isWidget: isWidgetMode });
});

ipcMain.on('show-notification', (_event, { title, body }) => {
  if (Notification.isSupported()) {
    const notification = new Notification({
      title: title || 'CryptoLive Alert',
      body: body || 'Price alert threshold triggered!',
      icon: path.join(__dirname, '../src/assets/tray-icon.png')
    });
    notification.show();
    notification.on('click', () => {
      if (mainWindow) {
        mainWindow.show();
        mainWindow.focus();
      }
    });
  }
});

ipcMain.on('set-start-on-boot', (_event, enable) => {
  app.setLoginItemSettings({
    openAtLogin: Boolean(enable),
    path: app.getPath('exe')
  });
});

ipcMain.handle('get-start-on-boot', () => {
  const settings = app.getLoginItemSettings();
  return settings.openAtLogin;
});

ipcMain.handle('get-window-bounds', () => {
  return loadWindowState();
});

// App Lifecycle
app.whenReady().then(() => {
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    } else if (mainWindow) {
      mainWindow.show();
    }
  });
});

app.on('before-quit', () => {
  app.isQuitting = true;
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});
