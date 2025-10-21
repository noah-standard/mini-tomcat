package com.example.minitc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP 요청을 나타내는 클래스입니다.
 */
public class HttpRequest {
    public String method;
    public String path;
    public String httpVersion;
    public Map<String, String> headers = new HashMap<>();
    public Map<String, String> query = new HashMap<>();
    public String body;

    /**
     * HttpRequest 인스턴스를 생성하는 private 생성자입니다.
     *
     * @param method      HTTP 메소드 (GET, POST 등)
     * @param path        요청 경로
     * @param httpVersion HTTP 버전
     * @param body        요청 본문
     */
    private HttpRequest(String method, String path, String httpVersion, String body) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.body = body;
    }

    /**
     * 입력 스트림에서 HTTP 요청을 파싱합니다.
     *
     * @param in HTTP 요청이 포함된 입력 스트림
     * @return 파싱된 요청을 나타내는 새로운 HttpRequest 객체
     * @throws IOException 파싱 중 I/O 오류가 발생한 경우
     */
    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String requestLine = br.readLine();
        if(requestLine == null || requestLine.isEmpty()) throw new IOException("Invalid request");

        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String url = parts[1];
        String version = parts.length > 2 ? parts[2] : "HTTP/1.1";


        String line;
        int contentLen = 0;
        Map<String, String> headers = new HashMap<>();
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if(idx > 0) {
                String k = line.substring(0, idx).trim();
                String v = line.substring(idx + 1).trim();
                headers.put(k.toLowerCase(Locale.ROOT), v);
            }
        }

        if(headers.containsKey("content-length")){
            try { contentLen = Integer.parseInt(headers.get("content-length")); } catch (Exception e) {}
        }
        char[] bodyBuf = new char[contentLen];
        int read = 0;
        while(read < contentLen){
            int r = br.read(bodyBuf, read, contentLen - read);
            if(r < 0) break;
            read += r;
        }
        String body = new String(bodyBuf, 0, read);

        String pathOnly = url;
        Map<String, String> query = new HashMap<>();
        int q = url.indexOf("?");
        if(q >= 0){
            pathOnly = url.substring(0, q);
            String qs = url.substring(q + 1);
            for(String kv : qs.split("&")){
                if(kv.isEmpty()) continue;
                String[] kvp = kv.split("=");
                String k = URLDecoder.decode(kvp[0],StandardCharsets.UTF_8);
                String v = kvp.length > 1 ? URLDecoder.decode(kvp[1],StandardCharsets.UTF_8) : "";
                query.put(k, v);
            }
        }

        HttpRequest req = new HttpRequest(method, pathOnly, version, body);
        req.headers.putAll(headers);
        req.query.putAll(query);
        return req;
    }

    /**
     * 지정된 HTTP 헤더의 값을 반환합니다.
     *
     * @param name 검색할 헤더의 이름
     * @return 헤더 값, 없는 경우 null 반환
     */
    public String header(String name){
        return headers.getOrDefault(name.toLowerCase(Locale.ROOT), null);
    }
}
