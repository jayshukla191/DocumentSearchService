import React, { useState } from 'react';
import { searchService } from '../services/apiService';
import SearchResults from './SearchResults';
import './SearchBar.css';

const SearchBar = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSearch = async (e) => {
    e.preventDefault();
    
    if (!query.trim()) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await searchService.search(query, 10);
      setResults(response.data);
    } catch (err) {
      setError('Search failed. Please try again.');
      setResults(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card search-card">
      <h3>🔍 Semantic Search</h3>
      <p className="search-description">
        Search your documents by meaning, not just keywords. Ask questions or describe what you're looking for.
      </p>
      
      <form onSubmit={handleSearch} className="search-form">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="e.g., 'safety procedures' or 'budget information'"
          disabled={loading}
          className="search-input"
        />
        <button type="submit" disabled={loading || !query.trim()}>
          {loading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && <div className="error">{error}</div>}
      
      {results && <SearchResults results={results} />}
    </div>
  );
};

export default SearchBar;

