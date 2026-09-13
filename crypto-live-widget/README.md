# CryptoLive Widget 🚀
> Professional Windows 10/11 Desktop Cryptocurrency Price Widget & Market Tracker

A lightweight, high-performance Windows desktop application built with Electron, HTML5, CSS3, and JavaScript that displays live cryptocurrency prices, interactive sparklines, detailed market metrics, and customizable desktop widget modes.

---

## 🌟 Key Features

- **Real-Time Live Market Data**: CoinGecko API integration with automatic rate-limit handling, offline fallback cache, and configurable 15–30s refresh intervals.
- **Top 5 Selection & Customization**:
  - Default: **Bitcoin (BTC)**, **Ethereum (ETH)**, **Tether (USDT)**, **BNB (BNB)**, and **Solana (SOL)**.
  - Search any of 10,000+ cryptocurrencies by name or ticker symbol.
  - Drag-and-drop to reorder your top 5 coins.
  - Replace any coin easily with one click.
- **Dual Display Currency**:
  - Primary: **INR (₹)** with Indian formatting (Lakhs / Crores).
  - Secondary: **USD ($)**, with support for **EUR (€)** and **GBP (£)**.
- **Desktop Floating Widget Mode**:
  - Always-on-top window pinning (`screen-saver` level).
  - Frameless glassmorphic appearance with adjustable opacity slider (30%–100%).
  - Compact mode (minimal ticker view) & Expanded mode (sparklines & metrics).
  - Remembers window position and coordinates across restarts.
- **In-Depth Coin Analytics**:
  - 1H, 24H, 7D, 30D, and 1Y price percentage change metrics.
  - Interactive Canvas chart with 1H, 24H, 7D, 30D, 1Y, and MAX ranges.
  - 24H High/Low, Market Capitalization, Volume/Market Cap ratio, FDV, Circulating Supply, Max Supply, and All-Time High/Low dates.
- **Smart Price Alerts**:
  - Set custom target thresholds (e.g., `BTC > ₹10,000,000` or `ETH < ₹300,000`).
  - Native Windows desktop toast notifications.
  - Optional audio chime synthesized via Web Audio API.
- **Windows System Tray Integration**:
  - Minimizes to system notification tray.
  - Right-click tray menu: Open Dashboard, Show Widget, Hide Widget, Refresh Prices, Settings, Exit.
- **Lightweight & Battery Efficient**:
  - Throttled API calls with caching to prevent 429 rate-limiting.
  - Pure zero-framework DOM components with hardware-accelerated CSS.

---

## 📂 Project Structure

```
crypto-live-widget/
├── electron/
│   ├── main.js        # Electron main process (window geometry, IPC, tray, startup)
│   ├── preload.js     # Secure contextBridge API bindings
│   └── tray.js        # Windows system-tray menu & notification handlers
├── src/
│   ├── api/
│   │   └── coingecko.js   # REST API client with throttling & offline caching
│   ├── components/
│   │   ├── dashboard.js   # Top 5 grid, drag-and-drop reorder, coin manager
│   │   └── widget.js      # Compact & expanded desktop widget renderer
│   ├── pages/
│   │   ├── coin-details.js # Detailed statistics & interactive performance chart
│   │   ├── alerts-view.js  # Price alert creation, toggle, and management
│   │   └── settings.js     # Application, widget, currency, and theme settings
│   ├── services/
│   │   ├── storage.js     # Local persistence for coins, settings, and alerts
│   │   └── alerts.js      # Alert evaluation engine & sound notification trigger
│   ├── styles/
│   │   └── main.css       # Dark fintech glassmorphism theme & animations
│   ├── utils/
│   │   └── formatters.js  # Indian INR formatting, currency conversion, SVG sparklines
│   ├── app.js             # Client bootstrap & navigation router
│   └── index.html         # Frameless window layout & container
├── package.json           # Scripts and build configuration for electron-builder
└── README.md              # Installation and build instructions
```

---

## 🛠️ Prerequisites

