package com.mebody.seller.dto;

import java.util.List;

public record SellerDashboardResponse(
    String status,
    List<String> cards,
    List<String> todos
) {
  public static SellerDashboardResponse preparing() {
    return new SellerDashboardResponse(
        "판매 기능 준비 중",
        List.of("상품 관리", "주문 관리", "매출 통계", "정산 관리"),
        List.of("상품 등록", "상품 이미지 업로드", "장바구니", "주문", "결제", "정산")
    );
  }
}
