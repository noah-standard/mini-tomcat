package com.example.minitc;

import java.util.HashMap;
import java.util.Map;

public class ServletRegistry {
    private final Map<String, Servlet> routes = new HashMap<>();

    /**
     * 서블릿을 URL 경로에 등록하는 메소드
     * 경로 끝의 슬래시를 제거하여 정규화함 (루트 경로 제외)
     */
    public void register(String path, Servlet servlet){
        // 정규화: 끝 슬래시 제거(루트 제외)
        if(path.startsWith("/") && path.length() > 1) path = path.substring(0,path.length()-1);
        routes.put(path, servlet);
    }

    /**
     * 주어진 URL 경로에 매칭되는 서블릿을 찾는 메소드
     * 정확한 경로 매칭을 우선하고, 없을 경우 가장 긴 접두어 매칭을 시도함
     */
    public Servlet match(String path){
        if(path == null || path.isEmpty()) return routes.get("/");
        // 정적 매칭 우선
        if(routes.containsKey(path)) return routes.get(path);
        // prefix 기반 가장 긴 경로 매칭(간단 버전)
        Servlet best = null;
        int bestLen = -1;
        for(String p : routes.keySet()){
            if(path.startsWith(p) && p.length() > bestLen){
                best = routes.get(p);
                bestLen = p.length();
            }
        }
        return best;
    }
}
