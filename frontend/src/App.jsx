import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './components/Login';
import Register from './components/Register';
import Navbar from './components/Navbar';
import DocumentUpload from './components/DocumentUpload';
import DocumentList from './components/DocumentList';
import SearchBar from './components/SearchBar';
import QuestionAnswer from './components/QuestionAnswer';
import './App.css';

const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div className="loading">Loading...</div>;
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />;
};

const AppContent = () => {
  const { isAuthenticated } = useAuth();

  return (
    <Router>
      {isAuthenticated && <Navbar />}
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <div className="container">
                <h1>Document Management System</h1>
                <DocumentUpload />
                <SearchBar />
                <DocumentList />
              </div>
            </PrivateRoute>
          }
        />
        <Route
          path="/qa"
          element={
            <PrivateRoute>
              <div className="container">
                <h1>Ask Questions About Your Documents</h1>
                <QuestionAnswer />
              </div>
            </PrivateRoute>
          }
        />
      </Routes>
    </Router>
  );
};

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;

