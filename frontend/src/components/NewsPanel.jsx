import { useState, useEffect } from 'react';
import { getNews } from '../services/api';
import CommentsPanel from './CommentsPanel';

export default function NewsPanel({ selectedZone, user }) {
  const [news, setNews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedNews, setExpandedNews] = useState(null);
  const [sourceFilter, setSourceFilter] = useState('all');
  const [showComments, setShowComments] = useState(null);

  useEffect(() => {
    setLoading(true);
    getNews()
      .then(data => {
        const filtered = applyFilters(data, sourceFilter, selectedZone);
        setNews(filtered);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [selectedZone, sourceFilter]);

  useEffect(() => {
    const interval = setInterval(() => {
      getNews()
        .then(data => {
          const filtered = applyFilters(data, sourceFilter, selectedZone);
          setNews(filtered);
        })
        .catch(console.error);
    }, 900000);
    return () => clearInterval(interval);
  }, [selectedZone, sourceFilter]);

  const applyFilters = (data, source, zoneId) => {
    let filtered = data;
    
    if (zoneId) {
      filtered = filtered.filter(n => n.conflictZoneId === zoneId);
    }
    
    if (source && source !== 'all') {
      filtered = filtered.filter(n => n.source === source);
    }
    
    return filtered;
  };

  const handleNewsClick = (item) => {
    setExpandedNews(expandedNews === item.id ? null : item.id);
  };

  const toggleComments = (e, item) => {
    e.stopPropagation();
    setExpandedNews(item.id);
    setShowComments(showComments === item.id ? null : item.id);
  };

  const openExternalLink = (e, url) => {
    e.stopPropagation();
    if (url) {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  };

  const getUniqueSources = () => {
    const sources = new Set(news.map(n => n.source).filter(Boolean));
    return ['all', ...Array.from(sources)];
  };

  if (loading) return <div className="panel-loading">Loading news...</div>;

  return (
    <div className="news-panel">
      <h2>Latest News</h2>
      
      <div className="news-filters">
        <select 
          value={sourceFilter} 
          onChange={(e) => setSourceFilter(e.target.value)}
          className="filter-select"
        >
          <option value="all">All Sources</option>
          {getUniqueSources().slice(1).map(source => (
            <option key={source} value={source}>{source}</option>
          ))}
        </select>
      </div>
      
      <div className="news-list">
        {news.length === 0 ? (
          <p>No news available</p>
        ) : (
          news.slice(0, 15).map(item => (
            <div 
              key={item.id} 
              className={`news-item ${expandedNews === item.id ? 'expanded' : ''}`}
              onClick={() => handleNewsClick(item)}
            >
              <h3>{item.title}</h3>
              <p className="news-desc">{item.description}</p>
              
              {expandedNews === item.id && (
                <div className="news-full-content">
                  <p className="news-full-description">{item.description}</p>
                  {item.url && (
                    <button 
                      className="news-link-button"
                      onClick={(e) => openExternalLink(e, item.url)}
                    >
                      Read Full Article →
                    </button>
                  )}
                </div>
              )}
              
              {showComments === item.id && (
                <CommentsPanel 
                  newsId={item.id} 
                  user={user} 
                />
              )}
              
              <div className="news-footer">
                <span className="news-source">{item.source}</span>
                <button 
                  className="comments-toggle"
                  onClick={(e) => toggleComments(e, item)}
                >
                  💬 Comments
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
