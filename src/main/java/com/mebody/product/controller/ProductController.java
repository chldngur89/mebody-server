package com.mebody.product.controller;

import com.mebody.common.response.ApiResponse;
import com.mebody.product.dto.ProductDto;
import com.mebody.product.service.ProductService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public ApiResponse<List<ProductDto>> products() {
    return ApiResponse.ok(productService.publicProducts());
  }

  @GetMapping("/{id}")
  public ApiResponse<ProductDto> product(@PathVariable UUID id) {
    return ApiResponse.ok(productService.product(id));
  }
}
