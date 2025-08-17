package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items " +
            "WHERE ir.requestor.id = :requestorId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findByRequestorIdWithItems(@Param("requestorId") Long requestorId);

    @EntityGraph(attributePaths = {"items"})
    Page<ItemRequest> findAllByOrderByCreatedDesc(Pageable pageable);

    @Query("SELECT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items " +
            "WHERE ir.id = :id")
    Optional<ItemRequest> findByIdWithItems(@Param("id") Long id);
}