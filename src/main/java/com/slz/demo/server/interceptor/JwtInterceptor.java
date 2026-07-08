package com.slz.demo.server.interceptor;

import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.JwtUtil;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.ao.RoleAO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * JWT 登录拦截器
 * <p>
 * 默认所有接口需要登录，PUBLIC_PATHS 中的路径允许游客访问。
 * 携带有效 Token 访问公开接口时仍会解析用户身份，便于接口区分登录状态。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 公开路径白名单（不需要登录即可访问）
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/user/register",
            "/user/login",
            // 帖子浏览
            "/topic/selectAll",
            "/topic/page",
            "/topic/detail/*",
            // 回复浏览
            "/reply/top/page",
            "/reply/child/page",
            // 分类浏览
            "/category/parents",
            "/category/children/*",
            "/category/search",
            "/category/selectAll",
            // 标签浏览
            "/tag/selectAll",
            "/tag/search",
            // 附件访问
            "/attachment/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String method = request.getMethod();
        String path = request.getServletPath();
        boolean isPublic = isPublicPath(path);

        log.debug("请求: {} {}", method, path);

        // 解析 Token：优先 Authorization Header，回退到 Cookie
        String token = resolveToken(request);

        // 有 Token 时尝试解析（公开接口也解析，以便获取用户身份）
        if (token != null && !token.isBlank()) {
            try {
                RoleAO ao = jwtUtil.parseToken(token);
                UserContext.set(ao);
                return true;
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                // 公开接口 Token 无效时仍允许访问，只是没有身份
                if (!isPublic) {
                    log.warn("Token解析失败: {} {} - {}", method, path, e.getMessage());
                    throw new BusinessException(ErrorCode.TOKEN_INVALID);
                }
                log.debug("公开接口Token解析失败，以游客身份继续: {} {}", method, path);
                return true;
            }
        }

        // 无 Token：公开接口放行，其余拒绝
        if (isPublic) {
            log.debug("游客访问公开接口: {} {}", method, path);
            return true;
        }

        log.warn("未登录访问受保护接口: {} {}", method, path);
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求中解析 JWT Token
     * <p>
     * 优先从 Authorization Header 读取（主站后端服务调用），
     * 回退到 Cookie 读取（浏览器自动携带）。
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. 从 Authorization Header 取
        String auth = request.getHeader("Authorization");
        if (auth != null && !auth.isBlank()) {
            if (!auth.startsWith("Bearer ")) {
                log.warn("Token格式错误");
                throw new BusinessException(ErrorCode.TOKEN_INVALID);
            }
            return auth.substring(7);
        }

        // 2. 从 Cookie 取（前端已改为 Cookie 存储）
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
