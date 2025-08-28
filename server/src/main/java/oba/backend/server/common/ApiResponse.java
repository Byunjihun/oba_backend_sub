package oba.backend.server.common;

/**
 * @param data 제네릭 타입 T를 사용하여 어떤 종류의 데이터든 담을 수 있습니다.
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    // 생성자를 private으로 선언하여 정적 팩토리 메소드로만 객체를 생성하도록 유도합니다.

    // 성공 응답을 생성하는 정적 팩토리 메소드 (데이터 포함)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 성공 응답을 생성하는 정적 팩토리 메소드 (데이터 미포함)
    public static <T> ApiResponse<T> success(String message) {
        // 데이터가 없는 경우 null을 전달합니다.
        return new ApiResponse<>(true, message, null);
    }

    // 실패 응답을 생성하는 정적 팩토리 메소드 (추후 예외 처리에서 사용)
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}