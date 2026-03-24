import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, CircleMarker } from 'react-leaflet';
import L from 'leaflet';
import { getZones, getNewsByZone } from '../services/api';
import 'leaflet/dist/leaflet.css';

const ZONE_COLORS = {
  Europe: '#ff4444',
  'Middle East': '#ff6600',
  Asia: '#ffaa00',
  Africa: '#ff2266',
  Americas: '#00ffaa'
};

const createPulseIcon = (color, isSelected) => {
  const size = isSelected ? 40 : 30;
  const pulseSize = isSelected ? 50 : 40;
  
  const iconHtml = `
    <div style="
      position: relative;
      width: ${pulseSize}px;
      height: ${pulseSize}px;
    ">
      <div style="
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: ${size}px;
        height: ${size}px;
        background: ${color};
        border-radius: 50%;
        ${isSelected ? `
        box-shadow: 0 0 0 4px ${color}40, 0 0 20px ${color};
        animation: pulse 1.5s infinite;
        ` : `
        box-shadow: 0 0 10px ${color}80;
        `}
      "></div>
      <div style="
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 8px;
        height: 8px;
        background: white;
        border-radius: 50%;
      "></div>
    </div>
    <style>
      @keyframes pulse {
        0% { box-shadow: 0 0 0 0 ${color}80, 0 0 10px ${color}; }
        70% { box-shadow: 0 0 0 15px ${color}00, 0 0 20px ${color}; }
        100% { box-shadow: 0 0 0 0 ${color}00, 0 0 10px ${color}; }
      }
    </style>
  `;
  
  return L.divIcon({
    html: iconHtml,
    className: 'pulse-marker',
    iconSize: [pulseSize, pulseSize],
    iconAnchor: [pulseSize/2, pulseSize/2]
  });
};

export default function ConflictMap({ onZoneSelect, selectedZone, onStatsUpdate }) {
  const [zones, setZones] = useState([]);
  const [news, setNews] = useState({});

  useEffect(() => {
    getZones().then(data => {
      setZones(data);
      if (onStatsUpdate) {
        onStatsUpdate({ totalZones: data.length });
      }
    }).catch(console.error);
  }, []);

  useEffect(() => {
    if (selectedZone) {
      getNewsByZone(selectedZone).then(data => {
        setNews(prev => ({ ...prev, [selectedZone]: data }));
        if (onStatsUpdate) {
          const zone = zones.find(z => z.id === selectedZone);
          onStatsUpdate({ 
            selectedZone: zone?.name,
            newsCount: data.length
          });
        }
      }).catch(console.error);
    }
  }, [selectedZone]);

  const getZoneColor = (zone) => {
    if (selectedZone === zone.id) return '#00e5ff';
    return ZONE_COLORS[zone.region] || '#ff4444';
  };

  return (
    <MapContainer center={[30, 10]} zoom={2} style={{ height: '100%', width: '100%' }}>
      <TileLayer
        url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        attribution='&copy; OpenStreetMap contributors &copy; CARTO'
      />
      {zones.map(zone => {
        const color = getZoneColor(zone);
        const isSelected = selectedZone === zone.id;
        
        return (
          <Marker 
            key={zone.id} 
            position={[zone.latitude, zone.longitude]}
            icon={createPulseIcon(color, isSelected)}
            eventHandlers={{
              click: () => onZoneSelect(zone.id)
            }}
          >
            <Popup>
              <div style={{ 
                minWidth: 250, 
                background: '#0a0b14',
                padding: '10px',
                borderRadius: '8px'
              }}>
                <h3 style={{ 
                  margin: '0 0 8px 0', 
                  color: color,
                  fontFamily: 'Orbitron, sans-serif',
                  fontSize: '14px'
                }}>
                  {zone.name}
                </h3>
                <div style={{
                  display: 'inline-block',
                  padding: '2px 8px',
                  background: color + '30',
                  borderRadius: '4px',
                  fontSize: '10px',
                  color: color,
                  marginBottom: '8px'
                }}>
                  {zone.region}
                </div>
                <p style={{ 
                  margin: '5px 0', 
                  fontSize: '11px', 
                  color: '#aaa' 
                }}>
                  {zone.description}
                </p>
                {news[zone.id] && news[zone.id].length > 0 && (
                  <div style={{ marginTop: 10, borderTop: '1px solid #333', paddingTop: '8px' }}>
                    <strong style={{ color: '#00e5ff', fontSize: '11px' }}>
                      📰 {news[zone.id].length} News Articles
                    </strong>
                    <ul style={{ margin: '5px 0', paddingLeft: 15, fontSize: '10px', color: '#ccc' }}>
                      {news[zone.id].slice(0, 3).map(n => (
                        <li key={n.id} style={{ marginBottom: '3px' }}>{n.title}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </Popup>
          </Marker>
        );
      })}
    </MapContainer>
  );
}
