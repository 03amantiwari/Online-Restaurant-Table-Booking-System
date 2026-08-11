package com.backend.restaurantTable.entity;

import com.backend.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "restaurant_table",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_restaurant_table_number",
                columnNames = {"restaurant_id", "table_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "restaurant")
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_restaurant"))
    private Restaurant restaurant;

    @NotNull
    @Column(name = "table_number", nullable = false)
    private Integer tableNumber;

    @NotNull
    @Positive
    @Column(name = "seating_capacity", nullable = false)
    private Integer seatingCapacity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    private LocationType locationType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "table_status", nullable = false, length = 20)
    private TableStatus tableStatus;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "rate_per_seat",nullable = false)
    private double ratePerSeat;

    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        if (this.tableStatus == null) {
            this.tableStatus = TableStatus.AVAILABLE;
        }
    }
    


    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public enum LocationType {
        INDOOR,
        OUTDOOR,
        WINDOW,
        ROOFTOP,
        PRIVATE
    }

    public enum TableStatus {
        AVAILABLE,
        RESERVED,
        OCCUPIED,
        UNAVAILABLE
    }
}