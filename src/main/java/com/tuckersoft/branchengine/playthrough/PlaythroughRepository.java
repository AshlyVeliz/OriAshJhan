package com.tuckersoft.branchengine.playthrough;

import com.tuckersoft.branchengine.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaythroughRepository extends JpaRepository<Playthrough, Long> {

    boolean existsByPlayerTag(String playerTag);

    List<Playthrough> findByUserOrderByCreatedAtDescIdDesc(User user);

    List<Playthrough> findAllByOrderByCreatedAtDescIdDesc();
}
