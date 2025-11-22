import React, { useState } from 'react';
import { ragService } from '../services/apiService';
import './QuestionAnswer.css';

const QuestionAnswer = () => {
  const [question, setQuestion] = useState('');
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!question.trim()) {
      return;
    }

    setLoading(true);
    setError('');
    setResponse(null);

    try {
      const result = await ragService.ask(question, 5);
      setResponse(result.data);
    } catch (err) {
      setError('Failed to get answer. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="qa-container">
      <div className="card qa-card">
        <h3>💬 Ask Questions About Your Documents</h3>
        <p className="qa-description">
          Ask any question about your uploaded documents. The AI will search through your documents 
          and provide an answer based on the relevant information found.
        </p>
        
        <form onSubmit={handleSubmit} className="qa-form">
          <textarea
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="e.g., 'What are the main safety requirements?' or 'Summarize the budget allocation'"
            rows="4"
            disabled={loading}
            className="qa-input"
          />
          <button type="submit" disabled={loading || !question.trim()}>
            {loading ? 'Getting Answer...' : 'Ask Question'}
          </button>
        </form>

        {error && <div className="error">{error}</div>}
        
        {response && (
          <div className="qa-response">
            <div className="answer-section">
              <h4>📝 Answer:</h4>
              <div className="answer-text">{response.answer}</div>
            </div>
            
            {response.sources && response.sources.length > 0 && (
              <div className="sources-section">
                <h4>📚 Sources ({response.sourceCount} relevant chunks):</h4>
                <div className="sources-list">
                  {response.sources.map((source) => (
                    <div key={source.id} className="source-item">
                      <div className="source-header">
                        <strong>Document ID: {source.documentId}</strong>
                        <span className="source-chunk">Chunk #{source.chunkIndex + 1}</span>
                      </div>
                      <div className="source-text">
                        {source.chunkText.substring(0, 200)}
                        {source.chunkText.length > 200 && '...'}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default QuestionAnswer;

