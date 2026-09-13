/**
 * CoinGecko API Service
 * Handles live cryptocurrency rates, caching, rate limiting, and fallback offline data.
 */

const BASE_URL = 'https://api.coingecko.com/api/v3';
const CACHE_PREFIX = 'cryptolive_cache_';
const CACHE_TTL_MS = 30000; // 30 seconds fresh cache

class CoinGeckoService {
  constructor() {
    this.lastRequestTime = 0;
    this.minRequestInterval = 2500; // Throttle to prevent 429 rate limit
    this.isOffline = !navigator.onLine;

    window.addEventListener('online', () => { this.isOffline = false; });
    window.addEventListener('offline', () => { this.isOffline = true; });
  }

  async throttle() {
    const now = Date.now();
    const timeSinceLast = now - this.lastRequestTime;
    if (timeSinceLast < this.minRequestInterval) {
      await new Promise(r => setTimeout(r, this.minRequestInterval - timeSinceLast));
    }
    this.lastRequestTime = Date.now();
  }

  /**
   * Fetch market data for a list of coin IDs
   * @param {string[]} coinIds 
   * @param {string} currency 'inr' | 'usd' | 'eur' | 'gbp'
   */
  async getCoinsMarkets(coinIds = ['bitcoin', 'ethereum', 'tether', 'binancecoin', 'solana'], currency = 'inr') {
    const idsString = coinIds.join(',');
    const cacheKey = `${CACHE_PREFIX}markets_${currency}_${idsString}`;

    try {
      await this.throttle();
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 10000);

      const url = `${BASE_URL}/coins/markets?vs_currency=${encodeURIComponent(currency)}&ids=${encodeURIComponent(idsString)}&order=market_cap_desc&per_page=50&page=1&sparkline=true&price_change_percentage=1h,24h,7d,30d,1y`;

      const response = await fetch(url, {
        signal: controller.signal,
        headers: { 'Accept': 'application/json' }
      });

      clearTimeout(timeoutId);

      if (response.status === 429) {
        console.warn('CoinGecko API rate limit reached, returning cached data.');
        const cached = this.getCached(cacheKey);
        if (cached) return { data: cached, isCached: true, warning: 'Rate limit reached — showing last available data.' };
        throw new Error('API Rate limit reached. Please wait a few moments.');
      }

      if (!response.ok) {
        throw new Error(`HTTP Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      if (Array.isArray(data) && data.length > 0) {
        this.setCached(cacheKey, data);
        return { data, isCached: false, timestamp: Date.now() };
      }

      // Empty result fallback
      const cached = this.getCached(cacheKey);
      if (cached) return { data: cached, isCached: true, warning: 'Unable to update prices — showing last available data.' };
      return { data: [], isCached: false };

    } catch (err) {
      console.warn('Network or API Error fetching markets:', err.message);
      const cached = this.getCached(cacheKey);
      if (cached) {
        return {
          data: cached,
          isCached: true,
          warning: 'Unable to update prices — showing last available data.'
        };
      }
      throw err;
    }
  }

  /**
   * Fetch coin market chart points
   * @param {string} coinId 
   * @param {string} currency 
   * @param {string} range '1h' | '24h' | '7d' | '30d' | '1y' | 'max'
   */
  async getCoinMarketChart(coinId, currency = 'inr', range = '24h') {
    const daysMap = {
      '1h': '1',
      '24h': '1',
      '7d': '7',
      '30d': '30',
      '1y': '365',
      'max': 'max'
    };
    const days = daysMap[range] || '1';
    const cacheKey = `${CACHE_PREFIX}chart_${coinId}_${currency}_${range}`;

    try {
      await this.throttle();
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 10000);

      const url = `${BASE_URL}/coins/${encodeURIComponent(coinId)}/market_chart?vs_currency=${encodeURIComponent(currency)}&days=${days}`;
      const response = await fetch(url, { signal: controller.signal });
      clearTimeout(timeoutId);

      if (!response.ok) {
        const cached = this.getCached(cacheKey);
        if (cached) return cached;
        throw new Error(`Chart fetch error: ${response.statusText}`);
      }

      const data = await response.json();
      if (data && data.prices) {
        this.setCached(cacheKey, data.prices);
        return data.prices;
      }
      return [];
    } catch (err) {
      const cached = this.getCached(cacheKey);
      if (cached) return cached;
      return [];
    }
  }

  /**
   * Search for cryptocurrencies by name or symbol
   */
  async searchCoins(query) {
    if (!query || query.trim().length < 2) return [];
    try {
      await this.throttle();
      const url = `${BASE_URL}/search?query=${encodeURIComponent(query.trim())}`;
      const response = await fetch(url);
      if (!response.ok) return [];
      const json = await response.json();
      return (json.coins || []).slice(0, 15);
    } catch (e) {
      console.error('Search coins error:', e);
      return [];
    }
  }

  getCached(key) {
    try {
      const raw = localStorage.getItem(key);
      if (!raw) return null;
      const parsed = JSON.parse(raw);
      return parsed.data;
    } catch (e) {
      return null;
    }
  }

  setCached(key, data) {
    try {
      localStorage.setItem(key, JSON.stringify({
        data,
        timestamp: Date.now()
      }));
    } catch (e) {
      console.warn('LocalStorage write failed (quota exceeded):', e);
    }
  }
}

export const cryptoApi = new CoinGeckoService();
