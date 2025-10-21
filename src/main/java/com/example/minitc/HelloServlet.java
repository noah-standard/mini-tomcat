package com.example.minitc;

public class HelloServlet implements Servlet{

    @Override
    public void service(HttpRequest req, HttpResponse res) throws Exception {
        String name = req.query.getOrDefault("name", "world");
        res.header("Content-Type","text/plain; charset=UTF-8");
        res.body("Hello " + name + "!\n(method=" + req.method + ", path=" + req.path + ")");
    }
}
