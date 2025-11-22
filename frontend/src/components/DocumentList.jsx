import React, { useState, useEffect } from 'react';
import { documentService } from '../services/apiService';
import './DocumentList.css';

const DocumentList = () => {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDocuments = async () => {
    try {
      setLoading(true);
      const response = await documentService.getAll();
      setDocuments(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const handleDownload = async (id, filename) => {
    try {
      const response = await documentService.download(id);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Failed to download document');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this document?')) {
      return;
    }

    try {
      await documentService.delete(id);
      loadDocuments();
    } catch (err) {
      alert('Failed to delete document');
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  if (loading) {
    return <div className="card">Loading documents...</div>;
  }

  if (error) {
    return <div className="card error">{error}</div>;
  }

  return (
    <div className="card">
      <h3>My Documents ({documents.length})</h3>
      {documents.length === 0 ? (
        <p className="no-documents">No documents uploaded yet. Upload your first document above!</p>
      ) : (
        <div className="document-list">
          {documents.map((doc) => (
            <div key={doc.id} className="document-item">
              <div className="document-info">
                <div className="document-icon">📄</div>
                <div className="document-details">
                  <h4>{doc.filename}</h4>
                  <p className="document-meta">
                    {formatFileSize(doc.fileSize)} • {formatDate(doc.uploadDate)}
                  </p>
                </div>
              </div>
              <div className="document-actions">
                <button onClick={() => handleDownload(doc.id, doc.filename)} className="btn-download">
                  Download
                </button>
                <button onClick={() => handleDelete(doc.id)} className="btn-delete">
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default DocumentList;

