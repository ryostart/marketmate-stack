package com.marketmate.favorite;

import com.marketmate.common.security.CurrentUserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoriteController.class)
// ← ここが肝。Securityフィルタをテストから外す（401/403や未認証処理を無効化）
@AutoConfigureMockMvc(addFilters = false)
class FavoriteControllerTest {

    private static final UUID USER =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    MockMvc mvc;

    @MockBean
    FavoriteService service;

    @Test
    void list_returnsJsonArray() throws Exception {
        var e = Favorite.builder()
                .userId(USER)
                .placeId("P1")
                .createdAt(Instant.parse("2024-02-01T00:00:00Z"))
                .build();
        given(service.list(USER)).willReturn(List.of(e));

        mvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placeId").value("P1"))
                .andExpect(jsonPath("$[0].userId").value(USER.toString()))
                .andExpect(jsonPath("$[0].createdAt")
                        .value(Instant.parse("2024-02-01T00:00:00Z").toEpochMilli()));
    }

    @Test
    void add_returnsOk_andBody() throws Exception {
        var created = Favorite.builder()
                .userId(USER)
                .placeId("SUPERMARKET_123")
                .createdAt(Instant.parse("2024-02-01T00:00:00Z"))
                .build();
        given(service.add(eq(USER), anyString())).willReturn(created);

        mvc.perform(post("/api/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placeId\":\"SUPERMARKET_123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeId").value("SUPERMARKET_123"))
                .andExpect(jsonPath("$.userId").value(USER.toString()))
                .andExpect(jsonPath("$.createdAt")
                        .value(Instant.parse("2024-02-01T00:00:00Z").toEpochMilli()));
    }

    @Test
    void delete_returnsOk() throws Exception {
        willDoNothing().given(service).remove(USER, "SUPERMARKET_123");

        mvc.perform(delete("/api/favorites/{placeId}", "SUPERMARKET_123"))
                .andExpect(status().isOk());

        then(service).should().remove(USER, "SUPERMARKET_123");
    }

    // @CurrentUserId UUID をテストで固定解決
    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Bean
        HandlerMethodArgumentResolver currentUserIdResolver() {
            return new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter p) {
                    return p.hasParameterAnnotation(CurrentUserId.class)
                            && p.getParameterType().equals(UUID.class);
                }

                @Override
                public Object resolveArgument(
                        MethodParameter p, ModelAndViewContainer c,
                        NativeWebRequest w, WebDataBinderFactory f) {
                    return USER;
                }
            };
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(currentUserIdResolver());
        }
    }
}
