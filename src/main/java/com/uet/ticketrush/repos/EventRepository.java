package com.uet.ticketrush.repos;

import com.uet.ticketrush.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    // Spring sẽ tự xử lý việc lấy dữ liệu từ DB
}