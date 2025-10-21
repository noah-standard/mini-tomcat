package com.example.minitc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class StaticFileHandler {
    private final Path root;

    /**
     * 정적 파일 핸들러를 초기화합니다.
     *
     * @param root 정적 파일이 위치한 루트 디렉토리 경로
     */
    public StaticFileHandler(Path root) {
        this.root = root;
    }

    /**
     * 요청된 경로에 해당하는 정적 파일을 서빙하려고 시도합니다.
     *
     * @param path 요청된 파일 경로
     * @param res  HTTP 응답 객체
     * @return 파일 서빙 성공 여부
     */
    public boolean tryServe(String path, HttpResponse res){
        try{
            if(path.equals("/")) path = "/index.html";
            Path target = root.resolve("." + path).normalize();
            if(!target.startsWith(root)) return false;
            if(!Files.exists(target) || Files.isDirectory(target)) return false;

            String ct = contentType(target.toString());
            res.header("Content-type",ct);
            res.body(Files.readAllBytes(target));
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 파일 이름을 기반으로 MIME 타입을 결정합니다.
     *
     * @param filename 파일 이름
     * @return 파일에 해당하는 MIME 타입
     */
    public static String contentType(String filename){
        String l = filename.toLowerCase(Locale.ROOT);
        if(l.endsWith(".html") || l.endsWith(".htm")) return "text/html; charset=UTF-8";
        if(l.endsWith(".css")) return "text/css; charset=UTF-8";
        if(l.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if(l.endsWith(".json")) return "application/json; charset=UTF-8";
        if(l.endsWith(".png")) return "image/png";
        if(l.endsWith(".jpg") || l.endsWith(".jpeg")) return "image/jpeg";
        if(l.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}
