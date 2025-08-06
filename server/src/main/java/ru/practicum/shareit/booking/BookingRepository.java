package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItemIdIn(List<Long> itemIds);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "AND b.item.id = :itemId " +
            "AND b.status = :status")
    List<Booking> findBookingsForCommentCheck(
            @Param("bookerId") Long bookerId,
            @Param("itemId") Long itemId,
            @Param("status") Status status
    );

    Page<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    Page<Booking> findCurrentByItemOwner(@Param("ownerId") Long ownerId,
                                         @Param("now") LocalDateTime now,
                                         Pageable pageable);

    Page<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId,
                                                                LocalDateTime end,
                                                                Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId,
                                                                 LocalDateTime start,
                                                                 Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId,
                                                             Status status,
                                                             Pageable pageable);

    Page<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND :now BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    Page<Booking> findCurrentByBooker(@Param("userId") Long userId,
                                      @Param("now") LocalDateTime now,
                                      Pageable pageable);

    Page<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId,
                                                             LocalDateTime end,
                                                             Pageable pageable);

    Page<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId,
                                                              LocalDateTime start,
                                                              Pageable pageable);

    Page<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId,
                                                          Status status,
                                                          Pageable pageable);
}