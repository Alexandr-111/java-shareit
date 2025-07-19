package ru.practicum.shareit.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users", schema = "public")
@Getter
@Setter
@ToString(exclude = {"items", "requests", "bookings"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Builder.Default
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "requestor", fetch = FetchType.LAZY)
    private List<ItemRequest> requests = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booker", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}