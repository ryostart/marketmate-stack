package com.marketmate.auth;

import jakarta.servlet.http.Cookie;

public class CookieUtils {
    public static Cookie createHttpOnlyCookie(String name, String value, int maxAgeSeconds, boolean secure) {
        Cookie c = new Cookie(name, value);
        c.setHttpOnly(true);
        // ローカル開発では false、本番は true に
        c.setSecure(secure);
        c.setPath("/");
        c.setAttribute("SameSite", secure ? "None" : "Lax");
        c.setMaxAge(maxAgeSeconds);
        return c;
    }

    public static Cookie deleteCookie(String name, boolean secure) {
        Cookie c = new Cookie(name, "");
        c.setPath("/");
        c.setMaxAge(0);
        c.setHttpOnly(true);
        c.setSecure(secure);
        c.setAttribute("SameSite", secure ? "None" : "Lax");
        return c;
    }
}
