const { app, Menu, Tray, nativeImage } = require('electron');
const path = require('path');

let tray = null;

function createTray(mainWindow, onRefreshCallback) {
  // Generate a fallback clean icon if asset doesn't exist
  const iconPath = path.join(__dirname, '../src/assets/tray-icon.png');
  let icon;
  try {
    icon = nativeImage.createFromPath(iconPath);
    if (icon.isEmpty()) {
      // Create empty 16x16 icon bitmap for fallback
      icon = nativeImage.createFromBuffer(
        Buffer.from('iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAZElEQVR42mNkQAO/fzP8J4S/f//PQCvN+J8BDBgx1FDEQEw12A2gqBn0BsDEidgAZA1wA/AYgGsA0Q7A446BNICgATAzCHgAZuA1A8IA3C7AA5iIMID4AGZgNWDRBsDFBmgmAgD4L/2/mR07GAAAAABJRU5ErkJggg==', 'base64')
      );
    }
  } catch (e) {
    icon = nativeImage.createEmpty();
  }

  tray = new Tray(icon.resize({ width: 16, height: 16 }));
  tray.setToolTip('CryptoLive Widget - Live Crypto Prices');

  const contextMenu = Menu.buildFromTemplate([
    {
      label: 'Open Dashboard',
      click: () => {
        if (!mainWindow.isVisible()) mainWindow.show();
        mainWindow.focus();
        mainWindow.webContents.send('navigate', 'dashboard');
      }
    },
    {
      label: 'Show Widget',
      click: () => {
        if (!mainWindow.isVisible()) mainWindow.show();
        mainWindow.webContents.send('navigate', 'widget');
      }
    },
    {
      label: 'Hide Widget',
      click: () => {
        mainWindow.hide();
      }
    },
    { type: 'separator' },
    {
      label: 'Refresh Prices',
      click: () => {
        if (onRefreshCallback) onRefreshCallback();
        mainWindow.webContents.send('refresh-requested');
      }
    },
    {
      label: 'Settings',
      click: () => {
        if (!mainWindow.isVisible()) mainWindow.show();
        mainWindow.focus();
        mainWindow.webContents.send('navigate', 'settings');
      }
    },
    { type: 'separator' },
    {
      label: 'Exit CryptoLive',
      click: () => {
        app.isQuitting = true;
        app.quit();
      }
    }
  ]);

  tray.setContextMenu(contextMenu);

  tray.on('double-click', () => {
    if (mainWindow.isVisible()) {
      mainWindow.hide();
    } else {
      mainWindow.show();
      mainWindow.focus();
    }
  });

  return tray;
}

function destroyTray() {
  if (tray) {
    tray.destroy();
    tray = null;
  }
}

module.exports = {
  createTray,
  destroyTray
};
