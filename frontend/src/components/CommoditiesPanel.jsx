import { useState, useEffect } from 'react';
import { getTopCommodities } from '../services/api';

function Sparkline({ data, positive }) {
  if (!data || data.length < 2) {
    return <div className="sparkline-placeholder"></div>;
  }
  
  const min = Math.min(...data);
  const max = Math.max(...data);
  const range = max - min || 1;
  const height = 24;
  const width = 60;
  
  const points = data.map((val, i) => {
    const x = (i / (data.length - 1)) * width;
    const y = height - ((val - min) / range) * height;
    return `${x},${y}`;
  }).join(' ');
  
  const color = positive ? '#27ae60' : '#e74c3c';
  
  return (
    <svg width={width} height={height} className="sparkline">
      <polyline
        fill="none"
        stroke={color}
        strokeWidth="1.5"
        points={points}
      />
    </svg>
  );
}

export default function CommoditiesPanel() {
  const [commodities, setCommodities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedCommodity, setExpandedCommodity] = useState(null);

  useEffect(() => {
    loadCommodities();
    
    const interval = setInterval(loadCommodities, 300000);
    return () => clearInterval(interval);
  }, []);

  async function loadCommodities() {
    setLoading(true);
    try {
      const data = await getTopCommodities();
      setCommodities(data.slice(0, 15));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  const handleCommodityClick = (id) => {
    setExpandedCommodity(expandedCommodity === id ? null : id);
  };

  const formatNumber = (num, decimals = 2) => {
    if (num == null) return '-';
    return num.toFixed(decimals);
  };

  const formatVolume = (vol) => {
    if (!vol) return '-';
    if (vol >= 1000000) return (vol / 1000000).toFixed(1) + 'M';
    if (vol >= 1000) return (vol / 1000).toFixed(1) + 'K';
    return vol.toString();
  };

  if (loading && commodities.length === 0) {
    return <div className="panel-loading">Loading commodities...</div>;
  }

  return (
    <div className="commodities-panel">
      <h2>Commodity Prices</h2>
      <div className="commodities-list">
        {commodities.map(item => (
          <div 
            key={item.id} 
            className={`commodity-item ${expandedCommodity === item.id ? 'expanded' : ''}`}
            onClick={() => handleCommodityClick(item.id)}
          >
            <div className="commodity-header">
              <div className="commodity-name">{item.name}</div>
              <div className="commodity-symbol">({item.symbol})</div>
            </div>
            
            <div className="commodity-main">
              <div className="commodity-price">
                ${formatNumber(item.price)}
              </div>
              <div className={`commodity-change ${item.changePercent >= 0 ? 'positive' : 'negative'}`}>
                {item.changePercent >= 0 ? '+' : ''}{formatNumber(item.changePercent)}%
              </div>
              <Sparkline data={[1,2,3,4,5]} positive={item.changePercent >= 0} />
            </div>
            
            {expandedCommodity === item.id && (
              <div className="commodity-details">
                <div className="detail-row">
                  <span className="detail-label">Open:</span>
                  <span className="detail-value">${formatNumber(item.open)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">High:</span>
                  <span className="detail-value">${formatNumber(item.high)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Low:</span>
                  <span className="detail-value">${formatNumber(item.low)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Prev Close:</span>
                  <span className="detail-value">${formatNumber(item.previousClose)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Volume:</span>
                  <span className="detail-value">{formatVolume(item.volume)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Change:</span>
                  <span className="detail-value">{item.priceChange >= 0 ? '+' : ''}{formatNumber(item.priceChange)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Unit:</span>
                  <span className="detail-value">{item.unit || '-'}</span>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
