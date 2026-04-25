package com.uet.ticketrush.services;

import com.uet.ticketrush.dtos.ChangePasswordRequest;
import com.uet.ticketrush.dtos.UserInformationResponseDTO;
import com.uet.ticketrush.dtos.UserUpdateProfileDTO;
import com.uet.ticketrush.exceptions.TicketRushException;
import com.uet.ticketrush.models.User;
import com.uet.ticketrush.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    @Qualifier("userAuthenticationManager")
    private AuthenticationManager authManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    public String verify(User user) {
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                user.getPassword()
                        ));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername());
        }

        return "fail";
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new TicketRushException("Không tìm thấy user: " + username, HttpStatus.NOT_FOUND);
        }

        if (!encoder.matches(request.oldPassword(), user.getPassword())) {
            throw new TicketRushException("Old Password is Wrong!", HttpStatus.BAD_REQUEST);
        }

        if (encoder.matches(request.newPassword(), user.getPassword())) {
            throw new TicketRushException("Mật khẩu mới không được trùng mật khẩu cũ!", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(encoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public UserInformationResponseDTO getDataByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new TicketRushException("Không tìm thấy user: " + username, HttpStatus.NOT_FOUND); //404
        }

        return UserInformationResponseDTO.fromEntity(user);
    }

    public User updateProfile(String username, UserUpdateProfileDTO dto) {
        User user = userRepository.findByUsername(username);
        user.updateProfile(dto.displayName(), dto.age(), dto.gender());
        return userRepository.save(user);
    }

    public String updateProfileAvatar(String username, MultipartFile file) {
        // 1. Kiểm tra file có phải ảnh không
        cloudinaryService.validateImage(file);

        // 2. Lấy user hiện tại
        User user = userRepository.findByUsername(username);

        // 3. Xóa ảnh old trên cloud (Nếu có)
        if (user.getAvatarUrl() != null) {
            cloudinaryService.deleteImage(user.getAvatarUrl());
        }

        // 4. Upload ảnh mới
        String newUrl = cloudinaryService.uploadAvatar(file);

        // 5. Lưu vào DB
        user.setAvatarUrl(newUrl);
        userRepository.save(user);

        return newUrl;
    }
}