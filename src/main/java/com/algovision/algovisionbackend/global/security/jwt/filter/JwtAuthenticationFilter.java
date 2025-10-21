package com.algovision.algovisionbackend.global.security.jwt.filter;

import com.algovision.algovisionbackend.global.security.jwt.JwtProvider;
import com.algovision.algovisionbackend.global.security.jwt.service.JwtRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
@Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final JwtRedisService jwtRedisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            if (jwtRedisService.isBlacklisted(token)) {
                log.warn("블랙리스트 토큰 차단: {}", token);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"blacklisted_token\",\"message\":\"This token has been logged out.\"}");
                return;
            }

            if (jwtProvider.validateToken(token)) {
                Long memberId = jwtProvider.getMemberId(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(memberId));

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(memberId, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

                MDC.put("userId", String.valueOf(memberId));
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
