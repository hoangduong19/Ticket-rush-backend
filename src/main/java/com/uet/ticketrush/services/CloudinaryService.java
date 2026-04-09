package com.uet.ticketrush.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.uet.ticketrush.exceptions.TicketRushException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * 1. HÀM UPLOAD CƠ BẢN
     * Upload file lên Cloudinary theo folder chỉ định.
     */
    public String uploadImage(MultipartFile file, String folderName) {
        if (file.isEmpty()) throw new TicketRushException("File không tồn tại!", HttpStatus.BAD_REQUEST);

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "ticketrush/" + folderName));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new TicketRushException("Lỗi upload ảnh: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 2. HÀM UPLOAD + TỰ ĐỘNG RESIZE (Optimization)
     * Thích hợp cho Avatar để tiết kiệm bộ nhớ Cloudinary và tăng tốc load UI.
     * Nó sẽ tự cắt ảnh thành hình vuông (square) và resize về 400x400.
     */
    public String uploadAvatar(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ticketrush/avatars",
                            "transformation", "c_fill,g_face,w_400,h_400,q_auto,f_auto"
                    ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new TicketRushException("Lỗi upload avatar: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 3. HÀM XÓA ẢNH (Cleanup)
     * Dùng để xóa ảnh cũ khi người dùng cập nhật ảnh mới, tránh tốn dung lượng mây.
     */
    public void deleteImage(String url) {
        if (url == null || url.isEmpty() || url.contains("dicebear")) return; // Không xóa ảnh mặc định

        try {
            String publicId = extractPublicId(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            System.err.println("Dọn rác thất bại: " + e.getMessage());
        }
    }

    /**
     * 4. HÀM TRÍCH XUẤT PUBLIC ID (Helper)
     * Lấy ID từ URL để phục vụ hàm xóa.
     */
    private String extractPublicId(String url) {
        try {
            // URL dạng: .../upload/v12345/ticketrush/avatars/abcxyz.jpg
            String parts[] = url.split("/upload/");
            String path = parts[1].substring(parts[1].indexOf("/") + 1);
            return path.substring(0, path.lastIndexOf("."));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 5. HÀM KIỂM TRA ĐỊNH DẠNG (Validation)
     * Đảm bảo user không upload nhầm file bậy bạ.
     */
    public void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new TicketRushException("Chỉ chấp nhận file ảnh (jpg, png, webp)!", HttpStatus.BAD_REQUEST);
        }
        // Giới hạn 5MB cho ảnh
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new TicketRushException("Ảnh quá nặng! Vui lòng chọn ảnh < 5MB.", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * UPLOAD ẢNH SỰ KIỆN (EVENT BANNER)
     * Tỉ lệ chuẩn 16:9, tự động căn giữa và tối ưu dung lượng.
     */
    public String uploadEventBanner(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ticketrush/events",
                            // Tỉ lệ 16:9, chiều rộng 1280px, tự động nén và chọn định dạng tốt nhất
                            "transformation", "c_fill,ar_16:9,w_1280,q_auto,f_auto"
                    ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new TicketRushException("Lỗi upload banner sự kiện!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * UPLOAD ẢNH VÉ (TICKET)
     * Thường là ảnh nhỏ hoặc QR code, cần giữ nguyên nội dung nhưng resize lại cho gọn.
     */
    public String uploadTicketImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "ticketrush/tickets",
                            // Chỉ giới hạn chiều rộng 600px, chiều cao tự nhảy theo tỉ lệ gốc (c_limit)
                            "transformation", "c_limit,w_600,q_auto,f_auto"
                    ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new TicketRushException("Lỗi upload ảnh vé!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}