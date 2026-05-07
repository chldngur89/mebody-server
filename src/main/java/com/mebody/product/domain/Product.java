package com.mebody.product.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "seller_id")
  private UUID sellerId;

  @Column(nullable = false)
  private String name;

  private String description;
  private BigDecimal price;

  @Column(name = "image_url")
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductStatus status = ProductStatus.DRAFT;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime updatedAt;

  public UUID getId() { return id; }
  public UUID getSellerId() { return sellerId; }
  public String getName() { return name; }
  public String getDescription() { return description; }
  public BigDecimal getPrice() { return price; }
  public String getImageUrl() { return imageUrl; }
  public ProductStatus getStatus() { return status; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
