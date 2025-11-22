import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const apiService = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token
apiService.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
apiService.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authService = {
  register: (username, email, password) =>
    apiService.post('/api/auth/register', { username, email, password }),
  
  login: (username, password) =>
    apiService.post('/api/auth/login', { username, password }),
};

export const documentService = {
  upload: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiService.post('/api/documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  
  getAll: () => apiService.get('/api/documents'),
  
  getById: (id) => apiService.get(`/api/documents/${id}`),
  
  download: (id) =>
    apiService.get(`/api/documents/${id}/download`, { responseType: 'blob' }),
  
  delete: (id) => apiService.delete(`/api/documents/${id}`),
};

export const searchService = {
  search: (query, limit = 10) =>
    apiService.get('/api/search', { params: { query, limit } }),
  
  searchWithScore: (query, threshold = 0.5, limit = 10) =>
    apiService.get('/api/search/scored', { params: { query, threshold, limit } }),
};

export const ragService = {
  ask: (question, topK = 5) =>
    apiService.post('/api/rag/ask', { question, topK: topK.toString() }),
  
  answer: (question, topK = 5) =>
    apiService.post('/api/rag/answer', { question, topK: topK.toString() }),
};

export default apiService;

