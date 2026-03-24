import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';

export const api = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/?session=expired';
    }
    return Promise.reject(error);
  }
);

export const getZones = () => api.get('/zones').then(res => res.data);
export const getNews = () => api.get('/news').then(res => res.data);
export const getNewsByZone = (zoneId) => api.get(`/news/zone/${zoneId}`).then(res => res.data);
export const getCommodities = () => api.get('/commodities').then(res => res.data);
export const getTopCommodities = () => api.get('/commodities/top').then(res => res.data);

export const login = (username, password, rememberMe = false) => 
  api.post('/auth/login', { username, password, rememberMe }).then(res => res.data);

export const register = (username, email, password, displayName) =>
  api.post('/auth/register', { username, email, password, displayName }).then(res => res.data);

export const getCurrentUser = () =>
  api.get('/auth/me').then(res => res.data);

export const getCommentsByNews = (newsId) =>
  api.get(`/comments/news/${newsId}`).then(res => res.data);

export const addComment = (newsId, content) =>
  api.post(`/comments/news/${newsId}`, { content }).then(res => res.data);

export const deleteComment = (commentId) =>
  api.delete(`/comments/${commentId}`).then(res => res.data);
