package com.marketmate.memo;

import com.marketmate.common.security.CurrentUserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingMemoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShoppingMemoControllerTest {

    private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant T = Instant.parse("2024-02-01T00:00:00Z");

    @Autowired
    MockMvc mvc;

    @MockBean
    ShoppingMemoService service;

    @Test
    void list_returnsJsonArray() throws Exception {
        var memo = ShoppingMemo.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .userId(USER)
                .placeId("P1")
                .title("Milk list")
                .createdAt(T)
                .updatedAt(T)
                .build();
        memo.setLines(List.of(
                ShoppingMemoLine.builder().memo(memo).pos(1).text("Banana").build(),
                ShoppingMemoLine.builder().memo(memo).pos(2).text("Milk").build()
        ));

        given(service.list(USER)).willReturn(List.of(memo));

        mvc.perform(get("/api/memos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(memo.getId().toString()))
                .andExpect(jsonPath("$[0].userId").value(USER.toString()))
                .andExpect(jsonPath("$[0].placeId").value("P1"))
                .andExpect(jsonPath("$[0].title").value("Milk list"))
                .andExpect(jsonPath("$[0].lines[0]").value("Banana"))
                .andExpect(jsonPath("$[0].lines[1]").value("Milk"))
                .andExpect(jsonPath("$[0].createdAt").value(T.toEpochMilli()))
                .andExpect(jsonPath("$[0].updatedAt").value(T.toEpochMilli()));
    }

    @Test
    void add_returnsOk_andBody() throws Exception {
        var created = ShoppingMemo.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-0000000000AA"))
                .userId(USER)
                .placeId("SUPERMARKET_123")
                .title("買い物メモ")
                .createdAt(T)
                .updatedAt(T)
                .build();
        created.setLines(List.of(
                ShoppingMemoLine.builder().memo(created).pos(1).text("Banana").build(),
                ShoppingMemoLine.builder().memo(created).pos(2).text("Milk").build()
        ));

        given(service.add(eq(USER), eq("SUPERMARKET_123"), eq("買い物メモ"), anyList()))
                .willReturn(created);

        mvc.perform(post("/api/memos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {"placeId":"SUPERMARKET_123","title":"買い物メモ","lines":["Banana","Milk"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.userId").value(USER.toString()))
                .andExpect(jsonPath("$.placeId").value("SUPERMARKET_123"))
                .andExpect(jsonPath("$.title").value("買い物メモ"))
                .andExpect(jsonPath("$.lines[0]").value("Banana"))
                .andExpect(jsonPath("$.lines[1]").value("Milk"))
                .andExpect(jsonPath("$.createdAt").value(T.toEpochMilli()))
                .andExpect(jsonPath("$.updatedAt").value(T.toEpochMilli()));
    }

    @Test
    void delete_returnsOk() throws Exception {
        var id = UUID.fromString("00000000-0000-0000-0000-00000000DE1E");
        willDoNothing().given(service).remove(USER, id);

        mvc.perform(delete("/api/memos/{id}", id.toString()))
                .andExpect(status().isOk());
    }

    // @CurrentUserId を固定 UUID に解決する Resolver を注入
    @TestConfiguration
    static class TestCfg implements WebMvcConfigurer {
        @Bean
        HandlerMethodArgumentResolver currentUserIdResolver() {
            return new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(CurrentUserId.class)
                            && parameter.getParameterType().equals(UUID.class);
                }

                @Override
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              org.springframework.web.context.request.NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
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
