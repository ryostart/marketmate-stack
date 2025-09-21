package com.marketmate.review;

import com.marketmate.common.security.CurrentUserId;
import com.marketmate.review.ReviewService.Summary;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    private static final UUID USER =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID1 =
            UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    private static final UUID ID2 =
            UUID.fromString("00000000-0000-0000-0000-0000000000bb");
    private static final Instant T0 = Instant.parse("2024-02-01T00:00:00Z");

    @Autowired
    MockMvc mvc;

    @MockBean
    ReviewService service;

    @Test
    void list_returnsJsonArray() throws Exception {
        var r1 = Review.builder()
                .id(ID1).userId(USER).placeId("p1").rating(5).comment("good")
                .createdAt(T0).build();
        var r2 = Review.builder()
                .id(ID2).userId(USER).placeId("p1").rating(3).comment("ok")
                .createdAt(T0.plusSeconds(60)).build();

        BDDMockito.given(service.list("p1")).willReturn(List.of(r2, r1));

        mvc.perform(get("/api/reviews").param("placeId", "p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID2.toString()))
                .andExpect(jsonPath("$[0].userId").value(USER.toString()))
                .andExpect(jsonPath("$[0].placeId").value("p1"))
                .andExpect(jsonPath("$[0].rating").value(3))
                .andExpect(jsonPath("$[0].comment").value("ok"))
                .andExpect(jsonPath("$[0].createdAt").value(T0.plusSeconds(60).toEpochMilli()))
                .andExpect(jsonPath("$[1].id").value(ID1.toString()))
                .andExpect(jsonPath("$[1].rating").value(5));
    }

    @Test
    void post_returnsOk_andBody() throws Exception {
        var created = Review.builder()
                .id(ID1).userId(USER).placeId("SHOP_123")
                .rating(3).comment("great").createdAt(T0).build();

        BDDMockito.given(service.add(eq(USER), eq("SHOP_123"), eq(3), eq("great")))
                .willReturn(created);

        mvc.perform(post("/api/reviews")
                        .with(csrf()) // addFilters=false でも付けておくと安全
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placeId\":\"SHOP_123\",\"rating\":3,\"comment\":\"great\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID1.toString()))
                .andExpect(jsonPath("$.userId").value(USER.toString()))
                .andExpect(jsonPath("$.placeId").value("SHOP_123"))
                .andExpect(jsonPath("$.rating").value(3))
                .andExpect(jsonPath("$.comment").value("great"))
                .andExpect(jsonPath("$.createdAt").value(T0.toEpochMilli()));
    }

    @Test
    void summary_returnsCountAndAverage() throws Exception {
        BDDMockito.given(service.summary("p1")).willReturn(new Summary(12L, 3.5));

        mvc.perform(get("/api/reviews/summary").param("placeId", "p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(12))
                .andExpect(jsonPath("$.average").value(3.5));
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
