package com.example.minitc;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MiniTomcat {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("port", "8080"));
        Path staticRoot = Path.of(System.getProperty("docRoot", "public")).toAbsolutePath();
        System.out.println("PORT: " + System.getProperty("port"));


        // 1) 라우팅 등록
        ServletRegistry registry = new ServletRegistry();
        registry.register("/hello", new HelloServlet());

        // 2) 정적 파일 핸들러
        StaticFileHandler staticHandler = new StaticFileHandler(staticRoot);
        boolean mkdirs = new File(staticRoot.toString()).mkdirs();

        if(!mkdirs) {
            System.err.println("Failed to create docRoot: " + staticRoot);
        }

        // 3) 스레드풀 + 서버 소켓
        // 스레드풀 설정: 코어 스레드 10개, 최대 100개, 유휴 시간 60초
        // 데몬 스레드로 생성하여 메인 스레드 종료시 함께 종료되도록 함
        ExecutorService pool = new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new SynchronousQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("worker-" + t.getId());
                    t.setDaemon(true);
                    return t;
                });

        // 서버 소켓 생성 및 클라이언트 요청 처리 루프
        // try-with-resources로 서버 소켓 자동 종료 보장
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("MiniTomcat started on http://localhost:" + port + " (docRoot=" + staticRoot + ")");
            while (true) {
                // 클라이언트 연결 수락 후 스레드풀에서 요청 처리
                Socket client = server.accept();
                pool.execute(new ClientHandler(client, registry, staticHandler));
            }
        }
    }
}
