
package com.example.minitc;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable{

    private final Socket socket;
    private final ServletRegistry registry;
    private final StaticFileHandler staticHandler;

    /**
     * 클라이언트 요청을 처리하는 핸들러를 초기화합니다.
     *
     * @param socket        클라이언트 소켓 연결
     * @param registry      서블릿 레지스트리
     * @param staticHandler 정적 파일 핸들러
     */
    public ClientHandler(Socket socket, ServletRegistry registry, StaticFileHandler staticHandler) {
        this.socket = socket;
        this.registry = registry;
        this.staticHandler = staticHandler;
    }

    /**
     * 클라이언트 요청을 처리하는 메인 실행 메소드입니다.
     * 1. HTTP 요청을 파싱합니다.
     * 2. 매칭되는 서블릿이 있다면 해당 서블릿으로 요청을 처리합니다.
     * 3. 서블릿이 없다면 정적 파일 제공을 시도합니다.
     * 4. 모든 처리가 실패하면 404 에러를 반환합니다.
     * 5. 처리 중 예외가 발생하면 500 에러를 반환합니다.
     */
    @Override
    public void run() {
        try (socket; InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()){
            HttpRequest req = HttpRequest.parse(in);
            HttpResponse res = new HttpResponse(out);

            if("/favicon.ico".equals(req.path)) {
                res.status(204);
                res.header("Content-Type","image/x-icon");
                res.body(new byte[0]);
                res.send();
                return;
            }

            Servlet servlet = registry.match(req.path);
            if(servlet != null){
                servlet.service(req,res);
                res.send();
                return;
            }
            // 서블릿 없으면 정적파일 시도
            if(staticHandler.tryServe(req.path,res)){
                res.send();
                return;
            }
            res.status(404);
            res.body("Not Found");
            res.send();
        } catch (Exception e) {
            try{
                HttpResponse res = new HttpResponse(socket.getOutputStream());
                res.status(500);
                res.body("Internal Server Error: " + e.getMessage());
                res.send();
            }catch (Exception ignore){}
        }
    }
}
