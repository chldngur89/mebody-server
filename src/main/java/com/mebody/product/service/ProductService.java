package com.mebody.product.service;

import com.mebody.common.exception.NotFoundException;
import com.mebody.product.domain.ProductStatus;
import com.mebody.product.dto.ProductDto;
import com.mebody.product.repository.ProductRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Transactional(readOnly = true)
  public List<ProductDto> publicProducts() {
    return productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE).stream()
        .map(ProductDto::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public ProductDto product(UUID id) {
    return productRepository.findById(id)
        .filter(product -> product.getStatus() == ProductStatus.ACTIVE)
        .map(ProductDto::from)
        .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
  }
}