Ensure you have Node.js and npm installed on your Windows machine:
- **Node.js**: `v18.0.0` or later (LTS recommended) -> [nodejs.org](https://nodejs.org/)
- **npm**: `v9.0.0` or later

Verify in PowerShell or Command Prompt:
```bash
node -v
npm -v
```

---

## 🚀 How to Install Dependencies

1. Open PowerShell, Command Prompt, or Windows Terminal.
2. Navigate to the `crypto-live-widget` directory:
   ```bash
   cd crypto-live-widget
   ```
3. Install all required dependencies:
   ```bash
   npm install
   ```

---

## 💻 How to Run in Development Mode

Run the following command to launch CryptoLive Widget in development mode with live console logs:

```bash
npm run dev
# or
npm start
```

---

## 📦 How to Build the Windows Installer (".exe")

CryptoLive Widget is configured with `electron-builder` to generate both an NSIS Setup Installer and a standalone Portable `.exe`.

### 1. Build the Standard NSIS Installer (.exe)
```bash
npm run build
```
This compiles the application and outputs the setup executable into the `dist/` folder:
- **Output file**: `dist/CryptoLive Widget Setup 1.0.0.exe`

### 2. Build the Portable Executable (No installation required)
```bash
npm run build:portable
```
- **Output file**: `dist/CryptoLive Widget 1.0.0.exe` (Run directly from any USB drive or desktop folder)

---

## 🖥️ How to Install the Generated Application

1. Open File Explorer and navigate to the `crypto-live-widget/dist/` directory.
2. Double-click **`CryptoLive Widget Setup 1.0.0.exe`**.
3. Follow the installation wizard:
   - Choose the installation folder (or accept default `C:\Users\<User>\AppData\Local\Programs\crypto-live-widget`).
   - Leave "Create Desktop Shortcut" checked.
4. Click **Finish** to immediately launch the app.
5. The CryptoLive Widget icon will appear on your desktop and in your Windows System Tray (near the clock).

---

## 💾 Where User Settings are Stored

CryptoLive Widget stores user preferences in two secure local locations:

1. **Window Geometries & Native Flags**:
   - Stored in: `%APPDATA%\crypto-live-widget\window-state.json`
   - Full path: `C:\Users\<YourUsername>\AppData\Roaming\crypto-live-widget\window-state.json`
   - Keeps track of:
     - Window position (`x`, `y`)
     - Window size (`width`, `height`)
     - Always-on-top state
     - Opacity preference

2. **Top 5 Coins, Alerts, Currency & UI Settings**:
   - Stored in standard HTML5 LocalStorage inside the isolated Electron user data directory:
     - `cryptolive_top_5_coins`: Array of coin IDs (e.g. `["bitcoin", "ethereum", "tether", "binancecoin", "solana"]`)
     - `cryptolive_settings`: JSON object with refresh intervals, currencies, themes, and modes
     - `cryptolive_alerts`: Array of user price alert targets

---

## 🔄 How to Change the Default Cryptocurrencies

There are two easy ways to change the coins:

### Method A: Directly inside the UI
1. Click the **"⚙️ Edit Selection (5/5)"** button in the Top 5 Dashboard, or click the **⋮ (three dots)** icon on any coin card.
2. In the modal that opens, type any coin name or ticker into the search bar (e.g., `XRP`, `Cardano`, `Dogecoin`, `Avalanche`).
3. Click **Select** or **Add / Replace**. The app will immediately fetch live quotes for your new coin and save your selection permanently.
4. You can also **drag and drop** the cards on the dashboard to change their order!

### Method B: Programmatically in Code
If you want to modify the factory default coins that load on initial start:
1. Open `src/services/storage.js`.
2. Locate line 10:
   ```javascript
   const DEFAULT_COINS = ['bitcoin', 'ethereum', 'tether', 'binancecoin', 'solana'];
   ```
3. Change the IDs to any CoinGecko cryptocurrency ID (e.g., `['bitcoin', 'ripple', 'cardano', 'dogecoin', 'chainlink']`).
4. Save the file.

---

## 🛡️ License
MIT License. Built for crypto enthusiasts and professional traders.
