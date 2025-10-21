package com.example.minitc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    // 응답을 출력할 출력 스트림
    private final OutputStream out;
    // HTTP 상태 코드 (기본값: 200)
    private int status = 200;
    // HTTP 헤더를 저장하는 맵
    private final Map<String, String> headers = new LinkedHashMap<>();
    // 응답 본문 데이터
    private byte[] body = new byte[0];

    /**
     * HttpResponse 객체를 생성하고 기본 헤더를 설정합니다.
     *
     * @param out 응답을 쓸 출력 스트림
     */
    public HttpResponse(OutputStream out) {
        this.out = out;
        header("Server","MiniTomcat/0.1");
        header("Connection","close");
        header("Content-Type","text/plain; charset=UTF-8");
    }

    /**
     * HTTP 상태 코드를 설정합니다.
     *
     * @param code HTTP 상태 코드
     */
    public void status(int code) {
        this.status = code;
    }

    /**
     * HTTP 헤더를 추가합니다.
     *
     * @param k 헤더 이름
     * @param v 헤더 값
     */
    public void header(String k, String v) {
        headers.put(k, v);
    }

    /**
     * 문자열 응답 본문을 설정합니다.
     *
     * @param text 응답 본문 문자열
     */
    public void body(String text) {
        this.body = text.getBytes(StandardCharsets.UTF_8);
        header("Content-Length", String.valueOf(body.length));
    }

    /**
     * 바이트 배열 응답 본문을 설정합니다.
     *
     * @param bytes 응답 본문 바이트 배열
     */
    public void body(byte[] bytes) {
        this.body = bytes;
        header("Content-Length", String.valueOf(body.length));
    }

    /**
     * HTTP 응답을 클라이언트에게 전송합니다.
     *
     * @throws IOException 입출력 오류 발생 시
     */
    public void send() throws IOException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        pw.printf("HTTP/1.1 %d %s\r\n",status,reason(status));
        for(var e : headers.entrySet()){
            pw.printf("%s: %s\r\n",e.getKey(),e.getValue());
        }
        pw.println("\r\n");
        pw.flush();
        out.write(body);
        out.flush();
    }

    /**
     * HTTP 상태 코드에 해당하는 상태 메시지를 반환합니다.
     *
     * @param s HTTP 상태 코드
     * @return 상태 메시지
     */
    private static String reason(int s){
        return switch (s) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }
}
