package com.uet.ticketrush.exceptions;

import com.uet.ticketrush.dtos.ErrorResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketRushException.class)
    public ResponseEntity<ErrorResponseDTO> handleTicketRushException(TicketRushException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getStatus().value(), // Lấy http status(410, 404, v.v.)
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, ex.getStatus());
    }

    //Hứng các lỗi Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDTO> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        // Trả về 409 Conflict thay vì 500
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Ghế vừa được người khác đặt xong, vui lòng chọn ghế khác!",
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Trích xuất thông tin lỗi thực tế để debug dễ hơn
        String message = "Lỗi dữ liệu hệ thống (Vi phạm ràng buộc)";
        if (ex.getRootCause() != null) {
            message = ex.getRootCause().getMessage();
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                message,
                System.currentTimeMillis()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleJsonError(HttpMessageNotReadableException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu gửi lên không đúng định dạng JSON hoặc sai kiểu dữ liệu!",
                System.currentTimeMillis()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Không tìm thấy dữ liệu yêu cầu (Entity not found)",
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Hứng lỗi 404 - Khi Frontend gọi sai đường dẫn (URL không tồn tại)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = String.format("API không tồn tại: %s %s. Vui lòng kiểm tra lại đường dẫn!",
                ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                message,
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Hứng lỗi 405 - Khi Frontend gọi đúng URL nhưng sai Method (VD: API cần POST nhưng gọi GET)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("Phương thức %s không được hỗ trợ cho API này. Thử lại với: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                message,
                System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralError(Exception ex) {

        log.error("Lỗi hệ thống chưa xác định: ", ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Hệ thống đang lỗi: " + ex.getClass().getSimpleName(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(500).body(error);
    }
}
