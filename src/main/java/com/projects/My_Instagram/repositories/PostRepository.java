package com.projects.My_Instagram.repositories;

import com.projects.My_Instagram.models.Post;
import com.projects.My_Instagram.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);

    void deleteByUser(User user);
}
