import React, { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { documentService } from '../services/apiService';
import './DocumentUpload.css';

const DocumentUpload = ({ onUploadSuccess }) => {
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const onDrop = useCallback(async (acceptedFiles) => {
    if (acceptedFiles.length === 0) return;

    const file = acceptedFiles[0];
    setUploading(true);
    setMessage('');
    setError('');

    try {
      const response = await documentService.upload(file);
      setMessage(`File "${response.data.filename}" uploaded successfully!`);
      if (onUploadSuccess) {
        onUploadSuccess();
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to upload file');
    } finally {
      setUploading(false);
    }
  }, [onUploadSuccess]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false,
    disabled: uploading,
  });

  return (
    <div className="card upload-card">
      <h3>Upload Document</h3>
      <div
        {...getRootProps()}
        className={`dropzone ${isDragActive ? 'active' : ''} ${uploading ? 'disabled' : ''}`}
      >
        <input {...getInputProps()} />
        {uploading ? (
          <p>Uploading...</p>
        ) : isDragActive ? (
          <p>Drop the file here...</p>
        ) : (
          <div>
            <p>📁 Drag and drop a file here, or click to select</p>
            <p className="file-types">Supported: PDF, Images, Text files</p>
          </div>
        )}
      </div>
      {message && <div className="success">{message}</div>}
      {error && <div className="error">{error}</div>}
    </div>
  );
};

export default DocumentUpload;

