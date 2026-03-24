package com.worldconflict.api.service;

import com.worldconflict.api.entity.Comment;
import com.worldconflict.api.entity.User;
import com.worldconflict.api.repository.CommentRepository;
import com.worldconflict.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    
    public Comment addComment(Long newsId, Long userId, String content) {
        User user = userRepository.findById(userId).orElse(null);
        
        Comment comment = new Comment();
        comment.setNewsId(newsId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setUserName(user != null ? user.getDisplayName() : "Anonymous");
        comment.setCreatedAt(LocalDateTime.now());
        
        return commentRepository.save(comment);
    }
    
    public List<Comment> getCommentsByNewsId(Long newsId) {
        return commentRepository.findByNewsIdOrderByCreatedAtDesc(newsId);
    }
    
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment != null && comment.getUserId().equals(userId)) {
            commentRepository.delete(comment);
        }
    }
}
