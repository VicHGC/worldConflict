import { useState, useEffect } from 'react';
import ConflictMap from './components/ConflictMap';
import NewsPanel from './components/NewsPanel';
import CommoditiesPanel from './components/CommoditiesPanel';
import AuthModal from './components/AuthModal';
import LoginPage from './pages/LoginPage';
import { getNews, getCommodities, getZones } from './services/api';
import './App.css';

function App() {
  const [selectedZone, setSelectedZone] = useState(null);
  const [stats, setStats] = useState({ totalZones: 0, totalNews: 0, totalCommodities: 0 });
  const [alerts, setAlerts] = useState([]);
  const [showAlerts, setShowAlerts] = useState(true);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('token');
    const userFromUrl = urlParams.get('user');

    if (tokenFromUrl) {
      localStorage.setItem('token', tokenFromUrl);
      if (userFromUrl) {
        const parts = userFromUrl.split(',');
        if (parts.length >= 3) {
          const userObj = {
            id: parseInt(parts[0]),
            username: parts[1],
            displayName: decodeURIComponent(parts[2])
          };
          localStorage.setItem('user', JSON.stringify(userObj));
          setUser(userObj);
          setIsAuthenticated(true);
        }
      }
      window.history.replaceState({}, document.title, '/');
    }

    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');

    if (savedToken && savedUser) {
      try {
        setUser(JSON.parse(savedUser));
        setIsAuthenticated(true);
      } catch (e) {
        console.error('Error parsing user:', e);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
  }, []);

  const loadStats = async () => {
    try {
      const [zones, news, commodities] = await Promise.all([
        getZones(),
        getNews(),
        getCommodities()
      ]);
      
      setStats({
        totalZones: zones.length,
        totalNews: news.length,
        totalCommodities: commodities.length,
        activeConflicts: zones.filter(z => z.isActive).length,
        totalComments: 0
      });

      const priceAlerts = commodities.filter(c => 
        c.changePercent && Math.abs(c.changePercent) > 3
      );
      
      if (priceAlerts.length > 0) {
        setAlerts(priceAlerts.map(c => ({
          type: c.changePercent > 0 ? 'success' : 'danger',
          message: `${c.name}: ${c.changePercent > 0 ? '+' : ''}${c.changePercent.toFixed(2)}%`,
          time: new Date()
        })));
      }
    } catch (err) {
      console.error('Error loading stats:', err);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      loadStats();
    }
  }, [isAuthenticated]);

  const handleStatsUpdate = (data) => {
    setStats(prev => ({ ...prev, ...data }));
  };

  const handleLogin = (userData) => {
    setUser(userData);
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setIsAuthenticated(false);
  };

  if (!isAuthenticated) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-content">
          <div>
            <h1>World Conflict Monitor</h1>
            <p>Real-time monitoring of global conflicts and commodity prices</p>
          </div>
          <div className="header-right">
            <div className="stats-dashboard">
              <div className="stat-item">
                <span className="stat-value">{stats.totalZones}</span>
                <span className="stat-label">Zones</span>
              </div>
              <div className="stat-item">
                <span className="stat-value">{stats.totalNews}</span>
                <span className="stat-label">News</span>
              </div>
              <div className="stat-item">
                <span className="stat-value">{stats.totalCommodities}</span>
                <span className="stat-label">Commodities</span>
              </div>
              <div className="stat-item highlight">
                <span className="stat-value">{stats.activeConflicts || 0}</span>
                <span className="stat-label">Active</span>
              </div>
              {stats.selectedZone && (
                <div className="stat-item highlight">
                  <span className="stat-value">{stats.newsCount || 0}</span>
                  <span className="stat-label">{stats.selectedZone} News</span>
                </div>
              )}
            </div>
            <div className="auth-buttons">
              <div className="user-info">
                <span>{user?.displayName || user?.username}</span>
                <button onClick={handleLogout} className="logout-btn">Logout</button>
              </div>
            </div>
          </div>
        </div>
      </header>

      {alerts.length > 0 && showAlerts && (
        <div className="alerts-container">
          {alerts.map((alert, idx) => (
            <div key={idx} className={`alert alert-${alert.type}`}>
              <span>{alert.message}</span>
              <button onClick={() => setShowAlerts(false)}>×</button>
            </div>
          ))}
        </div>
      )}
      
      <div className="main-content">
        <aside className="sidebar-left">
          <NewsPanel selectedZone={selectedZone} user={user} />
        </aside>
        
        <section className="map-section">
          <ConflictMap 
            selectedZone={selectedZone} 
            onZoneSelect={setSelectedZone}
            onStatsUpdate={handleStatsUpdate}
          />
        </section>
        
        <aside className="sidebar-right">
          <CommoditiesPanel />
        </aside>
      </div>

      <AuthModal 
        isOpen={showAuthModal} 
        onClose={() => setShowAuthModal(false)}
        onLogin={handleLogin}
      />
    </div>
  );
}

export default App;
