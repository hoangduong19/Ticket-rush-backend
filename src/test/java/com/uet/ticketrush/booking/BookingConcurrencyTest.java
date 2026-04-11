package com.uet.ticketrush.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.ticketrush.dtos.BookingRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
@SpringBootTest
@AutoConfigureMockMvc
class BookingConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    // 1. Xóa cái @Autowired trên ObjectMapper đi
// @Autowired
// private ObjectMapper objectMapper;

    // 2. Khởi tạo trực tiếp luôn
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Test
    @WithMockUser
        // Dự phòng nếu .with(user()) có vấn đề
    void testConcurrentBooking_OnlyOneShouldWin() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        UUID userId = UUID.fromString("16009e3c-667f-48a3-885f-1f27bba7c68c");
        UUID eventId = UUID.fromString("660f9511-f30c-52e5-b827-557766551111");
        List<UUID> seatIds = List.of(UUID.fromString("d5815e48-cd55-4625-8fb1-cf9492588f12"));

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    latch.await();
                    BookingRequestDTO request = new BookingRequestDTO(userId, eventId, seatIds);

                    var response = mockMvc.perform(post("/seats/reservations")
                                    .with(user(userId.toString()).roles("USER")) // Giả lập Auth
                                    .with(csrf()) // Vượt rào CSRF
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andReturn().getResponse();

                    if (response.getStatus() == 200 || response.getStatus() == 201) {
                        successCount.incrementAndGet();
                    } else if (response.getStatus() == 409) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        latch.countDown();
        finishLatch.await();

        System.out.println("--- KẾT QUẢ ĐẤM NHAU ---");
        System.out.println("Thành công: " + successCount.get());
        System.out.println("Thất bại (409): " + conflictCount.get());

        assertEquals(1, successCount.get(), "Chỉ duy nhất 1 người được giữ ghế!");
    }
}