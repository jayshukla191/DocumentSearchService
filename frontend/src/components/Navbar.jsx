import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          📄 Document Management
        </Link>
        
        <div className="navbar-menu">
          <Link to="/" className="navbar-link">
            Documents
          </Link>
          <Link to="/qa" className="navbar-link">
            Q&A
          </Link>
          <span className="navbar-user">
            👤 {user?.username}
          </span>
          <button onClick={handleLogout} className="navbar-logout">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

