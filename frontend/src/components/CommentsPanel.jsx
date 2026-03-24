import { useState, useEffect } from 'react';
import { getCommentsByNews, addComment } from '../services/api';

export default function CommentsPanel({ newsId, user }) {
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const token = localStorage.getItem('token');

  useEffect(() => {
    if (newsId) {
      loadComments();
    }
  }, [newsId]);

  const loadComments = async () => {
    setLoading(true);
    try {
      const data = await getCommentsByNews(newsId);
      setComments(data || []);
    } catch (err) {
      console.error('Error loading comments:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim() || !token) return;

    setSubmitting(true);
    setError(null);

    try {
      const result = await addComment(newsId, newComment);
      if (result && result.success) {
        setNewComment('');
        loadComments();
      } else {
        setError(result?.message || 'Failed to post comment');
      }
    } catch (err) {
      console.error('Error posting comment:', err);
      setError('Failed to post comment. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  if (!newsId) return null;

  return (
    <div className="comments-panel">
      <h3>Comments ({comments.length})</h3>
      
      {user && token ? (
        <form onSubmit={handleSubmit} className="comment-form">
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="Write a comment..."
            rows={2}
          />
          <button type="submit" disabled={!newComment.trim() || submitting}>
            {submitting ? 'Posting...' : 'Post'}
          </button>
          {error && <p className="error-message">{error}</p>}
        </form>
      ) : (
        <p className="login-prompt">Login to comment</p>
      )}
      
      <div className="comments-list">
        {loading ? (
          <p>Loading...</p>
        ) : comments.length === 0 ? (
          <p>No comments yet</p>
        ) : (
          comments.map(comment => (
            <div key={comment.id} className="comment-item">
              <div className="comment-header">
                <span className="comment-author">{comment.userName}</span>
                <span className="comment-date">{formatDate(comment.createdAt)}</span>
              </div>
              <p className="comment-content">{comment.content}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
