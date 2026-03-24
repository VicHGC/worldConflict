package com.worldconflict.api.controller;

import com.worldconflict.api.entity.Comment;
import com.worldconflict.api.service.AuthService;
import com.worldconflict.api.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentController {
    
    private final CommentService commentService;
    private final AuthService authService;
    
    @GetMapping("/news/{newsId}")
    public ResponseEntity<List<Comment>> getCommentsByNewsId(@PathVariable Long newsId) {
        return ResponseEntity.ok(commentService.getCommentsByNewsId(newsId));
    }
    
    @PostMapping("/news/{newsId}")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long newsId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Authentication required"));
        }
        
        var user = authService.getUserByToken(token.substring(7));
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Invalid token"));
        }
        
        String content = (String) request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Comment cannot be empty"));
        }
        
        Comment comment = commentService.addComment(newsId, user.getId(), content);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "comment", Map.of(
                "id", comment.getId(),
                "content", comment.getContent(),
                "userName", comment.getUserName(),
                "createdAt", comment.getCreatedAt().toString()
            )
        ));
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Authentication required"));
        }
        
        var user = authService.getUserByToken(token.substring(7));
        if (user == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Invalid token"));
        }
        
        commentService.deleteComment(commentId, user.getId());
        
        return ResponseEntity.ok(Map.of("success", true));
    }
}
