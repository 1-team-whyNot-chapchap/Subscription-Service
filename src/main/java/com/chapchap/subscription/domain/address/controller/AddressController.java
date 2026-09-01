package com.chapchap.subscription.domain.address.controller;

import com.chapchap.subscription.domain.address.request.AddressCreateRequest;
import com.chapchap.subscription.domain.address.request.AddressUpdateRequest;
import com.chapchap.subscription.domain.address.response.*;
import com.chapchap.subscription.domain.address.service.AddressService;
import com.chapchap.subscription.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription/addresses")
public class AddressController {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final AddressService addressService;

    // 배송지 조회
    @GetMapping
    public GlobalResponse<AddressListResponse> getAddresses(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        return GlobalResponse.success(
                addressService.getAddresses(userId)
        );
    }

    // 배송지 등록
    @PostMapping
    public GlobalResponse<AddressCreateResponse> createAddress(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        return GlobalResponse.success(
                addressService.createAddress(userId, request)
        );
    }

    // 배송지 수정
    @PatchMapping("/{addressId}")
    public GlobalResponse<AddressUpdateResponse> updateAddress(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable String addressId,
            @Valid @RequestBody AddressUpdateRequest request
    ) {
        return GlobalResponse.success(
                addressService.updateAddress(
                        userId,
                        addressId,
                        request
                )
        );
    }

    // 기본 배송지 설정
    @PatchMapping("/{addressId}/default")
    public GlobalResponse<AddressDefaultResponse> setDefaultAddress(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable String addressId
    ) {
        return GlobalResponse.success(
                addressService.setDefaultAddress(
                        userId,
                        addressId
                )
        );
    }

    // 배송지 삭제
    @DeleteMapping("/{addressId}")
    public GlobalResponse<AddressDeleteResponse> deleteAddress(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable String addressId
    ) {
        return GlobalResponse.success(
                addressService.deleteAddress(
                        userId,
                        addressId
                )
        );
    }
}
