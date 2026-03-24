import { useState, useEffect } from 'react';
import { login, register, api } from '../services/api';
import { useI18n } from '../i18n';

function calculatePasswordStrength(password) {
  if (!password) return { strength: 0, label: '', color: '' };
  
  let score = 0;
  if (password.length >= 8) score += 1;
  if (password.length >= 12) score += 1;
  if (/[a-z]/.test(password)) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/[0-9]/.test(password)) score += 1;
  if (/[^a-zA-Z0-9]/.test(password)) score += 1;
  
  if (score <= 2) return { strength: 1, label: 'weak', color: '#ff4444' };
  if (score <= 3) return { strength: 2, label: 'medium', color: '#ff9900' };
  if (score <= 4) return { strength: 3, label: 'strong', color: '#27ae60' };
  return { strength: 4, label: 'veryStrong', color: '#00e5ff' };
}

export default function LoginPage({ onLogin }) {
  const { language, setLanguage, t } = useI18n();
  const [view, setView] = useState('login');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [particles, setParticles] = useState([]);

  const passwordStrength = calculatePasswordStrength(password);

  useEffect(() => {
    const savedRememberToken = localStorage.getItem('rememberToken');
    if (savedRememberToken) {
      api.post('/auth/login/remember', { rememberToken: savedRememberToken })
        .then(res => res.data)
        .then(result => {
          if (result.success) {
            localStorage.setItem('token', result.token);
            localStorage.setItem('user', JSON.stringify(result.user));
            onLogin(result.user);
          }
        })
        .catch(() => {});
    }
  }, []);

  useEffect(() => {
    const newParticles = [];
    for (let i = 0; i < 50; i++) {
      newParticles.push({
        x: Math.random() * 100,
        y: Math.random() * 100,
        size: Math.random() * 3 + 1,
        duration: Math.random() * 20 + 10,
        delay: Math.random() * 10
      });
    }
    setParticles(newParticles);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    if (view === 'register' && password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);

    try {
      if (view === 'login') {
        const result = await login(username, password, rememberMe);
        if (result.success) {
          localStorage.setItem('token', result.token);
          localStorage.setItem('user', JSON.stringify(result.user));
          if (rememberMe && result.rememberToken) {
            localStorage.setItem('rememberToken', result.rememberToken);
          }
          onLogin(result.user);
        } else {
          setError(result.message);
        }
      } else {
        const result = await register(username, email, password, displayName);
        if (result.success) {
          localStorage.setItem('token', result.token);
          localStorage.setItem('user', JSON.stringify(result.user));
          onLogin(result.user);
        } else {
          setError(result.message);
        }
      }
    } catch (err) {
      setError('An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    if (!email) {
      setError('Please enter your email');
      return;
    }
    
    setLoading(true);
    try {
      const result = await api.post('/auth/forgot-password', { email }).then(res => res.data);
      if (result.success) {
        setSuccess(t('login.resetEmailSent'));
      } else {
        setError(result.message);
      }
    } catch (err) {
      setError('An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    
    setLoading(true);
    try {
      const urlParams = new URLSearchParams(window.location.search);
      const resetToken = urlParams.get('resetToken');
      
      const result = await api.post('/auth/reset-password', { 
        resetToken, 
        newPassword: password 
      }).then(res => res.data);
      
      if (result.success) {
        setSuccess(t('login.resetSuccess'));
        setTimeout(() => setView('login'), 2000);
      } else {
        setError(result.message);
      }
    } catch (err) {
      setError('An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="scanlines"></div>
      
      <div className="particles">
        {particles.map((particle, i) => (
          <div 
            key={i} 
            className="particle"
            style={{
              left: `${particle.x}%`,
              top: `${particle.y}%`,
              width: `${particle.size}px`,
              height: `${particle.size}px`,
              animationDuration: `${particle.duration}s`,
              animationDelay: `${particle.delay}s`
            }}
          />
        ))}
      </div>

      <div className="login-bg-animation">
        <div className="globe-container">
          <div className="globe">
            <div className="globe-inner">
              <div className="globe-line lat-1"></div>
              <div className="globe-line lat-2"></div>
              <div className="globe-line lat-3"></div>
              <div className="globe-line lon-1"></div>
              <div className="globe-line lon-2"></div>
              <div className="globe-line lon-3"></div>
              <div className="pulse-dot red"></div>
              <div className="pulse-dot orange"></div>
              <div className="pulse-dot yellow"></div>
            </div>
          </div>
        </div>
      </div>

      <div className="language-selector">
        <button 
          className={`lang-btn ${language === 'en' ? 'active' : ''}`}
          onClick={() => setLanguage('en')}
        >
          EN
        </button>
        <button 
          className={`lang-btn ${language === 'es' ? 'active' : ''}`}
          onClick={() => setLanguage('es')}
        >
          ES
        </button>
      </div>

      <div className="login-container">
        <div className="login-header">
          <h1>{t('login.title')}</h1>
          <div className="login-subtitle">{t('login.subtitle')}</div>
        </div>

        <div className="login-description">
          <p>
            {t('login.description')}
          </p>
          <ul className="feature-list">
            <li>🗺️ {t('login.features.map')}</li>
            <li>📰 {t('login.features.news')}</li>
            <li>📊 {t('login.features.commodities')}</li>
            <li>💬 {t('login.features.comments')}</li>
          </ul>
        </div>

        <div className="login-box">
          {loading && (
            <div className="loading-overlay">
              <div className="loading-spinner"></div>
            </div>
          )}
          
          <h2>
            {view === 'login' && t('login.accessPortal')}
            {view === 'register' && t('login.register')}
            {view === 'forgot' && t('login.resetPassword')}
            {view === 'reset' && t('login.resetPassword')}
          </h2>
          
          {view === 'reset' ? (
            <form onSubmit={handleResetPassword}>
              <div className="form-group">
                <label>{t('login.newPassword')}</label>
                <div className="password-input">
                  <input
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                  />
                  <button type="button" onClick={() => setShowPassword(!showPassword)}>
                    {showPassword ? '👁️' : '👁️‍🗨️'}
                  </button>
                </div>
                {password && (
                  <div className="password-strength">
                    <div className="strength-bar">
                      <div 
                        className="strength-fill" 
                        style={{ 
                          width: `${passwordStrength.strength * 25}%`,
                          backgroundColor: passwordStrength.color
                        }}
                      />
                    </div>
                    <span style={{ color: passwordStrength.color }}>
                      {t(`login.passwordStrength.${passwordStrength.label}`)}
                    </span>
                  </div>
                )}
              </div>
              
              <div className="form-group">
                <label>{t('login.confirmPassword')}</label>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={e => setConfirmPassword(e.target.value)}
                  required
                />
              </div>
              
              {error && <div className="error-message">{error}</div>}
              {success && <div className="success-message">{success}</div>}
              
              <button type="submit" disabled={loading}>
                {loading ? t('login.submitting') : t('login.resetPassword')}
              </button>
            </form>
          ) : view === 'forgot' ? (
            <form onSubmit={handleForgotPassword}>
              <div className="form-group">
                <label>{t('login.email')}</label>
                <input
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required
                />
              </div>
              
              {error && <div className="error-message">{error}</div>}
              {success && <div className="success-message">{success}</div>}
              
              <button type="submit" disabled={loading}>
                {loading ? t('login.submitting') : t('login.resetPassword')}
              </button>
              
              <div className="login-switch">
                <button type="button" onClick={() => { setView('login'); setError(''); setSuccess(''); }}>
                  {t('login.backToLogin')}
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>{t('login.username')}</label>
                <input
                  type="text"
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  required
                />
              </div>
              
              {view === 'register' && (
                <div className="form-group">
                  <label>{t('login.email')}</label>
                  <input
                    type="email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                  />
                </div>
              )}
              
              <div className="form-group">
                <label>{t('login.password')}</label>
                <div className="password-input">
                  <input
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                  />
                  <button type="button" onClick={() => setShowPassword(!showPassword)}>
                    {showPassword ? '👁️' : '👁️‍🗨️'}
                  </button>
                </div>
                {password && (
                  <div className="password-strength">
                    <div className="strength-bar">
                      <div 
                        className="strength-fill" 
                        style={{ 
                          width: `${passwordStrength.strength * 25}%`,
                          backgroundColor: passwordStrength.color
                        }}
                      />
                    </div>
                    <span style={{ color: passwordStrength.color }}>
                      {t(`login.passwordStrength.${passwordStrength.label}`)}
                    </span>
                  </div>
                )}
              </div>
              
              {view === 'register' && (
                <div className="form-group">
                  <label>{t('login.confirmPassword')}</label>
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={e => setConfirmPassword(e.target.value)}
                    required
                  />
                </div>
              )}
              
              {view === 'register' && (
                <div className="form-group">
                  <label>{t('login.displayName')}</label>
                  <input
                    type="text"
                    value={displayName}
                    onChange={e => setDisplayName(e.target.value)}
                  />
                </div>
              )}
              
              {view === 'login' && (
                <div className="form-group checkbox-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={rememberMe}
                      onChange={e => setRememberMe(e.target.checked)}
                    />
                    {t('login.rememberMe')}
                  </label>
                </div>
              )}
              
              {error && <div className="error-message">{error}</div>}
              
              <button type="submit" disabled={loading}>
                {loading ? t('login.submitting') : (view === 'login' ? t('login.enter') : t('login.register'))}
              </button>
            </form>
          )}

          {view !== 'forgot' && view !== 'reset' && (
            <>
              <div className="divider">
                <span>{t('login.or')}</span>
              </div>

              <a 
                href={import.meta.env.VITE_GOOGLE_OAUTH_URL || "http://localhost:8081/oauth2/authorization/google"}
                className="google-btn"
              >
                <svg viewBox="0 0 24 24" width="20" height="20">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                {t('login.continueWithGoogle')}
              </a>
              
              <div className="login-switch">
                {view === 'login' && (
                  <button type="button" onClick={() => { setView('register'); setError(''); }}>
                    {t('login.needAccount')}
                  </button>
                )}
                {view === 'register' && (
                  <button type="button" onClick={() => { setView('login'); setError(''); }}>
                    {t('login.haveAccount')}
                  </button>
                )}
                {view === 'login' && (
                  <button type="button" className="forgot-link" onClick={() => { setView('forgot'); setError(''); }}>
                    {t('login.forgotPassword')}
                  </button>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
