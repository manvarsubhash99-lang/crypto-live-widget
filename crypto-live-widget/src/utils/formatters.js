/**
 * Formatting and Sparkline Helper Utilities
 */

export const CURRENCY_SYMBOLS = {
  inr: '₹',
  usd: '$',
  eur: '€',
  gbp: '£'
};

export function formatCurrency(amount, currency = 'inr') {
  if (amount === undefined || amount === null || isNaN(amount)) return '—';
  const symbol = CURRENCY_SYMBOLS[currency.toLowerCase()] || '$';

  // Indian format for INR
  if (currency.toLowerCase() === 'inr') {
    return symbol + Number(amount).toLocaleString('en-IN', {
      maximumFractionDigits: amount < 10 ? 4 : 2,
      minimumFractionDigits: 2
    });
  }

  // Western format
  return symbol + Number(amount).toLocaleString('en-US', {
    maximumFractionDigits: amount < 10 ? 4 : 2,
    minimumFractionDigits: 2
  });
}

export function formatCompactCurrency(amount, currency = 'inr') {
  if (amount === undefined || amount === null || isNaN(amount)) return '—';
  const symbol = CURRENCY_SYMBOLS[currency.toLowerCase()] || '$';
  const num = Math.abs(amount);

  if (currency.toLowerCase() === 'inr') {
    if (num >= 1e12) return `${symbol}${(amount / 1e12).toFixed(2)}T`;
    if (num >= 1e7) return `${symbol}${(amount / 1e7).toFixed(2)}Cr`;
    if (num >= 1e5) return `${symbol}${(amount / 1e5).toFixed(2)}L`;
    if (num >= 1e3) return `${symbol}${(amount / 1e3).toFixed(2)}K`;
    return `${symbol}${amount.toFixed(2)}`;
  }

  if (num >= 1e12) return `${symbol}${(amount / 1e12).toFixed(2)}T`;
  if (num >= 1e9) return `${symbol}${(amount / 1e9).toFixed(2)}B`;
  if (num >= 1e6) return `${symbol}${(amount / 1e6).toFixed(2)}M`;
  if (num >= 1e3) return `${symbol}${(amount / 1e3).toFixed(2)}K`;
  return `${symbol}${amount.toFixed(2)}`;
}

export function formatPercentage(val) {
  if (val === undefined || val === null || isNaN(val)) return '0.00%';
  const prefix = val > 0 ? '+' : '';
  return `${prefix}${val.toFixed(2)}%`;
}

/**
 * Generate a responsive SVG path from sparkline points array
 */
export function generateSparklineSVG(points, width = 120, height = 36, isPositive = true) {
  if (!points || points.length < 2) {
    return `<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}"><line x1="0" y1="${height/2}" x2="${width}" y2="${height/2}" stroke="#64748B" stroke-width="1.5" stroke-dasharray="2 2"/></svg>`;
  }

  const min = Math.min(...points);
  const max = Math.max(...points);
  const range = max - min || 1;

  const strokeColor = isPositive ? '#10B981' : '#EF4444';
  const fillColor = isPositive ? 'rgba(16, 185, 129, 0.12)' : 'rgba(239, 68, 68, 0.12)';

  const step = width / (points.length - 1);
  const coords = points.map((p, i) => {
    const x = (i * step).toFixed(1);
    const y = (height - 4 - ((p - min) / range) * (height - 8)).toFixed(1);
    return `${x},${y}`;
  });

  const linePath = `M ${coords.join(' L ')}`;
  const areaPath = `M 0,${height} L ${coords.join(' L ')} L ${width},${height} Z`;

  return `
    <svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" class="sparkline-svg">
      <path d="${areaPath}" fill="${fillColor}" />
      <path d="${linePath}" fill="none" stroke="${strokeColor}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
    </svg>
  `;
}
