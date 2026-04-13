package com.uet.ticketrush.controllers;

import com.uet.ticketrush.services.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout (@RequestParam UUID holdId) {
        checkoutService.checkout(holdId);
        return ResponseEntity.ok("Thanh toán và xuất vé thành công!");
    }
}
