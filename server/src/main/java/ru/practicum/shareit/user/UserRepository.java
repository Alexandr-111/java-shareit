package ru.practicum.shareit.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM Item i WHERE i.owner.id = :userId) THEN TRUE ELSE FALSE END")
    boolean existsByIdAndItemsIsNotEmpty(@Param("userId") Long userId);

    Page<User> findAll(Pageable pageable);
}