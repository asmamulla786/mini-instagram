package com.projects.My_Instagram.repositories;

import com.projects.My_Instagram.models.FollowRequest;
import com.projects.My_Instagram.models.FollowRequestStatus;
import com.projects.My_Instagram.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {
    boolean existsByRequesterAndTargetUserAndStatus(
            User requester,
            User targetUser,
            FollowRequestStatus status
    );

    List<FollowRequest> findByTargetUserAndStatus(User targetUser, FollowRequestStatus status);
}
