package myshop.shop.controller;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import myshop.shop.exception.MemberMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.FileNotFoundException;

@Slf4j
@ControllerAdvice
public class ExceptionController {


    /**
     * JPA / DB 관련 예외
     * 트랜잭션 롤백
     */
    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String entityExistsException(Model model, Exception e) {
        log.error("EntityExistsException: {}", e.getMessage(), e);
        setExceptionModel(model, "500", "서버 내부 오류가 발생했습니다.", "동일한 데이터가 이미 있습니다.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * JPA / DB 관련 예외
     * 트랜잭션 롤백
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String entityNotFoundException(Model model, Exception e) {
        log.info("EntityNotFoundException: {}", e.getMessage());
        setExceptionModel(model, "404", "정보를 찾을 수 없습니다.", "요청하신 데이터가 삭제되었거나 존재하지 않습니다.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * JPA / DB 관련 예외
     * 트랜잭션 롤백 (충돌)
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String optimisticLockException(Model model, Exception e) {
        log.warn("OptimisticLockException: {}", e.getMessage());
        setExceptionModel(model, "409", "다른 사용자가 데이터를 수정했습니다.", "새로고침 후 다시 시도해 주세요 (버전 충돌)\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * JPA / DB 관련 예외
     * 트랜잭션 롤백 (충돌)
     */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String pessimisticLockException(Model model, Exception e) {
        log.warn("PessimisticLockException: {}", e.getMessage());
        setExceptionModel(model, "503", "요청이 지연되고 있습니다.", "현재 이용자가 많아 처리가 지연되었습니다. 잠시 후 다시 시도해 주세요.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * JPA / DB 관련 예외
     * (시간 초과)
     */
    @ExceptionHandler({LockTimeoutException.class, QueryTimeoutException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String jpaTimeoutException(Model model, Exception e) {
        log.warn("JpaTimeoutException: {}", e.getMessage());
        setExceptionModel(model, "503", "처리 시간 초과", "현재 서버 부하가 많아 요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * JPA / DB 관련 예외
     * 트랜잭션 롤백
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String dataIntegrityViolationException(Model model, Exception e) {
        log.warn("DataIntegrityViolationException: {}", e.getMessage());
        setExceptionModel(model, "400", "잘못된 요청입니다.", "입력하신 데이터가 규칙에 맞지 않거나 이미 존재합니다.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * JPA / DB 관련 예외
     */
    @ExceptionHandler(NoResultException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noResultException(Model model, Exception e) {
        log.info("NoResultException: {}", e.getMessage());
        setExceptionModel(model, "404", "조회 결과가 없습니다.", "요청하신 조건에 맞는 데이터가 존재하지 않습니다.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * JPA / DB 관련 예외
     */
    @ExceptionHandler(NonUniqueResultException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String nonUniqueResultException(Model model, Exception e) {
        log.error("NonUniqueResultException: {}", e.getMessage(), e);
        setExceptionModel(model, "400", "데이터 중복 오류", "시스템 내부에서 중복된 데이터가 발견되어 처리에 실패했습니다.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * JPA / DB 관련 예외
     * 500: 트랜잭션 관련 (Rollback, Required)
     */
    @ExceptionHandler({RollbackException.class, TransactionRequiredException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleTransactionError(Model model, Exception e) {
        log.error("TransactionError: {}", e.getMessage(), e);
        setExceptionModel(model, "500", "트랜잭션 처리 오류", "데이터 저장 중 시스템 오류가 발생하여 모든 변경 사항이 취소되었습니다.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * Java 기본 RuntimeException 관련 예외
     */
    @ExceptionHandler({IllegalArgumentException.class, NumberFormatException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Model model, Exception e) {
        log.info("BadRequestException: {}", e.getMessage());
        setExceptionModel(model, "400", "잘못된 입력값", "전달된 파라미터가 유효하지 않거나 형식이 맞지 않습니다.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * Java 기본 RuntimeException 관련 예외
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String nullPointerException(Model model, Exception e) {
        log.error("NullPointerException: {}", e.getMessage(), e);
        setExceptionModel(model, "500", "애플리케이션 오류", "서버 내부 로직 처리 중 문제가 발생했습니다. null 객체의 멤버를 호출\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * Java 기본 RuntimeException 관련 예외
     */
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String arrayIndexOutOfBoundsException(Model model, Exception e) {
        log.error("ArrayIndexOutOfBoundsException: {}", e.getMessage(), e);
        setExceptionModel(model, "500", "애플리케이션 오류", "서버 내부 로직 처리 중 문제가 발생했습니다. 배열의 범위를 벗어난 인덱스에 접근\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * Java 기본 RuntimeException 관련 예외
     */
    @ExceptionHandler(ClassCastException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String classCastException(Model model, Exception e) {
        log.error("ClassCastException: {}", e.getMessage(), e);
        setExceptionModel(model, "500", "애플리케이션 오류", "서버 내부 로직 처리 중 문제가 발생했습니다. 허용되지 않는 타입으로 객체를 형변환\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * Java 기본 RuntimeException 관련 예외
     */
    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String fileNotFoundException(Model model, Exception e) {
        log.info("FileNotFoundException: {}", e.getMessage());
        setExceptionModel(model, "404", "파일을 찾을 수 없습니다.", "요청하신 파일이 삭제되었거나, 경로가 잘못되었습니다. 파일명을 다시 확인해 주세요.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * Java 기본 RuntimeException 관련 예외
     */
    @ExceptionHandler(MemberMismatchException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden(Model model, Exception e) {
        log.info("Forbidden: {}", e.getMessage());
        setExceptionModel(model, "403", "회원 정보 불일치",
                "다른 사람의 정보(주문 내역 등)에 접근하거나 조작하였습니다.\n" + e.getMessage());
        return "error/generic_error";
    }


    /**
     * 나머지 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String runtimeException(RuntimeException e, Model model) {
        log.error("RuntimeException 발생: {}", e.getMessage(), e);
        setExceptionModel(model, "500", "서버 내부 오류가 발생했습니다.", "시스템에 일시적인 문제가 생겼습니다. 잠시 후 다시 시도해 주세요.");
        return "error/generic_error";
    }

    private void setExceptionModel(Model model, String code, String message, String detail) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("detail", detail);
    }
}
