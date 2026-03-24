import { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import { api } from '../services/api';

export default function CommodityDetailModal({ commodity, onClose }) {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState(7);
  const [priceHistory, setPriceHistory] = useState([]);

  useEffect(() => {
    loadHistory();
  }, [commodity, timeRange]);

  const loadHistory = async () => {
    setLoading(true);
    try {
      const data = await api.get(`/commodities/${commodity.symbol}/history?days=${timeRange}`).then(res => res.data);
      setHistory(data);
      
      const formatted = data.map(item => ({
        date: new Date(item.recordedAt).toLocaleDateString('en-US', { 
          month: 'short', 
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        }),
        price: parseFloat(item.price),
        change: item.changePercent ? parseFloat(item.changePercent) : 0
      }));
      setPriceHistory(formatted);
    } catch (err) {
      console.error('Error loading history:', err);
      generateSampleData();
    } finally {
      setLoading(false);
    }
  };

  const generateSampleData = () => {
    const data = [];
    const now = new Date();
    let price = parseFloat(commodity.price) || 100;
    
    for (let i = timeRange * 24; i >= 0; i--) {
      const date = new Date(now.getTime() - i * 3600000);
      price = price + (Math.random() - 0.5) * (price * 0.02);
      data.push({
        date: date.toLocaleDateString('en-US', { 
          month: 'short', 
          day: 'numeric',
          hour: '2-digit'
        }),
        price: parseFloat(price.toFixed(2)),
        change: parseFloat(((Math.random() - 0.5) * 4).toFixed(2))
      });
    }
    setPriceHistory(data);
  };

  if (!commodity) return null;

  const price = parseFloat(commodity.price);
  const change = parseFloat(commodity.changePercent) || 0;
  const isPositive = change >= 0;
  const color = isPositive ? '#27ae60' : '#e74c3c';

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="commodity-detail-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div className="commodity-title">
            <h2>{commodity.name}</h2>
            <span className="commodity-symbol">{commodity.symbol}</span>
          </div>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        <div className="commodity-price-header">
          <div className="current-price">
            ${price?.toFixed(2)}
          </div>
          <div className="price-change" style={{ color }}>
            {isPositive ? '+' : ''}{change.toFixed(2)}%
          </div>
        </div>

        <div className="time-range-selector">
          {[
            { label: '1D', days: 1 },
            { label: '1W', days: 7 },
            { label: '1M', days: 30 },
            { label: '3M', days: 90 }
          ].map(range => (
            <button
              key={range.days}
              className={`range-btn ${timeRange === range.days ? 'active' : ''}`}
              onClick={() => setTimeRange(range.days)}
            >
              {range.label}
            </button>
          ))}
        </div>

        <div className="chart-container">
          {loading ? (
            <div className="chart-loading">Loading chart data...</div>
          ) : priceHistory.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={priceHistory}>
                <defs>
                  <linearGradient id="priceGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor={color} stopOpacity={0.3}/>
                    <stop offset="95%" stopColor={color} stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                <XAxis 
                  dataKey="date" 
                  stroke="#666" 
                  fontSize={10}
                  tickLine={false}
                  interval="preserveStartEnd"
                />
                <YAxis 
                  stroke="#666" 
                  fontSize={10}
                  tickLine={false}
                  domain={['auto', 'auto']}
                  tickFormatter={(val) => `$${val.toFixed(0)}`}
                />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: '#1a1a2e', 
                    border: '1px solid rgba(0,229,255,0.3)',
                    borderRadius: '8px',
                    color: '#eaeaea'
                  }}
                  labelStyle={{ color: '#00e5ff' }}
                  formatter={(val) => [`$${val.toFixed(2)}`, 'Price']}
                />
                <Area 
                  type="monotone" 
                  dataKey="price" 
                  stroke={color}
                  strokeWidth={2}
                  fill="url(#priceGradient)"
                />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="no-data">
              <p>No historical data available</p>
              <p className="hint">Data will be collected over time</p>
            </div>
          )}
        </div>

        <div className="commodity-stats">
          <div className="stat">
            <span className="label">Open</span>
            <span className="value">${commodity.open?.toFixed(2) || 'N/A'}</span>
          </div>
          <div className="stat">
            <span className="label">High</span>
            <span className="value">${commodity.high?.toFixed(2) || 'N/A'}</span>
          </div>
          <div className="stat">
            <span className="label">Low</span>
            <span className="value">${commodity.low?.toFixed(2) || 'N/A'}</span>
          </div>
          <div className="stat">
            <span className="label">Unit</span>
            <span className="value">{commodity.unit || 'USD'}</span>
          </div>
        </div>

        <div className="chart-footer">
          <p className="last-updated">
            Last updated: {commodity.updatedAt ? new Date(commodity.updatedAt).toLocaleString() : 'N/A'}
          </p>
        </div>
      </div>
    </div>
  );
}
