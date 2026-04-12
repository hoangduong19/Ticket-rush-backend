// script để giả lập 50 người dùng tham gia hàng đợi
const eventId = process.argv[2];

if (!eventId) {
    console.error("Vui lòng cung cấp Event ID!");
    console.log("Cách dùng: node test-queue.js <YOUR-EVENT-ID>");
    process.exit(1);
}

async function simulateUsers() {
    console.log(`Đang chạy giả lập 50 users tham gia event: ${eventId}...`);
    
    for (let i = 1; i <= 50; i++) {
        const userId = `fake-user-${i}`;
        
        try {
            const response = await fetch('http://localhost:8080/queue/join', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ eventId: eventId, userId: userId })
            });
            const data = await response.json();
            console.log(`User ${i} (${userId}): ${data.status}`);
        } catch (error) {
            console.error(`Lỗi request thứ ${i}:`, error.message);
        }
    }
    
    console.log("\nĐã hoàn thành giả lập 50 slot.");
    console.log("Bây giờ bạn hãy mở trình duyệt và thử ấn 'Proceed to Booking' cho sự kiện này.");
    console.log("Bạn sẽ tự động bị đẩy vào Queue (vì là người thứ 51).");
}

simulateUsers();
