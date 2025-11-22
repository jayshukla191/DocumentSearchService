import React from 'react';
import './SearchResults.css';

const SearchResults = ({ results }) => {
  if (!results || !results.results) {
    return null;
  }

  const { query, results: chunks, count } = results;

  if (count === 0) {
    return (
      <div className="search-results">
        <p className="no-results">No results found for "{query}"</p>
      </div>
    );
  }

  // Group chunks by document
  const groupedByDocument = chunks.reduce((acc, chunk) => {
    const docId = chunk.documentId;
    if (!acc[docId]) {
      acc[docId] = [];
    }
    acc[docId].push(chunk);
    return acc;
  }, {});

  return (
    <div className="search-results">
      <h4>Search Results for "{query}" ({count} chunks found)</h4>
      
      <div className="results-list">
        {Object.entries(groupedByDocument).map(([docId, docChunks]) => (
          <div key={docId} className="result-group">
            <div className="result-header">
              <strong>Document ID: {docId}</strong>
              <span className="chunk-count">{docChunks.length} relevant chunk(s)</span>
            </div>
            
            {docChunks.map((chunk) => (
              <div key={chunk.id} className="result-item">
                <div className="chunk-text">
                  {chunk.chunkText.substring(0, 300)}
                  {chunk.chunkText.length > 300 && '...'}
                </div>
                <div className="chunk-info">
                  Chunk #{chunk.chunkIndex + 1}
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
};

export default SearchResults;

