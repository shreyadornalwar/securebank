package com.bank.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final JdbcTemplate jdbcTemplate;

    public NotificationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void createNotification(Long userId, String title, String message, String type) {
        jdbcTemplate.update(
            "INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, ?)",
            userId, title, message, type != null ? type : "INFO"
        );
    }

    public List<NotificationDTO> getNotificationsByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT id, user_id, title, message, type, is_read, created_at " +
            "FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50",
            (rs, rowNum) -> new NotificationDTO(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getString("type"),
                rs.getBoolean("is_read"),
                rs.getString("created_at")
            )
        );
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        jdbcTemplate.update("UPDATE notifications SET is_read = 1 WHERE id = ?", notificationId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        jdbcTemplate.update("UPDATE notifications SET is_read = 1 WHERE user_id = ?", userId);
    }

    public int getUnreadCount(Long userId) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0",
            Integer.class, userId
        );
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        jdbcTemplate.update("DELETE FROM notifications WHERE id = ? AND user_id = ?", notificationId, userId);
    }

    public static class NotificationDTO {
        private final Long id;
        private final Long userId;
        private final String title;
        private final String message;
        private final String type;
        private final boolean isRead;
        private final String createdAt;

        public NotificationDTO(Long id, Long userId, String title, String message, 
                              String type, boolean isRead, String createdAt) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.isRead = isRead;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public boolean isRead() { return isRead; }
        public String getCreatedAt() { return createdAt; }
    }
}