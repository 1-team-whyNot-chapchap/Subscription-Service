package com.chapchap.subscription.domain.subscription.controller;

import com.chapchap.subscription.domain.subscription.response.PlanDetailResponse;
import com.chapchap.subscription.domain.subscription.response.PlanListResponse;
import com.chapchap.subscription.domain.subscription.service.PlanQueryService;
import com.chapchap.subscription.global.response.GlobalResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanControllerTest {
    @Test
    void 공개_플랜_목록을_조회한다() {
        PlanQueryService service = mock(PlanQueryService.class);
        PlanListResponse expected = new PlanListResponse(List.of());
        when(service.getPlans()).thenReturn(expected);

        GlobalResponse<PlanListResponse> response = new PlanController(service).getPlans();

        assertThat(response.data()).isSameAs(expected);
        verify(service).getPlans();
    }

    @Test
    void 공개_식별자로_플랜_상세를_조회한다() {
        PlanQueryService service = mock(PlanQueryService.class);
        PlanDetailResponse expected = new PlanDetailResponse(
            "PLN-public", "가정식", "설명", 8_900L, List.of()
        );
        when(service.getPlan("PLN-public")).thenReturn(expected);

        GlobalResponse<PlanDetailResponse> response =
            new PlanController(service).getPlan("PLN-public");

        assertThat(response.data()).isSameAs(expected);
        verify(service).getPlan("PLN-public");
    }
}
