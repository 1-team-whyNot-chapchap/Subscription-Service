package com.chapchap.subscription.domain.payment.client;

import java.util.Arrays;

public enum PortOneCardCompany {

    KOREA_DEVELOPMENT_BANK("KDB산업은행 카드"),
    KFCC("새마을금고 카드"),
    SHINHYUP("신협 카드"),
    EPOST("우체국 카드"),
    SAVINGS_BANK_KOREA("저축은행 카드"),
    KAKAO_BANK("카카오뱅크 카드"),
    WOORI_CARD("우리카드"),
    BC_CARD("BC카드"),
    GWANGJU_CARD("광주카드"),
    SAMSUNG_CARD("삼성카드"),
    SHINHAN_CARD("신한카드"),
    HYUNDAI_CARD("현대카드"),
    LOTTE_CARD("롯데카드"),
    SUHYUP_CARD("수협카드"),
    CITI_CARD("씨티카드"),
    NH_CARD("NH 농협카드"),
    JEONBUK_CARD("전북카드"),
    JEJU_CARD("제주카드"),
    HANA_CARD("하나카드"),
    KOOKMIN_CARD("국민카드"),
    K_BANK("K뱅크 카드"),
    TOSS_BANK("토스뱅크 카드"),
    MIRAE_ASSET_SECURITIES("미래에셋증권 카드");

    private final String displayName;

    PortOneCardCompany(String displayName) {
        this.displayName = displayName;
    }

    public static String findDisplayName(String issuerCode) {
        if (issuerCode == null || issuerCode.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(company -> company.name().equals(issuerCode))
                .map(company -> company.displayName)
                .findFirst()
                .orElse(null);
    }
}
