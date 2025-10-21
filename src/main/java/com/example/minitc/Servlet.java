package com.example.minitc;

public interface Servlet {
    void service(HttpRequest req, HttpResponse res) throws Exception;
}