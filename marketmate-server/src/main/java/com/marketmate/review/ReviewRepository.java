package com.marketmate.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByPlaceIdOrderByCreatedAtDesc(String placeId);

    long countByPlaceId(String placeId);

    @Query("select coalesce(avg(r.rating),0) from Review r where r.placeId=:placeId")
    double avg(@Param("placeId") String placeId);
}
